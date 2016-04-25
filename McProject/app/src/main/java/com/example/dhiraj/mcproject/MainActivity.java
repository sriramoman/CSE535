package com.example.dhiraj.mcproject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;

import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.view.View;

import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
//import android.widget.Toast;


import java.util.ArrayList;
import java.util.LinkedHashMap;


public class MainActivity extends Activity
{
    LinearLayout layout;
    VisualizerView mVisualizer;
    private ArrayList <String> hooks = new ArrayList<String>();
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    LocationManager locationManager;
    boolean isGPSEnabled = false;
    long starttime = 0;
    // flag for network status
    boolean isNetworkEnabled = false;
    Messenger mService = null;
    EditText hookedText = null;
    long hookTime;
    int hookOn = 0;
    TextView voiceToText;
    private String m_chosenDir = "";
    boolean mBound;
    private String hookString = "";
    MyReceiver myReceiver;
    private ProgressBar level;
    private LinkedHashMap<Number,Number> mapLevels;
    String hookText;
    private String filename = "";
    int playmode = 0;
    private String curPath;
    //</editor-fold>
    @Override

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        filename = getIntent().getStringExtra("filename");
        curPath=getIntent().getStringExtra("curFolder");
        Log.e(LOG_TAG, "path is "+curPath);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled) {
            showSettingsAlert();
        }

        layout = (LinearLayout) findViewById(R.id.lay);
        mVisualizer = new VisualizerView(this);
        layout.addView(mVisualizer);

        //<editor-fold desc="svellangGraph">
        //level = (ProgressBar) findViewById(R.id.progressbar_level);
        //level.setProgress(500);
        mapLevels=new LinkedHashMap<>();
        Intent serviceIntent;
        //</editor-fold>

        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RecordService.RecordServiceAmplitude);
        registerReceiver(myReceiver, intentFilter);
        serviceIntent = new Intent(MainActivity.this.getBaseContext(), RecordService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        voiceToText = (TextView) findViewById(R.id.textFromSpeech);
        final Button startBtn = (Button) findViewById(R.id.startBtn);
        startBtn.setEnabled(true);
        final Button stopBtn = (Button) findViewById(R.id.stopBtn);
        final Button hookBtn = (Button) findViewById(R.id.hookBtn);
        if (isMyServiceRunning(RecordService.class))
        {
//            Toast.makeText(getBaseContext(),"Service is running",Toast.LENGTH_SHORT).show();
            if(RecordService.recordingOn == 1) {
                filename = RecordService.recordingPath;
                starttime = RecordService.starttime;
                startBtn.setEnabled(false);
                stopBtn.setEnabled(true);
                hookBtn.setEnabled(true);
            }
        }
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playmode == 0){
                    startBtn.setEnabled(false);
                    hookBtn.setEnabled(true);
                    starttime = System.currentTimeMillis();
                    startRecord(v);
                    stopBtn.setEnabled(true);
                    //promptSpeechInput();
                }

                else{
                    Intent intent = new Intent(MainActivity.this, PlaybackActivity.class).putExtra("filename",filename+".drs");
                    intent.putExtra("startTime", String.valueOf(starttime));
                    startActivity(intent);

                }

            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBtn.setEnabled(true);
                startBtn.setText("Play");
                playmode = 1;
                stopRecord(v);
                stopBtn.setEnabled(false);
                hookBtn.setEnabled(false);
            }
        });

        hookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    hookTime =  System.currentTimeMillis();
                    sendHooktime(hookTime);
                    Log.d("Hook", " " + hookTime);
                    getHook();

            }
        });

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mBound = false;
        }
    };
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void startRecord(View v) {
        if (!mBound) return;
        Bundle b = new Bundle();
        String filePath = filename;
        b.putString("str1", filePath);
        b.putString("curPath",curPath);
        Message msg = Message.obtain(null, 1);
        msg.setData(b);
        // Create and send a message to the service, using a supported 'what' value
        //Message msg = Message.obtain(null, MessengerService.MSG_SAY_HELLO, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord(View v) {
        if (!mBound) return;
        Bundle b = new Bundle();
        b.putString("str1", "ab" + "hi" + "cd");
        Message msg = Message.obtain(null, 2);
        msg.setData(b);
        //msg.replyTo = mMessenger;

        // Create and send a message to the service, using a supported 'what' value
        //Message msg = Message.obtain(null, MessengerService.MSG_SAY_HELLO, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //saveHooks();


    }




    private void hook() {
        if(hookOn == 0){
            sendHooks(hookText);
//            Toast.makeText(getBaseContext(), "Hooked it", Toast.LENGTH_SHORT).show();
        }

        else{
//            Toast.makeText(getBaseContext(), "Not hooking",Toast.LENGTH_SHORT).show();

        }

    }

    private void sendHooks(String s) {
        if (!mBound) return;
        Bundle b = new Bundle();
        b.putString("str1", s);
        Message msg = Message.obtain(null, 3);
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    private void sendHooktime(long t) {
        if (!mBound) return;
        Bundle b = new Bundle();
        b.putLong("str1", t);
        Message msg = Message.obtain(null, 4);
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void getHook()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Text To Hook");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        //input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hookText = input.getText().toString();
                hookOn = 0;
                hook();
            }

        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hookOn = 1;
                dialog.cancel();
            }
        });

        builder.show();
    }



    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    //<editor-fold desc="svellangGraph">
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            int datapassed = arg1.getIntExtra("RECORD_SERVICE_AMPLITUDE", 0);
            layout.removeAllViews();
            mVisualizer.updateVisualizer(datapassed / 20);
            layout.addView(mVisualizer);
            //level.setProgress(datapassed);
            long timeNow=System.currentTimeMillis() - starttime;
            //Log.d("Receiver", String.valueOf(timeNow));
            mapLevels.put(timeNow, datapassed);
        }
    }




}
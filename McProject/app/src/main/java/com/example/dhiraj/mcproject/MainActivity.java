package com.example.dhiraj.mcproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;

import android.os.Bundle;

import android.widget.Button;
import android.view.View;

import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class MainActivity extends Activity
{
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
    private boolean m_newFolderEnabled = true;
    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;
    private String hookString = "";
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled) {
            showSettingsAlert();
        }
        setContentView(R.layout.activity_main);
        Intent serviceIntent;
        serviceIntent = new Intent(MainActivity.this.getBaseContext(), RecordService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection,
                Context.BIND_AUTO_CREATE);
        hookedText =(EditText) findViewById(R.id.hookedText);
        voiceToText = (TextView) findViewById(R.id.textFromSpeech);
        final Button startBtn = (Button) findViewById(R.id.startBtn);
        final Button stopBtn = (Button) findViewById(R.id.stopBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hookedText.setText("");
                startBtn.setEnabled(false);
                startRecord(v);
                stopBtn.setEnabled(true);
                //promptSpeechInput();
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBtn.setEnabled(true);
                starttime = System.currentTimeMillis();
                stopRecord(v);
                stopBtn.setEnabled(false);
            }
        });
        final Button hookBtn = (Button) findViewById(R.id.hookBtn);
        hookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hookOn == 0)
                {
                    hookTime =  System.currentTimeMillis() - starttime ;
                    hookedText.setEnabled(true);
                    hookedText.requestFocus();
                    hookBtn.setText("Save");
                    hookOn = 1;

                }
                else
                {
                    hook(v);
                    hookedText.setEnabled(false);
                    hookBtn.setText("Hook");
                    hookBtn.requestFocus();
                    hookOn = 0;
                }


            }


        });
        Button dirChooserButton = (Button) findViewById(R.id.chooseDirButton);
        dirChooserButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                // Create DirectoryChooserDialog and register a callback
                DirectoryChooserDialog directoryChooserDialog =
                        new DirectoryChooserDialog(MainActivity.this,
                                new DirectoryChooserDialog.ChosenDirectoryListener() {
                                    @Override
                                    public void onChosenDir(String chosenDir) {
                                        m_chosenDir = chosenDir;
                                       /* Toast.makeText(
                                                MainActivity.this, "Chosen directory: " +
                                                        chosenDir, Toast.LENGTH_LONG).show();*/
                                        askFilename();
                                        Toast.makeText(
                                                MainActivity.this, "Chosen directory + file: " +
                                                        chosenDir + hookString, Toast.LENGTH_LONG).show();
                                    }

                                });
                // Toggle new folder button enabling
                directoryChooserDialog.setNewFolderEnabled(m_newFolderEnabled);
                // Load directory chooser dialog for initial 'm_chosenDir' directory.
                // The registered callback will be called upon final directory selection.
                directoryChooserDialog.chooseDirectory();
                //m_newFolderEnabled = !m_newFolderEnabled;
                startBtn.setEnabled(true);
            }
        });
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    public void startRecord(View v) {
        if (!mBound) return;
        Bundle b = new Bundle();
        String filePath = m_chosenDir + File.separator + hookString + ".3gp" ;
        b.putString("str1", filePath);
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
        saveHooks();
    }


    private void askFilename()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Title");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        //input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hookString = input.getText().toString();
            }

        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void hook(View v) {
        Toast.makeText(
                MainActivity.this, "Chosen directory + file: " +
                        m_chosenDir + hookString, Toast.LENGTH_LONG).show();
        String s = hookTime + " : " + hookedText.getText().toString();
        hooks.add(s);
        Toast.makeText(getBaseContext(),
                "Hooked it",
                Toast.LENGTH_SHORT).show();
        hookedText.setText("");

    }
    private void saveHooks() {
        try {
            File myFile = new File(m_chosenDir + File.separator+ hookString +".txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            for (String s : hooks){
                myOutWriter.append(s + "\n");
            }

            myOutWriter.close();
            fOut.close();
            Toast.makeText(getBaseContext(),
                    "Done writing SD 'mysdfile.txt'",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
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

    /*private final int REQ_CODE_SPEECH_INPUT = 100;
    protected static final int RESULT_SPEECH = 1;
    private void promptSpeechInput() {
        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        try {
            startActivityForResult(intent, RESULT_SPEECH);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn\'t support speech input",
                    Toast.LENGTH_SHORT).show();
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(getApplicationContext(),
               "inside onactivitresult",
                Toast.LENGTH_SHORT).show();

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    voiceToText.setText(result.get(0));
                    Toast.makeText(getApplicationContext(),
                            "out is"+ result.get(0),
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "not okay",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:   Toast.makeText(getApplicationContext(),
                    "not request code",
                    Toast.LENGTH_SHORT).show();

        }
    }*/
}
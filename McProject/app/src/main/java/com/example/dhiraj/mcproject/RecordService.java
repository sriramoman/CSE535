package com.example.dhiraj.mcproject;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RecordService extends Service implements LocationListener {
    LocationManager locationManager;
    // flag for GPS status
    boolean isGPSEnabled = false;
    private ArrayList<String> dataGps = new ArrayList<String>();
    private LinkedHashMap <Number,String> hooks;
    private ProgressBar level;
    private LinkedHashMap<Number,Number> mapLevels;
//    private String ampList;
    // flag for network status
    boolean isNetworkEnabled = false;
    public static String recordingPath;
    private static String mFileName = null;
    // flag for GPS status
    boolean canGetLocation = false;
    SQLiteDatabase db;
    public float startTimeHrs;
    public static final String DATABASE_NAME = "svellangDatabase";
    public static final String DATABASE_LOCATION = Environment.getExternalStorageDirectory() + File.separator + "Mydata" + File.separator + DATABASE_NAME;
    public static String TABLE = "Recording";
    long starttime = 0;
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    public static int recordingOn = 0;
    public long hookTime;
    double latitudeStart; // latitude
    double longitudeStart; // longitude
    String cityStart;
    double longitudeEnd; // longitude
    double latitudeEnd; // latitude
    String cityEnd;
    private String finalFileName;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    Notification recordingNotify;
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0; // 1 minute
    private MediaRecorder mRecorder = null;
    private static final String LOG_TAG = "AudioRecordTestt";
    //<editor-fold desc="svellangGraph">
//    private int lastLevel = 0;
    private Handler handler = new Handler();
    final static String RecordServiceAmplitude = "RecordServiceAmplitude";
    String filename;
    //</editor-fold>
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String s = msg.getData().getString("str1");
                    filename = s;
//                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                    startRecording(s);
                    //startRecord(msg);
                    break;
                case 2:
                    String st = msg.getData().getString("str1");
//                    Toast.makeText(getApplicationContext(), st, Toast.LENGTH_SHORT).show();
                    stopRecording();
                    saveHooks();
                    break;
                case 3:
                    String h = msg.getData().getString("str1");

//                    Toast.makeText(getApplicationContext(),"hooked text" + h, Toast.LENGTH_SHORT).show();
//                    String hookst = hookTime + ":" + h;
//                    hooks.add(hookst);
                    Log.e("HookgettingAdded",hookTime + ":" + h);

                    hooks.put(hookTime,h);
                case 4:
                    hookTime = msg.getData().getLong("str1") - starttime;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    private void saveHooks() {
        try {
            File myFile = new File(filename +"$.txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            ObjectOutputStream s = new ObjectOutputStream(fOut);
            s.writeObject(hooks);
            s.close();
            hooks.clear();
            fOut.close();

            //<editor-fold desc="svellangGraph">
            myFile = new File(filename +"~.txt");
            myFile.createNewFile();
            fOut = new FileOutputStream(myFile);
            s = new ObjectOutputStream(fOut);
            s.writeObject(mapLevels);
            s.close();
            fOut.close();
            mapLevels.clear();
            //</editor-fold>
            String[] allFiles = new String[3];
            allFiles[0] = filename + ".3gp";
            allFiles[1] = filename + "$.txt";
            allFiles[2] = filename + "~.txt";

            Compress compress = new Compress(allFiles,filename);
            compress.zip();
            compress.deleteFiles(allFiles);

//            Toast.makeText(getBaseContext(),
//                    "Done writing SD 'mysdfile.txt'",
//                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
//            Toast.makeText(getBaseContext(), e.getMessage(),
//                    Toast.LENGTH_SHORT).show();
        }
    }


    private void startRecording(String mFileName) {
        getLocation();
        recordingPath = mFileName;
        if(location != null){
            latitudeStart = latitude;
            longitudeStart = longitude;
            cityStart = getLocationName(latitudeStart, longitudeStart);
            dataGps.add(cityStart);
            Log.e(LOG_TAG, cityStart);

        }


        Date dateStart = new Date();
        String timestampStart = dateStart.toString();
        Log.e(LOG_TAG, timestampStart);

        System.out.print(mFileName);
        Log.e(LOG_TAG, mFileName);

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName + ".3gp");
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        finalFileName = mFileName;

        //<editor-fold desc="svellangGraph">
        hooks = new LinkedHashMap<>();
        mapLevels=new LinkedHashMap<>();
        handler.removeCallbacks(update);
        handler.postDelayed(update, 25);
        //</editor-fold>

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        recordingOn = 1;
        createNotification();
        starttime = System.currentTimeMillis();
        mRecorder.start();

        //promptSpeechInput();

    }
    private void stopRecording() {
        getLocation();
        if (location != null) {
            latitudeEnd = latitude;
            longitudeEnd = longitude;
            cityEnd = getLocationName(latitudeEnd, longitudeEnd);
            dataGps.add(cityEnd);
            Log.e(LOG_TAG, cityEnd);
        }

        handler.removeCallbacks(update);
        mRecorder.stop();
        mRecorder.release();

        insertRecord(cityStart, cityEnd, finalFileName, getstart(starttime));
        Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
        mRecorder = null;
        recordingOn = 0;
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(123);
        checkMobility();

    }

    private float getstart(long starttime) {


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(starttime);
        Date time = calendar.getTime();
        float hrs = time.getHours();
        float min = time.getMinutes();
        float hrmins = hrs + (min/100);
        String t = " " + min/100 + " " + hrmins;
        Log.e(LOG_TAG, t);
        return hrmins;
    }


    final Messenger mMessenger = new Messenger(new IncomingHandler());
    @Override
    public IBinder onBind(Intent intent) {
        System.out.print("hello start");
//        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        createDB();
        getLocation();

        MyTimerTask myTask = new MyTimerTask();
        Timer myTimer = new Timer();

        myTimer.schedule(myTask, 0, 60*15000);
        super.onCreate();
    }


    //<editor-fold desc="Locations">
    public void getLocation() {
        try {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {

                Log.e("Got GPS Location as", "Trying to get location" );
                this.canGetLocation = true;
                // First get location from Network Provider
                // if GPS Enabled get lat/long using GPS Services
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.e("Got network Location as", " " +  latitude + " " + longitude );
                            }
                        }
                    }

                }
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            Log.d("GPS Enabled", "location manger found");
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {

                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.e("Got GPS Location as", " " +  latitude + " " + longitude );
                            }
                        }
                    }
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public String getLocationName(double lattitude, double longitude) {

        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {

            List<Address> addresses = gcd.getFromLocation(lattitude, longitude,
                    10);

            for (Address adrs : addresses) {
                if (adrs != null) {

                    String city = adrs.getLocality();
                    if (city != null && !city.equals("")) {
                        cityName = city;
                        System.out.println("city ::  " + cityName);
                    } else {

                    }
                    // // you should also try with addresses.get(0).toSring();

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;

    }


    @Override
    public void onLocationChanged(Location location) {

    }
    //</editor-fold>

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //<editor-fold desc="svellangGraph">
    private Runnable update = new Runnable() {
        public void run() {
            Intent intent = new Intent();
            intent.setAction(RecordServiceAmplitude);
            int m=mRecorder.getMaxAmplitude();
            intent.putExtra("RECORD_SERVICE_AMPLITUDE", m);
            long timeNow=System.currentTimeMillis() - starttime;

            //Log.d("Receiver", String.valueOf(timeNow));
//            double db=20 * Math.log10( m/ 2700.0);
//            mapLevels.put(timeNow, Double.isInfinite(db) ? 0 : Math.abs(db));

            mapLevels.put(timeNow,m);
            sendBroadcast(intent);
            handler.postAtTime(this, SystemClock.uptimeMillis() + 300);
        }
    };

    private void createDB(){
        try {
            db = SQLiteDatabase.openOrCreateDatabase(DATABASE_LOCATION, null);
            db.beginTransaction();
            try {

                //perform your database operations here ...
                db.execSQL("create table " + TABLE + " ("
                        + " created_at DATETIME DEFAULT CURRENT_TIMESTAMP, "
                        + " Filename Text, "
                        + "startTime Real, "
                        + " longitudeStart Real, "
                        + " latitudeStart Real, "
                        + " StartCity Text, "
                        + " longitudeEnd Real, "
                        + " latitudeEnd Real, "
                        + " EndCity Text, "
                        + " Time float" +
                        " ); ");

                db.setTransactionSuccessful(); //commit your changes
//                Toast.makeText(this, "db and table created", Toast.LENGTH_LONG).show();
            } catch (SQLiteException e) {
                //report problem
            } finally {
                db.endTransaction();
            }
        } catch (SQLException e) {

//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
    private void insertRecord(String cityStart, String cityEnd, String finalFileName, float time){

        try {
            //perform your database operations here ...
            db.execSQL("insert into " + TABLE + " (Filename,startTime,longitudeStart,latitudeStart,StartCity,longitudeEnd,latitudeEnd,EndCity,Time) values " +
                    "('" + finalFileName
                    + "', '" + starttime
                    + "', '" + longitudeStart
                    + "', '" + latitudeStart
                    + "', '" + cityStart
                    + "', '" + longitudeEnd
                    + "', '" + latitudeEnd
                    + "', '" + cityEnd
                    + "', '" + time
                    + "' );");
            //db.setTransactionSuccessful(); //commit your changes
        } catch (SQLiteException e) {
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            //db.endTransaction();
        }

    }
    public void createNotification() {
        // Prepare intent which is triggered if the
        // notification is selected
        System.out.print("inside notidication class");
//        Toast.makeText(this, "I am notifiying seeeeeeee", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this,0,intent,0);

        // Build notification
        // Actions are just fake
        recordingNotify = new Notification.Builder(this)
                .setContentTitle("Intelligent recorder ")
                .setContentText("Recording on")
                .setContentIntent(pIntent)
                .setSmallIcon(R.drawable.ic)
                .build();
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(123, recordingNotify);

    }
    public void checkMobility(){
        long currentTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        Date time = calendar.getTime();
        float hrs = time.getHours();
        float min = time.getMinutes();
        float hrmins = hrs + (min/100);
        String t = " " + min/100 + " " + hrmins;
        //Log.e(LOG_TAG, t);
        float endhrsmins = hrmins + .15f;
        String query = "Select count(*) from Recording where "+
                "Time >= " + hrmins + " and Time < " + endhrsmins;
        //db.execSQL(query);
        Cursor mCount = db.rawQuery(query, null);

        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        //db.close();
        //Log.e(LOG_TAG, " the count is " + count);
        //Toast.makeText(this, "Checking if we want to notify" + count,Toast.LENGTH_LONG).show();
        if (count >=3){

            generateNotification(getApplicationContext(), "Wanna Start Recording" + hrmins);
        }

    }
    //</editor-fold>
    class MyTimerTask extends TimerTask {
        public void run() {
            checkMobility();
        }
    }

    private void generateNotification(Context context, String message) {

        int icon = R.drawable.ic;
        long when = System.currentTimeMillis();
        String appname = context.getResources().getString(R.string.app_name);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        Notification notification;
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        // To support 2.3 os, we use "Notification" class and 3.0+ os will use
        // "NotificationCompat.Builder" class.

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context);
        notification = builder.setContentIntent(contentIntent)
                .setSmallIcon(icon).setTicker(appname).setWhen(0)
                .setAutoCancel(true).setContentTitle(appname)
                .setContentText(message).build();

        notificationManager.notify((int) when, notification);



    }
}

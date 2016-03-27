package com.example.dhiraj.mcproject;

import android.Manifest;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class RecordService extends Service implements LocationListener {
    LocationManager locationManager;
    // flag for GPS status
    boolean isGPSEnabled = false;
    private ArrayList<String> dataGps = new ArrayList<String>();
    // flag for network status
    boolean isNetworkEnabled = false;
    private static String mFileName = null;
    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude


    double latitudeStart; // latitude
    double longitudeStart; // longitude
    String cityStart;
    double longitudeEnd; // longitude
    double latitudeEnd; // latitude
    String cityEnd;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private MediaRecorder mRecorder = null;
    private static final String LOG_TAG = "AudioRecordTestt";
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String s = msg.getData().getString("str1");
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                    startRecording(s);
                    //startRecord(msg);
                    break;
                case 2:
                    String st = msg.getData().getString("str1");
                    Toast.makeText(getApplicationContext(), st, Toast.LENGTH_SHORT).show();
                    stopRecording();
                    //startRecord(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void startRecording(String mFileName) {
        /*mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest1.3gp";*/
        Location startLocation = getLocation();
        latitudeStart = startLocation.getLatitude();
        longitudeStart = startLocation.getLongitude();
        cityStart = getLocationName(latitudeStart, longitudeStart);
        dataGps.add(cityStart);

        Date dateStart = new Date();
        String timestampStart = dateStart.toString();
        Log.e(LOG_TAG,timestampStart );
        Log.e(LOG_TAG, cityStart);
        System.out.print(mFileName);
        Log.e(LOG_TAG, mFileName);
        //Calendar.get(Calendar.DATE);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
        //promptSpeechInput();

    }
    private void stopRecording() {
        Location startLocation = getLocation();
        latitudeEnd = startLocation.getLatitude();
        longitudeEnd = startLocation.getLongitude();
        cityEnd = getLocationName(latitudeEnd, longitudeEnd);
        dataGps.add(cityEnd);
        Log.e(LOG_TAG, cityEnd);
        mRecorder.stop();
        mRecorder.release();
        Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
        mRecorder = null;
    }


    final Messenger mMessenger = new Messenger(new IncomingHandler());
    @Override
    public IBinder onBind(Intent intent) {
        System.out.print("hello start");
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {

        super.onCreate();
    }


    //Locations
    public Location getLocation() {
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
                this.canGetLocation = true;
                // First get location from Network Provider
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
                            }
                        }
                    }

                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
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

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}

package com.example.dhiraj.mcproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class GoogleMapView extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    HashMap<String,ArrayList<Double>> llMap;
    HashMap<String,String> fileMap = new HashMap<>();
    public static final String DATABASE_NAME = "svellangDatabase";
    public static final String DATABASE_LOCATION = Environment.getExternalStorageDirectory() + File.separator + "Mydata" + File.separator + DATABASE_NAME;
    SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_LOCATION, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        llMap = (HashMap<String,ArrayList<Double>>)intent.getSerializableExtra("map");
        Log.i("In GoogleMaps", llMap+" ");

        setContentView(R.layout.activity_google_map_view);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng place = new LatLng(0,0);
        Marker marker;

        String filePath = null;
        String fileName = null;
        int pos;
        double lat=10.0;
        double lon=10.0;

        int i=0;

        Set<String> set = llMap.keySet();
        Iterator<String> itr = set.iterator();
        while(itr.hasNext()){
            filePath = itr.next();
            pos = filePath.lastIndexOf("/")+1;
            fileName = filePath.substring(pos, filePath.length());
            //Log.i("file related ",fileName+" : "+filePath+" is map");
            fileMap.put(filePath, fileName);
            //Log.i("onMapReady ",fileMap+" is map");
            lat = llMap.get(filePath).get(0);
            lon = llMap.get(filePath).get(1);
            place = new LatLng(lat, lon);
            //i = i+5;
            marker = mMap.addMarker(new MarkerOptions().position(place).title(fileName));
        }


        /*for(int i=0,j=0; i<10; i++,j++){
            place = new LatLng(lat*i, lon*i);
            marker = mMap.addMarker(new MarkerOptions().position(place).title("Marker with i "+i));
        }*/

        //nrp = new LatLng(14.59, 79.59);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            mMap.setMyLocationEnabled(true);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker arg0) {
                //if(arg0.getTitle().equals("Marker in NRP")) // if marker source is clicked
                Toast.makeText(GoogleMapView.this, arg0.getTitle(), Toast.LENGTH_SHORT).show();// display toast
                String markerName = arg0.getTitle();
                String markerTempPath = getKeyByValue(markerName);
                String markerPath = markerTempPath;
                Log.i("Marker Path ",markerTempPath+" ");
                String query = "Select startTime from Recording where Filename = '" + markerPath + "'";
                Log.i("Marker query ",query+" ");
                /*Cursor cursor = db.rawQuery(query, null);
                String time1= null;
                if(cursor != null){
                    while(cursor.moveToNext()){
                        Log.i("Marker Path ", "cursor found somethjuing" + " ");
                        time1 = cursor.getString(cursor.getColumnIndex("startTime"));
                    }

                }
                Log.i("Marker Path ", time1 + " ");
                long time = Long.parseLong(time1);
                cursor.close();*/


                Cursor cursor = db.rawQuery(query, null);
                Long time = null;
                if (cursor != null && cursor.moveToFirst()) {
                    Log.i("Marker Path ", "cursor found somethjuing" + " ");
                    time = cursor.getLong(cursor.getColumnIndex("startTime"));
                }
                Log.i("Marker Path ", time + " ");
                cursor.close();


                markerPath = markerTempPath+".drs";
                Intent intent = new Intent(GoogleMapView.this, PlaybackActivity.class);
                intent.putExtra("filename", markerPath);
                intent.putExtra("startTime", String.valueOf(time));
                startActivity(intent);
                return true;
            }
        });
    }

    String getKeyByValue(String name){
        for (Map.Entry<String, String> entry : fileMap.entrySet()) {
            if (name.compareTo(entry.getValue()) == 0)
                return entry.getKey();
        }
        return null;
    }
}

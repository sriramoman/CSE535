package com.example.dhiraj.mcproject;

/**
 * Created by RK on 4/17/2016.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FolderView extends AppCompatActivity{

    private List<String> fileList = new ArrayList<String>();
    private List<String> filePath = new ArrayList<String>();
    private HashMap<String,String> fileMap = new HashMap<String, String>();
    private HashMap<String,Long> filesByTime = new HashMap<String,Long>();
    private HashMap<String,ArrayList<Double>> googleHashMap = new HashMap<String,ArrayList<Double>>();
    private boolean blRename;
    Button buttonUp;
    Button buttonSort;
    //svellang
    Button buttonRename;
    private int lastChoice;

    ListView listView;
    public static final String DATABASE_NAME = "svellangDatabase";
    public static final String DATABASE_LOCATION = Environment.getExternalStorageDirectory() + File.separator + "Mydata" + File.separator + DATABASE_NAME;
    SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_LOCATION, null);

    static final int CUSTOM_DIALOG_ID = 0;
    File root;
    File curFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_view);
        getSupportActionBar().setTitle("Tags");
        blRename=false;
        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mydata");
        curFolder = root;

        buttonRename = (Button)findViewById(R.id.btnrename);
        buttonRename.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setBlRename(!blRename);
            }
        });

        // Button to reach the parent directory
        buttonUp = (Button) findViewById(R.id.up);
        buttonUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(curFolder == root);
                else{
                    curFolder = curFolder.getParentFile();
                    ListDir(curFolder, -1);
                }
            }
        });

        // Listing all the files present in the directory
        FileListing(-1);

        // Button to sort the files as required
        final String[] options = {"Name","Time","Location"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, options);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Sorting Option");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 2){
                    Intent intent = new Intent(FolderView.this, GoogleMapView.class);
                    intent.putExtra("map",getGoogleHashMap());
                    startActivity(intent);
                }
                else{
                    FileListing(which);
                    //Log.i("This choice", which + " is inside while loop");
                    Toast.makeText(FolderView.this, "Sorted", Toast.LENGTH_LONG).show();
                }
            }
        });
        final AlertDialog alert = builder.create();
        buttonSort = (Button) findViewById(R.id.sort);
        buttonSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.show();
            }
        });
    }

    /**
     * Setter method for blRename (Heavy)
     * Param: Boolean
     */
    public void setBlRename(boolean blVal){
        blRename=blVal;
        if (blRename) {
            buttonRename.setText("Cancel");
            if (curFolder.compareTo(root)==0)
                getSupportActionBar().setTitle("Tap on tag to rename");
            else
                getSupportActionBar().setTitle("Tap on recording to rename");
        }
        else {
            buttonRename.setText("Rename");
            if (curFolder.compareTo(root)==0)
                getSupportActionBar().setTitle("Tags");
            else
                getSupportActionBar().setTitle(curFolder.getAbsolutePath().substring(curFolder.getAbsolutePath().lastIndexOf("/") + 1) + " > Recordings");
        }
    }

    void FileListing(final int choice){
        listView = (ListView) findViewById(R.id.listView);

        if(curFolder.compareTo(root)==0 && choice == 1)
            Toast.makeText(FolderView.this, "Folder sorting by time is not allowed", Toast.LENGTH_LONG).show();
        else{
            ListDir(curFolder, choice);

            //Log.i("This choice", choice + " is inside FileListing");
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Log.i("map",fileMap+" is this");
                    File selected = new File(fileMap.get(fileList.get(position)));
                    if (blRename)
                        rename(selected);
                    else {
                        if (selected.isDirectory()) {
                            ListDir(selected, choice);
                        } else {
                            String query = "Select startTime from Recording where Filename = '" + selected.toString().substring(0, selected.toString().length() - 4) + "'";
                            Cursor cursor = db.rawQuery(query, null);
                            Long time = null;
                            if (cursor != null && cursor.moveToFirst()) {
                                time = cursor.getLong(cursor.getColumnIndex("startTime"));
                            }
                            cursor.close();
                            //Log.i("startTime is ",time+" this");
                            Intent intent = new Intent(FolderView.this, PlaybackActivity.class);
                            intent.putExtra("filename", selected.toString());
                            intent.putExtra("startTime", String.valueOf(time));

                            startActivity(intent);
                        }
                    }
                }
            });
            //Log.i("root", root + " is the directory");
        }
    }

    void rename(final File selected){
        AlertDialog.Builder builder = new AlertDialog.Builder(FolderView.this);
        builder.setTitle("Rename");

        // Set up the input
        final EditText input = new EditText(FolderView.this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        //input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //<editor-fold desc="Rename">
                File renameTo;
                String ext=selected.isDirectory()?"":".drs";
                renameTo = new File(curFolder.toString()+"/"+input.getText().toString()+ext);
                selected.renameTo(renameTo);
                ListDir(curFolder,lastChoice);
                //TODO Rename files in database
                String srcVal = selected.getAbsolutePath().replace(ext,"");
                String query ="update Recording set Filename=replace(Filename,'"+srcVal+"','"+curFolder.toString()+"/"+input.getText().toString()+"')";
                Log.d("Q::", query);
                db.execSQL(query);
                setBlRename(!blRename);
            }

        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                setBlRename(!blRename);
            }
        });

        builder.show();
    }

    // Function to list files in the directory
    void ListDir(File f, int choice){
        lastChoice=choice;
        if(f.equals(root)) {
            if (blRename){
                getSupportActionBar().setTitle("Tap on tag to rename");
            }
            else
                getSupportActionBar().setTitle("Tags");
            buttonUp.setEnabled(false);
        }
        else {
            getSupportActionBar().setTitle(f.getPath().substring(f.getPath().lastIndexOf("/")+1)+" > Recordings");
            buttonUp.setEnabled(true);
        }

        curFolder = f;
        int pos;
        int lastPos;
//        textView.setText(f.getPath());

        File[] files = f.listFiles();
        fileMap.clear();
        fileList.clear();
        for (File file : files){
            pos = file.getPath().lastIndexOf("/")+1;
            if(file.isDirectory()){
                fileMap.put(file.getPath().substring(pos),file.getPath());
            }
            if(file.getPath().contains(".drs")){
                lastPos = file.getPath().indexOf(".drs");
                fileMap.put(file.getPath().substring(pos,lastPos),file.getPath());
            }
            //fileList.add(file.getPath());
        }
        fileList = new ArrayList<String>(fileMap.keySet());
        filePath = new ArrayList<String>(fileMap.values());
        //Log.i("This choice", choice + " is inside ListDir");
        if(choice == 0){
            Collections.sort(fileList);
        }
        if(choice == 1){
            filesByTime.clear();
            for(String str : filePath){
                //Log.i("filePath str is", str + " ");
                String query = "Select startTime from Recording where Filename = '" + str.substring(0,str.length()-4) + "'";
                Cursor cursor = db.rawQuery(query, null);
                Long time= null;
                if(cursor != null && cursor.moveToFirst()){
                    time = cursor.getLong(cursor.getColumnIndex("startTime"));
                }
                filesByTime.put(str, time);
                cursor.close();
            }
            //Log.i("This map", filesByTime + " is inside choice 1");
            fileList = sortMapByValues(filesByTime);
        }
        ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, fileList);

        listView.setAdapter(directoryList);
    }

    // Sorting the files based on the values in the HashMap
    List<String> sortMapByValues(HashMap<String, Long> input){
        List<String> al = new ArrayList<String>();
        Set<Map.Entry<String, Long>> set = input.entrySet();
        List<Map.Entry<String, Long>> list = new ArrayList<Map.Entry<String, Long>>(set);

        Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        for(Map.Entry<String, Long> entry:list){
            String file = entry.getKey();
            int place = file.lastIndexOf("/")+1;
            al.add(file.substring(place,file.length()-4));
        }
        return al;
    }

    // Collecting records with attributes FileName, Latitute and Longitude
    HashMap<String,ArrayList<Double>> getGoogleHashMap(){
        HashMap<String,ArrayList<Double>> ghm = new HashMap<>();
        ArrayList<Double> list = new ArrayList<>();
        String name = null;
        double lati = 10.0;
        double longi = 10.0;

        String query = "Select Filename, latitudeStart, longitudeStart from Recording";
        Cursor cursor = db.rawQuery(query, null);
        Log.i("Cursor count", cursor.getCount() + " width "+ cursor.getColumnCount()+" is inside getGoogleHashMap");
        if(cursor != null){
            while(cursor.moveToNext()){
                list = new ArrayList<>();
                name = cursor.getString(cursor.getColumnIndex("Filename"));
                lati = cursor.getInt(cursor.getColumnIndex("latitudeStart"));
                longi = cursor.getInt(cursor.getColumnIndex("longitudeStart"));
                Log.i("GHM latilongi are  ", lati+" and "+longi + " is inside getGoogleHashMap");
                list.add(lati);
                list.add(longi);
                //Log.i("GHM list are  ", list + " is inside getGoogleHashMap");
                ghm.put(name, list);
            }
        }
        //Log.i("GHM map is ", ghm + " is inside getGoogleHashMap");
        return ghm;
    }
}
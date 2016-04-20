package com.example.dhiraj.mcproject;

/**
 * Created by RK on 4/17/2016.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FolderView extends Activity{

    private List<String> fileList = new ArrayList<String>();
    private HashMap<String,String> fileMap = new HashMap<String, String>();
    Button buttonUp;
    Button buttonSort;
    TextView textView;
    ListView listView;

    static final int CUSTOM_DIALOG_ID = 0;
    File root;
    File curFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_view);

        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mydata");
        //root = new File("/storage/emulated/0/Mydata");
        curFolder = root;

        textView = (TextView) findViewById(R.id.folder);
        // Button to reach the parent directory
        buttonUp = (Button) findViewById(R.id.up);
        buttonUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(curFolder == root);
                else ListDir(curFolder.getParentFile(), -1);
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
                    startActivity(intent);
                }
                else{
                    FileListing(which);
                    //Log.i("This choice", which + " is inside while loop");
                    Toast.makeText(FolderView.this, "finally selected", Toast.LENGTH_LONG).show();
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

    void FileListing(final int choice){
        listView = (ListView) findViewById(R.id.listView);
        ListDir(root, choice);
        Log.i("This choice", choice + " is inside FileListing");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.i("map",fileMap+" is this");
                File selected = new File(fileMap.get(fileList.get(position)));
                if (selected.isDirectory())
                    ListDir(selected, choice);
                else {
                    // Toast.makeText(FolderView.this, selected.toString() + " selected", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(FolderView.this, PlaybackActivity.class).putExtra("filename", selected.toString());
                    startActivity(intent);
                }
            }
        });
        Log.i("root", root + " is the directory");
    }

    void ListDir(File f, int choice){
        if(f.equals(root))
            buttonUp.setEnabled(false);
        else
            buttonUp.setEnabled(true);

        curFolder = f;
        int pos;
        int lastPos;
        textView.setText(f.getPath());

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
        Log.i("This choice", choice + " is inside ListDir");
        if(choice == 0)
            Collections.sort(fileList);
        Log.i("This choice", fileList + " is inside ListDir");

        ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, fileList);

        listView.setAdapter(directoryList);
    }
}
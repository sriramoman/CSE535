package com.example.dhiraj.mcproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class HomeScreen extends AppCompatActivity {
    Button newRecording;
    Button viewRecording;
    private boolean m_newFolderEnabled = true;
    private String m_chosenDir = "";
    private String filename = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        getSupportActionBar().setTitle("Smart Voice Recorder | Dashboard");
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mydata");
        if (!folder.exists()) {
            folder.mkdir();
        }

        Button dirChooserButton = (Button) findViewById(R.id.newRecording);
        dirChooserButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                // Create DirectoryChooserDialog and register a callback
                DirectoryChooserDialog directoryChooserDialog =
                        new DirectoryChooserDialog(HomeScreen.this,
                                new DirectoryChooserDialog.ChosenDirectoryListener() {
                                    @Override
                                    public void onChosenDir(String chosenDir) {
                                        m_chosenDir = chosenDir;
                                       /* Toast.makeText(
                                                MainActivity.this, "Chosen directory: " +
                                                        chosenDir, Toast.LENGTH_LONG).show();*/
                                        askFilename();
//                                        Toast.makeText(
//                                                HomeScreen.this, "Chosen directory + file: " +
//                                                        chosenDir, Toast.LENGTH_LONG).show();
                                    }

                                });
                // Toggle new folder button enabling
                directoryChooserDialog.setNewFolderEnabled(m_newFolderEnabled);
                // Load directory chooser dialog for initial 'm_chosenDir' directory.
                // The registered callback will be called upon final directory selection.
                directoryChooserDialog.chooseDirectory();
                //m_newFolderEnabled = !m_newFolderEnabled;
                //startBtn.setEnabled(true);
            }
        });

        viewRecording = (Button) findViewById(R.id.viewRecording);
        viewRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreen.this, FolderView.class);
                startActivity(intent);
            }
        });

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
                filename = input.getText().toString();
                Intent intent = new Intent(HomeScreen.this, MainActivity.class).putExtra("filename",m_chosenDir + File.separator + filename);
                intent.putExtra("curFolder",m_chosenDir+File.separator);
                startActivity(intent);
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

}

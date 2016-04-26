package com.example.dhiraj.mcproject;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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

public class NumberLock extends AppCompatActivity {

    public static final String DATABASE_NAME = "svellangDatabase";
    public static final String DATABASE_LOCATION = Environment.getExternalStorageDirectory() + File.separator + "Mydata" + File.separator + DATABASE_NAME;
    public static String TABLE = "Password";
    SQLiteDatabase db;
    Button checkPassword;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createDB();
        insertRecord("user", 0000);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_lock);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        checkPassword = (Button) findViewById(R.id.check);
        editText = (EditText)findViewById(R.id.editText);
        String pass = editText.getEditableText().toString();
        checkPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int key = selectPassword();
                if(key == 0000){
                    Intent intent = new Intent(NumberLock.this, HomeScreen.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(NumberLock.this, "Incorrect Password", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void createDB(){
        try {
            db = SQLiteDatabase.openOrCreateDatabase(DATABASE_LOCATION, null);
            db.beginTransaction();
            try {

                //perform your database operations here ...
                db.execSQL("create table " + TABLE + " ("
                        + " user Text, "
                        + " key Real, "+
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

    private void insertRecord(String user, int pass){
        try {
            //perform your database operations here ...
            db.execSQL("insert into " + TABLE + " (user,key) values " +
                    "('" + user
                    + "', '" + pass
                    + "' );");
            //db.setTransactionSuccessful(); //commit your changes
        } catch (SQLiteException e) {
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            //db.endTransaction();
        }
    }

    private int selectPassword(){
        String query = "Select * from "+TABLE+" where "+
                "user = " + "user";
        //db.execSQL(query);
        Cursor mCount = db.rawQuery(query, null);

        mCount.moveToFirst();
        int key= mCount.getInt(0);
        mCount.close();

        return key;
    }

}

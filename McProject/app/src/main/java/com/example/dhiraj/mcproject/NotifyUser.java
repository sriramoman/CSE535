package com.example.dhiraj.mcproject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;


public class NotifyUser extends Service {
    Notification recordingNotify;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loadNotification();
    }

    private void loadNotification() {
        System.out.print("inside notidication class");
        Toast.makeText(this, "I am notifiying seeeeeeee", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this,0,intent,0);
        long when = System.currentTimeMillis();
        // Build notification
        // Actions are just fake
        recordingNotify = new Notification.Builder(this)
                .setContentTitle("Intelligent recorder ")
                .setContentText("Wanna record?")
                .setContentIntent(pIntent)
                .setSmallIcon(R.drawable.ic)
                .build();
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify((int) when, recordingNotify);


    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return 1;
    }

    public void onStart(Intent intent, int startId) {
        // TO DO
    }

    public IBinder onUnBind(Intent arg0) {
        // TO DO Auto-generated method
        return null;
    }

    public void onStop() {}

    public void onPause() {}

    @Override
    public void onDestroy() {}

    @Override
    public void onLowMemory() {}
}
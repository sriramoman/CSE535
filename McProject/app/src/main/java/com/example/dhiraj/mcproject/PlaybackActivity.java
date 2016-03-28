package com.example.dhiraj.mcproject;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.IOException;

public class PlaybackActivity extends AppCompatActivity {

    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();
    final MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar playBar;
    private Button btnStop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Button btnPlay = (Button)findViewById(R.id.btnPlay);
        btnStop = (Button)findViewById(R.id.btnStop);
        playBar = (SeekBar)findViewById(R.id.seekBar);
        playBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return !mediaPlayer.isPlaying();
            }
        });
        btnPlay.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                        }
                        else {
                            //warning will crash if u resume.
                            try {
                                final Uri myUri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mydata/test1/player.mp3");
                                mediaPlayer.setDataSource(getApplicationContext(), myUri);
                                mediaPlayer.prepareAsync();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                public void onPrepared(MediaPlayer player) {
                                    finalTime = mediaPlayer.getDuration();
                                    startTime = mediaPlayer.getCurrentPosition();
                                    Log.d("Player:", String.valueOf(finalTime));
                                    playBar.setMax((int) finalTime);
                                    playBar.setProgress((int) startTime);
                                    myHandler.postDelayed(UpdateSongTime, 100);
                                    player.start();
                                }
                            });
                        }
                    }
                }
        );
        btnStop.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopPlayer();
                    }
                }
        );
        playBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                        mediaPlayer.seekTo(seekBar.getProgress());
                        if (mediaPlayer.isPlaying())
                            myHandler.postDelayed(UpdateSongTime, 100);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                        myHandler.removeCallbacksAndMessages(null);
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // TODO Auto-generated method stub
//                        t1.setText(playBar.toString());
                    }
                }
        );
    }

    private void stopPlayer(){
        mediaPlayer.stop();
        mediaPlayer.reset();
        myHandler.removeCallbacksAndMessages(null);
        playBar.setProgress(0);
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            playBar.setProgress((int) startTime);
            myHandler.postDelayed(this, 100);
            if (!mediaPlayer.isPlaying())
                stopPlayer();
        }
    };
}

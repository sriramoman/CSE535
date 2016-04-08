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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.EntryXIndexComparator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class PlaybackActivity extends AppCompatActivity {

    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();
    final MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar playBar;
    private Button btnStop;
    private Button btnLoad;
    private BarChart amplitudeChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Button btnPlay = (Button)findViewById(R.id.btnPlay);
        btnStop = (Button)findViewById(R.id.btnStop);
        btnLoad = (Button)findViewById(R.id.btnLoad);
        playBar = (SeekBar)findViewById(R.id.seekBar);
        amplitudeChart = (BarChart)findViewById(R.id.chart);
        amplitudeChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return !mediaPlayer.isPlaying();
            }
        });
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
                        } else {
                            //warning will crash if u resume.
                            try {
                                final Uri myUri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mydata/mc/lq.3gp");
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

        btnLoad.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            loadStuff();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
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

    private void loadStuff() throws IOException, ClassNotFoundException {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mydata/mc/lq~.txt");
        FileInputStream f = new FileInputStream(file);
        ObjectInputStream s = new ObjectInputStream(f);
        LinkedHashMap<Number,Number> mapLevels  = (LinkedHashMap<Number,Number>) s.readObject();
        String ampList = mapLevels.toString().replaceAll(", ","\n").replaceAll("=",":").replaceAll("\\{","").replaceAll("\\}","");
        Log.d("Time", ampList);
        s.close();


        ArrayList<BarEntry> amplitudeList = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<String>();
        int i=0;
        for(Number time:mapLevels.keySet()){
            amplitudeList.add(new BarEntry(mapLevels.get(time).floatValue(),i++));
            xVals.add(mapLevels.get(time).toString());
        }
        BarDataSet setAmplitude = new BarDataSet(amplitudeList, "Amplitude");
//        setAmplitude.setAxisDependency(YAxis.AxisDependency.LEFT);
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(setAmplitude);

        BarData data = new BarData(xVals, dataSets);
        amplitudeChart.setData(data);
        amplitudeChart.invalidate();

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

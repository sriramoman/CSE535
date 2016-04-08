package com.example.dhiraj.mcproject;

import android.graphics.Color;
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
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class PlaybackActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();
    final MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar playBar;
    private Button btnStop;
    private Button btnLoad;
    private BarChart amplitudeChart;
    private ArrayList<String> xVals;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Button btnPlay = (Button)findViewById(R.id.btnPlay);
        btnStop = (Button)findViewById(R.id.btnStop);
        btnLoad = (Button)findViewById(R.id.btnLoad);
        playBar = (SeekBar)findViewById(R.id.seekBar);
        xVals = new ArrayList<>();
        //<editor-fold desc="Chart">
        amplitudeChart = (BarChart)findViewById(R.id.chart);
        //Appearance
        amplitudeChart.setDescription("");
        //Gesture configuration
        amplitudeChart.setScaleEnabled(false);
        amplitudeChart.setScaleXEnabled(false);
        amplitudeChart.setScaleYEnabled(false);
        amplitudeChart.setPinchZoom(false);
        amplitudeChart.setDoubleTapToZoomEnabled(false);
        amplitudeChart.setOnChartValueSelectedListener(this);
        amplitudeChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return !mediaPlayer.isPlaying();
            }
        });
        //</editor-fold>
        playBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return !mediaPlayer.isPlaying();
            }
        });
        //<editor-fold desc="UI Control callback methods">
        btnPlay.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                        } else {
                            //warning will crash if u resume.
                            try {
                                final Uri myUri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mydata/mc/lw.3gp");
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
        //</editor-fold>
    }

    private void loadStuff() throws IOException, ClassNotFoundException {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mydata/mc/lw~.txt");
        FileInputStream f = new FileInputStream(file);
        ObjectInputStream s = new ObjectInputStream(f);
        LinkedHashMap<Number,Number> mapLevels  = (LinkedHashMap<Number,Number>) s.readObject();
        String ampList = mapLevels.toString().replaceAll(", ","\n").replaceAll("=",":").replaceAll("\\{","").replaceAll("\\}","");
        Log.d("Time", ampList);
        s.close();
        f.close();

        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mydata/mc/lw$.txt");
        f = new FileInputStream(file);
        s = new ObjectInputStream(f);
        LinkedHashMap<Number,String> mapHooks  = (LinkedHashMap<Number,String>) s.readObject();
        String hookList = mapHooks.toString().replaceAll(", ","\n").replaceAll("=",":").replaceAll("\\{","").replaceAll("\\}","");
        Log.d("Time", hookList);
        s.close();
        f.close();


        //<editor-fold desc="Amplitude List">
        ArrayList<BarEntry> amplitudeList = new ArrayList<>();
        int i=0;
        float max=0.5f;
        for(Number time:mapLevels.keySet()){
            float yVal = mapLevels.get(time).floatValue();
            if (yVal>max)
                max=yVal;
            amplitudeList.add(new BarEntry(mapLevels.get(time).floatValue()==0?0.5f:mapLevels.get(time).floatValue(),i++));
            xVals.add(time.toString());
        }
        Log.d("MaxLevel", String.valueOf(max));
        BarDataSet setAmplitude = new BarDataSet(amplitudeList, "Amplitude");
//        setAmplitude.setColor(Color.rgb(63, 81, 181));
        setAmplitude.setHighLightColor(Color.rgb(254,255,2));
        //</editor-fold>

        ArrayList<BarEntry> hooksList = new ArrayList<>();
        i=0;
        max/=2;
        for(Number time:mapHooks.keySet()){
            Log.d("HookTime", String.valueOf(time)+", "+String.valueOf(max));
            hooksList.add(new BarEntry(max, i++));
            xVals.add(time.toString());
        }
        BarDataSet setHook = new BarDataSet(amplitudeList, "Hook");
        setHook.setColor(Color.rgb(255, 64, 129));


        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(setAmplitude);
        dataSets.add(setHook);



        BarData data = new BarData(xVals, dataSets);
        data.setDrawValues(false);
        amplitudeChart.setData(data);
        amplitudeChart.invalidate();

    }

    private void stopPlayer(){
        mediaPlayer.stop();
        mediaPlayer.reset();
        myHandler.removeCallbacksAndMessages(null);
        playBar.setProgress(0);
        amplitudeChart.highlightValue(-1, -1);
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            playBar.setProgress((int) startTime);
            ArrayList<Highlight> highlightArrayList=new ArrayList<>();
            int i=0;
            for (String x:xVals){
                int iBar = Integer.parseInt(x);
                Log.d("iBar"+String.valueOf(startTime),x);
                if (iBar<startTime) {
                    Highlight h = new Highlight(i++, 0);
                    highlightArrayList.add(h);
                }
            }
            Highlight[] highs = highlightArrayList.toArray(new Highlight[highlightArrayList.size()]);
            amplitudeChart.highlightValues(highs);

            myHandler.postDelayed(this, 100);
            if (!mediaPlayer.isPlaying())
                stopPlayer();
        }
    };

    /**
     * Called when a value has been selected inside the chart.
     *
     * @param e The selected Entry.
     * @param dataSetIndex The index in the datasets array of the data object
     * the Entrys DataSet is in.
     * @param h the corresponding highlight object that contains information
     * about the highlighted position
     */
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h){
        int position = Integer.parseInt(xVals.get(e.getXIndex()));
        mediaPlayer.seekTo(position);
    }

    /**
     * Called when nothing has been selected or an "un-select" has been made.
     */
    public void onNothingSelected(){

    }
}

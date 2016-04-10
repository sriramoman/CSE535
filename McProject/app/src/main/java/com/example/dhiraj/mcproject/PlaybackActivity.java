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
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
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
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class PlaybackActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();
    final private MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar playBar;
    private ImageButton btnStop;
    private ImageButton btnPlay;
    private ImageButton btnRewind;
    private ImageButton btnFwd;
    private BarChart amplitudeChart;
    private ArrayList<String> xVals;
    private ArrayList<String> xSVals;
    private ArrayList<Number> timeHooks;
    private boolean playerPaused;
    private LinkedHashMap<Number,String> mapHooks;
    private TextView hookedText;
    private String filename;
    private String filePath;
    private String[] allFiles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        //<editor-fold desc="Initialize objects">

        filePath=Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mydata/"+"50 cent/";
        filename="many men";
        allFiles = new String[3];
        allFiles[0] = filePath+filename + ".3gp";
        allFiles[1] = filePath+filename + "$.txt";
        allFiles[2] = filePath+filename + "~.txt";
        playerPaused=false;
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        btnPlay = (ImageButton)findViewById(R.id.btnPlay);
        btnStop = (ImageButton)findViewById(R.id.btnStop);
        btnRewind = (ImageButton)findViewById(R.id.btnRewind);
        btnFwd = (ImageButton)findViewById(R.id.btnFwd);
        btnStop.setEnabled(false);
        btnRewind.setEnabled(false);
        btnFwd.setEnabled(false);
        playBar = (SeekBar)findViewById(R.id.seekBar);
        hookedText = (TextView)findViewById(R.id.playback_hookText);
        xVals = new ArrayList<>();
        xSVals=new ArrayList<>();
        //</editor-fold>

        //<editor-fold desc="Chart">
        amplitudeChart = (BarChart)findViewById(R.id.chart);
        //Appearance
        amplitudeChart.setDescription("");
        Legend l = amplitudeChart.getLegend();
        l.setEnabled(false);
        XAxis xAxis = amplitudeChart.getXAxis();
        xAxis.setDrawGridLines(false);
        amplitudeChart.getAxisLeft().setDrawLabels(false);
        amplitudeChart.getAxisRight().setDrawLabels(false);
        amplitudeChart.getAxisLeft().setSpaceBottom(0.2f);
        amplitudeChart.getAxisRight().setSpaceBottom(0.2f);
        amplitudeChart.getAxisRight().setDrawGridLines(false);

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

        //<editor-fold desc="UI Control callback methods">
        playBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return !mediaPlayer.isPlaying();
            }
        });
        playBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // Resume update handler once user releases slider
                        mediaPlayer.seekTo(seekBar.getProgress());
                        if (mediaPlayer.isPlaying())
                            myHandler.postDelayed(UpdateSongTime, 100);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // Do not run update handler while user is dragging slider
                        myHandler.removeCallbacksAndMessages(null);
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // No-op
                    }
                }
        );

        btnPlay.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mediaPlayer.isPlaying())
                        //<editor-fold desc="Play->Pause::: Now, its playing. Toggle to pause mode">
                        {
                            mediaPlayer.pause();
                            btnPlay.setImageDrawable(getResources().getDrawable(R.drawable.play));
                            playerPaused = true;
                            btnStop.setEnabled(false);
                            btnRewind.setEnabled(false);
                            btnFwd.setEnabled(false);
                        }
                        //</editor-fold>
                        else if (playerPaused)
                        //<editor-fold desc="Pause->Play">
                        {
                            btnPlay.setImageDrawable(getResources().getDrawable(R.drawable.pause));
                            mediaPlayer.start();
                            playerPaused = false;
                            btnStop.setEnabled(true);
                        }
                        //</editor-fold>
                        else
                        //<editor-fold desc="Stopped->Play. Involves loading the file URL.">
                        {
                            btnPlay.setImageDrawable(getResources().getDrawable(R.drawable.pause));
                            try {
                                final Uri myUri = Uri.parse(filePath+filename + ".3gp");
                                mediaPlayer.setDataSource(getApplicationContext(), myUri);
                                mediaPlayer.prepareAsync();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                public void onPrepared(MediaPlayer player) {
                                    finalTime = mediaPlayer.getDuration();
                                    startTime = mediaPlayer.getCurrentPosition();
                                    playBar.setMax((int) finalTime);
                                    playBar.setProgress((int) startTime);
                                    myHandler.postDelayed(UpdateSongTime, 100);
                                    player.start();
                                    btnStop.setEnabled(true);
                                }
                            });
                        }
                        //</editor-fold>
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

        btnRewind.setOnClickListener(
                //TODO Seek to immediately previous rewind.
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<Number> candidateSeekPositions=new ArrayList<Number>();
                        for (int i=0;i<timeHooks.size();i++){
                            Number hookTime=timeHooks.get(i);
//                            Log.d("Hooktime",hookTime.toString());
                            if (startTime>=hookTime.doubleValue()) {
                                candidateSeekPositions.add(hookTime);
                            }
                        }
                        int candidateSize = candidateSeekPositions.size();
                        int currentHookPos = candidateSeekPositions.get(candidateSize-1).intValue();
                        //TODO In future, for extra credits, we could obtain value for user in place of the 3500 coded below.
                        //TODO User Preference (3500milliseconds=3.5seconds
                        if (/*We have sufficient hooks to seek back*/candidateSize>1 && /*Player has played sufficiently less(1second) from currentHook*/(startTime-currentHookPos)<3500)
                            //Seek 2 steps back
                            mediaPlayer.seekTo(candidateSeekPositions.get(candidateSize-2).intValue());
                        else
                            mediaPlayer.seekTo(currentHookPos);
                    }
                }
        );

        btnFwd.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i=0;i<timeHooks.size();i++){
                            Number hookTime=timeHooks.get(i);
//                            Log.d("Hooktime",hookTime.toString());
                            if (startTime<=hookTime.doubleValue()) {
                                mediaPlayer.seekTo(hookTime.intValue());
                                break;
                            }
                        }
                    }
                }
        );
        //</editor-fold>

        //<editor-fold desc="Load metadata">
        try {
            loadMetadata();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //</editor-fold>

    }

    private void loadMetadata() throws IOException, ClassNotFoundException {


        Compress compress = new Compress(allFiles,filename);
        compress.unpackZip(filePath,filename+".drs");

        File file = new File(filePath+filename+"~.txt");
        FileInputStream f = new FileInputStream(file);
        ObjectInputStream s = new ObjectInputStream(f);
        LinkedHashMap<Number,Number> mapLevels  = (LinkedHashMap<Number,Number>) s.readObject();
        s.close();
        f.close();

        file = new File(filePath+filename+"$.txt");
        f = new FileInputStream(file);
        s = new ObjectInputStream(f);
        mapHooks  = (LinkedHashMap<Number,String>) s.readObject();
//        String hookList = mapHooks.toString().replaceAll(", ","\n").replaceAll("=",":").replaceAll("\\{","").replaceAll("\\}","");
//        Log.d("Time", hookList);
        s.close();
        f.close();


        //<editor-fold desc="Amplitude List">
        ArrayList<BarEntry> amplitudeList = new ArrayList<>();
        float max=0;
        for(Number time:mapLevels.keySet()){
            float yVal = mapLevels.get(time).floatValue();
            if (yVal>max)
                max=yVal;
        }


        int i=0;
        int[] colors = new int[mapLevels.size()];
        Number prevTime=0;
        for(Number time:mapLevels.keySet()){
            float yval=-1;
            if (i>0){
                for (Number tm:mapHooks.keySet()){
                    if (tm.longValue()>prevTime.longValue()&&tm.longValue()<time.longValue()) {
                        yval = 1.15f*max;
                        colors[i]=Color.rgb(150, 10, 10);
                    }
                }
            }
            if (yval==-1) {
                yval = mapLevels.get(time).floatValue() == 0 ? 0.5f : mapLevels.get(time).floatValue();
                colors[i]= Color.rgb(193,236,255);
            }
            amplitudeList.add(new BarEntry(yval,i++));
            String tString=String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(time.longValue()),
                    TimeUnit.MILLISECONDS.toSeconds(time.longValue()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time.longValue()))
            );
            xSVals.add(tString);
            xVals.add(time.toString());
            prevTime = time;
        }
//        Log.d("MaxLevel", String.valueOf(max));
        BarDataSet setAmplitude = new BarDataSet(amplitudeList, "Amplitude");
        setAmplitude.setColors(colors);
        setAmplitude.setHighLightColor(Color.argb(122,255,255,122));
        setAmplitude.setBarSpacePercent(2f);
        //</editor-fold>

        timeHooks = new ArrayList<>();
        max/=2;
        for(Number time:mapHooks.keySet()){
            String tString=String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(time.longValue()),
                    TimeUnit.MILLISECONDS.toSeconds(time.longValue()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time.longValue()))
            );
            xSVals.add(tString);
            xVals.add(time.toString());
            timeHooks.add(time);
        }


        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(setAmplitude);



        BarData data = new BarData(xSVals, dataSets);
        data.setDrawValues(false);
        amplitudeChart.setData(data);
        amplitudeChart.invalidate();

    }

    private void stopPlayer(){
        mediaPlayer.stop();
        mediaPlayer.reset();
        myHandler.removeCallbacksAndMessages(null);
        playBar.setProgress(0);
        btnPlay.setImageDrawable(getResources().getDrawable(R.drawable.play));
        btnStop.setEnabled(false);
        btnRewind.setEnabled(false);
        btnFwd.setEnabled(false);
        hookedText.setText("");
        amplitudeChart.highlightValue(-1, -1);
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            if (playerPaused==false) {
                startTime = mediaPlayer.getCurrentPosition();
                playBar.setProgress((int) startTime);
                //<editor-fold desc="Highlight played tape">
                ArrayList<Highlight> highlightArrayList = new ArrayList<>();
                int i = 0;
                for (String x : xVals) {
                    int iBar = Integer.parseInt(x);
                    if (iBar < startTime) {
                        Highlight h = new Highlight(i++, 0);
                        highlightArrayList.add(h);
                    }
                }
                Highlight[] highs = highlightArrayList.toArray(new Highlight[highlightArrayList.size()]);
                amplitudeChart.highlightValues(highs);
                //</editor-fold>

                if (mapHooks.isEmpty()==false){
                    //<editor-fold desc="Update Hook text to last crossed hook">
                    boolean blHookFoundBehind=false;
                    boolean blHookFoundAhead=false;
                    for (i=0;i<timeHooks.size();i++){
                        Number hookTime=timeHooks.get(i);
//                        Log.d("Hooktime",hookTime.toString());
                        if (startTime>=hookTime.doubleValue()) {
                            blHookFoundBehind=true;
                            hookedText.setText(mapHooks.get(hookTime));
                            btnRewind.setEnabled(true);
                        }
                        else {
                            blHookFoundAhead= true;
                            btnFwd.setEnabled(blHookFoundAhead);
                        }
                    }
                    if (!blHookFoundBehind) {
                        hookedText.setText("");
                    }
                    btnRewind.setEnabled(blHookFoundBehind);
                    btnFwd.setEnabled(blHookFoundAhead);
                    //</editor-fold>
                }
            }

            myHandler.postDelayed(this, 100);
            if (!mediaPlayer.isPlaying() && playerPaused==false)
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy","Gonna stop");
        stopPlayer();
        Compress compress = new Compress(allFiles,filePath+filename+".drs");
        compress.deleteFiles(allFiles);
    }
}

package com.example.dhiraj.mcproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class VisualizerView extends View {

    private byte[] mBytes;
    private float[] mPoints;
    private Rect mRect = new Rect();
    private Paint mForePaint = new Paint();
    private float mRadius;
    private float oldRadius = 0.0f;
    public VisualizerView(Context context) {
        super(context);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBytes = null;
        mForePaint.setStyle(Paint.Style.STROKE);
        mForePaint.setStrokeWidth(5f);
        mForePaint.setAntiAlias(true);
        mForePaint.setColor(Color.rgb(220,20,60));
        //mForePaint.Style.STROKE;
        //Paint.Style.STROKE
    }

    public void updateVisualizer(float radius) {
        oldRadius = mRadius;
        mRadius = radius;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = this.getWidth();
        int height = this.getHeight();
        Log.e("hi"," "+height+ " "+ width);
        mForePaint.setColor(Color.rgb(100, 200, 235));
        canvas.drawCircle(width / 2, height / 2, oldRadius + 84, mForePaint);

        mForePaint.setColor(Color.rgb(220, 20, 60));
        canvas.drawCircle(width / 2, height / 2, mRadius + 84, mForePaint);

//        Bitmap b= BitmapFactory.decodeResource(getResources(), R.drawable.voice);
//        //p.setColor(Color.RED);
//        canvas.drawBitmap(b,(width / 2) - 64 , (height / 2) - 64 , mForePaint);
    }

}
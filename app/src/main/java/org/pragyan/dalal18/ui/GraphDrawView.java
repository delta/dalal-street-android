package org.pragyan.dalal18.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GraphDrawView extends View implements Runnable {

    public Context context;
    int width;
    int height;
    int FPS = 60;

    List<Pair<Integer, String>> points = new ArrayList<>();
    List<Integer> points2 = new ArrayList<>();
    Random random = new Random();
    int listLength = 20;
    int xMov = 0;
    int unitWidth = 0;

    Paint graphPaint = new Paint();
    Paint backgroundPaint = new Paint();
    Paint linePaint = new Paint();
    Paint textPaint = new Paint();

    Date date = new Date();
    DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.US);
    Paint graphPaint2;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            invalidate();
        }
    };

    public GraphDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        width = context.getResources().getDisplayMetrics().widthPixels;
        height = context.getResources().getDisplayMetrics().heightPixels;
        unitWidth = width / listLength;
        for (int i = 0; i <= listLength + 1; i++) {
            date.setTime(date.getTime() + 5 * 60000);
            points.add(new Pair<>(-random.nextInt(height / 4) + height / 2 - width / 6, dateFormat.format(date)));
        }
        for (int i = 0; i <= listLength + 1; i++) {
            points2.add((-random.nextInt(height / 4) + height - width / 6));
        }

        graphPaint.setColor(Color.GREEN);
        graphPaint.setAntiAlias(true);
        graphPaint.setStyle(Paint.Style.STROKE);
        graphPaint.setStrokeWidth(5.0f);

        backgroundPaint.setColor(Color.parseColor("#111111"));

        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(5.0f);
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);

        graphPaint2 = new Paint(graphPaint);
        graphPaint2.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#111111"));
        canvas.drawLine(0, height / 2, width, height / 2, graphPaint);
        for (int i = listLength; i >= 0; i--) {
            canvas.drawLine(xMov + (i - 1) * (unitWidth), points.get(i).first, xMov + (i) * unitWidth, points.get(i + 1).first, graphPaint);
            canvas.drawLine(xMov + (i - 1) * (unitWidth), points2.get(i), xMov + (i) * unitWidth, points2.get(i + 1), graphPaint2);
            canvas.drawLine(xMov + (i - 1) * (unitWidth), height / 2 - 5, xMov + (i - 1) * (unitWidth), height / 2 + 5, linePaint);
            canvas.drawText(points.get(i).second, xMov + (i - 1) * (unitWidth), height / 2 - 10, textPaint);
        }
        xMov -= 5;
        if (xMov <= 0) {
            xMov = unitWidth;
            points.remove(0);
            points2.remove(0);
            date.setTime(date.getTime() + 5 * 60000);
            points.add(new Pair<>(-random.nextInt(height / 4) + height / 2 - width / 6, dateFormat.format(date)));
            points2.add(-random.nextInt(height / 4) + height - (height / 4) + width / 6);
        }
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(1000 / FPS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.sendEmptyMessage(0);
        }
    }
}

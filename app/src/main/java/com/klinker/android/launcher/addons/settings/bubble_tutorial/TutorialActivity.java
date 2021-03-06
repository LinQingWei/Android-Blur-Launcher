package com.klinker.android.launcher.addons.settings.bubble_tutorial;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.klinker.android.launcher.R;

import java.text.DecimalFormat;

public class TutorialActivity extends Activity {

    private static final int STATE_LOADING = 0;
    private static final int STATE_B1_E = 1;
    private static final int STATE_T1_I = 2;
    private static final int STATE_B1_W = 3;
    private static final int STATE_T1_O = 4;
    private static final int STATE_B1_S = 5;
    private static final int STATE_B2_E = 6;
    private static final int STATE_T2_I = 7;
    private static final int STATE_B2_W = 8;
    private static final int STATE_T2_O = 9;
    private static final int STATE_B2_S = 10;
    private static final int STATE_B3_E = 11;
    private static final int STATE_T3_I = 12;
    private static final int STATE_B3_W = 13;
    private static final int STATE_FINISHING = 14;
    private static final int STATE_MOVING_LOGO = 15;
    private static final int STATE_FADING_OUT = 16;

    public static final String ACTION_HIGHLIGHT_PAGE_PICKER = "com.klinker.android.launcher.tutorial.PAGE_PICKER";
    public static final String ACTION_START_HIGHLIGHT_DISPLAY = "com.klinker.android.launcher.tutorial.DISPLAY";
    public static final String ACTION_HIGHLIGHT_HELP = "com.klinker.android.launcher.tutorial.HELP";

    private DrawingThread thread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(new DrawingPanel(this));
    }

    @Override
    public void onStop() {
        super.onStop();
        thread.setRunning(false);
        finish();
        overridePendingTransition(0, 0);
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("initial_tutorial", false).commit();
    }

    private class DrawingPanel extends SurfaceView implements
            SurfaceHolder.Callback {

        private String avgFps;
        private double fps;
        private Circle[] circles;
        private int state = 0;
        private long startTime = System.currentTimeMillis();

        private final String TEXT1;
        private final String TEXT2;
        private final String TEXT3;

        public DrawingPanel (Context context) {
            super(context);

            this.setBackgroundColor(Color.TRANSPARENT);
            this.setZOrderOnTop(true);
            getHolder().setFormat(PixelFormat.TRANSPARENT);

            getHolder().addCallback(this);
            setFocusable(true);
            thread = new DrawingThread(getHolder(), this);

            TEXT1 = "\nUse the\nPage Picker to\nadd content to\nBlur!";
            TEXT2 = "\nDisplay Settings\nare for all\nof your\ncustomizations.";
            TEXT3 = "The Help\nsection is a great\nplace to learn\nmore about\nthe app.";
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            float circle1 = getResources().getDimensionPixelSize(R.dimen.tutorial_bubble_1_size);
            float circle2 = getResources().getDimensionPixelSize(R.dimen.tutorial_bubble_2_size);
            float circle3 = getResources().getDimensionPixelSize(R.dimen.tutorial_bubble_3_size);
            float padding = getResources().getDimensionPixelSize(R.dimen.tutorial_bubble_padding);

            circles = new Circle[] {
                    new Circle(circle1,
                            .3,
                            .95,
                            getResources().getColor(android.R.color.holo_red_dark),
                            getWidth() - circle1 - padding,
                            padding + circle1,
                            500),
                    new Circle(circle2,
                            .25,
                            .9,
                            getResources().getColor(android.R.color.holo_orange_light),
                            padding * 3,
                            getHeight() / 2 - 4 * padding,
                            3500),
                    new Circle(circle3,
                            .35,
                            .85,
                            getResources().getColor(android.R.color.holo_green_dark),
                            getWidth() - circle3 - padding,
                            getHeight() - circle3 - (padding * 2),
                            6500)
            };

            thread.setRunning(true);
            thread.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            boolean retry = true;
            while (retry) {
                try {
                    thread.join();
                    retry = false;
                } catch (InterruptedException e) {

                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (state == STATE_B1_W && event.getAction() == MotionEvent.ACTION_DOWN) {
                state++;
                circles[0].startTime = System.currentTimeMillis() - (long) (1.04017 * circles[0].bounceTime * 1000);
            } else if (state == STATE_B2_W && event.getAction() == MotionEvent.ACTION_DOWN) {
                state++;
                circles[1].startTime = System.currentTimeMillis() - (long) (1.04017 * circles[1].bounceTime * 1000);
            } else if (state == STATE_B3_W && event.getAction() == MotionEvent.ACTION_DOWN) {
                state++;
                circles[2].startTime = System.currentTimeMillis();
            }

            return super.onTouchEvent(event);
        }

        @Override
        protected void onDraw(Canvas canvas) { }

        private boolean needHighlightPagePicker = true;
        private boolean needHighlightDisplays = true;
        private boolean needHighlightHelp = true;
        public void update() {
            switch (state) {
                case STATE_LOADING:
                    if (System.currentTimeMillis() - startTime >= 1000 && fps > 20) {
                        state++;
                        circles[0].startTime = System.currentTimeMillis();
                    }

                    break;
                case STATE_B1_E:
                    if (System.currentTimeMillis() - circles[0].startTime < circles[0].maxTime * 1000) {
                        circles[0].radius = getRadiusNeeded(circles[0]);
                    } else {
                        state++;
                        startTime = System.currentTimeMillis();
                    }

                    break;
                case STATE_T1_I:
                    textAlpha += 15;
                    if (textAlpha >= 255) {
                        textAlpha = 255;
                        state++;
                        startTime = System.currentTimeMillis();
                    }
                    break;
                case STATE_B1_W:
                    long time = System.currentTimeMillis();
                    if (time - startTime > 5000) {
                        state++;
                        circles[0].startTime = System.currentTimeMillis() - (long) (1.04017 * circles[0].bounceTime * 1000);
                    } else if (time - startTime > 1000 && needHighlightPagePicker) {
                        sendBroadcast(new Intent(ACTION_HIGHLIGHT_PAGE_PICKER));
                        needHighlightPagePicker = false;
                    }
                    break;
                case STATE_T1_O:
                    textAlpha -= 15;
                    if (textAlpha <= 0) {
                        textAlpha = 0;
                        state++;
                        circles[0].startTime = System.currentTimeMillis() - (long) (1.04017 * circles[0].bounceTime * 1000);
                    }
                    break;
                case STATE_B1_S:
                    circles[0].radius = getRadiusNeeded(circles[0]);
                    if (circles[0].radius <= 0) {
                        circles[0].radius = 0;
                        state++;
                        circles[1].startTime = System.currentTimeMillis();
                    }

                    break;
                case STATE_B2_E:
                    if (System.currentTimeMillis() - circles[1].startTime < circles[1].maxTime * 1000) {
                        circles[1].radius = getRadiusNeeded(circles[1]);
                    } else {
                        state++;
                        startTime = System.currentTimeMillis();
                    }

                    break;
                case STATE_T2_I:
                    textAlpha += 15;
                    if (textAlpha >= 255) {
                        textAlpha = 255;
                        state++;
                        startTime = System.currentTimeMillis();
                    }
                    break;
                case STATE_B2_W:
                    time = System.currentTimeMillis();
                    if (time - startTime > 5000) {
                        state++;
                        circles[1].startTime = System.currentTimeMillis() - (long) (1.04017 * circles[1].bounceTime * 1000);
                    } else if (time - startTime > 1500 && needHighlightDisplays) {
                        sendBroadcast(new Intent(ACTION_START_HIGHLIGHT_DISPLAY));
                        needHighlightDisplays = false;
                    }
                    break;
                case STATE_T2_O:
                    textAlpha -= 15;
                    if (textAlpha <= 0) {
                        textAlpha = 0;
                        state++;
                        circles[1].startTime = System.currentTimeMillis() - (long) (1.04017 * circles[1].bounceTime * 1000);
                    }
                    break;
                case STATE_B2_S:
                    circles[1].radius = getRadiusNeeded(circles[1]);
                    if (circles[1].radius <= 0) {
                        circles[1].radius = 0;
                        state++;
                        circles[2].startTime = System.currentTimeMillis();
                    }

                    break;
                case STATE_B3_E:
                    if (System.currentTimeMillis() - circles[2].startTime < circles[2].maxTime * 1000) {
                        circles[2].radius = getRadiusNeeded(circles[2]);
                    } else {
                        state++;
                        startTime = System.currentTimeMillis();
                    }

                    break;
                case STATE_T3_I:
                    textAlpha += 15;
                    if (textAlpha >= 255) {
                        textAlpha = 255;
                        state++;
                        startTime = System.currentTimeMillis();
                    }
                    break;
                case STATE_B3_W:
                    // just wait for a touch on the screen
                    time = System.currentTimeMillis();
                    if (time - startTime > 1000 && needHighlightHelp) {
                        sendBroadcast(new Intent(ACTION_HIGHLIGHT_HELP));
                        needHighlightHelp = false;
                    }
                    break;
                case STATE_FINISHING:
                    circles[2].radius += 2.5 * Math.pow((System.currentTimeMillis() - circles[2].startTime) / 700.0, 4) + 8;
                    textAlpha -= 5;

                    if (textAlpha <= 0) {
                        textAlpha = 0;
                    }

                    if (circles[2].y - circles[2].radius < TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -400, getResources().getDisplayMetrics())) {
                        textAlpha = 0;
                        state++;
                        startTime = System.currentTimeMillis();
                    }

                    break;
                case STATE_MOVING_LOGO:
                    state++;
                    /*logoY -= 2.5 * Math.pow((System.currentTimeMillis() - startTime) / 1000.0, 3) + 8;

                    if (logoY < -1 * logo.getHeight()) {
                        state++;
                        circles[2].startTime = System.currentTimeMillis();
                    }*/
                    break;
                case STATE_FADING_OUT:
                    alpha -= 4 * Math.pow((System.currentTimeMillis() - circles[2].startTime) / 1000.0, 2) + 8;
                    if (alpha <= 0) {
                        state++;
                    }
                    break;
                default:
                    thread.setRunning(false);
                    finish();
                    overridePendingTransition(0, 0);
                    PreferenceManager.getDefaultSharedPreferences(TutorialActivity.this).edit().putBoolean("initial_tutorial", false).commit();
                    break;
            }
        }

        public int alpha = 255;
        public int textAlpha = 0;
        public int logoY = 0;
        public void render(Canvas canvas) {
            if (canvas != null) {
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                //displayFps(canvas, avgFps);

                Paint paint = new Paint();
                paint.setAntiAlias(true);

                switch (state) {
                    case STATE_LOADING:
                        // Dont need to draw anything
                        break;
                    case STATE_B1_E:
                    case STATE_T1_I:
                    case STATE_B1_W:
                    case STATE_T1_O:
                    case STATE_B1_S:
                        // Draw the first circle at its state
                        paint.setColor(circles[0].color);
                        canvas.drawCircle(circles[0].x, circles[0].y, (float) circles[0].radius, paint);
                        paint.setColor(getResources().getColor(android.R.color.white));
                        paint.setTextAlign(Paint.Align.CENTER);
                        float textSize = (float) (1.5 * getResources().getDimensionPixelSize(R.dimen.tutorial_text_size));
                        paint.setTextSize(textSize);
                        paint.setAlpha(textAlpha);
                        String[] text = TEXT1.split("\n");
                        for (int i = 0; i < text.length; i++) {
                            canvas.drawText(text[i], circles[0].x, circles[0].y + (textSize/2) - (text.length * textSize / 2) + (i * textSize), paint);
                        }
                        break;
                    case STATE_B2_E:
                    case STATE_T2_I:
                    case STATE_B2_W:
                    case STATE_T2_O:
                    case STATE_B2_S:
                        // Draw the second circle at its state
                        paint.setColor(circles[1].color);
                        canvas.drawCircle(circles[1].x, circles[1].y, (float) circles[1].radius, paint);
                        paint.setColor(getResources().getColor(android.R.color.white));
                        paint.setTextAlign(Paint.Align.CENTER);
                        textSize = (float) (1.5 * getResources().getDimensionPixelSize(R.dimen.tutorial_text_size));
                        paint.setTextSize(textSize);
                        paint.setAlpha(textAlpha);
                        text = TEXT2.split("\n");
                        for (int i = 0; i < text.length; i++) {
                            canvas.drawText(text[i], circles[1].x, circles[1].y + (textSize/2) - (text.length * textSize / 2) + (i * textSize), paint);
                        }
                        break;
                    case STATE_B3_E:
                    case STATE_T3_I:
                    case STATE_B3_W:
                    case STATE_FINISHING:
                    case STATE_MOVING_LOGO:
                    case STATE_FADING_OUT:
                        // Draw the third circle at its state
                        paint.setColor(circles[2].color);
                        paint.setAlpha(alpha);
                        canvas.drawCircle(circles[2].x, circles[2].y, (float) circles[2].radius, paint);
                        paint.setColor(getResources().getColor(android.R.color.white));
                        paint.setTextAlign(Paint.Align.CENTER);
                        textSize = (float) (1.5 * getResources().getDimensionPixelSize(R.dimen.tutorial_text_size));
                        paint.setTextSize(textSize);
                        paint.setAlpha(textAlpha);
                        text = TEXT3.split("\n");
                        for (int i = 0; i < text.length; i++) {
                            canvas.drawText(text[i], circles[2].x, circles[2].y + (textSize) - (text.length * textSize / 2) + (i * textSize), paint);
                        }
                        paint.setAlpha(alpha);
                        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                        //canvas.drawBitmap(logo, getWidth() / 2 - logo.getWidth() / 2, logoY, paint);
                        break;
                }
            }
        }

        private double getRadiusNeeded(Circle circle) {
            long currentTime = System.currentTimeMillis();
            double elapsed = ((currentTime - circle.startTime) / 1000.0);

            if (elapsed < 0) {
                return 0;
            }

            double time = elapsed - (2.04017 * circle.bounceTime);

            return -1 * (1 - circle.bounceSize) * (circle.height / Math.pow(circle.bounceTime, 4)) * Math.pow(time, 4) +
                    2 * (1 - circle.bounceSize) * (circle.height / (circle.bounceTime * circle.bounceTime)) * time * time +
                    circle.bounceSize * circle.height;
        }

        public void setAvgFps(String avgFps) {
            this.avgFps = avgFps;
        }

        public void setFps(double fps) { this.fps = fps; }

        public void displayFps(Canvas canvas, String fps) {
            if (canvas != null && fps != null) {
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.tutorial_text_size));
                canvas.drawText(fps, 100, 200, paint);
            }
        }
    }

    private class DrawingThread extends Thread {

        private final static int MAX_FPS = 30;
        private final static int MAX_FRAME_SKIPS = 5;
        private final static int FRAME_PERIOD = 1000 / MAX_FPS;

        private DecimalFormat df = new DecimalFormat("0.##");
        private final static int STAT_INTERVAL = 1000;
        private final static int FPS_HISTORY_NR = 10;
        private long lastStatusStore = 0;
        private long statusIntervalTimer = 0l;
        private long totalFramesSkipped = 0l;
        private long framesSkippedPerStatCycle = 0l;

        private int frameCountPerStatCycle = 0;
        private long totalFrameCount = 0l;
        private double[] fpsStore;
        private long statsCount = 0;
        private double averageFps = 0.0;

        private boolean running;
        private SurfaceHolder holder;
        private DrawingPanel panel;

        public DrawingThread(SurfaceHolder holder, DrawingPanel panel) {
            super();
            this.holder = holder;
            this.panel = panel;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            Canvas canvas;

            initTimingElements();

            long beginTime;
            long timeDiff;
            int sleepTime;
            int framesSkipped;

            sleepTime = 0;

            while (running) {
                canvas = null;

                try {
                    canvas = this.holder.lockCanvas();
                    synchronized (holder) {
                        beginTime = System.currentTimeMillis();
                        framesSkipped = 0;

                        this.panel.update();
                        this.panel.render(canvas);

                        timeDiff = System.currentTimeMillis() - beginTime;
                        sleepTime = (int) (FRAME_PERIOD - timeDiff);

                        if (sleepTime > 0) {
                            try { Thread.sleep(sleepTime); } catch (Exception e) { }
                        }

                        while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
                            this.panel.update();
                            sleepTime += FRAME_PERIOD;
                            framesSkipped++;
                        }

                        framesSkippedPerStatCycle += framesSkipped;
                        storeStats();
                    }
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        private void storeStats() {
            frameCountPerStatCycle++;
            totalFrameCount++;

            statusIntervalTimer += (System.currentTimeMillis() - statusIntervalTimer);

            if (statusIntervalTimer >= lastStatusStore + STAT_INTERVAL) {
                double actualFps = (double) (frameCountPerStatCycle / (STAT_INTERVAL / 1000));
                panel.setFps(actualFps);
                fpsStore[(int) statsCount % FPS_HISTORY_NR] = actualFps;

                statsCount++;

                double totalFps = 0.0;

                for (int i = 0; i < FPS_HISTORY_NR; i++) {
                    totalFps += fpsStore[i];
                }

                if (statsCount < FPS_HISTORY_NR) {
                    averageFps = totalFps / statsCount;
                } else {
                    averageFps = totalFps / FPS_HISTORY_NR;
                }

                totalFramesSkipped += framesSkippedPerStatCycle;

                framesSkippedPerStatCycle = 0;
                statusIntervalTimer = 0;
                frameCountPerStatCycle = 0;

                statusIntervalTimer = System.currentTimeMillis();
                lastStatusStore = statusIntervalTimer;
                panel.setAvgFps("FPS: " + df.format(averageFps));
            }
        }

        private void initTimingElements() {
            fpsStore = new double[FPS_HISTORY_NR];

            for (int i = 0; i < FPS_HISTORY_NR; i++) {
                fpsStore[i] = 0.0;
            }
        }
    }
}

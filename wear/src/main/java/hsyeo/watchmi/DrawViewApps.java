package hsyeo.watchmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

public class DrawViewApps extends ImageView {
    public enum DrawModes {CLOCK, MAP, FILE, NONE}
    DrawModes drawModes = DrawModes.NONE;

    Paint strokeBlue;

    RectF oval, circle;

    float x,y,z;

    int displayWidth, displayheight;
    int halfScreen = 160; // 160 for circle watch, 140 for square watch
    boolean isRound = false;

    public DrawViewApps(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        displayWidth = metrics.widthPixels;
        displayheight = metrics.heightPixels;
        init();
        setupColors();
    }

    private void init() {
        oval = new RectF();
        oval.set(10,10,halfScreen*2-10,halfScreen*2-10);
        circle = new RectF();
        circle.set(25,25,halfScreen*2-25,halfScreen*2-25);

        hourLength = (float) (halfScreen * 0.5);
        minuteLength = (float) (halfScreen * 0.75);
    }

    void setupColors(){
        shadowColor = Color.BLACK;
        hourPaint = new Paint();
        hourPaint.setColor(Color.RED);
        hourPaint.setStrokeWidth(HOUR_STROKE_WIDTH);
        hourPaint.setAntiAlias(true);
        hourPaint.setStrokeCap(Paint.Cap.ROUND);
        hourPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, shadowColor);

        minutePaint = new Paint();
        minutePaint.setColor(Color.GREEN);
        minutePaint.setStrokeWidth(MINUTE_STROKE_WIDTH);
        minutePaint.setAntiAlias(true);
        minutePaint.setStrokeCap(Paint.Cap.ROUND);
        minutePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, shadowColor);

        strokeBlue = new Paint();
        strokeBlue.setColor(Color.argb(220, 0, 50, 255));
        strokeBlue.setStrokeWidth(20);
        strokeBlue.setStyle(Paint.Style.STROKE);
    }

    void setShape(boolean b, int chinSize) {
        isRound = b;
        if (chinSize != 0)
            isRound = true;
        if (isRound)
            halfScreen = displayWidth/2;
        else
            halfScreen = displayWidth/2;
        init();
    }

    void setDrawModes(DrawModes m){
        drawModes = m;
    }

    void setXYZ(float xx, float yy, float zz){
        x = xx * Constants.MAGIC_XY;
        y = yy * Constants.MAGIC_XY;
        z = zz * Constants.MAGIC_TWIST;
    }

    @Override
    public void onDraw(Canvas canvas) {
        switch (drawModes) {
            case CLOCK:
                DrawClock(canvas);
                break;
            case MAP:
                DrawMap(canvas);
                break;
            case FILE:
                break;
        }
    }

//region Clock related
    int second, millisecond, hour, minute;
    float gap = 4f;
    float minuteLength, hourLength;
    Paint hourPaint, minutePaint;
    int shadowColor;
    float HOUR_STROKE_WIDTH = 16f;
    float MINUTE_STROKE_WIDTH = 6f;
    int SHADOW_RADIUS = 6;
    void DrawClock(Canvas canvas){
        final float seconds = (second + millisecond / 1000f);
        final float secondsRotation = seconds * 6f;
        final float minutesRotation = (minute+second/60) * 6f;
        final float hourHandOffset = minute / 2f;
        final float hoursRotation = (hour * 30) + hourHandOffset;

        canvas.save();

        canvas.rotate(hoursRotation, halfScreen, halfScreen);
        canvas.drawLine(
                halfScreen,
                halfScreen - gap,
                halfScreen,
                halfScreen - hourLength,
                hourPaint);

        canvas.rotate(minutesRotation - hoursRotation, halfScreen, halfScreen);
        canvas.drawLine(
                halfScreen,
                halfScreen - gap,
                halfScreen,
                halfScreen - minuteLength,
                minutePaint);

        canvas.rotate(secondsRotation - minutesRotation, halfScreen, halfScreen);

        canvas.restore();
    }

    public void addHour(int hours){
        this.hour += hours;

        if(this.hour >= 12){
            this.hour -= 12;
        }else if(this.hour<0){
            this.hour += 12;
        }
    }

    public void addMinute(int minutes){
        this.minute += minutes;

        if(this.minute >= 60){
            this.addHour(this.minute / 60);
            this.minute = this.minute%60;

        }else if(this.minute<0){
            this.minute = Math.abs(this.minute);
            this.addHour(-(1 + this.minute / 60));
            this.minute = 60-this.minute%60;
        }
    }

    public void addSecond(int seconds){
        this.second += seconds;

        if(this.second >= 60){
            this.addMinute(this.second / 60);
            this.second = this.second%60;

        }else if(this.second<0) {
            this.second = Math.abs(this.second);
            this.addMinute(-(1 + this.second / 60));
            this.second = 60 - this.second % 60;
        }
    }
//endregion

//region Map related
    @Override
    public void setImageDrawable(Drawable drawable) {
        width = drawable.getIntrinsicWidth();
        height = drawable.getIntrinsicHeight();
//        mScaleFactor = Math.min(((float)getLayoutParams().width - 5) / width, ((float) getLayoutParams().height - 5) / height);
//        default_ScaleFactor = Math.max(((float)getLayoutParams().width - 5) / width, ((float)getLayoutParams().height - 5) / height);
        mScaleFactor = 0.5f;
        default_ScaleFactor = 0.5f;
//        pivotPointX = (((float)getLayoutParams().width) - (int)(width * mScaleFactor)) / 2;
//        pivotPointY = (((float)getLayoutParams().height) - (int)(height * mScaleFactor)) / 2;
//        pivotPointX = 140;
//        pivotPointY = 140;
        mPosX = 100; mPosY = 100;
        super.setImageDrawable(drawable);
    }
    int width, height;
    float old_width, old_height;
    private float mPosX = 0f, mPosY = 0f;
    private float mScaleFactor = 1.0f;
    float default_ScaleFactor;
    float pivotPointX = 0f;
    float pivotPointY = 0f;
    void DrawMap(Canvas canvas){
        if (this.getDrawable() != null) {
            canvas.save();
            mScaleFactor = Math.max(default_ScaleFactor,mScaleFactor);
            mScaleFactor = Math.min(4, mScaleFactor);
            float cur_width = (width * mScaleFactor);
            float cur_height = (height * mScaleFactor);

            //bounce to not avoid the image section
            mPosX = Math.min(0, mPosX);
            mPosX = Math.max(-(cur_width-getLayoutParams().width) + 500,mPosX);
            mPosY = Math.min(0, mPosY);
            mPosY = Math.max(-(cur_height-getLayoutParams().height) + 500,mPosY);
            mPosX -= (cur_width-old_width)/2;
            mPosY -= (cur_height-old_height)/2;
            canvas.translate(mPosX, mPosY);

            Matrix matrix = new Matrix();
            matrix.postScale(mScaleFactor, mScaleFactor);
            old_height = cur_height; old_width = cur_width;

            canvas.drawBitmap(((BitmapDrawable) this.getDrawable()).getBitmap(), matrix, null);
            canvas.restore();
        }
    }

    public void changeScaleFactor(float k, float x, float y){
        mScaleFactor *= k;
        pivotPointX = x;
        pivotPointY = y;
    }

    public void translateImage(float dx, float dy, float K){
        mPosX -= dx * K;
        mPosY -= dy * K;
    }
//endregion
}

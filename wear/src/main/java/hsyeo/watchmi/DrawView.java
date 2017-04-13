package hsyeo.watchmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class DrawView extends View {
    public enum DrawModes {PRESSURE, TWIST, PANNING, NONE}
    DrawModes drawModes = DrawModes.NONE;
    boolean isStudy;
    Paint strokeRed, strokeBlue, strokeGreen, fillOrange, strokeOrange, strokeWhite, strokeWhiteThick, strokeBlack;
    Paint paintHighlightTask;

    RectF oval, circle;

    int currentTrialPos, currentTrialLvl;

    float x,y,z;

    int halfScreen = 160; // 160 for circle watch, 140 for square watch
    boolean isRound = false;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        setupColors();
    }

    private void init() {
        oval = new RectF();
        oval.set(10,10,halfScreen*2-10,halfScreen*2-10);
        circle = new RectF();
        circle.set(25,25,halfScreen*2-25,halfScreen*2-25);
    }

    void setupColors(){
        strokeRed = new Paint();
        strokeRed.setColor(Color.argb(220, 255, 0, 0));
        strokeRed.setStrokeWidth(20);
        strokeRed.setStyle(Paint.Style.STROKE);
        strokeBlue = new Paint();
        strokeBlue.setColor(Color.argb(220, 0, 50, 255));
        strokeBlue.setStrokeWidth(20);
        strokeBlue.setStyle(Paint.Style.STROKE);
        strokeBlue.setStrokeCap(Paint.Cap.ROUND);
        strokeGreen = new Paint();
        strokeGreen.setColor(Color.argb(220, 0, 255, 0));
        strokeGreen.setStrokeWidth(20);
        strokeGreen.setStyle(Paint.Style.STROKE);
        strokeWhite = new Paint();
        strokeWhite.setColor(Color.WHITE);
        strokeWhite.setStyle(Paint.Style.STROKE);
        strokeWhite.setStrokeWidth(2);
        strokeWhiteThick = new Paint();
        strokeWhiteThick.setColor(Color.WHITE);
        strokeWhiteThick.setStyle(Paint.Style.STROKE);
        strokeWhiteThick.setStrokeWidth(40);
        strokeBlack = new Paint();
        strokeBlack.setColor(Color.BLACK);
        strokeBlack.setStrokeWidth(45);
        strokeBlack.setStyle(Paint.Style.STROKE);
        strokeBlack.setAntiAlias(true);
        fillOrange = new Paint();
        fillOrange.setColor(Color.argb(150, 255, 165, 0));
        fillOrange.setStrokeWidth(20);
        fillOrange.setStyle(Paint.Style.FILL);
        strokeOrange = new Paint();
        strokeOrange.setColor(Color.argb(150, 255, 165, 0));
        strokeOrange.setStrokeWidth(40);
        strokeOrange.setStyle(Paint.Style.STROKE);
        strokeOrange.setAntiAlias(true);
        paintHighlightTask = new Paint();
        paintHighlightTask.setColor(Color.argb(150, 255, 0, 0));
        paintHighlightTask.setStrokeWidth(20);
        paintHighlightTask.setStyle(Paint.Style.STROKE);
        paintHighlightTask.setAntiAlias(true);
    }

    void setShape(boolean b) {
        isRound = b;
        if (isRound) {
            halfScreen = 160;
        }
        else {
            halfScreen = 140;
        }
        init();
    }

    void setDrawModes(DrawModes m){
        drawModes = m;
        if(!initialized) InitExpVariables(drawModes);
    }

    void setStudyModes(boolean b){
        isStudy = b;
    }

    void setTrial(int p, int l){
        currentTrialPos = p;
        currentTrialLvl = l;
    }

    void setXYZ(float xx, float yy, float zz){
        x = xx * Constants.MAGIC_XY;
        y = yy * Constants.MAGIC_XY;
        z = zz * Constants.MAGIC_TWIST;
    }

    @Override
    public void onDraw(Canvas canvas) {
        switch (drawModes) {
            case PRESSURE:
                if (isStudy)
                    DrawExpPressure(canvas);
                canvas.drawLine(halfScreen, halfScreen, x + halfScreen, y + halfScreen, strokeBlue);
                break;
            case TWIST:
                if (isStudy)
                    DrawExpTwist(canvas);
                canvas.drawArc(circle, -90, z, false, strokeBlue);
                break;
            case PANNING:
                if (isStudy)
                    DrawExpPanning(canvas);
                canvas.drawCircle(halfScreen + x, halfScreen + y, 5, strokeBlue);
                break;
        }
    }


    void DrawExpPressure(Canvas canvas){
        canvas.drawLine(halfScreen - disX, halfScreen - disY, halfScreen + disX, halfScreen + disY, strokeWhite); // 4 white diagonal lines
        canvas.drawLine(halfScreen - disY, halfScreen - disX, halfScreen + disY, halfScreen + disX, strokeWhite);
        canvas.drawLine(halfScreen - disX, halfScreen + disY, halfScreen + disX, halfScreen - disY, strokeWhite);
        canvas.drawLine(halfScreen - disY, halfScreen + disX, halfScreen + disY, halfScreen - disX, strokeWhite);

        int convertedPos = currentTrialPos;
        canvas.drawArc(rr4,(float) 45 * (convertedPos) + 1-112-45, (float) 45-2, false, strokeOrange);

        canvas.drawArc(r1, (float) 45 * (convertedPos) + 1-112-45, (float) 45-2, false, strokeWhite); // 4 levels white thin arcs
        canvas.drawArc(r2, (float) 45 * (convertedPos) + 1-112-45, (float) 45-2, false, strokeWhite);
        canvas.drawArc(r3, (float) 45 * (convertedPos) + 1-112-45, (float) 45-2, false, strokeWhite);
        canvas.drawArc(r4, (float) 45 * (convertedPos) + 1-112-45, (float) 45-2, false, strokeWhite);

        switch (currentTrialLvl) {
            case 1: canvas.drawArc(rr1,(float) 45 * (convertedPos)+ 1-112-45, (float) 45-2, false, paintHighlightTask); break;
            case 2: canvas.drawArc(rr2,(float) 45 * (convertedPos)+ 1-112-45, (float) 45-2, false, paintHighlightTask); break;
            case 3: canvas.drawArc(rr3,(float) 45 * (convertedPos)+ 1-112-45, (float) 45-2, false, paintHighlightTask); break;
        }
    }

    int unitAngle = 360 / Constants.TWIST_INTERVALS;
    void DrawExpTwist(Canvas canvas){
        canvas.drawCircle(halfScreen, halfScreen, halfScreen - 2, strokeWhite);
        canvas.drawCircle(halfScreen, halfScreen, halfScreen - 50, strokeWhite);
        canvas.drawArc(circle, 0, 360, false, strokeWhiteThick);
        for (int i = 0; i < Constants.TWIST_INTERVALS; i++) {
            if(i==currentTrialPos-1){
                canvas.drawArc(circle,(float) unitAngle*(i)+1-90, (float) unitAngle-1, false, strokeBlack);
                canvas.drawArc(circle,(float) unitAngle*(i)+1-90, (float) unitAngle-1, false, paintHighlightTask); //+1 and -2 = minor gaps?
            } else {
                canvas.drawArc(circle,(float) unitAngle*(i)+1-90, (float) unitAngle-1, false, strokeBlack);
            }
        }
    }

    void DrawExpPanning(Canvas canvas){
        canvas.drawLine(halfScreen - disX, halfScreen - disY, halfScreen + disX, halfScreen + disY, strokeWhite); // 4 white diagonal lines
        canvas.drawLine(halfScreen - disY, halfScreen - disX, halfScreen + disY, halfScreen + disX, strokeWhite);
        canvas.drawLine(halfScreen - disX, halfScreen + disY, halfScreen + disX, halfScreen - disY, strokeWhite);
        canvas.drawLine(halfScreen - disY, halfScreen + disX, halfScreen + disY, halfScreen - disX, strokeWhite);

        int convertedPos = currentTrialPos;

        canvas.drawArc(r1, (float) 45 * (convertedPos) + 1-112-45, (float) 45-2, false, strokeWhite); // 4 levels white thin arcs
        canvas.drawArc(r2, (float) 45 * (convertedPos) + 1-112-45, (float) 45-2, false, strokeWhite);
        canvas.drawArc(r3, (float) 45 * (convertedPos) + 1-112-45, (float) 45-2, false, strokeWhite);
        canvas.drawArc(r4, (float) 45 * (convertedPos) + 1-112-45, (float) 45-2, false, strokeWhite);

        switch (currentTrialLvl) {
            case 1: canvas.drawArc(rr1,(float) 45 * (convertedPos)+ 1-112-45, (float) 45-2, false, paintHighlightTask); break;
            case 2: canvas.drawArc(rr2,(float) 45 * (convertedPos)+ 1-112-45, (float) 45-2, false, paintHighlightTask); break;
            case 3: canvas.drawArc(rr3,(float) 45 * (convertedPos)+ 1-112-45, (float) 45-2, false, paintHighlightTask); break;
        }
    }

    boolean initialized = false;
    RectF r1 = new RectF();
    RectF r2 = new RectF();
    RectF r3 = new RectF();
    RectF r4 = new RectF();
    RectF rr1 = new RectF();
    RectF rr2 = new RectF();
    RectF rr3 = new RectF();
    RectF rr4 = new RectF();
    float disX, disY;
    void InitExpVariables(DrawModes dm){
        int lvl1 = 20, lvl2 = 40, lvl3 = 60, lvl4 = 80, lvl5 = 130;
        if (dm == DrawModes.TWIST) {
            paintHighlightTask.setStrokeWidth(40);
            strokeBlue.setStrokeWidth(40);
            strokeBlue.setStrokeCap(Paint.Cap.SQUARE);
        } else if (dm == DrawModes.PANNING) {
            lvl1 = 30; lvl2 = 60; lvl3 = 90; lvl4 = 120;
            paintHighlightTask.setStrokeWidth(30);
        }
        disX = (float)Math.sin(Math.toRadians(22.5)) * lvl4;
        disY = (float)Math.sin(Math.toRadians(67.5)) * lvl4;
        r1.set(halfScreen - (lvl1), halfScreen - (lvl1),halfScreen + (lvl1), halfScreen + (lvl1));
        r2.set(halfScreen - (lvl2), halfScreen - (lvl2),halfScreen + (lvl2), halfScreen + (lvl2));
        r3.set(halfScreen - (lvl3), halfScreen - (lvl3),halfScreen + (lvl3), halfScreen + (lvl3));
        r4.set(halfScreen - (lvl4), halfScreen - (lvl4),halfScreen + (lvl4), halfScreen + (lvl4));
        int halfThick = lvl1 / 2;
        rr1.set(halfScreen - lvl2 + halfThick, halfScreen - lvl2 + halfThick, halfScreen + lvl2 - halfThick, halfScreen + lvl2 - halfThick);
        rr2.set(halfScreen - lvl3 + halfThick, halfScreen - lvl3 + halfThick, halfScreen + lvl3 - halfThick, halfScreen + lvl3 - halfThick);
        rr3.set(halfScreen - lvl4 + halfThick, halfScreen - lvl4 + halfThick, halfScreen + lvl4 - halfThick, halfScreen + lvl4 - halfThick);
        rr4.set(halfScreen - lvl5 + halfThick, halfScreen - lvl5 + halfThick, halfScreen + lvl5 - halfThick, halfScreen + lvl5 - halfThick);
        initialized = true;
    }

}

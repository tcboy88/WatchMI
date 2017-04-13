package hsyeo.watchmi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ActivityTechPanning extends WearableActivity implements SensorEventListener {

    private BoxInsetLayout mContainerView;
    private DrawView drawView;
    private TextView tv1, tv2, tv3;
    TwoFingersDoubleTapDetector twoFingersListener;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private float[] mRotationMatrix = new float[16];
    float[] rotValues = new float[3];
    double yaw, pitch, roll;
    float x = 0, y = 0, z = 0;
    float startX, startY, startZ;
    float diffX, diffY, diffZ;

    boolean started = false;
    boolean stopped = true;
    boolean isStudy = false;
    boolean reset = false;

    int trialCount = 0;
    Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core_stub);
//region Bundle getIntent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("study") == true) {
                isStudy = true;
                Toast.makeText(getApplicationContext(), "User study mode", Toast.LENGTH_SHORT).show();
            } else isStudy = false;
        }
//endregion
        WatchViewStub stub = (WatchViewStub) findViewById(R.id.stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mContainerView = (BoxInsetLayout) findViewById(R.id.container);
                setTouchListener();
                drawView = (DrawView) findViewById(R.id.draw_view);
                drawView.setShape(mContainerView.isRound());
                drawView.setDrawModes(DrawView.DrawModes.PANNING);
                drawView.setStudyModes(isStudy);
                tv1 = (TextView) findViewById(R.id.tv1);
                tv2 = (TextView) findViewById(R.id.tv2);
                tv3 = (TextView) findViewById(R.id.tv3);
                if (!isStudy) {
                    tv1.setText("Grab the watch and PAN like a joystick, keep 1 finger on screen (anywhere is ok)!");
                    tv3.setText("double tap with 2 fingers to exit");
                } else {
                    InitTrials();
                    SetNextTrial();
                    tv1.setText("Correct/Wrong");
                    tv3.setText("Trial:" + trialCount);
                }
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        if (mSensor == null) Log.e("YEO", "Failed to attach to sensor.");
//region Exit by two fingers double tap
        twoFingersListener = new TwoFingersDoubleTapDetector() {
            @Override
            public void onTwoFingersDoubleTap() {
//                Toast.makeText(getApplicationContext(), "Exit by Two Fingers Double Tap", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), ActivitySelectTech.class);
                if (isStudy) {
                    Bundle b = new Bundle();
                    b.putBoolean("study", isStudy);
                    i.putExtras(b);
                }
                startActivity(i);
                finish();
            }
        };
//endregion
        util = new Util();
    }

    void setTouchListener() {
        mContainerView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                twoFingersListener.onTouchEvent(event);

                float x = event.getX();
                float y = event.getY();

                int maskedAction = event.getActionMasked();
                switch (maskedAction) {
                    case MotionEvent.ACTION_DOWN:
                        started = true;
                        stopped = false;
                        return true;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        started = false;
                        stopped = true;
                        return true;
                }
                return false;
            }
        });
    }

    //region Not important, onResume(), onPause(), onAccuracyChanged()
    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    //endregion
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) { return; }

        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
            SensorManager.getOrientation(mRotationMatrix, rotValues);
            yaw = rotValues[0];
            pitch = rotValues[1];
            roll = -rotValues[2];
            int inclination = (int) Math.round(Math.toDegrees(Math.acos(mRotationMatrix[10])));
            if (inclination > 90) {
                // face down
                // when inclination near 90 (such as 85 or 95), roll is very sensitive
                // because it goes from 0 to pi suddenly
                pitch = -pitch;
            }
            x = -(float)roll;
            y = -(float)pitch;
            z = (float)yaw;

            if (started) {
                startX = x;
                startY = y;
                startZ = z;
                started = false; // only need get value once when touched down, don't constantly update
            }

            if (!stopped) {
                diffX = x - startX;
                diffY = y - startY;
                diffZ = z - startZ;

                if (isStudy)
                if (trialCount <= Constants.NUM_TRIALS)
                    Checking(diffX * Constants.MAGIC_XY, diffY * Constants.MAGIC_XY);

                drawView.setXYZ(diffX, diffY, diffZ);

                if (reset){
                    diffX = diffY = diffZ = 0;
                    drawView.setXYZ(0, 0, 0);
                    stopped = true;
                    reset = false;
                }
            }else {
                diffX = diffY = diffZ = 0;
            }

            drawView.invalidate();
        }
    }

    List<Pair<Integer,Integer>> trials = new ArrayList<>();
    void InitTrials(){
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 3; j++) {
                Pair<Integer, Integer> simplePair = new Pair<>(i, j);
                trials.add(simplePair);
            }
        }
        long seed = System.nanoTime();
        Collections.shuffle(trials, new Random(seed));
    }
    void SetNextTrial(){
        if (trialCount >= Constants.NUM_TRIALS) {
            Toast.makeText(getApplicationContext(), "THANKS " + trialCount, Toast.LENGTH_SHORT).show();
            tv2.setText("THANKS, GOODBYE");
        } else {
            currentTrialPos = trials.get(trialCount).first;
            currentTrialLvl = trials.get(trialCount).second;
            drawView.setTrial(currentTrialPos, currentTrialLvl);
            trialCount++;
        }
    }

    int pos = 0, lvl = 0;
    int currentTrialPos = 0, currentTrialLvl = 0;
    int previousPos = 0, previousLvl = 0;
    long currentTime, startTime, askTime = 0;
    void Checking(float x, float y){
        double angle = (Math.toDegrees(Math.atan2(y, x)) + 360 + 90) % 360;
        pos = (int)((angle + 22.5)/ 45) + 1;
        if (pos == 9) pos = 1; // quick hack
        lvl = util.DetermineLevel(x, y, 30, 30);

        if (pos == currentTrialPos && lvl == currentTrialLvl)
            drawView.paintHighlightTask.setColor(Color.argb(150, 0, 255, 0));
        else drawView.paintHighlightTask.setColor(Color.argb(150, 255, 0, 0));

        if (lvl != 0){ // started?
            if (lvl != previousLvl || pos != previousPos){ // enter new box
                startTime = System.currentTimeMillis();
                previousLvl = lvl;
                previousPos = pos;
            } else {
                currentTime = System.currentTimeMillis();
                if (currentTime - startTime >= Constants.TIME_OUT) {
                    if (pos == currentTrialPos && lvl == currentTrialLvl){
                        tv1.setText("CORRECT");
                        SetNextTrial();
                    } else {
                        tv1.setText("WRONG");
                    }

                    drawView.paintHighlightTask.setColor(Color.argb(150, 255, 0, 0));

                    pos = lvl = previousPos = previousLvl = 0;
                    reset = true;
                    tv3.setText(trialCount + "/" + Constants.NUM_TRIALS + " Pos:" + currentTrialPos + "Lvl:" + currentTrialLvl);
                    askTime = System.currentTimeMillis();
                }
            }
        } else { // level = 0
            previousPos = previousLvl = 0;
        }
    }
}

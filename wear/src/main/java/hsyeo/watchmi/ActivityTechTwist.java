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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ActivityTechTwist extends WearableActivity implements SensorEventListener {

    private BoxInsetLayout mContainerView;
    private DrawView drawView;
    private TextView tv1, tv2, tv3;
    TwoFingersDoubleTapDetector twoFingersListener;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private float[] mRotationMatrix = new float[16];
    float[] rotValues = new float[3];
    double yaw;
    float startZ;
    float diffZ;

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
                drawView = (DrawView) findViewById(R.id.draw_view);
                mContainerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        int mChinSize = insets.getSystemWindowInsetBottom();
                        v.onApplyWindowInsets(insets);
                        drawView.setShape(mContainerView.isRound(), mChinSize);
                        drawView.setDrawModes(DrawView.DrawModes.TWIST);
                        drawView.setStudyModes(isStudy);
                        return insets;
                    }
                });
                setTouchListener();
                tv1 = (TextView) findViewById(R.id.tv1);
                tv2 = (TextView) findViewById(R.id.tv2);
                tv3 = (TextView) findViewById(R.id.tv3);
                if (!isStudy) {
                    tv1.setText("Grab the watch and TWIST left and right, keep 1 finger on screen (anywhere is ok)!");
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
        if (mSensor == null) {
            Log.w("YEO", "Failed to attach to game rot vec.");
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            if (mSensor == null)
                Log.w("YEO", "Failed to attach to rot vec.");
        }
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

        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR || event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
            SensorManager.getOrientation(mRotationMatrix, rotValues);
            yaw = rotValues[0];
            float currentZ = (float)yaw + (float)(Math.PI * 2); // make it 0 to 6.284, easier to understand

            int inclination = (int) Math.round(Math.toDegrees(Math.acos(mRotationMatrix[10])));
            if (inclination > 90) {
                // face down
                // when inclination near 90 (such as 85 or 95), roll is very sensitive
                currentZ = -currentZ;
            }

            if (started) {
                startZ = currentZ;
                started = false; // only need get value once when touched down, don't constantly update
            }

            if (!stopped) {
                diffZ = currentZ - startZ;

                if (diffZ < -Math.PI){ // this is to avoid sudden jump when crossing zero
                    diffZ += (Math.PI * 2);
                } else if (diffZ > Math.PI){
                    diffZ -= (Math.PI * 2);
                }

                if (isStudy)
                if (trialCount <= Constants.NUM_TRIALS)
                    Checking(diffZ * Constants.MAGIC_TWIST);

                drawView.setXYZ(0, 0, diffZ);

                if (reset) {
                    diffZ = 0;
                    drawView.setXYZ(0, 0, 0);
                    stopped = true;
                    reset = false;
                }
            } else {
                diffZ = 0;
            }
            if (drawView != null)
            drawView.invalidate();
        }
    }

    List<Integer> trials = new ArrayList<>();
    void InitTrials(){
        for (int j = 0; j < Constants.TWIST_INTERVALS; j++) {
            trials.add(j + 1);
        }
        long seed = System.nanoTime();
        Collections.shuffle(trials, new Random(seed));
    }
    void SetNextTrial(){
        if (trialCount >= Constants.NUM_TRIALS) {
            Toast.makeText(getApplicationContext(), "THANKS " + trialCount, Toast.LENGTH_SHORT).show();
            tv2.setText("THANKS, GOODBYE");
        } else {
            currentTrialPos = trials.get(trialCount);
            drawView.setTrial(currentTrialPos, 0);
            trialCount++;
        }
    }

    int pos = 0;
    int currentTrialPos;
    int previousPos = 0;
    int unitAngle = 360 / Constants.TWIST_INTERVALS;
    long currentTime, startTime, askTime = 0;
    void Checking(float z){
        float value = (z<0)? 360+z:z; // clockwise and anti-clockwise
        pos = (int)(value / unitAngle) + 1;

        if (pos == currentTrialPos)
            drawView.paintHighlightTask.setColor(Color.argb(150, 0, 255, 0));
        else drawView.paintHighlightTask.setColor(Color.argb(150, 255, 0, 0));

        if (pos != previousPos){
            startTime = System.currentTimeMillis(); // restart timer whenever changed box
            previousPos = pos;
        } else { // remained in the same box, check elapsed time
            currentTime = System.currentTimeMillis();
            if (currentTime - startTime >= Constants.TIME_OUT) { // remained in box more than 1000ms
                if (pos == currentTrialPos) {
                    tv1.setText("CORRECT");
                    SetNextTrial();
                } else {
                    tv1.setText("WRONG");
                }

                pos = previousPos = -1;
                reset = true;

                drawView.paintHighlightTask.setColor(Color.argb(150, 255, 0, 0));
                tv3.setText(trialCount + "/" + Constants.NUM_TRIALS + " Pos:" + currentTrialPos);
                askTime = System.currentTimeMillis();
            }
        }
    }
}

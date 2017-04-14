package hsyeo.watchmi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ActivityAppsClock extends WearableActivity implements SensorEventListener {

    private BoxInsetLayout mContainerView;
    private DrawViewApps drawView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core_stub);

        WatchViewStub stub = (WatchViewStub) findViewById(R.id.stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mContainerView = (BoxInsetLayout) findViewById(R.id.container);
                drawView = (DrawViewApps) findViewById(R.id.draw_view_apps);
                mContainerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        int mChinSize = insets.getSystemWindowInsetBottom();
                        v.onApplyWindowInsets(insets);
                        drawView.setShape(mContainerView.isRound(), mChinSize);
                        drawView.setDrawModes(DrawViewApps.DrawModes.CLOCK);
                        return insets;
                    }
                });
                setTouchListener();
                tv1 = (TextView) findViewById(R.id.tv1);
                tv2 = (TextView) findViewById(R.id.tv2);
                tv3 = (TextView) findViewById(R.id.tv3);
                tv1.setText("TOUCH & TWIST to rotate clock hand!");

                Drawable drawable = getResources().getDrawable(R.drawable.watchmi); // ?
                drawView.setImageDrawable(drawable);
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
                Intent i = new Intent(getApplicationContext(), ActivitySelectApps.class);
                startActivity(i);
                finish();
            }
        };
//endregion
    }

    void setTouchListener() {
        mContainerView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                twoFingersListener.onTouchEvent(event);

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
            float currentZ = (float)yaw + (float)(Math.PI * 2); // make it 0 to 6.284
            int inclination = (int) Math.round(Math.toDegrees(Math.acos(mRotationMatrix[10])));
            if (inclination > 90) {
                // faced down, when inclination near 90 (such as 85 or 95), roll is very sensitive
                // because it goes from 0 to pi suddenly
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
                int zz = (int)(diffZ*36);
                int adjust_min = zz * 5;

                drawView.addSecond(adjust_min);
            } else {
                diffZ = 0;
            }

            if (drawView != null)
            drawView.invalidate();
        }
    }
}

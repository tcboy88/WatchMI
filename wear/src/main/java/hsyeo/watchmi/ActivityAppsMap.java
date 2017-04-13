package hsyeo.watchmi;

import android.content.Context;
import android.content.Intent;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityAppsMap extends WearableActivity implements SensorEventListener {

    private BoxInsetLayout mContainerView;
    private DrawViewApps drawView;
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

    float curX,curY;
    long last_activation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core_stub);

        WatchViewStub stub = (WatchViewStub) findViewById(R.id.stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mContainerView = (BoxInsetLayout) findViewById(R.id.container);
                setTouchListener();
                drawView = (DrawViewApps) findViewById(R.id.draw_view_apps);
                drawView.setShape(mContainerView.isRound());
                drawView.setDrawModes(DrawViewApps.DrawModes.MAP);
                tv1 = (TextView) findViewById(R.id.tv1);
                tv2 = (TextView) findViewById(R.id.tv2);
                tv3 = (TextView) findViewById(R.id.tv3);
                tv1.setText("PRESSURE to move, TWIST to zoom!");

                Drawable drawable = getResources().getDrawable(R.drawable.map);
                drawView.setImageDrawable(drawable);
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

                        curX = event.getX();
                        curY = event.getY();
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
            float currentZ = (float)yaw + (float)(Math.PI * 2); // make it 0 to 6.284, easier to understand
            int inclination = (int) Math.round(Math.toDegrees(Math.acos(mRotationMatrix[10])));
            if (inclination > 90) {
                // faced down, when inclination near 90 (such as 85 or 95), roll is very sensitive
                // because it goes from 0 to pi suddenly
                currentZ = -currentZ;
                pitch = -pitch;
            }

            x = -(float)roll;
            y = -(float)pitch;

            if (started) {
                startX = x;
                startY = y;
                startZ = currentZ;
                started = false; // only need get value once when touched down, don't constantly update
            }

            if (!stopped && (System.currentTimeMillis()-last_activation>10)) {
                diffX = x - startX;
                diffY = y - startY;
                diffZ = currentZ - startZ;

                if (diffZ < -Math.PI){ // this is to avoid sudden jump when crossing zero
                    diffZ += (Math.PI * 2);
                } else if (diffZ > Math.PI){
                    diffZ -= (Math.PI * 2);
                }

                float conv_x, conv_y, conv_z;
                conv_x = ((float)0.05*diffX*800)-(float)0.5;
                conv_y = ((float)0.05*diffY*800)-(float)0.5;
                conv_z = (diffZ*600);

                Math.max(conv_x, 3);
                Math.max(conv_y, 3);

                float k = Math.abs(conv_x)+Math.abs(conv_y);

                long old = last_activation;
                last_activation = System.currentTimeMillis();
                int zz = (int)Math.abs(conv_z)/30;

                if(k>3 && k<5){
                    drawView.translateImage(conv_x,conv_y,(float)0.3);
                }else if(k>=5 && k<8){
                    drawView.translateImage(conv_x,conv_y,(float)0.5);
                }else if(k>=8 ){
                    drawView.translateImage(conv_x,conv_y,(float)0.7);
                }else if(zz>=1 && zz<2){
                    drawView.changeScaleFactor((diffZ>0)?(float)1.01:(float)0.99,curX,curY);
                }else if(zz>=2 && zz<3){
                    drawView.changeScaleFactor((diffZ>0)?(float)1.015:(float)0.98,curX,curY);
                }else if(zz>=3){
                    drawView.changeScaleFactor((diffZ>0)?(float)1.02:(float)0.97,curX,curY);
                }else{
                    last_activation = old;
                }

                drawView.setXYZ(diffX, diffY, diffZ);
            }else {
                diffX = diffY = diffZ = 0;
            }

            drawView.invalidate();
        }
    }
}

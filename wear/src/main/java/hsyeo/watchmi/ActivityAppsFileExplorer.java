package hsyeo.watchmi;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ActivityAppsFileExplorer extends WearableActivity implements SensorEventListener {

    private BoxInsetLayout mContainerView;
    TwoFingersDoubleTapDetector twoFingersListener;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private float[] mRotationMatrix = new float[16];
    float[] rotValues = new float[3];
    double pitch, roll;
    float x = 0, y = 0;
    float startX, startY;
    float diffX, diffY;

    boolean started = false;
    boolean stopped = true;

    String[] first_step = {"2016","2015","2014","2013","2012","2011","2010"
            ,"2009","2008","2007","2006","2005","2004","2003","2002","2001","2000"
            ,"1999","1998"};
    String[] second_step = {"December","November","October","September","August","July","June","May","April","March","February","January"};
    String[] third_step = {"Photo 1","Photo 2","Photo 3","Photo 4","Photo 5","Photo 6","Photo 7","Photo 8","Photo 9"};
    ArrayList<String[]> steps = new ArrayList<>();

    private ListView listview;
    private CustomAdapter adapter;

    int cur_step = 0, old_step = 0;
    ImageView last_img;

    int cur_position;
    long last_activation;
    long change_list;
    int for_back;   //0:no, 1:back, 2:forward

    Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_file_explorer);

//        mContainerView = (BoxInsetLayout) findViewById(R.id.container);

        adapter = new CustomAdapter();
        listview = (ListView) findViewById(R.id.listview);
        listview.setAdapter(adapter);
        steps.add(first_step);
        steps.add(second_step);
        steps.add(third_step);
        last_img = (ImageView) findViewById(R.id.final_image);
        last_img.setVisibility(View.INVISIBLE);
        setTouchListener();

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
        util = new Util();
        refreshListView();
    }

    void setTouchListener() {
        listview.setOnTouchListener(new View.OnTouchListener() {
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
        last_img.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                twoFingersListener.onTouchEvent(event);

                int maskedAction = event.getActionMasked();
                switch (maskedAction) {
                    case MotionEvent.ACTION_DOWN:
                        started = true;
                        stopped = false;
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
            pitch = rotValues[1];
            roll = -rotValues[2];
            int inclination = (int) Math.round(Math.toDegrees(Math.acos(mRotationMatrix[10])));
            if (inclination > 90) {
                pitch = -pitch;
            }

            x = -(float)roll;
            y = -(float)pitch;

            if (started) {
                startX = x;
                startY = y;
                started = false; // only need get value once when touched down, don't constantly update
            }

            if (!stopped && (System.currentTimeMillis()-last_activation>50)) {
                diffX = x - startX;
                diffY = y - startY;
                double angle = (Math.toDegrees(Math.atan2(diffY * 800, diffX * 800)) + 360 + 90) % 360;
                int direction = (int)((angle + 22.5)/ 45) + 1;
                if (direction == 9) direction = 1; // quick hack
                int panning_level = util.DetermineLevel(diffX * 800, diffY * 800, 30, 30);

                if(panning_level>0){
                    if (direction == 1) { //up
                        cur_position--;
                        cur_position = (cur_position<0)? 0:cur_position;
                        adapter.setCurrentPos(cur_position);
                        listview.smoothScrollToPosition(cur_position);
                        last_activation = System.currentTimeMillis() +50*(4-panning_level);
                    } else if (direction == 5) { //down
                        cur_position++;
                        cur_position = (cur_position> adapter.getCount()-1)? adapter.getCount()-1:cur_position;
                        adapter.setCurrentPos(cur_position);
                        listview.smoothScrollToPosition(cur_position);
                        last_activation = System.currentTimeMillis() +50*(4-panning_level);
                    } else if (direction == 3 && panning_level > 1) { //right
                        Animation slideout = AnimationUtils.loadAnimation(this, R.anim.anim_slide_out_left);
                        listview.startAnimation(slideout);
                        change_list = System.currentTimeMillis();
                        for_back = 2;
                    } else if (direction == 7 && panning_level > 1){ //left
                        Animation slideout = AnimationUtils.loadAnimation(this, R.anim.anim_slide_out_right);
                        listview.startAnimation(slideout);
                        change_list = System.currentTimeMillis();
                        for_back = 1;
                    }
                    adapter.notifyDataSetChanged();
                }
            } else {
                diffX = diffY = 0;
            }

            if(for_back!=0&&(System.currentTimeMillis()-change_list>300)){
                if (for_back == 1) {
                    cur_step--;
                    refreshListView();
                    Animation slidein = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in_right);
                    listview.startAnimation(slidein);
                }else if(for_back==2){
                    cur_step++;
                    refreshListView();
                    Animation slidein = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in_left);
                    listview.startAnimation(slidein);
                }
                for_back = 0;
            }
        }
    }

    void refreshListView(){
        if(adapter !=null)
            adapter.clear();

        cur_step = Math.max(0, cur_step);
        cur_step = Math.min(3, cur_step);

        if(cur_step==3){
            listview.setVisibility(View.INVISIBLE);
            last_img.setVisibility(View.VISIBLE);
            Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
            last_img.startAnimation(fadein);
        } else{
            if(old_step==3){
                listview.setVisibility(View.VISIBLE);
                Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.fadeout);
                last_img.startAnimation(fadeout);
                last_img.setVisibility(View.INVISIBLE);
            }

            String[] tmp = steps.get(cur_step);

            for (int i = 0; i < tmp.length; i++) {
                adapter.add(tmp[i]);
            }

            adapter.setCurrentPos(cur_position);
            adapter.notifyDataSetChanged();
        }

        old_step = cur_step;
    }
}

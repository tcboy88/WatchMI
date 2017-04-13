package hsyeo.watchmi;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ActivitySelectTech extends WearableActivity {

    private Button btn1, btn2, btn3, btnBack;
    private boolean isStudy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_tech);
//        setAmbientEnabled();
        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(button1ClickListener);
        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(button2ClickListener);
        btn3 = (Button) findViewById(R.id.btn3);
        btn3.setOnClickListener(button3ClickListener);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(buttonBackClickListener);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if (extras.getBoolean("study") == true) {
                isStudy = true;
                Log.e("YEO","isStudy = true");
            } else {
                isStudy = false;
                Log.e("YEO","isStudy = false");
            }
        }
    }

    Button.OnClickListener button1ClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            b.putBoolean("study", isStudy);
            Intent i = new Intent(getApplicationContext(),  ActivityTechPressure.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };
    Button.OnClickListener button2ClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            b.putBoolean("study", isStudy);
            Intent i = new Intent(getApplicationContext(),  ActivityTechTwist.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };
    Button.OnClickListener button3ClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            b.putBoolean("study", isStudy);
            Intent i = new Intent(getApplicationContext(),  ActivityTechPanning.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };
    Button.OnClickListener buttonBackClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Intent i = new Intent(getApplicationContext(), ActivityMain.class);
            startActivity(i);
            finish();
        }
    };
}


package com.lm2a.crashdeterctor.gps;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.lm2a.crashdeterctor.AccelerometerListener;
import com.lm2a.crashdeterctor.AccelerometerManager;
import com.lm2a.crashdeterctor.R;
import com.lm2a.crashdeterctor.SplashScreenActivity;


public class CrashActivity extends Activity implements AccelerometerListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//Do your operation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        // Check
        // onResume Method to start accelerometer listener

       TextView tv = (TextView) findViewById(R.id.textView3);
       tv.setText(Html.fromHtml("<b>The AA Ireland</b><br />" +
                "<small>Innovation Mobile Team</small><br />" +
                "<small>" + "14 August 2016" + "</small>"));
    }

    public void onAccelerationChanged(float x, float y, float z) {
        // TODO Auto-generated method stub

    }

    public void onShake(float force) {

        // Do your stuff here

        // Called when Motion Detected
//        Toast.makeText(getBaseContext(), "Motion detected",
//                Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, SplashScreenActivity.class);
        startActivity(i);

    }

    @Override
    public void onResume() {
        super.onResume();
//        Toast.makeText(getBaseContext(), "onResume Accelerometer Started",
//                Toast.LENGTH_SHORT).show();

        //Check device supported Accelerometer senssor or not
        if (AccelerometerManager.isSupported(this)) {

            //Start Accelerometer Listening
            AccelerometerManager.startListening(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        //Check device supported Accelerometer senssor or not
        if (AccelerometerManager.isListening()) {

            //Start Accelerometer Listening
            AccelerometerManager.stopListening();

//            Toast.makeText(getBaseContext(), "onStop Accelerometer Stoped",
//                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Sensor", "Service  distroy");

        //Check device supported Accelerometer senssor or not
        if (AccelerometerManager.isListening()) {

            //Start Accelerometer Listening
            AccelerometerManager.stopListening();

//            Toast.makeText(getBaseContext(), "onDestroy Accelerometer Stoped",
//                    Toast.LENGTH_SHORT).show();
        }

    }

}
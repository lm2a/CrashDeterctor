package com.lm2a.crashdeterctor;

import java.util.Timer;
import java.util.TimerTask;
 
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class SplashScreenActivity extends Activity {
 
    // Set the duration of the splash screen
    private static final long SPLASH_SCREEN_DELAY = 15000;
    Timer timer;
    boolean activated=true;
    ToneGenerator toneG;

    Ringtone r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        // Set portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

//        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
//        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        setContentView(R.layout.splash_layout);
//        Button b = (Button) findViewById(R.id.button);



//        b.setOnClickListener(new Button.OnClickListener() {
//            public void onClick(View v) {
//
//                activated=false;
//
//                timer.cancel();
//                new AlertDialog.Builder(SplashScreenActivity.this)
//                        .setTitle("Crash notification deactivated")
//                        .setMessage("The crash notification was deactivated. Messages will not be sent.")
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//
//                                finish();
//                            }
//                        })
//
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .show();
//                finish();
//            }
//        });


        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
//                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                if(activated) {

//                    toneG.stopTone();
                    // Start the next activity
                    r.stop();
                    Intent mainIntent = new Intent(SplashScreenActivity.this, SendMessageActivity.class);//.setClass(
                    //SplashScreenActivity.this, SendMessageActivity.class);
                    startActivity(mainIntent);

                    // Close the activity so the user won't able to go back this
                    // activity pressing Back button
                    finish();
                }

            }
        };

        timer.schedule(task, SPLASH_SCREEN_DELAY);

 
//        // Simulate a long loading process on application startup.
//        Timer timer = new Timer();
//        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }


    @Override
    public void onResume() {
        super.onResume();






    }

    @Override
    public void onStop() {
        super.onStop();



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Sensor", "Service  destroy");


    }

    public void noCrash(View v){

        activated=false;
        r.stop();
        timer.cancel();

//        new AlertDialog.Builder(this)
//                .setTitle("Crash notification deactivated")
//                .setMessage("The crash notification was deactivated. Messages will not be sent.")
//                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        finish();
//                    }
//                })
//
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show();
        finish();
    }


}
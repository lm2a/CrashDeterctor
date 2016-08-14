package com.lm2a.crashdeterctor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;



public class MonitoringActivity extends Activity implements LocationListener, AccelerometerListener {


    private static final String TAG = "->lm2a-MonitoringActivity";
    private EditText editLocation = null;
    private ProgressBar pb =null;
    private boolean flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);


        ((TheAAApp)getApplication()).sMyLocation.addListener(this);

        setRequestedOrientation(ActivityInfo
                .SCREEN_ORIENTATION_PORTRAIT);

        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(View.INVISIBLE);

        editLocation = (EditText) findViewById(R.id.editTextLocation);

        flag = displayGpsStatus();
        if (flag) {
            editLocation.setText("Please!! move your device to"+
                    " see the changes in coordinates."+"\nWait..");

            pb.setVisibility(View.VISIBLE); 
        }else {
            alertbox("Gps Status!!", "Your GPS is: OFF");
        }

        //--------- SHOCK
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//Do your operation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        // Check onResume Method to start accelerometer listener

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.monitoring, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*----Method to Check GPS is enable or disable ----- */
    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }


    /*----------Method to create an AlertBox ------------- */
    protected void alertbox(String title, String mymessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your Device's GPS is Disable")
                .setCancelable(false)
                .setTitle("** Gps Status **")
                .setPositiveButton("Gps On",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // finish the current activity
                                // AlertBoxAdvance.this.finish();
                                Intent myIntent = new Intent(
                                        Settings.ACTION_SECURITY_SETTINGS);
                                startActivity(myIntent);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // cancel the dialog box
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void positionReached(){
        pb.setVisibility(View.INVISIBLE);
        String s = "You are in: "+((TheAAApp)getApplication()).lastLocation;
        ((TheAAApp)getApplication()).shockeable=true;
        editLocation.setText(s);
    }

    //-------------- LOCATION LISTENER------------------------------------------------

    @Override
    public void onLocationChanged(Location location) {
        positionReached();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    //-------------- SHOCK LISTENER------------------------------------------------
    @Override
    public void onAccelerationChanged(float x, float y, float z) {

    }

    int n=0;
    @Override
    public void onShake(float force) {
        Log.i(TAG, "");
        if(((TheAAApp)getApplication().getApplicationContext()).shockeable){

            // Called when Motion Detected
//            Toast.makeText(getBaseContext(), "Motion detected",
//                    Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, SplashScreenActivity.class);
            startActivity(i);
        }

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
        Log.i("Sensor", "Service  destroy");

        //Check device supported Accelerometer senssor or not
        if (AccelerometerManager.isListening()) {

            //Start Accelerometer Listening
            AccelerometerManager.stopListening();

//            Toast.makeText(getBaseContext(), "onDestroy Accelerometer Stoped",
//                    Toast.LENGTH_SHORT).show();
        }

    }
}

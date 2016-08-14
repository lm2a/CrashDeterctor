package com.lm2a.crashdeterctor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
 
public class SendMessageActivity extends Activity {

    public static final String PHONE_TO_CALL="0831632016";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sent_message_layout);

        sendSMS(PHONE_TO_CALL, "XXX has a crash accident in: "+((TheAAApp) getApplication()).lastLocation);
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send, menu);
        return true;
    }

    private void sendSMS(String phoneNumber, String message)
    {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }
 
}
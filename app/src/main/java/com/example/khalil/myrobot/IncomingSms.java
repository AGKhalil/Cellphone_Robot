package com.example.khalil.myrobot;

/**
 * Created by Khalil on 4/7/17.
 * This class receives an SMS and sends it to TaskActivity using an EventBus.
 *
 * Although changes have been made to this code, the main bulk of it is due to courtesy of:
 * http://androidexample.com/Incomming_SMS_Broadcast_Receiver_-_Android_Example/index.php?view=article_discription&aid=62
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

public class IncomingSms extends BroadcastReceiver {

    public String message;  // The SMS variable.

    /**
     * Public constructor.
     */
    public IncomingSms () {
    }

    /**
     * This method receives the SMS and sends it to TaskActivity as an EventBus.
     */
    public void onReceive(Context context, Intent intent) {
        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int j = 0; j < pdusObj.length; j++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[j]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    message = currentMessage.getDisplayMessageBody();

                    Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);

                    // This block takes care of sending the SMS to TaskActivity.
                    EventBus.getDefault().post(new TaskActivity.OnReceiverEvent(message));
                    Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);
        }
    }
}

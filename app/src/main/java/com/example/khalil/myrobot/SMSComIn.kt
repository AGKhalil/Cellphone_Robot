package com.example.khalil.myrobot

/**
 * Created by Khalil on 4/7/17.
 * This class receives an SMS and sends it to TaskActivity using an EventBus.
 * Although changes have been made to this code, the main bulk of it is due to courtesy of:
 * http://androidexample.com/Incomming_SMS_Broadcast_Receiver_-_Android_Example/index.php?view=article_discription&aid=62
 */

/**
 * Modified by Michael.
 * This class serves as the communication class for the main HUB.
 */

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast

/**
 * Public constructor.
 */
class SMSComIn : BroadcastReceiver() {

    var message: String = ""  // The SMS variable.

    /**
     * This method receives the SMS and sends it to TaskActivity as an EventBus.
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Retrieves a map of extended data from the intent.
        val bundle = intent.extras
        try {
            if (bundle != null) {
                val pdusObj = bundle.get("pdus") as Array<Any>

                for (j in pdusObj.indices) {

                    val currentMessage = SmsMessage.createFromPdu(pdusObj[j] as ByteArray)
                    val phoneNumber = currentMessage.displayOriginatingAddress

                    val senderNum = phoneNumber
                    message = currentMessage.displayMessageBody

                    Log.i("SmsReceiver", "senderNum: $senderNum; message: $message")

                    /*// This block takes care of sending the SMS to TaskActivity.
                    EventBus.getDefault().post(TaskActivity.OnReceiverEvent(message))*/

                    // Here starts service NLP
                    val i = Intent(context, NaturalLanguageProcessService::class.java)
                    i.putExtra("msg",message)
                    i.putExtra("phonenumber",phoneNumber)
                    context.startService(i)


                    val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
                    toast.show()
                }
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e)
        }

    }
}

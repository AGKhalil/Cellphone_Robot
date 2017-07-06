package com.example.khalil.myrobot;

/**
 * Created by Khalil on 4/8/17
 * This is the app's MainActivity. Here the app's view is set up, an instance of IOIOClass is
 * created and controlled, NavigationService is called, the picture is taken, and is uploaded
 * to Twitter. This is the app's main hub.
 */

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;

public class CentralHub extends AppCompatActivity {
    public String TAG = "CentralHub";
    public String message;  // The SMS message passed in through the EventBus.
    private Intent robotDriverIntent;

    /**
     **************************** Start of Testing Methods *****************************************
     */

    /**
     * This method is used to bypass the SMS segment of the code. It starts NavigationService with
     * a mock SMS destination.
     * @param view is the button view SEND SMS
     */
    public void mockStartRobotDriver(View view){
        String msg = "Engineering Fountain";
        if (msg != null) {
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
            startRobotDriver(msg);
        }
    }

    /**
     * This method is used to test posting a tweet. It bypasses all the code and takes a picture and
     * triggers SocialPost.
     * @param view is the button view TWEET
     */
//    public void mockTweet(View view){
//        stopService(mIntent);
//        cameraFragment.takePhotoOrCaptureVideo(CentralHub.this,
//                "/storage/self/primary", "thePicture001");
//    }

    /**
     **************************** End of Testing Methods *******************************************
     */

    /**
     * This method sets up the entire app, from the different intents that will be issued throughout
     * its lifecycle to the main XML file that is issued to the user.
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub); // Sets the XML view file that appears to the user.
        robotDriverIntent = new Intent(this, RobotDriver.class); // Associates mIntent with
                // RobotDriver.

        // This IF block insures all permissions are granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.
                        PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                        != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
                        != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                        != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                        != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                        != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Toast.makeText(this, "Please grant all the permissions needed for this app to " +
                    "function fully.", Toast.LENGTH_LONG).show();
            return;
        }
    }

    /**
     * This class receives the SMS sent by the EventBus triggered by IncomingSms.
    */
    static class OnReceiverEvent {
        private String smsMessage;

        OnReceiverEvent(String sms) {
            this.smsMessage = sms;
        }

        String getSmsMessage() {
            return smsMessage;
        }
    }

    /**
     * This method is triggered when CentralHub receives the SMS. Here the NavigationService
     * is started.
    */
    @Subscribe
    public void onSmsReceived(OnReceiverEvent event) {
        message = event.getSmsMessage();

        // This IF block ensures the SMS is not null. If not, the service starts.
        if (message != null) {
            startRobotDriver(message);
        }
    }

    /**
     * This is the method that starts the RobotDriver by filing the mIntent when called.
    */
    private void startRobotDriver(String message) {
        robotDriverIntent.putExtra("message", message); // The SMS, which is the destination name, is passed
                // to RobotDriver, because it needs it to file the API request.
        Log.d(TAG,"startRobotDriver");
        this.startActivity(robotDriverIntent);  // This line is what actually starts the RobotDriver.
    }
}


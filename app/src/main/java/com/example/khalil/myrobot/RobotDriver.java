package com.example.khalil.myrobot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.camerafragment.CameraFragment;
import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.listeners.CameraFragmentResultListener;

import static com.example.khalil.myrobot.Commands.GO_FORWARDS;
import static com.example.khalil.myrobot.Commands.STOP;
import static com.example.khalil.myrobot.Commands.TURN_CLOCKWISE;
import static com.example.khalil.myrobot.Commands.TURN_COUNTERCLOCKWISE;

/**
 * Created by Khalil on 7/5/17.
 */

public class RobotDriver extends AppCompatActivity implements CameraFragmentResultListener {
    public String TAG = "CentralHub";
    private Intent navigationServiceIntent; // The intent that starts the NavigationService.
    public float direction; // The robot's bearing to the next location point.
    public String message;  // The SMS message passed in through the EventBus.
    IOIOClass myRobot;  // An instance of the robot. A setter method will be used on this instance
    private String motionDirection = "not moving";  // An initialization of the direction the robot
    // is moving in.
    public final CameraFragment cameraFragment =
            CameraFragment.newInstance(new Configuration.Builder().build()); // A camera fragment
    private CommunicationOut postToMedia = new CommunicationOut(message); // Social media object that posts to
    // social media.

    public void mockStartNavigationService(View view){
        String msg = "Engineering Fountain";
        if (msg != null) {
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
            callNavigationService(msg);
        }
    }

    public void mockTweet(View view){
        cameraFragment.takePhotoOrCaptureVideo(RobotDriver.this,
                "/storage/self/primary", "thePicture001");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        message = getIntent().getStringExtra(Commands.HUB_TO_DRIVER_DESTINATION_MESSAGE);
        setContentView(R.layout.activity_driver); // Sets the XML view file that appears to the user.
        navigationServiceIntent = new Intent(this, NavigationService.class); // Associates navigationServiceIntent with
            // NavigationService.
        myRobot = new IOIOClass(this); // Creates the robot instance from IOIOClass.
        myRobot.getIOIOAndroidApplicationHelper().create(); // Retrieves the IOIO helper, which is
            // responsible for starting the IOIO loop from another class, and creates it.
            // This allows RobotDriver to access the IOIOClass instance.

        // Attaches the camera fragment to the XML file so the user can see it.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, cameraFragment, "TheCameraThing")
                .commit();
    }

    /**
     * This BroadcastReceiver starts when an intent is sent from NavigationActivity to CentralHub.
     */
    private BroadcastReceiver navigationServiceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        // onReceive receives the intent from NavigationService
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: Broadcasting");
            updateRobotDriver(intent); // The updateRobotDriver method is started. It needs the received intent to be
            // passed to it.
        }
    };

    /**
     * This method insures the app's lifecycle doesn't get messed up if the app is restarted due to
     * a global event occurring, i.e. the phone's orientation is flipped, an SMS or an email
     * is received, a call is incoming, etc.
     */
    @Override
    public void onStart() {
        super.onStart();
        myRobot.getIOIOAndroidApplicationHelper().start();
    }

    /**
     * This method insures the app's lifecycle doesn't get messed up if the app is stopped due to
     * a global event occurring, i.e. the phone's orientation is flipped, an SMS or an email
     * is received, a call is incoming, etc.
     */
    @Override
    public void onStop() {
        super.onStop();
        myRobot.getIOIOAndroidApplicationHelper().stop();
    }

    /**
     * This method insures the app's lifecycle doesn't get messed up if the app is paused due to
     * a global event occurring, i.e. the phone's orientation is flipped, an SMS or an email
     * is received, a call is incoming, etc.
     */
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(navigationServiceBroadcastReceiver);
        stopService(navigationServiceIntent);
    }

    /**
     * This method insures the app's lifecycle doesn't get messed up if the app is returned to after
     * a global event occurs, i.e. the phone's orientation is flipped, an SMS or an email is
     * received, a call is incoming, etc.
     */
    @Override
    public void onResume() {
        super.onResume();
        startService(navigationServiceIntent);
        registerReceiver(navigationServiceBroadcastReceiver, new IntentFilter(NavigationService.BROADCAST_ACTION));
    }

    /**
     * This method is where the commands are issued to move the robot. The intent passed to this
     * method is from NavigationService, which contains two parameters, direction and
     * distance. Direction is used to orient the robot and move it in the correct direction.
     * Distance is the distance between the robot and its final destination, it is used to
     * stop the robot when it is 5m away from the target and take a picture.
     */
    private void updateRobotDriver(Intent intent) {
        direction = intent.getFloatExtra(Commands.NAVIGATION_TO_DRIVER_DIRECTION, 0.0f); // obtains the direction parameter from
        // the intent passed by NavigationService.
        double distance = intent.getDoubleExtra(Commands.NAVIGATION_TO_DRIVER_DISTANCE, 0.0f); // obtains the distance parameter
        // from the intent passed by NavigationService.

        // This IF block sets the robot's motion based on the next location's bearing with respect
        // to the robot. If the robot is within 5m from the destination, the robot will
        // stop and take a picture.
        if (!(distance < 5 && distance > 0)) {
            if (direction > 350 || direction < 10) {
                myRobot.declareMotion(GO_FORWARDS, STOP, 3000); // A setter method that sets the robot's motion.
            } else if (direction < 350 && direction > 180) {
                myRobot.declareMotion(TURN_CLOCKWISE, STOP, 650);
                motionDirection = Commands.TURN_CLOCKWISE;
            } else if (direction < 180 && direction > 10) {
                myRobot.declareMotion(TURN_COUNTERCLOCKWISE, STOP, 650);
                motionDirection = Commands.TURN_COUNTERCLOCKWISE;
            }
        } else {
            myRobot.declareMotion(STOP, STOP, 0);
            motionDirection = "I have arrived to destination.";
            cameraFragment.takePhotoOrCaptureVideo(RobotDriver.this,
                    "/storage/self/primary", "thePicture001");
        }

        Log.d("OUTSIDE", String.valueOf(direction));  // Used for debugging.

        // This block updates the user with the phone's direction to the next point. Used for
        // debugging.
        TextView directionTextView = (TextView) findViewById(R.id.bearingToDis);
        directionTextView.setText("Direction: " + direction);

        // This block updates the user with the phone's motion to the next point. Used for
        // debugging.
        TextView motionTextView = (TextView) findViewById(R.id.motion);
        motionTextView.setText("I am moving: " + motionDirection);

        // This block updates the user with the phone's distance to the final destination. Used for
        // debugging.
        TextView distanceTextView = (TextView) findViewById(R.id.distanceToFinal);
        distanceTextView.setText("I am: " + distance + "m away.");
    }

    /**
     * This method must be included in order for CameraFragmentResultListener to be implemented.
     * This method would be used if we were recording a video.
     */
    @Override
    public void onVideoRecorded(String filePath) {
        Toast.makeText(this, "Video", Toast.LENGTH_SHORT).show();
    }

    /**
     * This method is triggered when a picture is taken. Here the picture is uploaded to Twitter.
     */
    @Override
    public void onPhotoTaken(byte[] bytes, String filePath) {
        Toast.makeText(this, "Photo: " + bytes.length + filePath, Toast.LENGTH_SHORT).show();
        postToMedia.postToSocialMedia(filePath, Commands.TWITTER);
    }

    private void callNavigationService(String message) {
        navigationServiceIntent.putExtra(Commands.DRIVER_TO_NAVIGATION_MESSAGE, message); // The SMS, which is the destination name, is passed
            // to NavigationService, because it needs it to file the API request.
        Log.d(TAG,"callNavigationService");
        this.startService(navigationServiceIntent);  // This line is what actually starts the NavigationService.
    }
}

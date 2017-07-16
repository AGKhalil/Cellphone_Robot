package com.example.khalil.myrobot;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.florent37.camerafragment.CameraFragment;
import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.listeners.CameraFragmentResultListener;

import static com.example.khalil.myrobot.Commands.STOP;
import static com.example.khalil.myrobot.Commands.TURN_CLOCKWISE;
import static com.example.khalil.myrobot.Commands.TURN_COUNTERCLOCKWISE;

/**
 * Created by Khalil on 7/5/17.
 */

public class RobotManipulator extends AppCompatActivity implements CameraFragmentResultListener {
    public String TAG = "RobotManipulator";
    private CommunicationOut postToMedia = new CommunicationOut("wherever I am"); // Social media object that posts to
    public final CameraFragment cameraFragment =
            CameraFragment.newInstance(new Configuration.Builder().build()); // A camera fragment
    public String socialParameter = "";
    IOIOClass myRobot;  // An instance of the robot. A setter method will be used on this instance

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manipulator); // Sets the XML view file that appears to the user.
        // Attaches the camera fragment to the XML file so the user can see it.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.manipulatorCameraFragment, cameraFragment, "TheCameraThing")
                .commit();

        myRobot = new IOIOClass(this);
        myRobot.getIOIOAndroidApplicationHelper().create(); // Retrieves the IOIO helper, which is
        Log.d(TAG, "onCreate: ");
        String action = getIntent().getStringExtra(Commands.HUB_TO_Manipulator_ACTION);
        String actionParameter = getIntent().getStringExtra(Commands.HUB_TO_Manipulator_ACTION_PAREMETER);

        switch (action) {
            case Commands.NLP_ACTION_PICTURE_TAKING:
                socialParameter = actionParameter;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        takeAPicture();
                    }
                }, 3000);
                break;
            case Commands.NLP_ACTION_TURNAROUND:
                turnRobot(actionParameter);
                break;
        }
    }

    public void takeAPicture() {
        Log.d(TAG, "takeAPicture: Picturetaking");
        Log.d(TAG, "takeAPicture: !!!"+socialParameter);
        cameraFragment.takePhotoOrCaptureVideo(RobotManipulator.this,
                "/storage/self/primary", "thePicture001");
    }

    public void turnRobot(String myActionParameter) {
        Log.d(TAG, "turnRobot: Turn"+ myActionParameter);
        // responsible for starting the IOIO loop from another class, and creates it.
        // This allows RobotDriver to access the IOIOClass instance.
        if(myActionParameter.equals(TURN_CLOCKWISE)) {
            Log.d(TAG, "turnRobot: clockwise!!!");
            myRobot.setMotion(TURN_CLOCKWISE);
        } else if (myActionParameter.equals(TURN_COUNTERCLOCKWISE)) {
            myRobot.setMotion(TURN_COUNTERCLOCKWISE);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                myRobot.setMotion(STOP);
            }
        }, 5000);
    }

    @Override
    public void onVideoRecorded(String filePath) {

    }

    @Override
    public void onPhotoTaken(byte[] bytes, String filePath) {
        postToMedia.postToSocialMedia(filePath, socialParameter);
    }

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
}

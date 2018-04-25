package com.example.khalil.myrobot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.florent37.camerafragment.CameraFragment;
import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.listeners.CameraFragmentResultListener;

/**
 * Created by Khalil on 6/24/17.
 * This class takes a picture 3 seconds after being called and posting it to Twitter.
 */

public class CommunicationOut extends AppCompatActivity implements CameraFragmentResultListener {
    private String contact;
    private String target;
    final private String TAG = "CommunicationOut";
    @SuppressLint("MissingPermission")
    public final CameraFragment cameraFragment =
            CameraFragment.newInstance(new Configuration.Builder().build()); // The camera fragment

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        contact = intent.getStringExtra("channel");
        target = intent.getStringExtra("person");


        // Activity layout is inflated.
        setContentView(R.layout.activity_commsout);

        // The camera fragment is initialized.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.manipulatorCameraFragment, cameraFragment, "TheCameraThing")
                .commit();

        // After 3 seconds a picture is captured.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cameraFragment != null) {
                    cameraFragment.switchCameraTypeFrontBack();
                }
            }
        }, 1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                takeAPicture();
            }
        }, 3000);
    }

    /**
     * This method uses the camera fragment to obtain an image. The image is saved to file.
     */
    public void takeAPicture() {
        cameraFragment.takePhotoOrCaptureVideo(CommunicationOut.this,
                "/storage/self/primary", "thePicture001");
    }

    /**
     * A similar function to onPhotoTaken(), although only triggered when a video is recorded. This
     * method is not used in this project scope, but is present due to the
     * CameraFragmentResultListener implementation.
     * @param filePath the video file path.
     */
    @Override
    public void onVideoRecorded(String filePath) {

    }

    /**
     * This is a method implemented by CameraFragmentResultListener, it is called immediately once
     * a picture is captured on a camera fragment. This method calls postToTwitter() and sends to
     * it the image's file path. Furthermore, this method creates an intent for CentralHub and
     * starts it after a delay of 3 seconds. The delay allows the app enough time to upload the
     * image prior to returning to the app's main activity.
     */
    @Override
    public void onPhotoTaken(byte[] bytes, String filePath) {
        postToSlack(filePath, contact);
        final Intent intent = new Intent(this, CentralHub.class);
        Log.d(TAG, "Target is " + target);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
            }
        }, 1500);
    }

    public void postToSlack (String filepath, String channel){
        final Intent intent = new Intent(this, SlackService.class);
        intent.putExtra("msg","");
        intent.putExtra("channelID", channel);
        intent.putExtra("person", target);
        //Log.d(TAG, "postToSlack: "+filepath);
        startService(intent);
    }
}

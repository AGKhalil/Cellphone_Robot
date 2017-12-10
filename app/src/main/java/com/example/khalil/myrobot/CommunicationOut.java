package com.example.khalil.myrobot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.florent37.camerafragment.CameraFragment;
import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.listeners.CameraFragmentResultListener;

import java.io.File;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Khalil on 6/24/17.
 * This class takes a picture 3 seconds after being called and posting it to Twitter.
 */

public class CommunicationOut extends AppCompatActivity implements CameraFragmentResultListener {
    @SuppressLint("MissingPermission")
    public final CameraFragment cameraFragment =
            CameraFragment.newInstance(new Configuration.Builder().build()); // The camera fragment

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                takeAPicture();
            }
        }, 3000);
    }

    /**
     * This method posts a picture and caption to Twitter.
     */
    void postToTwitter(String filePath) {
        new AsyncTask<String, Void, Void>() {

            @Override
            protected Void doInBackground(String... params) {
                ConfigurationBuilder twitterConfigBuilder = new ConfigurationBuilder();
                twitterConfigBuilder.setDebugEnabled(true);
                twitterConfigBuilder.setOAuthConsumerKey(getString(R.string.twitterSetOAuthConsumerKey));
                twitterConfigBuilder.setOAuthConsumerSecret(
                        getString(R.string.twitterSetOAuthConsumerSecret));
                twitterConfigBuilder.setOAuthAccessToken(
                        getString(R.string.twitterSetOAuthAccessToken));
                twitterConfigBuilder.setOAuthAccessTokenSecret(
                        getString(R.string.twitterSetOAuthAccessTokenSecret));

                Twitter twitter = new TwitterFactory(twitterConfigBuilder.build()).getInstance();
                File file = new File(params[0]);

                // Image caption.
                String caption = "I found both balls, I WIN!";

                StatusUpdate status = new StatusUpdate(caption);
                status.setMedia(file); // set the image to be uploaded here.
                try {
                    twitter.updateStatus(status);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(filePath);
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
        postToTwitter(filePath);
        final Intent intent = new Intent(this, CentralHub.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
            }
        }, 3000);
    }
}

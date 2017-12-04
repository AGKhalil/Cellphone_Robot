package com.example.khalil.myrobot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;

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
 * This class was in charge of posting to social media.
 * Modified to serve as communication out class by Michael.
 */

public class CommunicationOut extends AppCompatActivity implements CameraFragmentResultListener {
    private static String caption = "I found both balls, I WIN!";
    @SuppressLint("MissingPermission")
    public final CameraFragment cameraFragment =
            CameraFragment.newInstance(new Configuration.Builder().build()); // A camera fragment
    private static String message;

//    /** Constructor to create an instance of CommunicationOut and pass to it the text message. */
//    CommunicationOut(String message){
//        CommunicationOut.message = message;
//    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commsout);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.manipulatorCameraFragment, cameraFragment, "TheCameraThing")
                .commit();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                takeAPicture();
            }
        }, 5000);
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

    public void takeAPicture() {
        cameraFragment.takePhotoOrCaptureVideo(CommunicationOut.this,
                "/storage/self/primary", "thePicture001");
    }

    void sendSMS(String phoneNumber) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    @Override
    public void onVideoRecorded(String filePath) {

    }

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

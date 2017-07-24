package com.example.khalil.myrobot;

import android.os.AsyncTask;
import android.telephony.SmsManager;

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

class CommunicationOut {
    private static String message;

    /** Constructor to create an instance of CommunicationOut and pass to it the text message. */
    CommunicationOut(String message){
        CommunicationOut.message = message;
    }

    void postToSocialMedia(String filePath, String socialParamter) {
        switch (socialParamter) {
            case Commands.TWITTER:
                postToTwitter(filePath);
                break;
        }
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
                twitterConfigBuilder.setOAuthConsumerKey("lxRCnjL6HaMUg7HjxAJC1k6IH");
                twitterConfigBuilder.setOAuthConsumerSecret(
                        "6E3oLs4kln9p4oMkBRi2LceOkXuDYKASlXIm53UEq1wDNC4FxI");
                twitterConfigBuilder.setOAuthAccessToken(
                        "854512192879820800-zcc88HtCEEcHyXO0JjgZJEFKmLP2HUi");
                twitterConfigBuilder.setOAuthAccessTokenSecret(
                        "bUPpgmB6ipYkVb2kQ0LgAOeUPQtzZ78qBRB2iSrHQdJAe");

                Twitter twitter = new TwitterFactory(twitterConfigBuilder.build()).getInstance();
                File file = new File(params[0]);

                StatusUpdate status = new StatusUpdate("This is my view from " + message + "!");
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

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }
}

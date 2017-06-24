package com.example.khalil.myrobot;

import java.io.File;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Khalil on 6/24/17.
 * This class is in charge of posting to social media.
 */

class SocialPost {
    private static String message;

    SocialPost(String message){
        SocialPost.message = message;
    }

    /**
     * This method posts a picture and caption to Twitter.
     * @param params
     */
    void postToTwitter(String... params) {
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
    }
}

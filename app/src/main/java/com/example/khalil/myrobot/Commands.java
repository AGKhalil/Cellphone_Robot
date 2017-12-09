package com.example.khalil.myrobot;

/**
 * Created by Khalil on 7/13/17.
 * This file stores all the string commands for intents, intent pointers, commands, etc.
 */

// TODO more needs to be deleted after modifications

class Commands {
    static final String MICKEY = "Mickey";
    static final String LILY = "Lily";

    /** NLP JSON parameters **/
    static final String NLP_ACTION = "action";
    static final String NLP_SPEECH = "speech";

    /** NLP action cases **/
    static final String ORIGINAL_MESSAGE = "message";

    static final String NLP_ACTION_NAVIGATION = "navigation";
    // NLP_ACTION_NAVIGATION actionParameters
    static final String NLP_NAVIGATION_DESTINATION = "destination";

    static final String NLP_ACTION_PICTURE_TAKING = "picturetaking";
    // NLP_ACTION_PICTURE_TAKING actionParameters
    static final String NLP_PICTURE_TAKING_SOCIAL_MEDIA = "socialmedia";

    static final String NLP_ACTION_TURNAROUND = "turnaround";
    // NLP_ACTION_TURNAROUND actionParameters
    static final String NLP_TURNAROUND_ROTATION_DIRECTION = "rotation_direction";

    static final String NLP_ACTION_ROBOT = "robot";
    static final String NLP_ACTION_CONTACT = "contact";

    static final String NLP_ACTION_WALK = "walk";
    // NLP_ACTION_WALK actionParameters
    static final String NLP_WALK_SHAPE = "shape";
}

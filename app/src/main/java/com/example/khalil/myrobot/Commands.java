package com.example.khalil.myrobot;

/**
 * Created by Khalil on 7/13/17.
 * This file stores all the string commands for intents, intent pointers, commands, etc.
 */

class Commands {
    /** IOIO Commands **/
    static final String GO_FORWARDS = "forwards";
    static final String TURN_CLOCKWISE = "clockwise";
    static final String TURN_COUNTERCLOCKWISE = "counterclockwise";
    static final String STOP = "stop";

    /** Social Media Parameters **/
    static final String TWITTER = "Twitter";

    /** NLP JSON parameters **/
    static final String NLP_ACTION = "action";
    static final String NLP_SPEECH = "speech";

    /** NLP action cases **/
    static final String NLP_ACTION_NAVIGATION = "navigation";
    // NLP_ACTION_NAVIGATION actionParameters
    static final String NLP_NAVIGATION_DESTINATION = "destination";

    static final String NLP_ACTION_PICTURE_TAKING = "picturetaking";
    // NLP_ACTION_PICTURE_TAKING actionParameters
    static final String NLP_PICTURE_TAKING_SOCIAL_MEDIA = "socialmedia";

    static final String NLP_ACTION_TURNAROUND = "turnaround";
    // NLP_ACTION_TURNAROUND actionParameters
    static final String NLP_TURNAROUND_ROTATION_DIRECTION = "rotation_direction";

    /** CentralHub to RobotManipulator intent pointers **/
    static final String HUB_TO_Manipulator_ACTION = "action";
    static final String HUB_TO_Manipulator_ACTION_PAREMETER = "actionParameter";

    /** CentralHub to RobotDriver intent pointers **/
    static final String HUB_TO_DRIVER_DESTINATION_MESSAGE = "message";

    /** NavigationService to RobotDriver intent pointers **/
    static final String NAVIGATION_TO_DRIVER_DIRECTION = "direction";
    static final String NAVIGATION_TO_DRIVER_DISTANCE = "distance";

    /** RobotDriver to NavigationService intent pointers **/
    static final String DRIVER_TO_NAVIGATION_MESSAGE = "message";
}

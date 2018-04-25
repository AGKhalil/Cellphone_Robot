package com.example.khalil.myrobot;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;
import std_msgs.Int8;

public class LisBot extends AbstractNodeMain {
    private DeliveryBot botcontext; // Used to modify views on activity_robot_controller.xml.
    private static final java.lang.String TAG = "LisBot"; // Log tag name.
    private java.lang.String outputMsg; // Message shown on app view. Robot status
    private Boolean successFlag = true; // A flag used to indicate whether or not a success message
        // has been shown on the app screen

    LisBot(DeliveryBot deliveryBot) {
        this.botcontext = deliveryBot;
    }

    @Override
    public GraphName getDefaultNodeName() {
        // Node name.
        return GraphName.of("android_whisperer/botListener");
    }

    /**
     * This method is called once an instance of Listener is called. Here a subscriber is created
     * to listen to move_base robot status on a topic called move_base_bytestatus on the ROS network.
     * @param connectedNode the current node.
     */
    @Override
    public void onStart(ConnectedNode connectedNode) {
        // Node is connected.
        final org.apache.commons.logging.Log log = connectedNode.getLog();

        android.util.Log.d(TAG, "I am initialized.");

        final Subscriber<std_msgs.Int8> status_byte_subscriber = connectedNode.newSubscriber("move_base_bytestatus", Int8._TYPE);
        status_byte_subscriber.addMessageListener(new MessageListener<Int8>() {
            @Override
            public void onNewMessage(std_msgs.Int8 msg) {
                android.util.Log.d(TAG, "Status byte is: " + msg.getData());
                // Retrives the robot status and updates the app views accordingly.
                updateMoveBase(java.lang.String.valueOf(msg.getData()));
                updateBotState(java.lang.String.valueOf(msg.getData()));
                if (msg.getData() == 1) {
                    successFlag = true;
                }
                if (msg.getData() == 3) {
                    if (successFlag) {
                        android.util.Log.d(TAG, "Status is 3!!!");
                        botcontext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                botcontext.sendToSlack(botcontext.recipient);
                            }
                        });
                        successFlag = false;
                    }
                }
            }
        });
    }

    /**
     * This method updates the botState TextView on DeliveryBot.
     * @param textyText the status to be shown.
     */
    private void updateBotState(final java.lang.String textyText){
        // Must run on main thread.
        botcontext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (textyText) {
                    case "0":
                        outputMsg = botcontext.getString(R.string.status_explanation_0);
                        break;
                    case "1":
                        outputMsg = botcontext.getString(R.string.status_explanation_1);
                        break;
                    case "2":
                        outputMsg = botcontext.getString(R.string.status_explanation_2);
                        break;
                    case "3":
                        outputMsg = botcontext.getString(R.string.status_explanation_3);
                        break;
                    case "4":
                        outputMsg = botcontext.getString(R.string.status_explanation_4);
                        break;
                    case "5":
                        outputMsg = botcontext.getString(R.string.status_explanation_5);
                        break;
                    case "6":
                        outputMsg = botcontext.getString(R.string.status_explanation_6);
                        break;
                    case "7":
                        outputMsg = botcontext.getString(R.string.status_explanation_7);
                        break;
                    case "8":
                        outputMsg = botcontext.getString(R.string.status_explanation_8);
                        break;
                    case "9":
                        outputMsg = botcontext.getString(R.string.status_explanation_9);
                        break;
                }
                botcontext.botState.setText(outputMsg);
            }
        });
    }

    /**
     * This method updates the moveBase TextView on DeliveryBot.
     * @param textytext the status to be sown on screen.
     */
    private void updateMoveBase(final java.lang.String textytext){
        // Must run on main thread.
        botcontext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                botcontext.movebase.setText(textytext);
            }
        });
    }
}

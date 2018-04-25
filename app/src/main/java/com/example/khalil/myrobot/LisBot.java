package com.example.khalil.myrobot;

import android.content.Intent;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;
import std_msgs.Int8;
import std_msgs.String;

public class LisBot extends AbstractNodeMain {
    private DeliveryBot botcontext; // Used to modify views on activity_robot_controller.xml.
    private static final java.lang.String TAG = "LisBot"; // Log tag name.
    java.lang.String outputMsg;
    Boolean successFlag = true;

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
     * to listen to ultrasonic information on the distance topic on the ROS network. Furthermore,
     * a publisher is created to publish on action when deemed necessary to command the robot.
     * There is also a ParameterTree initialized here, which is used to listen to any ROS
     * parameters. This method looks at the ROS parameter "alert_distance" and if a reading on the
     * "distance" ROS topic is below it, a command is published on the "action" ROS topic to change
     * the robot trajectory.
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
                    //status_byte_subscriber.shutdown();
                }
            }
        });
    }

    /**
     * This method updates the ultrasonicReading TextView on RobotController.
     * @param textyText the distance reading to be shown.
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
     * This method updates the botState TextView on RobotController.
     * @param textytext the message to be sown on screen.
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

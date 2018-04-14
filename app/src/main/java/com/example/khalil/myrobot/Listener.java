package com.example.khalil.myrobot;
import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.parameter.ParameterTree;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.util.Collection;

/**
 * Created by Khalil on 10/18/17.
 * This class is the ROS listener node mainly used to listen to information on the ROS network.
 */

class Listener extends AbstractNodeMain {
    private RobotController context; // Used to modify views on activity_robot_controller.xml.
    private Publisher<std_msgs.String> publisher; // Publisher used to publish to the "action" topic.

    Listener(RobotController robotController) {
    }

    Listener(DeliveryBot deliveryBot) {
    }

    @Override
    public GraphName getDefaultNodeName() {
        // Node name.
        return GraphName.of("android_whisperer/listener");
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
        final Log log = connectedNode.getLog();

        // Subscriber created to listen to the "distance" topic.
        final Subscriber<sensor_msgs.Range> subscriber = connectedNode.newSubscriber("distance",
                sensor_msgs.Range._TYPE);

        // Publisher is allocated to the "action" topic.
        publisher = connectedNode.newPublisher("action", std_msgs.String._TYPE);

        double paramDistance; // Saves the ultrasonic alert distance.

        // This code block retrieves the ultrasonic distance parameter.
        ParameterTree params = connectedNode.getParameterTree();
        paramDistance = params.getInteger("alert_distance");

        // Obtains a collection of names of all the parameters.
        Collection paramNames = params.getNames();
        log.info("PARAMETER NAMES: " + paramNames);

        // paramDistance must be declared final to allow modification on RobotController.
        final double finalParamDistance = paramDistance;

        // The subscriber keeps listening for any change in data.
        subscriber.addMessageListener(new MessageListener<sensor_msgs.Range>() {
            @Override
            // On every new message this code performs.
            public void onNewMessage(sensor_msgs.Range range) {
                log.info("I heard: \"" + range.getRange() + "\""); // Output to log.

                // Update the ultrasonic reading value on screen. This is on RobotController.
                updateReading(Float.toString(range.getRange()));

                // If the current value is below the "alert_distance" parameter this code performs.
                if (range.getRange() < finalParamDistance) {

                    // Tell user I am blocked. Used for debugging more than anything.
                    updateState("I'm blocked!");
                    // Publishes "f" again to have the robot change trajectory.
                    publish();
                }else{
                    updateState("No block detected");
                }
            }
        });

        // Shuts down subscriber.
        subscriber.shutdown();
    }

    /**
     * This method updates the ultrasonicReading TextView on RobotController.
     * @param textyText the distance reading to be shown.
     */
    private void updateReading(final String textyText){
        // Must run on main thread.
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                context.ultrasonicReading.setText(textyText);
            }
        });
    }

    /**
     * This method updates the botState TextView on RobotController.
     * @param textytext the message to be sown on screen.
     */
    private void updateState(final String textytext){
        // Must run on main thread.
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                context.robotState.setText(textytext);
            }
        });
    }

    /**
     * This method publishes the message "f" for "freestyle movement on the "action" ROS topic.
     */
    public void publish() {
        if (publisher != null) {
            std_msgs.String toPublish = publisher.newMessage();
            toPublish.setData("f");
            publisher.publish(toPublish);
        }
    }
}
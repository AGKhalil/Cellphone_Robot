package com.example.khalil.myrobot;

import org.apache.commons.logging.Log;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

/**
 * Created by Khalil on 10/18/17.
 * This class is the ROS talker node mainly used to publish commands to the ROS network.
 */

class TalkBot extends AbstractNodeMain {
    private static final String TAG = "Talker";
    private Publisher<std_msgs.String> publisher; // Publisher used to publish to the "location_goal" topic.

    TalkBot(DeliveryBot deliveryBot) {
    }

    @Override
    public GraphName getDefaultNodeName() {
        // Node name.
        return GraphName.of("android_whisperer/talker");
    }

    /**
     * This method is called once an instance of Talker is called. Here the publisher
     * is initialized. The initialization includes specifying the topic that
     * will be published on as well as its data type. publish is used to issue the location goals to
     * the robot.
     * @param connectedNode current node.
     */
    @Override
    public void onStart(final ConnectedNode connectedNode) {
        final Log log = connectedNode.getLog();

        // Publisher is allocated to the "location_goal" topic.
        publisher = connectedNode.newPublisher("location_goal", std_msgs.String._TYPE);
    }

    /**
     * This method is public and can be accessed externally to publish a message on the publisher
     * in a Talker instance.
     * @param message the message to be published.
     */
    void publish(String message) {
        if (publisher != null) {
            std_msgs.String toPublish = publisher.newMessage();
            toPublish.setData(message);
            publisher.publish(toPublish);
        }
    }
}


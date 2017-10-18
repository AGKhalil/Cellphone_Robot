package com.example.khalil.myrobot;

import org.apache.commons.logging.Log;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

/**
 * Created by Khalil on 10/18/17.
 */

class Talker extends AbstractNodeMain {
    private String randomCommand = "Hello";
    private Publisher<std_msgs.String> publisher;
    Talker(CentralHub centralHub) {

    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android_whisperer");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        final Log log = connectedNode.getLog();
        publisher = connectedNode.newPublisher("action", std_msgs.String._TYPE);

    }

    public void publish(String message) {
        if (publisher != null) {
            std_msgs.String toPublish = publisher.newMessage();
            toPublish.setData(message);
            publisher.publish(toPublish);
        }
    }
}

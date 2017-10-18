package com.example.khalil.myrobot;

import android.content.Context;

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import sensor_msgs.Range;

/**
 * Created by Khalil on 10/18/17.
 */

public class Listener extends AbstractNodeMain {
    protected CentralHub context;

    public Listener(CentralHub mainActivity, Context context) {
        this.context = (CentralHub) context;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava_tutorial_pubsub/listener");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        final Log log = connectedNode.getLog();
        final Subscriber<Range> subscriber = connectedNode.newSubscriber("distance", sensor_msgs.Range._TYPE);
        subscriber.addMessageListener(new MessageListener<Range>() {
            @Override
            public void onNewMessage(sensor_msgs.Range range) {
                log.info("I heard: \"" + range.getRange() + "\"");
//                UpdateReading(Float.toString(range.getRange()));

//                if (range.getRange() < (float) 20) {
//                    Intent intent = new Intent(context, CommsOut.class);
//                    context.startActivity(intent);
//                    UpdateState("I'm blocked!");
//                    subscriber.shutdown();
//                }
            }
        });
    }

//    public void UpdateReading(final String textytext){
//        context.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                context.ultrasonicReading.setText(textytext);
//            }
//        });
//    }

//    public void UpdateState(final String textytext){
//        context.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                context.robotState.setText(textytext);
//            }
//        });
//    }
}
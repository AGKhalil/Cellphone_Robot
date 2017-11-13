package com.example.khalil.myrobot;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.parameter.ParameterTree;
import org.ros.node.topic.Subscriber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Khalil on 10/18/17.
 */

class Listener extends AbstractNodeMain {
    private RobotController context;
    Talker talkerNode;

    Listener(RobotController centralHub, Context context) {
        this.context = (RobotController) context;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava_tutorial_pubsub/listener");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        final Log log = connectedNode.getLog();
        final Subscriber<sensor_msgs.Range> subscriber = connectedNode.newSubscriber("distance",
                sensor_msgs.Range._TYPE);

        Map<?, ?> paramsActions;
        Map<?, ?> paramMotors;
        double paramDistance;
        Collection<?> motorData;

        ParameterTree params = connectedNode.getParameterTree();
        paramsActions = params.getMap("actiondic");
        paramMotors = params.getMap("motor");
        paramDistance = params.getInteger("alert_distance");
        Collection paramNames = params.getNames();

        Object motor_l = paramMotors.get("motor_l");

        log.info("PARAMETER NAMES: " + paramNames);
        log.info("PARAMETER MOTOR: " + Arrays.toString((Object[]) motor_l));
        ArrayList newStringList = new ArrayList<>(paramsActions.values());

        ArrayAdapter<String> actionList = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, newStringList);
        updateActionSpinner(actionList, paramsActions);

        log.info("PARAMETER READING: " + paramsActions.keySet());
        final double finalParamDistance = paramDistance;
        subscriber.addMessageListener(new MessageListener<sensor_msgs.Range>() {
            @Override
            public void onNewMessage(sensor_msgs.Range range) {
//                log.info("I heard: \"" + range.getRange() + "\"");
                updateReading(Float.toString(range.getRange()));

                if (range.getRange() < finalParamDistance) {
                    Intent intent = new Intent(context, CommunicationOut.class);
                    context.startActivity(intent);
//                    updateState("I'm blocked!");
                    subscriber.shutdown();
                }
            }
        });
    }

    private void updateReading(final String textytext){
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                context.ultrasonicReading.setText(textytext);
            }
        });
    }

    private void updateActionSpinner(final ArrayAdapter<String> newAdapter, final Map<?,
                ?> paramsInt){
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                context.spinner.setAdapter(newAdapter);
                context.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                               int position, long id) {
                        if (paramsInt != null) {
                            String actionCommand = context.spinner.getSelectedItem().toString();
                            for(Map.Entry entry: paramsInt.entrySet()){
                                if(actionCommand.equals(entry.getValue())){
                                    context.command = entry.getKey().toString();
                                    android.util.Log.d("SPINNER VALUE IS: ", context.command);
                                    break; //breaking because its one to one map
                                }
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });
            }
        });
    }

    private void updateState(final String textytext){
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                context.robotState.setText(textytext);
            }
        });
    }
}
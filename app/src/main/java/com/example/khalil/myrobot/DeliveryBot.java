package com.example.khalil.myrobot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.exception.RosRuntimeException;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.net.URISyntaxException;

public class DeliveryBot extends RosActivity {
    TextView botState; // Robot status TextView
    TextView movebase; // Robot status TextView
    Button compButton; // Job completion button
    Button cancelButton; // Job cancellation button

    String IP; // ROS_MASTER_URI
    private Context context; // Activity context
    TalkBot talkerNode = new TalkBot(this); // Talker instance used to publish commands.
    private static final String TAG = "DeliveryBot"; // Log tag name.
    String contact = ""; // Contact ID
    String recipient = ""; // Recipient ID
    private String room = ""; // Location
    private String target = ""; // Contact name

    protected DeliveryBot() {
        super("MainNode", "MainNode");
    }

    /**
     * This method starts up the ros nodes.
     * @param nodeMainExecutor used to create and sustain the nodes.
     */
    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        Log.d(TAG, "init is loaded");
        LisBot lisNode = new LisBot(this);

        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(
                InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration.setMasterUri(getMasterUri());

        nodeMainExecutor.execute(talkerNode, nodeConfiguration);
        nodeMainExecutor.execute(lisNode, nodeConfiguration);
    }

    /**
     * This method establishes the connection with the ROS network
     */
    @SuppressLint("StaticFieldLeak")
    @Override
    public void startMasterChooser() {
        URI uri;
        try {
            uri = new URI(IP);
        } catch (URISyntaxException e) {
            throw new RosRuntimeException(e);
        }

        // Connection is asynchronous.
        nodeMainExecutorService.setMasterUri(uri);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DeliveryBot.this.init(nodeMainExecutorService);
                return null;
            }
        }.execute();
    }

    /**
     * This function sets up the activity's main layout as well as associating the appropriate
     * variables with their views.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate is Loaded");
        Intent intent = getIntent();
        contact  = intent.getStringExtra("contact");
        target  = intent.getStringExtra("target");
        target = target.substring(1, target.length() - 1);
        Log.d(TAG, "Target is: " + target);
        room = "me" + intent.getStringExtra("room");
        room = room.replace("\"", "");

        switch (target){
            case "Ahmed":
                recipient = "D931MAEB1";
                Log.d(TAG, "Channel is: " + contact);
                break;
            case "Michael":
                recipient = "D92R2RHAA";
                break;
        }

        Log.d(TAG, "Room sent to ROS is: " + room);

        // Activity layout is inflated.
        setContentView(R.layout.activity_deliverybot);

        // View are matched up with the corresponding variables.
        botState = findViewById(R.id.status_byte);
        movebase = findViewById(R.id.status_text);

        compButton = findViewById(R.id.complete_button);
        compButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                botState.setText(R.string.status_text);
                movebase.setText(R.string.status_number);
                // Perform action on click
                Intent activityChangeIntent = new Intent(DeliveryBot.this, CommunicationOut.class);
                activityChangeIntent.putExtra("channel", contact);
                activityChangeIntent.putExtra("person", target);
                DeliveryBot.this.startActivity(activityChangeIntent);
                Intent intent = new Intent(DeliveryBot.this, SlackService.class);
                intent.putExtra("msg", "Delivery successful! Nice doing business with you.");
                intent.putExtra("channelID", contact);
                startService(intent);
            }
        });

        cancelButton = findViewById(R.id.cancel_job);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent intent = new Intent(DeliveryBot.this, CentralHub.class);
                startActivity(intent);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                talkerNode.publish(room);
            }
        }, 3000);

        IP = intent.getStringExtra("uri");
        Log.d(TAG, "onCreate: "+ IP);
    }

    /**
     * This method enables DeliveryBot to send messages to slack.
     * @param text The text to be sent on Slack.
     */
    void sendToSlack(String text) {
        final Intent intent = new Intent(DeliveryBot.this, SlackService.class);
        intent.putExtra("msg", "I'm here!");
        intent.putExtra("channelID", text);
        startService(intent);
    }
}

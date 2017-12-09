package com.example.khalil.myrobot;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.exception.RosRuntimeException;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Khalil on 11/8/17.
 */

// TODO remove the button and spinner and all associated functions m8.

public class RobotController extends RosActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    TextView ultrasonicReading; // Ultrasonic sensor reading TextView.
    TextView robotState; // Robot status TextView.

    //Needs to be deleted.
    Button btn;
    Spinner spinner;


    Talker talkerNode = new Talker(this); // Talker instance used to publish commands.
    boolean sawRedBall = false; // Flag to signify if robot saw the red ball.
    boolean sawYellowBall = false; // Flag to signify if robot saw the yellow ball.
    boolean robotWon = false; // Flag to signify if robot saw both balls.
    private static final String TAG = "RobotController"; // Log tag name.
    private JavaCameraView javaCameraView; // Camera fragment that inputs frame stream.
    Mat imgHSV, mYellowThresh, mRgba, mRedThresh; // Mats used for image manipulation.

    // This BaseLoaderCallback is used to instantiate javaCameraView.
    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status) {
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
            super.onManagerConnected(status);
        }
    };

    // Checks if the OpenCV Library is loaded or not.
    static {
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    protected RobotController() {
        super("MainNode", "MainNode");
    }

    /**
     * This method establishes the connection with the ROS network
     */
    @Override
    public void startMasterChooser() {
        URI uri;
        try {
            // The uri address.
            uri = new URI(getString(R.string.rosIP));
        } catch (URISyntaxException e) {
            throw new RosRuntimeException(e);
        }

        // Connection is asynchronous.
        nodeMainExecutorService.setMasterUri(uri);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                RobotController.this.init(nodeMainExecutorService);
                return null;
            }
        }.execute();
    }

    /**
     * This function sets up the activity's main layout as well as associating the appropriate
     * variables with their views. Afterwards, javaCameraView is configured and a listener to its
     * frames is established. onCreate then publishes "f" to the ROS network after a two-second
     * delay by calling publishOnStart(). This is done so the robot always starts by going
     * "freestyle".
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate is Loaded");

        // Activity layout is inflated.
        setContentView(R.layout.activity_robot_controller);

        // View are matched up with the corresponding variables.
        ultrasonicReading = (TextView) findViewById(R.id.ultrasonic_reading);
        robotState = (TextView) findViewById(R.id.robot_state);

        // Needs to be deleted.
        btn = (Button) findViewById(R.id.robot_button);
        spinner = (Spinner) findViewById(R.id.action_spinner);
        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishGo(v);
            }
        });

        javaCameraView = (JavaCameraView) findViewById(R.id.HelloOpenCvView);

        // Checking permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 1);
            }
        }

        // Setting up javaCameraView to fit on screen and start listening to stream.
        javaCameraView.setMaxFrameSize(640, 480);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);

        // Publishes "f" to robot to start moving 2 seconds after the activity is set up.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                publishOnStart();
            }
        }, 2000);

    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        Log.d(TAG, "init is loaded");
        Listener lisNode = new Listener(this, this);

        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(
                InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration.setMasterUri(getMasterUri());

        nodeMainExecutor.execute(talkerNode, nodeConfiguration);
        nodeMainExecutor.execute(lisNode, nodeConfiguration);


    }

    protected void publishGo(View view) {
        talkerNode.publish("f");
        Log.d("ALAASASAK", "I WOOORKKKKKKK");
    }

    protected void publishOnStart() {
        talkerNode.publish("f");
    }

    protected void publishToAbort() {
        talkerNode.setPublisherAbort("abort");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallBack);
        } else {
            Log.d(TAG, "OpenCV loaded");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        imgHSV = new Mat(height, width, CvType.CV_8UC4);
        mYellowThresh = new Mat(height, width, CvType.CV_8UC1);
        mRedThresh = new Mat(height, width, CvType.CV_8UC1);
        Log.d(TAG, "SIZE: " + height);
        Log.d(TAG, "SIZE: " + width);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        Imgproc.cvtColor(mRgba, imgHSV, Imgproc.COLOR_BGR2HSV);
        Core.inRange(imgHSV, new Scalar(50, 100, 50), new Scalar(95, 255, 255), mYellowThresh);
        Core.inRange(imgHSV, new Scalar(120, 100, 60), new Scalar(179, 255, 255), mRedThresh);
        Imgproc.blur(mYellowThresh, mYellowThresh, new Size(10,10));
        Imgproc.threshold(mYellowThresh, mYellowThresh, 150, 255, Imgproc.THRESH_BINARY);
        Imgproc.blur(mRedThresh, mRedThresh, new Size(10,10));
        Imgproc.threshold(mRedThresh, mRedThresh, 150, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> yellowContours = new ArrayList<MatOfPoint>();
        List<MatOfPoint> redContours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mYellowThresh, yellowContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.findContours(mRedThresh, redContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        double maxYellowArea = 5000;
        double maxRedArea = 5000;
        float[] yellowRadius = new float[1];
        float[] redRadius = new float[1];
        Point yellowCenter = new Point();
        Point redCenter = new Point();
        for (int i = 0; i < yellowContours.size(); i++) {
            MatOfPoint c = yellowContours.get(i);
            if (Imgproc.contourArea(c) > maxYellowArea) {
                MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
                Imgproc.minEnclosingCircle(c2f, yellowCenter, yellowRadius);

                if (!sawYellowBall) {
                    Intent intent = new Intent(this, SlackService.class);
                    intent.putExtra("msg", "I found the yellow ball!");
                    intent.putExtra("channelID", "G7Q5G4XS8");
                    startService(intent);
                    sawYellowBall = true;
                }
            }
        }
        for (int i = 0; i < redContours.size(); i++) {
            MatOfPoint d = redContours.get(i);
            if (Imgproc.contourArea(d) > maxRedArea) {
                MatOfPoint2f c2f = new MatOfPoint2f(d.toArray());
                Imgproc.minEnclosingCircle(c2f, redCenter, redRadius);

                if (!sawRedBall) {
                    Intent intent = new Intent(this, SlackService.class);
                    intent.putExtra("msg", "I found the red ball!");
                    intent.putExtra("channelID", "G7Q5G4XS8");
                    startService(intent);
                    sawRedBall = true;
                }
            }
        }

        if (!robotWon) {
            if (sawRedBall & sawYellowBall) {
                publishToAbort();
                Intent intent = new Intent(this, SlackService.class);
                intent.putExtra("msg", "I won!");
                intent.putExtra("channelID", "G7Q5G4XS8");
                startService(intent);
                robotWon = true;
                Intent otherIntent = new Intent(this, CommunicationOut.class);
                startActivity(otherIntent);
            }
        }

        Imgproc.circle(mRgba, yellowCenter, (int)yellowRadius[0], new Scalar(0, 255, 0), 2);
        Imgproc.circle(mRgba, redCenter, (int)redRadius[0], new Scalar(255, 0, 0), 2);
        return mRgba;
    }
}

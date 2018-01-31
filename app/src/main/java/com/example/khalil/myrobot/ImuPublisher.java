package com.example.khalil.myrobot;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import java.util.List;

import sensor_msgs.Imu;


/**
 * @author chadrockey@gmail.com (Chad Rockey)
 * @author axelfurlan@gmail.com (Axel Furlan)
 */
public class ImuPublisher implements NodeMain
{
    private String TAG= "ImuPublisher";
    private ImuThread imuThread;
    private SensorListener sensorListener;
    private SensorManager sensorManager;
    private Publisher<Imu> publisher;
    private int sensorDelay;

    private class ImuThread extends Thread
    {
        private final SensorManager sensorManager;
        private SensorListener sensorListener;
        private Looper threadLooper;

        private final Sensor accelSensor;
        private final Sensor gyroSensor;
        private final Sensor OrientationSensor;
        private final Sensor quatSensor;

        private ImuThread(SensorManager sensorManager, SensorListener sensorListener)
        {
            this.sensorManager = sensorManager;
            this.sensorListener = sensorListener;
            this.accelSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            this.gyroSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            this.OrientationSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            this.quatSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }


        public void run()
        {
            Looper.prepare();
            this.threadLooper = Looper.myLooper();
            this.sensorManager.registerListener(this.sensorListener, this.accelSensor, sensorDelay);
            this.sensorManager.registerListener(this.sensorListener, this.gyroSensor, sensorDelay);
            this.sensorManager.registerListener(this.sensorListener, this.OrientationSensor, sensorDelay);
            this.sensorManager.registerListener(this.sensorListener, this.quatSensor, sensorDelay);
            Looper.loop();
        }


        public void shutdown()
        {
            this.sensorManager.unregisterListener(this.sensorListener);
            if(this.threadLooper != null)
            {
                this.threadLooper.quit();
            }
        }
    }

    private class SensorListener implements SensorEventListener
    {

        private Publisher<Imu> publisher;

        private boolean hasAccel;
        private boolean hasGyro;
        private boolean hasQuat;
        private boolean hasOrient;

        private long accelTime;
        private long gyroTime;
        private long quatTime;
        private long OrientTime;
        private Imu imu;

        private SensorListener(Publisher<Imu> publisher, boolean hasAccel, boolean hasGyro, boolean hasQuat,boolean hasOrient)
        {
            Log.e(TAG, "SensorListener: init");
            this.publisher = publisher;
            this.hasAccel = hasAccel;
            this.hasGyro = hasGyro;
            this.hasQuat = hasQuat;
            this.hasOrient = hasOrient;
            if(hasAccel)
                Log.e("Accelerometer","Yes");
            else{
                Log.e("Accelerometer","No");
            }
            if(hasGyro)
                Log.e("Gyroscope","Yes");
            else{
                Log.e("Gyroscope","No");
            }
            if(hasQuat)
                Log.e("Quaternion","Yes");
            else{
                Log.e("Quaternion","No");
            }
            this.accelTime = 0;
            this.gyroTime = 0;
            this.quatTime = 0;
            this.OrientTime = 0;
            this.imu = this.publisher.newMessage();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {
        }

        @Override
        public void onSensorChanged(SensorEvent event)
        {
            Log.e(TAG, "onSensorChanged: ");
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                this.imu.getLinearAcceleration().setX(event.values[0]);
                this.imu.getLinearAcceleration().setY(event.values[1]);
                this.imu.getLinearAcceleration().setZ(event.values[2]);
                double[] tmpCov = {0,0,0, 0,0,0, 0,0,0};// TODO Make Parameter
                this.imu.setLinearAccelerationCovariance(tmpCov);
                this.accelTime = event.timestamp;
            }
            else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
            {
                this.imu.getAngularVelocity().setX(event.values[0]);
                this.imu.getAngularVelocity().setY(event.values[1]);
                this.imu.getAngularVelocity().setZ(event.values[2]);
                double[] tmpCov = {0,0,0, 0,0,0, 0,0,0};// TODO Make Parameter
                this.imu.setAngularVelocityCovariance(tmpCov);
                this.gyroTime = event.timestamp;
            }
            else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION)
            {
                Log.e("Size", String.valueOf(Math.cos(event.values[0] / 2))+String.valueOf(Math.sin(event.values[0] / 2)));
                // float[] quaternion = new float[4];
                this.imu.getOrientation().setW(Math.cos(event.values[0] / 2));
                this.imu.getOrientation().setX(0);
                this.imu.getOrientation().setY(0);
                this.imu.getOrientation().setZ(Math.sin(event.values[0] / 2.0));
                double[] tmpCov = {0,0,0, 0,0,0, 0,0,0};// TODO Make Parameter
                this.imu.setOrientationCovariance(tmpCov);
                this.OrientTime = event.timestamp;
            }
            else if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
            {
                float[] quaternion = new float[4];
                SensorManager.getQuaternionFromVector(quaternion, event.values);
                this.imu.getOrientation().setW(quaternion[0]);
                this.imu.getOrientation().setX(quaternion[1]);
                this.imu.getOrientation().setY(quaternion[2]);
                this.imu.getOrientation().setZ(quaternion[3]);
                double[] tmpCov = {0,0,0, 0,0,0, 0,0,0};// TODO Make Parameter
                this.imu.setOrientationCovariance(tmpCov);
                this.quatTime = event.timestamp;
            }

            publisher.publish(this.imu);
            // Currently storing event times in case I filter them in the future.  Otherwise they are used to determine if all sensors have reported.
            if((this.accelTime != 0 || !this.hasAccel) &&
                    (this.gyroTime != 0 || !this.hasGyro) &&
                    (this.quatTime != 0 || !this.hasQuat) &&
                    (this.OrientTime != 0 || !this.hasOrient))
            {
                // Convert event.timestamp (nanoseconds uptime) into system time, use that as the header stamp
                long time_delta_millis = System.currentTimeMillis() - SystemClock.uptimeMillis();
                this.imu.getHeader().setStamp(Time.fromMillis(time_delta_millis + event.timestamp/1000000));
                this.imu.getHeader().setFrameId("/imu");// TODO Make parameter

                publisher.publish(this.imu);

                // Create a new message
                this.imu = this.publisher.newMessage();

                // Reset times
                this.accelTime = 0;
                this.gyroTime = 0;
                this.quatTime = 0;
                this.OrientTime = 0;
            }
        }
    }

    public ImuPublisher(SensorManager manager, int sensorDelay)
    {
        this.sensorManager = manager;
        this.sensorDelay = sensorDelay;
    }

    public GraphName getDefaultNodeName()
    {
        return GraphName.of("android_sensors_driver/imuPublisher");
    }

    public void onError(Node node, Throwable throwable)
    {
    }

    public void onStart(ConnectedNode node)
    {
        Log.e(TAG, "onStart: ");
        try
        {
            this.publisher = node.newPublisher("android/imu", "sensor_msgs/Imu");
            // 	Determine if we have the various needed sensors
            boolean hasAccel = false;
            boolean hasGyro = false;
            boolean hasQuat = false;
            boolean hasOrient = false;
            Log.d(TAG, "onStart: 1");
            List<Sensor> accelList = this.sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
            Log.d(TAG, "onStart: 2");
            if(!accelList.isEmpty())
            {
                Log.d(TAG, "onStart: hasAccel");
                hasAccel = true;
            }
            Log.d(TAG, "onStart: 3");
            List<Sensor> gyroList = this.sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
            if(gyroList.size() > 0)
            {
                Log.d(TAG, "onStart: hasGyro");
                hasGyro = true;
            }

            List<Sensor> quatList = this.sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
            if(quatList.size() > 0)
            {
                Log.d(TAG, "onStart: hasQuat");
                hasQuat = true;
            }
            List<Sensor> OrientList = this.sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
            if(OrientList.size() > 0)
            {
                Log.d(TAG, "onStart: hasOrient");
                hasOrient = true;
            }
            this.sensorListener = new SensorListener(publisher, hasAccel, hasGyro, hasQuat,hasOrient);
            Log.e(TAG, "onStart: Register");
            this.imuThread = new ImuThread(this.sensorManager, sensorListener);
            this.imuThread.start();
        }
        catch (Exception e)
        {
            Log.e(TAG, "onStart: Error");
            if (node != null)
            {
                node.getLog().fatal(e);
            }
            else
            {
                e.printStackTrace();
            }
        }
    }

    //@Override
    public void onShutdown(Node arg0)
    {
        Log.e(TAG, "onShutdown: ");
        if(this.imuThread == null){
            return;
        }
        this.imuThread.shutdown();

        try
        {
            this.imuThread.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    //@Override
    public void onShutdownComplete(Node arg0)
    {
    }

}


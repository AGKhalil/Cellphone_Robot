package com.example.khalil.myrobot;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import sensor_msgs.NavSatFix;
/**
 * Created by Michael on 2018/1/30.
 */

public class NewNavSatFixPublisher implements NodeMain{

        private NavSatThread navSatThread;
        private NavSatFixPublisher navSatListner;
        private Context con;
        private Publisher<NavSatFix> publisher;
        public NewNavSatFixPublisher(Context context){
            this.con=context;
        }
        private class NavSatThread extends Thread
        {
            private Looper threadLooper;
            private NavSatThread()
            {
                navSatListner.startGettingLocation(new NavSatFixPublisher.getLocation() {
                    @Override
                    public void onLocationChanged(Location location) {
                        // Here is my working with location object
                        Log.e("Latitude", String.valueOf(location.getLatitude()));
                        NavSatFix navSatFix = publisher.newMessage();
                        navSatFix.getHeader().setStamp(Time.fromMillis(System.currentTimeMillis()));
                        navSatFix.getHeader().setFrameId("/gps");
                        navSatFix.setLatitude(location.getLatitude());
                        navSatFix.setLongitude(location.getLongitude());
                        navSatFix.setAltitude(location.getAltitude());
                        navSatFix.setPositionCovarianceType(NavSatFix.COVARIANCE_TYPE_APPROXIMATED);
                        double deviation = location.getAccuracy();
                        double covariance = deviation * deviation;
                        double[] tmpCov = {covariance, 0, 0, 0, covariance, 0, 0, 0, covariance};
                        navSatFix.setPositionCovariance(tmpCov);
                        publisher.publish(navSatFix);
                    }
                });
            }
            public void run()
            {
                Looper.prepare();
                this.threadLooper = Looper.myLooper();
                Looper.loop();
            }


            public void shutdown()
            {
                navSatListner.stopGettingLocation();
                if(this.threadLooper != null)
                {
                    this.threadLooper.quit();
                }
            }
        }

        public GraphName getDefaultNodeName()
        {
            return GraphName.of("android_sensors_driver/NewNavSatFixPublisher");
        }

        public void onError(Node node, Throwable throwable)
        {
        }

        public void onStart(ConnectedNode node)
        {
            try
            {
                this.publisher = node.newPublisher("android/fix", "sensor_msgs/NavSatFix");
                this.navSatListner = new NavSatFixPublisher(con);
                this.navSatThread = new NavSatThread();
                this.navSatThread.start();
            }
            catch (Exception e)
            {
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
            if(this.navSatThread == null){
                return;
            }
            this.navSatThread.shutdown();

            try
            {
                this.navSatThread.join();
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


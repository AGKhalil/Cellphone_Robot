package com.example.khalil.myrobot;

/**
 * Created by Khalil on 4/8/17.
 * Here the request to Google Directions API is filed, the result is obtained and filed to
 * DataParser for parsing, the routes location array is built, the bearing to next point is found,
 * and the distance to the final destination is found. Both, the bearing to next point and the
 * destination to the final location, are broadcast back to TaskActivity as an intent.
 *
 * Once this service class starts, through a request from TaskActivity, it creates its own thread
 * and keeps restarting every four seconds. Once the distance to the final destination is less than
 * 5m, the service stops itself completely.
 *
 * Although many changes have been done to this class, the main bulk of it is due to courtesy of:
 * https://www.androidtutorialpoint.com/intermediate/google-maps-draw-path-two-points-using-google-directions-google-map-android-api-v2/
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class NavigationService extends Service implements
        SensorEventListener{

    public Location finalDestination;     // This is the final destination location.
    public double nextDestinationLat;  // This is the next destination latitude.
    public double nextDestinationLng;  // This is the next destination longitude.
    private static final String TAG = "NavigationService";  // The TAG used for the logcat for
            // debugging.
    // This is the string used for broadcasting the results to TaskActivity.
    public static final String BROADCAST_ACTION = "com.websmithing.broadcasttest.displayevent";
    Intent intent; // The intent filed back to TaskActivity.
    public String mMessage;  // This stores the SMS message retrieved from TaskActivity.
    public Location nextDestinationObj;  // This stores the next destination point in the location
            // array.
    public LatLng destination;  // This stores the next destination point in the location
            // array as a LatLng.
    private SensorManager sensorManager;  // This manager allows access to the phone's sensors.
    private Sensor gSensor;  // This is the phone's gravity sensor.
    private Sensor mSensor;  // This is the phone's magnetic sensor.

    // These variables are necessary for finding the phone's current bearing due compass north and
            // the phone's bearing to a custom location point, which, in this case, is the next
            // location point in the routes array.
    private float[] mGravity = new float[3];  //
    private float[] mGeomagnetic = new float[3];
    private static final int earthRadius = 6371;

    public double distance;  // This stores the distance between the robot and the final
            // destination.

    private LocationHelper locationHelper = new LocationHelper(this); // LocationHelper object
    private DataParser dataParser = new DataParser(mMessage); // DataParser object

    /**
     * This method sets up locationHelper, the sensors, and the intent that gets sent back
     * to TaskActivity.
    */
    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate");
        locationHelper.startLocationServices();
        intent = new Intent(BROADCAST_ACTION);  // This creates the broadcast intent.

        // This block initiates the sensors and start them.
        sensorManager = (SensorManager) getBaseContext()
                .getSystemService(Context.SENSOR_SERVICE);
        gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        startCompass();

        super.onCreate();
    }

    /**
     * This method is called when the NavigationService is called from TaskActivity. It finds the
     * intent TaskActivity has filed, obtains the SMS message, and calls the startNavigation method.
    */
     public int onStartCommand(Intent intent, int flags, int startId) {
        mMessage = intent.getStringExtra("message");  // Obtains the SMS.
         Log.d(TAG, "onStartCommand: "+mMessage);
        // Creates a new thread upon which NavigationService runs.
        Thread t = new Thread(new Runnable() {
            @Override

            // Checks if the message is null, if not the service starts.
            public void run() {
                if (mMessage != null) {
                    startNavigation(mMessage);
                }
            }
        });

        t.start();  // The thread starts.
        return START_STICKY;
    }

    /**
     * This method initiates LocationHelper, which keeps updating NavigationService with the robot's
     * current location. This method then initiates DataParser, which returns the navigation route
     * from Google Directions API. Afterwards, this method uses the compass methods and
     * calculateDistance to obtain the robot's bearing to the next point in its path as well
     * as the robot's distance to its final destination.
     */
    public void startNavigation(String destinationSms) {
        if (locationHelper.currentLocationLatLng != null) {

            Log.d(TAG, "startNavigation");
            LatLng origin = locationHelper.currentLocationLatLng;  // Retrieves current location.
            ArrayList<Location> pointsArray = dataParser.retrieveData(origin, destinationSms);

            // This IF block retrieves the second point in the array unless the array's size is 1.
            if (pointsArray.size() == 1) {
                nextDestinationObj = pointsArray.get(0);
            } else {
                nextDestinationObj = pointsArray.get(1);
            }

            finalDestination = pointsArray.get(pointsArray.size() - 1);

//            nextDestinationLat = nextDestinationObj.getLatitude();
//            nextDestinationLng = nextDestinationObj.getLongitude();
            destination = new LatLng(nextDestinationObj.getLatitude(),
                    nextDestinationObj.getLongitude());
            double finalDestinationLat = finalDestination.getLatitude();
            double finalDestinationLng = finalDestination.getLongitude();
            distance = calculateDistance(locationHelper.currentLocationLat,
                    locationHelper.currentLocationLng,
                    finalDestinationLat, finalDestinationLng);

            intent.putExtra("distance", distance);
            sendBroadcast(intent);  // Intent is broadcast.
        }
        // This IF block puts the thread to sleep for four seconds when it is done. However, if
                // the distance is below 5m, it will stop the service.
        if (distance > 5 && distance > 0) {
            //job completed. Rest for 4 second before doing another one
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //do job again
            startNavigation(destinationSms);
        } else {
            stopSelf();
        }
    }

    /**
     * This method calculates the distance between two arbitrary locations.
    */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a =
                (Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2));
        double c = 2 * Math.asin(Math.sqrt(a));
        return earthRadius * c * 1000;
    }
  
 /**
     * This method is necessary for any service class. It is not being used however.
    */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This method is called when the service stops. It disconnects locationHelper.
     */
    @Override
    public void onDestroy() {
        locationHelper.mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    /**
     *************************** Compass Methods ***************************************************
     */

    /**
     * This method starts the compass sensors.
     */
    public void startCompass() {
        sensorManager.registerListener(this, gSensor,
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * This method is triggered whenever the compass readings are changed. This method returns the
     * phone's bearing to the next location point in the routes array.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.97f;

        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                mGravity[0] = alpha * mGravity[0] + (1 - alpha)
                        * event.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha)
                        * event.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha)
                        * event.values[2];
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha)
                        * event.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha)
                        * event.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha)
                        * event.values[2];
            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                    mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;
                if (destination != null) {
                    Toast.makeText(this, "I've been called", Toast.LENGTH_SHORT).show();
                    azimuth -= bearing(locationHelper.currentLocationLat,
                            locationHelper.currentLocationLng, nextDestinationLat,
                            nextDestinationLng);
                    if (azimuth < 0) {
                        azimuth = azimuth + 360;
                    }
                    intent.putExtra("direction", azimuth);
                    Log.d(TAG, "azimuth (deg): " + azimuth);
                }
            }
        }
    }

    /**
     * This method is what calculates the phone's bearing to the next location point. This method
     * is called inside onSensorChanged().
     */
    protected double bearing(double startLat, double startLng, double endLat, double endLng) {
        double latitude1 = Math.toRadians(startLat);
        double latitude2 = Math.toRadians(endLat);
        double longDiff = Math.toRadians(endLng - startLng);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) *
                Math.cos(latitude2) * Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    /**
     * This method is necessary for the SensorEventListener implementation. It is not used however.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     **************************** End of Compass Code **********************************************
     */
}

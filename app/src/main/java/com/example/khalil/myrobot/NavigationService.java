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
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.R.id.message;

public class NavigationService extends Service implements
        SensorEventListener{
    private LocationHelper locationHelper = new LocationHelper(this);

    public double nextDestinationLat;  // This is the final destination latitude.
    public double nextDestinationLng;  // This is the final destination longitude.
    public String routesUrl;  // This is the url that gets filed to the API.
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
    private Sensor gsensor;  // This is the phone's gravity sensor.
    private Sensor msensor;  // This is the phone's magnetic sensor.

    // These variables are necessary for finding the phone's current bearing due compass north and
            // the phone's bearing to a custom location point, which, in this case, is the next
            // location point in the routes array.
    private float[] mGravity = new float[3];  //
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private static final int earthRadius = 6371;

    public double distance;  // This stores the distance between the robot and the final
            // destination.

    /**
     * This method sets up the Google API client, the sensors, and the intent that gets sent back
     * to TaskActivity.
    */
    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate");
        locationHelper.startnavigationservice();
        intent = new Intent(BROADCAST_ACTION);  // This creates the broadcast intent.

        // This block initiates the sensors and start them.
        sensorManager = (SensorManager) getBaseContext()
                .getSystemService(Context.SENSOR_SERVICE);
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
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
     * This method is what gets everything going. It takes in the destination SMS message as an
     * input parameter. It also has the current location which is stored as a global variable
     * due to LocationHelper. startNavigation then constructs the url using getUrl().
     * Afterwards, it uses FetchUrl which takes care of downloading the url and sending it to
     * DataParser for parsing. What is returned is a List<List<HashMap<String, String>>> and
     * stored as routesList by the use of get() from ParserTask. Then routesUrl is
     * converted to an array using  positionArray(). The second point in this array is
     * extracted and that is the point that the robot will move to. Afterwards, the compass code
     * block is used to find the bearing to this point and passes it as an intent. Furthermore,
     * since we know the final destination, calculateDistance() is used to find the
     * distance between the robot and the final destination and it is then passed to TaskActivity
     * as a broadcast intent.
     */
    public void startNavigation(String destinationSms) {
        ArrayList<Location> pointsArray = null;

        if (locationHelper.currentLocationLatLng != null) {
            Log.d(TAG, "startNavigation");
            LatLng origin = locationHelper.currentLocationLatLng;  // Retrieves current location.
            String dest = destinationSms;  // Retrieves final destination.

            // Getting URL to the Google Directions API.
            String url = getUrl(origin, dest);
            Log.d("onMapClick", url.toString());

            // Start downloading json data from Google Directions API.
            try {
                routesUrl = new FetchUrl().execute(url).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            // Invokes the thread for parsing the JSON data.
            List<List<HashMap<String, String>>> routesList = null;
            try {
                routesList = new ParserTask().execute(routesUrl).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            pointsArray = positionArray(routesList);  // converts routesList to an array of
                    // locations.

            // This IF block retreives the second point in the array unless the array's size is 1.
            if (pointsArray.size() == 1) {
                nextDestinationObj = pointsArray.get(0);
            } else {
                nextDestinationObj = pointsArray.get(1);
            }

            nextDestinationLat = nextDestinationObj.getLatitude();
            nextDestinationLng = nextDestinationObj.getLongitude();
            destination = new LatLng(nextDestinationObj.getLatitude(),
                    nextDestinationObj.getLongitude());
            Location finalDestination = pointsArray.get(pointsArray.size() - 1);
            double finalDestinationLat = finalDestination.getLatitude();
            double finalDestinationLng = finalDestination.getLongitude();
            distance = calculateDistance(locationHelper.currentLocationLat, locationHelper.currentLocationLng,
                    finalDestinationLat, finalDestinationLng);

            intent.putExtra("distance", distance);
            sendBroadcast(intent);  // Intent is broadcast.
        }
        // This IF block puts the thread to sleep for four seconds when it is done. However, if
                // the distnace is below 5m, it will stop the service.
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
        double d = earthRadius * c * 1000;
        return d;
    }

    /**
     * This method converts a List<List<HashMap<String, String>>> into an ArrayList<Location>.
    */
    public ArrayList<Location> positionArray(List<List<HashMap<String, String>>> routesList) {
        ArrayList<Location> points = null;

        // Traversing through all the routes
        for (int i = 0; i < routesList.size(); i++) {
            points = new ArrayList<>();

            // Fetching i-th route
            List<HashMap<String, String>> path = routesList.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                Location position = new Location(LocationManager.GPS_PROVIDER);
                position.setLatitude(lat);
                position.setLongitude(lng);

                points.add(position);
            }
        }
        return points;
    }

    /**
     * This method builds the url that gets filed to the Google API. It takes in two parameters,
     * the first is the LatLng of the robot's current location, the second is the string
     * destination, which is retrieved from the SMS passed to this service.
    */
     public String getUrl(LatLng origin, String dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // If the SMS destination is composed of multiple words like "Engineering Fountain", this
                // block of code breaks this phrase into separate words and add a "+" sign between
                // each word resulting in "Engineering+Fountain". This is done because the url filed
                // to Google has to be in a certain format.
        String[] encodeDest = dest.split("\\P{L}+");
        StringBuilder str_dest = new StringBuilder("destination=");
        for (int encodeLength = 0; encodeLength < encodeDest.length; encodeLength++) {
            str_dest.append(encodeDest[encodeLength] + "+");
        }

        // This stores the destination as a variable.
        String destination = str_dest.toString();
        Log.i("SmsReceiver", "Destination: " + destination + "; message: " + message);

        // Sensor enabled.
        String sensor = "sensor=false";

        // Building the parameters to the web service.
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format.
        String output = "json";

        // Building the url to the web service.
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters
                + "&mode=walking";  // The mode is walking.

        return url;
    }

    /**
     * This method downloads the data returned from the API as a string.
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url.
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url.
            urlConnection.connect();

            // Reading data from url.
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
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
     * This class Fetches data from url passed. It runs in the background as an AsyncTask. After
     * the data is retrieved, this class starts another background class called ParserTask,
     * which parses the data.
     */
    public class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service.
            String data = "";

            try {
                // Fetching the data from web service.
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data.
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Directions in JSON format. This occurs in the background. Here,
     * DataParser is called.
     */
    private class ParserTask extends AsyncTask<String, Integer,
            List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread.
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process.
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;

            // Traversing through all the routes.
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();

                // Fetching i-th route.
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route.
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }
        }
    }

    /**
     * This method is called when the service stops. It disconnects the API client.
     */
    @Override
    public void onDestroy() {
//        mGoogleApiClient.disconnect();
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
        sensorManager.registerListener(this, gsensor,
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, msensor,
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
                azimuth = (float) Math.toDegrees(orientation[0]); // orientation
                azimuth = (azimuth + 360) % 360;
                if (destination != null) {
                    Toast.makeText(this, "I've been called", Toast.LENGTH_SHORT).show();
                    azimuth -= bearing(locationHelper.currentLocationLat, locationHelper.currentLocationLng, nextDestinationLat,
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
        double longitude1 = startLng;
        double longitude2 = endLng;
        double latitude1 = Math.toRadians(startLat);
        double latitude2 = Math.toRadians(endLat);
        double longDiff = Math.toRadians(longitude2 - longitude1);
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

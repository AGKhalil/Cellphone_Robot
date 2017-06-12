package com.example.khalil.myrobot;

/**
 * Created by Khalil on 4/7/17.
 * This class takes care of parsing the information retrieved from Google API. This class is
 * courtesy of: https://www.androidtutorialpoint.com/intermediate/google-maps-draw-path-two-points-using-google-directions-google-map-android-api-v2/
 */

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
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

class DataParser {

    private static String message;

    //Constructor get Context
    DataParser(String message){
        DataParser.message = message;
    }

    /**
     * This method builds the url that gets filed to the Google API. It takes in two parameters,
     * the first is the LatLng of the robot's current location, the second is the string
     * destination, which is retrieved from the SMS passed to this service.
     */
    String getUrl(LatLng origin, String dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // If the SMS destination is composed of multiple words like "Engineering Fountain", this
        // block of code breaks this phrase into separate words and add a "+" sign between
        // each word resulting in "Engineering+Fountain". This is done because the url filed
        // to Google has to be in a certain format.
        String[] encodeDest = dest.split("\\P{L}+");
        StringBuilder str_dest = new StringBuilder("destination=");
        for (String anEncodeDest : encodeDest) {
            str_dest.append(anEncodeDest).append("+");
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

        // Return the url to the web service.
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters
                + "&mode=walking";
    }

    /**
     * This method downloads the data returned from the API as a string.
     */
    String downloadUrl(String strUrl) throws IOException {
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

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data);
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            assert iStream != null;
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /** Receives a JSONObject and returns a list of lists containing latitude and longitude. */
    List<List<HashMap<String,String>>> parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<>() ;
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List<HashMap<String, String>> path = new ArrayList<>();

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        String polyline;
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline"))
                                .get("points");
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString((list.get(l)).latitude) );
                            hm.put("lng", Double.toString((list.get(l)).longitude) );
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception ignored){
        }
        return routes;
    }


    /**
     * Method to decode polyline points.
     * Courtesy: http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-
     * maps-direction-api-with-java
     * */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    /**
     * This method converts a List<List<HashMap<String, String>>> into an ArrayList<Location>.
     */
    ArrayList<Location> positionArray(List<List<HashMap<String, String>>> routesList) {
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
}

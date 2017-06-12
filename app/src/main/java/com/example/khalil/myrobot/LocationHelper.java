package com.example.khalil.myrobot;

/**
 * Created by Khalil on 4/11/17.
 * This is a helper class that takes care of requesting and receiving the phone's current location.
 * It does so every 750ms.
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;


public class LocationHelper implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener{
    /**
     * This method files for a location request. It specifies the priority as high accuracy and uses
     * ACCESS_FINE_LOCATION for best results. It also sets the time interval after which another
     * location request will be filed.
    */
    GoogleApiClient mGoogleApiClient;  // This is the API client that interacts with the Google API.
    Location mCurrentLocation;  // This variable stores the robot's current location.
    public LatLng currentLocationLatLng;  // This variable stores the current location as a LatLng.
    public double currentLocationLat;  // This is the current location latitude.
    public double currentLocationLng;  // This is the current location longitude.
    static public Context c;
    public static String TAG = "LocationHelper";
    //Constructor get Context
    public LocationHelper(Context c){
        this.c = c;
    }

    static void initLocation(LocationListener listener, GoogleApiClient apiClient) {

        Log.d(TAG, "initLocation");
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(750);
        mLocationRequest.setFastestInterval(750);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(c,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, mLocationRequest,
                    listener);
        }

    }

    void startnavigationservice() {
         Log.d(TAG, "startnavigationservice");
        // This IF block checks for permission and then builds the Google API client.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(c,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
            }
        } else {
            buildGoogleApiClient();
        }
    }

    /**
     * This method builds the Google Api Client and establishes the listener.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(c)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * This method is called when a connection to the Google API is established. The LocationHelper
     * is called to start the current location requests.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        initLocation(this,
                mGoogleApiClient);
    }

    /**
     * This method is called when a connection to the Google API is suspended.
     */
    @Override
    public void onConnectionSuspended(int i) {
    }

    /**
     * This method is called when a connection to the Google API has failed.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    /**
     * This method is called when the robot's location changes. This updates all the current
     * location variables.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        currentLocationLat = location.getLatitude();
        currentLocationLng = location.getLongitude();
        currentLocationLatLng = new LatLng(currentLocationLat, currentLocationLng);
    }


}

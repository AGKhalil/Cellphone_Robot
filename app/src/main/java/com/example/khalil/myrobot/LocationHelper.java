package com.example.khalil.myrobot;

/**
 * Created by Khalil on 4/11/17.
 * This is a helper class that takes care of requesting and receiving the phone's current location.
 * It does so every 750ms.
 */

import android.Manifest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {
    /**
     * This method files for a location request. It specifies the priority as high accuracy and uses
     * ACCESS_FINE_LOCATION for best results. It also sets the time interval after which another
     * location request will be filed.
    */
    static void initLocation(Context c, LocationListener listener, GoogleApiClient apiClient) {
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
}

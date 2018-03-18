package com.jahangir.fyp.recievers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.jahangir.fyp.utils.AppUtils;
import com.jahangir.fyp.utils.AttendanceUtils;

/**
 * Created by Bilal Rashid on 3/18/2018.
 */

public class MsgReceiver extends BroadcastReceiver {
    Context context;
    String number;
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 1000;  /* 1 sec */
    private long FASTEST_INTERVAL = 500; /* 1/2 sec */
    public static int counter = 0;
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "";
        String msg = "";
        this.context = context;
        if (bundle != null){
            // Retrieve the Binary SMS data
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            // For every SMS message received (although multipart is not supported with binary)
            for (int i=0; i<msgs.length; i++) {
                byte[] data = null;
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                number = msgs[i].getOriginatingAddress();
                str += "Binary SMS from " + msgs[i].getOriginatingAddress() + " :";
                str += "\nBINARY MESSAGE: ";
                // Return the User Data section minus the
                // User Data Header (UDH) (if there is any UDH at all)
                data = msgs[i].getUserData();
                // Generally you can do away with this for loop
                // You'll just need the next for loop
                for (int index=0; index < data.length; index++) {
                    str += Byte.toString(data[index]);
                }
                str += "\nTEXT MESSAGE (FROM BINARY): ";
                for (int index=0; index < data.length; index++) {
                    msg += Character.toString((char) data[index]);
                }
                str += "\n";
            }
            if(msg.contains("buzzer")){
                AppUtils.playSound(context);

            }else if(msg.contains("tracking")){
                startLocationUpdates();
            } else if(msg.contains("vibrate")){
                AppUtils.vibrate(context);

            }else if(msg.contains("profile")){
                AppUtils.changeProfile(context);

            }else {
                AppUtils.turnFlashOn();

            }
        }
    }
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        final LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        counter++;
                        if(counter > 2) {
                            onLocationChanged(locationResult.getLastLocation());
                            LocationServices.getFusedLocationProviderClient(context).removeLocationUpdates(this);
                        }
                    }
                },
                Looper.myLooper());
    }
    public void onLocationChanged(Location location) {
        // New location has now been determined
        counter = 0;
        String loc = Double.toString(location.getLatitude()) + "_" +
                Double.toString(location.getLongitude());
        AttendanceUtils.sendLocation(context,loc);

    }
}
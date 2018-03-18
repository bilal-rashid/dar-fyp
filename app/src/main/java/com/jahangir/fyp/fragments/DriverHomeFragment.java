package com.jahangir.fyp.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.jahangir.fyp.FrameActivity;
import com.jahangir.fyp.R;
import com.jahangir.fyp.dialog.SimpleDialog;
import com.jahangir.fyp.models.User;
import com.jahangir.fyp.toolbox.ToolbarListener;
import com.jahangir.fyp.utils.ActivityUtils;
import com.jahangir.fyp.utils.AppUtils;
import com.jahangir.fyp.utils.AttendanceUtils;
import com.jahangir.fyp.utils.LoginUtils;

/**
 * Created by Bilal Rashid on 1/20/2018.
 */

public class DriverHomeFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {
    private ViewHolder mHolder;
    private User mUser;
    private SimpleDialog mSimpleDialog;
    private static final int MY_SMS_REQ_CODE_EMERGENCY= 3;
    private static final int MY_SMS_REQ_CODE_CHECKIN= 4;
    private static final int MY_SMS_REQ_CODE_CHECKOUT= 5;
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 1000;  /* 1 sec */
    private long FASTEST_INTERVAL = 500; /* 1/2 sec */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ToolbarListener) {
            ((ToolbarListener) context).setTitle("Driver",true);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_home, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUser = LoginUtils.getUser(getContext());
        mHolder = new ViewHolder(view);
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(AppUtils.getImageOrientation(mUser.image_path));
            Bitmap bitmap = BitmapFactory.decodeFile(mUser.image_path);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
            mHolder.profileImage.setImageBitmap(rotatedBitmap);
        }catch (Exception e){
            mHolder.profileImage.setImageResource(R.mipmap.user);
        }
        mHolder.empCodeText.setText(mUser.employee_code);
        mHolder.usernameText.setText(mUser.username);
        mHolder.alarmCard.setOnTouchListener(this);
        mHolder.checkinCard.setOnTouchListener(this);
        mHolder.checkoutCard.setOnTouchListener(this);
        mHolder.logoutCard.setOnTouchListener(this);

        mHolder.alarmCard.setOnClickListener(this);
        mHolder.checkinCard.setOnClickListener(this);
        mHolder.checkoutCard.setOnClickListener(this);
        mHolder.logoutCard.setOnClickListener(this);
        if(AttendanceUtils.isGuardCheckin(getContext())){
            mHolder.checkinCard.setEnabled(false);
            mHolder.checkinCard.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey));
            mHolder.checkoutCard.setEnabled(true);
            mHolder.checkoutCard.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.card_checkout_color));
        }
        else {
            mHolder.checkinCard.setEnabled(true);
            mHolder.checkinCard.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.card_checkin_color));
            mHolder.checkoutCard.setEnabled(false);
            mHolder.checkoutCard.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey));
        }
    }
    @Override
    public void onClick(View view) {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        switch (view.getId()){
            case R.id.card_logout:
                mSimpleDialog = new SimpleDialog(getContext(), null, getString(R.string.msg_logout),
                        getString(R.string.button_cancel), getString(R.string.button_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.button_positive:
                                LoginUtils.logout(getContext());
                                ActivityUtils.startHomeActivity(getContext(), FrameActivity.class,null);
                                mSimpleDialog.dismiss();
                                break;
                            case R.id.button_negative:
                                mSimpleDialog.dismiss();
                                break;
                        }
                    }
                });
                mSimpleDialog.show();
                break;
            case R.id.card_alarm:
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                }else {
                    mSimpleDialog = new SimpleDialog(getContext(), null, getString(R.string.msg_alarm),
                            getString(R.string.button_cancel), getString(R.string.button_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            switch (view.getId()) {
                                case R.id.button_positive:
                                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                        mSimpleDialog.dismiss();
                                        requestPermissions(
                                                new String[]{Manifest.permission.SEND_SMS},
                                                MY_SMS_REQ_CODE_EMERGENCY);
                                    } else {
                                        startLocationUpdates(3);
                                        mSimpleDialog.dismiss();
                                        break;
                                    }

                                case R.id.button_negative:
                                    mSimpleDialog.dismiss();
                                    break;
                            }
                        }
                    });
                    mSimpleDialog.show();
                }
                break;
            case R.id.card_checkin:
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                }
                else {
                    mSimpleDialog = new SimpleDialog(getContext(), null, getString(R.string.msg_checkin),
                            getString(R.string.button_cancel), getString(R.string.button_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            switch (view.getId()) {
                                case R.id.button_positive:
                                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                        mSimpleDialog.dismiss();
                                        requestPermissions(
                                                new String[]{Manifest.permission.SEND_SMS},
                                                MY_SMS_REQ_CODE_CHECKIN);
                                    } else {
                                        mHolder.checkinCard.setEnabled(false);
                                        mHolder.checkinCard.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey));
                                        mHolder.checkoutCard.setEnabled(true);
                                        mHolder.checkoutCard.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.card_checkout_color));
                                        AttendanceUtils.checkinGuard(getContext());
                                        startLocationUpdates(1);
                                        AppUtils.startPulse(getContext());
                                        mSimpleDialog.dismiss();
                                    }
                                    break;
                                case R.id.button_negative:
                                    mSimpleDialog.dismiss();
                                    break;
                            }
                        }
                    });
                    mSimpleDialog.show();
                }
                break;
            case R.id.card_checkout:
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                }
                else {
                    mSimpleDialog = new SimpleDialog(getContext(), null, getString(R.string.msg_checkout),
                            getString(R.string.button_cancel), getString(R.string.button_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            switch (view.getId()) {
                                case R.id.button_positive:
                                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                        mSimpleDialog.dismiss();
                                        requestPermissions(
                                                new String[]{Manifest.permission.SEND_SMS},
                                                MY_SMS_REQ_CODE_CHECKOUT);
                                    } else {
                                        mHolder.checkoutCard.setEnabled(false);
                                        mHolder.checkoutCard.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey));
                                        mHolder.checkinCard.setEnabled(true);
                                        mHolder.checkinCard.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.card_checkin_color));
                                        AttendanceUtils.checkoutGuard(getContext());
                                        startLocationUpdates(2);
                                        AppUtils.stopPulse(getContext());
                                        mSimpleDialog.dismiss();
                                    }
                                    break;
                                case R.id.button_negative:
                                    mSimpleDialog.dismiss();
                                    break;
                            }
                        }
                    });
                    mSimpleDialog.show();
                }
                break;
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Your GPS seems to be disabled, please enable it to proceed")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()) {
            case R.id.card_alarm:
                animate(view, motionEvent, ContextCompat.getColor(getActivity(), R.color.card_alarm_color),
                        ContextCompat.getColor(getActivity(), R.color.card_alarm_color_pressed));
                break;
            case R.id.card_logout:
                animate(view, motionEvent, ContextCompat.getColor(getActivity(), R.color.card_logout_color),
                        ContextCompat.getColor(getActivity(), R.color.card_logout_color_pressed));
                break;
            case R.id.card_checkin:
                animate(view, motionEvent, ContextCompat.getColor(getActivity(), R.color.card_checkin_color),
                        ContextCompat.getColor(getActivity(), R.color.card_checkin_color_pressed));
                break;
            case R.id.card_checkout:
                animate(view, motionEvent, ContextCompat.getColor(getActivity(), R.color.card_checkout_color),
                        ContextCompat.getColor(getActivity(), R.color.card_checkout_color_pressed));
                break;
        }
        return false;
    }
    private void animate(View view, MotionEvent motionEvent, int color, int colorPressed) {
        CardView cardView = (CardView) view;
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                cardView.setCardBackgroundColor(color);
                break;
            case MotionEvent.ACTION_CANCEL:
                cardView.setCardBackgroundColor(color);
                break;
            case MotionEvent.ACTION_DOWN:
                cardView.setCardBackgroundColor(colorPressed);
                break;
            case MotionEvent.ACTION_MOVE:
                cardView.setCardBackgroundColor(colorPressed);
                break;

        }
    }
    public void onLocationChanged(Location location,int status) {
        // New location has now been determined
        counter = 0;
        String loc = Double.toString(location.getLatitude()) + "_" +
                Double.toString(location.getLongitude());
        if(status == 1){
            AttendanceUtils.sendCheckin(getContext(),loc);
        }else if (status == 2) {
            AttendanceUtils.sendCheckout(getContext(),loc);
        }else {
            AttendanceUtils.sendEmergency(getContext(),loc);
        }


    }

    public static int counter = 0;
    protected void startLocationUpdates(final int status) {

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
        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(getActivity()).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        counter++;
                        if(counter > 1) {
                            onLocationChanged(locationResult.getLastLocation(),status);
                            LocationServices.getFusedLocationProviderClient(getActivity()).removeLocationUpdates(this);
                        }
                    }
                },
                Looper.myLooper());
    }

    public static class ViewHolder {
        ImageView profileImage;
        TextView usernameText, empCodeText;
        CardView alarmCard, checkinCard, checkoutCard,logoutCard;
        public ViewHolder(View view) {
            profileImage = (ImageView) view.findViewById(R.id.image_profile);
            usernameText = (TextView) view.findViewById(R.id.text_user_name);
            empCodeText = (TextView) view.findViewById(R.id.text_emp_id);

            alarmCard = (CardView) view.findViewById(R.id.card_alarm);
            checkinCard = (CardView) view.findViewById(R.id.card_checkin);
            checkoutCard = (CardView) view.findViewById(R.id.card_checkout);
            logoutCard = (CardView) view.findViewById(R.id.card_logout);
        }

    }
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_SMS_REQ_CODE_CHECKIN: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mHolder.checkinCard.setEnabled(false);
                    mHolder.checkinCard.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey));
                    AttendanceUtils.checkinGuard(getContext());
                    startLocationUpdates(1);
                    AppUtils.startPulse(getContext());
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    AppUtils.showSnackBar(getView(), getString(R.string.err_permission_not_granted));
                }
                return;
            }
            case MY_SMS_REQ_CODE_CHECKOUT:{
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mHolder.checkoutCard.setEnabled(false);
                    mHolder.checkoutCard.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey));
                    AttendanceUtils.checkoutGuard(getContext());
                    startLocationUpdates(2);
                    AppUtils.stopPulse(getContext());
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    AppUtils.showSnackBar(getView(), getString(R.string.err_permission_not_granted));
                }
                return;
            }
            case MY_SMS_REQ_CODE_EMERGENCY: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates(3);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    AppUtils.showSnackBar(getView(), getString(R.string.err_permission_not_granted));
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}

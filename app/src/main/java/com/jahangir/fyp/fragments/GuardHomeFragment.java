package com.jahangir.fyp.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

public class GuardHomeFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {
    private ViewHolder mHolder;
    private User mUser;
    private SimpleDialog mSimpleDialog;
    private static final int MY_SMS_REQ_CODE_EMERGENCY= 3;
    private static final int MY_SMS_REQ_CODE_CHECKIN= 4;
    private static final int MY_SMS_REQ_CODE_CHECKOUT= 5;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ToolbarListener) {
            ((ToolbarListener) context).setTitle("Guard",true);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guard_home, container, false);
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
        }
        if(AttendanceUtils.isGuardCheckout(getContext())){
            mHolder.checkoutCard.setEnabled(false);
            mHolder.checkoutCard.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey));
        }
    }
    @Override
    public void onClick(View view) {
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
                                }else {
                                    AttendanceUtils.sendEmergency(getContext());
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
                break;
            case R.id.card_checkin:
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
                                }else {
                                    mHolder.checkinCard.setEnabled(false);
                                    mHolder.checkinCard.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey));
                                    AttendanceUtils.checkinGuard(getContext());
                                    AttendanceUtils.sendCheckin(getContext());
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
                break;
            case R.id.card_checkout:
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
                                }else {
                                    mHolder.checkoutCard.setEnabled(false);
                                    mHolder.checkoutCard.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey));
                                    AttendanceUtils.checkoutGuard(getContext());
                                    AttendanceUtils.sendCheckout(getContext());
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
                break;
        }
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
                    AttendanceUtils.sendCheckin(getContext());
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
                    AttendanceUtils.sendCheckout(getContext());
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
                    AttendanceUtils.sendEmergency(getContext());
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

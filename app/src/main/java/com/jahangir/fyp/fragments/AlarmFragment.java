package com.jahangir.fyp.fragments;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jahangir.fyp.R;
import com.jahangir.fyp.toolbox.ToolbarListener;
import com.jahangir.fyp.utils.AppUtils;
import com.jahangir.fyp.utils.AttendanceUtils;

import in.shadowfax.proswipebutton.ProSwipeButton;

/**
 * Created by Bilal Rashid on 1/24/2018.
 */

public class AlarmFragment extends Fragment implements ProSwipeButton.OnSwipeListener {


    private ViewHolder mHolder;
    private Handler mHandler;
    MediaPlayer mediaPlayer;
    public int count;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mHandler.removeCallbacks(mRunnable);
                count++;
                if(count > 40){
                    mHolder.proSwipeBtn.showResultIcon(false);
                    AttendanceUtils.sendNotResponded(getContext());
                    mediaPlayer.stop();
                    getActivity().finish();
                    Log.d("TAAAG","FINISGED");
                }
                else {
                    mHandler.postDelayed(mRunnable, 1000);
                }
            }catch (Exception e){}

        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ToolbarListener) {
            ((ToolbarListener) context).setTitle("Alarm",true);
        }
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alarm, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHolder = new ViewHolder(view);
        mHolder.proSwipeBtn.setOnSwipeListener(this);
        count = 0;
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 500);
        AppUtils.IncreaseSound(getContext());
        mediaPlayer = MediaPlayer.create(getContext(), Settings.System.DEFAULT_ALARM_ALERT_URI);
        mediaPlayer.setVolume(100,100);
        mediaPlayer.start();
        mediaPlayer.setLooping(true);
    }
    @Override
    public void onSwipeConfirm() {
        mediaPlayer.stop();
        mHandler.removeCallbacks(mRunnable);
        AttendanceUtils.sendResponded(getContext());
        getActivity().finish();
    }

    public static class ViewHolder {

        ProSwipeButton proSwipeBtn;

        public ViewHolder(View view) {
            proSwipeBtn = (ProSwipeButton) view.findViewById(R.id.proswipebutton_main);

        }

    }
}


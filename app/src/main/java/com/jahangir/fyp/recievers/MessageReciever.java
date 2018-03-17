package com.jahangir.fyp.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.jahangir.fyp.FrameActivity;
import com.jahangir.fyp.enumerations.StatusEnum;
import com.jahangir.fyp.fragments.EmergencyFragment;
import com.jahangir.fyp.models.Packet;
import com.jahangir.fyp.utils.ActivityUtils;
import com.jahangir.fyp.utils.AppUtils;
import com.jahangir.fyp.utils.Constants;
import com.jahangir.fyp.utils.GsonUtils;
import com.jahangir.fyp.utils.LoginUtils;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by Bilal Rashid on 2/3/2018.
 */

public class MessageReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            // Retrieve the SMS Messages received
            Object[] pdus = (Object[]) bundle.get("pdus");
            String message = SmsMessage.createFromPdu((byte[]) pdus[0]).getMessageBody();
            if (message.contains("\"" + Constants.UNIQUE_ID + "\"")) {
                Packet packet = GsonUtils.fromJson(message, Packet.class);
                if (packet.status.equals(StatusEnum.EMERGENCY.getName())) {
                    if(LoginUtils.isAdminUserLogin(context)) {
                        Log.d("TAAAG", "emergency");
                        PowerManager.WakeLock screenLock = ((PowerManager) context.getSystemService(POWER_SERVICE)).newWakeLock(
                                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
                        screenLock.acquire(5000);
                        AppUtils.vibrate(context);
                        Bundle bundle2 = new Bundle();
                        bundle2.putString(Constants.GUARD_DATA, message);
                        ActivityUtils.startAlarmActivity(context, FrameActivity.class, EmergencyFragment.class.getName(), bundle2, false);
                    }
                }
            }
        }
    }
}

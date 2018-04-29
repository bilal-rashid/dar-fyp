package com.jahangir.fyp.utils;

import android.content.Context;
import android.util.Log;

import com.jahangir.fyp.R;
import com.jahangir.fyp.enumerations.StatusEnum;
import com.jahangir.fyp.models.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bilal Rashid on 1/21/2018.
 */

public class AttendanceUtils {
    public static String getCheckinKey(){
        return Constants.CHECKIN;
    }
    public static String getCheckoutKey(){
        return Constants.CHECKIN;
    }
    public static void checkinGuard(Context context){
        PrefUtils.persistBoolean(context,getCheckinKey(),true);
    }
    public static boolean isGuardCheckin(Context context){
        return  PrefUtils.getBoolean(context,getCheckinKey(),false);
    }
    public static void checkoutGuard(Context context){
        PrefUtils.persistBoolean(context,getCheckoutKey(),false);
    }
    public static boolean isGuardCheckout(Context context){
        return  PrefUtils.getBoolean(context,getCheckoutKey(),false);
    }
    public static void sendCheckin(Context context,String location){
        Packet packet = new Packet(LoginUtils.getUser(context).employee_code,
                StatusEnum.CHECKIN.getName(),AppUtils.getDateAndTime(),location);
        AppUtils.sendSMS(context.getString(R.string.admin_number), GsonUtils.toJson(packet));
    }
    public static void sendCheckout(Context context,String location){
        Packet packet = new Packet(LoginUtils.getUser(context).employee_code,
                StatusEnum.CHECKOUT.getName(),AppUtils.getDateAndTime(),location);
        AppUtils.sendSMS(context.getString(R.string.admin_number), GsonUtils.toJson(packet));
    }
    public static void sendEmergency(Context context,String location){
        Packet packet = new Packet(LoginUtils.getUser(context).employee_code,
                StatusEnum.EMERGENCY.getName(),AppUtils.getDateAndTime(),location);
        AppUtils.sendSMS(context.getString(R.string.admin_number), GsonUtils.toJson(packet));
    }
    public static void sendLocation(Context context,String location){
        Packet packet = new Packet(LoginUtils.getUser(context).employee_code,
                StatusEnum.RESPONSE.getName(),AppUtils.getDateAndTime(),location);
        AppUtils.sendSMS(context.getString(R.string.admin_number), GsonUtils.toJson(packet));
    }
    public static void sendResponded(Context context){
        Packet packet = new Packet(LoginUtils.getUser(context).username+"-"+LoginUtils.getUser(context).employee_code,
                StatusEnum.RESPONSE.getName(),AppUtils.getDateAndTime());
        AppUtils.sendSMS(context.getString(R.string.admin_number), GsonUtils.toJson(packet));
    }
    public static void sendNotResponded(Context context){
        Packet packet = new Packet(LoginUtils.getUser(context).username+"-"+LoginUtils.getUser(context).employee_code,
                StatusEnum.NO_RESPONSE.getName(),AppUtils.getDateAndTime());
        AppUtils.sendSMS(context.getString(R.string.admin_number), GsonUtils.toJson(packet));
    }
    public static List<Packet> getJobPackets(Context context, Packet packet, String number){
        List<Packet> driverPackets = SmsUtils.getGuardPackets(context, number);
        List<Packet> result = new ArrayList<Packet>();
        int index = driverPackets.indexOf(packet);
        for(int i=index; i<driverPackets.size();i++){
            result.add(driverPackets.get(i));
            if (driverPackets.get(i).status.equals(StatusEnum.CHECKIN.getName())){
                break;
            }
        }
        return result;
    }
}

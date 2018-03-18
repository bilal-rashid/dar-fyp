package com.jahangir.fyp.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import com.jahangir.fyp.models.Driver;
import com.jahangir.fyp.models.Packet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bilal Rashid on 1/27/2018.
 */

public class SmsUtils {
    public static List<Driver> getAllGuards(Context context) {
        HashMap<String, String> hashMap = new HashMap<>();
        List<Driver> driverList = new ArrayList<>();
        final String SMS_URI_INBOX = "content://sms/inbox";
        final String SMS_URI_ALL = "content://sms/";
        try {
            Uri uri = Uri.parse(SMS_URI_INBOX);
            String[] projection = new String[]{"address", "body", "date"};
            Cursor cur = context.getContentResolver().query(uri, projection, null, null, "date desc");
            if (cur.moveToFirst()) {
                int index_Address = cur.getColumnIndex("address");
                int index_Body = cur.getColumnIndex("body");
                do {
                    String strAddress = cur.getString(index_Address);
                    String strbody = cur.getString(index_Body);
                    if (strbody.contains("\"" + Constants.UNIQUE_ID + "\"")) {
                        hashMap.put(strAddress, GsonUtils.fromJson(strbody, Packet.class).emp_id);
                    }
                } while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }
            } else {
            } // end if

        } catch (SQLiteException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
        if (hashMap.size() > 0) {
            for (int i = 0; i < hashMap.size(); i++) {
                driverList.add(new Driver(hashMap.keySet().toArray()[i] + "", hashMap.values().toArray()[i] + ""));
            }
        }
        return driverList;
    }

    public static List<Packet> getGuardPackets(Context context, String number) {
        List<Packet> packets = new ArrayList<>();
        final String SMS_URI_INBOX = "content://sms/inbox";
        final String SMS_URI_ALL = "content://sms/";
        try {
            Uri uri = Uri.parse(SMS_URI_INBOX);
            String[] projection = new String[]{"address", "body", "date"};
            Cursor cur = context.getContentResolver().query(uri, projection, "address='" + number + "'", null, "date desc");
            if (cur.moveToFirst()) {
                int index_Address = cur.getColumnIndex("address");
                int index_Body = cur.getColumnIndex("body");
                do {
                    String strAddress = cur.getString(index_Address);
                    String strbody = cur.getString(index_Body);
                    if (strbody.contains("\"" + Constants.UNIQUE_ID + "\"")) {
                        packets.add(GsonUtils.fromJson(strbody, Packet.class));
                    }
                } while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }
            } else {
            } // end if

        } catch (SQLiteException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
        return packets;
    }
    public static List<Packet> getLastWeekGuardPackets(Context context, String number) {
        List<Packet> packets = new ArrayList<>();
        final String SMS_URI_INBOX = "content://sms/inbox";
        final String SMS_URI_ALL = "content://sms/";
        try {
            Uri uri = Uri.parse(SMS_URI_INBOX);
            String[] projection = new String[]{"address", "body", "date"};
            Cursor cur = context.getContentResolver().query(uri, projection, "address='" + number + "'", null, "date desc");
            if (cur.moveToFirst()) {
                int index_Address = cur.getColumnIndex("address");
                int index_Body = cur.getColumnIndex("body");
                do {
                    String strAddress = cur.getString(index_Address);
                    String strbody = cur.getString(index_Body);
                    if (strbody.contains("\"" + Constants.UNIQUE_ID + "\"")) {
                        Packet temp_packet = GsonUtils.fromJson(strbody, Packet.class);
                        Date lastweek = new Date(Calendar.getInstance().getTime().getTime() - (7L * 24L * 60L * 60L * 1000L));
                        Date packetDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(temp_packet.date_time);
                        if(lastweek.compareTo(packetDate)<1){
                            packets.add(GsonUtils.fromJson(strbody, Packet.class));
                        }
                    }
                } while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }
            } else {
            } // end if

        } catch (SQLiteException ex) {
            Log.d("SQLiteException", ex.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return packets;
    }
    public static List<Packet> getAllPackets(Context context, boolean lastweek){
        List<Packet> packetList = new ArrayList<>();
        List<Driver> driverList = getAllGuards(context);
        for(int i = 0; i< driverList.size(); i++){
            if(lastweek)
                packetList.addAll(getLastWeekGuardPackets(context, driverList.get(i).number));
            else
                packetList.addAll(getGuardPackets(context, driverList.get(i).number));
        }
        return packetList;
    }
}

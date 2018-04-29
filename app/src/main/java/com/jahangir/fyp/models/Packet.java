package com.jahangir.fyp.models;

import com.jahangir.fyp.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Bilal Rashid on 1/23/2018.
 */

public class Packet {
    public String identifier;
    public String emp_id;
    public String status;
    public String date_time;
    public String point;

    public Packet(String emp_id, String status, String date_time) {
        this.emp_id = emp_id;
        this.status = status;
        this.date_time = date_time;
        this.identifier = Constants.UNIQUE_ID;
    }
    public Packet(String emp_id, String status, String date_time,String point) {
        this.emp_id = emp_id;
        this.status = status;
        this.date_time = date_time;
        this.identifier = Constants.UNIQUE_ID;
        this.point = point;
    }
    public int compare(Packet packet) {
        Date packetDate = null;
        Date thisDate= null;
        try {
            packetDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(packet.date_time);
            thisDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(this.date_time);
            return thisDate.compareTo(packetDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        Packet packet = (Packet) obj;
        if(packet.point.equals(this.point) &&
                packet.date_time.equals(this.date_time)&&
                packet.emp_id.equals(this.emp_id)&&
                packet.status.equals(this.status)){
            return true;
        }
        return false;
    }
}

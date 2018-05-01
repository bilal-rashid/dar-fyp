package com.jahangir.fyp;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jahangir.fyp.enumerations.StatusEnum;
import com.jahangir.fyp.models.Driver;
import com.jahangir.fyp.models.Packet;
import com.jahangir.fyp.utils.AttendanceUtils;
import com.jahangir.fyp.utils.Constants;
import com.jahangir.fyp.utils.GsonUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Packet mPacket;
    private Driver mDriver;
    double longitude,latitude;
    List<Packet> mPacketList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        manipulateBundle();
        if(mPacket.status.equals(StatusEnum.CHECKOUT.getName())){
            mPacketList = AttendanceUtils.getJobPackets(this,mPacket,mDriver.number);
//            FirebaseDatabase database = FirebaseDatabase.getInstance();
//            DatabaseReference myRef = database.getReference("message");
//            myRef.setValue("Hello, World!");

        }else {
            try {
                String[] separated = mPacket.point.split("_");
                latitude = Double.parseDouble(separated[0]);
                longitude = Double.parseDouble(separated[1]);
            } catch (Exception e) {
                latitude = Double.parseDouble("31.5");
                longitude = Double.parseDouble("74.3");
            }
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MapsActivity.this.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String[] separated;
        double point_lat,point_long;
        SimpleDateFormat time_format = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat date_format = new SimpleDateFormat("dd-MMM-yyyy");

        // Add a marker in Sydney and move the camera
        if(mPacket.status.equals(StatusEnum.CHECKOUT.getName())){
            PolylineOptions line = new PolylineOptions().width(5).color(Color.GREEN);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = null;
            Calendar cal = null;
            for (int i = 0; i<mPacketList.size(); i++) {
                try {
                    date = format.parse(mPacketList.get(i).date_time);
                    cal = Calendar.getInstance();
                    cal.setTime(date);
                }catch (ParseException e){

                }
                separated = mPacketList.get(i).point.split("_");
                point_lat = Double.parseDouble(separated[0]);
                point_long = Double.parseDouble(separated[1]);
                if(i==0){
                    mMap.addMarker(new MarkerOptions().position(new LatLng(point_lat,point_long)).
                            title(time_format.format(cal.getTime())+"  "+
                                    date_format.format(cal.getTime())).snippet(mPacketList.get(i).status)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                }else if (i==mPacketList.size()-1){
                    mMap.addMarker(new MarkerOptions().position(new LatLng(point_lat,point_long)).
                            title(time_format.format(cal.getTime())+"  "+
                                    date_format.format(cal.getTime())).snippet(mPacketList.get(i).status)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }else {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(point_lat,point_long)).
                            title(time_format.format(cal.getTime())+"  "+
                                    date_format.format(cal.getTime())).snippet(mPacketList.get(i).status)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
                line.add(new LatLng(point_lat,point_long));
                if(i == mPacketList.size()-1){
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(point_lat,point_long)));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
                }
            }
            mMap.addPolyline(line);

        }else {
            LatLng sydney = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(sydney).title(mPacket.status));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
        }
    }
    private void manipulateBundle() {
        Bundle bundle = getIntent().getBundleExtra(Constants.DATA);
        if (bundle !=  null) {
            mPacket = GsonUtils.fromJson(bundle.getString(Constants.PACKET_DATA),Packet.class);
            mDriver = GsonUtils.fromJson(bundle.getString(Constants.GUARD_DATA),Driver.class);
        }
    }
}

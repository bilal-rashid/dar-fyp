package com.jahangir.fyp;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jahangir.fyp.enumerations.StatusEnum;
import com.jahangir.fyp.models.Driver;
import com.jahangir.fyp.models.Packet;
import com.jahangir.fyp.utils.AttendanceUtils;
import com.jahangir.fyp.utils.Constants;
import com.jahangir.fyp.utils.GsonUtils;

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

        }else {
            try {
                String[] separated = mPacket.point.split("_");
                latitude = Double.parseDouble(separated[0]);
                longitude = Double.parseDouble(separated[1]);
            } catch (Exception e) {
                latitude = Double.parseDouble("31.5");
                longitude = Double.parseDouble("74.3");
            }
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
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

        // Add a marker in Sydney and move the camera
        if(mPacket.status.equals(StatusEnum.CHECKOUT.getName())){

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

package com.jahangir.fyp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jahangir.fyp.MapsActivity;
import com.jahangir.fyp.R;
import com.jahangir.fyp.adapters.PacketsAdapter;
import com.jahangir.fyp.models.Driver;
import com.jahangir.fyp.models.Packet;
import com.jahangir.fyp.toolbox.OnItemClickListener;
import com.jahangir.fyp.toolbox.ToolbarListener;
import com.jahangir.fyp.utils.ActivityUtils;
import com.jahangir.fyp.utils.AppUtils;
import com.jahangir.fyp.utils.Constants;
import com.jahangir.fyp.utils.GsonUtils;
import com.jahangir.fyp.utils.SmsUtils;

import java.util.List;

/**
 * Created by Bilal Rashid on 1/28/2018.
 */

public class DriverDetailsFragment extends Fragment implements OnItemClickListener{

    private ViewHolder mHolder;
    private Driver mDriver;
    private List<Packet> mPacketList;
    private PacketsAdapter mPacketsAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ToolbarListener) {
            ((ToolbarListener) context).setTitle("Driver Details",false);
        }
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_detail, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHolder = new ViewHolder(view);
        manipulateBundle();
        mPacketList = SmsUtils.getGuardPackets(getContext(), mDriver.number);
        mHolder.emp_id_text.setText(mDriver.emp_id);
        if(mPacketList.size() > 0){
            setupRecyclerView();
            populateData(mPacketList);
        }else {
        }

    }

    private void manipulateBundle() {
        if (getArguments() != null) {
            mDriver = GsonUtils.fromJson(getArguments().getString(Constants.GUARD_DATA),Driver.class);
        }
    }
    private void setupRecyclerView() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mHolder.guardsRecycler.setLayoutManager(mLayoutManager);
        mPacketsAdapter= new PacketsAdapter(this);
        mHolder.guardsRecycler.setAdapter(mPacketsAdapter);
    }
    private void populateData(List<Packet> objects) {
        mPacketsAdapter.addAll(objects);
    }

    @Override
    public void onItemClick(View view, Object data, int position) {
        Packet packet = (Packet) data;
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PACKET_DATA, GsonUtils.toJson(packet));
        ActivityUtils.startActivity(getActivity(), MapsActivity.class,bundle);
    }

    public static class ViewHolder {

        RecyclerView guardsRecycler;
        TextView emp_id_text;

        public ViewHolder(View view) {
            guardsRecycler = (RecyclerView) view.findViewById(R.id.recycler_driver);
            emp_id_text = (TextView) view.findViewById(R.id.text_emp_id);
        }
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.driver_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_location:
                sendTrigger("tracking");
                return true;
            case R.id.action_buzzer:
                sendTrigger("buzzer");
                return true;
            case R.id.action_vibrate:
                sendTrigger("vibrate");
                return true;
            case R.id.action_profile:
                sendTrigger("profile");
                return true;
            case R.id.action_flash:
                sendTrigger("flash");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendTrigger(String msg) {
        SmsManager smsManager = SmsManager.getDefault();
        short port = 6696;

        smsManager.sendDataMessage(mDriver.number.toString(), null, port, msg.getBytes(), null, null);
    }

}

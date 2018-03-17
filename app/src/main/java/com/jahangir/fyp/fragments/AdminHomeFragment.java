package com.jahangir.fyp.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.jahangir.fyp.FrameActivity;
import com.jahangir.fyp.R;
import com.jahangir.fyp.adapters.GuardAdapter;
import com.jahangir.fyp.api.ApiClient;
import com.jahangir.fyp.api.ApiInterface;
import com.jahangir.fyp.dialog.SimpleDialog;
import com.jahangir.fyp.models.Guard;
import com.jahangir.fyp.models.Packet;
import com.jahangir.fyp.models.ResponseModel;
import com.jahangir.fyp.toolbox.OnItemClickListener;
import com.jahangir.fyp.toolbox.ToolbarListener;
import com.jahangir.fyp.utils.ActivityUtils;
import com.jahangir.fyp.utils.AppUtils;
import com.jahangir.fyp.utils.Constants;
import com.jahangir.fyp.utils.GsonUtils;
import com.jahangir.fyp.utils.LoginUtils;
import com.jahangir.fyp.utils.SmsUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Bilal Rashid on 1/20/2018.
 */

public class AdminHomeFragment extends Fragment implements View.OnClickListener, OnItemClickListener {

    private ViewHolder mHolder;
    private List<Guard> mGuardList;
    private SimpleDialog mSimpleDialog;
    private GuardAdapter mGuardAdapter;
    private Handler mHandler;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mHandler.removeCallbacks(mRunnable);
                Syncdata();
            } catch (Exception e) {
            }

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
            ((ToolbarListener) context).setTitleAdmin("Admin", true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHolder = new ViewHolder(view);
        mHolder.progressBar.setVisibility(View.GONE);
        mHandler = new Handler();
        getMessagesAndPopulateList();

    }

    private static final int MY_WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 3;

    public void getMessagesAndPopulateList() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_SMS},
                    MY_WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
            return;
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
            return;
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.RECEIVE_SMS},
                    MY_WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
            return;
        }
        mGuardList = SmsUtils.getAllGuards(getContext());
        if (mGuardList.size() > 0) {
            setupRecyclerView();
            populateData(mGuardList);
            mHolder.errorContainer.setVisibility(View.GONE);
        } else {
            mHolder.errorContainer.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mHolder.guardsRecycler.setLayoutManager(mLayoutManager);
        mGuardAdapter = new GuardAdapter(this);
        mHolder.guardsRecycler.setAdapter(mGuardAdapter);
    }

    private void populateData(List<Guard> objects) {
        mGuardAdapter.addAll(objects);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onItemClick(View view, Object data, int position) {
        Guard guard = (Guard) data;
        Bundle bundle = new Bundle();
        bundle.putString(Constants.GUARD_DATA, GsonUtils.toJson(guard));
        ActivityUtils.startActivity(getActivity(), FrameActivity.class, GuardDetailsFragment.class.getName(), bundle);
    }

    public static class ViewHolder {

        RecyclerView guardsRecycler;
        LinearLayout errorContainer;
        ProgressBar progressBar;

        public ViewHolder(View view) {
            guardsRecycler = (RecyclerView) view.findViewById(R.id.recycler_guards);
            errorContainer = (LinearLayout) view.findViewById(R.id.error_container);
            progressBar = (ProgressBar) view.findViewById(R.id.progress);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.admin_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                mSimpleDialog = new SimpleDialog(getContext(), null, getString(R.string.msg_logout),
                        getString(R.string.button_cancel), getString(R.string.button_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.button_positive:
                                LoginUtils.logout(getContext());
                                ActivityUtils.startHomeActivity(getContext(), FrameActivity.class, null);
                                mSimpleDialog.dismiss();
                                break;
                            case R.id.button_negative:
                                mSimpleDialog.dismiss();
                                break;
                        }
                    }
                });
                mSimpleDialog.show();
                return true;
            case R.id.action_sync:
                if (AppUtils.isInternetAvailable(getContext())) {
                    mHolder.progressBar.setVisibility(View.VISIBLE);
                    mHandler.postDelayed(mRunnable, 100);
                } else {
                    AppUtils.makeToast(getContext(), "Internet not Available");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void Syncdata() {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        List<Packet> list = SmsUtils.getAllPackets(getContext(), true);
        if (list.size() > 0) {
            Call<ResponseModel> call = apiService.TEST(list);
            call.enqueue(new Callback<ResponseModel>() {
                @Override
                public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                    mHolder.progressBar.setVisibility(View.GONE);
                    if (response.body().Status) {
                        AppUtils.showSnackBar(getView(), "Sync Completed Successfully");
                    } else {
                        AppUtils.showSnackBar(getView(), "UnSuccessful");
                    }
                }

                @Override
                public void onFailure(Call<ResponseModel> call, Throwable t) {
                    mHolder.progressBar.setVisibility(View.GONE);
                    AppUtils.showSnackBar(getView(), "Some Error Occurred");
                }
            });
        } else {
            AppUtils.showSnackBar(getView(), "Data not available");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getMessagesAndPopulateList();
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

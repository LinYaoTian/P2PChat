package com.rdc.p2p.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rdc.p2p.R;
import com.rdc.p2p.contract.ScanDeviceContract;
import com.rdc.p2p.presenter.ScanDevicePresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

/**
 * Created by Lin Yaotian on 2018/5/14.
 */
public class ScanDeviceFragment extends DialogFragment implements ScanDeviceContract.View {

    private View mView;
    private Activity mActivity;
    private ScanDevicePresenter mPresenter;
    private String mKey;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mPresenter  = new ScanDevicePresenter();
        mPresenter.attachView(this);
        mView = inflater.inflate((R.layout.dialog_scab_device_progress),container,false);
        return mView;
    }

    public void setData(String key){
        mKey = key;
    }

    public String getData(){
        return mKey;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        EventBus.getDefault().register(this);
        mPresenter.scanDevice();
    }

    @Subscribe
    public void get(String s){

    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void scanDeviceSuccess(final List<String> ipList) {
        EventBus.getDefault().postSticky(ipList);
        dismiss();
    }

    @Override
    public void scanDeviceError(final String message) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mView.getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        dismiss();
    }
}

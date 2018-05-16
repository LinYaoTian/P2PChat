package com.rdc.p2p.presenter;

import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.contract.ScanDeviceContract;
import com.rdc.p2p.model.ScanDeviceModel;

import java.util.List;

/**
 * Created by Lin Yaotian on 2018/5/14.
 */
public class ScanDevicePresenter extends BasePresenter<ScanDeviceContract.View> implements ScanDeviceContract.Presenter {

    private ScanDeviceContract.Model model;

    public ScanDevicePresenter(){
        model = new ScanDeviceModel(this);
    }

    @Override
    public void scanDevice() {
        model.scanDevice();
    }

    @Override
    public void scanDeviceSuccess(List<String> ipList) {
        if (isAttachView()){
            getMvpView().scanDeviceSuccess(ipList);
        }
    }

    @Override
    public void scanDeviceError(String message) {
        if (isAttachView()){
            getMvpView().scanDeviceError(message);
        }
    }

}

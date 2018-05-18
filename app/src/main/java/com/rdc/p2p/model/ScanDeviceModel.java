package com.rdc.p2p.model;

import android.text.TextUtils;
import android.util.Log;

import com.rdc.p2p.base.BaseModel;
import com.rdc.p2p.contract.ScanDeviceContract;
import com.rdc.p2p.presenter.ScanDevicePresenter;
import com.rdc.p2p.util.ScanDeviceUtil;

import java.util.List;

/**
 * Created by Lin Yaotian on 2018/5/14.
 */
public class ScanDeviceModel extends BaseModel implements ScanDeviceContract.Model {

    private ScanDeviceContract.Presenter mPresenter;

    public ScanDeviceModel(ScanDeviceContract.Presenter presenter){
        mPresenter = presenter;
    }

    @Override
    public void scanDevice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!ScanDeviceUtil.getInstance().getLocalAddressPrefix()){
                    mPresenter.scanDeviceError("扫描端口失败，请检查WIFI连接！");
                    return;
                }
                Long startTime = System.currentTimeMillis();//开始扫描的时间
                ScanDeviceUtil.getInstance().scan();//开始扫描
                while (true) {
                    if (System.currentTimeMillis() - startTime > 30000){
                        //如果扫描时间超过30秒，则提示扫描端口超时！
                        mPresenter.scanDeviceError("扫描端口超时，请重新扫描端口！");
                        break;
                    }
                    try {
                        if (ScanDeviceUtil.getInstance().isFinish()) {// 扫描结束,开始验证
                            List<String> ipList = ScanDeviceUtil.getInstance().getIpList();
                            Log.d("ScanDeviceModel", "ipList："+ipList.toString());
                            mPresenter.scanDeviceSuccess(ipList);
                            break;
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        break;
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        break;
                    }
                }
            }


        }).start();
    }
}

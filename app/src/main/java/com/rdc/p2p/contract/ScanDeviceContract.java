package com.rdc.p2p.contract;

import java.util.List;

/**
 * Created by Lin Yaotian on 2018/5/14.
 */
public interface ScanDeviceContract {
    interface View{
        void scanDeviceSuccess(List<String> ipList);
        void scanDeviceError(String message);
    }

    interface Presenter{
        void scanDevice();
        void scanDeviceSuccess(List<String> ipList);
        void scanDeviceError(String message);
    }

    interface Model{
        void scanDevice();
    }
}

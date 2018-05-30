package com.rdc.p2p.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;

import com.rdc.p2p.bean.UserBean;
import com.rdc.p2p.util.UserUtil;

/**
 * Created by Lin Yaotian on 2018/5/1.
 */

public class App extends Application {
    
    @SuppressLint("StaticFieldLeak")
    private static Context sContxet;
    private static UserBean sUserBean;
    private static String sMyIP;

    @Override
    public void onCreate() {
        super.onCreate();
        sContxet = getApplicationContext();
        sUserBean = getUserBean();
        sMyIP = getMyIP();
    }

    public static String getMyIP() {
        if (sMyIP == null){
            sMyIP = UserUtil.getMyIp();
        }
        return sMyIP;
    }

    public static void setMyIP(String sMyIP) {
        UserUtil.saveMyIp(sMyIP);
        App.sMyIP = sMyIP;
    }

    public static UserBean getUserBean() {
        if (sUserBean == null){
            sUserBean = UserUtil.getUser();
        }
        return sUserBean;
    }

    public static void setUserBean(UserBean sUserBean) {
        App.sUserBean = sUserBean;
    }

    public static Context getContxet() {
        return sContxet;
    }

    public static void setContxet(Context sContxet) {
        App.sContxet = sContxet;
    }
}

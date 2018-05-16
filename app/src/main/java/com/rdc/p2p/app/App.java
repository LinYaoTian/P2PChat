package com.rdc.p2p.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.rdc.p2p.bean.UserBean;
import com.rdc.p2p.util.UserUtil;

/**
 * Created by Lin Yaotian on 2018/5/1.
 */

public class App extends Application {
    
    @SuppressLint("StaticFieldLeak")
    private static Context sContxet;
    private static UserBean sUserBean;

    @Override
    public void onCreate() {
        super.onCreate();
        sContxet = getApplicationContext();
        sUserBean = UserUtil.getUser();
    }

    public static UserBean getUserBean() {
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

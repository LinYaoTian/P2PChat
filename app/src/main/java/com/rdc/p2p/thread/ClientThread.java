package com.rdc.p2p.thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by Lin Yaotian on 2018/5/20.
 */
public class ClientThread implements Runnable{
    private Handler handler;

    public ClientThread(){
        Looper.loop();
        handler  = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {

                return false;
            }
        });
    }

    @Override
    public void run() {

    }
}

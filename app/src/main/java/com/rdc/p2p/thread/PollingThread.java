package com.rdc.p2p.thread;

/**
 * Created by Lin Yaotian on 2018/5/18.
 */
public class PollingThread extends Thread {

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}

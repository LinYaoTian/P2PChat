package com.rdc.p2p.model;

import android.annotation.SuppressLint;
import android.util.Log;

import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.contract.ChatDetailContract;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.thread.SocketThread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Lin Yaotian on 2018/5/17.
 */
public class ChatDetailModel implements ChatDetailContract.Model {

    private static final String TAG = "ChatDetailModel";
    private ChatDetailContract.Presenter mPresenter;
    private ThreadPoolExecutor mExecutor;
    /**
     * 核心池大小
     **/
    private static final int CORE_POOL_SIZE = 1;
    /**
     * 线程池最大线程数
     **/
    private static final int MAX_IMUM_POOL_SIZE = 255;

    public ChatDetailModel(ChatDetailContract.Presenter presenter) {
        mPresenter = presenter;
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_IMUM_POOL_SIZE,
                1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
                MAX_IMUM_POOL_SIZE));
    }


    @Override
    public void sendMessage(final MessageBean msg, final String targetIp) {
        Log.d(TAG, "sendMessage: "+msg.getText());
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                SocketThread socketThread = SocketManager.getInstance().getSocketThreadByIp(targetIp);
                if (socketThread != null){
                    if (socketThread.sendMsg(msg)){
                        Log.d(TAG, "run: "+msg.getText());
                        mPresenter.sendSuccess(msg);
                    }
                }else{
                    mPresenter.sendError("Socket连接已断开！");
                }
            }
        });
    }

}

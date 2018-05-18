package com.rdc.p2p.model;

import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.contract.ChatDetailContract;
import com.rdc.p2p.manager.SocketManager;

/**
 * Created by Lin Yaotian on 2018/5/17.
 */
public class ChatDetailModel implements ChatDetailContract.Model {

    private ChatDetailContract.Presenter mPresenter;

    public ChatDetailModel(ChatDetailContract.Presenter presenter){
        mPresenter = presenter;
    }

    @Override
    public void sendMessage(final MessageBean msg, final String targetIp) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                 if (SocketManager.getInstance().sendMsg(targetIp,msg)){
                     mPresenter.sendSuccess();
                 }else{
                     mPresenter.sendError("发送失败!");
                 }
            }
        }).start();
    }
}

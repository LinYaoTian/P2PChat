package com.rdc.p2p.model;

import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.ChatDetailContract;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.util.ImageUtil;
import com.zxy.tiny.callback.FileCallback;

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
                 switch (msg.getMsgType()){
                     case Protocol.TEXT:
                         if (SocketManager.getInstance().sendMsg(targetIp,msg)){
                             mPresenter.sendSuccess(msg);
                         }else{
                             mPresenter.sendError("发送失败!");
                         }
                         break;
                     case Protocol.IMAGE:
                         if (SocketManager.getInstance().sendImage(targetIp,msg)){
                             mPresenter.sendSuccess(msg);
                         }else{
                             mPresenter.sendError("发送失败!");
                         }
                         break;
                 }

            }
        }).start();
    }
}

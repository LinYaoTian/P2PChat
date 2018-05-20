package com.rdc.p2p.contract;

import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.presenter.PeerListPresenter;

/**
 * Created by Lin Yaotian on 2018/5/17.
 */
public interface ChatDetailContract {
    interface View{
        void sendSuccess(MessageBean msg);
        void sendError(String message);
    }

    interface Model{
        void sendMessage(MessageBean msg,String targetIp);
    }

    interface Presenter{
        void sendMessage(MessageBean msg,String targetIp);
        void sendError(String message);
        void sendSuccess(MessageBean msg);
    }

}

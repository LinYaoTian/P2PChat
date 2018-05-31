package com.rdc.p2p.contract;

import com.rdc.p2p.bean.FileBean;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.presenter.PeerListPresenter;

/**
 * Created by Lin Yaotian on 2018/5/17.
 */
public interface ChatDetailContract {
    interface View{
        void linkSocket();
        void sendMsgSuccess(int position);
        void sendMsgError(int position,String error);
        void fileSending(int position, FileBean fileBean);//更新文件发送进度
    }

    interface Model{
        void sendMessage(MessageBean msg,String targetIp,int position);
        void setLinkSocketState(boolean isLink);
        boolean getLinkSocketState();
    }

    interface Presenter{
        void linkSocket();
        void setLinkSocketState(boolean state);
        void sendMsg(MessageBean msg,int position);
        void sendMsgSuccess(int position);
        void sendMsgError(int position,String error);
        void fileSending(int position, FileBean fileBean);//更新文件发送进度
    }

}

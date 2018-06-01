package com.rdc.p2p.contract;

import com.rdc.p2p.bean.MessageBean;

/**
 * Created by Lin Yaotian on 2018/5/17.
 */
public interface ChatDetailContract {
    interface View{
        void linkSocket();
        void sendMsgSuccess(int position);
        void sendMsgError(int position,String error);
        void fileSending(int position,MessageBean messageBean);//更新文件发送进度
    }

    interface Model{
        void sendMessage(MessageBean msg,int position);
        void setLinkSocketState(boolean isLink);
        void exit();
    }

    interface Presenter{
        void linkSocket();
        void setLinkSocketState(boolean state);
        void sendMsg(MessageBean msg,int position);
        void sendMsgSuccess(int position);
        void sendMsgError(int position,String error);
        void fileSending(int position, MessageBean fileBean);//更新文件发送进度
        void exit();
    }

}

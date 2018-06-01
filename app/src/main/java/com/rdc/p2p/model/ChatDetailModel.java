package com.rdc.p2p.model;

import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.FileState;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.ChatDetailContract;
import com.rdc.p2p.listener.OnSocketSendCallback;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.thread.SocketThread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Lin Yaotian on 2018/5/17.
 */
public class ChatDetailModel implements ChatDetailContract.Model {

    private static final String TAG = "ChatDetailModel";
    private ChatDetailContract.Presenter mPresenter;
    private ThreadPoolExecutor mExecutor;
    private AtomicBoolean mIsLinkedSocket;//是否已经连接上Socket
    private AtomicBoolean mIsLinkingSocket;//是否正在连接Socket
    private OnSocketSendCallback mOnSocketSendCallback;
    private String mTargetIp;
    /**
     * 核心池大小
     **/
    private static final int CORE_POOL_SIZE = 1;
    /**
     * 线程池最大线程数
     **/
    private static final int MAX_IMUM_POOL_SIZE = 255;

    public ChatDetailModel(ChatDetailContract.Presenter presenter,String targetIp) {
        mPresenter = presenter;
        mTargetIp = targetIp;
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_IMUM_POOL_SIZE,
                1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
                MAX_IMUM_POOL_SIZE));
        mIsLinkedSocket = new AtomicBoolean(true);
        mIsLinkingSocket = new AtomicBoolean(false);
        mOnSocketSendCallback = new OnSocketSendCallback() {
            @Override
            public void sendMsgSuccess(int position) {
                mPresenter.sendMsgSuccess(position);
            }

            @Override
            public void sendMsgError(int position) {
                mPresenter.sendMsgError(position,"Socket连接已断开！");
            }

            @Override
            public void fileSending(int position,MessageBean messageBean) {
                mPresenter.fileSending(position,messageBean);
            }
        };
        SocketThread socketThread = SocketManager.getInstance().getSocketThreadByIp(targetIp);
        socketThread.setOnSocketSendCallback(mOnSocketSendCallback);
    }

    @Override
    public void sendMessage(final MessageBean msg, final int position) {
        if (mIsLinkedSocket.get()){
            //Socket已经连接
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    SocketThread socketThread = SocketManager.getInstance().getSocketThreadByIp(mTargetIp);
                    if (socketThread != null){
                        socketThread.sendMsg(msg,position);
                    }else{
                        mIsLinkedSocket.set(false);
                        if (msg.getMsgType() == Protocol.FILE){
                            msg.setFileState(FileState.SEND_FILE_ERROR);
                            mPresenter.fileSending(position,msg);
                        }else {
                            mPresenter.sendMsgError(position,"Socket连接已断开！");
                        }
                    }
                }
            });
        }else if (!mIsLinkingSocket.get()){
            //若Socket没有正在连接，立即去连接
            mIsLinkingSocket.set(true);
            mPresenter.linkSocket();
            if (msg.getMsgType() == Protocol.FILE){
                msg.setFileState(FileState.SEND_FILE_ERROR);
                mPresenter.fileSending(position,msg);
            }else {
                mPresenter.sendMsgError(position,"Socket连接已断开！");
            }
        }else {
            //Socket正在连接中
            if (msg.getMsgType() == Protocol.FILE){
                msg.setFileState(FileState.SEND_FILE_ERROR);
                mPresenter.fileSending(position,msg);
            }else {
                mPresenter.sendMsgError(position,"Socket连接已断开！");
            }
        }
    }

    @Override
    public void setLinkSocketState(boolean isLink) {
        if (isLink){
            SocketThread socketThread = SocketManager.getInstance().getSocketThreadByIp(mTargetIp);
            socketThread.setOnSocketSendCallback(mOnSocketSendCallback);
        }
        mIsLinkedSocket.set(isLink);
        mIsLinkingSocket.set(false);
    }

    @Override
    public void exit() {
        SocketManager.getInstance().removeSocketThreadByIp(mTargetIp);
        SocketManager.getInstance().removeSocketByIp(mTargetIp);
    }


}

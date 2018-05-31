package com.rdc.p2p.presenter;

import android.app.Activity;
import android.util.Log;

import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.FileBean;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Constant;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.ChatDetailContract;
import com.rdc.p2p.model.ChatDetailModel;
import com.rdc.p2p.util.ImageUtil;
import com.zxy.tiny.callback.FileCallback;

/**
 * Created by Lin Yaotian on 2018/5/17.
 */
public class ChatDetailPresenter extends BasePresenter<ChatDetailContract.View> implements ChatDetailContract.Presenter {

    private ChatDetailContract.Model mModel;
    private Activity mActivity;
    private String mTargetIp;

    public ChatDetailPresenter(Activity activity,String targetIp){
        mModel = new ChatDetailModel(this);
        mActivity = activity;
        mTargetIp = targetIp;
    }

    @Override
    public void linkSocket() {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().linkSocket();
                }
            });
        }
    }

    @Override
    public void setLinkSocketState(boolean state) {
        mModel.setLinkSocketState(state);
    }

    @Override
    public void sendMsg(final MessageBean msg, final int position) {
            if (msg.getMsgType() == Protocol.IMAGE){
                //压缩图片
                ImageUtil.compressImage(msg.getImagePath(), new FileCallback() {
                    @Override
                    public void callback(boolean isSuccess, String outfile, Throwable t) {
                        if (isSuccess){
                            msg.setImagePath(outfile);
                        }
                        mModel.sendMessage(msg,mTargetIp,position);
                    }
                });
            }else {
                mModel.sendMessage(msg,mTargetIp,position);
            }
            if (msg.getMsgType() == Protocol.FILE){
                msg.transformMessageEntity(mTargetIp).saveOrUpdate("targetIp = ? and filePath = ?",mTargetIp,msg.getFileBean().getFilePath());
            }else {
                msg.transformMessageEntity(mTargetIp).saveOrUpdate("targetIp = ? and sendStatus = 1",mTargetIp);
            }
    }

    @Override
    public void sendMsgSuccess(final int position) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().sendMsgSuccess(position);
                }
            });
        }
    }

    @Override
    public void sendMsgError(final int position, final String error) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().sendMsgError(position,error);
                }
            });
        }
    }

    @Override
    public void fileSending(final int position, final FileBean fileBean) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().fileSending(position, fileBean);
                }
            });
        }
    }
}

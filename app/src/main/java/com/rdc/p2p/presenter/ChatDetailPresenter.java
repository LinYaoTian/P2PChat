package com.rdc.p2p.presenter;

import android.app.Activity;

import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.MessageBean;
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

    public ChatDetailPresenter(Activity activity){
        mModel = new ChatDetailModel(this);
        mActivity = activity;
    }

    @Override
    public void sendMessage(final MessageBean msg, final String targetIp) {
        ImageUtil.compressImage(msg.getImagePath(), new FileCallback() {
            @Override
            public void callback(boolean isSuccess, String outfile, Throwable t) {
                if (isSuccess){
                    msg.setImagePath(outfile);
                }
                mModel.sendMessage(msg,targetIp);
            }
        });
    }


    @Override
    public void sendError(final String message) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().sendError(message);
                }
            });
        }
    }

    @Override
    public void sendSuccess(final MessageBean msg) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().sendSuccess(msg);
                }
            });
        }
    }
}

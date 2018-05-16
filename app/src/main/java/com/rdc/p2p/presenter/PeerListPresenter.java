package com.rdc.p2p.presenter;

import android.app.Activity;
import android.content.Context;

import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.contract.PeerListContract;
import com.rdc.p2p.model.PeerListModel;

import java.util.List;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class PeerListPresenter extends BasePresenter<PeerListContract.View> implements PeerListContract.Presenter {

    private PeerListContract.Model model;
    private Activity mActivity;

    public PeerListPresenter(Activity activity){
        model = new PeerListModel(this);
        mActivity = activity;
    }


    @Override
    public void initSocket() {
        model.initSocket();
    }

    @Override
    public void updatePeerList(final List<PeerBean> list) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().updatePeerList(list);
                }
            });
        }
    }

    @Override
    public void addPeer(final PeerBean peerBean) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().addPeer(peerBean);
                }
            });
        }
    }

    @Override
    public void messageReceived(final PeerBean peerBean) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().messageReceived(peerBean);
                }
            });
        }
    }

    @Override
    public void removePeer(final String ip) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().removePeer(ip);
                }
            });
        }
    }

}

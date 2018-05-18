package com.rdc.p2p.presenter;

import android.app.Activity;

import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.MessageBean;
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
    public void disconnect() {
        model.disconnect();
    }

    @Override
    public void initSocket() {
        model.initServerSocket();
    }

    @Override
    public void linkPeers(List<PeerBean> list) {
        model.linkPeers(list);
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
    public void messageReceived(final MessageBean messageBean) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().messageReceived(messageBean);
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

    @Override
    public void serverSocketError(final String msg) {
        if (isAttachView()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMvpView().serverSocketError(msg);
                }
            });
        }
    }

    @Override
    public boolean isInitServerSocket() {
        return model.isInitServerSocket();
    }

}

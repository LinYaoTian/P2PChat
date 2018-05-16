package com.rdc.p2p.presenter;

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

    public PeerListPresenter(){
        model = new PeerListModel();
    }


    @Override
    public void initSocket() {

    }

    @Override
    public void updatePeerList(List<PeerBean> list) {

    }

    @Override
    public void addPeer(PeerBean peerBean) {

    }

    @Override
    public void messageReceived(MessageBean messageBean) {

    }

    @Override
    public void sendMessage(MessageBean messageBean) {

    }
}

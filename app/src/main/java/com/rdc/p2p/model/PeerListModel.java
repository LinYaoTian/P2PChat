package com.rdc.p2p.model;

import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.contract.PeerListContract;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class PeerListModel implements PeerListContract.Model {

    private PeerListContract.Presenter mPresenter;
    private ServerSocket mServerSocket;

    public PeerListModel(PeerListContract.Presenter presenter){
        mPresenter = presenter;
    }

    @Override
    public void initSocket() {
        try {
            mServerSocket = new ServerSocket(3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void LinkPeers(List<PeerBean> list) {

    }
}

package com.rdc.p2p.contract;

import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.PeerBean;

import java.util.List;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public interface PeerListContract {

    interface View{
        void updatePeerList(List<PeerBean> list);
        void messageReceived(PeerBean peerBean);
        void addPeer(PeerBean peerBean);
    }

    interface Model{
        void initSocket();
    }

    interface Presenter{
        void initSocket();
        void updatePeerList(List<PeerBean> list);
        void addPeer(PeerBean peerBean);
        void messageReceived(MessageBean messageBean);
        void sendMessage(MessageBean messageBean);
    }

}

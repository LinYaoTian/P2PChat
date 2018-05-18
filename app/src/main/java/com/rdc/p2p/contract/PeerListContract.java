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
        void messageReceived(MessageBean messageBean);
        void addPeer(PeerBean peerBean);
        void removePeer(String ip);
        void serverSocketError(String msg);
    }

    interface Model{
        void initServerSocket();
        void linkPeers(List<PeerBean> list);
        void disconnect();
        boolean isInitServerSocket();
    }

    interface Presenter{
        void disconnect();
        void initSocket();
        void linkPeers(List<PeerBean> list);
        void updatePeerList(List<PeerBean> list);
        void addPeer(PeerBean peerBean);
        void messageReceived(MessageBean messageBean);
        void removePeer(String ip);
        void serverSocketError(String msg);
        boolean isInitServerSocket();
    }

}

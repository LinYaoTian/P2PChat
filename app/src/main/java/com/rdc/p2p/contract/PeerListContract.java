package com.rdc.p2p.contract;

import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.listener.ServerSocketInitCallback;

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
        void linkPeerSuccess(PeerBean peerBean);
        void linkPeerError(String message);
    }

    interface Model{
        void initServerSocket(ServerSocketInitCallback callback);
        void linkPeers(List<PeerBean> list);
        void linkPeer(PeerBean peerBean);
        void disconnect();
        boolean isInitServerSocket();
    }

    interface Presenter{
        void disconnect();
        void initSocket(List<PeerBean> list);
        void linkPeers(List<PeerBean> list);
        void linkPeer(PeerBean peerBean);
        void linkPeerSuccess(PeerBean peerBean);
        void linkPeerError(String message);
        void updatePeerList(List<PeerBean> list);
        void addPeer(PeerBean peerBean);
        void messageReceived(MessageBean messageBean);
        void removePeer(String ip);
        void serverSocketError(String msg);
        boolean isServerSocketConnected();
    }

}

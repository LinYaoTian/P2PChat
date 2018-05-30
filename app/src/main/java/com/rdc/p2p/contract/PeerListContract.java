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
        void fileReceiving(MessageBean messageBean);
        void addPeer(PeerBean peerBean);
        void removePeer(String ip);
        void serverSocketError(String msg);
        void linkPeerSuccess(PeerBean peerBean);
        void linkPeerError(String message,String targetIp);
        void initServerSocketSuccess();
    }

    interface Model{
        void initServerSocket();
        void linkPeers(List<String> list);
        void linkPeer(String targetIp);
        void disconnect();
        boolean isInitServerSocket();
    }

    interface Presenter{
        void disconnect();
        void initServerSocketSuccess();
        void initSocket();
        void linkPeers(List<String> list);
        void linkPeer(String targetIp);
        void linkPeerSuccess(PeerBean peerBean);
        void linkPeerError(String message,String targetIp);
        void updatePeerList(List<PeerBean> list);
        void addPeer(PeerBean peerBean);
        void messageReceived(MessageBean messageBean);
        void removePeer(String ip);
        void serverSocketError(String msg);
        boolean isServerSocketConnected();
        void fileReceiving(MessageBean messageBean);
    }

}

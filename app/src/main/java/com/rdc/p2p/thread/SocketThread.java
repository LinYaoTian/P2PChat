package com.rdc.p2p.thread;

import android.util.Log;

import com.rdc.p2p.app.App;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.bean.UserBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.PeerListContract;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.util.GsonUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Lin Yaotian on 2018/5/17.
 */
public class SocketThread implements Runnable {

    public static final String TAG = "SocketThread";
    private Socket socket;
    private DataInputStream dis = null;//读取消息
    private DataOutputStream dos = null;//发送消息
    private PeerListContract.Presenter presenter;
    private String targetIp;


    public SocketThread(Socket socket, PeerListContract.Presenter presenter){
        this.socket = socket;
        this.presenter = presenter;

        targetIp = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            while (true){
                  int type = dis.readInt();
                if (type == Protocol.KEEP_LIVE){
                    continue;
                }
                String u = dis.readUTF();
                Log.d(TAG, "type:"+type+",user:"+u);
                UserBean userBean = GsonUtil.gsonToBean(u, UserBean.class);
                PeerBean peer = new PeerBean();
                peer.setUserIp(targetIp);
                peer.setUserImageId(userBean.getUserImageId());
                peer.setNickName(userBean.getNickName());
                peer.setRecentMessage("");
                switch (type) {
                    case Protocol.DISCONNECT:
                        throw new IOException();
                    case Protocol.CONNECT:
                        //更新界面
                        presenter.addPeer(peer);
                        //回复连接响应
                        String userGson = GsonUtil.gsonToJson(App.getUserBean());
                        dos.writeInt(Protocol.CONNECT_RESPONSE);
                        dos.writeUTF(userGson);
                        break;
                    case Protocol.CONNECT_RESPONSE:
                        //更新界面
                        Log.d(TAG, "CONNECT_RESPONSE");
                        presenter.addPeer(peer);
                        break;
                    case Protocol.MSG:
                        peer.setRecentMessage(dis.readUTF());
                        MessageBean messageBean = peer.transformToMessageBean();
                        presenter.messageReceived(messageBean);
                        break;
                    case Protocol.IMAGE:

                        break;
                    case Protocol.FILE:

                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            SocketManager.getInstance().removeSocketByIp(targetIp);
            presenter.removePeer(targetIp);
            if (dis != null){
                try {
                    dis.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (dos != null){
                try {
                    dos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}

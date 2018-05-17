package com.rdc.p2p.thread;

import android.util.Log;

import com.rdc.p2p.app.App;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.bean.UserBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.PeerListContract;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.model.PeerListModel;
import com.rdc.p2p.util.GsonUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lin Yaotian on 2018/5/17.
 */
public class SocketThread implements Runnable {

    public static final String TAG = "SocketThread";
    private Socket socket;
    private DataInputStream dis = null;//读取消息
    private DataOutputStream dos = null;//发送消息
    private PeerListContract.Presenter presenter;


    public SocketThread(Socket socket, PeerListContract.Presenter presenter){
        this.socket = socket;
        this.presenter = presenter;
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            while (true){
                int type = dis.readInt();
                String u = dis.readUTF();
                Log.d(TAG, "type:"+type+",user:"+u);
                UserBean userBean = GsonUtil.gsonToBean(u, UserBean.class);
                PeerBean peer = new PeerBean();
                peer.setIp(socket.getInetAddress().getHostAddress());
                peer.setUserImageId(userBean.getUserImageId());
                peer.setNickName(userBean.getNickName());
                peer.setRecentMessage("");
                switch (type) {
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
                        break;
                    case Protocol.IMAGE:

                        break;
                    case Protocol.FILE:

                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            SocketManager.getInstance().removeSocketByIp(socket.getInetAddress().getHostAddress());
            presenter.removePeer(socket.getInetAddress().getHostAddress());
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
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}

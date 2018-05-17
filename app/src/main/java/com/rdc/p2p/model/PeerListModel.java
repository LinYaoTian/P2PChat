package com.rdc.p2p.model;

import android.util.Log;

import com.rdc.p2p.app.App;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.PeerListContract;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.thread.SocketThread;
import com.rdc.p2p.util.GsonUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class PeerListModel implements PeerListContract.Model {

    public static final String TAG = "PeerListModel";

    /** 核心池大小 **/
    private static final int CORE_POOL_SIZE = 1;
    /** 线程池最大线程数 **/
    private static final int MAX_IMUM_POOL_SIZE = 255;

    private PeerListContract.Presenter mPresenter;
    private ServerSocket mServerSocket;
    private ThreadPoolExecutor mExecutor;
    private AtomicBoolean isDestroy;//是否关闭ServerSocket
    private AtomicBoolean isInitServerSocket;

    public PeerListModel(PeerListContract.Presenter presenter){
        mPresenter = presenter;
        isDestroy = new AtomicBoolean(false);
        isInitServerSocket  = new AtomicBoolean(false);

    }

    @Override
    public void initServerSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mServerSocket = new ServerSocket(3000);
                } catch (IOException e) {
                    e.printStackTrace();
                    mPresenter.serverSocketError("启动ServerSocket失败，端口3000被占用！");
                    return;
                }
                mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_IMUM_POOL_SIZE,
                        1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
                        CORE_POOL_SIZE));
                isInitServerSocket.set(true);
                Log.d(TAG, "ServerSocket启动成功！");
                while (!isDestroy.get()){
                    Socket socket;
                    try {
                        Log.d(TAG, "等待socket连接");
                        socket = mServerSocket.accept();
                        Log.d(TAG, "接收到一个socket连接,ip:"+socket.getInetAddress().getHostAddress());
                        SocketManager.getInstance().addSocket(socket.getInetAddress().getHostAddress(),socket);
                        mExecutor.execute(new SocketThread(socket,mPresenter));
                    } catch (IOException e) {
                        e.printStackTrace();
                        mPresenter.serverSocketError("mServerSocket.accept() has error !");
                        break;
                    }
                }
                mExecutor.shutdownNow();
                try {
                    mServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void linkPeers(final List<PeerBean> list) {
        Log.d(TAG, "linkPeers: "+list.toString());
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isInitServerSocket.get()){
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for (PeerBean peerBean : list) {
                    if (!SocketManager.getInstance().isContainSocket(peerBean.getIp())) {
                        Log.d(TAG, "linkPeers: 不存在ip "+peerBean.getIp());
                        DataOutputStream dos = null;
                        try {
                            Socket socket = new Socket(peerBean.getIp(), 3000);
                            SocketManager.getInstance().addSocket(socket.getInetAddress().getHostAddress(), socket);
                            mExecutor.execute(new SocketThread(socket, mPresenter));
                            dos = new DataOutputStream(socket.getOutputStream());
                            String userGson = GsonUtil.gsonToJson(App.getUserBean());
                            dos.writeInt(Protocol.CONNECT);
                            dos.writeUTF(userGson);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    }
            }
        }).start();
    }

    @Override
    public void disconnect() {
        try {
            mExecutor.shutdownNow();
            mServerSocket.close();
            SocketManager.getInstance().destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

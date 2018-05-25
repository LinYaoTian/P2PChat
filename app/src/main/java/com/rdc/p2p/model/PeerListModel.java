package com.rdc.p2p.model;

import android.util.Log;

import com.rdc.p2p.app.App;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.PeerListContract;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.thread.SocketThread;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class PeerListModel implements PeerListContract.Model {

    public static final String TAG = "PeerListModel";

    /**
     * 核心池大小
     **/
    private static final int CORE_POOL_SIZE = 255;
    /**
     * 线程池最大线程数
     **/
    private static final int MAX_IMUM_POOL_SIZE = 255;

    private PeerListContract.Presenter mPresenter;
    private ServerSocket mServerSocket;
    private ThreadPoolExecutor mExecutor;
    private AtomicBoolean isInitServerSocket;
    private Thread mPollingSocketThread;

    public PeerListModel(PeerListContract.Presenter presenter) {
        mPresenter = presenter;
        isInitServerSocket = new AtomicBoolean(false);
    }

    @Override
    public void initServerSocket() {
        isInitServerSocket.set(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mServerSocket = new ServerSocket(3000);
                } catch (IOException e) {
                    e.printStackTrace();
                    mPresenter.serverSocketError("启动ServerSocket失败，端口3000被占用！");
                    isInitServerSocket.set(false);
                    return;
                }
                mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_IMUM_POOL_SIZE,
                        2000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
                        CORE_POOL_SIZE));
                mExecutor.allowCoreThreadTimeOut(true);
                isInitServerSocket.set(true);
                mPresenter.initServerSocketSuccess();
                while (true) {
                    Socket socket;
                    try {
                        socket = mServerSocket.accept();
                        String ip = socket.getInetAddress().getHostAddress();
                        Log.d(TAG, "接收到一个socket连接,ip:" + ip);
                        SocketManager socketManager = SocketManager.getInstance();
                        if (socketManager.isClosedSocket(ip)){
                            SocketThread socketThread = new SocketThread(socket,mPresenter);
                            socketManager.addSocket(ip, socket);
                            socketManager.addSocketThread(ip,socketThread);
                            mExecutor.execute(socketThread);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "mServerSocket.accept() error !");
                        break;
                    }
                }
                mExecutor.shutdownNow();
                try {
                    mServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isInitServerSocket.set(false);
            }
        }).start();
    }

    @Override
    public void linkPeers(final List<PeerBean> list) {
        if (!isInitServerSocket()){
            mPresenter.linkPeerError("请检查WIFI!");
            mPresenter.updatePeerList(new ArrayList<PeerBean>());
        }else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (final PeerBean peerBean : list) {
                        mExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                linkSocket(peerBean);
                            }
                        });
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (SocketManager.getInstance().socketCount() == 0) {
                        mPresenter.updatePeerList(new ArrayList<PeerBean>());
                    }
                }
            }).start();
        }
    }

    @Override
    public void linkPeer(final PeerBean peerBean) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (linkSocket(peerBean)){
                    mPresenter.linkPeerSuccess(peerBean);
                }else {
                    mPresenter.linkPeerError("建立Socket连接失败，对方已退出网络或网络错误！");
                }
            }
        });
    }

    private boolean linkSocket(PeerBean peerBean) {
        Socket socket;
        String targetIp = peerBean.getUserIp();
        try {
            socket = new Socket(targetIp, 3000);
            Log.d(TAG, "linkPeers: ip"+targetIp+"success !");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "linkPeer ip = " + targetIp + ",连接 Socket 失败");
            return false;
        }
        SocketManager socketManager = SocketManager.getInstance();
        SocketThread socketThread;
        if (socketManager.isClosedSocket(targetIp)){
            socketThread = new SocketThread(socket,mPresenter);
            socketManager.addSocket(targetIp, socket);
            socketManager.addSocketThread(targetIp,socketThread);
            mExecutor.execute(socketThread);
            //发送连接请求
            MessageBean messageBean = new MessageBean();
            messageBean.setMsgType(Protocol.CONNECT);
            messageBean.setNickName(App.getUserBean().getNickName());
            messageBean.setUserImageId(App.getUserBean().getUserImageId());
            return socketThread.sendMsg(messageBean);
        }else {
            return false;
        }
    }

    @Override
    public void disconnect() {
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isInitServerSocket.set(false);
            mExecutor.shutdownNow();
            SocketManager.getInstance().destroy();
        }
    }

    @Override
    public boolean isInitServerSocket() {
        return isInitServerSocket.get();
    }

    private void pollingSocket() {
        mPollingSocketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10000);
                        Set<Map.Entry<String, Socket>> socketSet = SocketManager.getInstance().getSocketSet();
                        Iterator<Map.Entry<String, Socket>> iterator = socketSet.iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<String, Socket> entry = iterator.next();
                            Socket socket = entry.getValue();
                            Log.d(TAG, "pollingSocket: "+entry.getKey());
                            DataOutputStream dos = null;
                            try {
                                if (socket == null) {
                                    iterator.remove();
                                    continue;
                                }
//                                socket.setKeepAlive(true);
//                                socket.sendUrgentData(0xFF);

                                OutputStream os = socket.getOutputStream();
                                os.write(Protocol.KEEP_LIVE);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d(TAG, "pollingSocket error: "+entry.getKey());
                                iterator.remove();
                                mPresenter.removePeer(entry.getKey());
                                try {
                                    socket.close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mPollingSocketThread.start();
    }


}

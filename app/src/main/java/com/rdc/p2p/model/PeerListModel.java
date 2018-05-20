package com.rdc.p2p.model;

import android.util.Log;

import com.rdc.p2p.app.App;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.PeerListContract;
import com.rdc.p2p.listener.ServerSocketInitCallback;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.thread.SocketThread;
import com.rdc.p2p.util.GsonUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
    private static final int CORE_POOL_SIZE = 1;
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
    public void initServerSocket(ServerSocketInitCallback callback) {
        isInitServerSocket.set(true);
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
                        1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
                        CORE_POOL_SIZE));
                isInitServerSocket.set(true);
                while (true) {
                    Socket socket;
                    try {
                        Log.d(TAG, "等待socket连接");
                        socket = mServerSocket.accept();
                        Log.d(TAG, "接收到一个socket连接,ip:" + socket.getInetAddress().getHostAddress());
                        SocketManager.getInstance().addSocket(socket.getInetAddress().getHostAddress(), socket);
                        mExecutor.execute(new SocketThread(socket, mPresenter));
                    } catch (IOException e) {
                        e.printStackTrace();
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
        Log.d(TAG, "linkPeers: " + list.toString());
                for (final PeerBean peerBean : list) {
                    if (SocketManager.getInstance().isClosed(peerBean.getUserIp())) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DataOutputStream dos = null;
                                Socket socket = null;
                                try {
                                    socket = new Socket(peerBean.getUserIp(), 3000);
                                    Log.d(TAG, "linkPeers: ip"+peerBean.getUserIp()+"success !");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Log.d(TAG, "linkPeer ip = " + peerBean.getUserIp() + ",连接 Socket 失败");
                                    return;
                                }
                                SocketManager.getInstance().addSocket(peerBean.getUserIp(), socket);
                                mExecutor.execute(new SocketThread(socket, mPresenter));
                                try {
                                    String userGson = GsonUtil.gsonToJson(App.getUserBean());
                                    dos = new DataOutputStream(socket.getOutputStream());
                                    dos.writeInt(Protocol.CONNECT);
                                    dos.writeUTF(userGson);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Log.d(TAG, "linkPeer ip = " + peerBean.getUserIp() + ",发送 Protocol.CONNECT 请求失败!");
                                }
                            }
                        }).start();
                    }
                }
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                if (SocketManager.getInstance().socketNum() == 0) {
//                    mPresenter.updatePeerList(new ArrayList<PeerBean>());
//                }
    }

    @Override
    public void disconnect() {
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mExecutor.shutdownNow();
            SocketManager.getInstance().destroy();
            isInitServerSocket.set(false);
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

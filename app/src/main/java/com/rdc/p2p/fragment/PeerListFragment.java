package com.rdc.p2p.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rdc.p2p.R;
import com.rdc.p2p.activity.ChatDetailActivity;
import com.rdc.p2p.adapter.PeerListRvAdapter;
import com.rdc.p2p.base.BaseFragment;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.contract.PeerListContract;
import com.rdc.p2p.listener.OnClickRecyclerViewListener;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.presenter.PeerListPresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class PeerListFragment extends BaseFragment<PeerListPresenter> implements PeerListContract.View  {

    public static final String TAG = "PeerListFragment";
    private static final int INIT_SERVER_SOCKET = 0;

    @BindView(R.id.rv_peer_list_fragment_peer_list)
    RecyclerView mRvPeerList;
    @BindView(R.id.ll_loadingPeersInfo_fragment_peer_list)
    LinearLayout mLlLoadingPeersInfo;
    @BindView(R.id.tv_tip_nonePeer_fragment_peer_list)
    TextView mTvTipNonePeer;

    private PeerListRvAdapter mPeerListRvAdapter;
    private WifiReceiver mWifiReceiver;
    private List<PeerBean> mPeerList;
    private boolean isFirstScanDeviceFinished;//第一次扫描设备结束
    private Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case INIT_SERVER_SOCKET:
                    mPresenter.initSocket(new ArrayList<PeerBean>());
                    break;
            }
            return true;
        }
    });



    @Override
    protected int setLayoutResourceId() {
        return R.layout.fragment_peer_list;
    }

    @Override
    protected PeerListPresenter getInstance() {
        return new PeerListPresenter(mBaseActivity);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.initSocket(mPeerList);
        EventBus.getDefault().register(this);
        mWifiReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mBaseActivity.registerReceiver(mWifiReceiver, filter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        mBaseActivity.unregisterReceiver(mWifiReceiver);
        mPresenter.disconnect();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void scanDeviceFinished(List<String> ipList){
        mPeerList.clear();
        for (String s : ipList) {
            PeerBean peerBean = new PeerBean();
            peerBean.setUserIp(s);
            mPeerList.add(peerBean);
        }
        if (isFirstScanDeviceFinished){
            //第一次扫描成功
            isFirstScanDeviceFinished = false;
        }else {
            if (mPresenter.isServerSocketConnected()){
                mPresenter.linkPeers(new ArrayList<PeerBean>(mPeerList));
                mPeerList.clear();
                getArguments();
            }
        }

        Log.d(TAG, "scanDeviceFinished: fragment");
    }

    public boolean isServerSocketConnected(){
        return mPresenter.isServerSocketConnected();
    }

    @Override
    protected void initData(Bundle bundle) {
        isFirstScanDeviceFinished = true;
        mPeerList = new ArrayList<>();
    }

    @Override
    protected void initView() {
        mPeerListRvAdapter = new PeerListRvAdapter();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mBaseActivity,DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(mBaseActivity, R.drawable.bg_divider)));
        mRvPeerList.addItemDecoration(dividerItemDecoration);
        mRvPeerList.setLayoutManager(new LinearLayoutManager(mBaseActivity,LinearLayoutManager.VERTICAL,false));
        mRvPeerList.setAdapter(mPeerListRvAdapter);
    }

    @Override
    protected void setListener() {
        mPeerListRvAdapter.setOnRecyclerViewListener(new OnClickRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
                PeerBean peerBean = mPeerListRvAdapter.getDataList().get(position);
                if (SocketManager.getInstance().isClosed(peerBean.getUserIp())){
                    showToast("正在建立Socket连接！");
                    mPresenter.linkPeer(peerBean);
                }else {
                    ChatDetailActivity.actionStart(mBaseActivity,peerBean.getUserIp(),peerBean.getNickName());
                }
            }

            @Override
            public boolean onItemLongClick(int position) {
                return false;
            }

            @Override
            public void onFooterViewClick() {

            }
        });
    }

    @Override
    public void updatePeerList(List<PeerBean> list) {
        if (list.size() == 0){
            Log.d(TAG, "updatePeerList: size=0");
            mRvPeerList.setVisibility(View.GONE);
            mLlLoadingPeersInfo.setVisibility(View.GONE);
            mTvTipNonePeer.setVisibility(View.VISIBLE);
        }else {
            mRvPeerList.setVisibility(View.VISIBLE);
            mLlLoadingPeersInfo.setVisibility(View.GONE);
            mTvTipNonePeer.setVisibility(View.GONE);
            mPeerListRvAdapter.updateData(list);
        }
    }

    @Override
    public void messageReceived(MessageBean messageBean) {
        EventBus.getDefault().postSticky(messageBean);
        List<PeerBean> list = mPeerListRvAdapter.getDataList();
        PeerBean peerBean = messageBean.transformToPeerBean();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUserIp().equals(peerBean.getUserIp())){
                list.set(i,peerBean);
                mPeerListRvAdapter.notifyItemChanged(i);
                return;
            }
        }
        mPeerListRvAdapter.appendData(peerBean);
    }

    @Override
    public void addPeer(PeerBean peerBean) {
        Log.d(TAG, "addPeer: "+peerBean.getUserIp());
        mRvPeerList.setVisibility(View.VISIBLE);
        mLlLoadingPeersInfo.setVisibility(View.GONE);
        mTvTipNonePeer.setVisibility(View.GONE);
        List<PeerBean> list = mPeerListRvAdapter.getDataList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUserIp().equals(peerBean.getUserIp())){
                //已经存在该成员，则更新它的信息
                list.set(i,peerBean);
                mPeerListRvAdapter.notifyItemChanged(i);
                return;
            }
        }
        //不存该成员，则添加进列表
        mPeerListRvAdapter.appendData(peerBean);
    }

    @Override
    public void removePeer(String ip) {
        Log.d(TAG, "removePeer: "+ip);
        List<PeerBean> list = mPeerListRvAdapter.getDataList();
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUserIp().equals(ip)){
                index = i;
                break;
            }
        }
        if (index != -1){
            list.remove(index);
            mPeerListRvAdapter.notifyItemRemoved(index);
        }
        if (list.size() == 0){
            mRvPeerList.setVisibility(View.GONE);
            mLlLoadingPeersInfo.setVisibility(View.GONE);
            mTvTipNonePeer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void serverSocketError(String msg) {
        showToast(msg);
        mRvPeerList.setVisibility(View.GONE);
        mLlLoadingPeersInfo.setVisibility(View.GONE);
        mTvTipNonePeer.setVisibility(View.VISIBLE);
    }

    @Override
    public void linkPeerSuccess(PeerBean peerBean) {
        ChatDetailActivity.actionStart(mBaseActivity,peerBean.getUserIp(),peerBean.getNickName());
    }

    @Override
    public void linkPeerError(String message) {
        showToast(message);
    }

    @Override
    public void initServerSocketSuccess() {
        mPresenter.linkPeers(mPeerList);
    }

    public class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)) {
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.d(TAG, "打开");
                    //已打开
                    if (!mPresenter.isServerSocketConnected()){
                        mHandler.sendEmptyMessage(INIT_SERVER_SOCKET);
                    }
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    //打开中
                    Log.d(TAG, "打开中");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    //已关闭
                    Log.d(TAG, "已关闭");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    //关闭中
                    Log.d(TAG, "关闭中");
                    mPresenter.disconnect();
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    //未知
                    Log.d(TAG, "未知");
                    break;
            }
        }
    }
}

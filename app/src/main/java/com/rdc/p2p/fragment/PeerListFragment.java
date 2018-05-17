package com.rdc.p2p.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rdc.p2p.R;
import com.rdc.p2p.activity.LoginActivity;
import com.rdc.p2p.activity.MainActivity;
import com.rdc.p2p.adapter.PeerListRvAdapter;
import com.rdc.p2p.base.BaseFragment;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.contract.PeerListContract;
import com.rdc.p2p.eventBean.IpDeviceEventBean;
import com.rdc.p2p.listener.OnClickRecyclerViewListener;
import com.rdc.p2p.presenter.PeerListPresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class PeerListFragment extends BaseFragment<PeerListPresenter> implements PeerListContract.View  {

    public static final String TAG = "PeerListFragment";

    @BindView(R.id.rv_peer_list_fragment_peer_list)
    RecyclerView mRvPeerList;
    @BindView(R.id.ll_loadingPeersInfo_fragment_peer_list)
    LinearLayout mLlLoadingPeersInfo;
    @BindView(R.id.tv_tip_nonePeer_fragment_peer_list)
    TextView mTvTipNonePeer;

    private PeerListRvAdapter mPeerListRvAdapter;
    private List<PeerBean> mPeerList = new ArrayList<>();



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
        mPresenter.initSocket();
        mPresenter.linkPeers(mPeerList);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        mPresenter.disconnect();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void ScanDeviceFinished(IpDeviceEventBean deviceEventBean){
        List<String> list = deviceEventBean.getList();
        mPeerList.clear();
        for (String s : list) {
            PeerBean peerBean = new PeerBean();
            peerBean.setIp(s);
            mPeerList.add(peerBean);
        }
        mPresenter.linkPeers(mPeerList);
    }

    public void setPeerList(List<String> list){
        for (String s : list) {
            PeerBean peerBean = new PeerBean();
            peerBean.setIp(s);
            mPeerList.add(peerBean);
        }
        Log.d(TAG, "getPeerList: ");
    }

    @Override
    protected void initData(Bundle bundle) {

    }

    @Override
    protected void initView() {
        mPeerListRvAdapter = new PeerListRvAdapter();
        mRvPeerList.setLayoutManager(new LinearLayoutManager(mBaseActivity,LinearLayoutManager.VERTICAL,false));
        mRvPeerList.setAdapter(mPeerListRvAdapter);
    }

    @Override
    protected void setListener() {
        mPeerListRvAdapter.setOnRecyclerViewListener(new OnClickRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
                String ip = mPeerListRvAdapter.getDataList().get(position).getIp();
                showToast("ip:"+ip);
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
    public void messageReceived(PeerBean peerBean) {
        List<PeerBean> list = mPeerListRvAdapter.getDataList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getIp().equals(peerBean.getIp())){
                list.set(i,peerBean);
                mPeerListRvAdapter.notifyDataSetChanged();
                return;
            }
        }
        list.add(peerBean);
        mPeerListRvAdapter.notifyDataSetChanged();
    }

    @Override
    public void addPeer(PeerBean peerBean) {
        mRvPeerList.setVisibility(View.VISIBLE);
        mLlLoadingPeersInfo.setVisibility(View.GONE);
        mTvTipNonePeer.setVisibility(View.GONE);
        List<PeerBean> list = mPeerListRvAdapter.getDataList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getIp().equals(peerBean.getIp())){
                //已经存在该成员，则更新它的信息
                list.set(i,peerBean);
                mPeerListRvAdapter.notifyDataSetChanged();
                return;
            }
        }
        list.add(peerBean);
        mPeerListRvAdapter.notifyDataSetChanged();
    }

    @Override
    public void removePeer(String ip) {
        List<PeerBean> list = mPeerListRvAdapter.getDataList();
        Iterator<PeerBean> iterator = list.iterator();
        while (iterator.hasNext()){
            PeerBean peerBean = iterator.next();
            if (peerBean.getIp().equals(ip)){
                iterator.remove();
                mPeerListRvAdapter.notifyDataSetChanged();
                break;
            }
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
}

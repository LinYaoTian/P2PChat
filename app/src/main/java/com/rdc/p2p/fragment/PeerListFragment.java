package com.rdc.p2p.fragment;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.rdc.p2p.R;
import com.rdc.p2p.adapter.PeerListRvAdapter;
import com.rdc.p2p.base.BaseFragment;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.contract.PeerListContract;
import com.rdc.p2p.listener.OnClickRecyclerViewListener;
import com.rdc.p2p.presenter.PeerListPresenter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class PeerListFragment extends BaseFragment<PeerListPresenter> implements PeerListContract.View  {

    @BindView(R.id.rv_peer_list_fragment_peer_list)
    RecyclerView mRvPeerList;

    private PeerListRvAdapter mPeerListRvAdapter;
    private List<PeerBean> mPeerList;



    @Override
    protected int setLayoutResourceId() {
        return R.layout.fragment_peer_list;
    }

    @Override
    protected PeerListPresenter getInstance() {
        return new PeerListPresenter(mBaseActivity);
    }


    @Override
    protected void initData(Bundle bundle) {
        mPeerList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            PeerBean peerBean1 = new PeerBean();
            peerBean1.setNickName("小明");
            peerBean1.setRecentMessage("我是琳琳琳！！");
            peerBean1.setUserImageId(9);
            peerBean1.setTime("05:20");
            mPeerList.add(peerBean1);
            PeerBean peerBean2 = new PeerBean();
            peerBean2.setNickName("林耀填");
            peerBean2.setRecentMessage("我是琳琳琳！！");
            peerBean2.setUserImageId(14);
            peerBean2.setTime("19:20");
            mPeerList.add(peerBean2);
        }
    }

    @Override
    protected void initView() {
        mPeerListRvAdapter = new PeerListRvAdapter();
        mRvPeerList.setLayoutManager(new LinearLayoutManager(mBaseActivity,LinearLayoutManager.VERTICAL,false));
        mRvPeerList.setAdapter(mPeerListRvAdapter);
        mPeerListRvAdapter.updateData(mPeerList);
    }

    @Override
    protected void setListener() {
        mPeerListRvAdapter.setOnRecyclerViewListener(new OnClickRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
                String ip = mPeerListRvAdapter.getDataList().get(position).getIp();
                showToast("ip");
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
        mPeerListRvAdapter.updateData(list);
    }

    @Override
    public void messageReceived(PeerBean peerBean) {
        List<PeerBean> list = mPeerListRvAdapter.getDataList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getIp().equals(peerBean.getIp())){
                list.set(i,peerBean);
                mPeerListRvAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void addPeer(PeerBean peerBean) {
        List<PeerBean> list = new ArrayList<>();
        list.add(peerBean);
        mPeerListRvAdapter.appendData(list);
    }

    @Override
    public void removePeer(String ip) {
        List<PeerBean> list = mPeerListRvAdapter.getDataList();
        Iterator<PeerBean> iterator = list.iterator();
        while (iterator.hasNext()){
            PeerBean peerBean = iterator.next();
            if (peerBean.getIp().equals(ip)){
                iterator.remove();
                break;
            }
        }
    }
}

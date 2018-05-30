package com.rdc.p2p.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rdc.p2p.R;
import com.rdc.p2p.base.BaseRecyclerViewAdapter;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.util.ImageUtil;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class PeerListRvAdapter extends BaseRecyclerViewAdapter<PeerBean> {

    /**
     *根据IP删除指定item
     * @param ip
     */
    public void removeItem(String ip){
        int index = getIndexByIp(ip);
        if (index != -1){
            mDataList.remove(index);
            notifyItemRemoved(index);
        }
    }

    /**
     * 添加一个Item
     * @param peerBean
     */
    public void addItem(PeerBean peerBean){
        mDataList.add(peerBean);
        notifyItemRangeChanged(mDataList.size()-1,1);
    }

    /**
     * 更新个Item的Text
     * @param text
     */
    public PeerBean updateItemText(String text, String peerIp){
        int index = getIndexByIp(peerIp);
        if (index != -1){
            PeerBean peer = getDataList().get(index);
            peer.setRecentMessage(text);
            notifyItemChanged(index);
            return peer;
        }
        return null;
    }

    /**
     * 更新某个Item
     * @param peerBean
     */
    public void updateItem(PeerBean peerBean){
        int index = getIndexByIp(peerBean.getUserIp());
        if (index != -1){
            mDataList.set(index,peerBean);
            notifyItemChanged(index);
        }
    }


    /**
     * 是否存在某个Item
     * @param ip
     * @return
     */
    public boolean isContained(String ip){
        return getIndexByIp(ip) != -1;
    }

    /**
     * 根据IP查询该成员在成员列表中的位置
     * @param ip
     * @return 成员的位置 ,如果找不到则返回 -1
     */
    private int getIndexByIp(String ip){
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i).getUserIp().equals(ip)){
                //找到成员的下标
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_peer_list,parent,false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ItemHolder)holder).bindView(mDataList.get(position));
    }

    class ItemHolder extends BaseRvHolder{

        @BindView(R.id.tv_nickname_item_peer_list)
        TextView mTvNickname;
        @BindView(R.id.civ_user_image_item_peer_list)
        CircleImageView mCivUserImage;
        @BindView(R.id.tv_recent_message_item_peer_list)
        TextView mTvRecentMessage;
        @BindView(R.id.tv_time_item_peer_list)
        TextView mTvTime;

        public ItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(PeerBean peerBean) {
            mTvNickname.setText(peerBean.getNickName());
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(peerBean.getUserImageId())).into(mCivUserImage);
            mTvRecentMessage.setText(peerBean.getRecentMessage());
            mTvTime.setText(peerBean.getTime());
        }
    }
}

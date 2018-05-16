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
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.util.ImageUtil;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class PeerListRvAdapter extends BaseRecyclerViewAdapter<PeerBean> {

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

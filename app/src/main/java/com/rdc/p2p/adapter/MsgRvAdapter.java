package com.rdc.p2p.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rdc.p2p.R;
import com.rdc.p2p.base.BaseRecyclerViewAdapter;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.util.ImageUtil;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Lin Yaotian on 2018/2/1.
 */

public class MsgRvAdapter extends BaseRecyclerViewAdapter<MessageBean> {


    @NonNull
    @Override
    public MsgRvAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((Holder)holder).bindView(mDataList.get(position));
    }


    class Holder extends BaseRvHolder {

        @BindView(R.id.rl_left_msg_item_message)
        RelativeLayout mRlLeft;
        @BindView(R.id.rl_right_msg_item_message)
        RelativeLayout mRlRight;
        @BindView(R.id.tv_text_left_item_message)
        TextView mTvLeftMsg;
        @BindView(R.id.tv_text_right_item_message)
        TextView mTvRightMsg;
        @BindView(R.id.civ_head_image_left_item_message)
        CircleImageView mCivLeftHeadImage;
        @BindView(R.id.civ_head_image_right_item_message)
        CircleImageView  mCivRightHeadImage;
        @BindView(R.id.iv_image_left_item_message)
        ImageView mIvLeftImage;
        @BindView(R.id.iv_image_right_item_message)
        ImageView mIvRightImage;
        @BindView(R.id.ll_left_text_item_message)
        LinearLayout mLlLeftText;
        @BindView(R.id.ll_right_text_item_message)
        LinearLayout mLlRightText;
        @BindView(R.id.cv_image_left_item_message)
        CardView mCvLeftImage;
        @BindView(R.id.cv_image_right_item_message)
        CardView mCvRightImage;


        Holder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(MessageBean msg) {
            if (msg.isMine()){
                mRlRight.setVisibility(View.VISIBLE);
                mRlLeft.setVisibility(View.GONE);
                Glide.with(itemView.getContext()).load(
                        ImageUtil.getImageResId(msg.getUserImageId())).into(mCivRightHeadImage);
                switch (msg.getMsgType()){
                    case Protocol.TEXT:
                        mLlRightText.setVisibility(View.VISIBLE);
                        mTvRightMsg.setText(msg.getText());
                        mCvRightImage.setVisibility(View.GONE);
                        break;
                    case Protocol.IMAGE:
                        mCvRightImage.setVisibility(View.VISIBLE);
                        Glide.with(itemView.getContext()).load(msg.getImageUrl()).into(mIvRightImage);
                        mLlRightText.setVisibility(View.GONE);
                        break;
                    case Protocol.FILE:

                        break;
                }
            }else {
                mRlLeft.setVisibility(View.VISIBLE);
                mRlRight.setVisibility(View.GONE);
                Glide.with(itemView.getContext()).load(
                        ImageUtil.getImageResId(msg.getUserImageId())).into(mCivLeftHeadImage);
                switch (msg.getMsgType()){
                    case Protocol.TEXT:
                        mLlLeftText.setVisibility(View.VISIBLE);
                        mTvLeftMsg.setText(msg.getText());
                        mCvLeftImage.setVisibility(View.GONE);
                        break;
                    case Protocol.IMAGE:
                        mCvLeftImage.setVisibility(View.VISIBLE);
                        Glide.with(itemView.getContext()).load(msg.getImageUrl()).into(mIvLeftImage);
                        mLlLeftText.setVisibility(View.GONE);
                        break;
                    case Protocol.FILE:

                        break;
                }
            }
        }
    }

}

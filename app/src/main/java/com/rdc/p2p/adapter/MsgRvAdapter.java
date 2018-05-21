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

    private static final int TYPE_RIGHT_TEXT = 0;
    private static final int TYPE_RIGHT_IMAGE = 1;
    private static final int TYPE_RIGHT_AUDIO = 2;
    private static final int TYPE_LEFT_TEXT = 3;
    private static final int TYPE_LEFT_IMAGE = 4;
    private static final int TYPE_LEFT_AUDIO = 5;

    @Override
    public int getItemViewType(int position) {
        MessageBean messageBean = mDataList.get(position);
        switch (messageBean.getMsgType()){
            case Protocol.TEXT:
                return messageBean.isMine() ? TYPE_RIGHT_TEXT : TYPE_LEFT_TEXT;
            case Protocol.IMAGE:
                return messageBean.isMine() ? TYPE_RIGHT_IMAGE : TYPE_LEFT_IMAGE;
            case Protocol.AUDIO:
                return messageBean.isMine() ? TYPE_RIGHT_AUDIO : TYPE_LEFT_AUDIO;
            default:
                return TYPE_RIGHT_TEXT;
        }
    }

    @NonNull
    @Override
    public BaseRvHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case TYPE_LEFT_TEXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_text, parent, false);
                return new LeftTextHolder(view);
            case TYPE_RIGHT_TEXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_text, parent, false);
                return new RightTextHolder(view);
            case TYPE_LEFT_IMAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_image, parent, false);
                return new LeftImageHolder(view);
            case TYPE_RIGHT_IMAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_image, parent, false);
                return new RightImageHolder(view);
            case TYPE_LEFT_AUDIO:

            case TYPE_RIGHT_AUDIO:

            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_text, parent, false);
                return new LeftTextHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case TYPE_LEFT_TEXT:
                ((LeftTextHolder)holder).bindView(mDataList.get(position));
                break;
            case TYPE_RIGHT_TEXT:
                ((RightTextHolder)holder).bindView(mDataList.get(position));
                break;
            case TYPE_LEFT_IMAGE:
                ((LeftImageHolder)holder).bindView(mDataList.get(position));
                break;
            case TYPE_RIGHT_IMAGE:
                ((RightImageHolder)holder).bindView(mDataList.get(position));
                break;
            case TYPE_LEFT_AUDIO:
                ((LeftTextHolder)holder).bindView(mDataList.get(position));
                break;
            case TYPE_RIGHT_AUDIO:
                ((LeftTextHolder)holder).bindView(mDataList.get(position));
                break;
            default:
                ((LeftTextHolder)holder).bindView(mDataList.get(position));
        }
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
                    case Protocol.AUDIO:

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
                    case Protocol.AUDIO:

                        break;
                }
            }
        }
    }

    class RightTextHolder extends BaseRvHolder{

        @BindView(R.id.civ_head_image_right_item_message)
        CircleImageView  mCivRightHeadImage;
        @BindView(R.id.tv_text_right_item_message)
        TextView mTvRightText;

        public RightTextHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(MessageBean messageBean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(messageBean.getUserImageId()))
                    .into(mCivRightHeadImage);
            mTvRightText.setText(messageBean.getText());
        }
    }

    class LeftTextHolder extends BaseRvHolder{
        @BindView(R.id.civ_head_image_left_item_message)
        CircleImageView mCivLeftHeadImage;
        @BindView(R.id.tv_text_left_item_message)
        TextView mTvLeftText;

        public LeftTextHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(MessageBean messageBean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(messageBean.getUserImageId()))
                    .into(mCivLeftHeadImage);
            mTvLeftText.setText(messageBean.getText());
        }
    }

    class RightImageHolder extends BaseRvHolder{

        @BindView(R.id.civ_head_image_right_item_message)
        CircleImageView  mCivRightHeadImage;
        @BindView(R.id.iv_image_right_item_message)
        ImageView mIvRightImage;

        public RightImageHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(MessageBean messageBean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(messageBean.getUserImageId()))
                    .into(mCivRightHeadImage);
            Glide.with(itemView.getContext())
                    .load(messageBean.getImageUrl())
                    .into(mIvRightImage);
        }
    }

    class LeftImageHolder extends BaseRvHolder{

        @BindView(R.id.civ_head_image_left_item_message)
        CircleImageView mCivLeftHeadImage;
        @BindView(R.id.iv_image_left_item_message)
        ImageView mIvLeftImage;

        public LeftImageHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(MessageBean messageBean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(messageBean.getUserImageId()))
                    .into(mCivLeftHeadImage);
            Glide.with(itemView.getContext())
                    .load(messageBean.getImageUrl())
                    .into(mIvLeftImage);
        }
    }

}

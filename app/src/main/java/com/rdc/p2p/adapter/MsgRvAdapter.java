package com.rdc.p2p.adapter;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rdc.p2p.R;
import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseRecyclerViewAdapter;
import com.rdc.p2p.bean.FileBean;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Constant;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.util.ImageUtil;
import com.rdc.p2p.util.SDUtil;
import com.rdc.p2p.util.ScreenUtil;
import com.rdc.p2p.widget.PlayerSoundView;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Lin Yaotian on 2018/2/1.
 */

public class MsgRvAdapter extends BaseRecyclerViewAdapter<MessageBean> {

    private static final String TAG = "MsgRvAdapter";

    private static final int TYPE_RIGHT_TEXT = 0;
    private static final int TYPE_RIGHT_IMAGE = 1;
    private static final int TYPE_RIGHT_AUDIO = 2;
    private static final int TYPE_LEFT_TEXT = 3;
    private static final int TYPE_LEFT_IMAGE = 4;
    private static final int TYPE_LEFT_AUDIO = 5;
    private static final int TYPE_LEFT_FILE = 6;
    private static final int TYPE_RIGHT_FILE= 7;
    private OnAudioClickListener mOnAudioClickListener;
    private int mTargetPeerImageId;//对方的用户头像id

    public MsgRvAdapter(int userImageId){
        mTargetPeerImageId = userImageId;
    }

    /**
     * 倒序遍历获取包含FileName的Item的下标
     * @param fileName
     * @return
     */
    public int getPositionByFileName(String fileName){
        for (int i = mDataList.size() -1; i >= 0; i--) {
            if (mDataList.get(i).getFileBean() != null){
                FileBean dataBean = mDataList.get(i).getFileBean();
                if (dataBean.getFileName().equals(fileName)){
                    return i;
                }
            }
        }
        return -1;
    }


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
            case Protocol.FILE:
                return messageBean.isMine() ? TYPE_RIGHT_FILE : TYPE_LEFT_FILE;
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_audio, parent, false);
                return new LeftAudioHolder(view);
            case TYPE_RIGHT_AUDIO:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_audio, parent, false);
                return new RightAudioHolder(view);
            case TYPE_LEFT_FILE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_file, parent, false);
                return new LeftFileHolder(view);
            case TYPE_RIGHT_FILE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_file, parent, false);
                return new RightFileHolder(view);
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
                ((LeftAudioHolder)holder).bindView(mDataList.get(position));
                break;
            case TYPE_RIGHT_AUDIO:
                ((RightAudioHolder)holder).bindView(mDataList.get(position));
                break;
            case TYPE_RIGHT_FILE:
                ((RightFileHolder)holder).bindView(mDataList.get(position));
                break;
            case TYPE_LEFT_FILE:
                ((LeftFileHolder)holder).bindView(mDataList.get(position));
                break;
            default:
                ((LeftTextHolder)holder).bindView(mDataList.get(position));
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
        if (payloads.isEmpty()){
            onBindViewHolder(holder,position);
        }else {
            int payload = (int) payloads.get(0);
            switch (payload){
                case Constant.RECEIVE_ING:
                    LeftFileHolder leftFileHolder = (LeftFileHolder) holder;
                    FileBean fileBean1 = mDataList.get(position).getFileBean();
                    float ratio1 = fileBean1.getTransmittedSize()*1f/fileBean1.getFileSize();
                    leftFileHolder.mPbReceive.setProgress((int) (ratio1*100));
                    leftFileHolder.mTvReceiveStates.setText((int)( ratio1*100)+"%");
                    break;
                case Constant.SEND_ING:
                    RightFileHolder rightFileHolder = (RightFileHolder) holder;
                    FileBean fileBean2 = mDataList.get(position).getFileBean();
                    float ratio2 = fileBean2.getTransmittedSize()*1f/fileBean2.getFileSize();
                    rightFileHolder.mPbSending.setProgress((int) (ratio2*100));
                    rightFileHolder.mTvSendStatus.setText((int)( ratio2*100)+"%");
                    break;
            }
        }
    }

    public interface OnAudioClickListener {
        void onClick( PlayerSoundView mPsvPlaySound,String audioUrl);
    }

    public void setOnAudioClickListener(OnAudioClickListener onAudioClickListener){
        this.mOnAudioClickListener = onAudioClickListener;
    }

    class RightAudioHolder extends BaseRvHolder{

        @BindView(R.id.civ_head_image_right_item_message)
        CircleImageView  mCivRightHeadImage;
        @BindView(R.id.psv_play_sound_right_item_message)
        PlayerSoundView mPsvPlaySound;
        @BindView(R.id.ll_right_audio_item_message)
        LinearLayout mLlRightAudio;

        public RightAudioHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(final MessageBean messageBean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(App.getUserBean().getUserImageId()))
                    .into(mCivRightHeadImage);
            if (mOnAudioClickListener != null){
                mLlRightAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnAudioClickListener.onClick(mPsvPlaySound,messageBean.getAudioPath());
                    }
                });
            }
        }
    }

    class LeftAudioHolder extends BaseRvHolder{

        @BindView(R.id.civ_head_image_left_item_message)
        CircleImageView mCivLeftHeadImage;
        @BindView(R.id.psv_play_sound_left_item_message)
        PlayerSoundView mPsvPlaySound;
        @BindView(R.id.ll_left_audio_item_message)
        LinearLayout mLlRightAudio;


        public LeftAudioHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(final MessageBean messageBean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(mTargetPeerImageId))
                    .into(mCivLeftHeadImage);
            if (mOnAudioClickListener != null){
                mLlRightAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnAudioClickListener.onClick(mPsvPlaySound,messageBean.getAudioPath());
                    }
                });
            }
        }
    }

    class RightTextHolder extends BaseRvHolder{

        @BindView(R.id.civ_head_image_right_item_message)
        CircleImageView  mCivRightHeadImage;
        @BindView(R.id.tv_text_right_item_message)
        TextView mTvRightText;


        RightTextHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(MessageBean messageBean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(App.getUserBean().getUserImageId()))
                    .into(mCivRightHeadImage);
            mTvRightText.setText(messageBean.getText());
        }
    }

    class LeftTextHolder extends BaseRvHolder{
        @BindView(R.id.civ_head_image_left_item_message)
        CircleImageView mCivLeftHeadImage;
        @BindView(R.id.tv_text_left_item_message)
        TextView mTvLeftText;

        LeftTextHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(MessageBean messageBean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(mTargetPeerImageId))
                    .into(mCivLeftHeadImage);
            mTvLeftText.setText(messageBean.getText());
        }
    }

    class RightImageHolder extends BaseRvHolder{

        @BindView(R.id.civ_head_image_right_item_message)
        CircleImageView  mCivRightHeadImage;
        @BindView(R.id.iv_image_right_item_message)
        ImageView mIvRightImage;

        RightImageHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(MessageBean messageBean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(App.getUserBean().getUserImageId()))
                    .into(mCivRightHeadImage);
            setIvLayoutParams(mIvRightImage,messageBean.getImagePath());
            Glide.with(itemView.getContext())
                    .load(messageBean.getImagePath())
                    .into(mIvRightImage);
        }
    }

    class LeftImageHolder extends BaseRvHolder{

        @BindView(R.id.civ_head_image_left_item_message)
        CircleImageView mCivLeftHeadImage;
        @BindView(R.id.iv_image_left_item_message)
        ImageView mIvLeftImage;

        LeftImageHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(MessageBean messageBean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(mTargetPeerImageId))
                    .into(mCivLeftHeadImage);
            setIvLayoutParams(mIvLeftImage,messageBean.getImagePath());
            Glide.with(itemView.getContext())
                    .load(messageBean.getImagePath())
                    .into(mIvLeftImage);
        }
    }

    class LeftFileHolder extends BaseRvHolder{

        @BindView(R.id.civ_head_image_left_item_message)
        CircleImageView mCivLeftHeadImage;
        @BindView(R.id.tv_file_name_left_item_message)
        TextView mTvFileName;
        @BindView(R.id.tv_file_size_left_item_message)
        TextView mTvFileSize;
        @BindView(R.id.pb_receiving_progress_left_item_message)
        ProgressBar mPbReceive;
        @BindView(R.id.tv_receiving_states_left_item_message)
        TextView mTvReceiveStates;

        LeftFileHolder(View itemView) {
            super(itemView);
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void bindView(MessageBean bean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(mTargetPeerImageId))
                    .into(mCivLeftHeadImage);
            FileBean fileBean = bean.getFileBean();
            Log.d(TAG, "bindView: "+fileBean.getFileName());
            mTvFileName.setText(fileBean.getFileName());
            mTvFileSize.setText(SDUtil.bytesTransform(fileBean.getFileSize()));
            mPbReceive.setVisibility(View.VISIBLE);
            switch (fileBean.getStates()){
                case Constant.RECEIVE_ING:
                    float ratio = fileBean.getTransmittedSize()*1f/fileBean.getFileSize();
                    mPbReceive.setProgress((int) (ratio*100));
                    mTvReceiveStates.setText(ratio*100+"%");
                    break;
                case Constant.RECEIVE_FINISH:
                    mPbReceive.setVisibility(View.INVISIBLE);
                    mTvReceiveStates.setText("已下载");
                    break;
                case Constant.RECEIVE_ERROR:
                    mPbReceive.setVisibility(View.INVISIBLE);
                    mTvReceiveStates.setText("传输出错");
                    break;
            }
        }
    }

    class RightFileHolder extends BaseRvHolder{

        @BindView(R.id.civ_head_image_right_item_message)
        CircleImageView mCivRightHeadImage;
        @BindView(R.id.tv_file_name_right_item_message)
        TextView mTvFileName;
        @BindView(R.id.tv_file_size_right_item_message)
        TextView mTvFileSize;
        @BindView(R.id.pb_sending_progress_right_item_message)
        ProgressBar mPbSending;
        @BindView(R.id.tv_send_status_right_item_message)
        TextView mTvSendStatus;

        RightFileHolder(View itemView) {
            super(itemView);
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void bindView(MessageBean bean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(App.getUserBean().getUserImageId()))
                    .into(mCivRightHeadImage);
            FileBean fileBean = bean.getFileBean();
            mTvFileName.setText(fileBean.getFileName());
            mTvFileSize.setText(SDUtil.bytesTransform(fileBean.getFileSize()));
            mPbSending.setVisibility(View.VISIBLE);
            switch (fileBean.getStates()){
                case Constant.SEND_ING:
                    float ratio = fileBean.getTransmittedSize()*1f/fileBean.getFileSize();
                    mPbSending.setProgress((int) (ratio*100));
                    mTvSendStatus.setText(ratio*100+"%");
                    break;
                case Constant.SEND_FINISH:
                    mPbSending.setVisibility(View.INVISIBLE);
                    mTvSendStatus.setText("已发送");
                    break;
                case Constant.SEND_ERROR:
                    mPbSending.setVisibility(View.INVISIBLE);
                    mTvSendStatus.setText("传输出错");
                    break;
            }
        }
    }


    /**
     * 根据图片的高宽比例处理ImageView的高宽
     * @param iv
     * @param path
     */
    private void setIvLayoutParams(ImageView iv,String path){
        float scale = ImageUtil.getBitmapSize(path);
        ViewGroup.LayoutParams layoutParams = iv.getLayoutParams();
        int ivWidth;
        if (scale <= 0.65f){
            //宽图
            ivWidth = ScreenUtil.dip2px(App.getContxet(),220);
        }else {
            //长图
            ivWidth = ScreenUtil.dip2px(App.getContxet(),160);
        }
        layoutParams.width = ivWidth;
        layoutParams.height = (int) (ivWidth * scale);
        iv.setLayoutParams(layoutParams);
    }

}

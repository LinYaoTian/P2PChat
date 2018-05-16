package com.rdc.p2p.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.rdc.p2p.R;
import com.rdc.p2p.base.BaseRecyclerViewAdapter;
import com.rdc.p2p.bean.ImageBean;
import com.rdc.p2p.util.ImageUtil;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Lin Yaotian on 2018/5/1.
 */
public class UserImageRvAdapter extends BaseRecyclerViewAdapter<ImageBean> {

    private Context mContext;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_head_image,parent,false);
        return new ImageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ImageHolder)holder).bindView(mDataList.get(position));
    }

    class ImageHolder extends BaseRvHolder{

        @BindView(R.id.civ_user_image_item)
        CircleImageView mCivImage;

        public ImageHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(ImageBean imageBean) {
            Glide.with(mContext).load(ImageUtil.getImageResId(imageBean.getImageId())).into(mCivImage);
        }
    }
}

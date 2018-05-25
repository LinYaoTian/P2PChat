package com.rdc.p2p.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.rdc.p2p.R;
import com.rdc.p2p.base.BaseFragment;
import com.rdc.p2p.base.BasePresenter;

import butterknife.BindView;

/**
 * Created by Lin Yaotian on 2018/5/25.
 */
public class PhotoFragment extends BaseFragment {

    private static final String TAG ="PhotoFragment";
    @BindView(R.id.pv_photo_fragment_photo)
    PhotoView mPvPhoto;
    private String mImagePath;

    @Override
    protected int setLayoutResourceId() {
        return R.layout.fragment_photo;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "initView: "+mImagePath);
        SimpleTarget<Bitmap> simpleTarget = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                mPvPhoto.setImageBitmap(resource);
            }
        };
        Glide.with(mRootView.getContext()).asBitmap().load(mImagePath).into(simpleTarget);
    }

    @Override
    protected BasePresenter getInstance() {
        return null;
    }

    @Override
    protected void initData(Bundle bundle) {

    }

    @Override
    protected void initView() {
    }

    @Override
    protected void setListener() {
        mPvPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBaseActivity.finish();
            }
        });
    }

    public void setData(String path){
        mImagePath = path;
    }
}

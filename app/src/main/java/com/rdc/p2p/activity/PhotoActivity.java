package com.rdc.p2p.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.google.gson.Gson;
import com.rdc.p2p.R;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.fragment.PhotoFragment;
import com.rdc.p2p.util.GsonUtil;
import com.rdc.p2p.widget.PhotosVP;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class PhotoActivity extends BaseActivity {

    @BindView(R.id.photosVP_act_photo)
    PhotosVP mPhotosVp;

    private static List<String> mImagePathList;
    private static int mCurrentPosition;
    private List<PhotoFragment> mPhotoFragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String s = GsonUtil.gsonToJson(mImagePathList);
        outState.putString("ImagePathList",s);
        outState.putInt("CurrentPosition",mCurrentPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String s = savedInstanceState.getString("ImagePathList");
        if (s != null){
            mImagePathList = GsonUtil.gsonToList(s,String.class);
        }
        mCurrentPosition = savedInstanceState.getInt("CurrentPosition");
    }

    @Override
    public BasePresenter getInstance() {
        return null;
    }

    public static void actionStart(Context context,List<String> imagePathList,int currentPosition){
        mImagePathList = imagePathList;
        mCurrentPosition = currentPosition;
        context.startActivity(new Intent(context,PhotoActivity.class));
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_photo;
    }

    @Override
    protected void initData() {
        mPhotoFragmentList = new ArrayList<>();
        for (String s : mImagePathList) {
            PhotoFragment photoFragment = new PhotoFragment();
            photoFragment.setData(s);
            mPhotoFragmentList.add(photoFragment);
        }
    }

    @Override
    protected void initView() {
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public PhotoFragment getItem(int position) {
                return mPhotoFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return mPhotoFragmentList.size();
            }
        };
        mPhotosVp.setAdapter(fragmentPagerAdapter);
        mPhotosVp.setCurrentItem(mCurrentPosition);
    }

    @Override
    protected void initListener() {

    }
}

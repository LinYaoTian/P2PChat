package com.rdc.p2p.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rdc.p2p.R;
import com.rdc.p2p.activity.LoginActivity;
import com.rdc.p2p.adapter.UserImageRvAdapter;
import com.rdc.p2p.bean.ImageBean;
import com.rdc.p2p.listener.OnClickRecyclerViewListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lin Yaotian on 2018/5/1.
 * 登录时选择头像界面
 */
public class SelectImageFragment extends DialogFragment {

    private View mDialogView;
    private List<ImageBean> mImageList;
    private int mImageId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDialogView = inflater.inflate(R.layout.dialog_select_head_image,container);
        initData();
        initViews();
        return mDialogView;
    }

    private void initViews() {
        RecyclerView rvImage = mDialogView.findViewById(R.id.rv_user_image_dialog);
        UserImageRvAdapter mUserImageRvAdapter = new UserImageRvAdapter();
        mUserImageRvAdapter.updateData(mImageList);
        mUserImageRvAdapter.setOnRecyclerViewListener(new OnClickRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
                LoginActivity activity = (LoginActivity) getActivity();
                assert activity != null;
                activity.setImageId(position);
                dismiss();
            }

            @Override
            public boolean onItemLongClick(int position) {
                return false;
            }

            @Override
            public void onFooterViewClick() {

            }
        });
        rvImage.setLayoutManager(new GridLayoutManager(mDialogView.getContext(),3));
        rvImage.setAdapter(mUserImageRvAdapter);
    }

    private void initData() {
        mImageId = 0;
        mImageList = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            ImageBean imageBean = new ImageBean();
            imageBean.setImageId(i);
            mImageList.add(imageBean);
        }
    }

    public int getImageId(){
        return mImageId;
    }
}

package com.rdc.p2p.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.rdc.p2p.app.App;
import com.rdc.p2p.bean.UserBean;
import com.rdc.p2p.fragment.ScanDeviceFragment;
import com.rdc.p2p.fragment.SelectImageFragment;
import com.rdc.p2p.R;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.bean.ImageBean;
import com.rdc.p2p.util.ImageUtil;
import com.rdc.p2p.util.NetUtil;
import com.rdc.p2p.util.UserUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends BaseActivity {

    public static final String TAG = "Login";

    @BindView(R.id.civ_user_image_act_login)
    CircleImageView mCivUserImage;
    @BindView(R.id.et_nickname_act_login)
    EditText mEtNickname;
    @BindView(R.id.btn_login_act_login)
    Button mBtnLogin;

    private List<ImageBean> mImageList;
    private int mSelectedImageId;
    @Override
    public BasePresenter getInstance() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
        getPermission(this);
    }

    /**
     * 获取储存权限
     * @param activity
     * @return
     */

    public void getPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0){
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            finish();
                            showToast("拒绝授权，无法使用本应用！");
                        }
                    }
                }else {
                    showToast("拒绝授权，无法使用本应用！");
                }
                break;
        }
    }
    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_login;
    }

    @Override
    protected void initData() {
        mSelectedImageId = 0;
        mImageList = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            ImageBean imageBean = new ImageBean();
            imageBean.setImageId(i);
            mImageList.add(imageBean);
        }
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initListener() {
        mCivUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImageFragment selectImageFragment = new SelectImageFragment();
                selectImageFragment.show(getSupportFragmentManager(),"DialogFragment");
            }
        });

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mEtNickname.getText())){
                    UserBean userBean = new UserBean();
                    userBean.setNickName(getString(mEtNickname));
                    userBean.setUserImageId(mSelectedImageId);
                    UserUtil.saveUser(userBean);
                    App.setUserBean(userBean);
                    if (NetUtil.isWifi(LoginActivity.this)){
                        ScanDeviceFragment scanDeviceFragment = new ScanDeviceFragment();
                        scanDeviceFragment.setCancelable(false);
                        scanDeviceFragment.show(getSupportFragmentManager(),"progressFragment");
                    }else {
                        showToast("请连接WIFI！");
                    }
                }else {
                    showToast("昵称不能为空！");
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void ScanDeviceFinished(List<String> ipList){
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.putStringArrayListExtra("ipList", (ArrayList<String>) ipList);
        startActivity(intent);
        finish();
    }

    public void setImageId(int imageId){
        mSelectedImageId = imageId;
        Glide.with(this).load(ImageUtil.getImageResId(imageId)).into(mCivUserImage);
    }
}

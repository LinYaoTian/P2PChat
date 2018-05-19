package com.rdc.p2p.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rdc.p2p.R;
import com.rdc.p2p.adapter.MsgRvAdapter;
import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.ChatDetailContract;
import com.rdc.p2p.presenter.ChatDetailPresenter;
import com.rdc.p2p.util.CameraGallaryUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

public class ChatDetailActivity extends BaseActivity<ChatDetailPresenter> implements ChatDetailContract.View {

    private static final String TAG = "ChatDetailActivity";
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.btn_select_image_act_chat_detail)
    Button mBtnSelectImage;
    @BindView(R.id.rv_msg_list_act_chat_detail)
    RecyclerView mRvMsgList;
    @BindView(R.id.btn_send_chat_detail)
    Button mBtnSend;
    @BindView(R.id.et_input_act_chat_detail)
    EditText mEtInput;

    private MsgRvAdapter mMsgRvAdapter;
    private MessageBean mRecentSendMsg;
    private static String mPeerName;
    private static String mPeerIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public static void actionStart(Context context, String peerIp, String peerName){
        mPeerName = peerName;
        mPeerIp = peerIp;
        context.startActivity(new Intent(context,ChatDetailActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public ChatDetailPresenter getInstance() {
        return new ChatDetailPresenter(this);
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_chat_detail;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {
        initToolbar();
        mTvTitle.setText(mPeerName);
        mMsgRvAdapter = new MsgRvAdapter();
        mRvMsgList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        mRvMsgList.setAdapter(mMsgRvAdapter);
    }

    @Override
    protected void initListener() {
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(getString(mEtInput))){
                    mRecentSendMsg = new MessageBean();
                    mRecentSendMsg.setMine(true);
                    mRecentSendMsg.setMsgType(Protocol.MSG);
                    mRecentSendMsg.setNickName(App.getUserBean().getNickName());
                    mRecentSendMsg.setUserImageId(App.getUserBean().getUserImageId());
                    mRecentSendMsg.setMessage(getString(mEtInput));
                    presenter.sendMessage(mRecentSendMsg,mPeerIp);
                }
            }
        });
        mBtnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecentSendMsg = new MessageBean();
                mRecentSendMsg.setMine(true);
                mRecentSendMsg.setMsgType(Protocol.IMAGE);
                mRecentSendMsg.setNickName(App.getUserBean().getNickName());
                mRecentSendMsg.setUserImageId(App.getUserBean().getUserImageId());
                mRecentSendMsg.setMessage(getString(mEtInput));
                presenter.sendMessage(mRecentSendMsg,mPeerIp);
            }
        });
    }

    @Override
    public void sendSuccess() {
        mMsgRvAdapter.appendData(mRecentSendMsg);
    }

    @Override
    public void sendError(String message) {
        showToast(message);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mToolbar.setTitle("");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageBean messageBean){
        Log.d(TAG, "receiveMessage: msgIp="+messageBean.getUserIp()+",mPeerIp="+mPeerIp);
        if (messageBean.getUserIp().equals(mPeerIp)){
            mMsgRvAdapter.appendData(messageBean);
        }
    }
}

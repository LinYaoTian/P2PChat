package com.rdc.p2p.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.EditText;

import com.rdc.p2p.R;
import com.rdc.p2p.adapter.MsgRvAdapter;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.base.BasePresenter;

import butterknife.BindView;

public class ChatDetailActivity extends BaseActivity {

    @BindView(R.id.rv_msg_list_act_chat_detail)
    RecyclerView mRvMsgList;
    @BindView(R.id.btn_send_chat_detail)
    Button mBtnSend;
    @BindView(R.id.et_input_act_chat_detail)
    EditText mEtInput;

    private MsgRvAdapter mMsgRvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public BasePresenter getInstance() {
        return null;
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

    }

    @Override
    protected void initListener() {

    }
}

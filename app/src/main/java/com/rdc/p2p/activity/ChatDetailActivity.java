package com.rdc.p2p.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rdc.p2p.R;
import com.rdc.p2p.adapter.MsgRvAdapter;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.bean.FileBean;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Constant;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.ChatDetailContract;
import com.rdc.p2p.listener.OnClickRecyclerViewListener;
import com.rdc.p2p.presenter.ChatDetailPresenter;
import com.rdc.p2p.util.AudioRecorderUtil;
import com.rdc.p2p.util.MediaPlayerUtil;
import com.rdc.p2p.util.ProgressTextUtil;
import com.rdc.p2p.util.SDUtil;
import com.rdc.p2p.widget.PlayerSoundView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class ChatDetailActivity extends BaseActivity<ChatDetailPresenter> implements ChatDetailContract.View {

    private static final String TAG = "ChatDetailActivity";
    private static final int CHOOSE_PHOTO = 2;
    private static final int TAKE_PHOTO = 3;
    private static final int FILE_MANAGER = 4;
    private static final int SCROLL = -1;//滑动到底部
    private static final int HIDE_SOFT_INPUT = -2;//隐藏软键盘
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.iv_photo_album_act_chat_detail)
    ImageView mIvPhotoAlbum;
    @BindView(R.id.iv_take_photo_act_chat_detail)
    ImageView mIvTakePhoto;
    @BindView(R.id.iv_record_voice_act_chat_detail)
    ImageView mIvRecordVoice;
    @BindView(R.id.rv_msg_list_act_chat_detail)
    RecyclerView mRvMsgList;
    @BindView(R.id.btn_send_chat_detail)
    Button mBtnSend;
    @BindView(R.id.et_input_act_chat_detail)
    EditText mEtInput;
    @BindView(R.id.layout_root_act_chat_detail)
    ConstraintLayout mRootLayout;
    @BindView(R.id.tv_pressed_start_record_act_chat_detail)
    TextView mTvPressedStartRecord;
    @BindView(R.id.iv_file_act_chat_detail)
    ImageView mIvFile;


    private MsgRvAdapter mMsgRvAdapter;
    private static String mTargetPeerName;
    private static String mTargetPeerIp;
    private static int mTargetPeerImageId;
    private Uri mTakePhotoUri;
    private File mTakePhotoFile;
    private AudioRecorderUtil mAudioRecorderUtil;
    private MediaPlayerUtil mMediaPlayerUtil;
    private ImageView mIvMicrophone;
    private TextView mTvRecordTime;
    private PopupWindow mPwMicrophone;
    private PlayerSoundView mPsvIsPlaying = null;//正在播放音频的view
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case SCROLL:
                    if (mMsgRvAdapter.getItemCount() - 1 > 0) {
                        mRvMsgList.smoothScrollToPosition(mMsgRvAdapter.getItemCount() - 1);
                    }
                    break;
                case HIDE_SOFT_INPUT:
                    hideKeyboard();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("peerName", mTargetPeerName);
        outState.putString("peerIp", mTargetPeerIp);
        outState.putInt("peerImageId",mTargetPeerImageId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTargetPeerName = savedInstanceState.getString("peerName");
        mTargetPeerIp = savedInstanceState.getString("peerIp");
        mTargetPeerImageId = savedInstanceState.getInt("peerImageId");
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        mAudioRecorderUtil.stopRecord();
        mMediaPlayerUtil.stopPlayer();
        mPwMicrophone.dismiss();
        super.onDestroy();
    }

    public static void actionStart(Context context, String peerIp, String peerName,int peerImageId) {
        mTargetPeerName = peerName;
        mTargetPeerIp = peerIp;
        mTargetPeerImageId = peerImageId;
        context.startActivity(new Intent(context, ChatDetailActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        mAudioRecorderUtil = new AudioRecorderUtil();
        mMediaPlayerUtil = MediaPlayerUtil.getInstance();
    }

    @Override
    protected void initView() {
        initToolbar();
        mTvTitle.setText(mTargetPeerName);
        mMsgRvAdapter = new MsgRvAdapter(mTargetPeerImageId);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRvMsgList.setLayoutManager(linearLayoutManager);
        mRvMsgList.setAdapter(mMsgRvAdapter);
        mRvMsgList.getItemAnimator().setChangeDuration(0);
        View view = View.inflate(this, R.layout.popupwindow_micorphone, null);
        mPwMicrophone = new PopupWindow(this);
        mPwMicrophone.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mPwMicrophone.setContentView(view);
        mPwMicrophone.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMicrophone.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mIvMicrophone = view.findViewById(R.id.iv_microphone_popupWindow);
        mTvRecordTime = view.findViewById(R.id.tv_record_time_popupWindow);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener() {
        mMsgRvAdapter.setOnAudioClickListener(new MsgRvAdapter.OnAudioClickListener() {
            @Override
            public void onClick(PlayerSoundView psvPlaySound, String audioUrl) {
                if (mPsvIsPlaying != null) {
                    //有正在播放的音频
                    mMediaPlayerUtil.stopPlayer();
                    mPsvIsPlaying.stopPlayer();
                    if (mPsvIsPlaying == psvPlaySound) {
                        //同一个Item，点击则停止播放
                        mPsvIsPlaying = null;
                        return;
                    }
                }
                mPsvIsPlaying = psvPlaySound;
                mPsvIsPlaying.startPlayer();
                mMediaPlayerUtil.startPlayer(audioUrl);
            }
        });
        mMsgRvAdapter.setOnRecyclerViewListener(new OnClickRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
                MessageBean bean = mMsgRvAdapter.getDataList().get(position);
                switch (bean.getMsgType()) {
                    case Protocol.TEXT:

                        break;
                    case Protocol.FILE:
                        if (bean.getFileBean().getStates() != Constant.RECEIVE_ING
                                || bean.getFileBean().getStates() != Constant.SEND_ING){
                            String mineType = SDUtil.getMimeTypeFromFilePath(bean.getFileBean().getFilePath());
                            if (mineType != null){
                                Intent intent = new Intent();
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                intent.setDataAndType(SDUtil.getFileUri(bean.getFileBean().getFilePath()),mineType);
                                List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
                                if (list.size() == 0){
                                    showToast("无法打开此类型的文件!");
                                }else {
                                    startActivity(intent);
                                }
                            }else {
                                showToast("无法打开此类型的文件!");
                            }
                            break;
                        }else {

                        }
                    case Protocol.IMAGE:
                        List<String> list = new ArrayList<>();
                        for (MessageBean messageBean : mMsgRvAdapter.getDataList()) {
                            if (messageBean.getMsgType() == Protocol.IMAGE) {
                                //获取所有的图片本地地址
                                list.add(messageBean.getImagePath());
                            }
                        }
                        String currentImagePath = mMsgRvAdapter.getDataList().get(position).getImagePath();
                        PhotoActivity.actionStart(ChatDetailActivity.this,list,list.indexOf(currentImagePath));

                        break;
                }
            }

            @Override
            public boolean onItemLongClick(int position) {
                return false;
            }
        });
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(getString(mEtInput))) {
                    MessageBean messageMean = new MessageBean();
                    messageMean.setMine(true);
                    messageMean.setMsgType(Protocol.TEXT);
                    messageMean.setText(getString(mEtInput));
                    presenter.sendMessage(messageMean, mTargetPeerIp);
                    mEtInput.setText("");
                }
            }
        });
        mIvPhotoAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_PHOTO);
            }
        });
        mIvTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ChatDetailActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatDetailActivity.this,
                            new String[]{android.Manifest.permission.CAMERA}, 2);
                } else {
                    openCamera();
                }
            }
        });
        mIvRecordVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                //若有音频正在播放，则先停止音频
                if (mPsvIsPlaying != null) {
                    mPsvIsPlaying.stopPlayer();
                    mPsvIsPlaying = null;
                    mMediaPlayerUtil.stopPlayer();
                }
                //判断权限
                if (ContextCompat.checkSelfPermission(ChatDetailActivity.this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatDetailActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO}, 3);
                } else {
                    //有权限
                    if (mEtInput.getVisibility() == View.VISIBLE) {
                        //显示 按住录音 按钮，同时隐藏输入框
                        mEtInput.setVisibility(View.INVISIBLE);
                        mBtnSend.setClickable(false);
                        mTvPressedStartRecord.setVisibility(View.VISIBLE);
                    } else {
                        mEtInput.setVisibility(View.VISIBLE);
                        mBtnSend.setClickable(true);
                        mTvPressedStartRecord.setVisibility(View.INVISIBLE);
                        mAudioRecorderUtil.cancelRecord();
                    }
                }
            }
        });
        mIvFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, FILE_MANAGER);
            }
        });
        mAudioRecorderUtil.setOnAudioStatusUpdateListener(new AudioRecorderUtil.OnAudioStatusUpdateListener() {
            @Override
            public void onUpdate(double db, long time) {
                //根据分贝值来设置录音时话筒图标的上下波动，下面有讲解
                mIvMicrophone.getDrawable().setLevel((int) (3000 + 6000 * db / 100));
                mTvRecordTime.setText(ProgressTextUtil.getProgressText(time));
            }

            @Override
            public void onStop(String filePath) {
                MessageBean messageMean = new MessageBean();
                messageMean.setMine(true);
                messageMean.setMsgType(Protocol.AUDIO);
                messageMean.setAudioPath(filePath);
                presenter.sendMessage(messageMean, mTargetPeerIp);
            }
        });
        mTvPressedStartRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mPwMicrophone.showAtLocation(mRootLayout, Gravity.CENTER, 0, 0);
                        mAudioRecorderUtil.startRecord();
                        break;
                    case MotionEvent.ACTION_UP:
                        mPwMicrophone.dismiss();
                        mAudioRecorderUtil.stopRecord();
                        break;
                }
                return false;
            }
        });
        mMediaPlayerUtil.setMediaPlayerListener(new MediaPlayerUtil.MediaPlayerListener() {
            @Override
            public void onCompletion() {
                mPsvIsPlaying.stopPlayer();
                mPsvIsPlaying = null;
            }

            @Override
            public void onError() {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    showToast("拒绝授权，无法使用相机！");
                }
                break;
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    showToast("拒绝授权，无法录音！");
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    String imagePath = SDUtil.getFilePathByUri(ChatDetailActivity.this,data.getData());
                    MessageBean messageMean = new MessageBean();
                    messageMean.setMine(true);
                    messageMean.setMsgType(Protocol.IMAGE);
                    messageMean.setImagePath(imagePath);
                    presenter.sendMessage(messageMean, mTargetPeerIp);
                }else {

                }
                break;
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    String path = mTakePhotoFile.getAbsolutePath();
                    MessageBean messageMean = new MessageBean();
                    messageMean.setMine(true);
                    messageMean.setMsgType(Protocol.IMAGE);
                    messageMean.setImagePath(path);
                    presenter.sendMessage(messageMean, mTargetPeerIp);
                } else {
                    showToast("获取拍照后的相片路径失败！");
                }
                break;
            case FILE_MANAGER:
                if (resultCode == RESULT_OK) {
                    MessageBean fileMsg = new MessageBean();
                    fileMsg.setMine(true);
                    fileMsg.setMsgType(Protocol.FILE);
                    fileMsg.setUserIp(mTargetPeerIp);//这里得设置为对方ip，否则在本窗口下 void receiveMessage(MessageBean messageBean) 会丢弃此消息
                    FileBean fileBean = new FileBean();
                    fileBean.setFilePath(SDUtil.getFilePathByUri(ChatDetailActivity.this,data.getData()));
                    fileBean.setFileName(SDUtil.getFileName(fileBean.getFilePath()));
                    fileBean.setFileSize(SDUtil.getFileByteSize(fileBean.getFilePath()));
                    fileMsg.setFileBean(fileBean);
                    presenter.sendMessage(fileMsg, mTargetPeerIp);
                } else {
                    showToast("从文件管理器获取文件失败！");
                }
                break;
        }
    }

    /**
     * 隐藏键盘
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
    /**
     * 打开相机
     */
    private void openCamera() {
        mTakePhotoFile = new File(getExternalCacheDir(), "take_photo.jpg");
        if (mTakePhotoFile.exists()) {
            mTakePhotoFile.delete();
            try {
                mTakePhotoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "openCamera: 创建File失败！");
            }
        }
        if (Build.VERSION.SDK_INT >= 24) {
            mTakePhotoUri = FileProvider.getUriForFile(ChatDetailActivity.this, "com.rdc.p2p.fileprovider", mTakePhotoFile);
        } else {
            mTakePhotoUri = Uri.fromFile(mTakePhotoFile);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mTakePhotoUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    @Override
    public void sendSuccess(MessageBean messageBean) {
        if (messageBean.getMsgType() != Protocol.FILE){
            mMsgRvAdapter.appendData(messageBean);
            mHandler.sendEmptyMessage(SCROLL);
        }
    }

    @Override
    public void sendError(String message) {
        showToast(message);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View currentFocusView = getCurrentFocus();
            if (isShouldHideInput(currentFocusView, ev)) {
                mHandler.sendEmptyMessageDelayed(HIDE_SOFT_INPUT, 100);
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return getWindow().superDispatchTouchEvent(ev) || onTouchEvent(ev);
    }

    /**
     * 点击EditText以外的地方，软键盘收起来
     *
     * @param v     获得焦点的View
     * @param event 点击事件
     * @return 是否隐藏键盘
     */
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
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
    public void receiveMessage(MessageBean messageBean) {
        if (messageBean.getUserIp().equals(mTargetPeerIp)) {
            if (messageBean.getMsgType() == Protocol.FILE){
                FileBean fileBean = messageBean.getFileBean();
                int position = mMsgRvAdapter.getPositionByFileName(fileBean.getFileName());
                if (position < 0){
                    //文件传输的第一次消息传到，要添加到列表并且自动滚动
                    mMsgRvAdapter.appendData(messageBean);
                    mHandler.sendEmptyMessage(SCROLL);
                }else {
                    if (mMsgRvAdapter.getDataList().get(position).getFileBean()
                            .getStates() == Constant.SEND_FINISH){
                        //本人传输同样的文件，要添加到列表并且自动滚动
                        mMsgRvAdapter.appendData(messageBean);
                        mHandler.sendEmptyMessage(SCROLL);
                        return;
                    }
                    //更新Adapter中的数据源
                    mMsgRvAdapter.getDataList().get(position).setFileBean(fileBean);
                    //更新RecyclerView视图
                    switch (fileBean.getStates()){
                        case Constant.RECEIVE_ING:
                            //局部更新
                            mMsgRvAdapter.notifyItemChanged(position,Constant.RECEIVE_ING);
                            break;
                        case Constant.RECEIVE_FINISH:
                            mMsgRvAdapter.notifyItemChanged(position);
                            break;

                        case Constant.SEND_ING:
                            //局部更新
                            mMsgRvAdapter.notifyItemChanged(position,Constant.SEND_ING);
                            break;
                        case Constant.SEND_FINISH:
                            mMsgRvAdapter.notifyItemChanged(position);
                            break;

                    }
                }
            }else {
                //其他消息直接添加到数据源中并更新RecyclerView界面
                mMsgRvAdapter.appendData(messageBean);
                mHandler.sendEmptyMessage(SCROLL);
            }
        }
    }
}

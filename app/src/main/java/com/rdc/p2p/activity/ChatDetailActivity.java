package com.rdc.p2p.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.ChatDetailContract;
import com.rdc.p2p.listener.OnClickRecyclerViewListener;
import com.rdc.p2p.listener.OnGlideLoadCompleted;
import com.rdc.p2p.presenter.ChatDetailPresenter;
import com.rdc.p2p.util.AudioRecorderUtil;
import com.rdc.p2p.util.MediaPlayerUtil;
import com.rdc.p2p.util.ProgressTextUtil;
import com.rdc.p2p.widget.PlayerSoundView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
    private static String mPeerName;
    private static String mPeerIp;
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
        outState.putString("peerName", mPeerName);
        outState.putString("peerIp", mPeerIp);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPeerName = savedInstanceState.getString("peerName");
        mPeerIp = savedInstanceState.getString("peerIp");
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        mAudioRecorderUtil.stopRecord();
        mMediaPlayerUtil.stopPlayer();
        mPwMicrophone.dismiss();
        super.onDestroy();
    }

    public static void actionStart(Context context, String peerIp, String peerName) {
        mPeerName = peerName;
        mPeerIp = peerIp;
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
        mTvTitle.setText(mPeerName);
        mMsgRvAdapter = new MsgRvAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRvMsgList.setLayoutManager(linearLayoutManager);
        mRvMsgList.setAdapter(mMsgRvAdapter);
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
                    case Protocol.AUDIO:

                        break;
                    case Protocol.IMAGE:
                        List<String> list = new ArrayList<>();
                        int currentPosition = -1;
                        for (MessageBean messageBean : mMsgRvAdapter.getDataList()) {
                            if (messageBean.getMsgType() == Protocol.IMAGE) {
                                //获取所有的图片本地地址
                                list.add(messageBean.getImageUrl());
                            }
                        }
                        String currentImagePath = mMsgRvAdapter.getDataList().get(position).getImageUrl();
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).equals(currentImagePath)){
                                //找到当前点击图片地址的序号
                                currentPosition = i;
                                break;
                            }
                        }
                        PhotoActivity.actionStart(ChatDetailActivity.this,list,currentPosition);
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
                    messageMean.setNickName(App.getUserBean().getNickName());
                    messageMean.setUserImageId(App.getUserBean().getUserImageId());
                    messageMean.setText(getString(mEtInput));
                    presenter.sendMessage(messageMean, mPeerIp);
                    mEtInput.setText("");
                }
            }
        });
        mIvPhotoAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ChatDetailActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatDetailActivity.this, new
                            String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
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
                messageMean.setNickName(App.getUserBean().getNickName());
                messageMean.setUserImageId(App.getUserBean().getUserImageId());
                messageMean.setAudioUrl(filePath);
                presenter.sendMessage(messageMean, mPeerIp);
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
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    showToast("拒绝授权，无法发送图片！");
                }
                break;
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

    public String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    String imagePath = handleImage(data);
                    MessageBean messageMean = new MessageBean();
                    messageMean.setMine(true);
                    messageMean.setMsgType(Protocol.IMAGE);
                    messageMean.setNickName(App.getUserBean().getNickName());
                    messageMean.setUserImageId(App.getUserBean().getUserImageId());
                    messageMean.setImageUrl(imagePath);
                    presenter.sendMessage(messageMean, mPeerIp);
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    String path = mTakePhotoFile.getAbsolutePath();
                    MessageBean messageMean = new MessageBean();
                    messageMean.setMine(true);
                    messageMean.setMsgType(Protocol.IMAGE);
                    messageMean.setNickName(App.getUserBean().getNickName());
                    messageMean.setUserImageId(App.getUserBean().getUserImageId());
                    messageMean.setImageUrl(path);
                    presenter.sendMessage(messageMean, mPeerIp);
                } else {
                    showToast("获取拍照后的相片路径失败！");
                }
                break;
            case FILE_MANAGER:
                if (resultCode == RESULT_OK) {
                    showToast(getPath(ChatDetailActivity.this,data.getData()));
                } else {
                    showToast("从文件管理器获取文件失败！");
                }
                break;
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 打开相册
     */
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
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

    /**
     * 根据相册返回的Intent解析处理，最终发送出去
     *
     * @param data
     */
    private String handleImage(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的uri，则通过document id 处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * 根据Uri通过ContentProvider获取图片路劲
     *
     * @param uri
     * @param selection
     * @return
     */
    public String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    @Override
    public void sendSuccess(MessageBean messageBean) {
        mMsgRvAdapter.appendData(messageBean);
        mHandler.sendEmptyMessage(SCROLL);
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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageBean messageBean) {
        if (messageBean.getUserIp().equals(mPeerIp)) {
            Log.d(TAG, "receiveMessage: ");
            mMsgRvAdapter.appendData(messageBean);
            mHandler.sendEmptyMessage(SCROLL);
        }
    }
}

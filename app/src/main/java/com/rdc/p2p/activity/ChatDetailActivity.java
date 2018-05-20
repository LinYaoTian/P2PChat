package com.rdc.p2p.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.rdc.p2p.presenter.ChatDetailPresenter;
import com.rdc.p2p.util.AudioRecorderUtil;
import com.rdc.p2p.util.ProgressTextUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;

public class ChatDetailActivity extends BaseActivity<ChatDetailPresenter> implements ChatDetailContract.View {

    private static final String TAG = "ChatDetailActivity";
    private static final int CHOOSE_PHOTO = 2;
    private static final int TAKE_PHOTO = 3;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.iv_photo_album_act_chat_detail)
    ImageView mIvPhotoAlbum;
    @BindView(R.id.iv_take_photo_act_chat_detail)
    ImageView mIvTakePhoto;
    @BindView(R.id.iv_file_act_chat_detail)
    ImageView mIvFile;
    @BindView(R.id.rv_msg_list_act_chat_detail)
    RecyclerView mRvMsgList;
    @BindView(R.id.btn_send_chat_detail)
    Button mBtnSend;
    @BindView(R.id.et_input_act_chat_detail)
    EditText mEtInput;
    @BindView(R.id.layout_root_act_chat_detail)
    ConstraintLayout mRootLayout;

    private MsgRvAdapter mMsgRvAdapter;
    private static String mPeerName;
    private static String mPeerIp;
    private Uri mTakePhotoUri;
    private File mTakePhotoFile;
    private AudioRecorderUtil mAudioRecorderUtil;
    private boolean start = false;
    private ImageView mIvMicrophone;
    private TextView mTvRecordTime;
    private PopupWindow mPwMicrophone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        mAudioRecorderUtil.stopRecord();
        mPwMicrophone.dismiss();
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
        mAudioRecorderUtil = new AudioRecorderUtil();
    }

    @Override
    protected void initView() {
        initToolbar();
        mTvTitle.setText(mPeerName);
        mMsgRvAdapter = new MsgRvAdapter();
        mRvMsgList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        mRvMsgList.setAdapter(mMsgRvAdapter);
        View view = View.inflate(this,R.layout.popupwindow_micorphone,null);
        mPwMicrophone = new PopupWindow(this);
        mPwMicrophone.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mPwMicrophone.setContentView(view);
        mPwMicrophone.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMicrophone.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mIvMicrophone = view.findViewById(R.id.iv_microphone_popupWindow);
        mTvRecordTime = view.findViewById(R.id.tv_record_time_popupWindow);
    }

    @Override
    protected void initListener() {
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(getString(mEtInput))){
                    MessageBean messageMean = new MessageBean();
                    messageMean.setMine(true);
                    messageMean.setMsgType(Protocol.TEXT);
                    messageMean.setNickName(App.getUserBean().getNickName());
                    messageMean.setUserImageId(App.getUserBean().getUserImageId());
                    messageMean.setText(getString(mEtInput));
                    presenter.sendMessage(messageMean,mPeerIp);
                }
            }
        });
        mIvPhotoAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ChatDetailActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ChatDetailActivity.this,new
                            String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
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
        mIvFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ChatDetailActivity.this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatDetailActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO}, 3);
                }else {
                    openRecord();
                }
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
                showToast("录音结束:"+filePath);
            }
        });

    }

    private void openRecord() {
        if (!start){
            mPwMicrophone.showAtLocation(mRootLayout,Gravity.CENTER,0,0);
            mAudioRecorderUtil.startRecord();
            start = !start;
        }else {
            mPwMicrophone.dismiss();
            mAudioRecorderUtil.stopRecord();
            start = false;
        }
    }

    private void openCamera() {
        mTakePhotoFile = new File(getExternalCacheDir(),"take_photo.jpg");
        if (mTakePhotoFile.exists()){
            mTakePhotoFile.delete();
            try {
                mTakePhotoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "openCamera: 创建File失败！");
            }
        }else {

        }
        if (Build.VERSION.SDK_INT >=24){
            mTakePhotoUri = FileProvider.getUriForFile(ChatDetailActivity.this,"com.rdc.p2p.fileprovider",mTakePhotoFile);
        }else {
            mTakePhotoUri = Uri.fromFile(mTakePhotoFile);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,mTakePhotoUri);
        startActivityForResult(intent,TAKE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    showToast("拒绝授权，无法使用相册！");
                }
                break;
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openCamera();
                }else {
                    showToast("拒绝授权，无法使用相机！");
                }
                break;
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openRecord();
                }else {
                    showToast("拒绝授权，无法打开录音！");
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK){
                    String imagePath = handleImage(data);
                    MessageBean messageMean = new MessageBean();
                    messageMean.setMine(true);
                    messageMean.setMsgType(Protocol.IMAGE);
                    messageMean.setNickName(App.getUserBean().getNickName());
                    messageMean.setUserImageId(App.getUserBean().getUserImageId());
                    messageMean.setImageUrl(imagePath);
                    presenter.sendMessage(messageMean,mPeerIp);
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK){
                    String path = mTakePhotoFile.getAbsolutePath();
                    Log.d(TAG, "onActivityResult: "+path);
                    MessageBean messageMean = new MessageBean();
                    messageMean.setMine(true);
                    messageMean.setMsgType(Protocol.IMAGE);
                    messageMean.setNickName(App.getUserBean().getNickName());
                    messageMean.setUserImageId(App.getUserBean().getUserImageId());
                    messageMean.setImageUrl(path);
                    presenter.sendMessage(messageMean,mPeerIp);
                }else {
                    showToast("获取拍照后的相片路径失败！");
                }
        }
    }

    /**
     * 打开相册
     */
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }

    /**
     * 根据相册返回的Intent解析处理，最终发送出去
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
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }
        }else if ("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(uri,null);
        }else if ("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    /**
     * 根据Uri通过ContentProvider获取图片路劲
     * @param uri
     * @param selection
     * @return
     */
    public String getImagePath(Uri uri,String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if (cursor != null){
            if (cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    @Override
    public void sendSuccess(MessageBean messageBean) {
        mMsgRvAdapter.appendData(messageBean);
        List<MessageBean> list = mMsgRvAdapter.getDataList();
        StringBuilder s= new StringBuilder();
        for (MessageBean bean : list) {
            s.append(bean.getText()).append(",");
        }
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

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageBean messageBean){
        if (messageBean.getUserIp().equals(mPeerIp)){
            Log.d(TAG, "receiveMessage: ");
            mMsgRvAdapter.appendData(messageBean);
        }
    }
}

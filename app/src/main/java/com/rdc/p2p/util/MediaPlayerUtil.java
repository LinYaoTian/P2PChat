package com.rdc.p2p.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by Lin Yaotian on 2018/5/22.
 */
public class MediaPlayerUtil implements MediaPlayer.OnPreparedListener {

    private static MediaPlayer mMediaPlayer;

    private static MediaPlayerUtil mMediaPlayerUtil;

    private MediaPlayerListener mMediaPlayerListener;

    private MediaPlayerUtil(){

    }

    public static MediaPlayerUtil getInstance(){
        if (mMediaPlayerUtil == null){
            mMediaPlayerUtil = new MediaPlayerUtil();
        }
        return mMediaPlayerUtil;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    public void setMediaPlayerListener(MediaPlayerListener mediaPlayerListener){
        this.mMediaPlayerListener = mediaPlayerListener;
    }

    public boolean isPlaying(){
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public void startPlayer(String url){
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (mMediaPlayerListener != null){
                    mMediaPlayerListener.onCompletion();
                }
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                if (mMediaPlayerListener != null){
                    mMediaPlayerListener.onError();
                }
                return false;
            }
        });
        try {
            mMediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("MediaPlayerUtil", "startPlayer: url="+url);
            return;
        }
        mMediaPlayer.prepareAsync();
    }

    public void stopPlayer(){
        if (mMediaPlayer != null){
            try {
                mMediaPlayer.stop();
            }catch (IllegalStateException e){

            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public interface MediaPlayerListener{
        void onCompletion();
        void onError();
    }

}

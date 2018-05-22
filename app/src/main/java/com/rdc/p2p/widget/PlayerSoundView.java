package com.rdc.p2p.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.rdc.p2p.R;

/**
 * Created by Lin Yaotian on 2018/5/22.
 */
public class PlayerSoundView extends View {

    private static final String TAG = "PlayerSoundView";
    public static final int DIRECTION_TO_LEFT = 0;//向左
    public static final int DIRECTION_TO_RIGHT = 1;//向右
    private int mSideLength;
    private Paint mPaint;
    private boolean isPlayer;
    private int mArcNum;//声波弧形数量（总共是3个）
    private int mDirection;// 0 向左，1 向右

    public PlayerSoundView(Context context) {
        super(context);
        initData();
        initPaint();
    }

    public PlayerSoundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initData();
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.PlayerSoundView);
        mDirection = ta.getInteger(R.styleable.PlayerSoundView_direction, DIRECTION_TO_LEFT);
        ta.recycle();
        initPaint();
    }

    public PlayerSoundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        initData();
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.PlayerSoundView);
        mDirection = ta.getInteger(R.styleable.PlayerSoundView_direction,DIRECTION_TO_LEFT);
        ta.recycle();
    }

    public PlayerSoundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMySize(30,widthMeasureSpec);
        int height = getMySize(30,heightMeasureSpec);
        //为了避免麻烦，这里保证宽高相等
        mSideLength = width < height ? width : height;
        setMeasuredDimension(mSideLength, mSideLength);
    }

    private int getMySize(int defaultSize, int measureSpec) {
        int mySize = defaultSize;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        switch (mode) {
            case MeasureSpec.UNSPECIFIED: {//如果没有指定大小，就设置为默认大小
                mySize = defaultSize;
                break;
            }
            case MeasureSpec.AT_MOST: {//如果测量模式是最大取值为size
                //我们将大小取最大值,你也可以取其他值
                mySize = size;
                break;
            }
            case MeasureSpec.EXACTLY: {//如果是固定的大小，那就不要去改变它
                mySize = size;
                break;
            }
        }
        return mySize;
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (mDirection == DIRECTION_TO_LEFT){
            mPaint.setColor(getResources().getColor(R.color.green_message_right_audio_playing));
        }else {
            mPaint.setColor(getResources().getColor(R.color.grey_message_left_audio_left));
        }
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(5);
    }

    private void initData() {
        mArcNum = 1;
        mDirection = DIRECTION_TO_RIGHT;
        isPlayer = false;
    }

    public void startPlayer() {
        isPlayer = true;
        switch (mDirection){
            case DIRECTION_TO_LEFT:
                mPaint.setColor(getResources().getColor(R.color.white));
                break;
            case DIRECTION_TO_RIGHT:
                mPaint.setColor(getResources().getColor(R.color.grey_message_left_audio_left));
                break;
            default:
        }
        postInvalidate();
    }

    public void stopPlayer(){
        isPlayer = false;
        switch (mDirection){
            case DIRECTION_TO_LEFT:
                mPaint.setColor(getResources().getColor(R.color.green_message_right_audio_playing));
                break;
            case DIRECTION_TO_RIGHT:
                mPaint.setColor(getResources().getColor(R.color.grey_message_left_audio_left));
                break;
            default:
        }
        postInvalidate();
    }

    public void setDirection(int direction){
        switch (direction){
            case DIRECTION_TO_LEFT:
                if (!isPlayer){
                    mPaint.setColor(getResources().getColor(R.color.green_message_right_audio_playing));
                }else {
                    mPaint.setColor(getResources().getColor(R.color.white));
                }
                mDirection = direction;
                postInvalidate();
                break;
            case DIRECTION_TO_RIGHT:
                mPaint.setColor(getResources().getColor(R.color.grey_message_left_audio_left));
                mDirection = direction;
                postInvalidate();
                break;
            default:
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float arcLength = (float) (Math.sqrt(2)* mSideLength /2);
        if (mDirection == DIRECTION_TO_LEFT){
            if (!isPlayer){
                drawToLeftArc(canvas,arcLength/3);
                drawToLeftArc(canvas,arcLength*2/3);
                drawToLeftArc(canvas,arcLength);
            }else {
                switch (mArcNum){
                    case 1:
                        drawToLeftArc(canvas,arcLength/3);
                        mArcNum = 2;
                        break;
                    case 2:
                        drawToLeftArc(canvas,arcLength/3);
                        drawToLeftArc(canvas,arcLength*2/3);
                        mArcNum = 3;
                        break;
                    case 3:
                        drawToLeftArc(canvas,arcLength/3);
                        drawToLeftArc(canvas,arcLength*2/3);
                        drawToLeftArc(canvas,arcLength);
                        mArcNum = 1;
                        break;
                }
                postInvalidateDelayed(400);
            }
        }else if (mDirection == DIRECTION_TO_RIGHT){
            if (!isPlayer){
                drawToRightArc(canvas,arcLength/3);
                drawToRightArc(canvas,arcLength*2/3);
                drawToRightArc(canvas,arcLength);
            }else {
                switch (mArcNum){
                    case 1:
                        drawToRightArc(canvas,arcLength/3);
                        mArcNum = 2;
                        break;
                    case 2:
                        drawToRightArc(canvas,arcLength/3);
                        drawToRightArc(canvas,arcLength*2/3);
                        mArcNum = 3;
                        break;
                    case 3:
                        drawToRightArc(canvas,arcLength/3);
                        drawToRightArc(canvas,arcLength*2/3);
                        drawToRightArc(canvas,arcLength);
                        mArcNum = 1;
                        break;
                }
                postInvalidateDelayed(400);
            }
        }
    }

    /**
     * 画向左的声波
     * @param canvas
     * @param arcLength
     */
    private void drawToLeftArc(Canvas canvas, float arcLength){
        canvas.drawArc(mSideLength-arcLength, mSideLength /2-arcLength,
                mSideLength+arcLength, mSideLength /2+arcLength,
                -220,80,false,mPaint);
    }

    /**
     * 画向右的声波
     * @param canvas
     * @param arcLength
     */
    private void drawToRightArc(Canvas canvas, float arcLength){
        canvas.drawArc(-arcLength, mSideLength /2-arcLength,
                arcLength, mSideLength /2+arcLength,
                -40,80,false,mPaint);
    }
}

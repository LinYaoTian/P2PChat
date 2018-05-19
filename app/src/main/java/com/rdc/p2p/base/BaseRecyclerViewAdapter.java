package com.rdc.p2p.base;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.rdc.p2p.listener.OnClickRecyclerViewListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;


public abstract class BaseRecyclerViewAdapter<T> extends RecyclerView.Adapter {

    protected static final int TYPE_FOOTER = 0;  //说明是带有Footer的
    protected static final int TYPE_NORMAL = 1;  //说明是不带有header和footer的
    protected static final int TYPE_NONE = 2; //没有数据

    protected List<T> mDataList = new ArrayList<>();
    private OnClickRecyclerViewListener mOnRecyclerViewListener;
    //HeaderView, FooterView
    protected View mFooterView;
    protected View mNoneView;


    //FooterView的get和set函数

    public View getFooterView() {
        return mFooterView;
    }

    public void setFooterView(View footerView) {
        mFooterView = footerView;
        notifyItemInserted(getItemCount()-1);
    }

    public View getNoneView(){
        return mNoneView;
    }

    public void setNoneView(View noneView){
        mNoneView = noneView;
    }

    //更新数据
    public void updateData(@NonNull List<T> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    //分页加载，追加数据
    public void appendData(List<T> dataList) {
        if (null != dataList && !dataList.isEmpty()) {
            int startPosition =  mDataList.size();
            mDataList.addAll(dataList);
            notifyItemRangeChanged(startPosition-1,mDataList.size()-startPosition);
        } else if (dataList != null && dataList.isEmpty()) {
//            notifyDataSetChanged();
            //空数据更新
        }
    }

    public void appendData(T t){
        mDataList.add(t);
        notifyItemRangeChanged(mDataList.size()-1,1);
    }

    public List<T> getDataList(){
        return mDataList;
    }

    /**
     * 判断item的类型，从而绑定不同的view
     */
    @Override
    public int getItemViewType(int position) {
        if (mDataList.size() == 0){
            if (mNoneView != null){
                //显示noneView
                return TYPE_NONE;
            }
        }else if (mFooterView == null){
            //普通item
            return TYPE_NORMAL;
        }else if (position == getItemCount()-1 && mFooterView != null){
            //最后一个,应该加载Footer
            return TYPE_FOOTER;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        if (mDataList.size() == 0 ){
            // 当 normal item 的数量为0时，若有 noneView，则显示 noneView
            return mNoneView != null ? 1 : 0;
        }
        return mDataList.size()+ (mFooterView == null ? 0 : 1 );
    }

    /**
     * RecyclerView不提供点击事件，自定义点击事件
     */
    public void setOnRecyclerViewListener(OnClickRecyclerViewListener onRecyclerViewListener) {
        mOnRecyclerViewListener = onRecyclerViewListener;
    }



    public abstract class BaseRvHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        public BaseRvHolder(View itemView) {
            super(itemView);
            //如果是footerView,设置点击事件，不 bind。
            if (itemView == mFooterView){
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnRecyclerViewListener != null) {
                            mOnRecyclerViewListener.onFooterViewClick();
                        }
                    }
                });
            }else if (itemView == mNoneView){

            }else {
                //这里的ButterKnife的bind要放在最后，否则报错
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }
        }

        protected abstract void bindView(T t);

        @Override
        public void onClick(View v) {
            if (mOnRecyclerViewListener != null) {
                mOnRecyclerViewListener.onItemClick(getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mOnRecyclerViewListener != null) {
                mOnRecyclerViewListener.onItemLongClick(getLayoutPosition());
                return true;
            }
            return false;
        }
    }

}


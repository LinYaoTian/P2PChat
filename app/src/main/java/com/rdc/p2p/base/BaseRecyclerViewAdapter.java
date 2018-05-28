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

    protected List<T> mDataList = new ArrayList<>();
    private OnClickRecyclerViewListener mOnRecyclerViewListener;

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
        } else if (dataList != null) {
//            notifyDataSetChanged();
            //空数据更新
        }
    }

    public void appendData(T t){
        mDataList.add(t);
        notifyItemInserted(mDataList.size()-1);
    }

    public List<T> getDataList(){
        return mDataList;
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0:mDataList.size();
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
            //这里的ButterKnife的bind要放在最后，否则报错
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

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


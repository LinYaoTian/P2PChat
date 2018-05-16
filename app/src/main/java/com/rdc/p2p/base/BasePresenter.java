package com.rdc.p2p.base;


public class BasePresenter<V> implements BaseContract.Prsenter{

    private V view;


    public void attachView(V view) {
        this.view = view;
    }


    public void detachView() {
        view = null;
    }


    public V getMvpView() {
        return view;
    }


    public boolean isAttachView() {
        return view != null;
    }
}

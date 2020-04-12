package com.example.mvp.mvp.model;

import android.util.Log;

import com.example.mvp.app.App;
import com.example.mvp.callback.IDataCallBack;
import com.example.mvp.callback.ProgressDataCallback;
import com.example.mvp.di.component.DaggerRxOperateComponent;
import com.example.mvp.mvp.model.api.ApiService;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * RxJava和OkHttp关联的封装类
 */
public class RxOperateImpl {
    @Inject
    ApiService mApiService;

    public RxOperateImpl() {
        //将apiService注入到RxOperateImpl里面
        DaggerRxOperateComponent.builder().
                appComponent(App.daggerAppComponent()).build().
                inject(this);
    }

    /**
     * @param url          get请求的URL地址
     * @param dataCallBack 结果回调(实际上是接口回调)
     * @param <T>          接口回调获取的值
     */
    public <T> void requestData(String url, IDataCallBack<T> dataCallBack) {
        RxSchedulersOperator.retryWhenOperator
                (mApiService.requestData(url)).
                subscribe(getObserver(dataCallBack));
    }


    /**
     * @param url          get请求的url地址
     * @param params       get请求的参数
     * @param dataCallBack get请求的结果回调
     * @param <T>
     */
    public <T> void requestData(String url, Map<String, T> params, IDataCallBack<T> dataCallBack) {
        if (params == null || params.size() == 0)
            requestData(url, dataCallBack);
        else
            RxSchedulersOperator.retryWhenOperator
                    (mApiService.requestData(url, params)).
                    subscribe(getObserver(dataCallBack));
    }


    /**
     * 没有参数的post请求
     *
     * @param url
     * @param dataCallBack
     * @param <T>
     */
    public <T> void requestFormData(String url, IDataCallBack<T> dataCallBack) {
        RxSchedulersOperator.retryWhenOperator(mApiService.requestFormData(url)).
                subscribe(getObserver(dataCallBack));

    }


    /**
     * 只有参数的post请求
     *
     * @param url
     * @param params
     * @param dataCallBack
     * @param <T>
     */
    public <T> void requestFormData(String url, Map<String, T> params, IDataCallBack<T> dataCallBack) {
        if (params == null || params.size() == 0)
            requestFormData(url, dataCallBack);
        else
            RxSchedulersOperator.retryWhenOperator(mApiService.requestFormData(url, params)).
                    subscribe(getObserver(dataCallBack));
    }


    /**
     * 只有请求头的post请求
     *
     * @param url
     * @param headers
     * @param dataCallBack
     * @param <T>
     */
    public <T> void requestFormData(String url, HashMap<String, T> headers, IDataCallBack<T> dataCallBack) {
        if (headers == null || headers.size() == 0)
            requestData(url, dataCallBack);
        else
            RxSchedulersOperator.retryWhenOperator(
                    mApiService.requestFormData(url, headers)).
                    subscribe(getObserver(dataCallBack));
    }


    /**
     * 有请求头并且有参数的post请求
     *
     * @param url
     * @param headers
     * @param params
     * @param dataCallBack
     * @param <T>
     */
    public <T> void requestFormData(String url, Map<String, T> headers, Map<String, T> params, IDataCallBack<T> dataCallBack) {
        if (headers == null || headers.size() == 0)  //请求头为空 但是参数不为空
            requestFormData(url, params, dataCallBack);
        else if (params == null || params.size() == 0)  //TODO参数为空但是请求头不为空
            requestFormData(url, headers, dataCallBack);
        else if ((headers == null || headers.size() == 0) && // 请求头和参数都为空
                (params == null || params.size() == 0))
            requestFormData(url, dataCallBack);
        else
            //请求头和参数都不为空
            RxSchedulersOperator.retryWhenOperator(mApiService.requestFormData(url, headers, params)).
                    subscribe(getObserver(dataCallBack));
    }

    /**
     * 带JSON串的没有请求头的post请求
     *
     * @param url
     * @param jsonStr
     * @param dataCallBack
     * @param <T>
     */
    public <T> void requestFormData(String url, String jsonStr, IDataCallBack<T> dataCallBack) {
        if (jsonStr.isEmpty())
            return;
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStr);
        RxSchedulersOperator.retryWhenOperator
                (mApiService.requestFormData(url, requestBody)).
                subscribe(getObserver(dataCallBack));
    }


    /**
     * 带请求头的上传json串的post请求
     *
     * @param url
     * @param haders       请求头
     * @param jsonStr      要上传的json串
     * @param dataCallBack 结果回调
     * @param <T>
     */
    public <T> void requestFormData(String url, Map<String, T> haders, String jsonStr, IDataCallBack<T> dataCallBack) {
        if (haders == null || haders.size() == 0)
            requestFormData(url, jsonStr, dataCallBack);
        else { //有请求头并且上传json串的post请求
            if (jsonStr.isEmpty())
                return;
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStr);
            RxSchedulersOperator.retryWhenOperator
                    (mApiService.requestFormData(url, haders, requestBody)).
                    subscribe(getObserver(dataCallBack));
        }
    }


    /**
     * @param url          下载的文件路径
     * @param progressDataCallback 下载结果回调
     * @param isProcess    是否带进度
     * @param <T>
     */
    public <T> void downloadFile(String url, boolean isProcess,
                                 ProgressDataCallback<T> progressDataCallback) {
        if (isProcess) {
            //TODO 需封装带进度的RxJava+Retrofit代码  作业

        } else
            RxSchedulersOperator.retryWhenOperator(mApiService.downloadFile(url)).
                    subscribe(getObserver(progressDataCallback));
    }

    //单个文件上传




    //单个文件+参数上传

    /**
     * 抽取出结果回调的方法
     *
     * @param dataCallBack
     * @param <T>
     * @return
     */
    private <T> RxObserver<T> getObserver(IDataCallBack<T> dataCallBack) {
        return new RxObserver<T>(dataCallBack) {
            @Override
            public void onSubscribe(Disposable d) {
                if (dataCallBack != null)
                    dataCallBack.onResponseDisposable(d);

            }

            @Override
            public void onNext(T t) {
                if (dataCallBack != null)
                    dataCallBack.onStateSucess(t);
            }
        };
    }

}

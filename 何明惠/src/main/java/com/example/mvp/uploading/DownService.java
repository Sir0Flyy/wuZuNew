package com.example.mvp.uploading;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;

public interface DownService {
    String url="http://cdn.banmi.com/banmiapp/apk/banmi_330.apk";
    String baseUrl = "http://cdn.banmi.com/";

    @GET("banmiapp/apk/banmi_330.apk")
    Observable<ResponseBody> downLoadFile();
}

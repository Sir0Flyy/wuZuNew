package com.example.mvp.mvp.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.mvp.R;
import com.example.mvp.base.BaseActivity;
import com.example.mvp.uploading.DownService;
import com.example.mvp.mvp.ui.adapter.HomeVpAdapter;
import com.example.mvp.mvp.ui.fragment.HomeFragmet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

public class HomeActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.home_viewpager)
    ViewPager mViewPager;
    @BindView(R.id.home_navigation)
    BottomNavigationView mBottomNv;
    private ViewPager mHomeViewpager;
    private BottomNavigationView mHomeNavigation;


    @Override
    protected void initListenner() {
        mBottomNv.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.item_home:
                    Log.e("TAG", "00000000");
                    switchTab(0);
                    break;
                case R.id.item_navigation:
                    Log.e("TAG", "000000011111111110");
                    switchTab(1);
                    break;
                case R.id.item_tixi:
                    Log.e("TAG", "22222");
                    switchTab(2);
                    break;
                case R.id.item_gongzhonghao:
                    switchTab(3);
                    break;
            }
            return true;
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //当ViewPager页面切换的时候让下面的tab标签跟着切换
                mBottomNv.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void switchTab(int i) {
        mViewPager.setCurrentItem(i);
    }

    @Override
    protected void onViewCreated() {
        List<HomeFragmet> fragments = getHomeFragmets();
        HomeVpAdapter adapter = new HomeVpAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(adapter);
    }

    private List<HomeFragmet> getHomeFragmets() {
        List<HomeFragmet> fragments = new ArrayList<HomeFragmet>();
        for (int i = 0; i < 4; i++) {
            HomeFragmet homeFragmet = new HomeFragmet(i);
            fragments.add(homeFragmet);
        }
        return fragments;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }


    private static final String TAG = "MainActivity";
    private Button mBtnDownLoadOk;
    private Button mBtnDownLoadRetrofit;
    public static final int UNKNOWN_CODE = 2019;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {

        mBtnDownLoadRetrofit = (Button) findViewById(R.id.btn_downLoad_retrofit);
        mBtnDownLoadRetrofit.setOnClickListener(this);
        mHomeViewpager = (ViewPager) findViewById(R.id.home_viewpager);
        mHomeNavigation = (BottomNavigationView) findViewById(R.id.home_navigation);
    }


    private void downLoadRetrofit() {
        //retrofit
        Retrofit retrofit = new Retrofit.Builder().baseUrl(DownService.baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        //接口对象
        DownService downService = retrofit.create(DownService.class);
        //被观察者
        Observable<ResponseBody> observable = downService.downLoadFile();
        //执行
        observable.subscribeOn(Schedulers.io())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        InputStream inputStream = responseBody.byteStream();
                        long length = responseBody.contentLength();
                        saveFile(inputStream, Environment.getExternalStorageDirectory() + "/r.apk", length);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError:" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private void saveFile(InputStream inputStream, final String apkPath, Long maxLength) {
        long count = 0;
        byte[] bytes = new byte[1024 * 10];
        int length = -1;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(apkPath));
            while ((length = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, length);
                count += length;
                Log.e(TAG, "下载进度：" + count + "/" + maxLength);
            }
            inputStream.close();
            fileOutputStream.close();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(HomeActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
                    installApk(HomeActivity.this, apkPath);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void installApk(Context context, String path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startInstallO(context, path);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startInstallN(context, path);
        } else {
            startInstall(context, path);
        }
    }

    private static void startInstall(Context context, String path) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(install);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static void startInstallN(Context context, String path) {
        //参数1 上下文, 参数2 在AndroidManifest中的android:authorities值, 参数3  共享的文件
        Uri apkUri = FileProvider.getUriForFile(context, "com.baidu.download.provider", new File(path));
        Intent install = new Intent(Intent.ACTION_VIEW);
        //由于没有在Activity环境下启动Activity,设置下面的标签
        install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //添加这一句表示对目标应用临时授权该Uri所代表的文件
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        install.setDataAndType(apkUri, "application/vnd.android.package-archive");
        context.startActivity(install);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void startInstallO(final Context context, String path) {
        boolean isGranted = context.getPackageManager().canRequestPackageInstalls();
        if (isGranted) startInstallN(context, path);//安装应用的逻辑(写自己的就可以)
        else new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("安装应用需要打开未知来源权限，请去设置中开启权限")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                        Activity act = (Activity) context;
                        act.startActivityForResult(intent, UNKNOWN_CODE);
                    }
                })
                .show();
    }

    //授权 可以安装apk的权限后，回调此方法，进行安装
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UNKNOWN_CODE) {
            installApk(HomeActivity.this, Environment.getExternalStorageDirectory() + "/a.apk");//再次执行安装流程，包含权限判等

        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_downLoad_retrofit:
                downLoadRetrofit();
                break;
        }
    }
}

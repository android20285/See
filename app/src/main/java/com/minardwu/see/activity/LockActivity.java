package com.minardwu.see.activity;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.minardwu.see.R;
import com.minardwu.see.base.ActivityController;
import com.minardwu.see.base.Config;
import com.minardwu.see.base.MyApplication;
import com.minardwu.see.entity.Photo;
import com.minardwu.see.event.GetShowPhotoEvent;
import com.minardwu.see.event.NewPhotoEvent;
import com.minardwu.see.event.ResultCodeEvent;
import com.minardwu.see.util.FontUtil;
import com.minardwu.see.widget.UnderView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

public class LockActivity extends AppCompatActivity {

    private View view;
    private TextView tv_time;
    private TextView tv_date;
    private TextView tv_info;
    private SimpleDraweeView simpleDraweeView;
    private UnderView underView;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        EventBus.getDefault().register(this);

        view = findViewById(R.id.ll);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_info = (TextView) findViewById(R.id.tv_info);
        simpleDraweeView = (SimpleDraweeView) findViewById(R.id.iv_lock);

        tv_time.setTypeface(FontUtil.getSegoeUISemilight());

        for(Photo tempPhoto:Config.yourPhotos){
            if(tempPhoto.getState()==1){
                simpleDraweeView.setImageURI(Uri.parse(tempPhoto.getPhotoUrl()));
                if(!tempPhoto.getPhotoInfo().equals("empty")){
                    tv_info.setText(tempPhoto.getPhotoInfo());
                }
            }
        }

        underView =  (UnderView) findViewById(R.id.underview);
        underView.setView(view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide navbar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);


        handler = new Handler();
        handler.post(updateRunnable);

    }

    Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm-MM月dd日 E", Locale.CHINESE);
            String date[] = simpleDateFormat.format(new Date()).split("-");
            tv_time.setText(date[0]);
            tv_date.setText(date[1]);
            handler.postDelayed(updateRunnable,10000);
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int key = event.getKeyCode();
        switch (key) {
            case KeyEvent.KEYCODE_BACK:
                return true;
            case KeyEvent.KEYCODE_MENU:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultCodeEvent(ResultCodeEvent event) {
        if(event.getResult()==1){
            finish();
        }
    };

    //好友发布新的照片后，在屏幕暗的时候也能刷新壁纸（不能只在onCreat设置UI）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewPhotoEvent(NewPhotoEvent event){
        if(event.getResult()==1){
            for(Photo tempPhoto:Config.yourPhotos){
                if(tempPhoto.getState()==1){
                    simpleDraweeView.setImageURI(Uri.parse(tempPhoto.getPhotoUrl()));
                    if(!tempPhoto.getPhotoInfo().equals("empty")){
                        tv_info.setText(tempPhoto.getPhotoInfo());
                    }
                }
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityController.lockActivityIsActive = false;
        EventBus.getDefault().unregister(this);
        handler.removeCallbacks(updateRunnable);
    }
}



package com.bupt.iheath.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;

import com.bupt.iheath.MyApplication;
import com.bupt.iheath.R;
import com.bupt.iheath.model.AccountInfo;
import com.bupt.iheath.ui.base.BaseActivity;
import com.bupt.iheath.utils.Constants;

public class SplashActivity extends BaseActivity {

    private static long DELAY_TIME = 2000L ;
    private boolean is_supervisor = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    is_supervisor = (boolean) MyApplication.sCache.getAsObject(Constants.IS_SUPERVISOR);
                }catch (Exception e) {
                    is_supervisor = false ;
                }

                AccountInfo accountInfo = MyApplication.getInstance().getCurrentUser();
                if (accountInfo == null) {
                    gotoActivity(SplashActivity.this, LoginActivity.class, null);
                } else if (is_supervisor) {
                    gotoActivity(SplashActivity.this, MonitorActivity.class, null);
                } else {
                    gotoActivity(SplashActivity.this, MainActivity.class, null);
                }
                //gotoActivity(SplashActivity.this, Analyze2Activity.class, null);
                finish();
            }
        },DELAY_TIME) ;
    }

}

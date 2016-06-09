package com.bupt.iheath.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bupt.iheath.MyApplication;
import com.bupt.iheath.R;
import com.bupt.iheath.model.AccountInfo;
import com.bupt.iheath.model.UserState;
import com.bupt.iheath.ui.base.BaseActivity;
import com.bupt.iheath.utils.Constants;
import com.bupt.iheath.utils.NotifyUtils;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

public class MonitorActivity extends BaseActivity {

    private SwipeRefreshLayout refreshLayout;

    private static final long BACK_PRESS_EXIT_TIME_INTERVAL = 2000L;
    private long mFirstTime; //第一次按下返回键的时间
    private Handler handler = new Handler() ;
    private Runnable mAutoRefreshTask = new Runnable() {
        @Override
        public void run() {
            // TODO: 2016/5/3 添加自动刷新逻辑
            refreshContent(true) ;
            handler.postDelayed(this, 2000*60) ;
        }
    } ;

    private TextView mHeartRateTxt ;
    private TextView mStateTxt ;
    private TextView mWhenTxt ;
    private TextView mStepTxt ;
    private Button mLogoutBtn ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHeartRateTxt = (TextView) findViewById(R.id.tv_heart_rate);
        mStateTxt = (TextView) findViewById(R.id.tv_state);
        mWhenTxt = (TextView) findViewById(R.id.tv_when);
        mStepTxt = (TextView) findViewById(R.id.tv_step);
        mLogoutBtn = (Button) findViewById(R.id.btn_log_out);
        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.sCache.put(Constants.IS_SUPERVISOR, false);
                MyApplication.getInstance().getCurrentUser().logOut(MonitorActivity.this);
                gotoActivity(MonitorActivity.this, LoginActivity.class, null);
                finish();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshContent(false);
            }
        });
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_fresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent(false) ;
            }
        });

        handler.postDelayed(mAutoRefreshTask, 1000) ;
    }

    @Override
    public void onBackPressed() {

        if (mFirstTime + BACK_PRESS_EXIT_TIME_INTERVAL > System.currentTimeMillis()) {
            MyApplication.getInstance().exit();
            super.onBackPressed();
        } else {
            NotifyUtils.showHints(getString(R.string.main_activity_exit_hint));
        }
        mFirstTime = System.currentTimeMillis();
    }

    private void refreshContent(boolean isAutoRefresh) {

        AccountInfo accountInfo = MyApplication.getInstance().getCurrentUser();
        BmobQuery<UserState> query = new BmobQuery<>();
        query.addWhereEqualTo("email", accountInfo.getEmail());
        query.setLimit(1);
        //按时间降序
        query.order("-createdAt");
        // start refresh
        if (isAutoRefresh) {
            refreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(true);
                }
            }) ;
        }
        query.findObjects(this, new FindListener<UserState>() {
            @Override
            public void onSuccess(List<UserState> object) {
                // stop refresh
                refreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }) ;
                if (object != null && object.size() ==0) {
                    NotifyUtils.showHints("暂无数据....");
                }
                for (UserState userState : object) {
                    mStepTxt.setText("步数: " + userState.getStep());
                    mWhenTxt.setText("时间: " + userState.getWhen().toString());
                    int s = userState.getState() ;
                    String state ="正常";
                    if (s==2) {
                        state = "危险" ;
                    } else if (s==1) {
                        state = "警告" ;
                    }
                    mStateTxt.setText("状态: " + state);
                    mHeartRateTxt.setText("心率: " + userState.getHeartRate()+" 次/分钟");
                }
            }

            @Override
            public void onError(int code, String msg) {
                // stop refresh
                refreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }) ;
            }
        });
    }
}

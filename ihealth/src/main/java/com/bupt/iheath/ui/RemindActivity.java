package com.bupt.iheath.ui;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.bupt.iheath.MyApplication;
import com.bupt.iheath.R;
import com.bupt.iheath.model.AccountInfo;
import com.bupt.iheath.model.AlarmInfo;
import com.bupt.iheath.ui.base.BaseActivity;
import com.bupt.iheath.utils.ClockManager;
import com.bupt.iheath.utils.NotifyUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class RemindActivity extends BaseActivity {

    private final static int REQUEST_ADD_REMIND = 100;
    private RecyclerView mRecyclerView;
    private FloatingActionButton fab;
    private RemindEventAdapter mEventAdapter;
    private static List<AlarmInfo> mAlarmList = new ArrayList<>();
    private static String[] items = new String[]{"删除"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remind);
        setupActionBar();

        mRecyclerView = (RecyclerView) findViewById(R.id.remind_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mEventAdapter = new RemindEventAdapter(mAlarmList, mOnAlarmItemClick);
        mRecyclerView.setAdapter(mEventAdapter);
        mRecyclerView.setOnScrollListener(mOnScrollListener);
        fab = (FloatingActionButton) findViewById(R.id.fab_remind);
        fab.setOnClickListener(mOnFABClickListener);
        if (mAlarmList.isEmpty()) {
            getRemindEvents() ;
        }
    }

    private void getRemindEvents() {
        AccountInfo accountInfo = MyApplication.getInstance().getCurrentUser();
        BmobQuery<AlarmInfo> query = new BmobQuery<>();
        query.addWhereEqualTo("email", accountInfo.getEmail());
        query.setLimit(50);
        query.findObjects(this, new FindListener<AlarmInfo>() {
            @Override
            public void onSuccess(List<AlarmInfo> object) {
                for (AlarmInfo alarmInfo : object) {
                    mEventAdapter.addItem(alarmInfo);
                    MyApplication.sClockManager.addAlarm(alarmInfo);
                }
            }

            @Override
            public void onError(int code, String msg) {
            }
        });
    }

    private void addEvent(final AlarmInfo event) {
        event.save(this, new SaveListener() {

            @Override
            public void onSuccess() {
                mEventAdapter.addItem(event);
                MyApplication.sClockManager.addAlarm(event);
                NotifyUtils.showHints("添加成功！");
            }

            @Override
            public void onFailure(int code, String msg) {
                NotifyUtils.showHints("添加失败！请联网后重试...");
            }
        });
    }

    private void removeEvent(final AlarmInfo event) {
        AlarmInfo info = new AlarmInfo(0,"name","comment");
        info.setObjectId(event.getObjectId());
        info.delete(this, new DeleteListener() {
            @Override
            public void onSuccess() {
                mEventAdapter.removeItem(event);
                MyApplication.sClockManager.removeAlarm(event);
                NotifyUtils.showHints("删除成功！");
            }

            @Override
            public void onFailure(int i, String s) {
                NotifyUtils.showHints("删除失败！联网后请重试...");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_ADD_REMIND) && (resultCode == RESULT_OK)) {
            String name = data.getStringExtra("name");
            String time = data.getStringExtra("time");
            String comment = data.getStringExtra("comment");
            int hour = data.getIntExtra("hour", 0);
            int minute = data.getIntExtra("minute", 0);
            //NotifyUtils.showHints("添加成功！");
            Calendar calendar = ClockManager.getCalendarByHourMinute(hour, minute);
            AlarmInfo alarmInfo = new AlarmInfo(calendar, name, comment);
            addEvent(alarmInfo);
        }
    }

    private OnItemLongClickListener mOnAlarmItemClick = new OnItemLongClickListener() {
        @Override
        public void onLongClick(AlarmInfo item, int index) {
            showDialog(item, index);
        }
    };

    private void showDialog(final AlarmInfo alarmInfo, final int index) {
        new AlertDialog.Builder(this)
                .setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                removeEvent(alarmInfo);
                                break;
                            default:
                                break;
                        }
                    }
                })
                .show();

    }

    private View.OnClickListener mOnFABClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(RemindActivity.this, AddRemindEventActivity.class);
            startActivityForResult(intent, REQUEST_ADD_REMIND);
        }
    };

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        private int mScrollThreshold = 4;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            boolean isSignificantDelta = Math.abs(dy) > mScrollThreshold;
            if (isSignificantDelta) {
                if (dy > 0) {
                    //onScrollUp();
                    fab.hide();
                } else {
                    //onScrollDown();
                    fab.show();
                }
            }
        }
    };

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 显示返回箭头
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public interface OnItemLongClickListener {
        void onLongClick(AlarmInfo item, int index);
    }
}

package com.bupt.iheath.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.bupt.iheath.R;
import com.bupt.iheath.model.AlarmInfo;
import com.bupt.iheath.ui.base.BaseActivity;
import com.bupt.iheath.utils.AlarmReceiver;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Calendar;

public class AddRemindEventActivity extends BaseActivity {

    private MaterialEditText mEventName ;
    private MaterialEditText mEventTime ;
    private MaterialEditText mEventComment ;
    private Button mEventAdd ;
    private int mHourOfDay = 0 ;
    private int mMinute = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_remind_event);
        initView() ;
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

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 显示返回箭头
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mEventName = (MaterialEditText) findViewById(R.id.et_event_name);
        mEventComment = (MaterialEditText) findViewById(R.id.et_event_comment);
        mEventTime = (MaterialEditText) findViewById(R.id.et_event_time);
        mEventTime.setOnClickListener(mEtTimeOnClickListener);
        mEventAdd = (Button) findViewById(R.id.btn_event_add);
        mEventAdd.setOnClickListener(mBtnOnClickListener);
    }

    private View.OnClickListener mEtTimeOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            pickTime();
        }
    };

    private View.OnClickListener mBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doAdd() ;
        }
    };

    private void doAdd() {
        String comment = mEventComment.getText().toString() ;
        String time = mEventTime.getText().toString() ;
        String name = mEventName.getText().toString() ;
        if (TextUtils.isEmpty(comment)) {
            mEventComment.setError("备注不能为空");
            mEventComment.requestFocus();
            return ;
        }
        if (TextUtils.isEmpty(time)) {
            mEventTime.setError("时间不能为空");
            mEventTime.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(name)) {
            mEventName.setError("事件不能为空");
            mEventName.requestFocus();
            return;
        }
        if (mMinute ==0 || mHourOfDay == 0) {
            return;
        }
        Intent intent = new Intent() ;
        intent.putExtra("name",name) ;
        intent.putExtra("comment",comment) ;
        intent.putExtra("time",time) ;
        intent.putExtra("hour", mHourOfDay) ;
        intent.putExtra("minute", mMinute) ;
        setResult(RESULT_OK, intent);

        finish();
    }

    private void pickTime() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timeDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String time = AlarmInfo.format(hourOfDay,minute);
                mEventTime.setText(time);

                mMinute = minute ;
                mHourOfDay = hourOfDay ;
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timeDialog.show();
    }
}

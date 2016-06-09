package com.bupt.iheath.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.bupt.iheath.model.AlarmInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by jumper on 2016/4/16.
 */
public class ClockManager {
    private Context mContext ;
    private AlarmManager mAlarmManager ;
    private List<AlarmInfo> mAlarmList = new ArrayList<>();

    public ClockManager(Context context) {
        mAlarmList.clear();
        mContext = context ;
        mAlarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
    }

    public static Calendar getCalendarByHourMinute(int hourofDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,hourofDay);
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        Calendar currentTime = Calendar.getInstance();
        if(calendar.getTimeInMillis() <= currentTime.getTimeInMillis()){
            calendar.setTimeInMillis(calendar.getTimeInMillis()+24*60*60*1000);
        }
        return calendar ;
    }

    public void addAlarm(AlarmInfo alarmInfo) {
        startAlarm(alarmInfo) ;
        mAlarmList.add(alarmInfo) ;
    }

    public void removeAlarm(AlarmInfo alarmInfo) {
        cancelAlarm(alarmInfo) ;
        mAlarmList.remove(alarmInfo) ;
    }

    public void removeAllAlarm() {
        for(AlarmInfo info : mAlarmList) {
            removeAlarm(info) ;
        }
        mAlarmList.clear();
    }

    private void startAlarm(AlarmInfo alarmInfo){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(alarmInfo.getTime());

        Calendar currentTime = Calendar.getInstance();

        if(calendar.getTimeInMillis() <= currentTime.getTimeInMillis()){
            calendar.setTimeInMillis(calendar.getTimeInMillis()+24*60*60*1000);
        }

        Intent intent = new Intent(mContext,AlarmReceiver.class) ;
        intent.putExtra("alarm_info",alarmInfo);
        PendingIntent sender = PendingIntent.getBroadcast(mContext,
                alarmInfo.getId(),
                intent,
                0) ;
        mAlarmManager.setExact(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                sender);
    }

    public void cancelAlarm(AlarmInfo ad) {
        Intent intent = new Intent(mContext,AlarmReceiver.class) ;
        PendingIntent sender = PendingIntent.getBroadcast(mContext,
                ad.getId(),
                intent,
                0) ;
        mAlarmManager.cancel(sender);
    }
}

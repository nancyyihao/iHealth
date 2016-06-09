package com.bupt.iheath.model;


import com.bupt.iheath.MyApplication;

import java.util.Calendar;
import java.util.Date;

import cn.bmob.v3.BmobObject;

/**
 * Created by jumper on 2016/4/15.
 */
public class AlarmInfo extends BmobObject {
    public  String name;
    public  String comment;
    public String timeLabel ;
    private String email ;
    private Calendar date;
    private long time = 0;

    public AlarmInfo(long time, String name, String comment){
        this.time = time;
        date = Calendar.getInstance();
        date.setTimeInMillis(time);
        timeLabel = format(date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE));
        email = MyApplication.getInstance().getCurrentUser().getEmail();
        this.name = name ;
        this.comment = comment ;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public AlarmInfo(Calendar c, String name, String comment){
        date = c;
        time = c.getTimeInMillis();
        timeLabel = format(date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE));
        email = MyApplication.getInstance().getCurrentUser().getEmail();
        this.name = name ;
        this.comment = comment ;
    }

    public long getTime(){
        return time;
    }

    public String getTimeLabel(){
        return timeLabel;
    }

    public static String format(int hourOfDay, int minute) {
        String hours;
        String minutes;
        if (hourOfDay < 10) {
            hours = "0" + String.valueOf(hourOfDay);
        } else {
            hours = String.valueOf(hourOfDay);
        }
        if (minute < 10) {
            minutes = "0" + String.valueOf(minute);
        } else {
            minutes = String.valueOf(minute);
        }
        return hours + ":" + minutes;
    }

    public int getId(){
        return (int)(getTime()/1000/60);
    }

    @Override
    public String toString(){
        return getTimeLabel();
    }

}

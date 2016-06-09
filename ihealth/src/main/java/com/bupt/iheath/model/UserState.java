package com.bupt.iheath.model;

import com.bupt.iheath.MyApplication;

import java.util.Date;

import cn.bmob.v3.BmobObject;

/**
 * Created by jumper on 2016/4/30.
 */
public class UserState extends BmobObject {
    private String when ;
    private Integer heartRate ;
    private Integer step ;
    private Integer state ;
    private String email ;
    public UserState() {
        AccountInfo accountInfo = MyApplication.getInstance().getCurrentUser() ;
        email = accountInfo.getEmail() ;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

}

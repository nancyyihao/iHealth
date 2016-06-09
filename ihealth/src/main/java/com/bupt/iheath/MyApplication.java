package com.bupt.iheath;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;

import com.bupt.iheath.model.AccountInfo;
import com.bupt.iheath.network.UploadManager;
import com.bupt.iheath.ui.MainActivity;
import com.bupt.iheath.utils.ACache;
import com.bupt.iheath.utils.ClockManager;
import com.miband.sdk.MiBand;

import java.util.ArrayList;
import java.util.prefs.Preferences;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;

/**
 * Created by jumper on 2016/3/13.
 */
public class MyApplication extends Application {

    private ArrayList<Activity> mActivityList = new ArrayList<Activity>();

    public static ACache sCache = null ;
    public static int sCurrentState = 0 ;
    public static int sCurrentStep = 0 ;
    public static int sCurrentHeart = 80 ;
    public static ClockManager sClockManager = null ;

    private static MyApplication sInstance;

    public static MyApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this ;
        initBmob();
        sCache = ACache.get(this) ;
        sCache.put("upload_interval", 0);
        sCache.put("emergency_cell_phone", "18612345678") ;
        sCache.put("auto_call_switch",true);
        sClockManager = new ClockManager(this) ;
    }

    public AccountInfo getCurrentUser() {

        return BmobUser.getCurrentUser(this, AccountInfo.class);
    }

    public void addActivity(Activity activity) {
        mActivityList.add(activity);
    }

    public void deleteActivity(Activity activity) {
        mActivityList.remove(activity);
    }

    public void exit() {
        UploadManager.getInstance().stopUpload();
        MainActivity.sMiband.turnOffBluetooth();
        deleteAllActivity();
    }

    public void deleteAllActivity() {
        for(Activity activity : mActivityList) {
            if (activity!=null && !activity.isFinishing()) {
                activity.finish();
            }
            activity = null ;
        }
        mActivityList.clear();
    }

    private void initBmob() {
        Bmob.initialize(this,"91d84c25d5f416573e9dfd881efbe79d");
    }
}

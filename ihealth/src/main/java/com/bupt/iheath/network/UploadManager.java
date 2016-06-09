package com.bupt.iheath.network;

import android.os.Handler;

import com.bupt.iheath.MyApplication;
import com.bupt.iheath.model.AccountInfo;
import com.bupt.iheath.model.UserState;
import com.bupt.iheath.utils.Constants;
import com.bupt.iheath.utils.NotifyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by jumper on 2016/4/11.
 */
public class UploadManager {

    private static int interval = 1000*60*1000 ;
    private Handler mHandler = new Handler() ;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            NotifyUtils.showHints("开始上传数据...");

            UserState userState = new UserState() ;
            userState.setState(MyApplication.sCurrentState);

            Date currentTime = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(currentTime);

            userState.setWhen(dateString);
            userState.setHeartRate(MyApplication.sCurrentHeart);
            userState.setStep(MyApplication.sCurrentStep);
            userState.save(MyApplication.getInstance(), new SaveListener() {
                @Override
                public void onSuccess() {
                    NotifyUtils.showHints("上传数据成功!");
                }

                @Override
                public void onFailure(int i, String s) {
                    NotifyUtils.showHints("上传数据失败!" + "原因："+s);
                }
            });

            mHandler.postDelayed(this, interval) ;
        }
    } ;

    private static class Nest {
        private final static UploadManager instance = new UploadManager() ;
    }

    public static UploadManager getInstance() {
        return Nest.instance ;
    }
//
//    public void upload(AccountInfo accountInfo, Context  context) {
//        accountInfo.update(context, new UpdateListener() {
//            @Override
//            public void onSuccess() {
//                // TODO 添加上传成功提示
//            }
//
//            @Override
//            public void onFailure(int code, String msg) {
//
//            }
//        });
//    }

    public void startUpload() {
       String s = MyApplication.sCache.getAsString(Constants.UPLOAD_INTERVAL) ;
        int tempInterval ;
        if (s.equals("5")) {  // five minute
            tempInterval = 5*60*1000 ;
        } else if (s.equals("3")) { // two minute
            tempInterval = 3*60*1000 ;
        } else if (s.equals("2")) {  // one minute
            tempInterval = 2*60*1000 ;
        } else {
            tempInterval = 0 ;
        }
        if (tempInterval == 0) {
            stopUpload();
            return;
        } else {
            interval = tempInterval ;
        }
        mHandler.postDelayed(mRunnable, interval) ;
    }

    public void stopUpload() {
        mHandler.removeCallbacks(mRunnable);
    }
}

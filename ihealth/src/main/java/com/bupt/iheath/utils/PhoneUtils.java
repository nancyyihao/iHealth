package com.bupt.iheath.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.bupt.iheath.MyApplication;

import java.util.ArrayList;

/**
 * Created by jumper on 2016/4/1.
 */
public class PhoneUtils {
    /**
     * 发短信
     * @param phoneNumber 电话号码
     * @param content 内容
     */
    public static void sendSms(String phoneNumber, String content) {
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> texts = smsManager.divideMessage(content);
        for (String text : texts) {
            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    text,
                    null,
                    null);
        }
        NotifyUtils.showHints("短信发送成功!");
    }

    /**
     * 直接打电话
     * @param phoneNumber 电话号码
     */
    public static void call(String phoneNumber) {
        Context context = MyApplication.getInstance().getApplicationContext();
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String myPhoneNumber = tm.getLine1Number() ;
        if (myPhoneNumber.equals(phoneNumber)) {
            return;
        }
        int state = tm.getCallState();
        if (state == TelephonyManager.CALL_STATE_IDLE) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            context.startActivity(intent);
        }

    }
}

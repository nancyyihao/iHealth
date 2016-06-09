package com.bupt.iheath.utils;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.bupt.iheath.model.AlarmInfo;
import com.bupt.iheath.ui.MainActivity;

/**
 * Created by nomasp on 2015/10/07.
 */
public class AlarmReceiver extends BroadcastReceiver{

    private static final String TAG = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("闹钟执行了！");

        //Toast.makeText(context, "闹钟执行了！", Toast.LENGTH_LONG).show();
        Log.e(TAG,"闹钟执行了 ");

        AlarmInfo alarmInfo = (AlarmInfo) intent.getSerializableExtra("alarm_info");
        String name = alarmInfo.getName();
        String comment = alarmInfo.getComment();
        //Toast.makeText(context, name+comment, Toast.LENGTH_LONG).show();
        NotifyUtils.vibrate();

        Dialog alertDialog = new AlertDialog.Builder(context).
                setTitle(name).
                setMessage(comment).
                setPositiveButton("确定", null)
                .create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();

    }
}

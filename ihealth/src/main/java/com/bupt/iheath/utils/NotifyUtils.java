package com.bupt.iheath.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.bupt.iheath.MyApplication;
import com.bupt.iheath.ui.MainActivity;
import com.miband.sdk.model.VibrationMode;

/**
 * Created by jumper on 2016/4/1.
 */
public class NotifyUtils {
    public static void showHints(String hints) {

        Context context = MyApplication.getInstance().getApplicationContext() ;
        Toast toast = Toast.makeText(context,
                hints, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void vibrate() {
        MainActivity.sMiband.startVibration(VibrationMode.VIBRATION_10_TIMES_WITH_LED);
    }
}

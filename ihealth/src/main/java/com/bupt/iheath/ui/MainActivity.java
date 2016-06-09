package com.bupt.iheath.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.bupt.iheath.MyApplication;
import com.bupt.iheath.R;
import com.bupt.iheath.model.AccountInfo;
import com.bupt.iheath.model.UserState;
import com.bupt.iheath.ui.base.BaseActivity;
import com.bupt.iheath.utils.Constants;
import com.bupt.iheath.utils.NotifyUtils;
import com.bupt.iheath.utils.PhoneUtils;
import com.bupt.iheath.widgets.CircleImageView;
import com.bupt.iheath.widgets.ColorArcProgressBar;
import com.miband.sdk.ActionCallback;
import com.miband.sdk.MiBand;
import com.miband.sdk.listeners.HeartRateNotifyListener;
import com.miband.sdk.listeners.NotifyListener;
import com.miband.sdk.listeners.RealtimeStepsNotifyListener;
import com.miband.sdk.model.BatteryInfo;
import com.miband.sdk.model.LedColor;
import com.miband.sdk.model.UserInfo;
import com.miband.sdk.model.VibrationMode;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static MiBand sMiband = null ;
    public static Context sMainContext = null ;
    private static final long BACK_PRESS_EXIT_TIME = 2000L;
    private static final String TAG = "MainActivity" ;
    private static final int HEART_RECEIVED = 100;
    private static final int STEP_RECEIVED = 200;
    private static final int BATTERY_INFO_RECEIVED = 300;
    private static final int RING_CONNECTED = 400;
    private static final int RING_DISCONNECTED = 500;
    private long mFirstTime; //第一次按下返回键的时间
    private FloatingActionButton fab ;
    private ColorArcProgressBar stepBar ;
    private CardView mStateCardView;
    private TextView mStateTxt;
    private CardView mRingCardView;
    private TextView mRingTxt;
    private TextView mSignatureTxt ;
    private TextView mNickNameTxt ;
    private CircleImageView mUserHeadImage ;
    private com.baoyz.swipemenulistview.SwipeMenuListView mListView ;
    private HeartRecordAdapter mAdapter;
    private int mLastStep = 0 ;
    private boolean isFirstHeartMeasure = true ;

    private boolean isConnected = false;
    private Handler handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HEART_RECEIVED:
                    doAddHeart(msg.arg1);
                    break;
                case STEP_RECEIVED:
                    mLastStep = msg.arg1 ;
                    MyApplication.sCurrentStep = msg.arg1;
                    stepBar.setCurrentValues(msg.arg1);
                    break;
                case BATTERY_INFO_RECEIVED:
                    BatteryInfo batteryInfo = (BatteryInfo) msg.obj;
                    String level = "剩余电量：" + batteryInfo.getLevel()+"%" ;
                    String status = "状态：" + batteryInfo.getStatus() ;
                    String lastCharge ="上次充电时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:SS", Locale.CHINA).format(batteryInfo.getLastChargedDate().getTime()) ;
                    //NotifyUtils.showHints(level+"\n"+status+"\n"+lastCharge);
                    showBatteryInfo(level+"\n"+status+"\n"+lastCharge);
                    break;
                case RING_CONNECTED:
                    mRingTxt.setText("手环已连接");
                    isConnected = true ;
                    mRingCardView.setCardBackgroundColor(R.color.color_normal);
                    break;
                case RING_DISCONNECTED:
                    mRingTxt.setText("手环已断开");
                    isConnected = false ;
                    mRingCardView.setCardBackgroundColor(R.color.white);
                    //断开重连
                    sMiband.startScan(scanCallback);
                    break;
            }
        }
    };

    final BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            Log.e(TAG,
                    "找到附近的蓝牙设备: name:" + device.getName() + ",uuid:"
                            + device.getUuids() + ",add:"
                            + device.getAddress() + ",type:"
                            + device.getType() + ",bondState:"
                            + device.getBondState() + ",rssi:" + rssi);

            if (device.getName().equals("MI1S")) {
                 // TODO: connect
                sMiband.stopScan(scanCallback);
                doConnect(device);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sMiband = new MiBand(this) ;
        sMainContext = this ;
        stepBar = (ColorArcProgressBar) findViewById(R.id.step_count) ;
        Integer i ;
        i = (Integer) MyApplication.sCache.getAsObject("last_step") ;
        if (i != null) {
            mLastStep = i.intValue();
            stepBar.setCurrentValues(mLastStep);
            MyApplication.sCurrentStep = mLastStep ;
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View view = navigationView.getHeaderView(0) ;
        mNickNameTxt = (TextView) view.findViewById(R.id.nav_nickname);
        mSignatureTxt = (TextView) view.findViewById(R.id.nav_signature);
        mUserHeadImage = (CircleImageView) view.findViewById(R.id.nav_img_head);

        AccountInfo accountInfo = MyApplication.getInstance().getCurrentUser();
        mNickNameTxt.setText(accountInfo.getNickName());
        mSignatureTxt.setText(accountInfo.getSignature());
        BmobFile image = accountInfo.getImage() ;
        if (image != null) {
            image.loadImage(this,mUserHeadImage);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(MainActivity.this, UserInfoEditActivity.class);
                AccountInfo accountInfo = MyApplication.getInstance().getCurrentUser();
                BmobFile image = accountInfo.getImage();
                intent.putExtra("photo",image);
                startActivityForResult(intent, Constants.EDIT_USER_INFO);
            }
        });

        mStateCardView = (CardView) findViewById(R.id.card_view);
        mStateTxt = (TextView) findViewById(R.id.txt_state);
        mRingCardView = (CardView) findViewById(R.id.ring_card_view);
        mRingTxt = (TextView) findViewById(R.id.ring_state);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isConnected) {
                    Snackbar.make(view,"请先连接手环！",Snackbar.LENGTH_LONG).show();
                    return;
                }
                doHeart();
            }
        });

        initListView();
        initNavigation();
        UploadManager.getInstance().setView(fab);
        UploadManager.getInstance().startUpload();
        getAllUserState(10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.EDIT_USER_INFO && resultCode == RESULT_OK) {
            AccountInfo accountInfo = MyApplication.getInstance().getCurrentUser();
            mNickNameTxt.setText(accountInfo.getNickName());
            mSignatureTxt.setText(accountInfo.getSignature());
            BmobFile image = accountInfo.getImage() ;
            if (image != null) {
                image.loadImage(this,mUserHeadImage);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyApplication.sCache.put("last_step", mLastStep);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);  //先关闭左侧抽屉菜单
            return ;
        }

        if (mFirstTime + BACK_PRESS_EXIT_TIME > System.currentTimeMillis()) {
            MyApplication.getInstance().exit();
            super.onBackPressed();
        } else {
            NotifyUtils.showHints(getString(R.string.main_activity_exit_hint));
        }
        mFirstTime = System.currentTimeMillis();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_connect) {
            sMiband.startScan(scanCallback);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_remind:
                gotoActivity(this, RemindActivity.class, null);
                break;
            case R.id.nav_settings:
                gotoActivity(this, SettingsActivity.class, null);
                break;
            case R.id.nav_logout:
                MyApplication.getInstance().getCurrentUser().logOut(this);
                UploadManager.getInstance().stopUpload();
                MainActivity.sMiband.turnOffBluetooth();
                gotoActivity(this, LoginActivity.class, null);
                finish();
                break;
            case R.id.nav_exit:
                MyApplication.getInstance().exit();
                break;
            case R.id.enable_step:
                RealtimeSteps() ;
                break;
            case R.id.disable_step:
                stopRealtimeSteps();
                break;
            case R.id.nav_vibrate:
                Vibration();
                break;
            case R.id.nav_ring:
                getBattery();
                break;
            case R.id.analyze:
                gotoActivity(this, Analyze2Activity.class, null);
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initNavigation() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initListView() {
        mListView = (com.baoyz.swipemenulistview.SwipeMenuListView) findViewById(R.id.heart_list);
        mAdapter = new HeartRecordAdapter(this);
        mListView.setAdapter(mAdapter);

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("start");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "open" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xf9, 0x3f,
                        0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set item title
                deleteItem.setTitle("delete");
                // set item title fontsize
                deleteItem.setTitleSize(18);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);

            }
        };
        // set creator
        mListView.setMenuCreator(creator);
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // open
                        break;
                    case 1:
                        // delete
                        deleteHeartRecord(position);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    private void deleteHeartRecord(final int position) {
        Dialog alertDialog = new AlertDialog.Builder(this).
                setTitle("提示").
                setMessage("删除此次记录？").
                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteUserState(mAdapter.getUserStateByIndex(position));
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        alertDialog.show();
    }

    private void getAllUserState(int limit) {
        AccountInfo accountInfo = MyApplication.getInstance().getCurrentUser();
        BmobQuery<UserState> query = new BmobQuery<>();
        query.addWhereEqualTo("email", accountInfo.getEmail());
        limit = limit < 50 ? limit : 50 ;
        query.setLimit(limit);
        query.order("-createdAt");
        query.findObjects(this, new FindListener<UserState>() {
            @Override
            public void onSuccess(List<UserState> object) {
                for (UserState userState : object) {
                    //mAdapter.addItem(userState);
                    mAdapter.addItemAtLast(userState);
                }
            }

            @Override
            public void onError(int code, String msg) {
            }
        });
    }

    private void uploadUserState(final UserState userState) {
        userState.save(this, new SaveListener() {

            @Override
            public void onSuccess() {
                mAdapter.addItem(userState);
                NotifyUtils.showHints("添加成功！");
            }

            @Override
            public void onFailure(int code, String msg) {
                NotifyUtils.showHints("添加失败！请联网后重试...");
            }
        });
    }

    private void deleteUserState(final UserState userState) {
        UserState info = new UserState();
        info.setObjectId(userState.getObjectId());
        info.delete(this, new DeleteListener() {
            @Override
            public void onSuccess() {
                mAdapter.removeItem(userState);
                NotifyUtils.showHints("删除成功！");
            }

            @Override
            public void onFailure(int i, String s) {
                NotifyUtils.showHints("删除失败！联网后请重试...");
            }
        });
    }

    private void doAddHeart(int heart) {

        UserState state  = new UserState() ;

        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);

        state.setWhen(dateString);
        state.setHeartRate(heart);
        state.setState(MyApplication.sCurrentState);
        state.setStep(MyApplication.sCurrentStep);

        MyApplication.sCurrentHeart = heart ;

        if (heart<=150 && heart>=60) {
            // normal
            mStateCardView.setCardBackgroundColor(R.color.color_normal);
            mStateTxt.setText("正常！");
            state.setState(0);
            MyApplication.sCurrentState = 0 ;
        } else if (heart>150 && heart<200) {
            // warning
            mStateCardView.setCardBackgroundColor(R.color.color_warning);
            mStateTxt.setText("警告！");
            state.setState(1);
            MyApplication.sCurrentState = 1 ;
        } else {
             //dangerous
            mStateCardView.setCardBackgroundColor(R.color.color_dangerous);
            mStateTxt.setText("危险！");
            state.setState(2);
            MyApplication.sCurrentState = 2 ;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    boolean autoCall = (boolean) MyApplication.sCache.getAsObject(Constants.AUTO_CALL_SWITCH);
                    if (autoCall) {
                        String phoneNumber = MyApplication.sCache.getAsString(Constants.EMERGENCY_CALL_HPONE);

                        PhoneUtils.call(phoneNumber);
                    }
                }
            }, 3000);
        }

        sMiband.setLedColor(LedColor.RED);
        uploadUserState(state);
    }

    private void doConnect(BluetoothDevice device){
        //final ProgressDialog pd = ProgressDialog.show(MainActivity.this, "", "连接手环中, 请稍后......");
        mRingTxt.setText("连接手环中, 请稍后......");
        sMiband.connect(device, new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                //pd.dismiss();
                Log.e(TAG, "连接成功!!!");
                Message msg = Message.obtain(handler);
                msg.what = RING_CONNECTED;
                msg.sendToTarget();
                sMiband.setDisconnectedListener(new NotifyListener() {
                    @Override
                    public void onNotify(byte[] data) {
                        Log.e(TAG, "连接断开!!!");
                        Message msg = Message.obtain(handler);
                        msg.what = RING_DISCONNECTED;
                        msg.sendToTarget();
                    }
                });
            }

            @Override
            public void onFail(int errorCode, String msg) {
                //pd.dismiss();
                mRingTxt.setText("手环已断开");
                Log.e(TAG, "connect fail, code:" + errorCode + ",mgs:" + msg);
            }
        });
    }

    private void getBattery(){
        sMiband.getBatteryInfo(new ActionCallback() {

            @Override
            public void onSuccess(Object data) {
                BatteryInfo info = (BatteryInfo) data;
                Log.e(TAG, "电池信息：" + info.toString());
                Message msg = Message.obtain(handler);
                msg.what = BATTERY_INFO_RECEIVED;
                msg.obj = info ;
                msg.sendToTarget();
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.e(TAG, "getBatteryInfo fail");
            }
        });
    }

    public void setUserInfo(){
        UserInfo userInfo = new UserInfo(20271234, 1, 32, 160, 40, "1哈哈", 0);
        Log.e(TAG, "setUserInfo:" + userInfo.toString() + ",data:" + Arrays.toString(userInfo.getBytes(sMiband.getDevice().getAddress())));
        sMiband.setUserInfo(userInfo);
        Log.e(TAG,"设置用户信息");
    }


    private HeartRateNotifyListener heartRateNotifyListener = new HeartRateNotifyListener() {
        @Override
        public void onNotify(int heartRate) {
            Log.e(TAG, "heart rate: " + heartRate);
            Message msg = Message.obtain(handler);
            msg.what = HEART_RECEIVED;
            msg.arg1 = heartRate;
            msg.sendToTarget();
        }
    } ;

    private void showBatteryInfo(String info) {
        Dialog alertDialog = new AlertDialog.Builder(this).
                setTitle("手环信息").
                setMessage(info).
                setPositiveButton("确定", null)
                .create();
        alertDialog.show();
    }

    private void doHeart(){
        if (isFirstHeartMeasure) {
            setUserInfo();
            NotifyUtils.showHints("测量心率失败，请再次测量");
            isFirstHeartMeasure = false ;
            return;
        }

        //设置心跳监听器
        sMiband.setHeartRateScanListener(heartRateNotifyListener);
        NotifyUtils.showHints("测试心率");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "正在测试心跳");
                sMiband.startHeartRateScan();
            }
        }, 1000);
    }

    private void Vibration(){
        //震动2次， 三颗led亮
        sMiband.startVibration(VibrationMode.VIBRATION_WITH_LED);
        Log.e(TAG,"震动！");
        NotifyUtils.showHints("震动");
        //震动2次, 没有led亮
        //miband.startVibration(VibrationMode.VIBRATION_WITHOUT_LED);

        //震动10次, 中间led亮蓝色
        //miband.startVibration(VibrationMode.VIBRATION_10_TIMES_WITH_LED);

    }

    private void RealtimeSteps(){
        NotifyUtils.showHints("开启步数通知");
        sMiband.setRealtimeStepsNotifyListener(new RealtimeStepsNotifyListener() {

            @Override
            public void onNotify(int steps)
            {
                Log.e(TAG, "RealtimeStepsNotifyListener:" + steps);
                //stepBar.setCurrentValues(steps);
                Message msg = Message.obtain(handler);
                msg.what = STEP_RECEIVED;
                msg.arg1 = steps;
                msg.sendToTarget();
            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,"开启步数通知") ;
                sMiband.enableRealtimeStepsNotify();
            }
        }, 1000);
    }

    private void stopRealtimeSteps(){
        NotifyUtils.showHints("关闭步数通知");
        sMiband.disableRealtimeStepsNotify();
        Log.e(TAG,"关闭步数通知") ;
    }

    private int dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static class UploadManager {

        private static int interval = 1000 * 60 * 1000;
        private Handler mHandler = new Handler();
        private View view ;
        private Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                doClick();
                mHandler.postDelayed(this, interval);
            }
        };

        private static class Nest {
            private final static UploadManager instance = new UploadManager();
        }

        public static UploadManager getInstance() {
            return Nest.instance;
        }

        public void startUpload() {
            String s = MyApplication.sCache.getAsString(Constants.UPLOAD_INTERVAL);
            int tempInterval;
            if (s.equals("5")) {  // five minute
                tempInterval = 5 * 60 * 1000;
            } else if (s.equals("3")) { // three minute
                tempInterval = 3 * 60 * 1000;
            } else if (s.equals("2")) {  // two minute
                tempInterval = 2 * 60 * 1000;
            } else {
                tempInterval = 0;
            }
            if (tempInterval == 0) {
                stopUpload();
                return;
            } else {
                interval = tempInterval;
            }
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, interval);
        }

        public void stopUpload() {
            mHandler.removeCallbacks(mRunnable);
        }

        public void setView(View v) {
            view = v ;
        }

        private void doClick() {
            if (view != null) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        view.performClick();
                    }
                }) ;
            }
        }
    }
}

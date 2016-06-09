package com.bupt.iheath.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.bupt.iheath.MyApplication;
import com.bupt.iheath.R;
import com.bupt.iheath.model.AccountInfo;
import com.bupt.iheath.ui.base.BaseActivity;
import com.bupt.iheath.utils.NotifyUtils;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.METValidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.listener.UpdateListener;

public class SupervisorInfoEditActivity extends BaseActivity {

    private MaterialEditText mPhoneMET;
    private MaterialEditText mEmailMET;
    private MaterialEditText mPasswordMET;
    private MaterialEditText mSmsMET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_info_edit);
        setupActionBar();

        mPhoneMET = (MaterialEditText) findViewById(R.id.supervisor_phone);
        mPhoneMET.addValidator(new METValidator("请填入正确的电话号码！") {
            @Override
            public boolean isValid(@NonNull CharSequence text, boolean isEmpty) {
                boolean flag = false;
                try {
                    Pattern regex = Pattern.compile("^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
                    Matcher matcher = regex.matcher(text);
                    flag = matcher.matches();
                } catch (Exception e) {
                    flag = false;
                }
                return flag;
            }
        });
        mEmailMET = (MaterialEditText) findViewById(R.id.supervisor_email);
        mEmailMET.addValidator(new METValidator("请填入正确的邮箱！") {
            @Override
            public boolean isValid(@NonNull CharSequence text, boolean isEmpty) {
                boolean flag = false;
                try {
                    String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
                    Pattern regex = Pattern.compile(check);
                    Matcher matcher = regex.matcher(text);
                    flag = matcher.matches();
                } catch (Exception e) {
                    flag = false;
                }
                return flag;
            }
        });
        mPasswordMET = (MaterialEditText) findViewById(R.id.supervisor_password);
        mPasswordMET.addValidator(new METValidator("密码至少6位！") {
            @Override
            public boolean isValid(@NonNull CharSequence text, boolean isEmpty) {
                return text.length() >= 6;
            }
        });
        mSmsMET = (MaterialEditText) findViewById(R.id.emergency_sms);

        AccountInfo accountInfo = MyApplication.getInstance().getCurrentUser();
        setText(mEmailMET, accountInfo.getSupervisorEmail());
        setText(mPhoneMET, accountInfo.getSupervisorPhone());
        setText(mPasswordMET, accountInfo.getSupervisorPassword());
        setText(mSmsMET, accountInfo.getEmergencySms());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_user_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_save) {
            // TODO 保存
            saveInfo();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setText(EditText et, String text) {
        if (!TextUtils.isEmpty(text)) {
            et.setText(text);
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 显示返回箭头
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void saveInfo() {
        if (mPhoneMET.validate() && mEmailMET.validate() && mPasswordMET.validate()) {

            String email = mEmailMET.getText().toString();
            String phone = mPhoneMET.getText().toString();
            String sms = mSmsMET.getText().toString();
            String password = mPasswordMET.getText().toString();

            String objectId = MyApplication.getInstance().getCurrentUser().getObjectId();

            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setSupervisorEmail(email);
            accountInfo.setSupervisorPhone(phone);
            accountInfo.setSupervisorPassword(password);
            accountInfo.setEmergencySms(sms);

            final ProgressDialog pd = ProgressDialog.show(this, "", "保存中，请稍候......");
            accountInfo.update(this, objectId, new UpdateListener() {
                @Override
                public void onSuccess() {
                    NotifyUtils.showHints("保存成功！！！");
                    finish();
                    pd.dismiss();
                }

                @Override
                public void onFailure(int i, String s) {
                    NotifyUtils.showHints("保存失败，请重新尝试!" + i + s);
                    pd.dismiss();
                }
            });
        }
    }

}

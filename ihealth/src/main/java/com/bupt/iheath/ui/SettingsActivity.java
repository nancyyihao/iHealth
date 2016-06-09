package com.bupt.iheath.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.bupt.iheath.MyApplication;
import com.bupt.iheath.R;
import com.bupt.iheath.model.AccountInfo;
import com.bupt.iheath.utils.Constants;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private  Preference.OnPreferenceChangeListener mOnPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else if (preference instanceof SwitchPreference){
                //preference.setSummary(stringValue);
            } else {
                preference.setSummary(stringValue);
            }

            if (preference == mCallPreference) {
                String newNumber = mCallPreference.getText() ;
                MyApplication.sCache.put(Constants.EMERGENCY_CALL_HPONE,newNumber);
            } else
            if (preference == mUploadPreference) {
                int index = mUploadPreference.findIndexOfValue(stringValue);
                String upload = (String) mUploadPreference.getEntryValues()[index];
                MyApplication.sCache.put(Constants.UPLOAD_INTERVAL,upload,24*60*60);
                MainActivity.UploadManager.getInstance().startUpload();
            } else
            if (preference == mAutoCallSwitchPreference) {
                boolean checked = mAutoCallSwitchPreference.isChecked() ;
                MyApplication.sCache.put(Constants.AUTO_CALL_SWITCH,checked);
            }

            return true;
        }
    };

    private EditTextPreference mCallPreference ;
    private ListPreference mUploadPreference ;
    private SwitchPreference mAutoCallSwitchPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        addPreferencesFromResource(R.xml.pref_general);

        mCallPreference = (EditTextPreference) findPreference(Constants.EMERGENCY_CALL_HPONE);
        mUploadPreference = (ListPreference) findPreference(Constants.UPLOAD_INTERVAL);
        mAutoCallSwitchPreference = (SwitchPreference) findPreference(Constants.AUTO_CALL_SWITCH);

        mCallPreference.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
        mUploadPreference.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
        mAutoCallSwitchPreference.setOnPreferenceChangeListener(mOnPreferenceChangeListener);

        findPreference("about").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                return true;
            }
        });
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 显示返回箭头
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

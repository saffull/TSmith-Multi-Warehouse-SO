package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Settings extends AppCompatActivity {
    TextView tvDeviceId, tvVersionName;
    EditText etUrlValue;
    String myuniqueID, URL;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_settings);
        tvDeviceId = findViewById(R.id.tvDeviceIdValue);
        etUrlValue = findViewById(R.id.etUrlValue);
        tvVersionName = findViewById(R.id.tvAppVersionValue);
        prefs = PreferenceManager.getDefaultSharedPreferences(Settings.this);

        try {
            String androidId = android.provider.Settings.Secure.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            myuniqueID = androidId;
        } catch (Exception e) {
            e.printStackTrace();
        }

        PackageInfo pinfo = null;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int versionNumber = pinfo.versionCode;
            String versionName = pinfo.versionName;
            tvVersionName.setText("" + versionName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        tvDeviceId.setText(myuniqueID);
    }

    public void SaveUrl(View view) {
        URL = etUrlValue.getText().toString().trim();
        SharedPreferences.Editor editor = prefs.edit();
        //editor.putString("MultiSOStoredDevId", myuniqueID);
        //editor.putString("MultiSOStoredDevId", "saffull@gmail.com");
        editor.putString("MultiSOStoredDevId", "salam_ka@yahoo.com");
        editor.putString("MultiSOURL", URL);
        editor.apply();
        finish();
    }
}
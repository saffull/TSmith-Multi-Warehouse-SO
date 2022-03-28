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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Settings extends AppCompatActivity {
    TextView tvDeviceId, tvVersionName;
    EditText etUrlValue, printer;
    String myuniqueID, URL, printerName;
    SharedPreferences prefs;
    List<String> printList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getSupportActionBar().hide();
        setContentView(R.layout.activity_settings);
        tvDeviceId = findViewById(R.id.tvDeviceIdValue);
        etUrlValue = findViewById(R.id.etUrlValue);
        tvVersionName = findViewById(R.id.tvAppVersionValue);
        printer = findViewById(R.id.printer);
        prefs = PreferenceManager.getDefaultSharedPreferences(Settings.this);

        try {
            String androidId = android.provider.Settings.Secure.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            myuniqueID = androidId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        printerName = prefs.getString("printlist", "");
        printList = new ArrayList<String>(Arrays.asList(printerName.split(",")));
        //printList.clear();
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
        String temp=printer.getText().toString();
        if (!temp.isEmpty()) {
            URL = etUrlValue.getText().toString().trim();
            printerName = printer.getText().toString().trim();
            if (!printList.contains(printerName))
                printList.add(printerName);
            else
                System.out.println("Already in list");
            SharedPreferences.Editor editor = prefs.edit();
            //editor.putString("MultiSOStoredDevId", myuniqueID);
            //editor.putString("MultiSOStoredDevId", "saffull@gmail.com");
            editor.putString("MultiSOStoredDevId", "salam_ka@yahoo.com");
            editor.putString("MultiSOURL", URL);
            editor.putString("printer", printList.toString().trim());
            editor.apply();
            finish();
        } else {

        }
    }
}
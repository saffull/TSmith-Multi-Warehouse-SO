package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.techsmith.mw_so.collection_utils.CSpinner;
import com.techsmith.mw_so.delivery_utils.GpsTracker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Delivery extends AppCompatActivity {
    SharedPreferences prefs;
    EditText edtotp, deliverydate;
    TextView txtDate;
    DatePickerDialog datePickerDialog;
    private CSpinner custSelect;
    public String[] customers = {"Ani", "Sam", " Joe"};
    private Double latitude, longitude;
    private GpsTracker gpsTracker;


    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(Delivery.this, Category.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);
        getSupportActionBar().hide();
        prefs = PreferenceManager.getDefaultSharedPreferences(Delivery.this);
        edtotp = findViewById(R.id.edtotp);
        Date c = Calendar.getInstance().getTime();
        deliverydate = findViewById(R.id.deliverydate);
        txtDate = findViewById(R.id.txtDate);
        custSelect = findViewById(R.id.custSelect);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);

        deliverydate.setText(formattedDate);
        txtDate.setText(formattedDate);


        ArrayAdapter ad
                = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                customers);

        ad.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);

        custSelect.setAdapter(ad);

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        gpsTracker = new GpsTracker(Delivery.this);
        if (gpsTracker.canGetLocation()) {
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
            System.out.println("Latitude & Longitude is " + latitude +"--"+ longitude);

        } else {
            gpsTracker.showSettingsAlert();
        }

        findViewById(R.id.datepick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);


                datePickerDialog = new DatePickerDialog(Delivery.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                System.out.println("Current time => " + year + "--" + month + "--" + day);
                                if (day < 10 && month < 10)
                                    deliverydate.setText("0" + day + "-" + "0" + month + "-" + year);
                                if (day > 10 && month < 10)
                                    deliverydate.setText(day + "-" + "0" + month + "-" + year);
                                if (day < 10 && month > 10)
                                    deliverydate.setText("0" + day + "-" + month + "-" + year);
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });
    }
}
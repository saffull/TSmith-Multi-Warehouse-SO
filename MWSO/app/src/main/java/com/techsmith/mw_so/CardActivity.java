package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class CardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
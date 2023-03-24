package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class CashActivity extends AppCompatActivity {
    private TextInputEditText cashAmount, cashRemarks;
    private TextView cashRcd;
    private String remarks,cash,tempTotal;
    private Button updateCash;
    private SharedPreferences prefs;
    private double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash);
        prefs = PreferenceManager.getDefaultSharedPreferences(CashActivity.this);
        tempTotal= prefs.getString("billTotal","200");
        System.out.println("Total amount is "+tempTotal);
        cashRemarks = findViewById(R.id.cashRemarks);
        cashAmount = findViewById(R.id.cashAmount);
        cashRcd = findViewById(R.id.cashRcd);
        updateCash=findViewById(R.id.updateCash);

        updateCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                total=Double.parseDouble(tempTotal);
                remarks=cashRemarks.getText().toString().trim();
                cash=cashAmount.getText().toString();
                if(Double.parseDouble(cash)>total){
                    Toast.makeText(CashActivity.this, "Please Check the amount again..!!", Toast.LENGTH_SHORT).show();
                }else if (cash.isEmpty()){
                    Toast.makeText(CashActivity.this, "cash field empty", Toast.LENGTH_SHORT).show();
                }else{
                    //((PaymentActivity)getActivity()).updateCashAmount(cash);
                }

            }
        });

        cashAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String cash = editable.toString();
                cashRcd.setText("\u20B9 "+cash);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
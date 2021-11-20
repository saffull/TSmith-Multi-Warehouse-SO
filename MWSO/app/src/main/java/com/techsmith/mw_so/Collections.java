package com.techsmith.mw_so;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class Collections extends AppCompatActivity {
    EditText cashAmount, autofill,userInput;
    RadioButton cash, cheque;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout linearLayoutBSheet, ll;
    private ToggleButton tbUpDown;
    private Button save, reset, remarks, autoFill;
    private TextView tvAmountValue;

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(Collections.this,Category.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collections);
        getSupportActionBar().hide();
        cashAmount = findViewById(R.id.cashAmount);
        cash = findViewById(R.id.radioCOLCash);
        cheque = findViewById(R.id.radioCOLCheque);
        ll = findViewById(R.id.paymentDetails);
        autoFill = findViewById(R.id.btnCOL_AutoFill);

        init();

        tbUpDown.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View view, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    tbUpDown.setChecked(true);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    tbUpDown.setChecked(false);
                }
            }

            @Override
            public void onSlide(View view, float v) {

            }
        });
    }

    private void init() {
        this.linearLayoutBSheet = findViewById(R.id.bottomSheet);
        this.bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBSheet);
        this.tbUpDown = findViewById(R.id.toggleButton);
        this.save = findViewById(R.id.btnCOL_Save);
        this.reset = findViewById(R.id.btnCOL_Reset);
        this.remarks = findViewById(R.id.btnCOL_Remarks);
        this.tvAmountValue = findViewById(R.id.tvAmountValue);
    }


    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        String str = "";
        switch (view.getId()) {
            case R.id.radioCOLCash:
                if (checked) {
                    cashAmount.setVisibility(View.VISIBLE);
                    ll.setVisibility(View.GONE);
                }
                break;

            case R.id.radioCOLCheque:
                if (checked) {
                    cashAmount.setVisibility(View.GONE);
                    ll.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
    }

    public void AutoFillAmount(View view) {
        LayoutInflater li = LayoutInflater.from(Collections.this);
        View promptsView = li.inflate(R.layout.autofill_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Collections.this);
        alertDialogBuilder.setView(promptsView);
        userInput = (EditText) promptsView.findViewById(R.id.etUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
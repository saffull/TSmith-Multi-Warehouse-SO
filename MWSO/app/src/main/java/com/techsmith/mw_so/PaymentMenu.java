package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.techsmith.mw_so.Expandable.RecyclerTouchListener;
import com.techsmith.mw_so.payment_util.PaymentAdapter;
import com.techsmith.mw_so.payment_util.PaymentList;
import com.techsmith.mw_so.payment_util.PaymentMethodModel;

import java.util.ArrayList;

public class PaymentMenu extends AppCompatActivity {
    private RecyclerView courseRV;
    PaymentAdapter courseAdapter;
    private ArrayList<PaymentMethodModel> courseModelArrayList;
    FragmentManager fm;
    SharedPreferences prefs, prefsD;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout linearLayoutBSheet;
    private ToggleButton tbUpDown;
    private Button cancelBtn, updatePayment;
    public TextView billAmt, cashAmt, pay_total, cardAmt, balAmt;
    private double total, payTotal = 0.0;
    private int Image = 0;
    Gson gson;
    private String tempTotal = "", iCash = "0.0", iCard = "0.0", iUpi = "0.0", cashSave = "", cardSave = "",
            cashAmount = "", cardAmount = "",cardNo="";
    private String[] modeNames = {"Cash", "Card", "UPI"};

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Toast.makeText(PaymentMenu.this, "Click on the Cancel Button", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_menu);
        prefs = PreferenceManager.getDefaultSharedPreferences(PaymentMenu.this);
        prefsD = getSharedPreferences("pref", Context.MODE_PRIVATE);
        init();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("billTotal");
            System.out.println("Total value is " + value);
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(PaymentMenu.this);
        tempTotal = prefs.getString("billTotal", "0.0");
        total = Double.parseDouble(tempTotal);

        if (!tempTotal.isEmpty()) {
            String temp = String.format("%.2f", total);
            billAmt.setText(temp);
        }
        cashSave = prefsD.getString("cashSave", "");
        cardSave = prefsD.getString("cardSave", "");
        System.out.println("Last Saved Card amount is " + cardSave);

        if (!cashSave.isEmpty()) {
            try {
                gson = new Gson();
                PaymentList paymentList = gson.fromJson(cashSave, PaymentList.class);
                cashAmount = paymentList.cashAmount;
                cashAmt.setText(cashAmount);
                addPayment();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!cardSave.isEmpty()) {
            try {
                gson = new Gson();
                PaymentList pay = gson.fromJson(cardSave, PaymentList.class);
                cardAmount = pay.cardAmount;
                cardNo=pay.cardNo;
                cardAmt.setText(cardAmount);
                addPayment();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        courseRV = findViewById(R.id.my_recycler_view);

        if (cardSave.isEmpty()&& cashSave.isEmpty()){
            startData();
        }else {
            updateListWithAmount(cashAmount,cardAmount);
        }


        courseAdapter = new PaymentAdapter(this, courseModelArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        courseRV.setLayoutManager(linearLayoutManager);
        courseRV.setAdapter(courseAdapter);
        courseRV.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), courseRV, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                PaymentMethodModel movie = courseModelArrayList.get(position);
                if (movie.getName().equalsIgnoreCase("Cash")) {
                    Cash_Dialog_Fragment dialogFragment = new Cash_Dialog_Fragment();
                    dialogFragment.setCancelable(false);
                    dialogFragment.show(getSupportFragmentManager(), "Cash Fragment");
                } else if (movie.getName().equalsIgnoreCase("Card")) {
                    Card_dialog_fragment dialogFragment = new Card_dialog_fragment();
                    dialogFragment.setCancelable(false);
                    dialogFragment.show(getSupportFragmentManager(), "Card Fragment");
                } else {
                    Toast.makeText(PaymentMenu.this, "Function not yet Implemented..", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

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

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("billCard", "");
                editor.putString("billCash", "");
                editor.apply();
                finish();
            }
        });

        updatePayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                System.out.println("Bill amount is "+billAmt.getText().toString()+"\n"+cashAmt.getText().toString()
                +"\n"+Double.parseDouble(cardAmt.getText().toString()));
                double dd=0.0;
                dd=dd+Double.parseDouble(cashAmt.getText().toString())+Double.parseDouble(cardAmt.getText().toString());
                //finish();

                if (dd > Double.parseDouble(billAmt.getText().toString())) {
                    Toast.makeText(PaymentMenu.this, "Total Amount Greater Than Bill Amount", Toast.LENGTH_LONG).show();
                } else if (dd < Double.parseDouble(billAmt.getText().toString())) {
                    Toast.makeText(PaymentMenu.this, "Total Amount Less Than Bill Amount", Toast.LENGTH_LONG).show();
                } else {
                    finish();
                }
            }
        });

    }

    private void addPayment() {
        double dd=0.0;
        dd=dd+Double.parseDouble(cashAmt.getText().toString())+Double.parseDouble(cardAmt.getText().toString());
        //pay_total.setText(String.format("%.2f",dd));
        pay_total.setText(String.valueOf(dd));
    }

    private void updateListWithAmount(String cashAmount, String cardAmount) {
    try {
        if (!cashAmount.isEmpty()&& cardAmount.isEmpty()){
            courseModelArrayList = new ArrayList<>();
            Image = R.drawable.payment_method;
            iCash=cashAmount;
            courseModelArrayList.add(new PaymentMethodModel("Cash", Image, iCash,""));
            courseModelArrayList.add(new PaymentMethodModel("Card", Image, iCard,""));
            courseModelArrayList.add(new PaymentMethodModel("UPI", Image, iUpi,""));
        }else if (cashAmount.isEmpty()&& !cardAmount.isEmpty()){
            courseModelArrayList = new ArrayList<>();
            Image = R.drawable.payment_method;
            iCard=cardAmount;
            courseModelArrayList.add(new PaymentMethodModel("Cash", Image, iCash,""));
            courseModelArrayList.add(new PaymentMethodModel("Card", Image, iCard,cardNo));
            courseModelArrayList.add(new PaymentMethodModel("UPI", Image, iUpi,""));
        }else  if (!cashAmount.isEmpty()){
            courseModelArrayList = new ArrayList<>();
            Image = R.drawable.payment_method;
            iCash=cashAmount;
            iCard=cardAmount;
            courseModelArrayList.add(new PaymentMethodModel("Cash", Image, iCash,""));
            courseModelArrayList.add(new PaymentMethodModel("Card", Image, iCard,cardNo));
            courseModelArrayList.add(new PaymentMethodModel("UPI", Image, iUpi,""));
        }else{
            System.out.println("Do nothing..");
        }
    }catch (Exception e){e.printStackTrace();}
    }


    private void startData() {
        courseModelArrayList = new ArrayList<>();
        Image = R.drawable.ic_list;
        for (int i = 0; i < modeNames.length; i++) {
            if (modeNames[i].equalsIgnoreCase("cash")) {
                Image = R.drawable.payment_method;
            } else if (modeNames[i].equalsIgnoreCase("card")) {
                Image = R.drawable.credit_card;
            } else {
                Image = R.drawable.bhim_upi;
            }
            courseModelArrayList.add(new PaymentMethodModel(modeNames[i], Image, iCash,""));
        }
        //courseModelArrayList.add(new PaymentMethodModel("Cash", Image, iCash));
        //courseModelArrayList.add(new PaymentMethodModel("Card", Image, iCard));
        // courseModelArrayList.add(new PaymentMethodModel("UPI", Image, iUpi));
    }

    public void updateList(String cash, String what, String cardno) {
        courseRV.setAdapter(null);
        if (what.equalsIgnoreCase("user_cash")) {
            iCash=cash;
            courseModelArrayList = new ArrayList<>();
            Image = R.drawable.payment_method;
            courseModelArrayList.add(new PaymentMethodModel("Cash", Image,iCash,""));
            courseModelArrayList.add(new PaymentMethodModel("Card", Image, iCard,""));
            courseModelArrayList.add(new PaymentMethodModel("UPI", Image, iUpi,""));

        } else if (what.equalsIgnoreCase("user_card")) {
            iCard = cash;
            courseModelArrayList = new ArrayList<>();
            Image = R.drawable.payment_method;
            courseModelArrayList.add(new PaymentMethodModel("Cash", Image, iCash,""));
            courseModelArrayList.add(new PaymentMethodModel("Card", Image, iCard,cardno));
            courseModelArrayList.add(new PaymentMethodModel("UPI", Image, iUpi,""));
        } else {
            courseModelArrayList = new ArrayList<>();
            Image = R.drawable.ic_list;
            for (int i = 0; i < modeNames.length; i++) {
                if (modeNames[i].equalsIgnoreCase("cash")) {
                    Image = R.drawable.payment_method;
                } else if (modeNames[i].equalsIgnoreCase("card")) {
                    Image = R.drawable.credit_card;
                } else {
                    Image = R.drawable.bhim_upi;
                }
                courseModelArrayList.add(new PaymentMethodModel(modeNames[i], Image, iCash,""));
            }
        }
        courseAdapter = new PaymentAdapter(this, courseModelArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        courseRV.setLayoutManager(linearLayoutManager);
        courseRV.setAdapter(courseAdapter);

    }

    private void init() {
        this.linearLayoutBSheet = findViewById(R.id.bottomSheetPayment);
        this.bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBSheet);
        this.tbUpDown = findViewById(R.id.toggleButton);
        this.cancelBtn = findViewById(R.id.cancelBtn);
        this.billAmt = findViewById(R.id.billAmt);
        this.cashAmt = findViewById(R.id.cashAmt);
        this.pay_total = findViewById(R.id.pay_total);
        this.updatePayment = findViewById(R.id.updatePayment);
        this.cardAmt = findViewById(R.id.cardAmt);
    }

    public void updateCashAmount(String cash) {
        cashAmt.setText(String.format("%.2f", Double.parseDouble(cash)));
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("billCash", String.format("%.2f", Double.parseDouble(cash)));
        editor.apply();
        updatePayTotal(String.format("%.2f", Double.parseDouble(cash)));
    }

    public void updateCardAmount( String cash,String cArdNo) {
       // cardAmt.setText(String.format("%.2f", Double.parseDouble(cash)));
        cardAmt.setText(cash);
        System.out.println("New Card Number is "+cArdNo);
        cardNo=cArdNo;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("billCard", cash);
        editor.apply();
        updatePayTotal(cash);
    }

    private void updatePayTotal(String cash) {
        payTotal = 0.0;
        String s = String.format("%.2f", Double.parseDouble(cashAmt.getText().toString()));
        String m = String.format("%.2f", Double.parseDouble(cardAmt.getText().toString()));
        payTotal = Double.parseDouble(cardAmt.getText().toString()) + Double.parseDouble(cashAmt.getText().toString());
        //payTotal=payTotal+Double.parseDouble(s)+Double.parseDouble(m);
        //pay_total.setText(String.valueOf(payTotal));
        pay_total.setText(String.format("%.2f",payTotal));
    }
}

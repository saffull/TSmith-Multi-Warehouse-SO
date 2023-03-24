package com.techsmith.mw_so;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;


public class FirstFragment extends Fragment {

    private TextInputEditText cashAmount, cashRemarks;
    private TextView cashRcd;
    private String remarks,cash,tempTotal;
    private Button updateCash;
    private SharedPreferences prefs;
    private double total;
    Context context=getActivity();


    public FirstFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_first, container, false);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        prefs= this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        tempTotal= prefs.getString("billTotal","200");
        System.out.println("Total amount is "+tempTotal);
        cashRemarks = view.findViewById(R.id.cashRemarks);
        cashAmount = view.findViewById(R.id.cashAmount);
        cashRcd = view.findViewById(R.id.cashRcd);
        updateCash=view.findViewById(R.id.updateCash);

        updateCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                total=Double.parseDouble(tempTotal);
                remarks=cashRemarks.getText().toString().trim();
                cash=cashAmount.getText().toString();
              if(Double.parseDouble(cash)>total){
                    Toast.makeText(getActivity(), "Please Check the amount again..!!", Toast.LENGTH_SHORT).show();
                }else if (cash.isEmpty()){
                    Toast.makeText(getActivity(), "cash field empty", Toast.LENGTH_SHORT).show();
                }else{
                  ((PaymentActivity)getActivity()).updateCashAmount(cash);
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
        return view;
    }
}
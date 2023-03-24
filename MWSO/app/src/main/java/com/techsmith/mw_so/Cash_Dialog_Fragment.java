package com.techsmith.mw_so;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.techsmith.mw_so.payment_util.DecimalDigitsInputFilter;
import com.techsmith.mw_so.payment_util.PaymentList;


public class Cash_Dialog_Fragment extends DialogFragment {
    private TextInputEditText cashAmount, cashRemarks;
    private TextView cashRcd, totalBill;
    private String remarks, cash, tempTotal, cashSave = "";
    private Button updateCash, cancel;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private double total = 0.0;
    Gson gson;
    ImageButton autoFill;
    PaymentList paymentList;
    Context context = getActivity();

    public Cash_Dialog_Fragment() {
        // Required empty public constructor
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

        prefs = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        tempTotal = prefs.getString("billTotal", "0.0");
        cashSave = prefs.getString("cashSave", "");
        System.out.println("Total amount is " + tempTotal);
        cashRemarks = view.findViewById(R.id.cashRemarks);
        cashAmount = view.findViewById(R.id.cashAmount);
        cashRcd = view.findViewById(R.id.cashRcd);
        totalBill = view.findViewById(R.id.totalBill);
        updateCash = view.findViewById(R.id.updateCash);
        cancel = view.findViewById(R.id.cancel);
        autoFill = view.findViewById(R.id.autoFill);
        try {
            totalBill.setText("Bill Amount is \u20B9"+((PaymentMenu) getActivity()).billAmt.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!cashSave.isEmpty()) {
            try {
                gson = new Gson();
                PaymentList paymentList = gson.fromJson(cashSave, PaymentList.class);
                cashAmount.setText(paymentList.cashAmount);
                cashRemarks.setText(paymentList.cashRemarks);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        updateCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    tempTotal = ((PaymentMenu) getActivity()).billAmt.getText().toString();
                    total = Double.parseDouble(tempTotal);
                    remarks = cashRemarks.getText().toString().trim();
                    cash = cashAmount.getText().toString();
                    double dd = 0.0;

                    if (cash.isEmpty()) {
                        popUp("cash field empty");
                    } else {
                        dd = Double.parseDouble(cash);
                        if (dd > total) {
                            popUp("Please Check the amount again..!!");
                        } else {
                            paymentList = new PaymentList();
                            paymentList.cashAmount = cash;
                            paymentList.cashRemarks = remarks;
                            gson = new Gson();
                            String cashSave = gson.toJson(paymentList);
                            editor = prefs.edit();
                            editor.putString("cashSave", cashSave);
                            editor.apply();
                            System.out.println("Saved amount is " + cashSave);
                            ((PaymentMenu) getActivity()).updateCashAmount(cash);
                            ((PaymentMenu) getActivity()).updateList(cash, "user_cash");
                            dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        cashAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 2)});
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
                cashRcd.setText("\u20B9 " + cash);
            }
        });
        autoFill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempTotal = ((PaymentMenu) getActivity()).billAmt.getText().toString();
                double temp = 0.0;
                temp = Double.parseDouble(((PaymentMenu) getActivity()).cardAmt.getText().toString());
                if (temp == 0.0) {
                    cashAmount.setText(tempTotal);

                } else {
                    double bal = 0.0;
                    bal = Double.parseDouble(tempTotal) - Double.parseDouble(((PaymentMenu) getActivity()).cardAmt.getText().toString());
                    cashAmount.setText(String.format("%.2f", bal));
                }
            }
        });
        return view;
    }

    private void popUp(String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

   /* private class DecimalDigitsInputFilter implements InputFilter {
        private Pattern mPattern;
        public DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
            mPattern=Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher matcher=mPattern.matcher(dest);
            if(!matcher.matches())
                return "";
            return null;
        }
    }*/
}
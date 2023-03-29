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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.techsmith.mw_so.Spinner.CustomAdapter;
import com.techsmith.mw_so.payment_util.CreditCardNumberTextWatcher;
import com.techsmith.mw_so.payment_util.DecimalDigitsInputFilter;
import com.techsmith.mw_so.payment_util.PaymentList;
import com.techsmith.mw_so.utils.CustomerReceivables;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Card_dialog_fragment extends DialogFragment {
    TextInputEditText remarks, expiry_month, expiry_year, smID, iB, auCode, cNo, cName, crdAmount;
    TextView cardNo, Name, ExpDate;
    Button updateCard, cancel;
    String mnth = "", yer = "", remarks_card, AuCode, cardno, cardname, issuingbank, swipingid, tempTotal = "0", cardTotal = "0.0", accquringbank = "",
            CardSave, tempBank = "", exp_Date = "";
    private SharedPreferences prefs, prefsD;
    private SharedPreferences.Editor editor;
    Gson gson;
    PaymentList paymentList;
    private double total = 0.0;
    private CustomAdapter customAdapter;
    ImageButton autoFill;
    private List<String> bList;
    Spinner accBank;
    String[] bankNames = {"HDFC BANK", "AXIS BANK",
            "SBI BANK", "CANARA BANK",
            "IOB BANK", "ESAF BANK"};


    public Card_dialog_fragment() {
        // Required empty public constructor
    }

    private void addBankList() {
        bList = new ArrayList<>();
        bList.add("Select Bank");
        bList.add("HDFC BANK");
        bList.add("AXIS BANK");
        bList.add("SBI BANK");
        bList.add("CANARA BANK");
        bList.add("IOB BANK");
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);
        prefs = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        prefsD = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        addBankList();
        tempTotal = prefs.getString("billTotal", "200");
        CardSave = prefsD.getString("cardSave", "");

        System.out.println("Saved Data is  " + CardSave);

        remarks = view.findViewById(R.id.card_remarks);
        autoFill = view.findViewById(R.id.autoFill);
        expiry_month = view.findViewById(R.id.expiry_month);
        expiry_year = view.findViewById(R.id.expiry_year);
        smID = view.findViewById(R.id.smID);
        iB = view.findViewById(R.id.iB);
        auCode = view.findViewById(R.id.auCode);
        cName = view.findViewById(R.id.cName);
        cNo = view.findViewById(R.id.cNo);
        Name = view.findViewById(R.id.Name);
        cardNo = view.findViewById(R.id.cardNo);
        updateCard = view.findViewById(R.id.updateCard);
        ExpDate = view.findViewById(R.id.Expdate);
        crdAmount = view.findViewById(R.id.crdAmount);
        cancel = view.findViewById(R.id.cancel);
        accBank = view.findViewById(R.id.accBank);
        // CustomAdapter customAdapter = new CustomAdapter(getContext(), bankNames);
        CustomAdapter customAdapter = new CustomAdapter(getContext(), bList);
        accBank.setAdapter(customAdapter);
     /*   editor = prefs.edit();
        editor.remove("cardSave");
        editor.apply();*/
        if (!CardSave.isEmpty()) {
            try {
                gson = new Gson();
                PaymentList paymentList = gson.fromJson(CardSave, PaymentList.class);
                crdAmount.setText(paymentList.cardAmount);
                auCode.setText(paymentList.auCode);
                cardNo.setText(paymentList.cardNo.replace("x", ""));
                cName.setText(paymentList.cardName);
                cNo.setText(paymentList.cardNo);
                iB.setText(paymentList.issuingBank);
                smID.setText(paymentList.swipingMachineId);
                expiry_year.setText(paymentList.expiryYear);
                expiry_month.setText(paymentList.expiryMonth);
                remarks.setText(paymentList.cardRemarks);

                for (int i = 0; i < bList.size(); i++) {
                    tempBank = paymentList.accquringBank;
                    if (bList.get(i).equalsIgnoreCase(tempBank)) {
                        System.out.println("selection found" + i);
                        accBank.setSelection(i);
                    } else {
                        System.out.println("Not found");
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //autoFill();
        crdAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 2)});
        TextWatcher textWatcher = new CreditCardNumberTextWatcher(this.cNo, this.cardNo);
        this.cNo.addTextChangedListener(textWatcher);


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        expiry_month.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    int month = Integer.parseInt(editable.toString());
                    if (month > 12) {
                        expiry_month.setError("Please check Expiry Month");
                        updateCard.setEnabled(false);
                    } else if (month < 1) {
                        expiry_month.setError("Please check Expiry Month");
                        updateCard.setEnabled(false);
                    } else if (month < 10) {
                        updateCard.setEnabled(true);
                        mnth = "0" + String.valueOf(month);

                       /* if (yer.isEmpty()) {
                            ExpDate.setText(mnth + "/YY");
                        } else {
                            ExpDate.setText(mnth + "/" + yer);
                        }*/
                    } else {
                        updateCard.setEnabled(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        expiry_year.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    int year = Integer.parseInt(editable.toString());
                    if (year < 0) {
                        expiry_year.setError("Invalid Year");
                        updateCard.setEnabled(false);
                    } else if (editable.length() < 2) {
                        expiry_year.setError("Invalid Year");
                        updateCard.setEnabled(false);
                    } else {
                        yer = editable.toString();
                        updateCard.setEnabled(true);
                    /*    if (mnth.isEmpty()) {
                            ExpDate.setText("MM/" + yer);
                        } else {
                            ExpDate.setText(mnth + "/" + yer);
                        }*/
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


        updateCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AuCode = auCode.getText().toString().trim();
                cardname = cName.getText().toString().trim();
                cardno = cardNo.getText().toString();
                cardTotal = crdAmount.getText().toString().trim();
                issuingbank = iB.getText().toString().trim();
                swipingid = smID.getText().toString().trim();
                remarks_card = remarks.getText().toString();
                exp_Date = ExpDate.getText().toString().trim();
                int count = cNo.getText().toString().replace("\t", "").length();
                System.out.println(count);
                // expiry_month.setText(mnth);
                //expiry_year.setText(yer);

                try {
                    tempTotal = ((PaymentMenu) getActivity()).billAmt.getText().toString();
                    if (!tempTotal.isEmpty()) {
                        total = Double.parseDouble(tempTotal);
                       /* if (count < 16) {
                            popUp("Invalid Credit Card Number");
                        } else if (Double.parseDouble(cardTotal) > total) {
                            popUp("Please Check the amount again..!!");
                        } else if (cardTotal.isEmpty()) {
                            popUp("Amount field empty..!!");
                        } else if (accquringbank.equalsIgnoreCase("Select Bank")) {
                            popUp("Select Accquring Bank..");
                        } else if (AuCode.isEmpty() || cardname.isEmpty() || cardno.isEmpty() || issuingbank.isEmpty() || swipingid.isEmpty()) {
                            popUp("Empty Fields, check the form again..!!!");
                        } else if (expiry_year.getText().toString().isEmpty() || expiry_month.getText().toString().isEmpty()) {
                            popUp("Expiry Month/Year Empty..!!!");
                        }*/if (cardTotal.isEmpty()){
                            popUp("Amount field empty..!!");
                        }else {
                            gson = new Gson();
                            paymentList = new PaymentList();
                            paymentList.cardNo = cardno.replace("\t", "");
                            paymentList.cardAmount = cardTotal;
                            paymentList.cardName = cName.getText().toString().trim();
                            paymentList.issuingBank = issuingbank;
                            paymentList.swipingMachineId = swipingid;
                            paymentList.cardRemarks = remarks_card;
                            paymentList.expiryYear = yer;
                            paymentList.accquringBank = accquringbank;
                            paymentList.auCode = AuCode;
                            paymentList.expiryMonth = mnth;

                            String cardSave = gson.toJson(paymentList);
                            editor = prefs.edit();
                            editor.putString("cardSave", cardSave);
                            editor.apply();
                            System.out.println("Card Save is " + cardSave);
                            ((PaymentMenu) getActivity()).updateCardAmount(cardTotal,cardno);
                            ((PaymentMenu) getActivity()).updateList(cardTotal, "user_card",cardno);
                            dismiss();
                        }
                    } else {
                        dismiss();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

        accBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println(bList.get(i));
                accquringbank = bList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        autoFill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempTotal = ((PaymentMenu) getActivity()).billAmt.getText().toString();
                double temp = 0.0;
                temp = Double.parseDouble(((PaymentMenu) getActivity()).cashAmt.getText().toString());
                System.out.println("current card amount is " + temp);
                if (temp == 0.0) {
                    crdAmount.setText(tempTotal);
                } else {
                    double bal = 0.0;
                    bal = Double.parseDouble(tempTotal) - Double.parseDouble(((PaymentMenu) getActivity()).cashAmt.getText().toString());
                    crdAmount.setText(String.format("%.2f", bal));
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
 /*   private class DecimalDigitsInputFilter implements InputFilter {
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
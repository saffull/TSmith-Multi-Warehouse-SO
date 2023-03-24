package com.techsmith.mw_so;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.techsmith.mw_so.payment_util.CreditCardNumberTextWatcher;

import java.util.Locale;


public class SecondFragment extends Fragment {

    TextInputEditText remarks, expiry_month, expiry_year, smID, iB, auCode, cNo, cName,crdAmount;
    TextView cardNo, Name, ExpDate;
    Button updateCard;
    String mnth = "", yer = "", remarks_card, AuCode, cardno, cardname, issuingbank, swipingid, tempTotal,cardTotal="";
    private  SharedPreferences prefs;
    private double total=0.0;

    public SecondFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_second, container, false);
        prefs = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        tempTotal = prefs.getString("billTotal", "200");
        System.out.println("Total amount is "+tempTotal);
        remarks = view.findViewById(R.id.card_remarks);
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
        crdAmount=view.findViewById(R.id.crdAmount);

        TextWatcher textWatcher = new CreditCardNumberTextWatcher(this.cNo, this.cardNo);
        this.cNo.addTextChangedListener(textWatcher);

        cName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Name.setText(charSequence.toString().toUpperCase(Locale.ROOT));
            }

            @Override
            public void afterTextChanged(Editable editable) {

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
                    } else if (month < 1) {
                        expiry_month.setError("Please check Expiry Month");
                    } else {
                        mnth = editable.toString();
                        if (yer.isEmpty()) {
                            ExpDate.setText(mnth + "/yy");
                        } else {
                            ExpDate.setText(mnth + "/" + yer);
                        }
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
                    } else if (editable.length() < 2) {
                        expiry_year.setError("Invalid Year");
                    } else {
                        yer = editable.toString();
                        if (mnth.isEmpty()) {
                            ExpDate.setText("MM/" + yer);
                        } else {
                            ExpDate.setText(mnth + "/" + yer);
                        }
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
                cardTotal=crdAmount.getText().toString().trim();
                issuingbank = iB.getText().toString().trim();
                swipingid = smID.getText().toString().trim();
                remarks_card = remarks.getText().toString();

                try {
                    total=Double.parseDouble(tempTotal);
                    if (Double.parseDouble(cardTotal)>total){
                        Toast.makeText(getActivity(), "Please Check the amount again..!!", Toast.LENGTH_SHORT).show();
                    }else if (cardTotal.isEmpty()){
                        Toast.makeText(getActivity(), "Amount field empty", Toast.LENGTH_SHORT).show();
                    }else{
                        ((PaymentActivity)getActivity()).updateCardAmount(cardTotal);
                    }

                }catch (Exception e){e.printStackTrace();}



            }
        });

        return view;
    }

    private Boolean validateDate(TextInputEditText expiry_Date) {
        Boolean flag = true;
        if (expiry_Date.getText().toString().isEmpty()) {
            expiry_Date.setError("Expiry cannot be empty. Format: MM/YY");
            Toast.makeText(getActivity(), "Expiry cannot be empty. Format: MM/YY", Toast.LENGTH_SHORT).show();
            flag = false;
        } else if (expiry_Date.getText().toString().length() < 5) {
            expiry_Date.setError("Please check Card expiry & try again");
            Toast.makeText(getActivity(), "Please check Card expiry & try again", Toast.LENGTH_SHORT).show();
            flag = false;
        } else if (expiry_Date.getText().toString().matches("(?:0[1-9]|1[0-2])/[0-9]{2}")) {
            expiry_Date.setError("Please check Card expiry format & try again");
            Toast.makeText(getActivity(), "Please check Card expiry format & try again", Toast.LENGTH_SHORT).show();
            flag = false;
        } else {
            flag = true;
        }

        return flag;
    }
}
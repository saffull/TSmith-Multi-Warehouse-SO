package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import java.util.UUID;

public class RetailCustomerInformation extends AppCompatActivity {
    AutoCompleteTextView acvLcardNo,acvmobileNo,acvCustomerName;
    SharedPreferences prefs;
    EditText etCustomerAdrs,place,pincode,gstno,cardType;
    Button btnCreateSO;
    String loginResponse, Url, strCustomer, strErrorMsg, strReceivables, strReceivableDetails, uniqueID,
            selectedCustomerName, multiSOStoredDevId, user_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retail_customer_information);
        prefs = PreferenceManager.getDefaultSharedPreferences(RetailCustomerInformation.this);
        etCustomerAdrs=findViewById(R.id.etCustomerAdrs);
        place=findViewById(R.id.place);
        pincode=findViewById(R.id.pincode);
        gstno=findViewById(R.id.gstno);
        acvLcardNo=findViewById(R.id.acvLcardNo);
        acvmobileNo=findViewById(R.id.acvmobileNo);
        acvCustomerName=findViewById(R.id.acvCustomerName);
        cardType=findViewById(R.id.cardType);

        Url = prefs.getString("MultiSOURL", "");
        uniqueID = UUID.randomUUID().toString();



    }

    public void ClearAll(View view) {
    }

    public void CreateSO(View view) {
    }

    public void SearchItemByLcard(View view) {
    }

    public void SearchItemByMobile(View view) {
    }

    public void SearchItemByName(View view) {
    }
}
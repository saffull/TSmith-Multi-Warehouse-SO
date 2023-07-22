package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.techsmith.mw_so.Global.AppWide;
import com.techsmith.mw_so.retail_utils.AutoCompleteRetailSalesReturnAdapter;
import com.techsmith.mw_so.retail_utils.RetailCustomerResponse;
import com.techsmith.mw_so.retail_utils.RetailReplyData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class RetailSRBillActivity extends AppCompatActivity {
    AutoCompleteTextView acvBillNo;
    ImageButton imgBtnBillByNo;
    AutoCompleteTextView acvLcardNo, acvmobileNo, acvCustomerName;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    EditText place, pincode, gstno, cardType, etCustomerGoogleAdrs, cEmail, latLong;
    String Url, strCustomer, strErrorMsg, editData = "", LoyaltyCardType = "", customerAddress = "", sendTestData = "",
            selectedCustomerName, LoyaltyCode = "", LoyaltyId = "", LoyaltyCardTypeDesc = "", cLatitude = "",
            cLongitude = "", strfromweb = "", strerrormsg = "", uniqueID = "";
    ProgressDialog pDialog;
    RetailCustomerResponse customerResponse;
    AppWide appWide;
    AutoCompleteRetailSalesReturnAdapter myAdapter;
    RetailReplyData rcData;
    Gson gson;
    int currenPos;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retail_srbill);
        prefs = PreferenceManager.getDefaultSharedPreferences(RetailSRBillActivity.this);
        appWide = AppWide.getInstance();
        imgBtnBillByNo = findViewById(R.id.imgBtnBillByNo);
        acvBillNo = findViewById(R.id.acvBillNo);
        Url = prefs.getString("MultiSOURL", "");
        uniqueID = UUID.randomUUID().toString();
    }

    public void gotoSRList(View view) {
        startActivity(new Intent(RetailSRBillActivity.this, RetailSalesReturnActivity.class));
    }

    private class TakeBillDetails extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSRBillActivity.this);
            pDialog.setMessage("Fetching Bill Data....");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                URL url = new URL(Url + "getcustomerlookup?name=" + editData);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setRequestProperty("authkey", appWide.getAuthID());
                connection.setRequestProperty("machineid", appWide.getMachineId());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();

                int responsecode = connection.getResponseCode();
                String responseMsg = connection.getResponseMessage();

                try {

                    if (responsecode == 200) {
                        InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                        BufferedReader reader = new BufferedReader(streamReader);
                        StringBuilder sb = new StringBuilder();
                        String inputLine = "";

                        while ((inputLine = reader.readLine()) != null) {
                            sb.append(inputLine);
                            break;
                        }

                        reader.close();
                        strCustomer = sb.toString();

                        System.out.println("Response of Customer--->" + strCustomer);
                    } else {
//                        strErrorMsg = connection.getResponseMessage();
                        strErrorMsg = responseMsg;
                        strCustomer = "httperror";
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    connection.disconnect();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            return strCustomer;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();
            System.out.println("Bill Fetch Response is " + s);
            if (strCustomer == null || strCustomer.equals("")) {

                Toast.makeText(RetailSRBillActivity.this, "No result from web", Toast.LENGTH_SHORT).show();

            } else {

            }
        }
    }
}
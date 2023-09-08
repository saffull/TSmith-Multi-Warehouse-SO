package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.techsmith.mw_so.Global.AppWide;
import com.techsmith.mw_so.retailSRReturns.RetrieveProductSO;
import com.techsmith.mw_so.retail_utils.AutoCompleteRetailSalesReturnAdapter;
import com.techsmith.mw_so.Retail_Customer_utils.RetailCustomerResponse;
import com.techsmith.mw_so.retail_utils.RetailReplyData;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class RetailSRBillCustomerInfoActivity extends AppCompatActivity {
    AutoCompleteTextView acvBillNo;
    ImageButton imgBtnBillByNo;
    AutoCompleteTextView acvLcardNo, acvmobileNo, acvCustomerName;
    SharedPreferences prefs, prefsD;
    SharedPreferences.Editor editor, editor1;
    EditText place, pincode, gstno, cardType, etCustomerGoogleAdrs, cEmail, latLong, state;
    String Url, strCustomer = "", strErrorMsg, editData = "", LoyaltyCardType = "", customerAddress = "", sendTestData = "",
            selectedCustomerName, LoyaltyCode = "", LoyaltyId = "", LoyaltyCardTypeDesc = "", cLatitude = "",
            cLongitude = "", strfromweb = "", strerrormsg = "", uniqueID = "";
    ProgressDialog pDialog;
    RetailCustomerResponse customerResponse;
    AppWide appWide;
    AutoCompleteRetailSalesReturnAdapter myAdapter;
    RetailReplyData rcData;
    Gson gson;
    public ListView lvProductlist;
    int currenPos;
    Button btnCreateSO;
    Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retail_srbill);
        prefs = PreferenceManager.getDefaultSharedPreferences(RetailSRBillCustomerInfoActivity.this);
        appWide = AppWide.getInstance();
        imgBtnBillByNo = findViewById(R.id.imgBtnBillByNo);
        acvBillNo = findViewById(R.id.acvBillNo);
        acvCustomerName = findViewById(R.id.acvCustomerName);
        acvmobileNo = findViewById(R.id.acvmobileNo);
        cEmail = findViewById(R.id.cEmail);
        place = findViewById(R.id.place);
        pincode = findViewById(R.id.pincode);
        state = findViewById(R.id.state);
        btnCreateSO = findViewById(R.id.btnCreateSO);
        etCustomerGoogleAdrs = findViewById(R.id.etCustomerGoogleAdrs);
        Url = prefs.getString("MultiSOURL", "");
        uniqueID = UUID.randomUUID().toString();

        editor = prefs.edit();
        editor.putString("SRDOCGUID", uniqueID);
        editor.putString("SRCURRENTGUID", uniqueID);
        editor.apply();

        if (!prefs.getString("billNo", "").isEmpty())
            acvBillNo.setText(prefs.getString("billNo", ""));


        prefsD = getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor1 = prefsD.edit();
        editor1.putString("cashSave", "");
        editor1.putString("cardSave", "");
        editor1.apply();

    }


    public void gotoSRList(View view) {
        editor = prefs.edit();
        editor.putString("billNo", acvBillNo.getText().toString());
        editor.apply();
        startActivity(new Intent(RetailSRBillCustomerInfoActivity.this, RetailSalesReturnActivity.class));
    }

    public void TakeBillDetails(View view) {
        new TakeBillDetails().execute();
    }

    public void CLearALL(View view) {
        acvCustomerName.setText("");
        acvmobileNo.setText("");
        cEmail.setText("");
        place.setText("");
        pincode.setText("");
        state.setText("");
        etCustomerGoogleAdrs.setText("");
        btnCreateSO.setEnabled(false);
        btnCreateSO.setAlpha(0.8f);
    }

    private class TakeBillDetails extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSRBillCustomerInfoActivity.this);
            pDialog.setMessage("Fetching Bill Data....");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                JSONObject object = new JSONObject();
                object.put("StoreCode", "2021-DLF-PH1");
                object.put("SubStoreCode", "MAIN");
                object.put("UserId", "1");
                object.put("CounterId", "1");
                object.put("CustType", "1");

                JSONObject object1 = new JSONObject();
                object1.put("BILLNO", acvBillNo.getText().toString());


                URL url = new URL(Url + "GetSBillDetailsForSR");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setRequestProperty("authkey", appWide.getAuthID());
                connection.setRequestProperty("machineid", appWide.getMachineId());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("inputxmlstd", object.toString());
                connection.connect();
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(object1.toString());
                wr.flush();
                wr.close();

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

                        // System.out.println("Response of Customer--->" + strCustomer);
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
                Toast.makeText(RetailSRBillCustomerInfoActivity.this, "No result from web", Toast.LENGTH_SHORT).show();

            } else {
                try {
                    gson = new Gson();
                    RetrieveProductSO sop = gson.fromJson(strCustomer, RetrieveProductSO.class);
                    if (sop.STATUSFLAG == 0) {
                        btnCreateSO.setAlpha(1f);
                        btnCreateSO.setEnabled(true);
                        System.out.println(sop.DATA.SALESBILL.CUSTOMERDETAIL.CUSTOMER);
                        acvCustomerName.setText(sop.DATA.SALESBILL.CUSTOMERDETAIL.CUSTOMER);
                        acvmobileNo.setText(sop.DATA.SALESBILL.CUSTOMERDETAIL.MOBILENO);
                        place.setText(sop.DATA.SALESBILL.CUSTOMERDETAIL.AREA);
                        if (sop.DATA.SALESBILL.CUSTOMERDETAIL.PINCODE != 0)
                            pincode.setText(String.valueOf(sop.DATA.SALESBILL.CUSTOMERDETAIL.PINCODE));
                        etCustomerGoogleAdrs.setText(sop.DATA.SALESBILL.CUSTOMERDETAIL.ADDRESS);

                    } else {
                        tsMessages(sop.ERRORMESSAGE);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(RetailSRBillCustomerInfoActivity.this,SoMenu.class));
    }

    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(RetailSRBillCustomerInfoActivity.this);
            dialog.setContentView(R.layout.ts_message_dialouge);
//            dialog.setCanceledOnTouchOutside(false);
            dialog.setCanceledOnTouchOutside(true);
//            dialog.setTitle("Save");
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            ImageButton imgBtnCloseSaveWindow = dialog.findViewById(R.id.imgBtnClosetsMsgWindow);
            TextView tvMsgTodisplay = dialog.findViewById(R.id.tvTsMessageDisplay);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.gravity = Gravity.CENTER;
            dialog.getWindow().setAttributes(lp);

            imgBtnCloseSaveWindow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            tvMsgTodisplay.setText(msg);
            dialog.show();
        } catch (Exception ex) {
            Toast.makeText(this, "" + ex, Toast.LENGTH_SHORT).show();
        }

    }
}
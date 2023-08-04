package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.techsmith.mw_so.Global.AppWide;
import com.techsmith.mw_so.retailSRReturns.ITEM;
import com.techsmith.mw_so.retailSRReturns.RetailSRActivityAdapter;
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
import java.util.ArrayList;
import java.util.List;

public class RetailSalesReturnActivity extends AppCompatActivity {
    ImageButton imgBtnCustSearchbyName;
    AutoCompleteTextView acvLcardNo, acvmobileNo, acvCustomerName;
    List<AutoCompleteTextView> autoCompleteTextViewList;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout linearLayoutBSheet;
    private ToggleButton tbUpDown;
    RetrieveProductSO sop;
    Button btnSaveSR;
    EditText place, pincode, gstno, cardType, etCustomerGoogleAdrs, cEmail, latLong;
    String Url, strCustomer, strErrorMsg, billNo = "", LoyaltyCardType = "", customerAddress = "";
    ProgressDialog pDialog;
    RetailCustomerResponse customerResponse;
    AppWide appWide;
    AutoCompleteRetailSalesReturnAdapter myAdapter;
    RetailReplyData rcData;
    Gson gson;
    public ArrayList<ITEM> sList;
    public ListView lvProductlist;
    public Double productTotal;
    public TextView tvAmountValue, tvCustomerName, tvDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retail_sales_return);
        prefs = PreferenceManager.getDefaultSharedPreferences(RetailSalesReturnActivity.this);
        init();
        appWide = AppWide.getInstance();
        imgBtnCustSearchbyName = findViewById(R.id.imgBtnCustSearchbyName);
        acvCustomerName = findViewById(R.id.acvCustomerName);
        acvmobileNo = findViewById(R.id.acvmobileNo);
        lvProductlist = findViewById(R.id.lvProductlist);
        etCustomerGoogleAdrs = findViewById(R.id.etCustomerGoogleAdrs);
        tvDate = findViewById(R.id.tvDate);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        Url = prefs.getString("MultiSOURL", "");
        billNo = prefs.getString("billNo", "");
        System.out.println(billNo);
        if (!billNo.isEmpty()) {
            new TakeBillDetails().execute();
        }

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
        btnSaveSR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSR();
            }
        });


    }

    private void saveSR() {// Repace
        String[] parts = tvAmountValue.getText().toString().split("\\.");
        String part1 = parts[0]; // 004
        String part2 = parts[1]; // 034556

        sop.DATA.SALESBILL.DETAIL.ITEM = sList;
        System.out.println(sop);


    }

    private void init() {
        this.linearLayoutBSheet = findViewById(R.id.bottomSheetSalesReturn);
        this.bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBSheet);
        this.tbUpDown = findViewById(R.id.toggleButton);
        this.tvAmountValue = findViewById(R.id.tvAmountValue);
        this.btnSaveSR = findViewById(R.id.btnSaveSR);
    }

    public void showInfo(View view) {
    }

    public void ResetList(View view) {
        lvProductlist.setAdapter(null);
        new TakeBillDetails().execute();
    }

    public void showDialog(View view) {
        try {
            gson = new Gson();
            sop = gson.fromJson(strCustomer, RetrieveProductSO.class);
            if (sop.STATUSFLAG == 0) {
                tvCustomerName.setText(sop.DATA.SALESBILL.CUSTOMERDETAIL.CUSTOMER);
               String msg="Name: "+sop.DATA.SALESBILL.CUSTOMERDETAIL.CUSTOMER+
                       "\nBillNo: "+billNo+"\nDOCGUID: "+sop.DATA.SALESBILL.SUMMARY.DOCGUID;
               tsMessages(msg);
            } else {
                tsMessages(sop.ERRORMESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class TakeBillDetails extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSalesReturnActivity.this);
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
                object1.put("BILLNO", billNo);


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
                Toast.makeText(RetailSalesReturnActivity.this, "No result from web", Toast.LENGTH_SHORT).show();

            } else {
                try {
                    gson = new Gson();
                    sop = gson.fromJson(strCustomer, RetrieveProductSO.class);
                    if (sop.STATUSFLAG == 0) {
                        tvCustomerName.setText(sop.DATA.SALESBILL.CUSTOMERDETAIL.CUSTOMER);
                        tvDate.setText("BillNo: " + billNo);
                        sList = sop.DATA.SALESBILL.DETAIL.ITEM;
                        RetailSRActivityAdapter arrayAdapter = new RetailSRActivityAdapter(RetailSalesReturnActivity.this, R.layout.list_row, sList);
                        lvProductlist.setAdapter(arrayAdapter);
                        arrayAdapter.notifyDataSetChanged();
                        double d=0.0;
                        for (int i = 0; i <sop.DATA.SALESBILL.DETAIL.ITEM.size() ; i++) {
                             d = d+sop.DATA.SALESBILL.DETAIL.ITEM.get(i).LINETOTAL;
                        }
                       // double d = Double.parseDouble(sop.DATA.SALESBILL.DETAIL.ITEM.get());
                        tvAmountValue.setText(String.valueOf(d));
                    } else {
                        tsMessages(sop.ERRORMESSAGE);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }


    public void GoBack(View view) {
        finish();
    }


    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getAddress(View view) {
        String msg = "Address: " + customerAddress;
        tsMessages(msg);
    }


    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(RetailSalesReturnActivity.this);
            dialog.setContentView(R.layout.ts_message_dialouge);
            dialog.setCanceledOnTouchOutside(true);
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
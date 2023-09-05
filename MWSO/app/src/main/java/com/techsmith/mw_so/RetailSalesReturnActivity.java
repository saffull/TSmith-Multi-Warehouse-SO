package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.techsmith.mw_so.Global.AppWide;
import com.techsmith.mw_so.payment_util.PaymentList;
import com.techsmith.mw_so.print_utils.PrintResponse;
import com.techsmith.mw_so.retailSRReturns.CUSTOMERDETAIL;
import com.techsmith.mw_so.retailSRReturns.DETAIL;
import com.techsmith.mw_so.retailSRReturns.ITEM;
import com.techsmith.mw_so.retailSRReturns.PAYMENT;
import com.techsmith.mw_so.retailSRReturns.RetailSRActivityAdapter;
import com.techsmith.mw_so.retailSRReturns.RetrieveProductSO;
import com.techsmith.mw_so.retailSRReturns.SRSaveSOResponse;
import com.techsmith.mw_so.retailSRReturns.SUMMARY;
import com.techsmith.mw_so.retailSRReturns.SaveSRBill;
import com.techsmith.mw_so.retail_utils.APIResponse;
import com.techsmith.mw_so.Retail_Customer_utils.RetailCustomerResponse;
import com.techsmith.mw_so.retail_utils.PAYDETAIL;
import com.techsmith.mw_so.retail_utils.PAYSUMMARY;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RetailSalesReturnActivity extends AppCompatActivity {
    ImageButton imgBtnCustSearchbyName;
    AutoCompleteTextView acvLcardNo, acvmobileNo, acvCustomerName;
    List<AutoCompleteTextView> autoCompleteTextViewList;
    SharedPreferences prefs, prefsD;
    SharedPreferences.Editor editor;
    PaymentList paymentList;
    PaymentList paymentCardList;
    private static final DecimalFormat df = new DecimalFormat("0.00");
    SimpleDateFormat dff = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout linearLayoutBSheet;
    private ToggleButton tbUpDown;
    RetrieveProductSO sop;
    public Button btnNew, btnPrint;
    Button btnSaveSR;
    EditText etCustomerGoogleAdrs;
    SaveSRBill sr;
    String Url, strCustomer, strfromweb, strerrormsg, strErrorMsg, billNo = "", customerAddress = "",
            cashAmount, cardAmount, totalData = "", DocGuid = "", CurrentGuid = "", strPrintBill = "";
    ProgressDialog pDialog;
    RetailCustomerResponse customerResponse;
    AppWide appWide;
    RetailSRActivityAdapter arrayAdapter;
    Gson gson;
    public ArrayList<ITEM> sList, itemArrayList;
    private Button paymentBtn;
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
        paymentBtn = findViewById(R.id.paymentBtn);
        etCustomerGoogleAdrs = findViewById(R.id.etCustomerGoogleAdrs);
        tvDate = findViewById(R.id.tvDate);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        Url = prefs.getString("MultiSOURL", "");
        billNo = prefs.getString("billNo", "");
        DocGuid = prefs.getString("SRDOCGUID", "");
        CurrentGuid = prefs.getString("SRCURRENTGUID", "");
        System.out.println(billNo);
        if (!billNo.isEmpty()) {
            new TakeBillDetails().execute();
        }
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor = prefs.edit();
                editor.putString("billTotal", "0.0");
                editor.putString("billCash", "0.0");
                editor.putString("billCard", "0.0");
                editor.apply();
                finish();
                startActivity(new Intent(RetailSalesReturnActivity.this, RetailSRBillCustomerInfoActivity.class));
            }
        });
        paymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String d = tvAmountValue.getText().toString();
                double dle = 0.0;
                if (!d.isEmpty())
                    dle = Double.parseDouble(d);
                else
                    dle = 0.0;

                if (dle > 0.0) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("billTotal", d);
                    System.out.println("total amount is " + d);
                    editor.apply();
                    startActivity(new Intent(RetailSalesReturnActivity.this, PaymentMenu.class));
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RetailSalesReturnActivity.this);
                    alertDialogBuilder.setMessage("No Items Found.....!!");
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

            }
        });
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrintSalesReturn();
            }
        });
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


    private void saveSR() {// Replace

        itemArrayList = new ArrayList<>();
        double totalSum = 0.0;
        try {
            PAYMENT payment = new PAYMENT();
            ArrayList<PAYDETAIL> paydetailList = new ArrayList<>();
            prefsD = getSharedPreferences("pref", Context.MODE_PRIVATE);
            String t1 = prefsD.getString("cashSave", "");
            String t2 = prefsD.getString("cardSave", "");

            gson = new Gson();
            if (!t1.isEmpty()) {
                paymentList = gson.fromJson(t1, PaymentList.class);
                cashAmount = paymentList.cashAmount;
                cardAmount = "0.00";
            }
            if (!t2.isEmpty()) {
                paymentCardList = gson.fromJson(t2, PaymentList.class);
                cardAmount = paymentCardList.cardAmount;
                cashAmount = "0.00";
            }


            System.out.println(cashAmount + "\t<------------->\t" + cardAmount);
            totalSum = totalSum + Double.parseDouble(cashAmount) + Double.parseDouble(cardAmount);
            if (cashAmount.equalsIgnoreCase("0.0") && cardAmount.equalsIgnoreCase("0.0")) {
                tsMessages("Add Payment");
            } else {
                PAYDETAIL pd = new PAYDETAIL();
                if (!t1.isEmpty()) {
                    pd.PAYTYPE = "CASH";
                    pd.AMOUNT = Float.parseFloat(paymentList.cashAmount);
                } else {
                    pd.PAYTYPE = "CASH";
                    pd.AMOUNT = 0;
                }

                paydetailList.add(pd);

                PAYDETAIL pdd = new PAYDETAIL();
                if (!t2.isEmpty()) {
                    pdd.PAYTYPE = "CARD";
                    pdd.AMOUNT = Float.parseFloat(paymentCardList.cardAmount);
                    pdd.CARDNAME = paymentCardList.accquringBank;
                    pdd.CARDNO = paymentCardList.cardNo;
                    pdd.AUTHORISATIONNO = paymentCardList.auCode;
                    pdd.CARDOWNER = paymentCardList.cardName;
                    pdd.CARDISSUEDBANK = paymentCardList.issuingBank;// paymentCardList.issuingBank
                    pdd.SWIPINGMACHINEID = paymentCardList.swipingMachineId;
                    pdd.CARDEXPIRY = paymentCardList.expiryMonth + "/" + paymentCardList.expiryYear;
                    paydetailList.add(pdd);
                } else {
                    pdd.AMOUNT = 0;
                    pdd.CARDNAME = "";
                    pdd.CARDNO = "";
                    pdd.AUTHORISATIONNO = "";
                    pdd.CARDOWNER = "";
                    pdd.CARDISSUEDBANK = "";// paymentCardList.issuingBank
                    pdd.SWIPINGMACHINEID = "";
                    pdd.CARDEXPIRY = "";
                }

            }


            PAYSUMMARY paysummary = new PAYSUMMARY();
            paysummary.BILLAMOUNT = Float.parseFloat(tvAmountValue.getText().toString());
            paysummary.PAIDAMOUNT = Float.parseFloat(tvAmountValue.getText().toString());


            payment.PAYSUMMARY = paysummary;
            payment.PAYDETAIL = paydetailList;
            double tempSum = 0.0;
            for (int i = 0; i < sList.size(); i++) {
                ITEM item = new ITEM();
                item.SALESBILLNO = billNo;
                item.SALESITEMROWID = Integer.parseInt(sList.get(i).LINEID);
                item.ITEMID = sList.get(i).ITEMID;
                item.ITEMCODE = sList.get(i).ITEMCODE;
                item.HSNCODE = sList.get(i).HSNCODE;
                item.BATCHCODE = sList.get(i).BATCHCODE;
                item.BATCHEXPIRY = sList.get(i).BATCHEXPIRY;
                item.BATCHID = sList.get(i).BATCHID;
                item.MRP = sList.get(i).MRP;
                item.RATE = sList.get(i).RATE;
                item.PACKQTY = sList.get(i).PACKQTY;
                item.DISCPER = sList.get(i).DISCPER;
                //item.TOTALDISCPERC = sList.get(i).DISCPER;
                if (Double.parseDouble(sList.get(i).DISCPER) > 0.0) {
                    double d = (Double.parseDouble(sList.get(i).PACKQTY) * Double.parseDouble(sList.get(i).RATE)) * Double.parseDouble(sList.get(i).DISCPER) / 100;
                    double tot = (Double.parseDouble(sList.get(i).PACKQTY) * Double.parseDouble(sList.get(i).RATE)) - d;
                    item.LINETOTAL = tot;
                } else {
                    item.LINETOTAL = Double.parseDouble(sList.get(i).PACKQTY) * Double.parseDouble(sList.get(i).RATE);
                }

                itemArrayList.add(item);
                tempSum = tempSum + item.LINETOTAL;
            }
            DETAIL detail = new DETAIL();
            detail.ITEM = itemArrayList;


            SUMMARY summary = new SUMMARY();
            CUSTOMERDETAIL CUSTOMERDETAILS = new CUSTOMERDETAIL();
            CUSTOMERDETAILS.CUSTOMER = sop.DATA.SALESBILL.CUSTOMERDETAIL.CUSTOMER;
            CUSTOMERDETAILS.LOYALTYCODE = sop.DATA.SALESBILL.CUSTOMERDETAIL.LOYALTYCODE;
            CUSTOMERDETAILS.LOYALTYID = sop.DATA.SALESBILL.CUSTOMERDETAIL.LOYALTYID;
            CUSTOMERDETAILS.ADDRESS = sop.DATA.SALESBILL.CUSTOMERDETAIL.ADDRESS;
            CUSTOMERDETAILS.MOBILENO = sop.DATA.SALESBILL.CUSTOMERDETAIL.MOBILENO;
            CUSTOMERDETAILS.AREA = sop.DATA.SALESBILL.CUSTOMERDETAIL.AREA;
            CUSTOMERDETAILS.PINCODE = sop.DATA.SALESBILL.CUSTOMERDETAIL.PINCODE;
            CUSTOMERDETAILS.STATE = sop.DATA.SALESBILL.CUSTOMERDETAIL.STATE;
            // CUSTOMERDETAILS.DOCGUID = sop.DATA.SALESBILL.CUSTOMERDETAIL.DOCGUID;


            Date c = Calendar.getInstance().getTime();
            String formattedDate = dff.format(c);
            //summary.BILLDATE = "17/08/2023";
            summary.BILLDATE = formattedDate;
            summary.BILLNO = sop.DATA.SALESBILL.SUMMARY.BILLNO;
            summary.REFNO = String.valueOf(sop.DATA.SALESBILL.SUMMARY.REFNO);
            summary.BILLTYPE = "CASH";
            summary.CUSTOMER = sop.DATA.SALESBILL.CUSTOMERDETAIL.CUSTOMER;
            summary.CCEID = sop.DATA.SALESBILL.SUMMARY.CCEID;
            summary.SUMMARYDISC = sop.DATA.SALESBILL.SUMMARY.SUMMARYDISC;
            summary.NOOFITEMS = sop.DATA.SALESBILL.SUMMARY.NOOFITEMS;
            summary.DOCGUID = DocGuid;
            summary.CURRENTGUID = CurrentGuid;
            summary.ROUNDOFF = Double.parseDouble(df.format(totalSum - tempSum));
            summary.NETAMOUNT = Math.round(totalSum);
            sr = new SaveSRBill();
            sr.CUSTOMERDETAIL = CUSTOMERDETAILS;
            sr.PAYMENT = payment;
            sr.DETAIL = detail;
            sr.SUMMARY = summary;
            String temp = gson.toJson(sr.CUSTOMERDETAIL);
            System.out.println(temp);
            totalData = gson.toJson(sr);
            System.out.println("Final Save Data is " + totalData);
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(RetailSalesReturnActivity.this);
            alertDialogBuilder.setMessage("Do you want to Update Retail Sales Bill..?");
            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    new SaveData().execute();
                }
            });
            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            android.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void init() {
        this.linearLayoutBSheet = findViewById(R.id.bottomSheetSalesReturn);
        this.bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBSheet);
        this.tbUpDown = findViewById(R.id.toggleButton);
        this.btnNew = findViewById(R.id.btnNew);
        this.tvAmountValue = findViewById(R.id.tvAmountValue);
        this.btnSaveSR = findViewById(R.id.btnSaveSR);
        this.btnPrint = findViewById(R.id.btnPrint);
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
                String msg = "Name: " + sop.DATA.SALESBILL.CUSTOMERDETAIL.CUSTOMER +
                        "\nBillNo: " + billNo + "\nLoyaltyCode: " + sop.DATA.SALESBILL.CUSTOMERDETAIL.LOYALTYCODE + "\nDOCGUID: " + sop.DATA.SALESBILL.SUMMARY.DOCGUID;
                tsMessages(msg);
            } else {
                tsMessages(sop.ERRORMESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetData(View view) {
        new TakeBillDetails().execute();
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
                        tvCustomerName.setText(sop.DATA.SALESBILL.CUSTOMERDETAIL.CUSTOMER + "\n" + sop.DATA.SALESBILL.CUSTOMERDETAIL.LOYALTYCODE);
                        tvDate.setText("BillNo: " + billNo);
                        sList = sop.DATA.SALESBILL.DETAIL.ITEM;
                        arrayAdapter = new RetailSRActivityAdapter(RetailSalesReturnActivity.this, R.layout.list_row, sList);
                        lvProductlist.setAdapter(arrayAdapter);
                        arrayAdapter.notifyDataSetChanged();
                        double d = 0.0;
                        for (int i = 0; i < sop.DATA.SALESBILL.DETAIL.ITEM.size(); i++) {
                            d = d + sop.DATA.SALESBILL.DETAIL.ITEM.get(i).LINETOTAL;
                        }
                        // double d = Double.parseDouble(sop.DATA.SALESBILL.DETAIL.ITEM.get());
                        tvAmountValue.setText(df.format(Math.round(d)));
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

    private class SaveData extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSalesReturnActivity.this);
            pDialog.setMessage("Saving Pre Data....");
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


                URL url = new URL(Url + "Save_RetailBill_SR");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(180000);
                connection.setRequestProperty("authkey", "SBRL1467-8950-4215-A5DC-AC04D7620B23");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("StoreCode", "2021-DLF-PH1");
                connection.setRequestProperty("SubStoreCode", "MAIN");
                connection.setRequestProperty("UserId", "1");
                connection.setRequestProperty("CounterId", "1");
                connection.setRequestProperty("CustType", "1");
                connection.setRequestProperty("inputxmlstd", object.toString());
                connection.connect();

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(totalData);
                wr.flush();
                wr.close();
                int responsecode = connection.getResponseCode();
                if (responsecode == 200) {
                    try {
                        InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                        BufferedReader reader = new BufferedReader(streamReader);
                        StringBuilder sb = new StringBuilder();
                        String inputLine = "";
                        while ((inputLine = reader.readLine()) != null) {
                            sb.append(inputLine);
                            break;
                        }

                        reader.close();
                        strfromweb = sb.toString();
                        System.out.println("SR Bill  Save Response is " + strfromweb);


                    } finally {
                        connection.disconnect();
                    }

                } else {
                    strerrormsg = connection.getResponseMessage();
                    strfromweb = "httperror";
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return strfromweb;
        }

        //
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();
            System.out.println("Save Data Response is " + s);
            try {
                gson = new Gson();
                //customerResponse = gson.fromJson(strCustomer, RetailCustomerResponse.class);
                SRSaveSOResponse apiResponse = gson.fromJson(s, SRSaveSOResponse.class);
                if (apiResponse.STATUSFLAG == 0) {
                    String msg = apiResponse.DATA.RESPONSE.MSG + "\nBill No: " + apiResponse.DATA.RESPONSE.BILLNO;
                    popUp(msg);
                    editor = prefsD.edit();
                    editor.putString("cashSave", "");
                    editor.putString("cardSave", "");
                    editor.putString("billNo", "");
                    editor.apply();

                } else {
                    tsMessages(apiResponse.ERRORMESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void PrintSalesReturn() {
        new PrintSR().execute();
    }

    private class PrintSR extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSalesReturnActivity.this);
            pDialog.setMessage("Fetching SR Data....");
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

                JSONObject jObject = new JSONObject();
                jObject.put("INVOICENO", "2021/23/SR-1");
                //jObject.put("INVOICENO","2021/23/S-58");
                jObject.put("FORMAT", "1");


                URL url = new URL(Url + "GetSalesReturnPrint");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(180000);
                connection.setRequestProperty("authkey", "SBRL1467-8950-4215-A5DC-AC04D7620B23");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("StoreCode", "2021-DLF-PH1");
                connection.setRequestProperty("SubStoreCode", "MAIN");
                connection.setRequestProperty("UserId", "1");
                connection.setRequestProperty("CounterId", "1");
                connection.setRequestProperty("CustType", "1");
                connection.setRequestProperty("inputxmlstd", object.toString());
                connection.connect();

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(jObject.toString());
                wr.flush();
                wr.close();
                int responsecode = connection.getResponseCode();
                if (responsecode == 200) {
                    try {
                        InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                        BufferedReader reader = new BufferedReader(streamReader);
                        StringBuilder sb = new StringBuilder();
                        String inputLine = "";
                        while ((inputLine = reader.readLine()) != null) {
                            sb.append(inputLine);
                            break;
                        }

                        reader.close();
                        strPrintBill = sb.toString();
                        System.out.println("Print SR Bill Response is " + strPrintBill);


                    } finally {
                        connection.disconnect();
                    }

                } else {
                    strerrormsg = connection.getResponseMessage();
                    strPrintBill = "httperror";
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return strPrintBill;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();

            try {
                gson = new Gson();
                //customerResponse = gson.fromJson(strCustomer, RetailCustomerResponse.class);
                PrintResponse apiResponse = gson.fromJson(s, PrintResponse.class);
                if (apiResponse.STATUSFLAG == 0) {
                    String msg = apiResponse.DATA.BILL;
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(RetailSalesReturnActivity.this);
                    alertDialogBuilder.setMessage("Print Sales Bill For " + prefs.getString("billNo", "") + "..?");
                    alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // dialog.cancel();
                            startPrint(msg);
                        }
                    });
                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();


                } else {
                    tsMessages(apiResponse.ERRORMESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void popUp(String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RetailSalesReturnActivity.this);
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

    public boolean isBluetoothEnabled() {
        BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return myBluetoothAdapter.isEnabled();
    }

    //Printing Function
    private void startPrint(String msg) {

        if (isBluetoothEnabled()) {
            try {
                //String temp = "Design and deploy business solutions that are relevant to the needs of the real India.";
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, 1);
                } else {
                    /*  "[L]" + df.format(new Date()) + "\n","[C]--------------------------------\n" + */
                    BluetoothConnection connection = BluetoothPrintersConnections.selectFirstPaired();
                    if (connection != null) {
                        EscPosPrinter printer = new EscPosPrinter(connection, 210, 48f, 32);
                        printer.printFormattedText(msg);

                        connection.disconnect();
                    } else {
                        //Toast.makeText(this, "No printer was connected...!", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RetailSalesReturnActivity.this);
                        alertDialogBuilder.setMessage("Check The Printer Status or Add Printer..!!");
                        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {

                            }
                        });
                        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();


                    }
                }
            } catch (Exception e) {
                Log.e("APP", "Can't print", e);
            }


        } else {
            popUp("Enable bluetooth and try again..!!!");
        }

    }
}
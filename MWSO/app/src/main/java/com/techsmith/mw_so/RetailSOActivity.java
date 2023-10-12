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
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.anggastudio.printama.Printama;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.techsmith.mw_so.Global.AppWide;
import com.techsmith.mw_so.Spinner.RetailCustomAdapter;
import com.techsmith.mw_so.payment_util.PaymentList;
import com.techsmith.mw_so.print_utils.EmailResponse;
import com.techsmith.mw_so.print_utils.PrintResponse;
import com.techsmith.mw_so.retail_utils.APIResponse;
import com.techsmith.mw_so.retail_utils.AutoCompleteRetailProductCustomAdapter;
import com.techsmith.mw_so.retail_utils.BatchRetailResponse;
import com.techsmith.mw_so.retail_utils.CUSTOMERDETAIL;
import com.techsmith.mw_so.retail_utils.ITEM;
import com.techsmith.mw_so.retail_utils.PAYDETAIL;
import com.techsmith.mw_so.retail_utils.PAYMENT;
import com.techsmith.mw_so.retail_utils.PAYSUMMARY;
import com.techsmith.mw_so.retail_utils.ProductRetailResponse;
import com.techsmith.mw_so.retail_utils.RetailReplyData;
import com.techsmith.mw_so.retail_utils.RetailSOActivityArrayAdapter;
import com.techsmith.mw_so.retail_utils.RetailSOSchemeArrayAdapter;
import com.techsmith.mw_so.retail_utils.SaveProductSO;
import com.techsmith.mw_so.retail_utils.SaveProductSOPL;
import com.techsmith.mw_so.retail_utils.Summary;
import com.techsmith.mw_so.scheme_reverse.SchemeReverseItem;
import com.techsmith.mw_so.scheme_reverse.SchemeReverseResponse;
import com.techsmith.mw_so.scheme_utils.SchemeResponse;
import com.techsmith.mw_so.utils.AllocateQty;
import com.techsmith.mw_so.utils.AllocateQtyPL;
import com.techsmith.mw_so.utils.ItemDetails;
import com.techsmith.mw_so.utils.ItemList;
import com.techsmith.mw_so.utils.UserPL;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RetailSOActivity extends AppCompatActivity {
    SharedPreferences prefs, prefsD;
    RetailSOActivityArrayAdapter arrayAdapter;
    private static final DecimalFormat decfor = new DecimalFormat("0.00");
    private BluetoothConnection selectedDevice;
    com.techsmith.mw_so.retail_utils.DETAIL detail;
    SimpleDateFormat dff = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    BatchRetailResponse pd;
    SaveProductSO sap;
    Context appContext;
    TextView tvSelectedItemName, tvSelectedBatchcode, tvSelectedBatchID, tvSelectedItemCode, tvItemSOH, tvSelectedItemExp, tvItemMRP, tvbMRP;
    Button caculate;
    EditText etQty;
    ArrayList<com.techsmith.mw_so.scheme_utils.ITEM> schemeList;
    String loginResponse, filter = "", Url = "", strGetItems, strErrorMsg, strRevertScheme = "", billRemarks = "", itemExpiry,
            strGetItemDetail, strPrintBill = "", strProductBatch, itemName, strSchemeResponse = "", itemCode, sendSchemeData = "", strCheckLogin, LoyaltyID = "", LoyaltyCode = "", itemMRP = "", itemRate = "",
            multiSOStoredDevId = "", uniqueId, strfromweb, strerrormsg, DocGuid = "", customer_Details, CurrentGuid = "";
    public Double itemSoh, total = 0.0, totalSOH;// item made public so that to access in its adapter class
    public String itemMrp, ptotal = "", formedSO = "", pRate = "", tempTotal = "", subStoreId = "", StoreId = "", cardAmount = "0.0", cashAmount = "0.0",
            cceId = "", sendTestData = "", roundingTotal = "No";// item made public so that to access in its adapter class
    int itemId, FORMAT = 0;
    public int selectedPos;
    ImageButton ic_search, imgBtn;
    private ToggleButton tbUpDown;
    Gson gson;
    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    ItemList itemList;
    ItemDetails itemDetails;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout linearLayoutBSheet;
    public TextView tvAmountValue, discText, lineTotal, fullTotal; // item made public so that to access in its adapter class
    public ArrayList<AllocateQtyPL> detailProductList;
    public ArrayList<SaveProductSOPL> sList;
    public ArrayList<ITEM> itemArrayList;
    SaveProductSO saveProductSO;
    Boolean batchFlag = false;
    public Button btnAllClear, btnSave;
    EditText etAddRemarks;
    PaymentList paymentList;
    PaymentList paymentCardList;
    List<String> offerList, houseList, sohList, batchCode, batchExpiry;
    List<Double> batchMrp, batchRate, batchSOH;
    List<Integer> batchID;
    TextView tvCustomerName, tvDate, etReceivables;
    AutoCompleteTextView acvItemSearchSOActivity;
    ProgressDialog pDialog;
    ImageButton imgBtnRemarksPrescrptn, backBtn;
    Dialog qtydialog, dialog;
    UserPL userPLObj;
    public ListView lvProductlist;// item made public so that to access in its adapter class
    AllocateQty allocateQty;
    SharedPreferences.Editor editor;
    List<AllocateQtyPL> listSODetailPL;
    public ArrayList<AllocateQtyPL> detailList;
    HashMap<String, String> offerMap = new HashMap<>();
    FloatingActionButton mAddAlarmFab, mAddPersonFab, email_fab, invoice_fab, preview_fab;
    ExtendedFloatingActionButton mAddFab;
    TextView addAlarmActionText, addPersonActionText, email_text, MessageDisplay, invoice_text, preview_text;
    Boolean isAllFabsVisible;
    public Double productTotal = 0.0, payTotal = 0.0;
    private Button paymentBtn;
    JSONObject mObject;
    AppWide appWide;
    TextInputEditText discPer;
    private static final DecimalFormat df = new DecimalFormat("0.00");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retail_soactivity);
//        getSupportActionBar().hide();
        appWide = AppWide.getInstance();
        StoreId = String.valueOf(appWide.getStoreId());
        subStoreId = String.valueOf(appWide.getSubStoreId());
        appContext = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(RetailSOActivity.this);
        acvItemSearchSOActivity = findViewById(R.id.acvItemSearchSOActivity);
        mAddFab = findViewById(R.id.add_fab);
        mAddAlarmFab = findViewById(R.id.add_alarm_fab);
        mAddPersonFab = findViewById(R.id.add_person_fab);
        preview_fab = findViewById(R.id.preview_fab);
        MessageDisplay = findViewById(R.id.MessageDisplay);
        discText = findViewById(R.id.discText);
        lineTotal = findViewById(R.id.lineTotal);
        fullTotal = findViewById(R.id.fullTotal);
        lvProductlist = findViewById(R.id.lvProductlist);
        addAlarmActionText = findViewById(R.id.add_alarm_action_text);
        addPersonActionText = findViewById(R.id.add_person_action_text);
        preview_text = findViewById(R.id.preview_text);
        paymentBtn = findViewById(R.id.paymentBtn);
        imgBtn = findViewById(R.id.imgBtn);
        email_fab = findViewById(R.id.email_fab);
        email_text = findViewById(R.id.email_text);
        invoice_fab = findViewById(R.id.invoice_fab);
        invoice_text = findViewById(R.id.invoice_text);
        initializeFab();
        detailProductList = new ArrayList<>();
        appWide = AppWide.getInstance();
        sList = new ArrayList<>();
        schemeList = new ArrayList<>();
        itemArrayList = new ArrayList<>();
        tvCustomerName = findViewById(R.id.tvCustomerName);
        ic_search = findViewById(R.id.imgBtnSearchItem);
        etReceivables = findViewById(R.id.etReceivables);
        imgBtnRemarksPrescrptn = findViewById(R.id.imgBtnRemarksPrescrptn);
        tvDate = findViewById(R.id.tvDate);
        detailList = new ArrayList<>();
        //  loginResponse = prefs.getString("loginResponse", "");
        gson = new Gson();
        userPLObj = gson.fromJson(loginResponse, UserPL.class);
        Url = prefs.getString("MultiSOURL", "");
        multiSOStoredDevId = prefs.getString("MultiSOStoredDevId", "");
        uniqueId = prefs.getString("guid", "");
        roundingTotal = prefs.getString("roundingPermsn", "No");
        System.out.println("Rounding Permission Given" + roundingTotal);

        customer_Details = prefs.getString("customer_Details", "");
        cceId = prefs.getString("cceId", "");
        DocGuid = prefs.getString("DOCGUID", "");
        CurrentGuid = prefs.getString("CURRENTGUID", "");
        System.out.println("GUIDs are " + customer_Details);
        mAddPersonFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FORMAT = 1;
                new PrintBill().execute();

                //startPrint();
            }
        });
        try {
            Gson gson = new Gson();
            //gson.fromJson(loginResponse, UserPL.class);
            RetailReplyData rcData = new RetailReplyData();
            rcData = gson.fromJson(customer_Details, RetailReplyData.class);
            System.out.println(rcData.GoogleAddress);
            tvCustomerName.setText(rcData.NAME);
            LoyaltyID = String.valueOf(rcData.LOYALTYID);
            LoyaltyCode = rcData.LOYALTYCODE;


            // tvDate.setText(String.valueOf(customer_id));

            mObject = new JSONObject();
            mObject.put("StoreCode", "2021-DLF-PH1");
            mObject.put("SubStoreCode", "MAIN");
            mObject.put("UserId", "1");
            mObject.put("CounterId", "1");
            mObject.put("CustType", "1");

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Unique Id is " + uniqueId);

        acvItemSearchSOActivity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String temp = editable.toString();
                if (temp.length() == 0) {
                    acvItemSearchSOActivity.setAdapter(null);
                }

            }
        });
        acvItemSearchSOActivity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                String SelectedText = acvItemSearchSOActivity.getText().toString();
                if (SelectedText.length() >= 3) {
                    selectedPos = pos;
                    itemId = itemList.DATA.get(pos).PMID;
                    itemName = itemList.DATA.get(pos).PRODUCT;
                    itemCode = String.valueOf(itemList.DATA.get(pos).PID);
                    itemMrp = String.valueOf(itemList.DATA.get(pos).MRP);
                    itemSoh = itemList.DATA.get(pos).SOHINPACKS;
                    itemMRP = String.valueOf(itemList.DATA.get(pos).MRP);
                    //itemRate=itemList.data.get(pos).


                    if (!itemCode.isEmpty())
                        new GetProductDetailsTask().execute();
                } else {
                    acvItemSearchSOActivity.setAdapter(null);
                }
            }
        });
        email_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FORMAT = 2;
                new EmailBill().execute();
            }
        });
        preview_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RetailSOActivity.this);
                alertDialogBuilder.setMessage("Apply Schemes..?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        try {
                            sap = new SaveProductSO();
                            sap.DETAIL = detail;
                            RetailReplyData rcData = new RetailReplyData();
                            rcData = gson.fromJson(customer_Details, RetailReplyData.class);
                            Summary summary = new Summary();
                            CUSTOMERDETAIL CUSTOMERDETAIL = new CUSTOMERDETAIL();

                            Date c = Calendar.getInstance().getTime();
                            String formattedDate = dff.format(c);
                            summary.BILLDATE = formattedDate;
                            // summary.BILLDATE = "31/07/2023";
                            summary.BILLNO = "NEW";
                            summary.REFNO = "";
                            summary.BILLTYPE = "CASH";
                            summary.CUSTOMER = rcData.NAME;
                            summary.CCEID = cceId;
                            summary.SUMMARYDISC = 0;
                            summary.NOOFITEMS = sList.size();
                            summary.DOCGUID = prefs.getString("DOCGUID", "");
                            summary.CURRENTGUID = prefs.getString("CURRENTGUID", "");
                            if (Double.parseDouble(tvAmountValue.getText().toString()) > 0) {

                                double doubleNumber = Double.parseDouble(tvAmountValue.getText().toString());
                                String doubleAsString = String.valueOf(doubleNumber);
                                int indexOfDecimal = doubleAsString.indexOf(".");
                                System.out.println("Double Number: " + doubleNumber);
                                System.out.println("Integer Part: " + doubleAsString.substring(0, indexOfDecimal));
                                System.out.println("Decimal Part: " + doubleAsString.substring(indexOfDecimal));
                                double beforeRound = 0.0, afterRound = 0.0, diff = 0.0;
                                for (int i = 0; i < sap.DETAIL.ITEM.size(); i++) {
                                    beforeRound = beforeRound + sap.DETAIL.ITEM.get(i).LINETOTAL;
                                }
                                afterRound = Math.round(beforeRound);
                                diff = afterRound - beforeRound;
                                // summary.NETAMOUNT = Double.valueOf(doubleAsString.substring(0, indexOfDecimal));
                                summary.NETAMOUNT = afterRound;
                                //summary.ROUNDOFF = (Double.parseDouble(doubleAsString.substring(indexOfDecimal)));
                                summary.ROUNDOFF = Double.parseDouble(df.format(diff));

                            } else {
                                summary.ROUNDOFF = 0.0;
                                summary.NETAMOUNT = 0.0;
                            }

                            CUSTOMERDETAIL.CUSTOMER = rcData.NAME;
                            CUSTOMERDETAIL.LOYALTYCODE = LoyaltyCode;
                            CUSTOMERDETAIL.LOYALTYID = LoyaltyID;
                            CUSTOMERDETAIL.ADDRESS = (String) rcData.GoogleAddress;
                            if (rcData.PHONE1.isEmpty())
                                CUSTOMERDETAIL.MOBILENO = Long.parseLong(rcData.PHONE2);
                            else
                                CUSTOMERDETAIL.MOBILENO = Long.parseLong(rcData.PHONE1);
                            CUSTOMERDETAIL.AREA = rcData.AREA;
                            CUSTOMERDETAIL.PINCODE = rcData.PINCODE;
                            CUSTOMERDETAIL.STATE = rcData.STATE;


                            sap.SUMMARY = summary;
                            sap.CUSTOMERDETAIL = CUSTOMERDETAIL;
                            sendSchemeData = gson.toJson(sap);
                            System.out.println("Final Scheme Input json is " + sendSchemeData);

                            prefsD = getSharedPreferences("pref", Context.MODE_PRIVATE);
                            editor = prefsD.edit();
                            editor.putString("cashSave", "");
                            editor.putString("cardSave", "");
                            editor.apply();

                            new applyRetailScheme().execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();
            }
        });
        mAddAlarmFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (schemeList.size() > 0) {
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(RetailSOActivity.this);
                    alertDialogBuilder.setMessage("Do you want to Reset The Schemes Applied..?");
                    alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            new revertScheme().execute();
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
                    tsMessages("Scheme Not Applied..");
                }

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
                    startActivity(new Intent(RetailSOActivity.this, PaymentMenu.class));
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RetailSOActivity.this);
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
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (schemeList.size() == 0) {
                    System.out.println("Asset List Not found");
                    SaveRetailSo();
                } else {
                    System.out.println("Asset List found");
                    SaveRetailSchemeSo();
                }
            }
        });
        mAddFab.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isAllFabsVisible) {
                            mAddAlarmFab.show();
                            mAddPersonFab.show();
                            preview_fab.show();
                            addAlarmActionText
                                    .setVisibility(View.VISIBLE);
                            addPersonActionText
                                    .setVisibility(View.VISIBLE);
                            preview_text.setVisibility(View.VISIBLE);
                            email_fab.setVisibility(View.VISIBLE);
                            email_text.setVisibility(View.VISIBLE);
                            mAddFab.extend();
                            isAllFabsVisible = true;
                        } else {
                            mAddAlarmFab.hide();
                            mAddPersonFab.hide();
                            preview_fab.hide();
                            email_fab.hide();
                            email_text.setVisibility(View.GONE);
                            addAlarmActionText.setVisibility(View.GONE);
                            addPersonActionText.setVisibility(View.GONE);
                            preview_text.setVisibility(View.GONE);
                            mAddFab.shrink();
                            isAllFabsVisible = false;
                        }
                    }
                });

        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otherOptions();
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
    }


    private void SaveRetailSchemeSo() {
        try {
            addSchemeItem(schemeList, strSchemeResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SaveRetailSo() {
        System.out.println("Formed json is " + formedSO);

        try {
            sap = new SaveProductSO();
            sap.DETAIL = detail;
            PAYMENT payment = new PAYMENT();
            ArrayList<PAYDETAIL> paydetailList = new ArrayList<>();
            prefsD = getSharedPreferences("pref", Context.MODE_PRIVATE);
            String t1 = prefsD.getString("cashSave", "");
            String t2 = prefsD.getString("cardSave", "");

            gson = new Gson();
            if (!t1.isEmpty()) {
                paymentList = gson.fromJson(t1, PaymentList.class);
                cashAmount = paymentList.cashAmount;
            }
            if (!t2.isEmpty()) {
                paymentCardList = gson.fromJson(t2, PaymentList.class);
                cardAmount = paymentCardList.cardAmount;
            }


            System.out.println(cashAmount + "\t<------------->\t" + cardAmount);
            if (cashAmount.equalsIgnoreCase("0.0") && cardAmount.equalsIgnoreCase("0.0")) {
                tsMessages("Add Payment");
            } else {
                PAYDETAIL pd = new PAYDETAIL();
                if (!t1.isEmpty()) {
                    pd.PAYTYPE = "CASH";
                    //pd.AMOUNT = Float.parseFloat(paymentList.cashAmount);
                    pd.AMOUNT = Float.parseFloat(String.valueOf(Math.round(Double.parseDouble(paymentList.cashAmount))));
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
            paysummary.BILLAMOUNT =Math.round(Double.parseDouble(tvAmountValue.getText().toString()));
            paysummary.PAIDAMOUNT =Math.round( Double.parseDouble(tvAmountValue.getText().toString()));


            payment.PAYSUMMARY = paysummary;
            payment.PAYDETAIL = paydetailList;


            RetailReplyData rcData = new RetailReplyData();
            rcData = gson.fromJson(customer_Details, RetailReplyData.class);
            Summary summary = new Summary();
            CUSTOMERDETAIL CUSTOMERDETAIL = new CUSTOMERDETAIL();
            //  String uniqueID = UUID.randomUUID().toString();

            //summary.BILLDATE = response.DATA.OUTPUTXML.SUMMARY.BILLDATE;
            Date c = Calendar.getInstance().getTime();
            String formattedDate = dff.format(c);
            // summary.BILLDATE = "17/08/2023";
            summary.BILLDATE = formattedDate;
            summary.BILLNO = "NEW";
            summary.REFNO = "";
            summary.BILLTYPE = "CASH";
            summary.CUSTOMER = rcData.NAME;
            summary.CCEID = cceId;
            summary.SUMMARYDISC = 0;
            summary.NOOFITEMS = sList.size();
            summary.DOCGUID = prefs.getString("DOCGUID", "");
            summary.CURRENTGUID = prefs.getString("CURRENTGUID", "");
            if (Double.parseDouble(tvAmountValue.getText().toString()) > 0) {
                double doubleNumber = Double.parseDouble(tvAmountValue.getText().toString());
                String doubleAsString = String.valueOf(doubleNumber);
                int indexOfDecimal = doubleAsString.indexOf(".");
                System.out.println("Double Number: " + doubleNumber);
                System.out.println("Integer Part: " + doubleAsString.substring(0, indexOfDecimal));
                System.out.println("Decimal Part: " + doubleAsString.substring(indexOfDecimal));

             /*   String temp = tvAmountValue.getText().toString();
                String[] partsTemp = temp.split("\\.");
                String Temp1 = partsTemp[0]; // 004
                String Temp2 = partsTemp[1]; // 034556
                summary.NETAMOUNT = Double.parseDouble(Temp1);
                summary.ROUNDOFF = Double.parseDouble(Temp2) / 100;*/

                double beforeRound = 0.0, afterRound = 0.0, diff = 0.0;
                for (int i = 0; i < sap.DETAIL.ITEM.size(); i++) {
                    beforeRound = beforeRound + sap.DETAIL.ITEM.get(i).LINETOTAL;
                }
                afterRound = Math.round(beforeRound);
                diff = afterRound - beforeRound;

                summary.NETAMOUNT = afterRound;
                summary.ROUNDOFF = Double.parseDouble(df.format(diff));
                //summary.NETAMOUNT = Double.valueOf(doubleAsString.substring(0, indexOfDecimal));
                //summary.ROUNDOFF = (Double.parseDouble(doubleAsString.substring(indexOfDecimal)));

            } else {
                summary.ROUNDOFF = 0.0;
                summary.NETAMOUNT = 0.0;
            }


            CUSTOMERDETAIL.CUSTOMER = rcData.NAME;
            CUSTOMERDETAIL.LOYALTYCODE = LoyaltyCode;
            CUSTOMERDETAIL.LOYALTYID = LoyaltyID;
            CUSTOMERDETAIL.ADDRESS = (String) rcData.GoogleAddress;
            if (rcData.PHONE1.isEmpty())
                CUSTOMERDETAIL.MOBILENO = Long.parseLong(rcData.PHONE2);
            else
                CUSTOMERDETAIL.MOBILENO = Long.parseLong(rcData.PHONE1);
            CUSTOMERDETAIL.AREA = rcData.AREA;
            CUSTOMERDETAIL.PINCODE = rcData.PINCODE;
            CUSTOMERDETAIL.STATE = rcData.STATE;


            sap.SUMMARY = summary;
            sap.CUSTOMERDETAIL = CUSTOMERDETAIL;
            sap.PAYMENT = payment;
            sendTestData = gson.toJson(sap);
            System.out.println("Final Save json is " + sendTestData);
            if (sap.PAYMENT.PAYDETAIL.size() > 0) {

                android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(RetailSOActivity.this);
                alertDialogBuilder.setMessage("Do you want to Save Retail SO..?");
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

            } else {
                Toast.makeText(RetailSOActivity.this, "Payment Not Added", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(RetailSOActivity.this, "Data Forming Error..", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }


    }

    private void init() {
        this.linearLayoutBSheet = findViewById(R.id.bottomSheetPayment);
        this.bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBSheet);
        this.tbUpDown = findViewById(R.id.toggleButton);
        this.tvAmountValue = findViewById(R.id.tvAmountValue);
        this.btnAllClear = findViewById(R.id.btnAllClear);
        this.btnSave = findViewById(R.id.btnSaveSO);
    }

    private void otherOptions() {
        Rect displayRectangle = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.so_data_dialog, viewGroup, false);
        dialogView.setMinimumWidth((int) (displayRectangle.width() * .5f));
        dialogView.setMinimumHeight((int) (displayRectangle.height() * .5f));
        builder.setView(dialogView);
        alertDialog = builder.create();
        ImageView btnExit = dialogView.findViewById(R.id.btnExit);
        EditText controlInfo = dialogView.findViewById(R.id.controlInfo);
        try {
            AppWide appWide = AppWide.getInstance();

            String msg = " Store Name:" + appWide.getStoreName() + "\n Store Code:" + appWide.getStoreCode() +
                    "\n SubStore Id: " + appWide.getSubStoreId() + "\n Name: " + appWide.getName()
                    + "\n LoyaltyCode: " + appWide.getLoyaltyCode() + "\n DOCGUID: " + uniqueId + "\n" + "\nCURRENTGUID" + uniqueId + "\n";
            controlInfo.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
        try {
            alertDialog.setCancelable(false);
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeFab() {
        mAddAlarmFab.setVisibility(View.GONE);
        mAddPersonFab.setVisibility(View.GONE);
        preview_fab.setVisibility(View.GONE);
        email_fab.setVisibility(View.GONE);
        addAlarmActionText.setVisibility(View.GONE);
        addPersonActionText.setVisibility(View.GONE);
        preview_text.setVisibility(View.GONE);
        email_text.setVisibility(View.GONE);
        invoice_fab.setVisibility(View.GONE);
        invoice_text.setVisibility(View.GONE);
        isAllFabsVisible = false;
        mAddFab.shrink();
        init();
    }

    public void SearchItem(View view) {
        filter = acvItemSearchSOActivity.getText().toString();
        if (filter.length() >= 3) {
            new GetItemsTask().execute();
        } else {
            popUp("Add atleast 3 characters");
        }
    }

    public void ShowRemarks(View view) {
        try {
            prefs = PreferenceManager.getDefaultSharedPreferences(RetailSOActivity.this);
            billRemarks = prefs.getString("BillRemarksMWSO", "");
            dialog = new Dialog(RetailSOActivity.this);
            dialog.setContentView(R.layout.add_remarks);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setTitle("Remarks");
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


            ImageButton imgBtnCloseRemarksWindow = dialog.findViewById(R.id.imgBtnCloseRemarksWindow);
            Button btnOkRemarks = dialog.findViewById(R.id.btnOkRemarks_Itemwise);
            Button btnClearRemarks_Itemwise = dialog.findViewById(R.id.btnClearRemarks_Itemwise);
            etAddRemarks = dialog.findViewById(R.id.etAddRemarks_Itemwise);
            etAddRemarks.setText(billRemarks);


            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            //lp.height = Integer.parseInt(String.valueOf(saveDialogWindowHeight));
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            dialog.getWindow().setAttributes(lp);


            imgBtnCloseRemarksWindow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            btnOkRemarks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    billRemarks = etAddRemarks.getText().toString();

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("BillRemarksMWSO", billRemarks);
                    editor.commit();

                    if (billRemarks.equalsIgnoreCase("")) {
                        imgBtnRemarksPrescrptn.setImageResource(R.drawable.ic_comments);
                    } else {
                        imgBtnRemarksPrescrptn.setImageResource(R.drawable.ic_comment_changes);
                    }

                    dialog.dismiss();

                }
            });

            btnClearRemarks_Itemwise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etAddRemarks.setText("");
                }
            });

            dialog.show();

        } catch (Exception ex) {
            Toast.makeText(RetailSOActivity.this, "--->" + ex, Toast.LENGTH_SHORT).show();
        }
    }

    public void GoBack(View view) {
        finish();
        //startActivity(new Intent(RetailSOActivity.this, CustomerInformation.class));
    }

    public class getBatchTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSOActivity.this);
            pDialog.setMessage("Loading Product Batch..");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject object = new JSONObject();
                object.put("ITEMID", itemId);
                object.put("DOCGUID", prefs.getString("DOCGUID", ""));
                object.put("CURRENTGUID", prefs.getString("CURRENTGUID", ""));
                URL url = new URL(Url + "getbatchlookup?storeId");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setRequestProperty("authkey", appWide.getAuthID());
                connection.setRequestProperty("docguid", uniqueId);
                connection.setRequestProperty("machineid", appWide.getMachineId());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("inputxmlstd", mObject.toString());
                connection.connect();

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(object.toString());
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
                        String str = "";
                        strProductBatch = sb.toString();
                        System.out.println("Response of Batch DETAIL  is --->" + strProductBatch);
                    } else {
//                        strErrorMsg = connection.getResponseMessage();
                        strErrorMsg = responseMsg;
                        strProductBatch = "httperror";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    connection.disconnect();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return strProductBatch;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();

            try {
                pd = new BatchRetailResponse();
                pd = gson.fromJson(strProductBatch, BatchRetailResponse.class);

                if (pd.STATUSFLAG == 0) {
                    decfor.setRoundingMode(RoundingMode.DOWN);
                    if (pd.DATA.size() == 1) {
                        batchCode = new ArrayList<>();
                        batchExpiry = new ArrayList<>();
                        batchMrp = new ArrayList<>();
                        batchRate = new ArrayList<>();
                        batchSOH = new ArrayList<>();
                        batchID = new ArrayList<>();

                        for (int i = 0; i < pd.DATA.size(); i++) {
                            batchCode.add(pd.DATA.get(i).BATCHCODE);
                            batchExpiry.add(pd.DATA.get(i).BATCHEXPIRY);
                            batchMrp.add(pd.DATA.get(i).BATCHMRP);
                            batchRate.add(pd.DATA.get(i).RATE);
                            batchSOH.add(pd.DATA.get(i).SOHINPACKS);
                            batchSOH.add(Double.valueOf(decfor.format(pd.DATA.get(i).SOHINPACKS)));
                            batchID.add(pd.DATA.get(i).BATCHID);
                        }
                        System.out.println("Batch Rate List is " + batchRate);
                    } else {
                        batchCode = new ArrayList<>();
                        batchCode.add("Select");
                        batchExpiry = new ArrayList<>();
                        batchExpiry.add("00/00/0000");
                        batchMrp = new ArrayList<>();
                        batchMrp.add(0.0);
                        batchRate = new ArrayList<>();
                        batchRate.add(0.0);
                        batchSOH = new ArrayList<>();
                        batchSOH.add(0.0);
                        batchID = new ArrayList<>();
                        batchID.add(0);
                        for (int i = 0; i < pd.DATA.size(); i++) {
                            batchCode.add(pd.DATA.get(i).BATCHCODE);
                            batchExpiry.add(pd.DATA.get(i).BATCHEXPIRY);
                            batchMrp.add(pd.DATA.get(i).BATCHMRP);
                            batchRate.add(pd.DATA.get(i).RATE);
                            //batchSOH.add(pd.DATA.get(i).sohInPacks);
                            batchSOH.add(Double.valueOf(decfor.format(pd.DATA.get(i).SOHINPACKS)));
                            batchID.add(pd.DATA.get(i).BATCHID);
                        }
                    }


                    Rect displayRectangle = new Rect();
                    Window window = RetailSOActivity.this.getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
                    builder = new AlertDialog.Builder(RetailSOActivity.this, R.style.CustomAlertDialog);
                    ViewGroup viewGroup = findViewById(android.R.id.content);
                    View dialogView = LayoutInflater.from(RetailSOActivity.this).inflate(R.layout.quantity_retail_selection_dialogwindow, viewGroup, false);
                    dialogView.setMinimumWidth((int) (displayRectangle.width() * .5f));
                    dialogView.setMinimumHeight((int) (displayRectangle.height() * .5f));
                    builder.setView(dialogView);
                    alertDialog = builder.create();
                    tvSelectedItemName = dialogView.findViewById(R.id.tvSelectedItemName);
                    tvSelectedBatchcode = dialogView.findViewById(R.id.tvSelectedBatchcode);
                    tvSelectedBatchID = dialogView.findViewById(R.id.tvSelectedBatchID);
                    tvSelectedItemCode = dialogView.findViewById(R.id.tvSelectedItemCode);
                    tvItemSOH = dialogView.findViewById(R.id.tvItemSOH);
                    tvSelectedItemExp = dialogView.findViewById(R.id.tvSelectedItemExp);
                    tvItemMRP = dialogView.findViewById(R.id.tvItemMRP);
                    tvbMRP = dialogView.findViewById(R.id.tvbMRP);
                    caculate = dialogView.findViewById(R.id.caculate);
                    etQty = dialogView.findViewById(R.id.etQty);
                    ImageButton imgBtn = dialogView.findViewById(R.id.imgBtn);
                    ImageButton imgBtnMinusPack = dialogView.findViewById(R.id.imgBtnMinusPack);
                    ImageButton imgBtnPlusPack = dialogView.findViewById(R.id.imgBtnPlusPack);
                    Button btnAddItem_qtySelection = dialogView.findViewById(R.id.btnAddItem_qtySelection);
                    Spinner bSpinner = dialogView.findViewById(R.id.batchSpinner);
                    RetailCustomAdapter ad = new RetailCustomAdapter(getApplicationContext(), batchCode, batchExpiry, batchMrp, batchRate, batchSOH);
                  /*  ArrayAdapter ad = new ArrayAdapter(RetailSOActivity.this, android.R.layout.simple_spinner_item, batchCode);
                    ad.setDropDownViewResource(
                            android.R.layout
                                    .simple_spinner_dropdown_item);*/

                    bSpinner.setAdapter(ad);
                    bSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                            if (batchCode.size() > 1) {
                                System.out.println(batchCode.get(i));
                                if (!batchCode.get(i).equalsIgnoreCase("Select")) {
                                    itemExpiry = batchExpiry.get(i);
                                    tvSelectedItemExp.setText("Expiry: " + itemExpiry);
                                    pRate = String.valueOf(batchRate.get(i));
                                    tvItemSOH.setText("SOH: " + batchSOH.get(i));
                                    tvbMRP.setText("MRP: " + batchMrp.get(i));
                                    tvSelectedBatchcode.setText(batchCode.get(i));
                                    tvSelectedBatchID.setText(String.valueOf(batchID.get(i)));
                                    tvItemMRP.setText("Rate: " + batchRate.get(i));
                                    selectedPos = i;
                                }
                            } else {
                                System.out.println("Single Item Batch Code is " + batchCode.get(i));
                                itemExpiry = batchExpiry.get(i);
                                selectedPos = i;
                                tvSelectedItemExp.setText("Expiry: " + itemExpiry);
                                pRate = String.valueOf(batchRate.get(i));
                                tvSelectedBatchcode.setText(batchCode.get(i));
                                tvItemSOH.setText("SOH: " + batchSOH.get(i));
                                tvbMRP.setText("MRP: " + batchMrp.get(i));
                                tvItemMRP.setText("Rate: " + batchRate.get(i));
                                tvSelectedBatchID.setText(String.valueOf(batchID.get(i)));

                            }
                            if (batchCode.get(i).equalsIgnoreCase("Select"))
                                batchFlag = true;
                            else
                                batchFlag = false;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                    TextView pTotal = dialogView.findViewById(R.id.pTotal);
                    //tvSelectedBatchCode.setText("Batch Code: " + pBatchCode);
                    tvSelectedItemName.setText(itemName);
                    tvSelectedItemCode.setText("Product Code: " + itemCode);

                    discPer = dialogView.findViewById(R.id.discField);
                    discPer.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                    etQty.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            String temp = editable.toString().trim();
                            System.out.println(temp);
                            if (temp.length() > 1) {
                                if (temp.contains("-")) {
                                    // temp.replace("-", "");
                                    temp = String.valueOf(Integer.parseInt(temp) * -1);
                                    System.out.println(temp);
                                }
                            }
                        }
                    });
                    imgBtnMinusPack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int qty = Integer.parseInt(etQty.getText().toString());
                            qty--;
                            etQty.setText(String.valueOf(qty));
                            if (qty < 0) {
                                etQty.setText("0");
                            }
                        }
                    });
                    imgBtnPlusPack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int qty = Integer.parseInt(etQty.getText().toString());
                            qty++;
                            etQty.setText(String.valueOf(qty));
                            if (qty < 0) {
                                etQty.setText("0");
                            }
                        }
                    });
                    imgBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.cancel();
                        }
                    });
                    btnAddItem_qtySelection.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            System.out.println(discPer.getText().toString());
                            productTotal = productTotal + Double.parseDouble(tempTotal);
                            System.out.println("Total Product Total 1 is " + productTotal);
                          /*  if (roundingTotal.equalsIgnoreCase("No")) {
                                tvAmountValue.setText(String.valueOf(productTotal));
                            } else {
                                df.setRoundingMode(RoundingMode.UP);
                                tvAmountValue.setText(String.valueOf(df.format(productTotal)));

                            }*/
                            try {
                                detail = new com.techsmith.mw_so.retail_utils.DETAIL();
                                SaveProductSOPL so = new SaveProductSOPL();
                                so.pName = itemName;
                                so.pID = String.valueOf(itemId);
                                so.ItemId = itemId;
                                so.ItemCode = itemCode;
                                so.pCode = itemCode;
                                so.batchExpiry = tvSelectedItemExp.getText().toString().trim().replace("Expiry: ", "");
                                // so.batchMrp = pd.DATA.get(0).batchMrp;
                                so.batchMrp = Double.parseDouble(tvbMRP.getText().toString().replace("MRP: ", ""));
                                so.qty = etQty.getText().toString();
                                // so.Rate = pd.DATA.get(0).Rate;
                                so.Rate = Double.parseDouble(tvItemMRP.getText().toString().replace("Rate: ", ""));
                                so.batchCode = tvSelectedBatchcode.getText().toString();
                                so.batchId = Integer.parseInt(tvSelectedBatchID.getText().toString());
                                so.SOH = pd.DATA.get(0).SOHINPACKS;
                                so.pTotal = ptotal;
                                if (!discPer.getText().toString().isEmpty()) {
                                    if (Double.parseDouble(discPer.getText().toString()) > 0)
                                        so.Disc = Double.parseDouble(discPer.getText().toString());
                                    else
                                        so.Disc = 0.0;
                                }

                                sList.add(so);
                                formedSO = gson.toJson(sList);
                                System.out.println("Temp Formed Json is " + formedSO);
                                setItemList(sList);

                             /*   ITEM item = new ITEM();
                                item.ITEMID = itemId;
                                item.ITEMCODE = Integer.parseInt(itemCode);
                                item.ITEMNAME = itemName;
                                item.HSNCODE = "";
                                item.BATCHCODE = tvSelectedBatchcode.getText().toString();
                                item.BATCHID = Integer.parseInt(tvSelectedBatchID.getText().toString());
                                item.BATCHEXPIRY = itemExpiry;
                                item.MRP = Double.parseDouble(tvbMRP.getText().toString().replace("MRP: ", ""));
                                item.RATE = Double.parseDouble(tvItemMRP.getText().toString().replace("Rate: ", ""));
                                item.PACKQTY = Integer.parseInt(etQty.getText().toString());
                                if (!discPer.getText().toString().isEmpty()) {
                                    if (Double.parseDouble(discPer.getText().toString()) > 0)
                                        item.DISCPER = Integer.parseInt(discPer.getText().toString());
                                    else
                                        item.DISCPER = 0;
                                }
                                item.DISCCODE = "ZART";
                                item.LINETOTAL = Double.parseDouble(tvItemMRP.getText().toString().replace("Rate: ", "")) * Integer.parseInt(etQty.getText().toString());



                                itemArrayList.add(item);*/


                                arrayAdapter = new RetailSOActivityArrayAdapter(RetailSOActivity.this, R.layout.list_row,
                                        productTotal, listSODetailPL, detailList, sList, batchCode, batchExpiry, batchMrp, batchRate, batchSOH, appContext,
                                        selectedPos, itemCode);
                                lvProductlist.setAdapter(arrayAdapter);
                                arrayAdapter.notifyDataSetChanged();
                                if (roundingTotal.equalsIgnoreCase("No")) {
                                    //tvAmountValue.setText(String.valueOf(productTotal));
                                    tvAmountValue.setText(String.valueOf(df.format(productTotal)));
                                } else {
                                    //df.setRoundingMode(RoundingMode.UP);
                                    //After adding rounding function
                                    tvAmountValue.setText(String.valueOf(df.format(Math.round(productTotal))));
                                    //tvAmountValue.setText(String.valueOf(df.format(productTotal)));

                                }

                                acvItemSearchSOActivity.setText("");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            if (!batchFlag) {
                                alertDialog.cancel();
                            } else
                                tsMessages("Select Batch Code");
                        }
                    });
                    caculate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            btnAddItem_qtySelection.setEnabled(true);
                            double d = 0.0, disc = 0.0, temp;
                            try {
                                if (!etQty.getText().toString().isEmpty()) {
                                    if (!discPer.getText().toString().isEmpty()) {
                                        if (Double.parseDouble(discPer.getText().toString()) > 0) {
                                            System.out.println("Came here 2");
                                            disc = Double.parseDouble(discPer.getText().toString()) / 100;
                                            d = Double.parseDouble(pRate) * Double.parseDouble(etQty.getText().toString());
                                            temp = disc * d;
                                            tempTotal = String.valueOf(round(d, 2) - temp);
                                            //ptotal = tempTotal;
                                            ptotal = String.valueOf(decfor.format(Double.parseDouble(tempTotal)));
                                            System.out.println("After Discount " + total);
                                            //pTotal.setText("Total: " + tempTotal);
                                            //pTotal.setText("Total: " + String.format("%.2f", tempTotal));
                                            pTotal.setText(decfor.format(Double.parseDouble(tempTotal)));
                                        } else {
                                            System.out.println("Ca,e here 1");
                                            d = d + (Double.parseDouble(pRate) * Double.parseDouble(etQty.getText().toString()));
                                            tempTotal = String.valueOf(round(d, 2));
                                            ptotal = tempTotal;
                                            // pTotal.setText("Total: " + tempTotal);
                                            //pTotal.setText("Total: " + String.format("%.2f", tempTotal));
                                            // pTotal.setText(decfor.format(d));
                                            pTotal.setText(String.format("%.2f", d));
                                        }
                                    } else {
                                        System.out.println("Came here 3");
                                        d = Double.parseDouble(pRate) * Double.parseDouble(etQty.getText().toString());
                                        tempTotal = String.valueOf(round(d, 2));
                                        ptotal = tempTotal;
                                        // pTotal.setText("Total: " + tempTotal);
                                        pTotal.setText(decfor.format(d));
                                        //String.format("%.2f", sList.get(position).pTotal)
                                    }
                                    System.out.println("Product Total is " + tempTotal);
                                } else {
                                    Toast.makeText(RetailSOActivity.this, "Empty Quantity", Toast.LENGTH_SHORT).show();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    try {
                        alertDialog.setCancelable(false);
                        alertDialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setItemList(ArrayList<SaveProductSOPL> sList) {
        System.out.println("Function Called");
        itemArrayList = new ArrayList<>();
        for (int i = 0; i < sList.size(); i++) {
            ITEM item = new ITEM();
            item.ITEMID = sList.get(i).ItemId;
            item.ITEMCODE = Integer.parseInt(sList.get(i).ItemCode);
            item.ITEMNAME = sList.get(i).pName;
            item.HSNCODE = "";
            item.BATCHCODE = sList.get(i).batchCode;
            item.BATCHID = sList.get(i).batchId;
            item.BATCHEXPIRY = sList.get(i).batchExpiry;
            item.MRP = sList.get(i).batchMrp;
            item.RATE = sList.get(i).Rate;
            item.PACKQTY = Double.parseDouble(sList.get(i).qty);
            item.DISCPER = Float.parseFloat(String.valueOf(sList.get(i).Disc));
            item.DISCCODE = "ZART";

            //item.LINETOTAL = Double.parseDouble(tvItemMRP.getText().toString().replace("Rate: ", "")) * Integer.parseInt(etQty.getText().toString());
            if (sList.get(i).Disc == 0.0)
                item.LINETOTAL = Double.parseDouble(df.format(sList.get(i).Rate * Integer.parseInt(sList.get(i).qty)));
            else {
                Double d = (sList.get(i).Disc / 100) * (sList.get(i).Rate * Integer.parseInt(sList.get(i).qty));
                item.LINETOTAL = Double.parseDouble(df.format((sList.get(i).Rate * Integer.parseInt(sList.get(i).qty)) - d));
                item.TOTALDISCPERC = String.valueOf(sList.get(i).Disc);

            }
            itemArrayList.add(item);
        }
        detail.ITEM = itemArrayList;
        double d = 0.0;
        for (int i = 0; i < detail.ITEM.size(); i++) {
            if (detail.ITEM.get(i).DISCPER == 0.0)
                d = d + Double.parseDouble(df.format(sList.get(i).Rate * Integer.parseInt(sList.get(i).qty)));
            else {
                Double dd = (sList.get(i).Disc / 100) * (sList.get(i).Rate * Integer.parseInt(sList.get(i).qty));
                d = d + Double.parseDouble(df.format((sList.get(i).Rate * Integer.parseInt(sList.get(i).qty)) - dd));

            }
            tvAmountValue.setText(String.valueOf(Math.round(d)));
        }
        String temp = gson.toJson(detail);
        System.out.println("New Formed JSon is " + temp);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private class GetProductDetailsTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSOActivity.this);
            pDialog.setMessage("Loading Product details...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                //https://tsmithy.in/somemouat/api/GetSOHAndSchemes?ItemId=14290&CustId=382,getproductdetails
                //URL url = new URL(Url + "GetSOHAndSchemes?ItemId=" + itemId + "&CustId=" + CustomerId);
                JSONObject object = new JSONObject();
                object.put("ITEMID", itemCode);
                object.put("DOCGUID", prefs.getString("DOCGUID", ""));
                object.put("CURRENTGUID", prefs.getString("CURRENTGUID", ""));

                URL url = new URL(Url + "getproductdetails");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setRequestProperty("authkey", appWide.getAuthID());
                connection.setRequestProperty("docguid", uniqueId);
                connection.setRequestProperty("machineid", appWide.getMachineId());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("inputxmlstd", mObject.toString());
                connection.connect();

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(object.toString());
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
                        String str = "";
                        strGetItemDetail = sb.toString();
                        System.out.println("Response of Item DETAIL  is --->" + strGetItemDetail);
                    } else {
//                        strErrorMsg = connection.getResponseMessage();
                        strErrorMsg = responseMsg;
                        strGetItemDetail = "httperror";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    connection.disconnect();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return strGetItemDetail;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();


            ProductRetailResponse pd = new ProductRetailResponse();
            pd = gson.fromJson(strGetItemDetail, ProductRetailResponse.class);
            if (strGetItemDetail == null || strGetItemDetail.isEmpty()) {
                Toast.makeText(RetailSOActivity.this, "No result from web for item details", Toast.LENGTH_LONG).show();
            } else {
                if (pd.STATUSFLAG == 0) {
                    try {
                        if (!pd.DATA.get(0).PNAME.isEmpty()) {
                            itemName = pd.DATA.get(0).PNAME;
                            itemCode = pd.DATA.get(0).PCODE;
                            itemId = pd.DATA.get(0).PID;
                            new getBatchTask().execute();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(RetailSOActivity.this, itemDetails.errorMessage, Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    private class GetItemsTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSOActivity.this);
            pDialog.setMessage("Loading items...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                //URL url = new URL(Url + "GetProduct?name=" + filter);//
                JSONObject object = new JSONObject();
                object.put("WILDCARD", filter);
                object.put("DOCGUID", prefs.getString("DOCGUID", ""));
                object.put("CURRENTGUID", prefs.getString("CURRENTGUID", ""));
                URL url = new URL(Url + "getproductLookup");
                System.out.println(Url + "getproductLookup?wildcard=" + filter + "&storeid=" + StoreId + "&substoreid=" + subStoreId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(180000);
                connection.setConnectTimeout(180000);
                connection.setRequestProperty("authkey", appWide.getAuthID());
                connection.setRequestProperty("machineid", appWide.getMachineId());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("inputxmlstd", mObject.toString());
                connection.connect();

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(object.toString());
                wr.flush();
                wr.close();

                int responsecode = connection.getResponseCode();
                String responseMsg = connection.getResponseMessage();
                System.out.println("Response code is " + responsecode);

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
                        String str = "";
                        strGetItems = sb.toString();
                        System.out.println("Response of Product List is --->" + strGetItems);
                    } else {
//                        strErrorMsg = connection.getResponseMessage();
                        strErrorMsg = responseMsg;
                        strGetItems = "httperror";
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    connection.disconnect();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return strGetItems;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();
            try {
                if (strGetItems.equals("httperror")) {
                    //tsMessages("Http error occured\n\n" + strErrorMsg);
                    Toast.makeText(RetailSOActivity.this, "" + strErrorMsg, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (strGetItems == null || strGetItems.equals("")) {
                    tsMessages("No result from web");
                } else {
                    gson = new Gson();
                    itemList = gson.fromJson(strGetItems, ItemList.class);
                    if (itemList.STATUSFLAG == 0) {
                        String[] arrProducts = new String[itemList.DATA.size()];
                        String[] arrpCode = new String[itemList.DATA.size()];
                        String[] arrpId = new String[itemList.DATA.size()];
                        String[] arrSoh = new String[itemList.DATA.size()];
                        for (int i = 0; i < itemList.DATA.size(); i++) {
                            arrProducts[i] = itemList.DATA.get(i).PNAME;
                            arrpCode[i] = String.valueOf(itemList.DATA.get(i).PCODE);
                            // arrSoh[i] = String.valueOf(round(itemList.data.get(i).SOH, 2));
                            decfor.setRoundingMode(RoundingMode.DOWN);
                            arrSoh[i] = String.valueOf(decfor.format(itemList.DATA.get(i).SOH));
                            arrpId[i] = String.valueOf(itemList.DATA.get(i).PID);
                        }
                        AutoCompleteRetailProductCustomAdapter myAdapter = new AutoCompleteRetailProductCustomAdapter(RetailSOActivity.this, R.layout.autocomplete_retail_row, arrProducts, arrpCode, arrSoh, arrpId);
                        acvItemSearchSOActivity.setAdapter(myAdapter);
                        acvItemSearchSOActivity.showDropDown();
                    } else {
                        tsMessages(itemList.ERRORMESSAGE);
                        acvItemSearchSOActivity.setText("");
                        acvItemSearchSOActivity.setAdapter(null);

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    public void ClearSearch(View view) {
        acvItemSearchSOActivity.setText("");
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RetailSOActivity.this);
        alertDialogBuilder.setMessage("Do you want to Cancel The SO..!!");
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                finish();
                startActivity(new Intent(RetailSOActivity.this, SoMenu.class));
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

    private class applyRetailScheme extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSOActivity.this);
            pDialog.setMessage("Loading Schemes...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject object = new JSONObject();
                object.put("ITEMID", itemCode);
                object.put("DOCGUID", prefs.getString("DOCGUID", ""));
                object.put("CURRENTGUID", prefs.getString("CURRENTGUID", ""));

                URL url = new URL(Url + "ApplyScheme_RetailBill");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setRequestProperty("authkey", appWide.getAuthID());
                connection.setRequestProperty("docguid", uniqueId);
                connection.setRequestProperty("machineid", appWide.getMachineId());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("inputxmlstd", mObject.toString());
                connection.connect();

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(sendSchemeData);
                wr.flush();
                wr.close();


                int responsecode = connection.getResponseCode();
                String responseMsg = connection.getResponseMessage();

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
                        System.out.println("Scheme Apply Response is " + strfromweb);


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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();
            System.out.println("Scheme Response is " + s);
            strSchemeResponse = s;

            try {
                SchemeResponse response = gson.fromJson(s, SchemeResponse.class);
                if (response.STATUSFLAG == 0) {
                    initializeFab();
                    schemeList = new ArrayList<>();
                    schemeList = response.DATA.OUTPUTXML.DETAIL.ITEM;
                    productTotal = 0.0;
                    acvItemSearchSOActivity.setEnabled(false);
                    ic_search.setEnabled(false);


                    for (int i = 0; i < response.DATA.OUTPUTXML.DETAIL.ITEM.size(); i++) {
                        productTotal = productTotal + (response.DATA.OUTPUTXML.DETAIL.ITEM.get(i).LINETOTAL);
                    }
                    //  productTotal = Double.parseDouble(response.DATA.OUTPUTXML.SUMMARY.NETAMOUNT);
                    // tvAmountValue.setText(String.valueOf(df.format(productTotal)));
                    tvAmountValue.setText(String.valueOf(df.format(Math.round(productTotal))));
                  /*  if (roundingTotal.equalsIgnoreCase("Yes"))
                        tvAmountValue.setText(String.valueOf(df.format(Math.round(productTotal))));
                    else
                        tvAmountValue.setText(String.valueOf(df.format(productTotal)));*/
                    RetailSOSchemeArrayAdapter arrayAdapter = new RetailSOSchemeArrayAdapter(RetailSOActivity.this, R.layout.list_row, s, schemeList.size(), schemeList);
                    lvProductlist.setAdapter(arrayAdapter);
                    arrayAdapter.notifyDataSetChanged();

                } else {
                    tsMessages(response.ERRORMESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void addSchemeItem(ArrayList<com.techsmith.mw_so.scheme_utils.ITEM> schemeList, String s) {
        try {
            itemArrayList = new ArrayList<>();
            PAYMENT payment = new PAYMENT();
            double totalSum = 0.0;
            ArrayList<PAYDETAIL> paydetailList = new ArrayList<>();
            prefsD = getSharedPreferences("pref", Context.MODE_PRIVATE);
            String t1 = prefsD.getString("cashSave", "");
            String t2 = prefsD.getString("cardSave", "");

            gson = new Gson();
            if (!t1.isEmpty()) {
                paymentList = gson.fromJson(t1, PaymentList.class);
                cashAmount = paymentList.cashAmount;
            }
            if (!t2.isEmpty()) {
                paymentCardList = gson.fromJson(t2, PaymentList.class);
                cardAmount = paymentCardList.cardAmount;
            }


            System.out.println(cashAmount + "\t<------------->\t" + cardAmount);
            totalSum = totalSum + Double.parseDouble(cashAmount) + Double.parseDouble(cardAmount);

            if (cashAmount.equalsIgnoreCase("0.0") && cardAmount.equalsIgnoreCase("0.0")) {
                tsMessages("Add Payment");
            } else if (Double.parseDouble(cashAmount) + Double.parseDouble(cardAmount) != Double.parseDouble(tvAmountValue.getText().toString())) {
                tsMessages("Update Payment");
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
            //paysummary.BILLAMOUNT = Float.parseFloat(tvAmountValue.getText().toString());
            paysummary.BILLAMOUNT =Double.parseDouble(tvAmountValue.getText().toString());
            paysummary.PAIDAMOUNT = totalSum;


            payment.PAYSUMMARY = paysummary;
            payment.PAYDETAIL = paydetailList;
            double tempSum = 0.0;
            System.out.println("look here");
            for (int i = 0; i < schemeList.size(); i++) {
                ITEM item = new ITEM();
                item.ITEMID = schemeList.get(i).ITEMID;
                item.ITEMCODE = schemeList.get(i).ITEMCODE;
                item.ITEMNAME = schemeList.get(i).ITEMNAME;
                item.HSNCODE = schemeList.get(i).HSNCODE;
                item.BATCHCODE = schemeList.get(i).BATCHCODE;
                item.BATCHID = schemeList.get(i).BATCHID;
                item.BATCHEXPIRY = schemeList.get(i).BATCHEXPIRY;
                item.MRP = schemeList.get(i).MRP;
                item.RATE = schemeList.get(i).RATE;
                item.PACKQTY = schemeList.get(i).PACKQTY;
                item.DISCPER = schemeList.get(i).DISCPER;
                item.DISCCODE = "ZART";
                item.LINETOTAL = schemeList.get(i).LINETOTAL;
                item.TOTALDISCAMOUNT = schemeList.get(i).TOTALDISCAMOUNT;
                item.TOTALDISCPERC = schemeList.get(i).TOTALDISCPERC;
                item.EFFECTIVESCHEMEDISCPER = schemeList.get(i).EFFECTIVESCHEMEDISCPER;
                item.LCARDDISCPER = schemeList.get(i).LCARDDISCPER;
                item.SCHEMEDISCPER = schemeList.get(i).SCHEMEDISCPER;
                item.SCHEMEOFFAMOUNT = schemeList.get(i).SCHEMEOFFAMOUNT;
                item.EFFSCHEMEDISCPERC = schemeList.get(i).EFFSCHEMEDISCPERC;
                item.LCARDDISCID = schemeList.get(i).LCARDDISCID;
                item.EFFLCARDDISCPERC = schemeList.get(i).EFFLCARDDISCPERC;
                item.SCHEMECAUSINGID = schemeList.get(i).SCHEMECAUSINGID;
                item.SCHEMEBENEFICIARYID = schemeList.get(i).SCHEMEBENEFICIARYID;

                itemArrayList.add(item);
                tempSum = tempSum + schemeList.get(i).LINETOTAL;

            }
            detail.ITEM = itemArrayList;
            // String temp = gson.toJson(detail);
            // System.out.println("New Formed Scheme Json is " + temp);
            SchemeResponse response = gson.fromJson(s, SchemeResponse.class);


            Summary summary = new Summary();
            CUSTOMERDETAIL CUSTOMERDETAIL = new CUSTOMERDETAIL();

            //summary.BILLDATE = response.DATA.OUTPUTXML.SUMMARY.BILLDATE;
            Date c = Calendar.getInstance().getTime();
            String formattedDate = dff.format(c);
            // summary.BILLDATE = "31/07/2023";
            summary.BILLDATE = formattedDate;
            summary.BILLNO = response.DATA.OUTPUTXML.SUMMARY.BILLNO;
            summary.REFNO = String.valueOf(response.DATA.OUTPUTXML.SUMMARY.REFNO);
            summary.BILLTYPE = response.DATA.OUTPUTXML.SUMMARY.BILLTYPE;
            summary.CUSTOMER = response.DATA.OUTPUTXML.CUSTOMERDETAIL.CUSTOMER;
            summary.CCEID = response.DATA.OUTPUTXML.SUMMARY.CCEID;
            summary.SUMMARYDISC = Integer.parseInt(response.DATA.OUTPUTXML.SUMMARY.SUMMARYDISC);
            summary.NOOFITEMS = response.DATA.OUTPUTXML.SUMMARY.NOOFITEMS;
            summary.DOCGUID = response.DATA.OUTPUTXML.SUMMARY.DOCGUID;
            summary.CURRENTGUID = response.DATA.OUTPUTXML.SUMMARY.CURRENTGUID;
            // summary.ROUNDOFF = Double.parseDouble(response.DATA.OUTPUTXML.SUMMARY.ROUNDOFF);
            summary.ROUNDOFF = Double.parseDouble(df.format(totalSum - tempSum));
            //summary.NETAMOUNT = Double.parseDouble(response.DATA.OUTPUTXML.SUMMARY.NETAMOUNT);//need round amount
            summary.NETAMOUNT = Math.round(totalSum);


            CUSTOMERDETAIL.CUSTOMER = response.DATA.OUTPUTXML.CUSTOMERDETAIL.CUSTOMER;
            CUSTOMERDETAIL.LOYALTYCODE = response.DATA.OUTPUTXML.CUSTOMERDETAIL.LOYALTYCODE;
            CUSTOMERDETAIL.LOYALTYID = response.DATA.OUTPUTXML.CUSTOMERDETAIL.LOYALTYID;
            CUSTOMERDETAIL.ADDRESS = response.DATA.OUTPUTXML.CUSTOMERDETAIL.ADDRESS;
            CUSTOMERDETAIL.MOBILENO = Long.parseLong(response.DATA.OUTPUTXML.CUSTOMERDETAIL.MOBILENO);
            CUSTOMERDETAIL.AREA = response.DATA.OUTPUTXML.CUSTOMERDETAIL.AREA;
            CUSTOMERDETAIL.PINCODE = Integer.parseInt(response.DATA.OUTPUTXML.CUSTOMERDETAIL.PINCODE);
            CUSTOMERDETAIL.STATE = response.DATA.OUTPUTXML.CUSTOMERDETAIL.STATE;


            sap = new SaveProductSO();
            sap.SUMMARY = summary;
            sap.CUSTOMERDETAIL = CUSTOMERDETAIL;
            sap.DETAIL = detail;
            sap.PAYMENT = payment;
            sendSchemeData = gson.toJson(sap);
            System.out.println("New Formed Scheme Json is " + sendSchemeData);
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(RetailSOActivity.this);
            alertDialogBuilder.setMessage("Do you want to Save Retail SO..?");
            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    new saveSchemeSO().execute();
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

    private void clearPaymentSave() {
        SharedPreferences.Editor editor1 = prefs.edit();
        editor1.putString("billTotal", "0.0");
        editor1.putString("billCash", "");
        editor1.putString("billCard", "");
        editor1.apply();


        prefsD = getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefsD.edit();
        editor.putString("cashSave", "");
        editor.putString("cardSave", "");
        editor.apply();
    }

    private void disableButtons() {
        btnSave.setEnabled(false);
        btnSave.setAlpha(0.5f);
        btnAllClear.setEnabled(false);
        btnAllClear.setAlpha(0.5f);
        paymentBtn.setEnabled(false);
        paymentBtn.setAlpha(0.5f);
        acvItemSearchSOActivity.setEnabled(false);
        ic_search.setEnabled(false);
    }

    public void ClearList(View view) {
        try {

            if (sList.size() > 0) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RetailSOActivity.this);
                alertDialogBuilder.setMessage("Do you want to Cancel The SO..!!");
                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        acvItemSearchSOActivity.setText("");
                        acvItemSearchSOActivity.setAdapter(null);
                        discText.setText("Disc%");
                        sList.clear();
                        lvProductlist.setAdapter(null);
                        tvAmountValue.setText("00.00");
                        prefsD = getSharedPreferences("pref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefsD.edit();
                        editor.putString("cashSave", "");
                        editor.putString("cardSave", "");
                        editor.apply();
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
            } else {
                tsMessages("List is EMpty");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Restart(View view) {
        prefsD = getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefsD.edit();
        editor.putString("cashSave", "");
        editor.putString("cardSave", "");
        editor.apply();
        Intent i = new Intent(RetailSOActivity.this, RetailCustomerInformation.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(RetailSOActivity.this);
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

    public void popUp(String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RetailSOActivity.this);
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

    private class SaveData extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSOActivity.this);
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


                URL url = new URL(Url + "Save_RetailBill");
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
                wr.writeBytes(sendTestData);
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
                        System.out.println("SO Save Response is " + strfromweb);


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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();
            System.out.println("Save Data Response is " + s);
            try {
                gson = new Gson();
                //customerResponse = gson.fromJson(strCustomer, RetailCustomerResponse.class);
                APIResponse apiResponse = gson.fromJson(s, APIResponse.class);
                if (apiResponse.STATUSFLAG == 0) {
                    String msg = apiResponse.DATA.RESPONSE.MSG + "\nBill No: " + apiResponse.DATA.RESPONSE.BILLNO;
                    editor = prefs.edit();
                    editor.putString("billNo", apiResponse.DATA.RESPONSE.BILLNO);
                    editor.apply();
                    tsMessages(msg);
                    disableButtons();
                    //
                } else {
                    tsMessages(apiResponse.ERRORMESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class saveSchemeSO extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSOActivity.this);
            pDialog.setMessage("Saving Scheme Data....");
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


                URL url = new URL(Url + "Save_RetailBill");
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
                wr.writeBytes(sendSchemeData);
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
                        System.out.println("SO Save Response is " + strfromweb);


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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();
            System.out.println("Scheme Save Data Response is " + s);
            try {
                gson = new Gson();
                //customerResponse = gson.fromJson(strCustomer, RetailCustomerResponse.class);
                APIResponse apiResponse = gson.fromJson(s, APIResponse.class);
                if (apiResponse.STATUSFLAG == 0) {
                    String msg = apiResponse.DATA.RESPONSE.MSG + "\nBill No: " + apiResponse.DATA.RESPONSE.BILLNO;
                    tsMessages(msg);
                    editor = prefs.edit();
                    editor.putString("billNo", apiResponse.DATA.RESPONSE.BILLNO);
                    editor.apply();
                    disableButtons();
                    //
                } else {
                    tsMessages(apiResponse.ERRORMESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class revertScheme extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSOActivity.this);
            pDialog.setMessage("Saving Pre Data....");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            //
            try {
                JSONObject object = new JSONObject();
                object.put("StoreCode", "2021-DLF-PH1");
                object.put("SubStoreCode", "MAIN");
                object.put("UserId", "1");
                object.put("CounterId", "1");
                object.put("CustType", "1");

                JSONObject jObject = new JSONObject();
                jObject.put("CURRENTGUID", prefs.getString("CURRENTGUID", ""));
                jObject.put("DOCGUID", prefs.getString("DOCGUID", ""));


                URL url = new URL(Url + "RevertScheme_RetailBill");
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
                        strRevertScheme = sb.toString();
                        System.out.println("Revert Response is " + strRevertScheme);


                    } finally {
                        connection.disconnect();
                    }

                } else {
                    strerrormsg = connection.getResponseMessage();
                    strRevertScheme = "httperror";
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return strRevertScheme;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();
            System.out.println("Scheme Revert Response is " + s);
            try {
                SchemeReverseResponse sc = gson.fromJson(strRevertScheme, SchemeReverseResponse.class);
                if (sc.STATUSFLAG == 0) {
                    ic_search.setEnabled(true);
                    acvItemSearchSOActivity.setEnabled(true);
                    schemeList = new ArrayList<>();
                    strSchemeResponse = "";
                    initializeFab();
                    ArrayList<SchemeReverseItem> rItem = new ArrayList<>();
                    rItem = sc.DATA.INPUTXML.DETAIL.ITEM;
                    for (int i = 0; i < rItem.size(); i++) {
                        sList.get(i).MRP = Double.parseDouble(rItem.get(i).MRP);
                        sList.get(i).batchCode = rItem.get(i).BATCHCODE;
                        sList.get(i).batchId = Integer.parseInt(rItem.get(i).BATCHID);
                        sList.get(i).Rate = Double.parseDouble(rItem.get(i).RATE);
                        sList.get(i).batchExpiry = rItem.get(i).BATCHEXPIRY;
                        sList.get(i).qty = rItem.get(i).PACKQTY;
                        sList.get(i).Disc = Double.parseDouble(rItem.get(i).DISCPER);
                    }

                    // sList.add(sc.DATA.INPUTXML.DETAIL.ITEM) ;

                    arrayAdapter = new RetailSOActivityArrayAdapter(RetailSOActivity.this, R.layout.list_row,
                            productTotal, listSODetailPL, detailList, sList, batchCode, batchExpiry, batchMrp, batchRate, batchSOH, appContext,
                            selectedPos, itemCode);
                    lvProductlist.setAdapter(arrayAdapter);
                    arrayAdapter.notifyDataSetChanged();
                    Double revertTotal = 0.0, d = 0.0;

                    for (int i = 0; i < sList.size(); i++) {
                        if (sList.get(i).Disc > 0) {
                            d = sList.get(i).Disc / 100;
                            revertTotal = revertTotal + ((Double.parseDouble(sList.get(i).qty) * sList.get(i).Rate) -
                                    ((Double.parseDouble(sList.get(i).qty) * sList.get(i).Rate) * d));

                        } else {
                            revertTotal = revertTotal + (Double.parseDouble(sList.get(i).qty) * sList.get(i).Rate);
                        }
                    }

                    tvAmountValue.setText(String.valueOf(df.format(revertTotal)));

                  /*  arrayAdapter = new RetailSOActivityArrayAdapter(RetailSOActivity.this, R.layout.list_row,
                            productTotal, listSODetailPL, detailList, sList, batchCode, batchExpiry, batchMrp, batchRate, batchSOH, appContext,
                            selectedPos, itemCode);
                    lvProductlist.setAdapter(arrayAdapter);
                    arrayAdapter.notifyDataSetChanged();*/


                } else {
                    tsMessages(sc.ERRORMESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
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
                     /*   final String text =
                                "[L]" + temp;*/
                       /* final String text="[L]\n" +
                                "[C]<u><font size='medium'>"+msg+"</font></u>\n" ;*/

                        printer.printFormattedText(msg);

                        connection.disconnect();
                    } else {
                        //Toast.makeText(this, "No printer was connected...!", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RetailSOActivity.this);
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

    private void printamaPrint(String msg) {
        BluetoothDevice connectedPrinter = Printama.with(this).getConnectedPrinter();
        if (connectedPrinter != null) {

            Printama.with(this).connect(printama -> {
                printama.printText(msg, Printama.CENTER);
                printama.close();
            });
        }


    }

    private void showToast(String s) {
        // System.out.println("Message is " + message);
        // Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public boolean isBluetoothEnabled() {
        BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return myBluetoothAdapter.isEnabled();
    }

    private void browseBluetooth() {
        try {

            if (isBluetoothEnabled()) {
                final BluetoothConnection[] bluetoothDevicesList = (new BluetoothPrintersConnections()).getList();

                if (bluetoothDevicesList != null) {
                    final String[] items = new String[bluetoothDevicesList.length + 1];
                    items[0] = "Default printer";
                    int i = 0;
                    for (BluetoothConnection device : bluetoothDevicesList) {
                        items[++i] = device.getDevice().getName();
                    }

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(RetailSOActivity.this);
                    alertDialog.setTitle("Bluetooth printer selection");
                    alertDialog.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int index = i - 1;
                            if (index == -1) {
                                selectedDevice = null;
                            } else {
                                selectedDevice = bluetoothDevicesList[index];
                            }

                            // button.setText(items[i]);
                            //  printer.setText(items[i]);
                        }
                    });

                    AlertDialog alert = alertDialog.create();
                    alert.setCanceledOnTouchOutside(false);
                    alert.show();

                }
            } else {
                Toast.makeText(RetailSOActivity.this, "Enable Bluetooth First", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(RetailSOActivity.this, "Bluetooth Browse Exception", Toast.LENGTH_LONG).show();
        }

    }

    private class PrintBill extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSOActivity.this);
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

                JSONObject jObject = new JSONObject();
                jObject.put("INVOICENO", prefs.getString("billNo", ""));
                //jObject.put("INVOICENO","2021/23/S-58");
                jObject.put("FORMAT", FORMAT);


                URL url = new URL(Url + "GetSBillPrint");
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
                        System.out.println("Print Bill Response is " + strPrintBill);


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
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(RetailSOActivity.this);
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

                    disableButtons();
                    //
                } else {
                    tsMessages(apiResponse.ERRORMESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class EmailBill extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSOActivity.this);
            pDialog.setMessage("Emailing....");
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
                jObject.put("INVOICENO", prefs.getString("billNo", ""));
                //jObject.put("INVOICENO","2021/23/S-58");
                jObject.put("FORMAT", FORMAT);


                URL url = new URL(Url + "GetSBillPrint");
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
                        System.out.println("Print Bill Response is " + strPrintBill);


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

                EmailResponse apiResponse = gson.fromJson(s, EmailResponse.class);
                if (apiResponse.STATUSFLAG == 0) {
                    String msg = apiResponse.DATA;
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(RetailSOActivity.this);
                    alertDialogBuilder.setMessage(msg);

                    alertDialogBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    disableButtons();
                } else {
                    tsMessages(apiResponse.ERRORMESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}


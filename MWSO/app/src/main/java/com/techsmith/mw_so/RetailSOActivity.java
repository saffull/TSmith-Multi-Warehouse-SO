package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.techsmith.mw_so.Expandable.RecyclerTouchListener;
import com.techsmith.mw_so.Global.AppWide;
import com.techsmith.mw_so.Spinner.RetailCustomAdapter;
import com.techsmith.mw_so.e_invoice.Einvoice;
import com.techsmith.mw_so.e_invoice.EinvoicePL;
import com.techsmith.mw_so.e_invoice.IRNAdapter;
import com.techsmith.mw_so.e_invoice.InvResponse;
import com.techsmith.mw_so.e_invoice.InvoiceAdapter;
import com.techsmith.mw_so.retail_utils.AutoCompleteRetailProductCustomAdapter;
import com.techsmith.mw_so.retail_utils.BatchRetailResponse;
import com.techsmith.mw_so.retail_utils.ProductRetailResponse;
import com.techsmith.mw_so.retail_utils.RetailReplyData;
import com.techsmith.mw_so.retail_utils.RetailSOActivityArrayAdapter;
import com.techsmith.mw_so.retail_utils.SaveProductSO;
import com.techsmith.mw_so.retail_utils.SaveProductSOPL;
import com.techsmith.mw_so.utils.AllocateQty;
import com.techsmith.mw_so.utils.AllocateQtyPL;
import com.techsmith.mw_so.utils.AppConfigSettings;
import com.techsmith.mw_so.utils.ItemDetails;
import com.techsmith.mw_so.utils.ItemList;
import com.techsmith.mw_so.utils.UserPL;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RetailSOActivity extends AppCompatActivity {
    SharedPreferences prefs, prefsD;
    RetailSOActivityArrayAdapter arrayAdapter;
    private static final DecimalFormat decfor = new DecimalFormat("0.00");
    List<String> storeidList;
    BatchRetailResponse pd;
    Context appContext;
    String loginResponse, filter = "", Url = "", strGetItems, strErrorMsg, strSaveMiniSO, billRemarks = "", itemExpiry,
            strGetItemDetail, strProductBatch, itemName, strGetTotal, itemCode, soString = "", strCheckLogin, printData, s1 = "", s2 = "",
            multiSOStoredDevId = "", uniqueId, strfromweb, strerrormsg, strstocktake, customer_Details;
    public Double itemSoh, total = 0.0, totalSOH;// item made public so that to access in its adapter class
    public String itemMrp, ptotal = "", formedSO = "", pRate = "", tempTotal = "", subStoreId = "", StoreId = "",
            customer_name = "", customer_id = "", roundingTotal = "No";// item made public so that to access in its adapter class
    int CustomerId, itemId, itemQty, selectedQty, soResponseCount = 0;
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
    TextView cashDisc, volDisc, allocStore, tvRate, whText, tvSelectedItemName, tvMrp, whSoh, offers;
    public TextView tvAmountValue; // item made public so that to access in its adapter class
    public ArrayList<AllocateQtyPL> detailProductList;
    public ArrayList<SaveProductSOPL> sList;
    SaveProductSO saveProductSO;
    Boolean batchFlag = false;
    EditText etAddRemarks;
    public Button caculate, btnAdd, btnAllClear, btnSave;
    List<String> offerList, houseList, sohList, batchCode, batchExpiry;
    List<Double> batchMrp, batchRate, batchSOH;
    TextView tvCustomerName, tvDate, Freeqty, etReceivables;
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
    public Double productTotal = 0.0;
    private Button paymentBtn;
    AppWide appWide;
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
        lvProductlist = findViewById(R.id.lvProductlist);
        addAlarmActionText = findViewById(R.id.add_alarm_action_text);
        addPersonActionText = findViewById(R.id.add_person_action_text);
        preview_text = findViewById(R.id.preview_text);
        paymentBtn = findViewById(R.id.paymentBtn);
        imgBtn = findViewById(R.id.imgBtn);
        initializeFab();
        detailProductList = new ArrayList<>();
        appWide = AppWide.getInstance();
        sList = new ArrayList<>();
        tvCustomerName = findViewById(R.id.tvCustomerName);
        ic_search = findViewById(R.id.imgBtnSearchItem);
        lvProductlist = findViewById(R.id.lvProductlist);
        btnAllClear = findViewById(R.id.btnAllClear);
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

        customer_Details = prefs.getString("customer_Details", "");
        System.out.println("Rounding total value " + roundingTotal);
        try {
            Gson gson = new Gson();
            //gson.fromJson(loginResponse, UserPL.class);
            RetailReplyData rcData = new RetailReplyData();
            rcData = gson.fromJson(customer_Details, RetailReplyData.class);
//            System.out.println(rcData.Name);
            tvCustomerName.setText(rcData.Name);
            // tvDate.setText(String.valueOf(customer_id));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Unique Id is " + uniqueId);
        if (roundingTotal.equalsIgnoreCase("Yes"))

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
                    itemId = itemList.data.get(pos).pmid;
                    itemName = itemList.data.get(pos).product;
                    itemCode = String.valueOf(itemList.data.get(pos).pId);
                    itemMrp = String.valueOf(itemList.data.get(pos).mrp);
                    itemSoh = itemList.data.get(pos).sohInPacks;


                    if (!itemCode.isEmpty())
                        new GetProductDetailsTask().execute();
                } else {
                    acvItemSearchSOActivity.setAdapter(null);
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
                SaveRetailSo();
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
                            mAddFab.extend();
                            isAllFabsVisible = true;
                        } else {
                            mAddAlarmFab.hide();
                            mAddPersonFab.hide();
                            preview_fab.hide();
                            addAlarmActionText.setVisibility(View.GONE);
                            addPersonActionText.setVisibility(View.GONE);
                            preview_text.setVisibility(View.GONE);
                            mAddFab.shrink();
                            isAllFabsVisible = false;
                        }
                    }
                });

        mAddPersonFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RetailSOActivity.this, "Function not yet implemented..", Toast.LENGTH_SHORT).show();
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

    private void SaveRetailSo() {
        if (sList.size() > 0)
            tsMessages(formedSO);
        else
            tsMessages("List Empty");

    }

    private void init() {
        this.linearLayoutBSheet = findViewById(R.id.bottomSheetPayment);
        this.bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBSheet);
        this.tbUpDown = findViewById(R.id.toggleButton);
        this.tvAmountValue = findViewById(R.id.tvAmountValue);
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
                    + "\n LoyaltyCode: " + appWide.getLoyaltyCode() + "\n Class Name: ";
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
        addAlarmActionText.setVisibility(View.GONE);
        addPersonActionText.setVisibility(View.GONE);
        preview_text.setVisibility(View.GONE);
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
                URL url = new URL(Url + "getbatchlookup?storeId=" + appWide.getStoreId() + "&substoreId=" + appWide.getSubStoreId() + "&itemid=" + itemId);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setRequestProperty("authkey", appWide.getAuthID());
                connection.setRequestProperty("docguid", uniqueId);
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
                        String str = "";
                        strProductBatch = sb.toString();
                        System.out.println("Response of Batch Detail  is --->" + strProductBatch);
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

                if (pd.statusFlag == 0) {
                    if (pd.data.size() == 1) {
                        batchCode = new ArrayList<>();
                        batchExpiry = new ArrayList<>();
                        batchMrp = new ArrayList<>();
                        batchRate = new ArrayList<>();
                        batchSOH = new ArrayList<>();

                        for (int i = 0; i < pd.data.size(); i++) {
                            batchCode.add(pd.data.get(i).batchCode);
                            batchExpiry.add(pd.data.get(i).batchExpiry);
                            batchMrp.add(pd.data.get(i).batchMrp);
                            batchRate.add(pd.data.get(i).Rate);
                            batchSOH.add(pd.data.get(i).sohInPacks);
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
                        for (int i = 0; i < pd.data.size(); i++) {
                            batchCode.add(pd.data.get(i).batchCode);
                            batchExpiry.add(pd.data.get(i).batchExpiry);
                            batchMrp.add(pd.data.get(i).batchMrp);
                            batchRate.add(pd.data.get(i).Rate);
                            batchSOH.add(pd.data.get(i).sohInPacks);
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
                    TextView tvSelectedItemName = dialogView.findViewById(R.id.tvSelectedItemName);
                    TextView tvSelectedBatchcode = dialogView.findViewById(R.id.tvSelectedBatchcode);
                    TextView tvSelectedItemCode = dialogView.findViewById(R.id.tvSelectedItemCode);
                    TextView tvItemSOH = dialogView.findViewById(R.id.tvItemSOH);
                    TextView tvSelectedItemExp = dialogView.findViewById(R.id.tvSelectedItemExp);
                    TextView tvItemMRP = dialogView.findViewById(R.id.tvItemMRP);
                    TextView tvbMRP = dialogView.findViewById(R.id.tvbMRP);
                    Button caculate = dialogView.findViewById(R.id.caculate);
                    EditText etQty = dialogView.findViewById(R.id.etQty);
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
                                    tvItemMRP.setText("Rate: " + batchRate.get(i));
                                    selectedPos = i;
                                }
                            } else {
                                System.out.println(batchCode.get(i));
                                itemExpiry = batchExpiry.get(i);
                                selectedPos = i;
                                tvSelectedItemExp.setText("Expiry: " + itemExpiry);
                                pRate = String.valueOf(batchRate.get(i));
                                tvSelectedBatchcode.setText(batchCode.get(i));
                                tvItemSOH.setText("SOH: " + batchSOH.get(i));
                                tvbMRP.setText("MRP: " + batchMrp.get(i));
                                tvItemMRP.setText("Rate: " + batchRate.get(i));

                            }
                            if (batchCode.get(i).equalsIgnoreCase("Select"))
                                batchFlag = true;
                            else
                                batchFlag = false;
                        /*    for (int j = 0; j <sList.size() ; j++) {
                                if (sList.get(j).pID.equalsIgnoreCase(itemCode)){
                                    if (sList.get(j).batchCode.equalsIgnoreCase(tvSelectedBatchcode.getText().toString())){
                                        System.out.println("Same Product and Same BAtch Id FOund");
                                        Toast.makeText(RetailSOActivity.this,"Product with Same " +
                                                "Batch Id Found",Toast.LENGTH_LONG).show();
                                        caculate.setEnabled(false);
                                        caculate.setAlpha(0.5F);
                                    }else{
                                        caculate.setAlpha(1F);
                                        caculate.setEnabled(true);
                                    }
                                }
                            }*/
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                    TextView pTotal = dialogView.findViewById(R.id.pTotal);
                    //tvSelectedBatchCode.setText("Batch Code: " + pBatchCode);
                    tvSelectedItemName.setText(itemName);
                    tvSelectedItemCode.setText("Product Code: " + itemCode);

                    TextInputEditText discPer = dialogView.findViewById(R.id.discField);
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
                            if (roundingTotal.equalsIgnoreCase("No")) {
                                tvAmountValue.setText(String.valueOf(productTotal));
                            } else {
                                df.setRoundingMode(RoundingMode.UP);
                                tvAmountValue.setText(String.valueOf(df.format(productTotal)));

                            }
                            try {
                                SaveProductSOPL so = new SaveProductSOPL();
                                so.pName = itemName;
                                so.pID = itemCode;
                                so.pCode = itemCode;
                                so.batchExpiry = itemExpiry;
                                so.batchMrp = pd.data.get(0).batchMrp;
                                so.qty = etQty.getText().toString();
                                so.Rate = pd.data.get(0).Rate;
                                so.batchCode = tvSelectedBatchcode.getText().toString();
                                so.SOH = pd.data.get(0).sohInPacks;
                                so.pTotal = ptotal;
                                if (!discPer.getText().toString().isEmpty()) {
                                    if (Double.parseDouble(discPer.getText().toString()) > 0)
                                        so.Disc = Double.parseDouble(discPer.getText().toString());
                                    else
                                        so.Disc = 0.0;
                                }

                                sList.add(so);
                                formedSO = gson.toJson(sList);
                                System.out.println("Formed json is " + formedSO);

                                arrayAdapter = new RetailSOActivityArrayAdapter(RetailSOActivity.this, R.layout.list_row,
                                        productTotal, listSODetailPL, detailList, sList, batchCode, batchExpiry, batchMrp, batchRate, batchSOH, appContext,
                                        selectedPos, itemCode);
                                lvProductlist.setAdapter(arrayAdapter);
                                arrayAdapter.notifyDataSetChanged();
                                //tvAmountValue.setText(String.format("%.2f", productTotal));
                                // tvAmountValue.setText(String.valueOf(round(productTotal, 2)));
                                if (roundingTotal.equalsIgnoreCase("No")) {
                                    tvAmountValue.setText(String.valueOf(productTotal));
                                } else {
                                    df.setRoundingMode(RoundingMode.UP);
                                    tvAmountValue.setText(String.valueOf(df.format(productTotal)));

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
                URL url = new URL(Url + "getproductdetails?storeId=" + appWide.getStoreId() + "&substoreId=" + appWide.getSubStoreId() + "&itemid=" + itemCode);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setRequestProperty("authkey", appWide.getAuthID());
                connection.setRequestProperty("docguid", uniqueId);
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
                        String str = "";
                        strGetItemDetail = sb.toString();
                        System.out.println("Response of Item Detail  is --->" + strGetItemDetail);
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
                if (pd.statusFlag == 0) {
                    try {
                        if (!pd.data.get(0).pName.isEmpty()) {
                            itemName = pd.data.get(0).pName;
                            itemCode = pd.data.get(0).pCode;
                            itemId = pd.data.get(0).pId;
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

    private class AllocateQtyTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSOActivity.this);
            pDialog.setMessage("Updating details...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject appUserPlJsnStr = new JSONObject();
                appUserPlJsnStr.put("qty", selectedQty);
                appUserPlJsnStr.put("itemid", itemId);
                appUserPlJsnStr.put("customer", CustomerId);

                //https://tsmithy.in1/somemouat/api/AllocateQty
                URL url = new URL(Url + "AllocateQty");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(120000);
                connection.setConnectTimeout(120000);
                connection.setRequestProperty("authkey", AppConfigSettings.auth_id);
                connection.setRequestProperty("name", "");
                connection.setRequestProperty("password", "");
                connection.setRequestProperty("debugkey", "");
                connection.setRequestProperty("remarks", "");
                connection.setRequestProperty("docguid", uniqueId);
                connection.setRequestProperty("machineid", "salam_ka@yahoo.com");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                wr.write(String.valueOf(appUserPlJsnStr));
                wr.flush();
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
                        String str = "";
                        strGetTotal = sb.toString();
                        System.out.println("Response of AllocateQty is --->" + sb);
                    } else {
//                        strErrorMsg = connection.getResponseMessage();
                        strErrorMsg = responseMsg;
                        strGetTotal = "httperror";
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    connection.disconnect();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return strGetTotal;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();

            try {

                gson = new Gson();
                allocateQty = gson.fromJson(strGetTotal, AllocateQty.class);
                if (allocateQty.statusFlag == 0) {

                    try {
                        caculate.setEnabled(true);
                        cashDisc.setText("Cash Disc: " + allocateQty.data.cashDiscPer + "%");
                        volDisc.setText("Vol Disc: " + allocateQty.data.volDiscPer + "%");
                        tvRate.setText("Rate: " + String.format("%.2f", allocateQty.data.rate));
                        allocStore.setText("Allocated Warehouse: " + allocateQty.data.allocStoreCode);
                        Freeqty.setText("Free Quantity: " + allocateQty.data.freeQty);
                        tvSelectedItemName.setText("" + itemName);
                        tvMrp.setText("MRP: " + itemMrp);
                        totalSOH = allocateQty.data.soh;

                        StringBuilder builder = new StringBuilder();
                        for (String details : houseList) {
                            builder.append(details.subSequence(0, 9) + "\n\n");
                        }
                        StringBuilder sbuilder = new StringBuilder();
                        for (String soh : sohList) {
                            sbuilder.append(soh + "\n\n");
                        }
                        whText.setText(builder.toString());
                        whSoh.setText(sbuilder
                                .toString());


                        offers.setText(offerList.toString().replace("[", "").replace("]", ""));
                        offerMap.put(String.valueOf(itemId), offerList.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    qtydialog.dismiss();
                    acvItemSearchSOActivity.setText("");
                    acvItemSearchSOActivity.setAdapter(null);
                    tsMessages(allocateQty.errorMessage);

                }

            } catch (Exception e) {
                e.printStackTrace();
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
                URL url = new URL(Url + "getproductLookup?wildcard=" + filter + "&storeid=" + StoreId + "&substoreid=" + subStoreId);
                System.out.println(Url + "getproductLookup?wildcard=" + filter + "&storeid=" + StoreId + "&substoreid=" + subStoreId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(180000);
                connection.setConnectTimeout(180000);
                connection.setRequestProperty("authkey", appWide.getAuthID());
                connection.setRequestProperty("machineid", appWide.getMachineId());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();

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
                    if (itemList.statusFlag == 0) {
                        String[] arrProducts = new String[itemList.data.size()];
                        String[] arrpCode = new String[itemList.data.size()];
                        String[] arrpId = new String[itemList.data.size()];
                        String[] arrSoh = new String[itemList.data.size()];
                        for (int i = 0; i < itemList.data.size(); i++) {
                            arrProducts[i] = itemList.data.get(i).pName;
                            arrpCode[i] = String.valueOf(itemList.data.get(i).pCode);
                            arrSoh[i] = String.valueOf(round(itemList.data.get(i).SOH, 2));
                            arrpId[i] = String.valueOf(itemList.data.get(i).pId);
                        }
                        AutoCompleteRetailProductCustomAdapter myAdapter = new AutoCompleteRetailProductCustomAdapter(RetailSOActivity.this, R.layout.autocomplete_retail_row, arrProducts, arrpCode, arrSoh, arrpId);
                        acvItemSearchSOActivity.setAdapter(myAdapter);
                        acvItemSearchSOActivity.showDropDown();
                    } else {
                        tsMessages(itemList.errorMessage);
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
        //btnSave.setEnabled(false);
        //btnAllClear.setEnabled(false);
        //acvItemSearchSOActivity.setEnabled(false);
        //ic_search.setEnabled(false);
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
                        sList.clear();
                        lvProductlist.setAdapter(null);
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
    }

    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(RetailSOActivity.this);
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

    private void popUp(String msg) {
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

}
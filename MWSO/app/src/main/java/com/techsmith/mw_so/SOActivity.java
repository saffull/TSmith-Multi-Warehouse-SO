package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.techsmith.mw_so.utils.AllocateQty;
import com.techsmith.mw_so.utils.AllocateQtyPL;
import com.techsmith.mw_so.utils.AppConfigSettings;
import com.techsmith.mw_so.utils.AutoCompleteProductListCustomAdapter;
import com.techsmith.mw_so.utils.ItemDetails;
import com.techsmith.mw_so.utils.ItemList;
import com.techsmith.mw_so.utils.SOActivityArrayAdapter;
import com.techsmith.mw_so.utils.SOMemo;
import com.techsmith.mw_so.utils.SOPL;
import com.techsmith.mw_so.utils.SOSave;
import com.techsmith.mw_so.utils.SaveItemSO;
import com.techsmith.mw_so.utils.SaveSODetail;
import com.techsmith.mw_so.utils.SaveSOResponse;
import com.techsmith.mw_so.utils.SaveSummarySO;
import com.techsmith.mw_so.utils.UserPL;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SOActivity extends AppCompatActivity {

    SharedPreferences prefs;
    String loginResponse, filter = "", Url = "", strGetItems, strErrorMsg, strSaveMiniSO,billRemarks,
            strReceivables, CustomerName, strGetItemDetail, itemName, strGetTotal, itemCode, soString = "", cceId = "", machineId = "";

    public Double itemSoh, total = 0.0;// item made public so that to access in its adapter class
    public String itemMrp;// item made public so that to access in its adapter class
    Boolean isRepeat = false;
    EditText etQty;
    int CustomerId, itemId, itemQty, selectedQty;
    ImageButton ic_search;
    ItemList itemList;
    ItemDetails itemDetails;
    TextView cashDisc, volDisc, allocStore, tvRate, whText, tvSelectedItemName, tvMrp, whSoh, offers;
    public TextView tvAmountValue; // item made public so that to access in its adapter class
    SOPL soplObj;
    Button caculate, btnAdd, btnAllClear, btnSave;
    List<String> offerList, houseList, sohList;
    TextView tvCustomerName, tvDate, Freeqty;
    AutoCompleteTextView acvItemSearchSOActivity;
    ProgressDialog pDialog;
    ImageButton imgBtnRemarksPrescrptn;
    Dialog qtydialog, dialog;
    UserPL userPLObj;
    Double tsMsgDialogWindowHeight, saveDialogWindowHeight;
    public ListView lvProductlist;// item made public so that to access in its adapter class
    AllocateQty allocateQty;
    List<AllocateQtyPL> listSODetailPL;
    public ArrayList<AllocateQtyPL> detailList;/*list made common to both main & adapter class, so that UI changes done in adapter class
    eg: changing the qty or deleting get reflected globally*/
    HashMap<String, String> offerMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_so);
        getSupportActionBar().hide();
        prefs = PreferenceManager.getDefaultSharedPreferences(SOActivity.this);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        ic_search = findViewById(R.id.imgBtnSearchItem);
        lvProductlist = findViewById(R.id.lvProductlist);
        tvAmountValue = findViewById(R.id.tvAmountValue);
        btnAllClear = findViewById(R.id.btnAllClear);
        imgBtnRemarksPrescrptn=findViewById(R.id.imgBtnRemarksPrescrptn);
        btnSave = findViewById(R.id.btnSave);
        tvDate = findViewById(R.id.tvDate);
        loginResponse = prefs.getString("loginResponse", "");
        Gson gson = new Gson();
        userPLObj = gson.fromJson(loginResponse, UserPL.class);
        Url = prefs.getString("MultiSOURL", "");
        CustomerName = userPLObj.summary.customerName;
        CustomerId = userPLObj.summary.customerId;
        cceId = String.valueOf(userPLObj.summary.cceId);
        listSODetailPL = new ArrayList<>();
        detailList = new ArrayList<>();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screen_height = displayMetrics.heightPixels;
        int screen_width = displayMetrics.widthPixels;
        tsMsgDialogWindowHeight = Double.valueOf((screen_height * 38) / 100);
        saveDialogWindowHeight = (double) (screen_height * 42) / 100;

        acvItemSearchSOActivity = findViewById(R.id.acvItemSearchSOActivity);
        tvCustomerName.setText(CustomerName);
        tvDate.setText(String.valueOf(CustomerId));


        acvItemSearchSOActivity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int pos, long arg3) {
                try {

                    String SelectedText = acvItemSearchSOActivity.getText().toString();

                    if (SelectedText.length() >= 3) {
                        itemId = itemList.data.get(pos).pmid;
                        itemName = itemList.data.get(pos).product;
                        itemCode = itemList.data.get(pos).code;

                        itemMrp = String.valueOf(itemList.data.get(pos).mrp);
                        itemSoh = itemList.data.get(pos).sohInPacks;
                        for (int i = 0; i < detailList.size(); i++) {
                            if (detailList.get(i).itemId == itemId) {
                                Toast.makeText(SOActivity.this, "item already added..", Toast.LENGTH_LONG).show();
                                acvItemSearchSOActivity.setAdapter(null);
                                isRepeat = true;
                                return;
                            } else {
                                isRepeat = false;
                            }
                        }
                        if (!isRepeat)
                            new GetItemDetailsTask().execute();
                        else
                            Toast.makeText(SOActivity.this, "item already added..", Toast.LENGTH_LONG).show();

                    } else {
                        acvItemSearchSOActivity.setAdapter(null);
                    }
                } catch (Exception ex) {
                    Toast.makeText(SOActivity.this, "" + ex, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void SearchItem(View view) {
        filter = acvItemSearchSOActivity.getText().toString();
        if (filter.length() >= 3) {
            new GetItemsTask().execute();
        } else {
            Toast.makeText(SOActivity.this, "Add atleast 3 characters", Toast.LENGTH_LONG).show();
        }
    }

    public void ShowRemarks(View view) {
        try {
            prefs = PreferenceManager.getDefaultSharedPreferences(SOActivity.this);
            billRemarks = prefs.getString("BillRemarksWSSO", "");
            dialog = new Dialog(SOActivity.this);
            dialog.setContentView(R.layout.add_remarks);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setTitle("Remarks");
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


            ImageButton imgBtnCloseRemarksWindow = (ImageButton) dialog.findViewById(R.id.imgBtnCloseRemarksWindow);
            Button btnOkRemarks = (Button) dialog.findViewById(R.id.btnOkRemarks_Itemwise);
            Button btnClearRemarks_Itemwise = (Button) dialog.findViewById(R.id.btnClearRemarks_Itemwise);
            final EditText etAddRemarks = (EditText) dialog.findViewById(R.id.etAddRemarks_Itemwise);
            etAddRemarks.setText(billRemarks);


            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = Integer.parseInt(String.valueOf(saveDialogWindowHeight));
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
                    editor.putString("BillRemarksWSSO", billRemarks);
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
            Toast.makeText(SOActivity.this, "" + ex, Toast.LENGTH_SHORT).show();
        }
    }


    private class GetItemDetailsTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SOActivity.this);
            pDialog.setMessage("Loading item details...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                //https://tsmithy.in/somemouat/api/GetSOHAndSchemes?ItemId=14290&CustId=382
                URL url = new URL(Url + "GetSOHAndSchemes?ItemId=" + itemId + "&CustId=" + CustomerId);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setRequestProperty("authkey", AppConfigSettings.auth_id);
                connection.setRequestProperty("name", "");
                connection.setRequestProperty("password", "");
                connection.setRequestProperty("debugkey", "");
                connection.setRequestProperty("remarks", "");
                connection.setRequestProperty("machineid", "salam_ka@yahoo.com");
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


            itemDetails = new ItemDetails();
            if (strGetItemDetail == null || strGetItemDetail.isEmpty()) {
                Toast.makeText(SOActivity.this, "No result from web for item details", Toast.LENGTH_LONG).show();
            } else {
                if (itemDetails.statusFlag == 0) {
                    try {
                        soplObj = new SOPL();
                        Gson gson = new Gson();
                        soplObj = gson.fromJson(strGetItemDetail, SOPL.class);
                        offerList = new ArrayList<>();
                        houseList = new ArrayList<>();
                        sohList = new ArrayList<>();


                        for (int i = 0; i < soplObj.data.size(); i++) {
                            houseList.add(soplObj.data.get(i).storeName);
                            sohList.add(String.valueOf(soplObj.data.get(i).SOH));
                        }

                        for (int j = 0; j < soplObj.data.size(); j++) {
                            if (soplObj.data.get(j).offerDesc == null)
                                System.out.println("Empty data came");
                            else
                                offerList.add(soplObj.data.get(j).offerDesc.replace("null", ""));
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    qtydialog = new Dialog(SOActivity.this);
                    qtydialog.setContentView(R.layout.quantity_selection_dialogwindow);
                    qtydialog.setCanceledOnTouchOutside(false);
                    qtydialog.setTitle("Quantity Selection");
                    qtydialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    acvItemSearchSOActivity.setAdapter(null);

                    tvSelectedItemName = qtydialog.findViewById(R.id.tvSelectedItemName);
                    offers = qtydialog.findViewById(R.id.offers);
                    tvMrp = qtydialog.findViewById(R.id.tvMrpInQtySelection);
                    tvRate = qtydialog.findViewById(R.id.tvSOHInQtySelection);
                    Freeqty = qtydialog.findViewById(R.id.Freeqty);
                    allocStore = qtydialog.findViewById(R.id.allocStore);
                    whText = qtydialog.findViewById(R.id.whText);
                    whSoh = qtydialog.findViewById(R.id.whSoh);

                    cashDisc = qtydialog.findViewById(R.id.cashDisc);
                    volDisc = qtydialog.findViewById(R.id.volDisc);

                    ImageButton imgBtnCloseQtySelection = qtydialog.findViewById(R.id.imgBtnCloseQtySelection);
                    ImageButton btnPlus = qtydialog.findViewById(R.id.imgBtnPlusPack);
                    ImageButton btnMinus = qtydialog.findViewById(R.id.imgBtnMinusPack);
                    caculate = qtydialog.findViewById(R.id.caculate);
                    btnAdd = qtydialog.findViewById(R.id.btnAddItem_qtySelection);

                    etQty = qtydialog.findViewById(R.id.etQty);
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(qtydialog.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.MATCH_PARENT;

                    lp.gravity = Gravity.CENTER;
                    qtydialog.getWindow().setAttributes(lp);

                    etQty.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            System.out.println(s.toString());
                            btnAdd.setEnabled(false);
                        }
                    });

                    etQty.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            etQty.setSelection(0, etQty.getText().toString().length());

                        }
                    });

                    tvSelectedItemName.setText("" + itemName);
                    selectedQty = Integer.parseInt(etQty.getText().toString());
                    if (selectedQty > 0)
                        new AllocateQtyTask().execute();

                    caculate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            btnAdd.setEnabled(true);
                            selectedQty = Integer.parseInt(etQty.getText().toString());
                            if (selectedQty < 1)
                                Toast.makeText(SOActivity.this, "Add atleast 1", Toast.LENGTH_LONG).show();
                            else
                                new AllocateQtyTask().execute();

                        }
                    });
                    imgBtnCloseQtySelection.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            qtydialog.dismiss();
                        }
                    });
                    etQty.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            etQty.setSelection(0, etQty.getText().toString().length());

                        }
                    });
                    btnPlus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            try {
                                if (etQty.getText().toString().equals("") || etQty.getText().toString() == null) {
                                    etQty.setText("0");
                                }

                                itemQty = Integer.parseInt(etQty.getText().toString());
                                itemQty += 1;
                                etQty.setText("" + itemQty);
                                if (itemQty > 0) {
                                }

                            } catch (Exception ex) {
                                Toast.makeText(SOActivity.this, "" + ex, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    btnMinus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if (!etQty.getText().toString().equals("") && etQty.getText().toString() != null) {

                                    itemQty = Integer.parseInt(etQty.getText().toString());
                                    if (itemQty >= 1)
                                        itemQty -= 1;

                                    etQty.setText("" + itemQty);

                                } else {
                                    Toast.makeText(SOActivity.this, "Qty field is empty", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception ex) {
                                Toast.makeText(SOActivity.this, "" + ex, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    btnAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if (pDialog.isShowing())
                                    pDialog.dismiss();
                                if (etQty.getText().toString().equals("")) {
                                    Toast.makeText(SOActivity.this, "Qty cannot be empty", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (Integer.parseInt(etQty.getText().toString()) < 1) {
                                    Toast.makeText(SOActivity.this, "Qty cannot be less than 1", Toast.LENGTH_SHORT).show();
                                    return;
                                } else {
                                    allocateQty.data.productName = itemName;
                                    allocateQty.data.MRP = Double.parseDouble(itemMrp);
                                    allocateQty.data.code = Integer.parseInt(itemCode);
                                    allocateQty.data.storeId = String.valueOf(allocateQty.data.allocStore);
                                    detailList.add(allocateQty.data);
                                    listSODetailPL.add(allocateQty.data);
                                    total = total + (allocateQty.data.qty * Double.parseDouble(itemMrp));

                                    String[] from = {"SlNo,ItemName", "BatchCode", "ExpiryDate", "MRP", "BillingRate", "TaxPer", "TaxAmount", "TotalDisc", "LineTotalAmount"};
                                    int[] to = {R.id.itemcode, R.id.name, R.id.batchcode, R.id.batchbarcode, R.id.location, R.id.uperpack, R.id.expiry, R.id.sysstock, R.id.currentsoh};

                                    SOActivityArrayAdapter arrayAdapter = new SOActivityArrayAdapter(SOActivity.this, R.layout.list_row, listSODetailPL,
                                            listSODetailPL.size(), detailList, total);
                                    lvProductlist.setAdapter(arrayAdapter);
                                    arrayAdapter.notifyDataSetChanged();
                                    tvAmountValue.setText(String.format("%.2f", total));

                                    qtydialog.dismiss();
                                    acvItemSearchSOActivity.setText("");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    qtydialog.show();


                } else {
                    Toast.makeText(SOActivity.this, itemDetails.errorMessage, Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    private class AllocateQtyTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SOActivity.this);
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

                //https://tsmithy.in/somemouat/api/AllocateQty
                URL url = new URL(Url + "AllocateQty");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setRequestProperty("authkey", AppConfigSettings.auth_id);
                connection.setRequestProperty("name", "");
                connection.setRequestProperty("password", "");
                connection.setRequestProperty("debugkey", "");
                connection.setRequestProperty("remarks", "");
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
                        System.out.println("Response of AllocateQty is --->" + sb.toString());
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

                Gson gson = new Gson();
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
            pDialog = new ProgressDialog(SOActivity.this);
            pDialog.setMessage("Loading items...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Url + "GetProduct?name=" + filter);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setRequestProperty("authkey", AppConfigSettings.auth_id);
                connection.setRequestProperty("name", "");
                connection.setRequestProperty("password", "");
                connection.setRequestProperty("debugkey", "");
                connection.setRequestProperty("remarks", "");
                connection.setRequestProperty("machineid", "salam_ka@yahoo.com");
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
                    Toast.makeText(SOActivity.this, "" + strErrorMsg, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (strGetItems == null || strGetItems.equals("")) {
                    tsMessages("No result from web");
                } else {
                    Gson gson = new Gson();
                    itemList = gson.fromJson(strGetItems, ItemList.class);
                    if (itemList.statusFlag == 0) {
                        String[] arrProducts = new String[itemList.data.size()];
                        for (int i = 0; i < itemList.data.size(); i++) {
                            arrProducts[i] = itemList.data.get(i).product;
                        }
                        AutoCompleteProductListCustomAdapter myAdapter = new AutoCompleteProductListCustomAdapter(SOActivity.this, R.layout.autocomplete_view_row, arrProducts);
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

    public void SaveSO(View view) {
        if (detailList.size() > 0) {
            listSODetailPL = detailList;
            SaveSODetail saveSODetail = new SaveSODetail();
            saveSODetail.item = listSODetailPL;

            SaveSummarySO saveSummarySO = new SaveSummarySO();
            saveSummarySO.custId = CustomerId;
            saveSummarySO.customer = CustomerName;
            saveSummarySO.docSeries = "SOM";
            saveSummarySO.docDate = "26-10-2021";
            saveSummarySO.docComplete = 1;
            saveSummarySO.docSeries = "";
            saveSummarySO.remarks = "Testing DOC";
            saveSummarySO.cceId = cceId;
            saveSummarySO.machineId = "";
            saveSummarySO.userId = "";


            SOMemo soMemo = new SOMemo();
            soMemo.detail = saveSODetail;
            soMemo.summary = saveSummarySO;


            SOSave soSave = new SOSave();
            soSave.soMemo = soMemo;


            Gson gson = new Gson();
            soString = gson.toJson(soSave);
            System.out.println(soString);
            new SaveSOTask().execute();
        } else {
            Toast.makeText(SOActivity.this, "Add to the list", Toast.LENGTH_LONG).show();
        }


        //tsMessages("Function not yet implemented...");
    }

    private class SaveSOTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SOActivity.this);
            pDialog.setMessage("Saving SO...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            //https://tsmithy.in/somemouat/api/SaveSO
            try {

                URL url = new URL(Url + "SaveSO");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("authkey", AppConfigSettings.auth_id);
                connection.setRequestProperty("name", "");
                connection.setRequestProperty("password", "");
                connection.setRequestProperty("debugkey", "");
                connection.setRequestProperty("remarks", "");
                connection.setRequestProperty("machineid", "salam_ka@yahoo.com");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setDoOutput(true);

                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                wr.write(soString);
                wr.flush();

                connection.connect();

                int responsecode = connection.getResponseCode();
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
                        strSaveMiniSO = sb.toString();
                        System.out.println("SO Saving Response is " + sb.toString());

                    } else {
                        strErrorMsg = connection.getResponseMessage();
                        strSaveMiniSO = "httperror";
                    }

                } finally {
                    connection.disconnect();
                }

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
            return strSaveMiniSO;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();
            try {
                Gson gson = new Gson();

                SaveSOResponse saveSOResponse;
                saveSOResponse = gson.fromJson(strSaveMiniSO, SaveSOResponse.class);
                if (saveSOResponse.statusFlag == 0) {
                    dialog = new Dialog(SOActivity.this);
                    dialog.setContentView(R.layout.save_dialogwindow);
//                        dialog.setCanceledOnTouchOutside(false);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setTitle("Save");
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

//                        ImageButton imgBtnCloseSaveWindow = (ImageButton) dialog.findViewById(R.id.imgBtnCloseSaveWindow);
                    Button btnOkSavePopup = dialog.findViewById(R.id.btnOkSavePopup);
                    TextView tvSaveStatus = dialog.findViewById(R.id.tvSaveStatus);

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(dialog.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = Integer.parseInt(String.valueOf(saveDialogWindowHeight));
                    lp.gravity = Gravity.CENTER;
                    dialog.getWindow().setAttributes(lp);

                    btnOkSavePopup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            disableButtons();
                            dialog.dismiss();
                        }
                    });

                    // tvSaveStatus.setText("Saved \n\nToken No: " + tokenNo);
                    tvSaveStatus.setText("Saved\n\n SOMemoNo:\t" + saveSOResponse.data.get(saveSOResponse.data.size() - 1).SOMemoNo);
                    tvSaveStatus.setMovementMethod(new ScrollingMovementMethod());
                    dialog.show();

                } else {
                    tsMessages(saveSOResponse.errorMessage);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void disableButtons() {
        btnSave.setEnabled(false);
        btnAllClear.setEnabled(false);
    }


    public void ClearList(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SOActivity.this);
        alertDialogBuilder.setMessage("Do you want to delete the full list..?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                detailList.clear();
                total = total * 0.0;
                SOActivityArrayAdapter arrayAdapter = new SOActivityArrayAdapter(SOActivity.this, R.layout.list_row, listSODetailPL,
                        listSODetailPL.size(), detailList, total);
                lvProductlist.setAdapter(arrayAdapter);
                arrayAdapter.notifyDataSetChanged();
                tvAmountValue.setText("");

            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void Restart(View view) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SOActivity.this);
        alertDialogBuilder.setMessage("Do you want to start a new SO..?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);

            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(SOActivity.this);
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
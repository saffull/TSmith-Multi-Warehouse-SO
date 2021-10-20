package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
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
import com.techsmith.mw_so.utils.CustomerReceivables;
import com.techsmith.mw_so.utils.ItemDetails;
import com.techsmith.mw_so.utils.ItemList;
import com.techsmith.mw_so.utils.SOActivityAdapter;
import com.techsmith.mw_so.utils.SOActivityArrayAdapter;
import com.techsmith.mw_so.utils.SOPL;
import com.techsmith.mw_so.utils.UserPL;

import org.json.JSONArray;
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
    String loginResponse, filter = "", Url = "", strGetItems, strErrorMsg,
            strReceivables, CustomerName, strGetItemDetail, itemName, strGetTotal;

    public Double itemSoh, total = 0.0;// item made public so that to access in its adapter class
    public String itemMrp;// item made public so that to access in its adapter class
    Boolean isRepeat = false;
    int CustomerId, itemId, itemQty, selectedQty;
    ImageButton ic_search;
    ItemList itemList;
    ItemDetails itemDetails;
    TextView cashDisc, volDisc, allocStore, tvSOH, whText, tvSelectedItemName, tvMrp, whSoh, offers;
    public TextView tvAmountValue; // item made public so that to access in its adapter class
    SOPL soplObj;
    Button caculate;
    List<String> offerList, houseList, sohList;
    TextView tvCustomerName, tvDate, Freeqty;
    AutoCompleteTextView acvItemSearchSOActivity;
    ProgressDialog pDialog;
    Dialog qtydialog;
    UserPL userPLObj;
    Double tsMsgDialogWindowHeight;
    public ListView lvProductlist;// item made public so that to access in its adapter class
    AllocateQty allocateQty;
    List<AllocateQtyPL> listSODetailPL;
    ArrayList<AllocateQtyPL> detailList;
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
        tvDate = findViewById(R.id.tvDate);
        loginResponse = prefs.getString("loginResponse", "");
        Gson gson = new Gson();
        userPLObj = gson.fromJson(loginResponse, UserPL.class);
        Url = prefs.getString("MultiSOURL", "");
        CustomerName = userPLObj.summary.customerName;
        CustomerId = userPLObj.summary.customerId;
        listSODetailPL = new ArrayList<>();
        detailList = new ArrayList<>();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screen_height = displayMetrics.heightPixels;
        int screen_width = displayMetrics.widthPixels;
        tsMsgDialogWindowHeight = Double.valueOf((screen_height * 38) / 100);

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

    public void ClearSearch(View view) {
        acvItemSearchSOActivity.setText("");
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
                    tvSOH = qtydialog.findViewById(R.id.tvSOHInQtySelection);
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
                    Button btnAdd = qtydialog.findViewById(R.id.btnAddItem_qtySelection);

                    EditText etQty = qtydialog.findViewById(R.id.etQty);
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(qtydialog.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.MATCH_PARENT;

                    lp.gravity = Gravity.CENTER;
                    qtydialog.getWindow().setAttributes(lp);

                    tvSelectedItemName.setText("" + itemName);
                    selectedQty = Integer.parseInt(etQty.getText().toString());
                    if (selectedQty > 0)
                        new AllocateQtyTask().execute();

                    caculate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            btnAdd.setEnabled(true);
                            selectedQty = Integer.parseInt(etQty.getText().toString());
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
                                    detailList.add(allocateQty.data);
                                    listSODetailPL.add(allocateQty.data);
                                    total = total + (allocateQty.data.qty * Double.parseDouble(itemMrp));

                                    //SOActivityAdapter soActivityAdapter = new SOActivityAdapter(SOActivity.this, strGetTotal, itemName, offerList, itemMrp, houseList, itemId, sohList, jsonArray);
                                    //lvProductlist.setAdapter(soActivityAdapter);
                                    String[] from = {"SlNo,ItemName", "BatchCode", "ExpiryDate", "MRP", "BillingRate", "TaxPer", "TaxAmount", "TotalDisc", "LineTotalAmount"};
                                    int[] to = {R.id.itemcode, R.id.name, R.id.batchcode, R.id.batchbarcode, R.id.location, R.id.uperpack, R.id.expiry, R.id.sysstock, R.id.currentsoh};

                                    SOActivityArrayAdapter arrayAdapter = new SOActivityArrayAdapter(SOActivity.this, R.layout.list_row, listSODetailPL,
                                            listSODetailPL.size(), detailList, total);
                                    lvProductlist.setAdapter(arrayAdapter);
                                    arrayAdapter.notifyDataSetChanged();
                                    tvAmountValue.setText(String.valueOf(total));

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
                cashDisc.setText("Cash Disc - " + allocateQty.data.cashDiscPer + "%");
                volDisc.setText("Vol Disc - " + allocateQty.data.volDiscPer + "%");
                tvSOH.setText("Soh : " + String.format("%.2f", allocateQty.data.soh));
                allocStore.setText("Allocated Warehouse : " + allocateQty.data.allocStoreCode);
                Freeqty.setText("Free Quantity: " + allocateQty.data.freeQty);

                try {
                    tvSelectedItemName.setText("" + itemName);
                    tvMrp.setText("MRP : " + itemMrp);

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

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(SOActivity.this);
            dialog.setContentView(R.layout.ts_message_dialouge);
//            dialog.setCanceledOnTouchOutside(false);
            dialog.setCanceledOnTouchOutside(true);
//            dialog.setTitle("Save");
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            ImageButton imgBtnCloseSaveWindow = (ImageButton) dialog.findViewById(R.id.imgBtnClosetsMsgWindow);
            TextView tvMsgTodisplay = (TextView) dialog.findViewById(R.id.tvTsMessageDisplay);

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
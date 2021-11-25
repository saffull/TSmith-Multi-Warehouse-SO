package com.techsmith.mw_so;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.techsmith.mw_so.collection_utils.Collection;
import com.techsmith.mw_so.collection_utils.CollectionPL;
import com.techsmith.mw_so.collection_utils.FillAmount;
import com.techsmith.mw_so.collection_utils.SOAutoFillAdapter;
import com.techsmith.mw_so.collection_utils.SOCollectionAdapter;
import com.techsmith.mw_so.utils.AppConfigSettings;
import com.techsmith.mw_so.utils.AutocompleteCustomArrayAdapter;
import com.techsmith.mw_so.utils.CustomerList;
import com.techsmith.mw_so.utils.UserPL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Collections extends AppCompatActivity {
    EditText cashAmount;
    TextInputEditText userInput;
    RadioButton cash, cheque;
    Gson gson;
    Double TempAutoFillAmount, total = 0.0;
    Collection collection;
    private Spinner storeSelect;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout linearLayoutBSheet, ll;
    private ToggleButton tbUpDown;
    private Button save, reset, remarks, autoFill;
    public TextView tvAmountValue;
    SharedPreferences prefs;
    public ListView lvCollectionlist;
    ProgressDialog pDialog;
    SOCollectionAdapter arrayAdapter;
    AutoCompleteTextView acvCustomerName;
    String StoreId, etCustomerId, Url, multiSOStoredDevId, loginResponse, strErrorMsg, strCollections, strCustomer;
    UserPL userPLObj;
    public String[] stores, amt;
    int i = 0, edPos, autoCount = 0;
    HashMap<String, String> storeMap;
    List<CollectionPL> listCollectionPL;
    public ArrayList<CollectionPL> detailList;

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(Collections.this, Category.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collections);
        getSupportActionBar().hide();
        prefs = PreferenceManager.getDefaultSharedPreferences(Collections.this);
        cashAmount = findViewById(R.id.cashAmount);
        acvCustomerName = findViewById(R.id.acvCustomerName);
        cash = findViewById(R.id.radioCOLCash);
        cheque = findViewById(R.id.radioCOLCheque);
        storeSelect = findViewById(R.id.storeSelect);
        ll = findViewById(R.id.paymentDetails);
        autoFill = findViewById(R.id.btnCOL_AutoFill);
        listCollectionPL = new ArrayList<>();
        detailList = new ArrayList<>();
        lvCollectionlist = findViewById(R.id.lvCollectionlist);
        loginResponse = prefs.getString("loginResponse", "");
        gson = new Gson();
        userPLObj = gson.fromJson(loginResponse, UserPL.class);
        Url = prefs.getString("MultiSOURL", "");
        multiSOStoredDevId = prefs.getString("MultiSOStoredDevId", "");
        storeMap = new HashMap<>();

        init();

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

        try {
            gson = new Gson();
            if (userPLObj.detail.size() > 0)
                stores = new String[userPLObj.detail.size() + 1];
            else
                stores = new String[5000];
            userPLObj = gson.fromJson(loginResponse, UserPL.class);
            acvCustomerName.setText(userPLObj.summary.customerName);
            stores[0] = "Select Store";
            for (int i = 0; i < userPLObj.detail.size(); i++) {
                stores[i + 1] = userPLObj.detail.get(i).storeName;
                storeMap.put(userPLObj.detail.get(i).storeName, String.valueOf(userPLObj.detail.get(i).storeId));
            }
            ArrayAdapter ad
                    = new ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    stores);

            ad.setDropDownViewResource(
                    android.R.layout
                            .simple_spinner_dropdown_item);

            storeSelect.setAdapter(ad);
            storeSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    System.out.println("Stores position is " + stores[position] + storeMap.get(stores[position]));
                    edPos = position;
                    StoreId = storeMap.get(stores[position]);
                    if (StoreId != null && position > 0) {
                        autoFill.setEnabled(true);
                        new TakeCollectionTask().execute();
                    } else if (position == 0) {
                        autoFill.setEnabled(false);
                        lvCollectionlist.setAdapter(null);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    System.out.println("Nothing selected");

                }
            });

            if (userPLObj.summary.customerId != 0)
                etCustomerId = String.valueOf(userPLObj.summary.customerId);
            else
                etCustomerId = "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-message"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mTotalReceiver, new IntentFilter("custom-total"));

        // new TakeCollectionTask().execute();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Collections.this, "Functionality Not Implemented..", Toast.LENGTH_SHORT).show();
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        remarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Collections.this, "Functionality Not Implemented..", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public BroadcastReceiver mTotalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String command = intent.getStringExtra("command");
                if (command.equalsIgnoreCase("ok")) {

                    calculateTotal();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }

    };

    private void init() {
        this.linearLayoutBSheet = findViewById(R.id.bottomSheet);
        this.bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBSheet);
        this.tbUpDown = findViewById(R.id.toggleButton);
        this.save = findViewById(R.id.btnCOL_Save);
        this.reset = findViewById(R.id.btnCOL_Reset);
        this.remarks = findViewById(R.id.btnCOL_Remarks);
        this.tvAmountValue = findViewById(R.id.tvAmountValue);
    }


    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        String str = "";
        switch (view.getId()) {
            case R.id.radioCOLCash:
                if (checked) {
                    if (autoCount > 0)
                        cashAmount.setVisibility(View.GONE);
                    else
                        cashAmount.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.radioCOLCheque:
                if (checked)
                    cashAmount.setVisibility(View.GONE);
                callDialog();

                break;
            default:
                break;
        }
    }

    private void callDialog() {
        LayoutInflater li = LayoutInflater.from(Collections.this);
        View promptsView = li.inflate(R.layout.cheque_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Collections.this);
        alertDialogBuilder.setView(promptsView);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void AutoFillAmount(View view) {
        LayoutInflater li = LayoutInflater.from(Collections.this);
        View promptsView = li.inflate(R.layout.autofill_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Collections.this);
        alertDialogBuilder.setView(promptsView);
        userInput = promptsView.findViewById(R.id.etUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TempAutoFillAmount = 0.0;
                        TempAutoFillAmount = Double.parseDouble(userInput.getText().toString());
                        new AutoFillTask().execute();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void resetSpinner(View view) {
        storeSelect.setSelection(0);
        total=0.0;
        tvAmountValue.setText("");
        autoCount=0;

    }


    private class AutoFillTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Collections.this);
            pDialog.setMessage("AutoFilling..Please wait.!!");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Url + "CollectionAutoFill?CustId=382&StoreId=" + StoreId + "&AmountToFill=" + TempAutoFillAmount);
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
                        strCollections = sb.toString();
                        System.out.println("Response of  collection Auto Fill  is--->" + strCollections);
                    } else {
//                        strErrorMsg = connection.getResponseMessage();
                        strErrorMsg = responseMsg;
                        strCollections = "httperror";
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return strCollections;
        }

        @Override
        protected void onPostExecute(String s) {
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            if (strCollections == null || strCollections.isEmpty()) {
                Toast.makeText(Collections.this, "No result from web", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    gson = new Gson();
                    FillAmount fillAmount = gson.fromJson(strCollections, FillAmount.class);
                    if (fillAmount.statusFlag == 0) {
                        autoCount++;
                        detailList = new ArrayList<>();
                        detailList = (ArrayList<CollectionPL>) fillAmount.data;
                        lvCollectionlist.setAdapter(null);
                        SOAutoFillAdapter arrayFillAdapter = new SOAutoFillAdapter(Collections.this, R.layout.list_row, detailList);
                        lvCollectionlist.setAdapter(arrayFillAdapter);
                        arrayFillAdapter.notifyDataSetChanged();
                    } else {
                        lvCollectionlist.setAdapter(null);
                        tsMessages(collection.errorMessage);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class TakeCollectionTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Collections.this);
            pDialog.setMessage("Collecting..Please wait.!!");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Url + "CustomerReceivablesForCollection?CustId=382&StoreId=" + StoreId);
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
                        strCollections = sb.toString();
                        System.out.println("Response of Store collection is--->" + strCollections);
                    } else {
//                        strErrorMsg = connection.getResponseMessage();
                        strErrorMsg = responseMsg;
                        strCollections = "httperror";
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return strCollections;
        }

        @Override
        protected void onPostExecute(String s) {
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            if (strCollections == null || strCollections.isEmpty()) {
                Toast.makeText(Collections.this, "No result from web", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    gson = new Gson();
                    collection = gson.fromJson(strCollections, Collection.class);
                    /*ObjectMapper om = new ObjectMapper();
                    collection = om.readValue(strCollections, Collection.class);*/
                    if (collection.statusFlag == 0) {
                        listCollectionPL = collection.data;
                        detailList = (ArrayList<CollectionPL>) collection.data;
                        amt = new String[detailList.size()];
                        for (int i = 0; i < detailList.size(); i++) {
                            amt[i] = "0";
                        }

                        for (i = 0; i < collection.data.size(); i++) {
                            System.out.println(collection.data.get(i).Balance);
                            // detailList.add(collection.data.get(i));
                            //System.out.println(listCollectionPL.get(i).docDate);

                        }
                        arrayAdapter = new SOCollectionAdapter(Collections.this, R.layout.list_row, detailList);
                        lvCollectionlist.setAdapter(arrayAdapter);
                        arrayAdapter.notifyDataSetChanged();

                    } else {
                        lvCollectionlist.setAdapter(null);
                        tsMessages(collection.errorMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        }
    }

    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(Collections.this);
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

    private class GetCustomerListTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(Collections.this);
            pDialog.setMessage("Loading customers...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            //https://tsmithy.in/somemouat/api/GetCustomer?name=m%20g%20medic
            try {
                URL url = new URL(Url + "GetCustomer?name=" + acvCustomerName.getText().toString().trim());
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

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (strCustomer == null || strCustomer.equals("")) {

                Toast.makeText(Collections.this, "No result from web", Toast.LENGTH_SHORT).show();

            } else {

                Gson gson = new Gson();
                CustomerList customerList = gson.fromJson(strCustomer, CustomerList.class);

                if (customerList.statusFlag == 0) {

                    try {
                        String[] arrCust = new String[customerList.data.size()];

                        for (int i = 0; i < customerList.data.size(); i++) {
                            arrCust[i] = customerList.data.get(i).customer;
                            System.out.println(arrCust[i]);
                        }


                        AutocompleteCustomArrayAdapter myAdapter = new AutocompleteCustomArrayAdapter(Collections.this, R.layout.autocomplete_view_row, arrCust);
                        acvCustomerName.setAdapter(myAdapter);
                        acvCustomerName.showDropDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    //tsMessages(customerList.errorMessage);
                }
            }

        }
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                double qty = intent.getDoubleExtra("sum", 0.0);
                System.out.println("Sum Recieved is " + qty);
                if (qty > 0)
                    total = total + qty;
                else
                    total = total - (-1 * qty);
                tvAmountValue.setText(String.valueOf(total));
                //calculateTotal();

            } catch (Exception e) {
                e.printStackTrace();
            }


        }

    };

    private void calculateTotal() {
        tvAmountValue.setText("");
        total = 0.0;
        for (int i = 0; i < detailList.size(); i++) {
            View view = lvCollectionlist.getChildAt(i);
            EditText editText = view.findViewById(R.id.pay);
            String string = editText.getText().toString();
            if (!string.equals(""))
                total += Double.parseDouble(string);
            tvAmountValue.setText(String.valueOf(total));
        }
    }
}
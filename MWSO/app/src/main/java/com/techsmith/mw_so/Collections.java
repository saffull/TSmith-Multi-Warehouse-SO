package com.techsmith.mw_so;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.techsmith.mw_so.collection_utils.Collection;
import com.techsmith.mw_so.collection_utils.CollectionPL;
import com.techsmith.mw_so.collection_utils.SOCollectionAdapter;
import com.techsmith.mw_so.utils.AppConfigSettings;
import com.techsmith.mw_so.utils.AutocompleteCustomArrayAdapter;
import com.techsmith.mw_so.utils.CustomerList;
import com.techsmith.mw_so.utils.SOActivityArrayAdapter;
import com.techsmith.mw_so.utils.UserPL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Collections extends AppCompatActivity {
    EditText cashAmount, autofill, userInput;
    RadioButton cash, cheque;
    Gson gson;
    Collection collection;
    private Spinner storeSelect;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout linearLayoutBSheet, ll;
    private ToggleButton tbUpDown;
    private Button save, reset, remarks, autoFill;
    private TextView tvAmountValue;
    SharedPreferences prefs;
    ListView lvCollectionlist;
    ProgressDialog pDialog;
    SOCollectionAdapter arrayAdapter;
    AutoCompleteTextView acvCustomerName;
    String username, etCustomerId, Url, multiSOStoredDevId, loginResponse, strErrorMsg, strCollections, strCustomer;
    UserPL userPLObj;
    String[] stores;
    HashMap<String, String> storeMap;
    List<CollectionPL> listCollectionPL;

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
                stores = new String[userPLObj.detail.size()];
            else
                stores = new String[5000];
            userPLObj = gson.fromJson(loginResponse, UserPL.class);
            acvCustomerName.setText(userPLObj.summary.customerName);
            for (int i = 0; i < userPLObj.detail.size(); i++) {
                stores[i] = userPLObj.detail.get(i).storeName;
                storeMap.put(userPLObj.detail.get(i).storeName, String.valueOf(userPLObj.detail.get(i).storeId));
            }
            System.out.println(stores[1]);
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
                    System.out.println(stores[position] + storeMap.get(stores[position]));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (userPLObj.summary.customerId != 0)
                etCustomerId = String.valueOf(userPLObj.summary.customerId);
            else
                etCustomerId = "";
        } catch (Exception e) {
            e.printStackTrace();
        }

        new TakeCollectionTask().execute();

    }

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
                if (checked)
                    cashAmount.setVisibility(View.VISIBLE);

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
        userInput = (EditText) promptsView.findViewById(R.id.etUserInput);

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
                URL url = new URL(Url + "CustomerReceivablesForCollection?CustId=382&StoreId=15");
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
                    //collection=gson.fromJson(strCollections,Collection.class);
                    ObjectMapper om = new ObjectMapper();
                    collection = om.readValue(strCollections, Collection.class);
                    if (collection.statusFlag == 0) {
                        listCollectionPL = collection.data;
                        for (int i = 0; i < collection.data.size(); i++) {
                            // System.out.println(collection.data.get(i).balance);
                            System.out.println(listCollectionPL.get(i).docDate);
                             arrayAdapter = new SOCollectionAdapter(Collections.this, R.layout.list_row, listCollectionPL,
                                    listCollectionPL.size());
                            lvCollectionlist.setAdapter(arrayAdapter);
                            arrayAdapter.notifyDataSetChanged();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


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
}
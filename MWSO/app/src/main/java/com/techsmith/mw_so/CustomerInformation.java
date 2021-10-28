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
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.techsmith.mw_so.utils.AppConfigSettings;
import com.techsmith.mw_so.utils.AutocompleteCustomArrayAdapter;
import com.techsmith.mw_so.utils.CustomerList;
import com.techsmith.mw_so.utils.CustomerReceivables;
import com.techsmith.mw_so.utils.UserPL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CustomerInformation extends AppCompatActivity {
    SharedPreferences prefs;
    AutoCompleteTextView acvCustomerName;
    ImageButton imgBtnCustSearchbyName;
    EditText etCustomerId, etCustomerAdrs, etCustomerMobile, etCustomerGSTNo, etReceivables;
    Button btnCreateSO;
    String loginResponse, Url, strCustomer, strErrorMsg, selectedCustomerId, strReceivables;
    ProgressDialog pDialog;
    CustomerList customerList;
    Double tsMsgDialogWindowHeight;
    CustomerReceivables customerReceivables;
    UserPL userPLObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_information);
        getSupportActionBar().hide();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screen_height = displayMetrics.heightPixels;
        int screen_width = displayMetrics.widthPixels;
        tsMsgDialogWindowHeight = Double.valueOf((screen_height * 38) / 100);

        prefs = PreferenceManager.getDefaultSharedPreferences(CustomerInformation.this);
        acvCustomerName = findViewById(R.id.acvCustomerName);
        imgBtnCustSearchbyName = findViewById(R.id.imgBtnCustSearchbyName);
        etCustomerId = findViewById(R.id.etCustomerId);
        etCustomerAdrs = findViewById(R.id.etCustomerAdrs);
        etCustomerMobile = findViewById(R.id.etCustomerMobile);
        etCustomerGSTNo = findViewById(R.id.etGSTIN);
        btnCreateSO = findViewById(R.id.btnCreateSO);
        etReceivables = findViewById(R.id.etReceivables);
        loginResponse = prefs.getString("loginResponse", "");
        Url = prefs.getString("MultiSOURL", "");
        try {
            Gson gson = new Gson();
            userPLObj = gson.fromJson(loginResponse, UserPL.class);
            acvCustomerName.setText(userPLObj.summary.customerName);
            etCustomerId.setText(String.valueOf(userPLObj.summary.customerId));
            etCustomerAdrs.setText(userPLObj.summary.customerAddress);
            etCustomerGSTNo.setText(userPLObj.summary.customerGSTIN);
            etCustomerMobile.setText(userPLObj.summary.customerPhoneNo);
        } catch (Exception e) {
            e.printStackTrace();
        }


        imgBtnCustSearchbyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (acvCustomerName.getText().toString().equalsIgnoreCase("") || acvCustomerName.getText().toString().length() < 3) {

                    Toast.makeText(CustomerInformation.this, "Please input minimum 3 characters", Toast.LENGTH_SHORT).show();

                } else {
                    new GetCustomerListTask().execute();
                }
            }
        });
        acvCustomerName.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                    long id) {


                String selectedCustomer = (String) parent.getItemAtPosition(pos);
                selectedCustomerId = String.valueOf(customerList.data.get(pos).acid);
                etCustomerGSTNo.setText(customerList.data.get(pos).gstin);
                etCustomerId.setText(String.valueOf(customerList.data.get(pos).acid));
                etCustomerAdrs.setText(customerList.data.get(pos).customerAddress);
                etCustomerMobile.setText(customerList.data.get(pos).customerPhoneNo);


                prefs = PreferenceManager.getDefaultSharedPreferences(CustomerInformation.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("CustomerId", Integer.parseInt(selectedCustomerId));
                editor.putString("CustomerName", userPLObj.summary.customerName);
                editor.commit();
                editor.apply();


            }
        });

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CustomerInformation.this);
        alertDialogBuilder.setMessage("Do you want to logout..!!");
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                finish();
                startActivity(new Intent(CustomerInformation.this,MainActivity.class));
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

    public void ClearAll(View view) {
        acvCustomerName.setText("");
        etCustomerId.setText("");
        etCustomerMobile.setText("");
        etCustomerAdrs.setText("");
        etCustomerGSTNo.setText("");
    }

    public void CreateSO(View view) {
        finish();
        startActivity(new Intent(CustomerInformation.this, SOActivity.class));
    }

    public void GetReceivables(View view) {
        if (userPLObj.summary.customerId != 0) {
            new GetReceivablesTask().execute();
        }
    }

    private class GetReceivablesTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CustomerInformation.this);
            pDialog.setMessage("Loading...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            //https://tsmithy.in/somemouat/api/GetReceivables?CustId=382
            try {
                URL url = new URL(Url + "GetReceivables?CustId=" + userPLObj.summary.customerId);
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
                        strReceivables = sb.toString();
                        System.out.println("Response of Customer Recievables is--->" + strReceivables);
                    } else {
//                        strErrorMsg = connection.getResponseMessage();
                        strErrorMsg = responseMsg;
                        strReceivables = "httperror";
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return strReceivables;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (strReceivables == null || strReceivables.isEmpty()) {
                Toast.makeText(CustomerInformation.this, "No result from web", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    Gson gson = new Gson();
                    customerReceivables = gson.fromJson(strReceivables, CustomerReceivables.class);
                    if (customerReceivables.statusFlag == 0) {
                        String var = String.valueOf(customerReceivables.data.get(0).receivables);
                        etReceivables.setText(var);
                    } else {
                        tsMessages(customerReceivables.errorMessage);
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

            pDialog = new ProgressDialog(CustomerInformation.this);
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

                Toast.makeText(CustomerInformation.this, "No result from web", Toast.LENGTH_SHORT).show();

            } else {

                Gson gson = new Gson();
                customerList = gson.fromJson(strCustomer, CustomerList.class);

                if (customerList.statusFlag == 0) {

                    try {
                        String[] arrCust = new String[customerList.data.size()];

                        for (int i = 0; i < customerList.data.size(); i++) {
                            arrCust[i] = customerList.data.get(i).customer;
                            System.out.println(arrCust[i]);
                        }


                        AutocompleteCustomArrayAdapter myAdapter = new AutocompleteCustomArrayAdapter(CustomerInformation.this, R.layout.autocomplete_view_row, arrCust);
                        acvCustomerName.setAdapter(myAdapter);
                        acvCustomerName.showDropDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    tsMessages(customerList.errorMessage);
                }
            }

        }
    }

    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(CustomerInformation.this);
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
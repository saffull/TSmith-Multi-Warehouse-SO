package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.techsmith.mw_so.Global.AppWide;
import com.techsmith.mw_so.retail_utils.AutoCompleteRetailSalesReturnAdapter;
import com.techsmith.mw_so.retail_utils.RetailCustomerResponse;
import com.techsmith.mw_so.retail_utils.RetailReplyData;
import com.techsmith.mw_so.utils.AutocompleteRetailCustomArrayAdapter;

import java.io.BufferedReader;
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
    EditText place, pincode, gstno, cardType, etCustomerGoogleAdrs, cEmail, latLong;
    String Url, strCustomer, strErrorMsg, editData = "", LoyaltyCardType = "", customerAddress = "", sendTestData = "",
            selectedCustomerName, LoyaltyCode = "", LoyaltyId = "", LoyaltyCardTypeDesc = "", cLatitude = "",
            cLongitude = "", strfromweb = "", strerrormsg = "", className = "";
    ProgressDialog pDialog;
    RetailCustomerResponse customerResponse;
    AppWide appWide;
    AutoCompleteRetailSalesReturnAdapter myAdapter;
    RetailReplyData rcData;
    Gson gson;
    int currenPos;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retail_sales_return);
        prefs = PreferenceManager.getDefaultSharedPreferences(RetailSalesReturnActivity.this);
        appWide = AppWide.getInstance();
        imgBtnCustSearchbyName = findViewById(R.id.imgBtnCustSearchbyName);
        acvCustomerName = findViewById(R.id.acvCustomerName);
        acvmobileNo = findViewById(R.id.acvmobileNo);
        etCustomerGoogleAdrs = findViewById(R.id.etCustomerGoogleAdrs);
        Url = prefs.getString("MultiSOURL", "");


        imgBtnCustSearchbyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (acvCustomerName.getText().toString().equalsIgnoreCase("") || acvCustomerName.getText().toString().length() < 3) {

                    Toast.makeText(RetailSalesReturnActivity.this, "Please input minimum 3 characters", Toast.LENGTH_SHORT).show();

                } else {
                    hideKeyboard();
                    new GetCustomerListTask().execute();
                }
            }
        });
        acvCustomerName.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                    long id) {


                try {
                    String selectedCustomer = (String) parent.getItemAtPosition(pos);
                    selectedCustomerName = customerResponse.data.get(pos).cName;
                    ;
                    appWide.setName(selectedCustomerName);
                    LoyaltyCode = customerResponse.data.get(pos).cLoyaltyCode;
                    appWide.setLoyaltyCode(LoyaltyCode);
                    LoyaltyCardTypeDesc = customerResponse.data.get(pos).cLoyaltyCardTypeDesc;
                    LoyaltyId = String.valueOf(customerResponse.data.get(pos).cLoyaltyId);
                    // acvLcardNo.setText(customerResponse.data.get(pos).cLoyaltyCode);
                    LoyaltyCardType = String.valueOf(customerResponse.data.get(pos).cLoyaltyCardType);
                    acvmobileNo.setText(customerResponse.data.get(pos).aiPhone2);
                    //cardType.setText(customerResponse.data.get(pos).aiEmail);

                    String gender = customerResponse.data.get(pos).cGender;
                    cLatitude = String.valueOf(customerResponse.data.get(pos).cLatitude);
                    cLongitude = String.valueOf(customerResponse.data.get(pos).cLongitude);
                    customerAddress = customerResponse.data.get(pos).cGoogleAddress;
                    etCustomerGoogleAdrs.setText(customerResponse.data.get(pos).cGoogleAddress.substring(0, 15) + "...");


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        acvCustomerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() == 0) {
                    acvCustomerName.setAdapter(null);
                    clearFields();

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void GoBack(View view) {
    }

    private void clearFields() {
        // acvCustomerName.setText("");
        // acvmobileNo.setText("");
        // acvLcardNo.setText("");
        // etCustomerGoogleAdrs.setText("");
        // etCustomerAdrs.setText("");
        //cardType.setText("");
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


    private class GetCustomerListTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailSalesReturnActivity.this);
            pDialog.setMessage("Loading Retail Customers...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            editData = acvCustomerName.getText().toString().trim();
            try {
                //URL url = new URL(Url + "GetCustomer?name=" + acvCustomerName.getText().toString().trim());
                //URL url = new URL("https://tsmithy.in/dev/sbill/api/getcustomerlookup?name=" + editData);
                URL url = new URL(Url + "getcustomerlookup?name=" + editData);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setRequestProperty("authkey", appWide.getAuthID());
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

                Toast.makeText(RetailSalesReturnActivity.this, "No result from web", Toast.LENGTH_SHORT).show();

            } else {
                try {
                    Gson gson = new Gson();
                    customerResponse = gson.fromJson(strCustomer, RetailCustomerResponse.class);
                    if (customerResponse.statusFlag == 0) {
                        String[] arrCust = new String[customerResponse.data.size()];
                        String[] arrMob = new String[customerResponse.data.size()];

                        for (int i = 0; i < customerResponse.data.size(); i++) {
                            arrCust[i] = customerResponse.data.get(i).cName;
                            arrMob[i] = String.valueOf(customerResponse.data.get(i).aiPhone2);
                        }
                        myAdapter = new AutoCompleteRetailSalesReturnAdapter(RetailSalesReturnActivity.this, R.layout.custom_spinner_retail, arrCust, arrMob);
                        acvCustomerName.setAdapter(myAdapter);
                        acvCustomerName.showDropDown();


                    } else {
                        tsMessages(customerResponse.errorMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

    public void showInfo(View view) {
        try {
            dialog = new Dialog(RetailSalesReturnActivity.this);
            dialog.setContentView(R.layout.customer_details_dialog);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(false);
            // dialog.getWindow().getAttributes().windowAnimations = R.style.animation;
            place = dialog.findViewById(R.id.place);
            pincode = dialog.findViewById(R.id.pincode);
            gstno = dialog.findViewById(R.id.gstno);
            acvLcardNo = dialog.findViewById(R.id.acvLcardNo);
            acvmobileNo = dialog.findViewById(R.id.acvmobileNo);
            EditText etCustomerGoogleAdrss = dialog.findViewById(R.id.etCustomerGoogleAdrs);
            acvCustomerName = dialog.findViewById(R.id.acvCustomerName);
            cardType = dialog.findViewById(R.id.cEmail);
            acvCustomerName.setText(selectedCustomerName);
            cardType.setText(customerResponse.data.get(currenPos).aiEmail);
            acvCustomerName.setText(customerResponse.data.get(currenPos).cName);
            etCustomerGoogleAdrss.setText(customerAddress);
            acvLcardNo.setText(customerResponse.data.get(currenPos).cLoyaltyCode);
            if (!customerResponse.data.get(currenPos).aiPhone2.isEmpty())
                acvmobileNo.setText(customerResponse.data.get(currenPos).aiPhone2);
            else
                acvmobileNo.setText(customerResponse.data.get(currenPos).aiPhone1);
            ImageView btnOk = dialog.findViewById(R.id.btnSOReport1);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                }
            });


            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
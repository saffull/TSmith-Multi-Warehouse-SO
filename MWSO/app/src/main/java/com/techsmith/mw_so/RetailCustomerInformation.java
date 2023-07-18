package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.techsmith.mw_so.Global.AppWide;
import com.techsmith.mw_so.retail_utils.APIResponse;
import com.techsmith.mw_so.retail_utils.RetailCustomerData;
import com.techsmith.mw_so.retail_utils.RetailCustomerResponse;
import com.techsmith.mw_so.retail_utils.RetailReplyData;
import com.techsmith.mw_so.utils.AutocompleteRetailCustomArrayAdapter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class RetailCustomerInformation extends AppCompatActivity {
    AutoCompleteTextView acvLcardNo, acvmobileNo, acvCustomerName;
    List<AutoCompleteTextView> autoCompleteTextViewList;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    List<EditText> editTextList;
    EditText etCustomerAdrs, place, pincode, gstno, cardType, etCustomerGoogleAdrs, cEmail, latLong;
    ImageView btnSOReport;
    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    Gson gson;
    String Url, strCustomer, strErrorMsg, editData = "", LoyaltyCardType = "", uniqueID, sendTestData = "",
            selectedCustomerName, LoyaltyCode = "", LoyaltyId = "", LoyaltyCardTypeDesc = "", cLatitude = "",
            cLongitude = "", strfromweb = "", strerrormsg = "", className = "";
    ProgressDialog pDialog;
    ImageButton imgBtnCustSearchbyName, imgBtnCustSearchbyCardNo;
    List<RetailCustomerData> retailCustomerData;
    TextView addAlarmActionText, addPersonActionText, MessageDisplay, invoice_text, preview_text;
    int checkFlag = 0;
    RetailCustomerResponse customerResponse;
    FloatingActionButton mAddAlarmFab, mAddPersonFab, email_fab, invoice_fab, preview_fab;
    ExtendedFloatingActionButton mAddFab;
    Boolean isAllFabsVisible;
    RetailReplyData rcData;
    AppWide appWide;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retail_customer_information);
        prefs = PreferenceManager.getDefaultSharedPreferences(RetailCustomerInformation.this);
        appWide = AppWide.getInstance();
        editTextList = new ArrayList<>();
        autoCompleteTextViewList = new ArrayList<>();
        etCustomerAdrs = findViewById(R.id.etCustomerAdrs);
        place = findViewById(R.id.place);
        pincode = findViewById(R.id.pincode);
        retailCustomerData = new ArrayList<>();
        gstno = findViewById(R.id.gstno);
        btnSOReport = findViewById(R.id.btnSOReport1);
        acvLcardNo = findViewById(R.id.acvLcardNo);
        acvmobileNo = findViewById(R.id.acvmobileNo);
        etCustomerGoogleAdrs = findViewById(R.id.etCustomerGoogleAdrs);
        acvCustomerName = findViewById(R.id.acvCustomerName);
        imgBtnCustSearchbyName = findViewById(R.id.imgBtnCustSearchbyName);
        imgBtnCustSearchbyCardNo = findViewById(R.id.imgBtnCustSearchbyCardNo);
        cardType = findViewById(R.id.cEmail);
        mAddFab = findViewById(R.id.add_fab);
        mAddAlarmFab = findViewById(R.id.add_alarm_fab);
        addAlarmActionText = findViewById(R.id.add_alarm_action_text);
        MessageDisplay = findViewById(R.id.MessageDisplay);
        initializeFab();


        Url = prefs.getString("MultiSOURL", "");
        reInitialize();
        uniqueID = UUID.randomUUID().toString();

        btnSOReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    AppWide appWide = AppWide.getInstance();
                    String msg = "Store Name:" + appWide.getStoreName() + "\n Store Code:" + appWide.getStoreCode() +
                            "\nSubStore Id: " + appWide.getSubStoreId();
                    popUp(msg);
                } catch (Exception e) {
                    Toast.makeText(RetailCustomerInformation.this, "Data Controller Error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                // popUp(sendTestData);
            }
        });

        imgBtnCustSearchbyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (acvCustomerName.getText().toString().equalsIgnoreCase("") || acvCustomerName.getText().toString().length() < 3) {

                    Toast.makeText(RetailCustomerInformation.this, "Please input minimum 3 characters", Toast.LENGTH_SHORT).show();

                } else {
                    checkFlag = 1;
                    hideKeyboard();
                    new GetCustomerListTask().execute();
                }
            }
        });

        imgBtnCustSearchbyCardNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (acvLcardNo.getText().toString().equalsIgnoreCase("") || acvLcardNo.getText().toString().length() < 3) {
                    Toast.makeText(RetailCustomerInformation.this, "Please input minimum 3 characters", Toast.LENGTH_SHORT).show();
                } else {
                    checkFlag = 2;
                    new GetCustomerListTask().execute();
                }
            }
        });

        acvLcardNo.setOnItemClickListener((parent, view, pos, id) -> {
            try {
                String selectedCustomer = (String) parent.getItemAtPosition(pos);
                selectedCustomerName = customerResponse.data.get(pos).cName;
                // etCustomerAdrs.setText(customerResponse.data.get(pos).cGoogleAddress);
                etCustomerGoogleAdrs.setText(customerResponse.data.get(pos).cGoogleAddress);
                LoyaltyCode = customerResponse.data.get(pos).cLoyaltyCode;
                LoyaltyCardTypeDesc = customerResponse.data.get(pos).cLoyaltyCardTypeDesc;
                LoyaltyId = String.valueOf(customerResponse.data.get(pos).cLoyaltyId);
                acvLcardNo.setText(customerResponse.data.get(pos).cLoyaltyCode);
                LoyaltyCardType = String.valueOf(customerResponse.data.get(pos).cLoyaltyCardType);
                acvmobileNo.setText(customerResponse.data.get(pos).aiPhone2);
                cardType.setText(customerResponse.data.get(pos).aiEmail);
                acvCustomerName.setText(customerResponse.data.get(pos).cName);
                cLatitude = String.valueOf(customerResponse.data.get(pos).cLatitude);
                cLongitude = String.valueOf(customerResponse.data.get(pos).cLongitude);

                rcData = new RetailReplyData();
                Gson gson = new Gson();
                rcData.Name = selectedCustomerName;
                rcData.Latitude = customerResponse.data.get(pos).cLongitude;
                rcData.Longitude = customerResponse.data.get(pos).cLongitude;
                rcData.GoogleAddress = customerResponse.data.get(pos).cGoogleAddress;
                rcData.UHId = customerResponse.data.get(pos).cUHId;
                rcData.Gender = customerResponse.data.get(pos).cGender;
                rcData.Phone2 = customerResponse.data.get(pos).aiPhone2;
                rcData.Phone1 = customerResponse.data.get(pos).aiPhone1;
                rcData.LoyaltyId = customerResponse.data.get(pos).cLoyaltyId;
                rcData.LoyaltyCode = customerResponse.data.get(pos).cLoyaltyCode;
                rcData.LoyaltyCardType = customerResponse.data.get(pos).cLoyaltyCardType;
                rcData.LoyaltyCardTypeDesc = customerResponse.data.get(pos).cLoyaltyCardTypeDesc;
                rcData.AgeSlab = customerResponse.data.get(pos).cAgeSlab;
                rcData.DateOfBirth = customerResponse.data.get(pos).cDateOfBirth;
                rcData.Email = customerResponse.data.get(pos).aiEmail;
                rcData.AlternateCode = customerResponse.data.get(pos).cLoyaltyCode;


                sendTestData = gson.toJson(rcData);
                System.out.println("RC data is " + sendTestData);

                prefs = PreferenceManager.getDefaultSharedPreferences(RetailCustomerInformation.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("customer_Details", sendTestData);
                editor.apply();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        acvCustomerName.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                    long id) {


                try {
                    String selectedCustomer = (String) parent.getItemAtPosition(pos);
                    selectedCustomerName = customerResponse.data.get(pos).cName;
                    //etCustomerAdrs.setText(customerResponse.data.get(pos).cGoogleAddress);
                    appWide.setName(selectedCustomerName);
                    System.out.println("RcData is " + selectedCustomerName);
                    LoyaltyCode = customerResponse.data.get(pos).cLoyaltyCode;
                    appWide.setLoyaltyCode(LoyaltyCode);
                    LoyaltyCardTypeDesc = customerResponse.data.get(pos).cLoyaltyCardTypeDesc;
                    LoyaltyId = String.valueOf(customerResponse.data.get(pos).cLoyaltyId);
                    acvLcardNo.setText(customerResponse.data.get(pos).cLoyaltyCode);
                    LoyaltyCardType = String.valueOf(customerResponse.data.get(pos).cLoyaltyCardType);
                    acvmobileNo.setText(customerResponse.data.get(pos).aiPhone2);
                    cardType.setText(customerResponse.data.get(pos).aiEmail);

                    String gender = customerResponse.data.get(pos).cGender;
                    cLatitude = String.valueOf(customerResponse.data.get(pos).cLatitude);
                    cLongitude = String.valueOf(customerResponse.data.get(pos).cLongitude);
                    etCustomerGoogleAdrs.setText(customerResponse.data.get(pos).cGoogleAddress);

                    rcData = new RetailReplyData();
                    gson = new Gson();
                    rcData.Name = selectedCustomerName;
                    rcData.Latitude = customerResponse.data.get(pos).cLongitude;
                    rcData.Longitude = customerResponse.data.get(pos).cLongitude;
                    rcData.GoogleAddress = customerResponse.data.get(pos).cGoogleAddress;
                    rcData.UHId = customerResponse.data.get(pos).cUHId;
                    rcData.Gender = customerResponse.data.get(pos).cGender;
                    rcData.Phone2 = customerResponse.data.get(pos).aiPhone2;
                    rcData.Phone1 = customerResponse.data.get(pos).aiPhone1;
                    rcData.LoyaltyId = customerResponse.data.get(pos).cLoyaltyId;
                    rcData.LoyaltyCode = customerResponse.data.get(pos).cLoyaltyCode;
                    rcData.LoyaltyCardType = customerResponse.data.get(pos).cLoyaltyCardType;
                    rcData.LoyaltyCardTypeDesc = customerResponse.data.get(pos).cLoyaltyCardTypeDesc;
                    rcData.AgeSlab = customerResponse.data.get(pos).cAgeSlab;
                    rcData.DateOfBirth = customerResponse.data.get(pos).cDateOfBirth;
                    rcData.Email = customerResponse.data.get(pos).aiEmail;
                    rcData.AlternateCode = customerResponse.data.get(pos).cLoyaltyCode;
                    sendTestData = gson.toJson(rcData);
                    System.out.println("RC data is " + sendTestData);

                    prefs = PreferenceManager.getDefaultSharedPreferences(RetailCustomerInformation.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("customer_Details", sendTestData);
                    editor.apply();


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
        acvLcardNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() == 0) {
                    acvLcardNo.setAdapter(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mAddFab.setOnClickListener(
                view -> {
                    startFab();
                });

    }

    private void startSave() {
        new SaveData().execute();
    }

    private void startFab() {
        if (!isAllFabsVisible) {
            mAddAlarmFab.show();

            addAlarmActionText
                    .setVisibility(View.VISIBLE);
            mAddFab.extend();
            isAllFabsVisible = true;
        } else {
            mAddAlarmFab.hide();
            addAlarmActionText.setVisibility(View.GONE);
            mAddFab.shrink();
            isAllFabsVisible = false;
        }
    }

    private void initializeFab() {
        editTextList.add(etCustomerAdrs);
        editTextList.add(place);
        editTextList.add(pincode);
        editTextList.add(gstno);
        editTextList.add(cardType);
        editTextList.add(etCustomerGoogleAdrs);
        autoCompleteTextViewList.add(acvLcardNo);
        autoCompleteTextViewList.add(acvmobileNo);
        autoCompleteTextViewList.add(acvCustomerName);
        mAddAlarmFab.setVisibility(View.GONE);
        addAlarmActionText.setVisibility(View.GONE);
        // make the boolean variable as false, as all the
        // action name texts and all the sub FABs are
        // invisible
        isAllFabsVisible = false;
        // Set the Extended floating action button to
        // shrinked state initially
        mAddFab.shrink();
    }

    private void reInitialize() {
        editor = prefs.edit();
        editor.putString("customer_Details", "");
        editor.apply();
    }

    private void popUp(String msg) {
        try {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RetailCustomerInformation.this);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ClearAll(View view) {
        for (int i = 0; i < editTextList.size(); i++) {
            editTextList.get(i).setText("");
        }
        for (int i = 0; i < autoCompleteTextViewList.size(); i++) {
            autoCompleteTextViewList.get(i).setText("");
            autoCompleteTextViewList.get(i).setAdapter(null);
        }
    }

    public void CreateSO(View view) {
        if (!acvCustomerName.getText().toString().isEmpty())
            //startActivity(new Intent(RetailCustomerInformation.this, RetailSOActivity.class));
            startSave();
        else
            Toast.makeText(RetailCustomerInformation.this, "Select a Customer", Toast.LENGTH_LONG).show();
    }


    public void SearchItemByMobile(View view) {
    }

    public void SearchItemByName(View view) {

    }

    private void clearFields() {
        // acvCustomerName.setText("");
        // acvmobileNo.setText("");
        // acvLcardNo.setText("");
        // etCustomerGoogleAdrs.setText("");
        // etCustomerAdrs.setText("");
        //cardType.setText("");
    }

    public void showLcard(View view) {
        Rect displayRectangle = new Rect();
        Window window = RetailCustomerInformation.this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(RetailCustomerInformation.this).inflate(R.layout.lcardcustomer_dialog, viewGroup, false);
        dialogView.setMinimumWidth((int) (displayRectangle.width() * .5f));
        dialogView.setMinimumHeight((int) (displayRectangle.height() * .5f));
        builder.setView(dialogView);
        alertDialog = builder.create();
        AutoCompleteTextView acvLcardid = dialogView.findViewById(R.id.acvLcardID);
        EditText lcode = dialogView.findViewById(R.id.lcode);
        EditText lcDescription = dialogView.findViewById(R.id.lcDescription);
        EditText lcardType = dialogView.findViewById(R.id.lcardType);
        EditText latLong = dialogView.findViewById(R.id.latLong);
        lcDescription.setText(LoyaltyCardTypeDesc);
        lcode.setText(LoyaltyCode);
        acvLcardid.setText(LoyaltyId);
        lcardType.setText(LoyaltyCardType);

        try {
            latLong.setText(cLatitude + "&" + cLongitude);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button save = dialogView.findViewById(R.id.btnSavepop);
        save.setOnClickListener(new View.OnClickListener() {
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

    private class GetCustomerListTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailCustomerInformation.this);
            pDialog.setMessage("Loading Retail Customers...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            editData = acvCustomerName.getText().toString().trim();
            if (editData.isEmpty())
                editData = acvLcardNo.getText().toString().trim();
            System.out.println("https://tsmithy.in/dev/sbill/api/getcustomerlookup?name=" + editData);
            System.out.println(Url + "getcustomerlookup?name=" + editData);
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

                Toast.makeText(RetailCustomerInformation.this, "No result from web", Toast.LENGTH_SHORT).show();

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
                        AutocompleteRetailCustomArrayAdapter myAdapter = new AutocompleteRetailCustomArrayAdapter(RetailCustomerInformation.this, R.layout.custom_spinner_retail, arrCust, arrMob);
                        if (checkFlag == 1) {
                            acvCustomerName.setAdapter(myAdapter);
                            acvCustomerName.showDropDown();
                            checkFlag = 0;
                        } else if (checkFlag == 2) {
                            acvLcardNo.setAdapter(myAdapter);
                            acvLcardNo.showDropDown();
                            checkFlag = 0;
                        }

                    } else {
                        tsMessages(customerResponse.errorMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

    private String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        String address = "";


        try {
            geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return address;
    }

    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(RetailCustomerInformation.this);
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

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class SaveData extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RetailCustomerInformation.this);
            pDialog.setMessage("Saving Pre Data....");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                URL url = new URL(Url +"SaveDevCustInfo");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(180000);
                connection.setRequestProperty("authkey", "SBRL1467-8950-4215-A5DC-AC04D7620B23");
                connection.setRequestProperty("Content-Type", "application/json");
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
                        System.out.println("Einvoice Response is " + strfromweb);


                    } finally {
                        connection.disconnect();
                    }

                } else {
                    strerrormsg = connection.getResponseMessage();
                    strfromweb = "httperror";
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
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
                if (apiResponse.statusFlag == 0) {
                    //tsMessages("Saved Sucessfully");
                    startActivity(new Intent(RetailCustomerInformation.this, RetailSOActivity.class));
                } else {
                    tsMessages(apiResponse.errorMessage);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
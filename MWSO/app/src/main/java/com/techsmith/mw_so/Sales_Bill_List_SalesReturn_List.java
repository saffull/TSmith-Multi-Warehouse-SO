package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.techsmith.mw_so.Sales_Return_List.SalesReturnAdapter;
import com.techsmith.mw_so.Sales_Return_List.SalesReturnList;
import com.techsmith.mw_so.Sales_Return_List.SalesReturnListData;
import com.techsmith.mw_so.print_utils.EmailResponse;
import com.techsmith.mw_so.print_utils.PrintResponse;
import com.techsmith.mw_so.retail_utils.ProductRetailResponse;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Sales_Bill_List_SalesReturn_List extends AppCompatActivity {
    public ListView lvCollectionlist;
    AutoCompleteTextView acvDate;
    Calendar myCalendar;
    DatePickerDialog dialog;
    ProgressDialog pDialog;
    ImageButton imgBtnCustSearchbyName;
    RadioButton salesBillRadio, salesReturnRadio;
    String strErrorMsg = "", strCollections = "", Url = "", apiName = "", type = "", strPrintBill = "", strerrormsg = "", billInputData = "";
    SharedPreferences prefs, prefsD;
    TextView billCount;
    ArrayList<SalesReturnListData> sList;
    Gson gson;
    Button btnClearAll, btnExitSO;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout linearLayoutBSheet;
    private ToggleButton tbUpDown;
    RadioGroup radioCOLPayType;
    SalesReturnAdapter sap;
    SimpleDateFormat dff = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_bill_list_sales_return_list);
        prefs = PreferenceManager.getDefaultSharedPreferences(Sales_Bill_List_SalesReturn_List.this);
        init();
        acvDate = findViewById(R.id.acvDate);
        lvCollectionlist = findViewById(R.id.lvCollectionlist);
        imgBtnCustSearchbyName = findViewById(R.id.imgBtnCustSearchbyDate);
        salesBillRadio = findViewById(R.id.radioCOLCash);
        salesReturnRadio = findViewById(R.id.radioCOLCheque);
        radioCOLPayType = findViewById(R.id.radioCOLPayType);
        Url = prefs.getString("MultiSOURL", "");
        myCalendar = Calendar.getInstance(TimeZone.getDefault());
        Date c = Calendar.getInstance().getTime();
        String formattedDate = dff.format(c);
        acvDate.setText(formattedDate);
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
        radioCOLPayType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton checkedRadioButton = (RadioButton) radioGroup.findViewById(i);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked) {
                    // Changes the textview's text to "Checked: example radiobutton text"
                    //tv.setText("Checked:" + checkedRadioButton.getText());
                    System.out.println(checkedRadioButton.getText());
                    acvDate.setText("");
                    lvCollectionlist.setAdapter(null);
                }
            }
        });
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                updateLabel();
            }
        };
        acvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new DatePickerDialog(Sales_Bill_List_SalesReturn_List.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        imgBtnCustSearchbyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!acvDate.getText().toString().isEmpty())
                    fetchBills();
                else
                    popUp("Add Date");
            }
        });

        btnExitSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(Sales_Bill_List_SalesReturn_List.this,SoMenu.class));
            }
        });

    }

    private void init() {
        this.linearLayoutBSheet = findViewById(R.id.bottom_print_sheet);
        this.bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBSheet);
        this.tbUpDown = findViewById(R.id.toggleButton_SR);
        this.billCount=findViewById(R.id.billCount);
        this.btnExitSO = findViewById(R.id.btnExitSO);
        this.btnClearAll = findViewById(R.id.btnClearAll);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(Sales_Bill_List_SalesReturn_List.this,SoMenu.class));
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.getDefault());
        acvDate.setText(dateFormat.format(myCalendar.getTime()));
    }

    public void onRadioButtonClicked(View view) {
    }

    public void ClearSearch(View view) {
    }

    private void fetchBills() {
        if (salesBillRadio.isChecked()) {
            apiName = "GetListOfSalesBill";
            type = "sales";
        } else {
            apiName = "GetListOfSalesReturn";
            type = "returns";
        }
        new fetchData().execute();
    }


    private class fetchData extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Sales_Bill_List_SalesReturn_List.this);
            pDialog.setMessage("Loading Data..");
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
                jObject.put("DATE", acvDate.getText().toString().trim());


                URL url = new URL(Url + apiName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(180000);
                connection.setRequestProperty("authkey", "SBRL1467-8950-4215-A5DC-AC04D7620B23");
                connection.setRequestProperty("Content-Type", "application/json");
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
                        strCollections = sb.toString();
                        System.out.println("Print Bill Response is " + strCollections);


                    } finally {
                        connection.disconnect();
                    }

                } else {
                    strErrorMsg = connection.getResponseMessage();
                    strCollections = "httperror";
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return strCollections;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing()) {
                pDialog.dismiss();

                try {
                    Gson gson = new Gson();
                    SalesReturnList salesReturnList;
                    salesReturnList = gson.fromJson(strCollections, SalesReturnList.class);
                    sList = new ArrayList<>();

                    if (salesReturnList.STATUSFLAG == 0) {

                        sList = salesReturnList.DATA;
                        billCount.setText(String.valueOf(sList.size()));

                         sap = new SalesReturnAdapter(Sales_Bill_List_SalesReturn_List.this, R.layout.list_row, sList, type);
                        lvCollectionlist.setAdapter(sap);
                        sap.notifyDataSetChanged();
                    } else {
                        popUp(salesReturnList.ERRORMESSAGE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startSessionPrint(String data) {
        billInputData = data;
        if (type.equalsIgnoreCase("returns")) {
            new PrintSR().execute();
        } else {
            new PrintBill().execute();
        }

    }

    public void startEmailSession(String data){
        billInputData = data;
        new EmailBill().execute();
    }

    private class PrintSR extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Sales_Bill_List_SalesReturn_List.this);
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
                jObject.put("INVOICENO", billInputData.trim());
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
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(Sales_Bill_List_SalesReturn_List.this);
                    alertDialogBuilder.setMessage("Print Sales Returns For " + billInputData + "..?");
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
                    popUp(apiResponse.ERRORMESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class PrintBill extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Sales_Bill_List_SalesReturn_List.this);
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
                jObject.put("INVOICENO", billInputData.trim());
                jObject.put("FORMAT", "1");


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
                   /* android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(Sales_Bill_List_SalesReturn_List.this);
                    alertDialogBuilder.setMessage("Print Sales Bill For " + billInputData + " ..?");
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
                    alertDialog.show();*/

                    startPrint(msg);
                    //
                } else {
                    popUp(apiResponse.ERRORMESSAGE);
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
            pDialog = new ProgressDialog(Sales_Bill_List_SalesReturn_List.this);
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
                jObject.put("INVOICENO", billInputData);
                //jObject.put("INVOICENO","2021/23/S-58");
                jObject.put("FORMAT", "2");


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
                        System.out.println("Email Bill Response is " + strPrintBill);


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
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(Sales_Bill_List_SalesReturn_List.this);
                    alertDialogBuilder.setMessage(msg);

                    alertDialogBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                } else {
                    popUp(apiResponse.ERRORMESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Sales_Bill_List_SalesReturn_List.this);
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


    public void popUp(String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Sales_Bill_List_SalesReturn_List.this);
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
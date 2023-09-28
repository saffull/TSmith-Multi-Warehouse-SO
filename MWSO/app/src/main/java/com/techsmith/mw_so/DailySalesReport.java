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
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.techsmith.mw_so.Dsr.DailySalesAdapter;
import com.techsmith.mw_so.Dsr.DailySalesResponse;
import com.techsmith.mw_so.Dsr.REPORT;
import com.techsmith.mw_so.Sales_Return_List.SalesReturnAdapter;
import com.techsmith.mw_so.Sales_Return_List.SalesReturnList;
import com.techsmith.mw_so.Sales_Return_List.SalesReturnListData;

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

public class DailySalesReport extends AppCompatActivity {
    public ListView lvCollectionlist;
    Gson gson;
    SharedPreferences.Editor editor;
    DailySalesAdapter dsa;
    ArrayList<REPORT> sList;
    AutoCompleteTextView acvDate;
    Calendar myCalendar;
    DatePickerDialog dialog;
    ProgressDialog pDialog;
    TextView billCount;
    Button btnClearAll, btnExitSO, paymentBtn, btnPdf;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout linearLayoutBSheet;
    private ToggleButton tbUpDown;
    ImageButton imgBtnCustSearchbyName;
    String strErrorMsg = "", strCollections = "", Url = "", pdfUrl = "", type = "", strPrintBill = "", strerrormsg = "", billInputData = "";
    SharedPreferences prefs;
    SimpleDateFormat dff = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    CheckBox taxTick;
    int showTax = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_sales_report);
        prefs = PreferenceManager.getDefaultSharedPreferences(DailySalesReport.this);
        intiSheet();
        acvDate = findViewById(R.id.acvDate);
        lvCollectionlist = findViewById(R.id.lvCollectionlist);
        taxTick = findViewById(R.id.taxTick);
        imgBtnCustSearchbyName = findViewById(R.id.imgBtnCustSearchbyDate);
        Url = prefs.getString("MultiSOURL", "");
        myCalendar = Calendar.getInstance(TimeZone.getDefault());
        Date c = Calendar.getInstance().getTime();
        String formattedDate = dff.format(c);
        acvDate.setText(formattedDate);

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
                dialog = new DatePickerDialog(DailySalesReport.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        imgBtnCustSearchbyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!acvDate.getText().toString().isEmpty())
                    new fetchData().execute();
                else
                    popUp("Add Date");
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
        paymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!strPrintBill.isEmpty())
                    startPrint(strPrintBill);
                else
                    popUp("List Empty");
            }
        });

        btnPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("View to pdf clicked..");
                openUrl();
            }
        });

        btnExitSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(DailySalesReport.this, SoMenu.class));
            }
        });

    }

    private void intiSheet() {
        this.linearLayoutBSheet = findViewById(R.id.bottom_dsr);
        this.bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBSheet);
        this.tbUpDown = findViewById(R.id.toggleButton_SR);
        this.billCount = findViewById(R.id.billCount);
        this.btnExitSO = findViewById(R.id.btnExitSO);
        this.btnPdf = findViewById(R.id.btnPdf);
        this.paymentBtn = findViewById(R.id.paymentBtn);

    }

    private class fetchData extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(DailySalesReport.this);
            pDialog.setMessage("Loading Daily Report..");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                if (!taxTick.isChecked())
                    showTax = 0;
                else
                    showTax = 1;
                JSONObject object = new JSONObject();
                object.put("StoreCode", "2021-DLF-PH1");
                object.put("SubStoreCode", "MAIN");
                object.put("UserId", "1");
                object.put("CounterId", "1");
                object.put("CustType", "1");

                JSONObject jObject = new JSONObject();
                jObject.put("STOREID", "25");
                jObject.put("ASONDATE", acvDate.getText().toString().trim());
                jObject.put("SHOWTAX", showTax);


                URL url = new URL(Url + "GetDSRReport");
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
                        System.out.println("DSR Response is " + strCollections);


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
                    gson = new Gson();
                    sList = new ArrayList<>();
                    DailySalesResponse dsr;
                    dsr = gson.fromJson(strCollections, DailySalesResponse.class);
                    if (dsr.STATUSFLAG == 0) {
                        sList = dsr.REPORT;
                        billCount.setText(String.valueOf(sList.size()));
                        strPrintBill = dsr.PRINTSTRING.PRINTSTRINGVALUE;
                        pdfUrl = dsr.PDFPATH.trim();
                        dsa = new DailySalesAdapter(DailySalesReport.this, R.layout.list_row, sList);
                        lvCollectionlist.setAdapter(dsa);
                        dsa.notifyDataSetChanged();
                    } else {
                        popUp(dsr.ERRORMESSAGE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(DailySalesReport.this, SoMenu.class));
    }

    private void openUrl() {
        if (pdfUrl.startsWith("https://") || pdfUrl.startsWith("http://")) {
            Uri uri = Uri.parse(pdfUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else {
            Toast.makeText(DailySalesReport.this, "Invalid Url", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.getDefault());
        acvDate.setText(dateFormat.format(myCalendar.getTime()));
        new fetchData().execute();
    }

    public void popUp(String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DailySalesReport.this);
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

    public boolean isBluetoothEnabled() {
        BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return myBluetoothAdapter.isEnabled();
    }

    //Printing Function
    private void startPrint(String msg) {

        if (isBluetoothEnabled()) {
            try {
                //String temp = "Design and deploy business solutions that are relevant to the needs of the real India.";
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
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
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DailySalesReport.this);
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
}
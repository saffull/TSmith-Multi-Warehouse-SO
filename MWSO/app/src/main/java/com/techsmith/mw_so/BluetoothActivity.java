package com.techsmith.mw_so;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anggastudio.printama.Printama;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.techsmith.mw_so.utils.PrintResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BluetoothActivity extends AppCompatActivity {
    ProgressDialog pDialog;
    String strCheckLogin, strErrorMsg, printData = "", printStoreId = "",
            printSbNumber = "", s1 = "", s2 = "", printSBNumber, printStoreID;
    PrintResponse printResponse;
    TextView MessageDisplay;
    SharedPreferences prefs;
    List<String> printList, sbList, idList;
    String[] sbNumber, storeId;
    ArrayList<String> list1, list2;
    ImageView imageView, qrDisplay;
    int sizeCount = 0, reCount = 0;
    Bitmap bitmap1, bitmaap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        prefs = PreferenceManager.getDefaultSharedPreferences(BluetoothActivity.this);
        MessageDisplay = findViewById(R.id.MessageDisplay);
        qrDisplay = findViewById(R.id.qrDisplay);
        imageView = findViewById(R.id.imageView);
        printList = new ArrayList<>();
        sbList = new ArrayList<>();
        idList = new ArrayList<>();
        qrdo();
        sizeCount = prefs.getInt("sizeCount", 0);
        printStoreID = prefs.getString("printStoreID", "");
        printSBNumber = prefs.getString("printSBNumber", "");


        printStoreId = prefs.getString("printStoreId", "").replace("[", "").replace("]", "");
        printSbNumber = prefs.getString("printSbNumber", "").replace("[", "").replace("]", "");

        System.out.println("Incoming data is " + printStoreId + "<----------------------------->" + printSbNumber);

        try {
            sbNumber = printSbNumber.split(",");
            storeId = printStoreId.split(",");
        } catch (Exception e) {
            e.printStackTrace();
        }

        findViewById(R.id.btn_printer_settings).setOnClickListener(v -> showPrinterList());
        findViewById(R.id.btn_print_text_last).setOnClickListener(v -> printOurReceipt());

        if (sizeCount == 1) {
            try {
                s1 = printStoreID;
                s2 = printSBNumber;
                new TakeBillSingle().execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (sizeCount > 1) {
            if (!printSbNumber.isEmpty() && !printStoreId.isEmpty()) {
                try {
                    new TakeBill().execute();
                    findViewById(R.id.printtsmith).setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(BluetoothActivity.this, "SbNumber or StoreID is Empty", Toast.LENGTH_LONG).show();
            }

        }

        //new TakePic().execute();
    }

    private void CheckCount() {
        reCount++;
        if (reCount != sizeCount) {
            try {
                new TakeBill().execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            findViewById(R.id.printtsmith).setEnabled(true);
        }
    }


    public boolean isBluetoothEnabled() {
        BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return myBluetoothAdapter.isEnabled();
    }


    public void SampReceipt(View view) {

        if (isBluetoothEnabled()) {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix;
            try {
                bitMatrix = writer.encode("https://www.reddit.com/", BarcodeFormat.QR_CODE, 150, 150);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                Bitmap bitmaap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int color = Color.WHITE;
                        if (bitMatrix.get(x, y)) color = Color.BLACK;
                        bitmaap.setPixel(x, y, color);
                    }
                }

            } catch (WriterException e) {
                e.printStackTrace();
            }

            String temp = MessageDisplay.getText().toString().trim();
            try {
                Bitmap bitmap = Printama.getBitmapFromVector(this, R.drawable.techsmith);
                Printama.with(this).connect(printama -> {
                    printama.addNewLine(1);
                    // printama.setNormalText();
                    printama.setSmallText();
                    if (bitmaap != null) {
                        printama.printImage(bitmaap,200, Printama.CENTER);
                        printama.printText(temp , Printama.LEFT);
                       // printama.printImage(bitmaap,150, Printama.CENTER);
                        printama.addNewLine(3);
                    }
                    printama.addNewLine(3);
                    printama.close();
                }, this::showToast);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast toast = Toast.makeText(BluetoothActivity.this,
                    "Please Enable Bluetooth & Try Again.", Toast.LENGTH_SHORT);
            toast.show();
        }


    }

    private class TakeBill extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(BluetoothActivity.this);
            pDialog.setMessage("Loading..Please waitttt.!!");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            strCheckLogin = "";

            //  System.out.println("Url used is " + Url);//https://tsmithy.in/somemouat/api/
            try {
                //URL url = new URL("https://tsmithy.in/somemouat/api/LoginVer2?Name=salam_ka@yahoo.com&secret=1047109119116122626466");
                //URL url = new URL("https://tsmithy.in/somemouat/api/PrintBill?StoreId=15&SBillNo=APPL/22/WS-13");
                URL url = new URL("https://tsmithy.in/somemouat/api/PrintBill?StoreId=" + storeId[reCount].trim() + "&SBillNo=" + sbNumber[reCount].trim());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setRequestProperty("authkey", "MGG427A3-F9F6-N7DA-T698-SOF60CE0MEMO");
                connection.setRequestProperty("name", "");
                connection.setRequestProperty("password", "");
                connection.setRequestProperty("debugkey", "");
                connection.setRequestProperty("remarks", "");
                //connection.setRequestProperty("machineid", "saffull@gmail.com");
                connection.setRequestProperty("machineid", "salam_ka@yahoo.com");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();

                int responsecode = connection.getResponseCode();
                String responseMsg = connection.getResponseMessage();
                System.out.println("Response message is " + responsecode);


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
                        strCheckLogin = sb.toString();
                        System.out.println("Response of Print Bill--->" + strCheckLogin);
                    } else {
//                        strErrorMsg = connection.getResponseMessage();
                        strErrorMsg = responseMsg;
                        strCheckLogin = "httperror";
                    }

//                    {"Id":9,"Username":"500_9","Name":"500 User 9","Password":"","Email":"pavithrapurushan06@gmail.com","Phone":"","Active":0,"StoreId":50,"StoreCode":"500","StoreName":"SEPL-PHARMA WAREHOUSE","DeviceId":"","PasswordOtp":"","ErrorStatus":0,"Message":"Success"}

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    connection.disconnect();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return strCheckLogin;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            printData = s;


            if (strCheckLogin.equals("") || strCheckLogin == null) {
                Toast.makeText(BluetoothActivity.this, "No result", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    Gson gson = new Gson();
                    printResponse = gson.fromJson(printData, PrintResponse.class);
                    String test = printResponse.data.replace("\r", "\n");
                    System.out.println("Print data is " + test);
                    printList.add(test);
                    System.out.println("Print List is " + printList);
                    MessageDisplay.setText(printList.toString().replace(",", "\n").
                            replace("[", "").replace("]", ""));
                    MessageDisplay.setMovementMethod(new ScrollingMovementMethod());
                    // tsMessages(test);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            CheckCount();
                        }
                    }, 2000);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(BluetoothActivity.this);
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

    private void showPrinterList() {
        Printama.showPrinterList(this, R.color.colorBlue, printerName -> {
            Toast.makeText(this, printerName, Toast.LENGTH_SHORT).show();
            TextView connectedTo = findViewById(R.id.tv_printer_info);
            String text = "Connected to : " + printerName;
            connectedTo.setText(text);
            if (!printerName.contains("failed")) {
                findViewById(R.id.btn_printer_test).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_printer_test).setOnClickListener(v -> testPrinter());
            }
        });
    }

    private void testPrinter() {
        Printama.with(this).printTest();
    }

    private void showPrinterListActivity() {
        // only use this when your project is not androidX
        Printama.showPrinterList(this);
    }

    private void qrdo() {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = writer.encode("https://www.youtube.com/", BarcodeFormat.QR_CODE, 200, 200);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            bitmaap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int color = Color.WHITE;
                    if (bitMatrix.get(x, y)) color = Color.BLACK;
                    bitmaap.setPixel(x, y, color);
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        qrDisplay.setImageBitmap(bitmaap);
    }


    private void printOurReceipt() {
        String address = "http://www.tsmith.co.in";
        bitmap1 = Printama.getBitmapFromVector(this, R.drawable.techsmith);
        Printama.with(this).connect(printama -> {
            printama.printTextln("Techsmith Software Pvt Limited", Printama.CENTER);
            printama.setNormalText();
            printama.printDashedLine();
            printama.addNewLine();

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix;
            try {
                bitMatrix = writer.encode(address, BarcodeFormat.QR_CODE, 100, 100);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                Bitmap bitmaap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int color = Color.WHITE;
                        if (bitMatrix.get(x, y)) color = Color.BLACK;
                        bitmaap.setPixel(x, y, color);
                    }
                }
                if (bitmaap != null) {
                    printama.printImage(bitmap1, 600, Printama.CENTER);
                    printama.printImage(bitmaap);
                }
            } catch (WriterException e) {
                e.printStackTrace();
            }
            printama.addNewLine();
            printama.feedPaper();
            printama.close();


        }, this::showToast);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String printerName = Printama.getPrinterResult(resultCode, requestCode, data);
        showResult(printerName);
    }

    private void showResult(String printerName) {
        showToast(printerName);
        TextView connectedTo = findViewById(R.id.tv_printer_info);
        String text = "Connected to : " + printerName;
        connectedTo.setText(text);
        if (!printerName.contains("failed")) {
            findViewById(R.id.btn_printer_test).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_printer_test).setOnClickListener(v -> testPrinter());
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private class TakeBillSingle extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(BluetoothActivity.this);
            pDialog.setMessage("Loading Single Bill..Please wait.!!");
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            strCheckLogin = "";

            //  System.out.println("Url used is " + Url);//https://tsmithy.in/somemouat/api/
            try {
                //URL url = new URL("https://tsmithy.in/somemouat/api/LoginVer2?Name=salam_ka@yahoo.com&secret=1047109119116122626466");
                //URL url = new URL("https://tsmithy.in/somemouat/api/PrintBill?StoreId=15&SBillNo=APPL/22/WS-13");
                URL url = new URL("https://tsmithy.in/somemouat/api/PrintBill?StoreId=" + s1 + "&SBillNo=" + s2);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setRequestProperty("authkey", "MGG427A3-F9F6-N7DA-T698-SOF60CE0MEMO");
                connection.setRequestProperty("name", "");
                connection.setRequestProperty("password", "");
                connection.setRequestProperty("debugkey", "");
                connection.setRequestProperty("remarks", "");
                //connection.setRequestProperty("machineid", "saffull@gmail.com");
                connection.setRequestProperty("machineid", "salam_ka@yahoo.com");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();

                int responsecode = connection.getResponseCode();
                String responseMsg = connection.getResponseMessage();
                System.out.println("Response message is " + responsecode);


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
                        strCheckLogin = sb.toString();
                        System.out.println("Response of Print Bill--->" + strCheckLogin);
                    } else {
//                        strErrorMsg = connection.getResponseMessage();
                        strErrorMsg = responseMsg;
                        strCheckLogin = "httperror";
                    }

//                    {"Id":9,"Username":"500_9","Name":"500 User 9","Password":"","Email":"pavithrapurushan06@gmail.com","Phone":"","Active":0,"StoreId":50,"StoreCode":"500","StoreName":"SEPL-PHARMA WAREHOUSE","DeviceId":"","PasswordOtp":"","ErrorStatus":0,"Message":"Success"}

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    connection.disconnect();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return strCheckLogin;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            printData = s;


            if (strCheckLogin.equals("") || strCheckLogin == null) {
                Toast.makeText(BluetoothActivity.this, "No result", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    Gson gson = new Gson();
                    printResponse = gson.fromJson(printData, PrintResponse.class);
                    String test = printResponse.data.replace("\r", "\n");
                    System.out.println("Print data is " + test);
                    printList.add(test);
                    System.out.println("Print List is " + printList);
                    MessageDisplay.setText(printList.toString().replace(",", "\n").
                            replace("[", "").replace("]", ""));
                    MessageDisplay.setMovementMethod(new ScrollingMovementMethod());
                    // tsMessages(test);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private class TakePic extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(BluetoothActivity.this);
            pDialog.setMessage("Fetching Image...Please wait.!!");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            strCheckLogin = "";

            //  System.out.println("Url used is " + Url);//https://tsmithy.in/somemouat/api/
            try {
                URL url = new URL("https://tsmithy.in/dummy/api/GetImage?name=5");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(300000);
                connection.setConnectTimeout(300000);
                connection.setRequestProperty("authkey", "D39C1EB0-DC32-4CF2-817B-EE748A8E4A30");
                connection.setRequestProperty("name", "");
                connection.setRequestProperty("password", "");
                connection.setRequestProperty("debugkey", "");
                connection.setRequestProperty("remarks", "");
                //connection.setRequestProperty("machineid", "saffull@gmail.com");
                connection.setRequestProperty("machineid", "salam_ka@yahoo.com");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();

                int responsecode = connection.getResponseCode();
                String responseMsg = connection.getResponseMessage();
                System.out.println("Response message is " + responsecode);

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
                        strCheckLogin = sb.toString();
                        System.out.println("Response of Image URL is --->" + strCheckLogin);
                    } else {
//                        strErrorMsg = connection.getResponseMessage();
                        strErrorMsg = responseMsg;
                        strCheckLogin = "httperror";
                    }

//                    {"Id":9,"Username":"500_9","Name":"500 User 9","Password":"","Email":"pavithrapurushan06@gmail.com","Phone":"","Active":0,"StoreId":50,"StoreCode":"500","StoreName":"SEPL-PHARMA WAREHOUSE","DeviceId":"","PasswordOtp":"","ErrorStatus":0,"Message":"Success"}

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return strCheckLogin;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            printData = s;


            if (strCheckLogin.equals("") || strCheckLogin == null) {
                Toast.makeText(BluetoothActivity.this, "No result", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    Gson gson = new Gson();
                    printResponse = gson.fromJson(printData, PrintResponse.class);
                    String test = printResponse.info;
                    System.out.println("Url data is " + test);

                    Glide.with(BluetoothActivity.this)
                            .asBitmap()
                            .override(200, 400)
                            .load(test.trim())
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    imageView.setImageBitmap(resource);
                                    PrintPic(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                }
                            });


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void PrintPic(Bitmap resource) {
        if (resource != null) {
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            Printama.with(this).connect(printama -> {
                printama.addNewLine(1);
                //printama.printImage(resource, 500, Printama.LEFT);
                printama.printImage(resource, Printama.ORIGINAL_WIDTH);
                //printama.printImage(resource,Printama.FULL_WIDTH);
                printama.addNewLine(3);
                printama.printImage(bitmap, Printama.ORIGINAL_WIDTH);
                printama.close();
            }, this::showToast);
        } else {
            System.out.println("Resource EMPTY");
        }
    }
}
package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.anggastudio.printama.Printama;
import com.google.gson.Gson;

import com.techsmith.mw_so.collection_utils.CSpinner;
import com.techsmith.mw_so.utils.LineResponse;
import com.techsmith.mw_so.utils.UnicodeFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class BleActivity extends AppCompatActivity {
    String strerrormsg, strfromweb, print_message, printer, printerSelect;
    // will show the statuses like bluetooth open, close or data sent
    TextView myLabel, status;
    byte FONT_TYPE;

    // will enable user to enter any text to be printed
    EditText myTextbox;

    // android built in classes for bluetooth operations
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    // needed for communication to bluetooth device / network
    OutputStream os;
    InputStream mmInputStream;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    /* Get time and date */


    SharedPreferences prefs;
    List<String> printList;
    private CSpinner storeSelect;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            closeBT();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        prefs = PreferenceManager.getDefaultSharedPreferences(BleActivity.this);

        Button openButton = findViewById(R.id.open);
        Button sendButton = findViewById(R.id.send);
        Button closeButton = findViewById(R.id.close);

        myLabel = findViewById(R.id.label);
        myTextbox = findViewById(R.id.entry);
        status = findViewById(R.id.status);
        storeSelect = findViewById(R.id.storeSelect);
        printer = prefs.getString("printer", "UTP-1004003").trim().replace("[", "").replace("]", "");
        printList = new ArrayList<String>(Arrays.asList(printer.split(",")));


        new takeDocument().execute();


        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    findBT();
                    openBT();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    sendData();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    closeBT();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        ArrayAdapter ad
                = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                printList);

        ad.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);

        storeSelect.setAdapter(ad);
        storeSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                printerSelect = printList.get(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void findBT() {

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                myLabel.setText("No bluetooth adapter available");
            }

            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {

                    // RPP300 is the name of the bluetooth printer device
                    // we got this name from the list of paired devices
                    if (device.getName().equals(printerSelect)) {
                        mmDevice = device;
                        status.setText("Device Connected: UTP-1004003 ");
                        break;
                    }
                }
            }

            myLabel.setText("Bluetooth device found.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void openBT() throws IOException {
        try {

            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            os = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

            myLabel.setText("Bluetooth Opened");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = mmInputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                myLabel.setText(data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendData() throws IOException {
        try {

            // the text typed by the user
            // String msg = myTextbox.getText().toString();
            //  msg += "\n";

            //os.write(msg.getBytes());
            p3();

            // tell the user data were sent
            myLabel.setText("Data sent.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeBT() throws IOException {
        try {
            stopWorker = true;
            os.close();
            mmInputStream.close();
            mmSocket.close();
            myLabel.setText("Bluetooth Closed");
            status.setText("Device Connection Ended.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void p3() {
        Thread tt = new Thread() {
            public void run() {
                try {
                    // OutputStream os = mmSocket.getOutputStream();
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
                    final String formattedDate = df.format(c.getTime());
                    byte[] cc = new byte[]{0x1B, 0x21, 0x00};  // 0- normal size text
                    byte[] bb = new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
                    byte[] bb2 = new byte[]{0x1B, 0x21, 0x20}; // 2- bold with medium text
                    byte[] bb3 = new byte[]{0x1B, 0x21, 0x10}; // 3- bold with large text
                    byte[] format = {29, 3, 35}; // manipulate your font size in the second parameter
                    byte[] center = {0x1b, 'a', 0x01}; // center alignment
                    byte[] printformat = {0x1B, 0x21, FONT_TYPE};

                    String blank = "\n";
                    String he = "<font size='big'>ORDER N°045</font>\n";
                    he = he + "********************************\n\n";
                    // String BILL = print_message;
                    String BILL = "Techsmith Software Private Limited \n"; // 32 including space

                    String time = formattedDate + "\n\n";

                    os.write(blank.getBytes());
                    os.write(cc);
                    os.write(BILL.getBytes());//writing the data to the bluetooth output stream
                    os.write(blank.getBytes());
                   // os.write(print_message.getBytes());
                    os.write(time.getBytes());
                    // os.write(he.getBytes());
                    os.write(0x0D);
                    os.write(0x0D);
                    os.write(0x0D);
                    os.flush();

                   /* os.write(blank.getBytes());
                    os.write(he.getBytes());
                    os.write(format);
                    os.write(BILL.getBytes());
                    os.write(time.getBytes());
                    os.write(he.getBytes());
                    os.write(blank.getBytes());
                    os.write(blank.getBytes());*/

                    // Setting height
                    int gs = 29;
                    os.write(intToByteArray(gs));
                    int h = 150;
                    os.write(intToByteArray(h));
                    int n = 170;
                    os.write(intToByteArray(n));

                    // Setting Width
                    int gs_width = 29;
                    os.write(intToByteArray(gs_width));
                    int w = 119;
                    os.write(intToByteArray(w));
                    int n_width = 2;
                    os.write(intToByteArray(n_width));


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        };
        tt.start();

    }

    public static byte intToByteArray(int value) {
        byte[] b = ByteBuffer.allocate(4).putInt(value).array();

        for (int k = 0; k < b.length; k++) {
            System.out.println("Vishal  [" + k + "] = " + "0x"
                    + UnicodeFormatter.byteToHex(b[k]));
        }

        return b[3];
    }

    private class takeDocument extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog = new ProgressDialog(BleActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progressDialog.setMessage("Taking Lines...");
                progressDialog.show();
            } catch (WindowManager.BadTokenException e) {
                //use a log message
            }

        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                /*&LineBreak=%23*/
                URL url = new URL("https://tsmithy.in/dummy/api/PrintTextA?CharactersInARow=13&NoOfLines=5&LineBreak=%23");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(30000);

                connection.setRequestProperty("debugkey", "");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("authkey", "D39C1EB0-DC32-4CF2-817B-EE748A8E4A30");
                connection.setRequestProperty("docid", "2022");
                connection.setRequestProperty("module", "Ts-MWSO");
                connection.connect();

                int responsecode = connection.getResponseCode();
                System.out.println("Response code is " + responsecode);
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
                        System.out.println("Line Response is " + strfromweb);


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
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            try {
                Gson gson = new Gson();
                LineResponse lineResponse = gson.fromJson(s, LineResponse.class);
                print_message = lineResponse.PrintText.replace("#", "\n");
                myLabel.setText(print_message);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
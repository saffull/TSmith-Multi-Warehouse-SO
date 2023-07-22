package com.techsmith.mw_so.retail_utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.techsmith.mw_so.R;
import com.techsmith.mw_so.RetailSOActivity;
import com.techsmith.mw_so.SOActivity;
import com.techsmith.mw_so.Spinner.RetailCustomAdapter;
import com.techsmith.mw_so.utils.AllocateQtyPL;
import com.techsmith.mw_so.utils.ItemList;
import com.techsmith.mw_so.utils.SOActivityArrayAdapter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RetailSOActivityArrayAdapter extends ArrayAdapter {
    Context context, appContext;
    Double productTotal, pRate, total = 0.0, prevTotal = 0.0;
    int selectedPos, prevBCodepPOS;
    public ArrayList<SaveProductSOPL> sList;
    RetailSOActivityArrayAdapter arrayAdapter;
    ArrayList<AllocateQtyPL> itemArraylist;
    AllocateQtyPL itemDetail;
    List<AllocateQtyPL> listSODetailPL;
    ImageButton btnDelete;
    List<String> batchCode, batchExpiry, tempBatchCode, tempbatchList;
    List<Double> batchMrp, batchRate, batchSOH;
    String tempTotal = "", ptotal = "", tempBCode = "", temppCode = "", pID = "", prevBCode,currentBcode="";
    Dialog qtydialog;
    Gson gson;
    RetailCustomAdapter ad;
    TextView tvProductName, tvSOH, tvmrp, tvSelectedItemName, tvMrp;
    private static final DecimalFormat decfor = new DecimalFormat("0.00");


    public RetailSOActivityArrayAdapter(@NonNull Context context, int resource, double ptotal, List<AllocateQtyPL> listSODetailPL, ArrayList<AllocateQtyPL> detailList,
                                        ArrayList<SaveProductSOPL> sList, List<String> batchCode, List<String> batchExpiry,
                                        List<Double> batchMrp, List<Double> batchRate, List<Double> batchSOH, Context appContext, int selectedPos, String itemCode) {
        super(context, resource);
        this.context = context;
        this.appContext = appContext;
        this.productTotal = ptotal;
        this.itemArraylist = detailList;
        this.listSODetailPL = listSODetailPL;
        this.sList = sList;
        this.batchCode = batchCode;
        this.tempBatchCode = batchCode;
        this.batchExpiry = batchExpiry;
        this.batchMrp = batchMrp;
        this.batchRate = batchRate;
        this.batchSOH = batchSOH;
        this.selectedPos = selectedPos;
        this.pID = itemCode;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        try {
            //itemDetail = itemArraylist.get(position);
        } catch (Exception e) {
            e.printStackTrace();
            //callItemList(position);
        }

        try {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_retail_saleorder, parent, false);
            tvProductName = convertView.findViewById(R.id.tvProductName);
            tvmrp = convertView.findViewById(R.id.tvmrp);
            tvMrp = convertView.findViewById(R.id.tvMRP);
            TextView tvRate = convertView.findViewById(R.id.tvRate);
            TextView editQty = convertView.findViewById(R.id.tvqty);
            tvSOH = convertView.findViewById(R.id.tvSOHInQtySelection);
            TextView tvQty = convertView.findViewById(R.id.tvQty);
            TextView tvFreeQty = convertView.findViewById(R.id.tvFreeQty);
            TextView tvTotal = convertView.findViewById(R.id.tvTotal);
            final ImageButton imgBtnRemarksItem = convertView.findViewById(R.id.imgBtnRemarksItem);
            btnDelete = convertView.findViewById(R.id.btnDeleteItem);

            try {
                //"batchCode":"EX271","batchId":1025034269,"batchMrp":14.96,"Rate":10.02,"sohInPacks":0.47,"batchExpiry":"01/01/2023"}]}

                tvProductName.setText(sList.get(position).pName);
                tvmrp.setText(String.valueOf(sList.get(position).batchMrp));
                tvMrp.setText(String.valueOf(sList.get(position).Rate));
                tvQty.setText(String.valueOf(sList.get(position).qty));
                editQty.setText(String.valueOf(sList.get(position).batchCode));
                tvRate.setText(String.valueOf(sList.get(position).SOH));
                double d = Double.parseDouble(sList.get(position).pTotal);
                //tvTotal.setText(sList.get(position).pTotal);
                tvTotal.setText(decfor.format(d));
                tvFreeQty.setText(String.valueOf(sList.get(position).Disc) + "%");

            } catch (Exception e) {
                e.printStackTrace();
            }


            btnDelete.setOnClickListener(new View.OnClickListener() {// delete
                @Override
                public void onClick(View v) {
                    try {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        alertDialogBuilder.setMessage("Do you want to delete the entry ...?");
                        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                productTotal = productTotal - Double.parseDouble(sList.get(position).pTotal);
                                //((RetailSOActivity) context).productTotal = productTotal;
                                sList.remove(position);
                                ((RetailSOActivity) context).sList = sList;
                                // callItemTotal();
                                ((RetailSOActivity) context).productTotal = productTotal;
                                System.out.println("save list size is " + ((RetailSOActivity) context).sList.size());
                                gson = new Gson();
                                ((RetailSOActivity) context).formedSO = gson.toJson(((RetailSOActivity) context).sList);
                                System.out.println("Formed json in Adapter class is  " +  ((RetailSOActivity) context).formedSO);
                                RetailSOActivityArrayAdapter arrayAdapter = new RetailSOActivityArrayAdapter(context,
                                        R.layout.list_row, productTotal, listSODetailPL, itemArraylist, sList, batchCode, batchExpiry, batchMrp, batchRate, batchSOH, appContext, selectedPos, pID);
                                ((RetailSOActivity) context).lvProductlist.setAdapter(arrayAdapter);
                                if (sList.size() == 0) {
                                    productTotal = 0.00;
                                    ((RetailSOActivity) context).productTotal = 0.0;
                                }
                                ((RetailSOActivity) context).tvAmountValue.setText(String.format("%.2f", productTotal));
                                arrayAdapter.notifyDataSetChanged();

                            }
                        });
                        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();

                            }
                        });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            editQty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prevTotal = Double.parseDouble(sList.get(position).pTotal);
                    System.out.println("Current total is " + position);
                    callDialog(position);
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;

    }

    private void callDialog(int position) {

        qtydialog = new Dialog(context);
        qtydialog.setContentView(R.layout.quantity_retail_selection_dialogwindow);
        qtydialog.setCanceledOnTouchOutside(false);
        qtydialog.setTitle("Quantity Selection");
        qtydialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(qtydialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        System.out.println("currentBatchCode:--------------->"+sList.get(position).batchCode);
        currentBcode=sList.get(position).batchCode;
        TextView tvSelectedItemName = qtydialog.findViewById(R.id.tvSelectedItemName);
        TextInputEditText discPer = qtydialog.findViewById(R.id.discField);
        TextView pTotal = qtydialog.findViewById(R.id.pTotal);
        pTotal.setText(sList.get(position).pTotal);
        tvSelectedItemName.setText(sList.get(position).pName);
        discPer.setText(String.valueOf(sList.get(position).Disc));
        TextView tvSelectedItemCode = qtydialog.findViewById(R.id.tvSelectedItemCode);
        tvSelectedItemCode.setText("Product Code: " + sList.get(position).pCode);
        temppCode = sList.get(position).pCode;
        TextView tvItemSOH = qtydialog.findViewById(R.id.tvItemSOH);
        tvItemSOH.setText("SOH: " + sList.get(position).SOH);
        TextView tvSelectedItemExp = qtydialog.findViewById(R.id.tvSelectedItemExp);
        TextView tvItemMRP = qtydialog.findViewById(R.id.tvItemMRP);
        TextView tvbMRP = qtydialog.findViewById(R.id.tvbMRP);
        tvbMRP.setText("MRP: " + sList.get(position).batchMrp);
        tvItemMRP.setText("Rate: " + sList.get(position).Rate);
        pRate = sList.get(position).Rate;
        tvSelectedItemExp.setText("Expiry: " + sList.get(position).batchExpiry);
        Button caculate = qtydialog.findViewById(R.id.caculate);
        EditText etQty = qtydialog.findViewById(R.id.etQty);
        etQty.setText(sList.get(position).qty);
        ImageButton imgBtn = qtydialog.findViewById(R.id.imgBtn);
        ImageButton imgBtnMinusPack = qtydialog.findViewById(R.id.imgBtnMinusPack);
        ImageButton imgBtnPlusPack = qtydialog.findViewById(R.id.imgBtnPlusPack);
        etQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String temp = editable.toString().trim();
                System.out.println(temp);
                if (temp.length() > 1) {
                    if (temp.contains("-")) {
                        // temp.replace("-", "");
                        temp = String.valueOf(Integer.parseInt(temp) * -1);
                        System.out.println(temp);
                    }
                }
            }
        });
        imgBtnMinusPack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qty = Integer.parseInt(etQty.getText().toString());
                qty--;
                etQty.setText(String.valueOf(qty));
                if (qty < 0) {
                    etQty.setText("0");
                }
            }
        });
        imgBtnPlusPack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qty = Integer.parseInt(etQty.getText().toString());
                qty++;
                etQty.setText(String.valueOf(qty));
                if (qty < 0) {
                    etQty.setText("0");
                }
            }
        });
        Button btnAddItem_qtySelection = qtydialog.findViewById(R.id.btnAddItem_qtySelection);
        Spinner bSpinner = qtydialog.findViewById(R.id.batchSpinner);
        ad = new RetailCustomAdapter(appContext, batchCode, batchExpiry, batchMrp, batchRate, batchSOH);
        bSpinner.setAdapter(ad);
        for (int i = 0; i < sList.size(); i++) {
            if (sList.get(i).batchCode.equalsIgnoreCase(currentBcode)){
                bSpinner.setSelection(i+1);
                System.out.println("Selected batch code");
            }
        }
        // bSpinner.setSelection(selectedPos);
        bSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tempBCode = batchCode.get(i);

                for (int j = 0; j < sList.size(); j++) {
                    if (sList.get(j).pID.equalsIgnoreCase(pID)) {
                        if (!tempBCode.equalsIgnoreCase("Select")) {
                            for (int k = 0; k < sList.size(); k++) {
                                if (sList.get(k).batchCode.equalsIgnoreCase(tempBCode)) {
                                } else {
                                    prevBCode = sList.get(j).batchCode;
                                    prevBCodepPOS = j;
                                    sList.get(j).batchCode = tempBCode;
                                }
                            }
                        } else {
                            Toast.makeText(context, "Invalid Selection", Toast.LENGTH_SHORT).show();
                        }

                    }
                }


                ((RetailSOActivity) context).sList = sList;
                ((RetailSOActivity) context).selectedPos = i;

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qtydialog.cancel();
            }
        });

        caculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnAddItem_qtySelection.setEnabled(true);
                double d = 0.0, disc = 0.0, temp;
                if (!etQty.getText().toString().isEmpty()) {
                    ((RetailSOActivity) context).sList.get(position).qty = etQty.getText().toString();
                    if (!discPer.getText().toString().isEmpty()) {
                        ((RetailSOActivity) context).sList.get(position).Disc = Double.parseDouble(discPer.getText().toString());
                        sList.get(position).Disc = Double.parseDouble(discPer.getText().toString());
                        if (Double.parseDouble(discPer.getText().toString()) > 0) {
                            disc = Double.parseDouble(discPer.getText().toString()) / 100;
                            d = pRate * Double.parseDouble(etQty.getText().toString());
                            temp = disc * d;
                            tempTotal = String.valueOf(round(d, 2) - temp);
                            //ptotal = tempTotal;
                            ptotal = String.valueOf(decfor.format(Double.parseDouble(tempTotal)));
                            System.out.println("After Discount " + total);
                            //pTotal.setText("Total: " + tempTotal);
                            //pTotal.setText("Total: " + String.format("%.2f", tempTotal));
                            pTotal.setText(decfor.format(Double.parseDouble(tempTotal)));
                        } else {

                            d = d + (pRate * Double.parseDouble(etQty.getText().toString()));
                            tempTotal = String.valueOf(round(d, 2));
                            ptotal = tempTotal;
                            // pTotal.setText("Total: " + tempTotal);
                            //pTotal.setText("Total: " + String.format("%.2f", tempTotal));
                            pTotal.setText(decfor.format(d));
                        }
                    } else {
                        d = pRate * Double.parseDouble(etQty.getText().toString());

                        tempTotal = String.valueOf(round(d, 2));
                        ptotal = tempTotal;
                        // pTotal.setText("Total: " + tempTotal);
                        pTotal.setText(decfor.format(d));
                        //String.format("%.2f", sList.get(position).pTotal)

                    }
                    System.out.println("Product Total is " + tempTotal);


                } else {
                    Toast.makeText(context, "Empty Quantity", Toast.LENGTH_SHORT).show();
                }

            }
        });


        btnAddItem_qtySelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((RetailSOActivity) context).sList.get(position).pTotal = tempTotal;
                productTotal = productTotal - prevTotal;
                System.out.println("Value 1 " + productTotal);
                productTotal = productTotal + Double.parseDouble(tempTotal);
                System.out.println("Value 2 " + productTotal);
                ((RetailSOActivity) context).productTotal = productTotal;
                System.out.println("Total Product Total after Edit is  " + productTotal);
                ((RetailSOActivity) context).tvAmountValue.setText(String.format("%.2f", productTotal));
                try {


                    gson = new Gson();
                    String temp = gson.toJson(sList);
                    ((RetailSOActivity) context).formedSO=temp;
                    System.out.println("New SList is " + temp);


                    arrayAdapter = new RetailSOActivityArrayAdapter(context,
                            R.layout.list_row, productTotal, listSODetailPL, itemArraylist, sList, batchCode, batchExpiry, batchMrp, batchRate, batchSOH, appContext, selectedPos, pID);
                    ((RetailSOActivity) context).lvProductlist.setAdapter(arrayAdapter);
                    // ((RetailSOActivity) context).tvAmountValue.setText(String.format("%.2f", productTotal));
                    arrayAdapter.notifyDataSetChanged();
                    qtydialog.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //qtydialog.cancel();
            }
        });

        lp.gravity = Gravity.CENTER;
        qtydialog.getWindow().setAttributes(lp);
        qtydialog.show();

    }

    @Override
    public int getCount() {
        return sList.size();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void callItemTotal() {
        double CurrentproductTotal = 0.0;
        try {
            for (int i = 0; i < sList.size(); i++) {
                CurrentproductTotal = CurrentproductTotal + (Double.parseDouble(sList.get(i).qty) * sList.get(i).Rate);
            }
            System.out.println("Current Total value is " + CurrentproductTotal);
            productTotal = CurrentproductTotal;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
    /*   if (tempbatchList.contains(tempBCode))
                        System.out.println("Duplicate batch code");
                    else {
                        System.out.println("No duplicates.. Adding New Batch mCode");
                        RetailSOActivityArrayAdapter arrayAdapter = new RetailSOActivityArrayAdapter(context,
                                R.layout.list_row, productTotal, listSODetailPL, itemArraylist, sList, batchCode, batchExpiry, batchMrp, batchRate, batchSOH, appContext, selectedPos, pID);
                        ((RetailSOActivity) context).lvProductlist.setAdapter(arrayAdapter);
                        ((RetailSOActivity) context).tvAmountValue.setText(String.format("%.2f", productTotal));
                        arrayAdapter.notifyDataSetChanged();
                        qtydialog.cancel();
                    }*/

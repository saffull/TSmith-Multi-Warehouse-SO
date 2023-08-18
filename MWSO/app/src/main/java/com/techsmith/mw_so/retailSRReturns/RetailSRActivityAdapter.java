package com.techsmith.mw_so.retailSRReturns;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.techsmith.mw_so.RetailSRBillCustomerInfoActivity;
import com.techsmith.mw_so.RetailSalesReturnActivity;
import com.techsmith.mw_so.retail_utils.SaveProductSOPL;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RetailSRActivityAdapter extends ArrayAdapter {
    Context context;
    TextView tvProductName, tvSOH, tvmrp, tvSelectedItemName, tvMrp, tvSlNo;
    private static final DecimalFormat df = new DecimalFormat("0.00");
    ImageButton btnDelete;
    Gson gson;
    RetailSRActivityAdapter arrayAdapter;
    Double productTotal, total = 0.0, prevTotal = 0.0, pRate, prevQty = 0.0;
    Dialog qtydialog;
    public ArrayList<SaveProductSOPL> pList;
    RetailSRCustomAdapter ad;
    List<String> batchCode, batchExpiry, qtyList, discList;
    List<Double> batchMrp, batchRate, batchSOH;
    String tempTotal = "", ptotal = "", tempBCode = "", currentQty = "", currentDisc = "", currentBcode = "";
    public ArrayList<ITEM> sList;

    public RetailSRActivityAdapter(@NonNull Context context, int resource, ArrayList<ITEM> sList) {
        super(context, resource);
        this.context = context;
        this.sList = sList;

        batchCode = new ArrayList<>();
        batchExpiry = new ArrayList<>();
        batchMrp = new ArrayList<>();
        batchRate = new ArrayList<>();
        batchSOH = new ArrayList<>();
        qtyList = new ArrayList<>();
        pList = new ArrayList<>();
        discList = new ArrayList<>();
        for (int i = 0; i < sList.size(); i++) {
            batchCode.add(sList.get(i).BATCHCODE);
            batchExpiry.add(sList.get(i).BATCHEXPIRY);
            batchMrp.add(Double.valueOf(sList.get(i).MRP));
            qtyList.add(sList.get(i).PACKQTY);
            discList.add(sList.get(i).DISCPER);
        }

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.retail_sales_return_order, parent, false);
        try {
            tvProductName = convertView.findViewById(R.id.tvProductName);
            tvmrp = convertView.findViewById(R.id.tvmrp);
            tvMrp = convertView.findViewById(R.id.tvMRP);
            tvSlNo = convertView.findViewById(R.id.tvSlNo);
            TextView tvRate = convertView.findViewById(R.id.tvRate);
            TextView editQty = convertView.findViewById(R.id.tvqty);
            tvSOH = convertView.findViewById(R.id.tvSOHInQtySelection);
            TextView tvQty = convertView.findViewById(R.id.tvQty);
            TextView tvFreeQty = convertView.findViewById(R.id.tvFreeQty);
            TextView tvTotal = convertView.findViewById(R.id.tvTotal);
            final ImageButton imgBtnRemarksItem = convertView.findViewById(R.id.imgBtnRemarksItem);
            btnDelete = convertView.findViewById(R.id.btnDeleteItem);

            try {

                tvProductName.setText(sList.get(position).ITEMNAME);
                tvmrp.setText(String.valueOf(sList.get(position).MRP));
                tvMrp.setText(String.valueOf(sList.get(position).RATE));
                tvQty.setText(String.valueOf(sList.get(position).PACKQTY));
                prevQty = 0.0;
                prevQty = Double.valueOf(sList.get(position).PACKQTY);
                editQty.setText(String.valueOf(sList.get(position).BATCHCODE));

                tvTotal.setText(String.valueOf(sList.get(position).LINETOTAL));
                tvSlNo.setText(sList.get(position).LINEID);
                currentDisc = sList.get(position).DISCPER;
                tvFreeQty.setText(String.valueOf(sList.get(position).DISCPER) + "%");

                btnDelete.setOnClickListener(new View.OnClickListener() {// delete
                    @Override
                    public void onClick(View v) {
                        try {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                            alertDialogBuilder.setMessage("Do you want to delete the entry ...?");
                            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int arg1) {
                                    sList.remove(position);
                                    ((RetailSalesReturnActivity) context).sList = sList;
                                    callItemTotal();
                                    System.out.println("save list size is " + ((RetailSalesReturnActivity) context).sList.size());
                                    RetailSRActivityAdapter arrayAdapter = new RetailSRActivityAdapter(context,
                                            R.layout.list_row, sList);
                                    ((RetailSalesReturnActivity) context).lvProductlist.setAdapter(arrayAdapter);
                                    if (sList.size() == 0) {
                                        productTotal = 0.00;
                                        ((RetailSalesReturnActivity) context).productTotal = 0.0;
                                    }
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
                    public void onClick(View view) {
                        System.out.println("Current total is " + position);
                        callDialog(position);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    private void callItemTotal() {
        Double newTotal = 0.0;
        for (int i = 0; i < sList.size(); i++) {
            newTotal = newTotal + Double.parseDouble(sList.get(i).RATE) * Double.parseDouble(sList.get(i).PACKQTY);
        }
        System.out.println("New Total is " + newTotal);
        ((RetailSalesReturnActivity) context).tvAmountValue.setText(String.valueOf(round(newTotal, 2)));
        try {
            gson = new Gson();
            String temp = gson.toJson(sList);
            //((RetailSalesReturnActivity) context).formedSO = temp;
            System.out.println("New SList is " + temp);


            arrayAdapter = new RetailSRActivityAdapter(context, R.layout.list_row, sList);
            ((RetailSalesReturnActivity) context).lvProductlist.setAdapter(arrayAdapter);
            ((RetailSalesReturnActivity) context).tvAmountValue.setText(String.format("%.2f", newTotal));
            arrayAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        System.out.println("currentBatchCode:--------------->" + sList.get(position).BATCHCODE);
        currentBcode = sList.get(position).BATCHCODE;
        TextView tvSelectedItemName = qtydialog.findViewById(R.id.tvSelectedItemName);
        TextInputEditText discPer = qtydialog.findViewById(R.id.discField);
        TextView pTotal = qtydialog.findViewById(R.id.pTotal);

        tvSelectedItemName.setText(sList.get(position).ITEMNAME);
        discPer.setText(String.valueOf(sList.get(position).DISCPER));
        TextView tvSelectedItemCode = qtydialog.findViewById(R.id.tvSelectedItemCode);
        tvSelectedItemCode.setText("Product Code: " + sList.get(position).ITEMCODE);
        // temppCode = sList.get(position).pCode;
        TextView tvItemSOH = qtydialog.findViewById(R.id.tvItemSOH);
        tvItemSOH.setText("Article Quantity: " + sList.get(position).PACKQTY);
        TextView tvSelectedItemExp = qtydialog.findViewById(R.id.tvSelectedItemExp);
        TextView tvItemMRP = qtydialog.findViewById(R.id.tvItemMRP);
        TextView tvbMRP = qtydialog.findViewById(R.id.tvbMRP);
        tvbMRP.setText("MRP: " + sList.get(position).MRP);
        tvItemMRP.setText("Rate: " + sList.get(position).RATE);
        pRate = Double.parseDouble(sList.get(position).RATE);
        tvSelectedItemExp.setText("Expiry: " + sList.get(position).BATCHEXPIRY);
        Button caculate = qtydialog.findViewById(R.id.caculate);
        EditText etQty = qtydialog.findViewById(R.id.etQty);
        if (sList.get(position).PACKQTY.contains(".")) {
            String string = sList.get(position).PACKQTY;
            String[] parts = string.split("\\.");
            currentQty = parts[0]; // 004
            String part2 = parts[1];
            etQty.setText(currentQty);
        } else {
            etQty.setText(sList.get(position).PACKQTY);
        }

        ImageButton imgBtn = qtydialog.findViewById(R.id.imgBtn);
        ImageButton imgBtnMinusPack = qtydialog.findViewById(R.id.imgBtnMinusPack);
        ImageButton imgBtnPlusPack = qtydialog.findViewById(R.id.imgBtnPlusPack);
        pTotal.setText(String.valueOf(Double.parseDouble(sList.get(position).RATE) * Double.parseDouble(sList.get(position).PACKQTY)));
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
        Button btnAddItem_qtySelection = qtydialog.findViewById(R.id.btnAddItem_qtySelection);
        Spinner bSpinner = qtydialog.findViewById(R.id.batchSpinner);
        ad = new RetailSRCustomAdapter(context, batchCode, batchExpiry, batchMrp, batchRate, batchSOH);
        bSpinner.setAdapter(ad);
        bSpinner.setEnabled(false);
        for (int i = 0; i < sList.size(); i++) {
            if (sList.get(i).BATCHCODE.equalsIgnoreCase(currentBcode)) {
                bSpinner.setSelection(i);
                System.out.println("Selected batch code");
            }
        }

        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qtydialog.cancel();
            }
        });

        caculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Double.parseDouble(etQty.getText().toString()) > Double.parseDouble(sList.get(position).PACKQTY)) {
                    System.out.println("Current item quantity is " + prevQty);
                    Toast.makeText(context, " Quantity Cannot Be Increased", Toast.LENGTH_SHORT).show();
                } else if (Double.parseDouble(discPer.getText().toString()) > 100) {
                    Toast.makeText(context, "Discount cannot be more than 100%", Toast.LENGTH_SHORT).show();
                } else if (Double.parseDouble(discPer.getText().toString()) < Double.parseDouble(currentDisc)) {
                    Toast.makeText(context, "Discount cannot be decreased..!!", Toast.LENGTH_SHORT).show();
                } else {
                    btnAddItem_qtySelection.setEnabled(true);
                    double d = 0.0, disc = 0.0, temp;
                    if (!etQty.getText().toString().isEmpty()) {
                        ((RetailSalesReturnActivity) context).sList.get(position).PACKQTY = etQty.getText().toString();
                        sList.get(position).PACKQTY = etQty.getText().toString();
                        if (!discPer.getText().toString().isEmpty()) {
                            ((RetailSalesReturnActivity) context).sList.get(position).DISCPER = discPer.getText().toString();
                            sList.get(position).DISCPER = discPer.getText().toString();
                            if (Double.parseDouble(discPer.getText().toString()) > 0) {
                                disc = Double.parseDouble(discPer.getText().toString()) / 100;
                                d = pRate * Double.parseDouble(etQty.getText().toString());
                                temp = disc * d;
                                tempTotal = String.valueOf(round(d, 2) - temp);
                                ptotal = String.valueOf(df.format(Double.parseDouble(tempTotal)));
                                sList.get(position).LINETOTAL = Double.parseDouble(ptotal);
                                System.out.println("After Discount " + total);
                                //df.format(Math.round(productTotal))
                                pTotal.setText(df.format(Math.round(Double.parseDouble(tempTotal))));
                                ((RetailSalesReturnActivity) context).tvAmountValue.setText(df.format(Math.round(Double.parseDouble(tempTotal))));

                            } else {
                                d = Double.parseDouble(sList.get(position).RATE) * Double.parseDouble(etQty.getText().toString());
                                tempTotal = String.valueOf(round(d, 2));
                                ptotal = String.valueOf(df.format(Double.parseDouble(tempTotal)));
                                sList.get(position).LINETOTAL = Double.parseDouble(ptotal);
                                System.out.println("After Discount " + total);
                                //df.format(Math.round(productTotal))
                                pTotal.setText(df.format(Math.round(Double.parseDouble(tempTotal))));
                                ((RetailSalesReturnActivity) context).tvAmountValue.setText(df.format(Math.round(Double.parseDouble(tempTotal))));
                            }

                        } else {
                            d = Double.parseDouble(sList.get(position).RATE) * Double.parseDouble(etQty.getText().toString());
                            sList.get(position).LINETOTAL = d;
                            tempTotal = String.valueOf(round(d, 2));
                            ptotal = tempTotal;
                            pTotal.setText(df.format(Math.round(productTotal)));
                            ((RetailSalesReturnActivity) context).tvAmountValue.setText(df.format(Math.round(productTotal)));
                        }
                        arrayAdapter = new RetailSRActivityAdapter(context, R.layout.list_row, sList);
                        ((RetailSalesReturnActivity) context).lvProductlist.setAdapter(arrayAdapter);
                        arrayAdapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(context, "Empty Quantity", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });


        btnAddItem_qtySelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Double newTotal = 0.0;
                for (int i = 0; i < sList.size(); i++) {
                    if (Double.parseDouble(sList.get(i).DISCPER) == 0.0) {
                        newTotal = newTotal + Double.parseDouble(sList.get(i).RATE) * Double.parseDouble(sList.get(i).PACKQTY);
                    } else {
                        double d = Double.parseDouble(sList.get(i).RATE) * Double.parseDouble(sList.get(i).PACKQTY);
                        newTotal = newTotal + d - ((Double.parseDouble(sList.get(i).DISCPER) / 100) *
                                (Double.parseDouble(sList.get(i).RATE) * Double.parseDouble(sList.get(i).PACKQTY)));
                    }

                }
                System.out.println("New Total is " + newTotal);
                ((RetailSalesReturnActivity) context).tvAmountValue.setText(String.valueOf(round(newTotal, 2)));
                try {
                    gson = new Gson();
                    String temp = gson.toJson(sList);
                    //((RetailSalesReturnActivity) context).formedSO = temp;
                    System.out.println("New SList is " + temp);


                    arrayAdapter = new RetailSRActivityAdapter(context, R.layout.list_row, sList);
                    ((RetailSalesReturnActivity) context).lvProductlist.setAdapter(arrayAdapter);
                    ((RetailSalesReturnActivity) context).tvAmountValue.setText(df.format(Math.round(newTotal)));

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
}

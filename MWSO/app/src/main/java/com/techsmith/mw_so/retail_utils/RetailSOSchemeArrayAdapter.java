package com.techsmith.mw_so.retail_utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.techsmith.mw_so.R;
import com.techsmith.mw_so.RetailSOActivity;
import com.techsmith.mw_so.Spinner.RetailCustomAdapter;
import com.techsmith.mw_so.scheme_utils.ITEM;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RetailSOSchemeArrayAdapter extends ArrayAdapter {
    Context context, appContext;
    String IncomingData = "";
    int totalCount = 0;
    ImageButton btnDelete, imgBtn;
    EditText totaldiscamount, totaldiscperc, effschemediscperc, schemeoffamount, lcarddiscper, schemediscper;
    Dialog qtydialog;
    Gson gson;
    RetailCustomAdapter ad;
    ArrayList<ITEM> sList;
    TextView tvProductName, tvSOH, tvmrp, lineTotal, tvMrp;
    private static final DecimalFormat decfor = new DecimalFormat("0.00");

    public RetailSOSchemeArrayAdapter(@NonNull Context context, int resource, String s, int size, ArrayList<ITEM> schemeList) {
        super(context, resource);
        this.context = context;
        this.IncomingData = s;
        this.totalCount = size;
        this.sList = schemeList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        try {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_retail_schemes_order, parent, false);
            tvProductName = convertView.findViewById(R.id.tvProductName);
            tvmrp = convertView.findViewById(R.id.tvmrp);
            tvMrp = convertView.findViewById(R.id.tvMRP);
            TextView tvRate = convertView.findViewById(R.id.tvRate);
            TextView editQty = convertView.findViewById(R.id.tvqty);
            lineTotal = convertView.findViewById(R.id.lineTotal);
            tvSOH = convertView.findViewById(R.id.tvSOHInQtySelection);
            TextView tvQty = convertView.findViewById(R.id.tvQty);
            TextView tvFreeQty = convertView.findViewById(R.id.tvFreeQty);// disc space
            TextView tvTotal = convertView.findViewById(R.id.tvTotal);
            final ImageButton imgBtnRemarksItem = convertView.findViewById(R.id.imgBtnRemarksItem);
            btnDelete = convertView.findViewById(R.id.btnDeleteItem);


            try {
                tvProductName.setText(sList.get(position).ITEMNAME);
                tvmrp.setText(String.valueOf(sList.get(position).MRP));
                tvMrp.setText(String.valueOf(sList.get(position).RATE));
                tvQty.setText(String.valueOf(sList.get(position).PACKQTY));
                editQty.setText(String.valueOf(sList.get(position).BATCHCODE));
                tvRate.setText(String.valueOf(sList.get(position).BATCHID));
//                lineTotal.setText(String.valueOf(sList.get(position).LINETOTAL));
                double d = sList.get(position).RATE * sList.get(position).PACKQTY;
                //tvTotal.setText(sList.get(position).pTotal);
                tvTotal.setText(decfor.format(d));
                tvTotal.setText(String.valueOf(sList.get(position).LINETOTAL));
                lineTotal.setText(decfor.format(d));

                tvFreeQty.setText(sList.get(position).TOTALDISCPERC + "%, ...");

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                ((RetailSOActivity) context).discText.setText("Tot Disc%");
                ((RetailSOActivity) context).lineTotal.setVisibility(View.VISIBLE);
                ((RetailSOActivity) context).lineTotal.setText("Total");
                ((RetailSOActivity) context).fullTotal.setText("Line Total");
            } catch (Exception e) {
                e.printStackTrace();
            }

          /*  btnDelete.setOnClickListener(new View.OnClickListener() {// delete
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
                                System.out.println("Formed json in Adapter class is  " + ((RetailSOActivity) context).formedSO);
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
            });*/

            tvFreeQty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
        qtydialog.setContentView(R.layout.disc_retail_schemes);
        qtydialog.setCanceledOnTouchOutside(false);
        qtydialog.setTitle("Quantity Selection");
        qtydialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(qtydialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        imgBtn = qtydialog.findViewById(R.id.imgBtn);
        totaldiscamount = qtydialog.findViewById(R.id.totaldiscamount);
        totaldiscamount.setText(sList.get(position).TOTALDISCAMOUNT);
        totaldiscperc = qtydialog.findViewById(R.id.totaldiscperc);
        totaldiscperc.setText(sList.get(position).TOTALDISCPERC);
        effschemediscperc = qtydialog.findViewById(R.id.effschemediscperc);
        effschemediscperc.setText(sList.get(position).EFFSCHEMEDISCPERC);
        schemeoffamount = qtydialog.findViewById(R.id.schemeoffamount);
        schemeoffamount.setText(sList.get(position).SCHEMEOFFAMOUNT);
        lcarddiscper = qtydialog.findViewById(R.id.lcarddiscper);
        lcarddiscper.setText(sList.get(position).LCARDDISCPER);
        schemediscper = qtydialog.findViewById(R.id.schemediscper);
        schemediscper.setText(sList.get(position).SCHEMEDISCPER);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qtydialog.cancel();
            }
        });


        lp.gravity = Gravity.CENTER;
        qtydialog.getWindow().setAttributes(lp);
        qtydialog.show();
    }

    @Override
    public int getCount() {
        return totalCount;
    }


}

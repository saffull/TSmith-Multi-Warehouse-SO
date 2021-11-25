package com.techsmith.mw_so.collection_utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.techsmith.mw_so.Collections;
import com.techsmith.mw_so.R;
import com.techsmith.mw_so.SOActivity;
import com.techsmith.mw_so.utils.AllocateQtyPL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SOCollectionAdapter extends ArrayAdapter {

    public Double total = 0.0;
    ArrayList<CollectionPL> itemArraylist;
    Context context;
    TextView due, billno, count;
    EditText pay;
    CheckBox select;
    CollectionPL collectionPL;
    SOCollectionAdapter arrayAdapter;
    Button btnOk;

    public SOCollectionAdapter(@NonNull Context context, int resource, ArrayList<CollectionPL> detailList) {
        super(context, resource);
        this.context = context;
        this.itemArraylist = detailList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        try {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.collection_row_layout, parent, false);
            collectionPL = itemArraylist.get(position);
            final int edPos = position;
            due = convertView.findViewById(R.id.due);
            billno = convertView.findViewById(R.id.billno);
            pay = convertView.findViewById(R.id.pay);
            select = convertView.findViewById(R.id.select);
            btnOk = convertView.findViewById(R.id.btnOK);

            due.setText(String.valueOf(collectionPL.Balance));
            billno.setText(collectionPL.DocNo);
            pay.setText(collectionPL.ReceivedAmt);

            if (collectionPL.select)
                select.setChecked(true);
            else
                select.setChecked(false);


            try {
                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("pos" + itemArraylist.get(position).ReceivedAmt);

                        Intent intent = new Intent("custom-total");
                        intent.putExtra("command", "ok");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                });
                pay.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        System.out.println("text change at postion" + edPos);

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        itemArraylist.get(edPos).ReceivedAmt = "";
                        itemArraylist.get(edPos).ReceivedAmt = s.toString();
                        ((Collections) context).detailList = itemArraylist;

                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                // Intent intent = new Intent("custom-total");
                                // intent.putExtra("command", "ok");
                                // LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }
                        }, 3000);
                    }
                });

                select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (buttonView.isChecked()) {
                            System.out.println("CUrrent balance is " + itemArraylist.get(edPos).Balance);
                            itemArraylist.get(edPos).ReceivedAmt = String.valueOf(itemArraylist.get(edPos).Balance);
                            // total = ((double) itemArraylist.get(edPos).Balance) * 1;
                            System.out.println("Total is " + total);
                            itemArraylist.get(edPos).select = true;
                        } else {
                            itemArraylist.get(edPos).ReceivedAmt = "";
                            itemArraylist.get(edPos).select = false;
                            //total = -1 * Double.parseDouble(String.valueOf(itemArraylist.get(edPos).Balance));
                        }

                        ((Collections) context).detailList = itemArraylist;
                        arrayAdapter = new SOCollectionAdapter(context, R.layout.list_row, itemArraylist);
                        ((Collections) context).lvCollectionlist.setAdapter(arrayAdapter);
                        arrayAdapter.notifyDataSetChanged();
                        //((Collections) context).tvAmountValue.setText(String.format("%.2f", total));
                       /* Intent intent = new Intent("custom-message");
                        intent.putExtra("sum", total);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);*/
                        Intent intent = new Intent("custom-total");
                        intent.putExtra("command", "ok");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);


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

    @Override
    public int getCount() {
        return itemArraylist.size();
    }

}

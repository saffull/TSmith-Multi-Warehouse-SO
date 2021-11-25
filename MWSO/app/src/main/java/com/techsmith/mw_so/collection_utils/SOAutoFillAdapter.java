package com.techsmith.mw_so.collection_utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.techsmith.mw_so.R;

import java.util.ArrayList;

public class SOAutoFillAdapter extends ArrayAdapter {
    ArrayList<CollectionPL> itemArraylist;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Context context;
    int size;
    String loginResponse;
    TextView due, billno, count;
    EditText pay;
    CheckBox select;
    CollectionPL collectionPL;
    SOCollectionAdapter arrayAdapter;
    Button btnOk;

    public SOAutoFillAdapter(@NonNull Context context, int resource, ArrayList<CollectionPL> detailList) {
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

            due.setText(String.valueOf(collectionPL.balance));
            billno.setText(collectionPL.docNo);
            String temp = String.valueOf((int) collectionPL.amountCollected);
            if (!temp.isEmpty()) {
                pay.setText(temp);
                btnOk.setEnabled(false);
                select.setEnabled(false);
                pay.setEnabled(false);
            } else {
                pay.setText("");
                btnOk.setEnabled(true);
                select.setEnabled(true);
                pay.setEnabled(true);
            }
            Intent intent = new Intent("custom-total");
            intent.putExtra("command", "ok");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

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

package com.techsmith.mw_so.Dsr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.techsmith.mw_so.R;
import com.techsmith.mw_so.Sales_Return_List.SalesReturnListData;

import java.util.ArrayList;

public class DailySalesAdapter extends ArrayAdapter {
    Context context;
    LayoutInflater inflater;
    ArrayList<REPORT> sList;
    TextView tvSlNo, transaction, tender, tvDescription;

    public DailySalesAdapter(@NonNull Context context, int resource, ArrayList<REPORT> sList) {
        super(context, resource);
        this.context = context;
        inflater = (LayoutInflater.from(context));
        this.sList = sList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_daily_sales_report, parent, false);
        tvSlNo = convertView.findViewById(R.id.tvSlNo);
        transaction = convertView.findViewById(R.id.transaction);
        tender = convertView.findViewById(R.id.tender);
        tvDescription = convertView.findViewById(R.id.tvDescription);

        try {
            tvSlNo.setText(String.valueOf(" " + sList.get(position).SLNO));
            tvDescription.setText(sList.get(position).DESCRIPTION.trim());
            tender.setText("\u20B9 " + String.valueOf(sList.get(position).TENDER));
            transaction.setText("\u20B9 " + String.valueOf(sList.get(position).TRANSACTIONS));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return sList.size();
    }
}

package com.techsmith.mw_so.Spinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.techsmith.mw_so.R;

import java.util.List;

public class CustomAdapter extends BaseAdapter {

    Context context;
    String[] bankNames;
    private List<String> bList;
    LayoutInflater inflater;
    public CustomAdapter(Context applicationContext,  List<String> bList) {
        this.context = applicationContext;
        this.bList = bList;
        inflater = (LayoutInflater.from(applicationContext));
    }

   /* public CustomAdapter(Context applicationContext, String[] countryNames) {
        this.context = applicationContext;
        this.bankNames = countryNames;
        inflater = (LayoutInflater.from(applicationContext));
    }*/


    @Override
    public int getCount() {
        //return bankNames.length;
        return bList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView bankName;
        view = inflater.inflate(R.layout.custom_spinner_items, null);
        bankName = view.findViewById(R.id.bankNames);
        //bankName.setText(bankNames[i]);
        bankName.setText(bList.get(i));
        return view;
    }
}

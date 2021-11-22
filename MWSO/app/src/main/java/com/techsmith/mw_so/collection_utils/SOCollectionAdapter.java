package com.techsmith.mw_so.collection_utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.techsmith.mw_so.R;

import java.util.List;

public class SOCollectionAdapter extends ArrayAdapter {

    List<CollectionPL> listCollectionPL;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Context context;
    int size;
    String loginResponse;
    TextView due,billno,count;
    EditText pay;
    CheckBox select;

    public SOCollectionAdapter(@NonNull Context context, int resource, List<CollectionPL> listCollectionPL, int size) {
        super(context, resource);
        this.context = context;
        this.listCollectionPL = listCollectionPL;
        this.size = size;

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();
        loginResponse = prefs.getString("loginResponse", "");
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        try {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.collection_row_layout, parent, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

}

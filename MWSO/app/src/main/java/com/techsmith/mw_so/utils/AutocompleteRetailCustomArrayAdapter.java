package com.techsmith.mw_so.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.techsmith.mw_so.CustomerInformation;
import com.techsmith.mw_so.R;
import com.techsmith.mw_so.RetailCustomerInformation;

public class AutocompleteRetailCustomArrayAdapter extends KArrayAdapter<String>{


    Context mContext;
    int layoutResourceId;
    public String[]  items;
    public String[] mobs;
    public  String [] loyals;


    public AutocompleteRetailCustomArrayAdapter(Context mContext, int layoutResourceId, String[] objects,String[] mobs,String[] loyals) {

        super(mContext, layoutResourceId, objects);

        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.items = objects;
        this.mobs=mobs;
        this.loyals=loyals;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try {

            if(convertView == null) {
                // inflate the layout
                LayoutInflater inflater = ((RetailCustomerInformation) mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }

            // object item based on the position
            String objectItem = items[position];
            String MobItem=mobs[position];
            String loyalItem=loyals[position];

            // get the TextView and then set the text (item name) and tag (item ID) values
            TextView textViewItem = convertView.findViewById(R.id.cName);
            TextView txtmob=convertView.findViewById(R.id.cMob);
            TextView txtLoyalty=convertView.findViewById(R.id.cLoyalty);
            textViewItem.setText(objectItem);
            txtmob.setText(MobItem);
            txtLoyalty.setText("LC: "+loyalItem);

            // in case you want to add some style, you can do something like:
//            textViewItem.setBackgroundColor(Color.CYAN);

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;

    }

    @Override
    public int getCount() {
        return items.length;
    }

}


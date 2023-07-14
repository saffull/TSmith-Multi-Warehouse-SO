package com.techsmith.mw_so.retail_utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.techsmith.mw_so.R;
import com.techsmith.mw_so.RetailSOActivity;
import com.techsmith.mw_so.SOActivity;
import com.techsmith.mw_so.utils.KArrayAdapter;

public class AutoCompleteRetailProductCustomAdapter extends KArrayAdapter<String> {

    final String TAG = "AutoCompleteRetailProductCustomAdapter.java";
    Context mContext;
    int layoutResourceId;
    //    MyObject data[] = null;
    public String[] items;
    public String[] pCode;
    public String[] Soh;
    public String[] arrpId;


    public AutoCompleteRetailProductCustomAdapter(Context mContext, int layoutResourceId, String[] objects, String[] mrp, String[] soh, String[] arrpId) {

        super(mContext, layoutResourceId, objects);

        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.items = objects;
        this.pCode = mrp;
        this.Soh = soh;
        this.arrpId = arrpId;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try {

            /*
             * The convertView argument is essentially a "ScrapView" as described is Lucas post
             * http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
             * It will have a non-null value when ListView is asking you recycle the row layout.
             * So, when convertView is not null, you should simply update its contents instead of inflating a new row layout.
             */

            if (convertView == null) {
                // inflate the layout
                LayoutInflater inflater = ((RetailSOActivity) mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }

            // object item based on the position
            String objectItem = items[position];

            // get the TextView and then set the text (item name) and tag (item ID) values
            TextView textViewItem = convertView.findViewById(R.id.textViewItem);
            TextView txtMrp = convertView.findViewById(R.id.pCode);
            TextView txtSoh = convertView.findViewById(R.id.pSOH);

            try {
                textViewItem.setText(objectItem);
                txtMrp.setText("PCODE: " + pCode[position]);
                txtSoh.setText("SOH: " + Soh[position]);
            } catch (Exception e) {
                e.printStackTrace();
            }


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

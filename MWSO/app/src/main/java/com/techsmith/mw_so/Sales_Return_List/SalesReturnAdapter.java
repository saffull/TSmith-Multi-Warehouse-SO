package com.techsmith.mw_so.Sales_Return_List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.techsmith.mw_so.R;
import com.techsmith.mw_so.RetailSOActivity;
import com.techsmith.mw_so.RetailSalesReturnActivity;
import com.techsmith.mw_so.Sales_Bill_List_SalesReturn_List;
import com.techsmith.mw_so.retail_utils.RetailSOActivityArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class SalesReturnAdapter extends ArrayAdapter {
    Context context;
    LayoutInflater inflater;
    ImageButton btnPrintItem,btnEmailItem;
    TextView tvSlNo, tvssid, tvPartyName;
    ArrayList<SalesReturnListData> sList;
    String type = "";

    public SalesReturnAdapter(Context applicationContext, int resource, ArrayList<SalesReturnListData> sList, String type) {
        super(applicationContext, resource);
        this.context = applicationContext;
        inflater = (LayoutInflater.from(applicationContext));
        this.sList = sList;
        this.type = type;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        try {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_sales_list_bill_sr, parent, false);
            tvPartyName = convertView.findViewById(R.id.tvPartyName);
            tvssid = convertView.findViewById(R.id.tvssid);
            tvSlNo = convertView.findViewById(R.id.tvSlNo);
            btnPrintItem = convertView.findViewById(R.id.btnPrintItem);
            btnEmailItem=convertView.findViewById(R.id.btnEmailItem);

            try {
                if (type.equalsIgnoreCase("returns")) {
                    //tvSlNo.setText(sList.get(position).srsId);
                    tvSlNo.setText(String.valueOf(position+1));
                    tvPartyName.setText(sList.get(position).srsPartyName);
                    tvssid.setText(sList.get(position).srsFormatedNo);
                } else {
                    tvSlNo.setText(String.valueOf(position+1));
                    tvPartyName.setText(sList.get(position).ssPartyName);
                    tvssid.setText(sList.get(position).ssFormatedNo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            btnPrintItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (type.equalsIgnoreCase("returns")) {
                        System.out.println(sList.get(position).srsFormatedNo);
                        ((Sales_Bill_List_SalesReturn_List) context).startSessionPrint(sList.get(position).srsFormatedNo);
                    } else {
                        System.out.println(sList.get(position).ssFormatedNo);
                        ((Sales_Bill_List_SalesReturn_List) context).startSessionPrint(sList.get(position).ssFormatedNo);
                    }
                }
            });
            btnEmailItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (type.equalsIgnoreCase("returns")) {
                        System.out.println(sList.get(position).srsFormatedNo);
                        ((Sales_Bill_List_SalesReturn_List) context).startEmailSession(sList.get(position).srsFormatedNo);
                    } else {
                        System.out.println(sList.get(position).ssFormatedNo);
                        ((Sales_Bill_List_SalesReturn_List) context).startEmailSession(sList.get(position).ssFormatedNo);
                    }
                }
            });

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

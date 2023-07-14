package com.techsmith.mw_so.Spinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.techsmith.mw_so.R;

import java.util.List;

public class RetailCustomAdapter extends BaseAdapter {
    LayoutInflater inflater;
    Context context;
    List<String> batchCode, batchExpiry;
    List<Double> batchMrp, batchRate, batchSOH;

    public RetailCustomAdapter(Context applicationContext, List<String> batchCode, List<String> batchExpiry, List<Double> batchMrp, List<Double> batchRate, List<Double> batchSOH) {
        this.context = applicationContext;
        inflater = (LayoutInflater.from(applicationContext));
        this.batchCode = batchCode;
        this.batchSOH = batchSOH;
        this.batchExpiry = batchExpiry;
        this.batchMrp = batchMrp;
        this.batchRate = batchRate;
    }

    @Override
    public int getCount() {
        return batchCode.size();
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
        TextView pBatchCode, pSOH, pBatchExpiry, pRate, pMRP;
        LinearLayout mainLL;

        view = inflater.inflate(R.layout.custom_spinner_batch_retail, null);
        pBatchCode = view.findViewById(R.id.pBatchCode);
        pSOH = view.findViewById(R.id.pSOH);
        pBatchExpiry = view.findViewById(R.id.pBatchExpiry);
        pRate = view.findViewById(R.id.pRate);
        pMRP = view.findViewById(R.id.pMRP);
        mainLL = view.findViewById(R.id.mainLL);

       try {
           String temp = batchCode.get(i);
           if (!temp.equalsIgnoreCase("Select")) {
               pBatchCode.setText("BatchCode:"+batchCode.get(i));
               pSOH.setText("SOH:"+String.valueOf(batchSOH.get(i)));
               pBatchExpiry.setText("Expiry:"+batchExpiry.get(i));
               pRate.setText("Rate:"+String.valueOf(batchRate.get(i)));
               pMRP.setText("MRP:"+String.valueOf(batchMrp.get(i)));
           } else {
               pBatchCode.setText(batchCode.get(i));
               pSOH.setVisibility(View.GONE);
               pBatchExpiry.setVisibility(View.GONE);
               pRate.setVisibility(View.GONE);
               pMRP.setVisibility(View.GONE);
           }
       }catch (Exception e){
           Toast.makeText(context,"List Forming Error",Toast.LENGTH_SHORT).show();
           e.printStackTrace();
       }
        return view;
    }
}

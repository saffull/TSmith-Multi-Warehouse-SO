package com.techsmith.mw_so.e_invoice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.techsmith.mw_so.R;
import com.techsmith.mw_so.SOActivity;

import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private String[] myImageNameList;

    List<String> billidList;

    public InvoiceAdapter(Context ctx, String[] myImageNameList, List<String> billidList) {
        inflater = LayoutInflater.from(ctx);
        this.myImageNameList = myImageNameList;
        this.billidList = billidList;
        System.out.println("new list is " + billidList);
    }

    @NonNull
    @Override
    public InvoiceAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.invoice_recycler, parent, false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceAdapter.MyViewHolder holder, int position) {
        holder.name.setText(myImageNameList[position]);
        holder.billid.setText(billidList.get(position));
    }

    @Override
    public int getItemCount() {
        return myImageNameList.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, billid;
        Button invbtn;

        public MyViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            billid = itemView.findViewById(R.id.billid);
            invbtn=itemView.findViewById(R.id.invBtn);


        }

    }
}

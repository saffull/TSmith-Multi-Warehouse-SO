package com.techsmith.mw_so.payment_util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.techsmith.mw_so.Expandable.CourseAdapter;
import com.techsmith.mw_so.Model.CardModel;
import com.techsmith.mw_so.R;

import java.util.ArrayList;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.Viewholder> {
    private final Context context;
    private final ArrayList<PaymentMethodModel> courseModelArrayList;
    private String temp = "";

    // Constructor
    public PaymentAdapter(Context context, ArrayList<PaymentMethodModel> courseModelArrayList) {
        this.context = context;
        this.courseModelArrayList = courseModelArrayList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_menu_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        PaymentMethodModel model = courseModelArrayList.get(position);
        try {
            holder.tvProfileName.setText(model.getName());
            holder.tv_amount.setText("\u20B9 " + model.getAmount());
            holder.ivProfilePic.setImageResource(model.getImageName());
            if (model.getName().equalsIgnoreCase("Card")) {
                temp = "xxxx-xxxx-xxxx-" + model.getCardNo().trim().substring(10, 15);
                holder.card_no.setText(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return courseModelArrayList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        private final ImageView ivProfilePic;
        private TextView tvProfileName, tv_amount, card_no;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
            tvProfileName = itemView.findViewById(R.id.tvProfileName);
            tv_amount = itemView.findViewById(R.id.tv_amount);
            card_no = itemView.findViewById(R.id.card_no);
        }
    }
}


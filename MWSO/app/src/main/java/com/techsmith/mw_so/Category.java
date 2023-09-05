package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.techsmith.mw_so.Global.AppWide;

public class Category extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
//        getSupportActionBar().hide();
        startActivity(new Intent(Category.this, SoMenu.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Category.this);
        alertDialogBuilder.setMessage("Do you want to Cancel The SO..!!");
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                Intent i = new Intent(Category.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();


    }

    public void gotoSalesOrder(View view) {
        finish();
        //startActivity(new Intent(Category.this, CustomerInformation.class));
        startActivity(new Intent(Category.this, SoMenu.class));
    }

    public void gotoDeliveryPage(View view) {
        finish();
        startActivity(new Intent(Category.this, Collections.class));
    }

    public void gotoCollectionPage(View view) {
        finish();
        startActivity(new Intent(Category.this, Delivery.class));
    }
}
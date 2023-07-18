package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.view.View;

import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.techsmith.mw_so.Expandable.CourseAdapter;
import com.techsmith.mw_so.Expandable.RecyclerTouchListener;
import com.techsmith.mw_so.Global.AppWide;
import com.techsmith.mw_so.Model.CardModel;


public class SoMenu extends AppCompatActivity {
    List<String> groupList;
    List<String> childList;
    Map<String, List<String>> mobileCollection;
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    SharedPreferences myPrefs;
    SharedPreferences.Editor editor;
    String storeName = "Test NAme", className = "";


    private RecyclerView courseRV;
    CourseAdapter courseAdapter;
    private ArrayList<CardModel> courseModelArrayList;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(SoMenu.this, Category.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_so_menu);
        courseRV = findViewById(R.id.my_recycler_view);
        myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = myPrefs.edit();
        editor.remove("cardSave");
        editor.remove("cashSave");
        editor.apply();
        startData();
        className = this.getClass().getSimpleName();
        System.out.println("Class Name is " + className);
        courseAdapter = new CourseAdapter(this, courseModelArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        courseRV.setLayoutManager(linearLayoutManager);
        courseRV.setAdapter(courseAdapter);

        courseRV.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), courseRV, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                CardModel movie = courseModelArrayList.get(position);
                // Toast.makeText(getApplicationContext(), movie.getName()+ " is selected!", Toast.LENGTH_SHORT).show();
                if (movie.getName().equalsIgnoreCase("Create New SO")) {
                    finish();
                    startActivity(new Intent(SoMenu.this, CustomerInformation.class));
                } else if (movie.getName().equalsIgnoreCase("New Payment")) {
                    startActivity(new Intent(SoMenu.this, PaymentMenu.class));
                } else if (movie.getName().equalsIgnoreCase("Retail SO")) {
                    startActivity(new Intent(SoMenu.this, RetailCustomerInformation.class));
                } else {
                    Toast.makeText(SoMenu.this, "Function not yet Implemented..", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


        //  createGroupList();
        //createCollection();
        /*expandableListView = findViewById(R.id.elvMobiles);
        expandableListAdapter = new MyExpandableListAdapter(this, groupList, mobileCollection);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int lastExpandedPosition = -1;
            @Override
            public void onGroupExpand(int i) {
                if(lastExpandedPosition != -1 && i != lastExpandedPosition){
                    expandableListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = i;
            }
        });
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                String selected = expandableListAdapter.getChild(i,i1).toString();
                Toast.makeText(getApplicationContext(), "Selected: " + selected, Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
    }

    private void startData() {
        courseModelArrayList = new ArrayList<>();
        int i = R.drawable.ic_list;
        courseModelArrayList.add(new CardModel("Create New SO", i));
        courseModelArrayList.add(new CardModel("Retail SO", i));
        // courseModelArrayList.add(new CardModel("Stock Register", R.drawable.ic_report));
        // courseModelArrayList.add(new CardModel("View Invoices", R.drawable.ic_report));
        //courseModelArrayList.add(new CardModel("Party Ledger", R.drawable.ic_report));
        courseModelArrayList.add(new CardModel("Test Print", R.drawable.ic_report));
        courseModelArrayList.add(new CardModel("New Payment", R.drawable.ic_report));
        // courseModelArrayList.add(new CardModel("Stock Register", R.drawable.ic_report));
        // courseModelArrayList.add(new CardModel("Stock Register", R.drawable.ic_report));
        // courseModelArrayList.add(new CardModel("Stock Register", R.drawable.ic_report));
    }

    private void createCollection() {
        String[] samsungModels = {"New Sales Order"};
        String[] googleModels = {"Stock Register", "Sales Report"};
        String[] redmiModels = {"Redmi 9i", "Redmi Note 9 Pro Max", "Redmi Note 9 Pro"};
        String[] vivoModels = {"Vivo V20", "Vivo S1 Pro", "Vivo Y91i", "Vivo Y12"};
        String[] nokiaModels = {"Nokia 5.3", "Nokia 2.3", "Nokia 3.1 Plus"};
        String[] motorolaModels = {"Motorola One Fusion+", "Motorola E7 Plus", "Motorola G9"};
        mobileCollection = new HashMap<String, List<String>>();
        for (String group : groupList) {
            if (group.equals("Create SO")) {
                loadChild(samsungModels);
            } else if (group.equals("Reports"))
                loadChild(googleModels);
          /*  else if (group.equals("Redmi"))
                loadChild(redmiModels);
            else if (group.equals("Vivo"))
                loadChild(vivoModels);
            else if (group.equals("Nokia"))
                loadChild(nokiaModels);
            else
                loadChild(motorolaModels);*/
            mobileCollection.put(group, childList);
        }
    }

    private void loadChild(String[] mobileModels) {
        childList = new ArrayList<>();
        for (String model : mobileModels) {
            childList.add(model);
        }
    }

    private void createGroupList() {
        groupList = new ArrayList<>();
        groupList.add("Create SO");
        groupList.add("Reports");
        // groupList.add("Redmi");
        // groupList.add("Vivo");
        // groupList.add("Nokia");
        // groupList.add("Motorola");
    }

    public void gotosalesReport(View view) {
        Toast.makeText(SoMenu.this, "Function not yet Implemented..", Toast.LENGTH_LONG).show();
    }

    public void gotoStockRegister(View view) {
        Toast.makeText(SoMenu.this, "Function not yet Implemented..", Toast.LENGTH_LONG).show();
    }

    public void gotoSalesOrder(View view) {
        finish();
        startActivity(new Intent(SoMenu.this, CustomerInformation.class));
    }

    public void goToControllerVariables(View view) {
        try {
            AppWide appWide = AppWide.getInstance();
            String msg = "Store Name:" + appWide.getStoreName() + "\n Store Code:" + appWide.getStoreCode() +
                    "\nSubStore Id: " + appWide.getSubStoreId();
           /* String msg = "Store Name:" +   ((GlobalClass) getApplication()).getStoreName() + "\n Store Code:" + ((GlobalClass) getApplication()).getStoreCode() +
                    "\nSubStore Id: " + ((GlobalClass) getApplication()).getSubStoreId();*/
            popUp(msg);
        } catch (Exception e) {
            Toast.makeText(SoMenu.this, "Data Controller Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void popUp(String msg) {
        try {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SoMenu.this);
            alertDialogBuilder.setMessage(msg);
            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
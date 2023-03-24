package com.techsmith.mw_so;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.flarebit.flarebarlib.FlareBar;
import com.flarebit.flarebarlib.Flaretab;
import com.flarebit.flarebarlib.TabEventObject;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class PaymentActivity extends AppCompatActivity {
    FlareBar bottomBar;
    ViewPager simpleViewPager;
    TabLayout tabLayout;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout linearLayoutBSheet;
    private ToggleButton tbUpDown;
    private Button cancelBtn,updatePayment;
    public TextView billAmt,cashAmt,pay_total,cardAmt,balAmt;
    private  SharedPreferences prefs;
    private double total,payTotal=0.0;
    private String tempTotal="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        init();
        prefs = PreferenceManager.getDefaultSharedPreferences(PaymentActivity.this);
        simpleViewPager = findViewById(R.id.simpleViewPager);
        tabLayout = (TabLayout) findViewById(R.id.simpleTabLayout);
        tempTotal= prefs.getString("billTotal","200");
        total=Double.parseDouble(tempTotal);

        if (!tempTotal.isEmpty()){
            billAmt.setText(tempTotal);
        }

        // Create a new Tab named "First"
        TabLayout.Tab firstTab = tabLayout.newTab();
        firstTab.setText("Card"); // set the Text for the first Tab
        //firstTab.setIcon(R.drawable.avatar); // set an icon for the
        // first tab
        tabLayout.addTab(firstTab); // add  the tab at in the TabLayout
        // Create a new Tab named "Second"
        TabLayout.Tab secondTab = tabLayout.newTab();
        secondTab.setText("Cash"); // set the Text for the second Tab
        //secondTab.setIcon(R.drawable.avatar); // set an icon for the second tab
        tabLayout.addTab(secondTab); // add  the tab  in the TabLayout

        PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        simpleViewPager.setAdapter(adapter);
        // addOnPageChangeListener event change the tab on slide
        simpleViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                simpleViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



        tbUpDown.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View view, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    tbUpDown.setChecked(true);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    tbUpDown.setChecked(false);
                }
            }

            @Override
            public void onSlide(View view, float v) {

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        updatePayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (payTotal>Double.parseDouble(billAmt.getText().toString().trim())){
                    Toast.makeText(PaymentActivity.this,"Total Amount & Paid Amount Not tallied Correctly...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void init(){
        this.linearLayoutBSheet = findViewById(R.id.bottomSheetPayment);
        this.bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBSheet);
        this.tbUpDown = findViewById(R.id.toggleButton);
        this.cancelBtn=findViewById(R.id.cancelBtn);
        this.billAmt=findViewById(R.id.billAmt);
        this.cashAmt=findViewById(R.id.cashAmt);
        this.pay_total=findViewById(R.id.pay_total);
        this.updatePayment=findViewById(R.id.updatePayment);
        this.cardAmt=findViewById(R.id.cardAmt);
    }

    public void updateCashAmount(String cash){
        cashAmt.setText(cash);
        updatePayTotal(cash);


    }

    public void updateCardAmount(String cash){
        cardAmt.setText(cash);
        updatePayTotal(cash);
    }

    private void updatePayTotal(String cash){
        payTotal= Double.parseDouble(pay_total.getText().toString())+Double.parseDouble(cash);
        pay_total.setText(String.valueOf(payTotal));
    }
}














/*    bottomBar = findViewById(R.id.bottomBar);
        bottomBar.setBarBackgroundColor(Color.parseColor("#FFFFFF"));
        ArrayList<Flaretab> tabs = new ArrayList<>();
        tabs.add(new Flaretab(getResources().getDrawable(R.drawable.inboxb),"Inbox","#FFECB3"));
        tabs.add(new Flaretab(getResources().getDrawable(R.drawable.searchb),"Search","#80DEEA"));
        tabs.add(new Flaretab(getResources().getDrawable(R.drawable.phoneb),"Call Log","#B39DDB"));
        tabs.add(new Flaretab(getResources().getDrawable(R.drawable.avatarb),"Profile","#EF9A9A"));
        tabs.add(new Flaretab(getResources().getDrawable(R.drawable.settingsb),"Settings","#B2DFDB"));

        bottomBar.setTabList(tabs);
        bottomBar.attachTabs(PaymentActivity.this);

        bottomBar.setTabChangedListener(new TabEventObject.TabChangedListener() {
            @Override
            public void onTabChanged(LinearLayout selectedTab, int selectedIndex, int oldIndex) {
                //tabIndex starts from 0 (zero). Example : 4 tabs = last Index - 3
                Toast.makeText(PaymentActivity.this,"Tab "+ selectedIndex+" Selected.", Toast.LENGTH_SHORT).show();
            }
        });*/
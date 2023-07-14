package com.techsmith.mw_so.Global;

import android.app.Application;

public class GlobalClass extends Application {

    private String storeName;
    private int storeId;
    private int subStoreId;
    private String storeCode;
    private String LoyaltyCode;
    private String Name;

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public int getSubStoreId() {
        return subStoreId;
    }

    public void setSubStoreId(int subStoreId) {
        this.subStoreId = subStoreId;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getLoyaltyCode() {
        return LoyaltyCode;
    }

    public void setLoyaltyCode(String loyaltyCode) {
        this.LoyaltyCode = loyaltyCode;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }
}

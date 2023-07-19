package com.techsmith.mw_so.Global;

public class AppWide {
    private static volatile AppWide instance;

    // Restrict the constructor from being instantiated
    private AppWide() {
    }

    private int storeId;
    private int subStoreId;
    private String storeCode;
    private String storeName;
    private String LoyaltyCode;
    private String Name;
    private String count="test count";
    private String machineId;
    private String authID="SBRL1467-8950-4215-A5DC-AC04D7620B23";
    private String appUrl="";
    private String Rounding;
    private String MOBILENO;
    private String AREA;
    private String STATE;
    private  String ADDRESS;


    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public static synchronized AppWide getInstance() {
        if (instance == null) {
            instance = new AppWide();
        }
        return instance;
    }

    public int getSubStoreId() {
        return subStoreId;
    }

    public void setSubStoreId(int subStoreId) {
        this.subStoreId = subStoreId;
    }

    public String getLoyaltyCode() {
        return LoyaltyCode;
    }

    public void setLoyaltyCode(String loyaltyCode) {
        LoyaltyCode = loyaltyCode;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getAuthID() {
        return authID;
    }

    public void setAuthID(String authID) {
        this.authID = authID;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public String getRounding() {
        return Rounding;
    }

    public void setRounding(String rounding) {
        Rounding = rounding;
    }

    public String getMOBILENO() {
        return MOBILENO;
    }

    public void setMOBILENO(String MOBILENO) {
        this.MOBILENO = MOBILENO;
    }

    public String getAREA() {
        return AREA;
    }

    public void setAREA(String AREA) {
        this.AREA = AREA;
    }

    public String getSTATE() {
        return STATE;
    }

    public void setSTATE(String STATE) {
        this.STATE = STATE;
    }

    public String getADDRESS() {
        return ADDRESS;
    }

    public void setADDRESS(String ADDRESS) {
        this.ADDRESS = ADDRESS;
    }
}

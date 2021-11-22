package com.techsmith.mw_so.collection_utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class CollectionPL {
    @JsonProperty("DocNo")
    public String docNo;
    @JsonProperty("DocDate")
    public String docDate;
    @JsonProperty("VoucherNo")
    public String voucherNo;
    @JsonProperty("VoucherDate")
    public Date voucherDate;
    @JsonProperty("BillAmount")
    public double billAmount;
    @JsonProperty("Balance")
    public double balance;
    @JsonProperty("OverDuedays")
    public int overDuedays;
    @JsonProperty("DueDate")
    public Date dueDate;
    @JsonProperty("Terms")
    public String terms;
    @JsonProperty("Locationid")
    public int locationid;
    @JsonProperty("Lineid")
    public int lineid;
}

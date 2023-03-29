package com.techsmith.mw_so.payment_util;

public class PaymentMethodModel {
    private String amount;
    private String name;
    private int imageName;
    private String cardNo;

    public PaymentMethodModel(String name, int imageName,String amount,String cardNo) {
        this.name = name;
        this.imageName = imageName;
        this.amount=amount;
        this.cardNo=cardNo;
    }

    public int getImageName() {
        return imageName;
    }

    public void setImageName(int imageName) {
        this.imageName = imageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }
}

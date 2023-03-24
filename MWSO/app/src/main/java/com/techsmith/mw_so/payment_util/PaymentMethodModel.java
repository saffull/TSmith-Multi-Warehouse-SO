package com.techsmith.mw_so.payment_util;

public class PaymentMethodModel {
    private String amount;
    private String name;
    private int imageName;

    public PaymentMethodModel(String name, int imageName,String amount) {
        this.name = name;
        this.imageName = imageName;
        this.amount=amount;
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
}

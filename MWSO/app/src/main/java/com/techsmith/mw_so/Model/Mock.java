package com.techsmith.mw_so.Model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Mock {

    private Mock() {

    }

    public static PrintModel getPrintModelMock() {
        PrintBody printBody = new PrintBody();
        printBody.setDate("17/08/2020");
        printBody.setTime("22:29:52");
        printBody.setInvoice("000034");
        printBody.setQrCode("test qr code");
        printBody.setTotalPayment("Rp125.000");

        Date date = new Date();
        date.setTime(System.currentTimeMillis());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String format = simpleDateFormat.format(date);
        printBody.setTimeStamp(format);

        PrintFooter printFooter = new PrintFooter();
        printFooter.setInitial("INI BUKAN BUKTI PEMBAYARAN SAH");
        printFooter.setPaymentBy("Acquirer ACQ1");
        printFooter.setPowered("*** POWERED BY PRINTAMA ***");
        printFooter.setEnvironment("Development");

        PrintHeader printHeader = new PrintHeader();

        //set print header
        printHeader.setMerchantAddress1("Above UTI Mutual Funds");
        printHeader.setMerchantAddress2("Chittor Road, Ernakulam");
        printHeader.setMerchantName("Techsmith Software Private Limited");
        printHeader.setMerchantId("008");

        return new PrintModel(printHeader, printBody, printFooter);
    }
}

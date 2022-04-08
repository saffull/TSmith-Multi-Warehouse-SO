package com.techsmith.mw_so;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.anggastudio.printama.Printama;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.techsmith.mw_so.Model.Mock;
import com.techsmith.mw_so.Model.PrintBody;
import com.techsmith.mw_so.Model.PrintFooter;
import com.techsmith.mw_so.Model.PrintHeader;
import com.techsmith.mw_so.Model.PrintModel;
import com.techsmith.mw_so.Model.Util;

public class BluetoothActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        findViewById(R.id.btn_printer_settings).setOnClickListener(v -> showPrinterList());
        findViewById(R.id.btn_print_text_left).setOnClickListener(v -> printTextLeft());
        findViewById(R.id.btn_print_text_center).setOnClickListener(v -> printTextCenter());
        findViewById(R.id.btn_print_text_right).setOnClickListener(v -> printTextRight());
        findViewById(R.id.btn_print_text_style).setOnClickListener(v -> printTextStyles());
        findViewById(R.id.btn_print_text_justify).setOnClickListener(v -> printTextJustified());
        findViewById(R.id.btn_print_image_left).setOnClickListener(v -> printImageLeft());
        findViewById(R.id.btn_print_image_center).setOnClickListener(v -> printImageCenter());
        findViewById(R.id.btn_print_image_right).setOnClickListener(v -> printImageRight());
        findViewById(R.id.btn_print_image_ori).setOnClickListener(v -> printImageOri());
        findViewById(R.id.btn_print_image_full).setOnClickListener(v -> printImageFull());
        findViewById(R.id.btn_print_background).setOnClickListener(v -> printImageBackground());
        //findViewById(R.id.btn_print_image_photo).setOnClickListener(v -> printImagePhoto());
        findViewById(R.id.btn_print_layout).setOnClickListener(v -> printView());
        findViewById(R.id.btn_print_receipt).setOnClickListener(v -> printQrReceipt());
        findViewById(R.id.btn_print_receipt2).setOnClickListener(v -> printQrReceipt2());
        findViewById(R.id.printtsmith).setOnClickListener(v -> printOurReceipt());
    }

    private void showPrinterList() {
        Printama.showPrinterList(this, R.color.colorBlue, printerName -> {
            Toast.makeText(this, printerName, Toast.LENGTH_SHORT).show();
            TextView connectedTo = findViewById(R.id.tv_printer_info);
            String text = "Connected to : " + printerName;
            connectedTo.setText(text);
            if (!printerName.contains("failed")) {
                findViewById(R.id.btn_printer_test).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_printer_test).setOnClickListener(v -> testPrinter());
            }
        });
    }

    private void testPrinter() {
        Printama.with(this).printTest();
    }

    private void showPrinterListActivity() {
        // only use this when your project is not androidX
        Printama.showPrinterList(this);
    }

    private void printTextLeft() {
        String text = "-------------\n" +
                "This will be printed\n" +
                "Left aligned\n" +
                "cool isn't it?\n" +
                "A while back I needed to count top " +
                "------------------\n\n\n";
        Printama.with(this).connect(printama -> {
            printama.printText(text, Printama.LEFT);
            // or simply
            // printama.printText("some text") --> will be printed left aligned as default
            printama.close();
        });
    }

    private void printTextCenter() {
        String text = "-------------\n" +
                "This will be printed\n" +
                "Center aligned\n" +
                "cool isn't it?\n" +
                "A while back I needed to count top " +
                "------------------\n\n\n";
        Printama.with(this).connect(printama -> {
            printama.printText(text, Printama.CENTER);
            printama.close();
        });
    }

    private void printTextRight() {
        String text = "-------------\n" +
                "This will be printed\n" +
                "Right aligned\n" +
                "cool isn't it?\n" +
                "A while back I needed to count t" +
                "------------------\n\n\n";
        Printama.with(this).connect(printama -> {
            printama.printText(text, Printama.RIGHT);
            printama.close();
        });
    }

    private void printTextJustified() {
        Printama.with(this).connect(printama -> {

            printama.printTextJustify("text1", "text2");
            printama.printTextJustify("text1", "text2", "text3");
            printama.printTextJustify("text1", "text2", "text3", "text4");

            printama.printTextJustifyBold("text1", "text2");
            printama.printTextJustifyBold("text1", "text2", "text3");
            printama.printTextJustifyBold("text1", "text2", "text3", "text4");

            printama.setNormalText();
            printama.feedPaper();
            printama.close();
        }, this::showToast);
    }

    private void printTextStyles() {
        Printama.with(this).connect(printama -> {
            printama.setSmallText();
            printama.printText("small___________\n");
            printama.printTextln("Techsmith Software Private Limited");

            printama.setNormalText();
            printama.printText("normal__________\n");
            printama.printTextln("Techsmith Software Private Limited");

            printama.printTextNormal("bold____________\n");
            printama.printTextlnBold("Techsmith Software Private Limited");

            printama.setNormalText();
            printama.printTextNormal("tall____________\n");
            printama.printTextlnTall("Techsmith Software Private Limited");

            printama.printTextNormal("tall bold_______\n");
            printama.printTextlnTallBold("Techsmith Software Private Limited");

            printama.printTextNormal("wide____________\n");
            printama.printTextlnWide("Techsmith Software Private Limited");

            printama.printTextNormal("wide bold_______\n");
            printama.printTextlnWideBold("Techsmith Software Private Limited");

            printama.printTextNormal("wide tall_______\n");
            printama.printTextlnWideTall("Techsmith Software Private Limited");

            printama.printTextNormal("wide tall bold__\n");
            printama.printTextlnWideTallBold("Techsmith Software Private Limited");

            printama.printTextNormal("underline_______\n");
            printama.setUnderline();
            printama.printTextln("Techsmith Software Private Limited");

            printama.printTextNormal("delete line_____\n");
            printama.setDeleteLine();
            printama.printTextln("Techsmith Software Private Limited");

            printama.setNormalText();
            printama.feedPaper();
            printama.close();
        }, this::showToast);
    }

    private void printImageLeft() {
        Bitmap bitmap = Printama.getBitmapFromVector(this, R.mipmap.ic_launcher);
        Printama.with(this).connect(printama -> {
            printama.printImage(bitmap, 200, Printama.LEFT);
            printama.close();
        }, this::showToast);
    }

    private void printImageCenter() {
        Bitmap bitmap = Printama.getBitmapFromVector(this, R.mipmap.ic_launcher);
        Printama.with(this).connect(printama -> {
            boolean print = printama.printImage(bitmap, 200, Printama.CENTER);
            if (!print) {
                Toast.makeText(BluetoothActivity.this, "Print image failed", Toast.LENGTH_SHORT).show();
            }
            printama.close();
        }, this::showToast);
    }

    private void printImageRight() {
        Bitmap bitmap = Printama.getBitmapFromVector(this, R.mipmap.ic_launcher);
        Printama.with(this).connect(printama -> {
            printama.printImage(bitmap, 200, Printama.RIGHT);
            printama.close();
        }, this::showToast);
    }

    private void printImageOri() {
        Bitmap bitmap = Printama.getBitmapFromVector(this, R.mipmap.ic_launcher);
        Printama.with(this).connect(printama -> {
            printama.printImage(bitmap); // original size, centered as default
            printama.close();
        }, this::showToast);
    }

    private void printImageFull() {
        Bitmap bitmap = Printama.getBitmapFromVector(this, R.mipmap.ic_launcher);
        Printama.with(this).connect(printama -> {
            printama.printImage(bitmap, Printama.FULL_WIDTH);
            printama.close();
        }, this::showToast);
    }

    private void printImageBackground() {
        Bitmap bitmap = Printama.getBitmapFromVector(this, R.drawable.ic_launcher_background);
        Printama.with(this).connect(printama -> {
            printama.printImage(bitmap, Printama.ORIGINAL_WIDTH);
            printama.close();
        }, this::showToast);
    }

   /* private void printImagePhoto() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rose);
        Printama.with(this).connect(printama -> {
            printama.printImage(bitmap, Printama.FULL_WIDTH);
            printama.close();
        }, this::showToast);
    }*/

    private void printView() {
        View view = findViewById(R.id.root_view);
        Printama.with(this).connect(printama -> {
            printama.printFromView(view);
            new Handler().postDelayed(printama::close, 2000);
        }, this::showToast);
    }

    private void printQrReceipt() {
        PrintModel printModel = Mock.getPrintModelMock();
        Bitmap logo = Printama.getBitmapFromVector(this, R.drawable.logo_gopay_print);
        PrintHeader header = printModel.getPrintHeader();
        PrintBody body = printModel.getPrintBody();
        PrintFooter footer = printModel.getPrintFooter();
        String date = "DATE: " + body.getDate();
        String invoice = "INVOICE: " + body.getInvoice();

        Printama.with(this).connect(printama -> {
            printama.printImage(logo, 300);
            printama.addNewLine(1);
            printama.setNormalText();
            printama.printTextln(header.getMerchantName().toUpperCase(), Printama.CENTER);
            printama.printTextln(header.getMerchantAddress1().toUpperCase(), Printama.CENTER);
            printama.printTextln(header.getMerchantAddress2().toUpperCase(), Printama.CENTER);
            printama.printTextln("MERC" + header.getMerchantId().toUpperCase(), Printama.CENTER);
            printama.printDoubleDashedLine();

            // body
            printama.printTextln(date);
            printama.printTextln(invoice);

            printama.printDashedLine();
            printama.printTextln("PRINTABLE DATA", Printama.CENTER);
            printama.printDashedLine();
            printama.printTextln("Scan Qr Code", Printama.CENTER);
            printama.printImage(Util.getQrCode(body.getQrCode()), 100);
            printama.printTextln("TOTAL         " + body.getTotalPayment(), Printama.CENTER);

            // footer
            printama.printTextln(footer.getPaymentBy(), Printama.CENTER);
            if (footer.getIssuer() != null) printama.printText(footer.getIssuer(), Printama.CENTER);
            printama.printTextln(footer.getPowered(), Printama.CENTER);
            if (footer.getEnvironment() != null)
                printama.printTextln(footer.getEnvironment(), Printama.CENTER);
            printama.addNewLine(8);

            printama.close();
        }, this::showToast);
    }

    private void printOurReceipt() {
        String address = "http://www.tsmith.co.in";
        Printama.with(this).connect(printama -> {
            printama.printTextln("Techsmith Software Pvt Limited", Printama.CENTER);
            printama.setNormalText();
            printama.printDashedLine();
            printama.addNewLine();

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix;
            try {
                bitMatrix = writer.encode(address, BarcodeFormat.QR_CODE, 100, 100);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int color = Color.WHITE;
                        if (bitMatrix.get(x, y)) color = Color.BLACK;
                        bitmap.setPixel(x, y, color);
                    }
                }
                if (bitmap != null) {
                    printama.printImage(bitmap);
                }
            } catch (WriterException e) {
                e.printStackTrace();
            }
            printama.addNewLine();
            printama.feedPaper();
            printama.close();


        }, this::showToast);

    }

    private void printQrReceipt2() {
        Bitmap logo = Printama.getBitmapFromVector(this, R.drawable.logo_gopay_print);
        String nota = "Some Text";
        Printama.with(this).connect(printama -> {
            printama.printImage(logo, 200);
            printama.addNewLine();
            printama.printTextln("Title Text", Printama.CENTER);
            printama.setNormalText();
            printama.printTextln("Some Text", Printama.CENTER);
            printama.printDashedLine();
            printama.addNewLine();
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix;
            try {
                bitMatrix = writer.encode(nota, BarcodeFormat.QR_CODE, 300, 300);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int color = Color.WHITE;
                        if (bitMatrix.get(x, y)) color = Color.BLACK;
                        bitmap.setPixel(x, y, color);
                    }
                }
                if (bitmap != null) {
                    printama.printImage(bitmap);
                }
            } catch (WriterException e) {
                e.printStackTrace();
            }

            printama.addNewLine();
            printama.feedPaper();
            printama.close();
        }, this::showToast);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String printerName = Printama.getPrinterResult(resultCode, requestCode, data);
        showResult(printerName);
    }

    private void showResult(String printerName) {
        showToast(printerName);
        TextView connectedTo = findViewById(R.id.tv_printer_info);
        String text = "Connected to : " + printerName;
        connectedTo.setText(text);
        if (!printerName.contains("failed")) {
            findViewById(R.id.btn_printer_test).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_printer_test).setOnClickListener(v -> testPrinter());
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
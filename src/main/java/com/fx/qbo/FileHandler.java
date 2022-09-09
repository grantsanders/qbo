package com.fx.qbo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Scanner;

import javax.naming.LinkRef;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Line;

public class FileHandler {

    private String path;
    private ArrayList<Line> lineList = new ArrayList<Line>();
    private ArrayList<Invoice> finalInvoiceList = new ArrayList<Invoice>();
    private int refNumber;
    private int refPrevious;

    public FileHandler(String filePath) {
        path = "/Users/grantsanders/Downloads/BlueCartAccounting Report_09092022_0633pm.csv";
    }

    public void formatData() { // run filepath and pull data from CSV to be stored
        try {
            File inFile = new File(path);
            FileReader input = new FileReader(inFile);
            Scanner in = new Scanner(input);
            in.nextLine();
            ArrayList<String[]> baseItems = new ArrayList<String[]>();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            while (in.hasNext()) {
                String line = in.nextLine();
                line = line.replace("\"", "");
                String[] split = line.split(",");
                baseItems.add(split);
            }

            String[] currentArray = baseItems.get(0);
            refNumber = Integer.parseInt(currentArray[0]);
            refPrevious = refNumber;
            int invoiceCounter = 1;

            for (int i = 0; i < baseItems.size(); i++) {
                currentArray = baseItems.get(i);
                refNumber = Integer.parseInt(currentArray[0]);

                if (refNumber == refPrevious) {
                    System.out.println(i + " " + refNumber + " invoice # " + invoiceCounter);

                    Line newLine = createLineItem(currentArray);

                    lineList.add(newLine);

                    refPrevious = refNumber;
                } else {
                    createNewInvoice(lineList);
                    invoiceCounter++;
                }
            System.out.println(gson.toJson(lineList));

            }

        } catch (

        FileNotFoundException e) {
            Popup FileNotFoundException = new Popup("FileNotFoundException", "Error: File not found");
        }
    }

    public Line createLineItem(String[] data) {

        Line lineItem = new Line();
        lineItem.setAmount(new BigDecimal(100));
        // set line fields

        // customer = data.get(3);
        // billingAddress = data.get(8);
        // billCity = data.get(11);
        // billState = data.get(12);
        // billZip = data.get(13);
        // shippingAddress = data.get(15);
        // shipCity = data.get(18);
        // shipState = data.get(19);
        // shipZip = data.get(20);
        // privateNote = data.get(22);
        // msg = data.get(23);
        // billEmail = data.get(24);
        // lineItem = data.get(27);
        // lineUoM = data.get(28);
        // lineQty = data.get(29);
        // lineDescription = data.get(30);
        // lineUnitPrice = data.get(31);
        // lineUnitTaxable = "N";

        return lineItem;
    }

    public void createNewInvoice(ArrayList<Line> lineList) {
        Invoice newInvoice = new Invoice();
        finalInvoiceList.add(newInvoice);
    }

    public void flush() {
        lineList.clear();
    }
}

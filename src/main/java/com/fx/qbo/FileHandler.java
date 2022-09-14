package com.fx.qbo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.ipp.data.BatchItemRequest;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.ItemTypeEnum;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.LineDetailTypeEnum;
import com.intuit.ipp.data.ReferenceType;
import com.intuit.ipp.data.SalesItemLineDetail;
import com.intuit.ipp.data.TaxLineDetail;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.serialization.BatchItemRequestSerializer;
import com.intuit.oauth2.exception.OAuthException;

public class FileHandler {

    private String path;
    // private ArrayList<Line> lineList = new ArrayList<Line>();
    private ArrayList<Invoice> finalInvoiceList = new ArrayList<Invoice>();
    private int refNumber;
    private int refPrevious;
    public static APIController api;
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    ArrayList<Line> lineList = new ArrayList<Line>();

    public FileHandler() {

    }

    public FileHandler(String filePath, APIController apiController) {
        path = filePath;
        api = apiController;
    }

    public int formatData() throws OAuthException, FMSException {

        List<Item> items = api.getItemList();

        int invoiceCounter = 0;

        try {

            File inFile = new File(path);
            FileReader input = new FileReader(inFile);
            Scanner in = new Scanner(input);
            in.nextLine();
            ArrayList<String[]> baseItems = new ArrayList<String[]>();
            // Gson gson = new GsonBuilder().setPrettyPrinting().create();

            while (in.hasNext()) {

                String line = in.nextLine();
                String[] split = line.split("\",\"");
                split[0] = split[0].replace("\"", "");
                baseItems.add(split);

            }

            String[] currentArray = baseItems.get(0);
            refNumber = Integer.parseInt(currentArray[0]);
            refPrevious = refNumber;
            invoiceCounter = 1;

            for (int i = 0; i < baseItems.size(); i++) {

                currentArray = baseItems.get(i);
                refNumber = Integer.parseInt(currentArray[0]);

                if (refNumber == refPrevious) {

                    Line newLine = createLineItem(currentArray, items);
                    // System.out.println(i + " " + refNumber + " invoice # " + invoiceCounter);

                    // System.out.println(gson.toJson(newLine));

                    lineList.add(newLine);

                } else {

                    String[] pastArray = baseItems.get(i - 1);
                    createNewInvoices(lineList, pastArray[3]);
                    invoiceCounter++;
                    Line newLine = createLineItem(currentArray, items);
                    lineList.add(newLine);
                    refPrevious = Integer.parseInt(currentArray[0]);

                }
            }

            createNewInvoices(lineList, currentArray[3]);

            System.out.println(gson.toJson(finalInvoiceList));

            api.postInvoices(finalInvoiceList);

            System.out.println("Created " + invoiceCounter + " invoices");

        } catch (FileNotFoundException e) {

            Popup FileNotFoundException = new Popup("FileNotFoundException", "Error: File not found");
            FileNotFoundException.setVisible(true);

        }

        return invoiceCounter;
    }

    public Item itemLocator(List<Item> existingItemsList, String name, double unitPrice) {
        Item csvItem = new Item();
        Item existingItem = new Item();
        Iterator itr = existingItemsList.iterator();
        ReferenceType itemRef = new ReferenceType();
        itemRef.setValue("91");

        while (itr.hasNext()) {

            existingItem = (Item) itr.next();

            if (existingItem.getName().equals(name)) { // check to see if item update is necessary

                if (existingItem.getUnitPrice() == new BigDecimal(unitPrice)) {
                    return existingItem;
                } else {
                    return api.updateItem(existingItem);
                }
            }
        } // if item does not exist in item list, create new one

        csvItem = new Item();
        csvItem.setName(name);
        csvItem.setType(ItemTypeEnum.NON_INVENTORY);
        csvItem.setIncomeAccountRef(itemRef);
        existingItemsList.add(csvItem);

        return api.createNewItem(csvItem);
    }

    public Line createLineItem(String[] data, List<Item> items) {

        Line lineItem = new Line();
        SalesItemLineDetail detail = new SalesItemLineDetail();
        TaxLineDetail tax = new TaxLineDetail();
        ReferenceType ref = new ReferenceType();
        BigDecimal rate;
        String name = data[30];
        Double unitPrice = Double.parseDouble(data[31]);

        Item item = itemLocator(items, name, unitPrice);
        ref.setValue(item.getId());
        ref.setName(item.getName());
        rate = item.getUnitPrice();

        detail.setItemRef(ref);
        detail.setQty(new BigDecimal(Double.parseDouble(data[29])));
        detail.setUnitPrice(new BigDecimal(Double.parseDouble(data[31])));

        lineItem.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);
        lineItem.setSalesItemLineDetail(detail);
        lineItem.setTaxLineDetail(tax);

        BigDecimal totalAmount = new BigDecimal((Double.parseDouble(data[29]) * Double.parseDouble(data[31])));
        lineItem.setAmount(totalAmount);

        // line fields
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

    public void createNewInvoices(ArrayList<Line> lineList, String customerName) throws OAuthException, FMSException {

        ReferenceType ref = new ReferenceType();
        Invoice newInvoice = new Invoice();
        ArrayList<Line> finalLineList = new ArrayList<Line>(lineList);

        Customer customer = api.getCustomer(customerName);
        System.out.println(customerName);
        // if (customerName.equals("Bold Bean Jax Beach") || customerName.equals("Bold
        // Bean Riverside")) {
        // Line discount = new Line();
        // DiscountLineDetail discountRef = new DiscountLineDetail();
        // discountRef.setDiscountPercent(new BigDecimal("15"));
        // discount.setDetailType(LineDetailTypeEnum.DISCOUNT_LINE_DETAIL);
        // discount.setDiscountLineDetail(discountRef);

        // finalLineList.add(discount);
        // }

        Collections.sort(finalLineList, new Comparator<Line>() {
            @Override
            public int compare(Line s1, Line s2) {
                return s1.getSalesItemLineDetail().getUnitPrice().compareTo(s2.getSalesItemLineDetail().getUnitPrice());
            }
        });

        newInvoice.setLine(finalLineList);
        newInvoice.setCustomerRef(ref);
        ref.setValue(customer.getId());
        ref.setName(customerName);

        finalInvoiceList.add(newInvoice);

        lineList.clear();

    }

}

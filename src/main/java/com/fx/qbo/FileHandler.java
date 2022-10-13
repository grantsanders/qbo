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
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.DiscountLineDetail;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.ItemTypeEnum;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.LineDetailTypeEnum;
import com.intuit.ipp.data.MemoRef;
import com.intuit.ipp.data.PhysicalAddress;
import com.intuit.ipp.data.ReferenceType;
import com.intuit.ipp.data.SalesItemLineDetail;
import com.intuit.ipp.data.TaxLineDetail;
import com.intuit.ipp.exception.FMSException;
import com.intuit.oauth2.exception.OAuthException;

public class FileHandler {

    private String path;
    private ArrayList<Invoice> finalInvoiceList = new ArrayList<Invoice>();
    private int refNumber;
    private int refPrevious;
    public static APIController api;
    ArrayList<Line> lineList = new ArrayList<Line>();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public FileHandler() {

    }

    public FileHandler(String filePath, APIController apiController) {
        path = filePath;
        api = apiController;
    }

    public int formatData() throws OAuthException, FMSException {

        List<Item> workingItemList = api.getItemList();
        List<Customer> workingCustomerList = api.getCustomerList();

        System.out.println("\n\n\n");
        System.out.println(workingItemList.size());
        System.out.println("\n\n\n");

        System.out.println(workingCustomerList.size());
        System.out.println("\n\n\n");

        int invoiceCounter = 0;

        try {

            File inFile = new File(path);
            FileReader input = new FileReader(inFile);
            Scanner in = new Scanner(input);

            in.nextLine();
            ArrayList<String[]> baseItems = new ArrayList<String[]>();

            while (in.hasNext()) {

                String line = in.nextLine();

                // while line does not end with "","" continue adding nextLine to line. tis is to account for potential occurrences
                // of newline character within customer memo section

                while (!(line.endsWith("\"\",\"\""))) {
                    line += " " + in.nextLine();
                }

                String[] split = line.split("\",\"");

                split[0] = split[0].replace("\"", "");
                baseItems.add(split);

                for (int i = 0; i < split.length; i++) {
                    System.out.println((i + 1) + split[i]);
                }
            }

            String[] currentArray = baseItems.get(0);
            refNumber = Integer.parseInt(currentArray[0]);
            refPrevious = refNumber;
            invoiceCounter = 1;

            for (int i = 0; i < baseItems.size(); i++) {

                currentArray = baseItems.get(i);
                refNumber = Integer.parseInt(currentArray[0]);

                if (refNumber == refPrevious) {

                    Line newLine = createLineItem(currentArray, workingItemList);

                    lineList.add(newLine);

                } else {

                    String[] pastArray = baseItems.get(i - 1);
                    createNewInvoices(lineList, pastArray, workingCustomerList);
                    invoiceCounter++;
                    Line newLine = createLineItem(currentArray, workingItemList);
                    lineList.add(newLine);
                    refPrevious = Integer.parseInt(currentArray[0]);

                }
            }

            createNewInvoices(lineList, currentArray, workingCustomerList);

            System.out.println(gson.toJson(finalInvoiceList));

            api.postInvoices(finalInvoiceList);

            System.out.println("Created " + invoiceCounter + " invoices");

            in.close();

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
        itemRef.setValue("1");

        while (itr.hasNext()) {

            existingItem = (Item) itr.next();

            if (existingItem.getName().equals(name)) { // check to see if item update is necessary
                System.out.println("Using existing item - existing name = " + existingItem.getName() + " test name = "
                        + name + "\n\n\n");
                existingItem.setUnitPrice(new BigDecimal(unitPrice));
                return existingItem;
            }
        } // if item does not exist in item list, create new object

        csvItem = new Item();

        csvItem.setName(name);
        csvItem.setType(ItemTypeEnum.NON_INVENTORY);

        csvItem.setIncomeAccountRef(itemRef);

        existingItemsList.add(csvItem);

        return api.updateItem(csvItem);
    }

    public Customer customerLocator(List<Customer> workingCustomerList, String name) {

        Customer csvCustomer = new Customer();
        csvCustomer.setDisplayName(name);
        Customer existingCustomer;

        java.util.Iterator itr = workingCustomerList.iterator();

        while (itr.hasNext()) {

            existingCustomer = (Customer) itr.next();

            if (existingCustomer.getDisplayName().equalsIgnoreCase(name)) {

                return existingCustomer;

            }

        } // if customer does not exist in customer list, create new object

        return api.createNewCustomer(csvCustomer);
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

        return lineItem;
    }

    public void createNewInvoices(ArrayList<Line> lineList, String[] customerInfo, List<Customer> workingCustomerList)
            throws OAuthException, FMSException {

        String customerName = customerInfo[3];
        ReferenceType ref = new ReferenceType();
        Invoice newInvoice = new Invoice();
        ArrayList<Line> finalLineList = new ArrayList<Line>(lineList);

        Customer customer = customerLocator(workingCustomerList, customerName);

        PhysicalAddress shipAddr = new PhysicalAddress();
        PhysicalAddress billAddr = new PhysicalAddress();

        shipAddr.setLine1(customerInfo[15]);
        shipAddr.setLine2(customerInfo[16]);
        shipAddr.setLine3(customerInfo[17]);
        shipAddr.setPostalCode(customerInfo[20]);
        shipAddr.setCity(customerInfo[18]);
        shipAddr.setCountrySubDivisionCode(customerInfo[11]);

        EmailAddress emailAddress = new EmailAddress();
        emailAddress.setAddress(customerInfo[24]);

        billAddr.setLine1(customerInfo[8]);
        billAddr.setLine2(customerInfo[9]);
        billAddr.setLine3(customerInfo[10]);
        billAddr.setPostalCode(customerInfo[13]);
        billAddr.setCity(customerInfo[11]);
        billAddr.setCountrySubDivisionCode(customerInfo[12]);

        customer.setBillAddr(billAddr);
        customer.setShipAddr(shipAddr);

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

        Collections.sort(finalLineList, new Comparator<Line>() {
            @Override
            public int compare(Line s1, Line s2) {
                return s1.getSalesItemLineDetail().getUnitPrice().compareTo(s2.getSalesItemLineDetail().getUnitPrice());
            }
        });

        if (customer.getDisplayName().equals("Bold Bean Jax Beach")
                || customer.getDisplayName().equals("Bold Bean Riverside")) {
            Line discount = new Line();
            ReferenceType discountRef = new ReferenceType();
            DiscountLineDetail discountLineDetail = new DiscountLineDetail();
            discountLineDetail.setPercentBased(true);
            discountLineDetail.setDiscountPercent(new BigDecimal(15));
            discount.setDiscountLineDetail(discountLineDetail);
            discount.setDetailType(LineDetailTypeEnum.DISCOUNT_LINE_DETAIL);
            finalLineList.add(discount);

        }
        newInvoice.setLine(finalLineList);
        newInvoice.setCustomerRef(ref);

        MemoRef memo = new MemoRef();
        memo.setValue(customerInfo[23]);

        newInvoice.setCustomerMemo(memo);

        ref.setValue(customer.getId());
        ref.setName(customerName);

        finalInvoiceList.add(newInvoice);

        lineList.clear();

    }

}

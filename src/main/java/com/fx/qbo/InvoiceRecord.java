package com.fx.qbo;

public class InvoiceRecord {

    private String id = "null";
    private boolean importedSuccessfully;
    private String numberOfInvoices;
    private String created = "null";
    private String note = "null";

    public void setNumberOfInvoices(int numberOfInvoices) {
        this.numberOfInvoices = String.valueOf(numberOfInvoices);
    }

    public void setImportedSuccessfully(boolean importedSuccessfully) {
        this.importedSuccessfully = importedSuccessfully;
    }
}

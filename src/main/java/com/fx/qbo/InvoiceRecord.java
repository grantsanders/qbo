package com.fx.qbo;

public class InvoiceRecord {

    private String id = "";
    private boolean importedSuccessfully;
    private String numberOfInvoices = "";
    private String created = "";
    private String note = "";

    public void setNumberOfInvoices(int numberOfInvoices) {
        this.numberOfInvoices = String.valueOf(numberOfInvoices);
    }

    public void setImportedSuccessfully(boolean importedSuccessfully) {
        this.importedSuccessfully = importedSuccessfully;
    }
}

package jp.co.riso.smartdeviceapp.model;

public class Printer {
    
    private int mPrinterId;
    private String mPrinterName;

    public Printer(int mPrinterId, String mPrinterName) {
        super();
        this.mPrinterId = mPrinterId;
        this.mPrinterName = mPrinterName;
    }

    public int getPrinterId() {
        return mPrinterId;
    }
    
    public void setPrinterId(int mPrinterId) {
        this.mPrinterId = mPrinterId;
    }
    
    public String getPrinterName() {
        return mPrinterName;
    }
    
    public void setPrinterName(String mPrinterName) {
        this.mPrinterName = mPrinterName;
    }
}

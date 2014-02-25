/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PDFFileManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.pdf;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.radaee.pdf.Document;
import com.radaee.pdf.Global;
import com.radaee.pdf.Matrix;
import com.radaee.pdf.Page;

public class PDFFileManager {
    public static final String TAG = "PDFFileManager";
    
    private static boolean CONST_KEEP_DOCUMENT_CLOSED = true;
    
    private Document mDocument;
    private String mPath;
    private WeakReference<PDFFileManagerInterface> mInterfaceRef;
    
    PDFInitTask mInitTask = null;
    
    private volatile boolean mIsInitialized;
    private volatile int mPageCount;
    
    public static final int PDF_OK = 0;
    public static final int PDF_ENCRYPTED = -1;
    public static final int PDF_UNKNOWN_ENCRYPTION = -2;
    public static final int PDF_DAMAGED = -3;
    public static final int PDF_INVALID_PATH = -10;
    
    /**
     * Constructor
     * 
     * @param pdfFileManagerInterface
     *            Object to send callbacks
     */
    public PDFFileManager(PDFFileManagerInterface pdfFileManagerInterface) {
        mDocument = new Document();
        mPath = null;
        mInterfaceRef = new WeakReference<PDFFileManagerInterface>(pdfFileManagerInterface);
    }
    
    /**
     * Gets the current path of the pdf
     * 
     * @return Gets the current path.
     */
    public String getPath() {
        return mPath;
    }
    
    /**
     * Sets the path of the PDF
     * 
     * @param path
     *            New path value
     */
    public void setPath(String path) {
        mIsInitialized = false;
        mPath = path;
    }
    
    /**
     * Checks if PDF path is initialized
     * 
     * @return PDF path is initialized
     */
    public boolean isInitialized() {
        return mIsInitialized;
    }
    
    /**
     * Gets the number of pages in the PDF
     * 
     * @return page count
     */
    public int getPageCount() {
        return mPageCount;
    }
    
    /**
     * Initializes the set PDF path asynchronously. <br>
     * Performs the following: <br>
     * 1. Save to file <br>
     * 2. Test if file is openable. <br>
     * 3. Returns status via PDFFileManagerInterface
     */
    public void initializeAsync() {
        mIsInitialized = false;
        mPageCount = 0;
        
        // Cancel any previous initialize task
        if (mInitTask != null) {
            mInitTask.cancel(true);
        }
        
        mInitTask = new PDFInitTask();
        mInitTask.execute();
    }
    
    /**
     * Gets the Bitmap of the page
     * 
     * @param pageNo
     *            PDF page of the requested page
     *            
     * @return Bitmap of the page
     */
    public synchronized Bitmap getPageBitmap(int pageNo) {
        if (!isInitialized()) {
            return null;
        }
        
        if (pageNo < 0 || pageNo >= getPageCount()) {
            return null;
        }
        
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Open(mPath, null);
        } else {
            // For temporary fix of bug
            mDocument.Close(); // This will clear the buffer
            mDocument.Open(mPath, null);
        }
        
        Page page = mDocument.GetPage(pageNo);
        
        float pageWidth = mDocument.GetPageWidth(pageNo);
        float pageHeight = mDocument.GetPageHeight(pageNo);
        
        Matrix mat = new Matrix(1, -1, 0, pageHeight);
        
        Bitmap bitmap = Bitmap.createBitmap((int) pageWidth, (int) pageHeight, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        
        if (!page.RenderToBmp(bitmap, mat)) {
            bitmap.recycle();
            bitmap = null;
        }
        
        mat.Destroy();
        page.Close();
        Global.RemoveTmp();
        
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Close();
        }
        
        return bitmap;
    }

    //================================================================================
    // Protected methods
    //================================================================================

    /**
     * Opens the document
     * 
     * @return status of open <br>
     *         PDF_OK = 0; <br>
     *         PDF_ENCRYPTED = -1; <br>
     *         PDF_UNKNOWN_ENCRYPTION = -2; <br>
     *         PDF_DAMAGED = -3; <br>
     *         PDF_INVALID_PATH = -10;
     */
    protected synchronized int openDocument() {
        if (mDocument.is_opened()) {
            closeDocument();
        }
        
        if (mPath == null || mPath == "") {
            return PDF_INVALID_PATH;
        }
        
        int status = mDocument.Open(mPath, null);
        
        if (status == PDF_OK) {
            mIsInitialized = true;
            mPageCount = mDocument.GetPageCount();
        }
        
        return status;
    }
    
    /**
     * Closes the document
     */
    protected synchronized void closeDocument() {
        if (mDocument.is_opened()) {
            mDocument.Close();
        }
    }
    
    // ================================================================================
    // Internal classes
    // ================================================================================
    
    class PDFInitTask extends AsyncTask<Void, Void, Integer> {
        
        /**
         * Save the document in sandbox and test if openable
         */
        @Override
        protected Integer doInBackground(Void... params) {
            int status = openDocument();
            
            if (CONST_KEEP_DOCUMENT_CLOSED) {
                closeDocument();
            }
            
            return status;
        }
        
        /**
         * Delegate the result
         */
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            
            if (mInterfaceRef != null && mInterfaceRef.get() != null) {
                mInterfaceRef.get().onFileInitialized(result);
            }
        }
    }
}

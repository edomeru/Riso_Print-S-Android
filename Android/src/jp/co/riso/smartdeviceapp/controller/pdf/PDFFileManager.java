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

    public PDFFileManager(PDFFileManagerInterface pdfFileManagerInterface) {
        mDocument = new Document();
        mPath = null;
        mInterfaceRef = new WeakReference<PDFFileManagerInterface>(pdfFileManagerInterface);
    }
    
    public String getPath() {
        return mPath;
    }
    
    public void setPath(String path) {
        mIsInitialized = false;
        mPath = path;
    }
    
    public void openAsync() {
        mIsInitialized = false;
        mPageCount = 0;
        
        if (mInitTask != null) {
            mInitTask.cancel(true);            
        }

        mInitTask = new PDFInitTask();
        mInitTask.execute();
    }
    
    public boolean isInitialized() {
        return mIsInitialized;
    }
    
    public int getPageCount() {
        return mPageCount;
    }
    
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
        }
        
        Page page = mDocument.GetPage(pageNo);
        
        float pageWidth = mDocument.GetPageWidth(pageNo);
        float pageHeight = mDocument.GetPageHeight(pageNo);
        
        Matrix mat = new Matrix(1, -1, 0, pageHeight);
        
        Bitmap bitmap = Bitmap.createBitmap((int)pageWidth, (int)pageHeight, Bitmap.Config.ARGB_8888);
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
    
    protected synchronized void closeDocument() {
        if (mDocument.is_opened()) {
            mDocument.Close();
        }
    }

    //================================================================================
    // Internal classes
    //================================================================================

    class PDFInitTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            int status = openDocument();
            
            if (CONST_KEEP_DOCUMENT_CLOSED) {
                closeDocument();
            }
            
            return status;
        }
        
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            
            if (mInterfaceRef != null && mInterfaceRef.get() != null) {
                mInterfaceRef.get().onFileInitialized(result);
            }
        }
    }
}

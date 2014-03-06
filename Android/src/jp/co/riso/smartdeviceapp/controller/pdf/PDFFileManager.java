/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PDFFileManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.pdf;

import java.io.File;
import java.lang.ref.WeakReference;

import jp.co.riso.android.util.FileUtils;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.radaee.pdf.Document;
import com.radaee.pdf.Global;
import com.radaee.pdf.Matrix;
import com.radaee.pdf.Page;

public class PDFFileManager {
    public static final String TAG = "PDFFileManager";
    
    // For design
    private static boolean CONST_KEEP_DOCUMENT_CLOSED = true;
    
    public static String KEY_NEW_PDF_DATA = "new_pdf_data";
    
    private Document mDocument;
    private String mPath;
    private String mFileName;
    private String mSandboxPath;
    private WeakReference<PDFFileManagerInterface> mInterfaceRef;
    
    PDFInitTask mInitTask = null;
    
    private volatile boolean mIsInitialized;
    private volatile int mPageCount;
    private volatile float mPageWidth;
    private volatile float mPageHeight;
    
    public static final int PDF_OK = 0;
    public static final int PDF_ENCRYPTED = -1;
    public static final int PDF_UNKNOWN_ENCRYPTION = -2;
    public static final int PDF_DAMAGED = -3;
    public static final int PDF_INVALID_PATH = -10;
    public static final int UNKNOWN_ERROR = -100;
    
    /**
     * Constructor
     * 
     * @param pdfFileManagerInterface
     *            Object to send callbacks
     */
    public PDFFileManager(PDFFileManagerInterface pdfFileManagerInterface) {
        mDocument = new Document();
        mInterfaceRef = new WeakReference<PDFFileManagerInterface>(pdfFileManagerInterface);
        setPDF(null);
    }
    
    /**
     * Gets the current path of the pdf
     * 
     * @return Path from the file system.
     */
    public String getPath() {
        return mPath;
    }
    
    /**
     * Gets the current filename of the pdf
     * 
     * @return Filename of the PDF.
     */
    public String getFileName() {
        return mFileName;
    }
    
    /**
     * Gets the path of the pdf in the app sandbox
     * 
     * @return Path from the app sandbax.
     */
    public String getSandboxPath() {
        return mSandboxPath;
    }
    
    /**
     * Sets the path of the PDF
     * 
     * @param path
     *            New path value
     */
    public void setPDF(String path) {
        mIsInitialized = false;
        
        if (path == null) {
            mPath = null;
            mFileName = null;
            mSandboxPath = null;
            
            return;
        }
        
        if (SmartDeviceApp.getAppContext() == null) {
            return;
        }
        
        File file = new File(path);
        
        mPath = path;
        mFileName = file.getName();
        mSandboxPath = SmartDeviceApp.getAppContext().getExternalFilesDir("pdfs") + "/" + file.getName();
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
     * Gets the page width
     * 
     * @return page width
     */
    public float getPageWidth() {
        return mPageWidth;
    }
    
    /**
     * Gets the page height
     * 
     * @return page height
     */
    public float getPageHeight() {
        return mPageHeight;
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
        
        float scale = 1.0f;
        Page page = mDocument.GetPage(pageNo);
        
        float pageWidth = mDocument.GetPageWidth(pageNo) * scale;
        float pageHeight = mDocument.GetPageHeight(pageNo) * scale;
        
        float x0 = 0;
        float y0 = pageHeight;
        
        Matrix mat = new Matrix(scale, -scale, x0, y0);
        
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
    
    // ================================================================================
    // Protected methods
    // ================================================================================
    
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
        
        if (mSandboxPath == null || mSandboxPath == "") {
            return PDF_INVALID_PATH;
        }
        
        int status = mDocument.Open(mSandboxPath, null);
        
        if (status == PDF_OK) {
            mIsInitialized = true;
            mPageCount = mDocument.GetPageCount();
            mPageWidth = mDocument.GetPageWidth(0);
            mPageHeight = mDocument.GetPageHeight(0);
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
            if (SmartDeviceApp.getAppContext() == null) {
                return UNKNOWN_ERROR;
            }
            
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
            if (prefs.getBoolean(KEY_NEW_PDF_DATA, false)) {
                
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean(KEY_NEW_PDF_DATA, false);
                edit.commit();
                
                try {
                    FileUtils.copy(new File(mPath), new File(mSandboxPath));
                } catch (Exception e) {
                    return PDF_INVALID_PATH;
                }
            }
            
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

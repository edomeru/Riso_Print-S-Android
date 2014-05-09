/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
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
import com.radaee.pdf.Matrix;
import com.radaee.pdf.Page;

public class PDFFileManager {
    public static final String TAG = "PDFFileManager";
    
    public static final String KEY_NEW_PDF_DATA = "new_pdf_data";
    
    public static final int PDF_OK = 0;
    public static final int PDF_ENCRYPTED = -1;
    public static final int PDF_PRINT_RESTRICTED = -2;
    public static final int PDF_OPEN_FAILED = -3;
    
    private static final int RADAEE_OK = 0;
    private static final int RADAEE_ENCRYPTED = -1;
    private static final int RADAEE_UNKNOWN_ENCRYPTION = -2;
    @SuppressWarnings("unused") // Radaee error with general error handling
    private static final int RADAEE_DAMAGED = -3;
    @SuppressWarnings("unused") // Radaee error with general error handling
    private static final int RADAEE_INVALID_PATH = -10;
    
    // For design consideration
    private static final boolean CONST_KEEP_DOCUMENT_CLOSED = false;
    
    private Document mDocument;
    private String mPath;
    private String mFileName;
    private String mSandboxPath;
    private WeakReference<PDFFileManagerInterface> mInterfaceRef;
    
    private PDFInitTask mInitTask = null;
    
    private volatile boolean mIsInitialized;
    private volatile int mPageCount;
    private volatile float mPageWidth;
    private volatile float mPageHeight;
    
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
        
        if (mPath.equalsIgnoreCase(mSandboxPath)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(KEY_NEW_PDF_DATA, false);
            edit.apply();
        }
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
        return getPageBitmap(pageNo, 1.0f, false, false);
    }
    
    /**
     * Gets the Bitmap of the page
     * 
     * @param pageNo
     *            PDF page of the requested page
     * 
     * @return Bitmap of the page
     */
    public synchronized Bitmap getPageBitmap(int pageNo, float scale, boolean flipX, boolean flipY) {
        if (!isInitialized()) {
            return null;
        }
        
        if (pageNo < 0 || pageNo >= getPageCount()) {
            return null;
        }
        
        if (CONST_KEEP_DOCUMENT_CLOSED) {
            mDocument.Open(mSandboxPath, null);
        } else {
            // TODO: re-check: For temporary fix of bug
            // mDocument.Close(); // This will clear the buffer
            // mDocument.Open(mSandboxPath, null);
        }
        
        Page page = mDocument.GetPage(pageNo);
        
        float pageWidth = mDocument.GetPageWidth(pageNo) * scale;
        float pageHeight = mDocument.GetPageHeight(pageNo) * scale;
        
        float x0 = 0;
        float y0 = pageHeight;
        
        if (flipX) {
            x0 = pageWidth;
        }
        if (flipY) {
            y0 = 0;
        }
        
        float scaleX = scale;
        float scaleY = -scale;
        
        if (flipX) {
            scaleX = -scaleX;
        }
        if (flipY) {
            scaleY = -scaleY;
        }
        
        Bitmap bitmap = Bitmap.createBitmap((int) pageWidth, (int) pageHeight, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        
        Matrix mat = new Matrix(scaleX, scaleY, x0, y0);
        
        if (!page.RenderToBmp(bitmap, mat)) {
            bitmap.recycle();
            bitmap = null;
        }
        
        mat.Destroy();
        page.Close();
        
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
        
        if (mPath == null || mPath.length() == 0) {
            return PDF_OPEN_FAILED;
        }
        
        int status = mDocument.Open(mPath, null);
        
        switch (status) {
            case RADAEE_OK:
                int permission = mDocument.GetPermission();
                // check if (permission != 0) means that license is not standard. if standard license, just display.
                if (permission != 0) {
                    if ((permission & 0x4) == 0) {
                        mDocument.Close();
                        return PDF_PRINT_RESTRICTED;
                    }
                }
                
                mIsInitialized = true;
                mPageCount = mDocument.GetPageCount();
                mPageWidth = mDocument.GetPageWidth(0);
                mPageHeight = mDocument.GetPageHeight(0);
                return PDF_OK;
            case RADAEE_ENCRYPTED:
            case RADAEE_UNKNOWN_ENCRYPTION:
                return PDF_ENCRYPTED;
            default:
                return PDF_OPEN_FAILED;
        }
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
                return PDF_OPEN_FAILED;
            }
            
            int status = openDocument();
            
            if (CONST_KEEP_DOCUMENT_CLOSED) {
                closeDocument();
            }
            
            if (status == PDF_OK) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
                if (prefs.getBoolean(KEY_NEW_PDF_DATA, false)) {
                    
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putBoolean(KEY_NEW_PDF_DATA, false);
                    edit.apply();
                    
                    try {
                        FileUtils.copy(new File(mPath), new File(mSandboxPath));
                    } catch (Exception e) {
                        return PDF_OPEN_FAILED;
                    }
                }
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

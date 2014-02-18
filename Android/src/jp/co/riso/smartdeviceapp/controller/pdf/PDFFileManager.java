/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PDFFileManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.pdf;

import java.lang.ref.WeakReference;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.LruCache;

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
    
    PDFOpenTask mOpenTask = null;
    PDFPreBufferTask mPreBufferTask = null;
    
    private volatile boolean mIsOpenable;
    private volatile int mPageCount;
    
    private LruCache<String, Bitmap> bmpCache;
    
    public static final int PDF_OK = 0;
    public static final int PDF_ENCRYPTED = -1;
    public static final int PDF_UNKNOWN_ENCRYPTION = -2;
    public static final int PDF_DAMAGED = -3;
    public static final int PDF_INVALID_PATH = -10;

    public PDFFileManager(PDFFileManagerInterface pdfFileManagerInterface) {
        mDocument = new Document();
        mPath = null;
        mInterfaceRef = new WeakReference<PDFFileManagerInterface>(pdfFileManagerInterface);
        
        int cacheSize = 16 * 1024 * 1024; // 4MiB
        bmpCache = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }
    
    public String getPath() {
        return mPath;
    }
    
    public void setPath(String path) {
        mIsOpenable = false;
        mPath = path;
    }
    
    public void preBuffer() {
        if (mPreBufferTask != null) {
            mPreBufferTask.cancel(true);            
        }

        mPreBufferTask = new PDFPreBufferTask();
        mPreBufferTask.execute();
    }
    
    public void openAsync() {
        mIsOpenable = false;
        mPageCount = 0;
        
        if (mOpenTask != null) {
            mOpenTask.cancel(true);            
        }

        mOpenTask = new PDFOpenTask();
        mOpenTask.execute();
    }
    
    public boolean isOpenable() {
        return mIsOpenable;
    }
    
    public int getPageCount() {
        return mPageCount;
    }
    
    public synchronized Bitmap getPageBitmap(int pageNo) {
        if (!isOpenable()) {
            return null;
        }
        
        if (pageNo < 0 || pageNo >= getPageCount()) {
            return null;
        }
        
        String key = String.format(Locale.getDefault(), "%04d%s", pageNo, mPath);
        
        Bitmap bitmap = bmpCache.get(key);
        
        if (bitmap == null) {
            if (CONST_KEEP_DOCUMENT_CLOSED) {
                mDocument.Open(mPath, null);
            } else {
                // For temporary fix of bug
            }
            
            Page page = mDocument.GetPage(pageNo);
            
            float pageWidth = mDocument.GetPageWidth(pageNo);
            float pageHeight = mDocument.GetPageHeight(pageNo);
            
            Matrix mat = new Matrix(1, -1, 0, pageHeight);
            
            bitmap = Bitmap.createBitmap((int)pageWidth, (int)pageHeight, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.WHITE);
            
            if (page.RenderToBmp(bitmap, mat)) {
                bmpCache.put(key, bitmap);
            } else {
                bitmap.recycle();
                bitmap = null;
            }
            /*
            int dib = Global.dibGet(0, (int)pageWidth, (int)pageHeight);
            
            page.Render_Normal(dib, mat);
            
            Global.dibFree(dib);
            */
            mat.Destroy();
            
            page.Close();
            Global.RemoveTmp();
            
            if (CONST_KEEP_DOCUMENT_CLOSED) {
                mDocument.Close();
            }
        } else {
            //saveBitmapToCache(bitmap, pageNo);
            bmpCache.put(key, bitmap);
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
            mIsOpenable = true;
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

    class PDFOpenTask extends AsyncTask<Void, Void, Integer> {

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
                mInterfaceRef.get().onFileOpenedResult(result);
            }
        }
    }

    class PDFPreBufferTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            getPageBitmap(0);
            getPageBitmap(1);
            
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            
            if (mInterfaceRef != null && mInterfaceRef.get() != null) {
                mInterfaceRef.get().onPreBufferFinished();
            }
        }
    }
    
}

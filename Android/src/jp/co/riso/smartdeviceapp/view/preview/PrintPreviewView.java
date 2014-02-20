/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PrintPreviewView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.preview;

import java.lang.ref.WeakReference;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.model.PrintSettings;
import fi.harism.curl.CurlPage;
import fi.harism.curl.CurlView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class PrintPreviewView extends ViewGroup {
    public static final String TAG = "PrintPreviewView"; 
    
    CurlView mCurlView;
    PDFFileManager mPdfManager = null;
    PrintSettings mPrintSettings = new PrintSettings(); // Should not be null
    
    public PrintPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initializeCurlView();
    }

    public PrintPreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrintPreviewView(Context context) {
        super(context);

        initializeCurlView();
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //compute of mCurlView here
        fitCurlView(l, t, r, b);
    }
    
    // ================================================================================
    // Public methods
    // ================================================================================
    
    public CurlView getCurlView() {
        return mCurlView;
    }
    
    public void refreshCurlView() {
        invalidate();
    }
    
    public void setPdfManager(PDFFileManager pdfManager) {
        mPdfManager = pdfManager;
    }
    
    public void setPrintSettings(PrintSettings printSettings) {
        mPrintSettings = printSettings;
    }
    
    public int getCurrentPage() {
        return mCurlView.getCurrentIndex();
    }
    
    public void setCurrentPage(int page) {
        mCurlView.setCurrentIndex(page);
    }
    
    // ================================================================================
    // Protected methods
    // ================================================================================
    
    protected int[] getFitToAspectRatioSize(float srcWidth, float srcHeight, int destWidth, int destHeight) {
        float ratioSrc = srcWidth / srcHeight;
        float ratioDest = (float)destWidth / destHeight;

        int newWidth = 0;
        int newHeight = 0;
        
        if (ratioDest > ratioSrc) {
            newHeight = destHeight;
            newWidth = (int)(destHeight * ratioSrc);
            
        } else {
            newWidth = destWidth;
            newHeight = (int)(destWidth / ratioSrc);
        }
        
        return new int[]{newWidth, newHeight};
    }
    
    // ================================================================================
    // Private methods
    // ================================================================================
    
    private boolean shouldDisplayLandscape() {
        // will depend on PDF and pagination, always false for now
        return false;
    }
    
    private float getPaperDisplayWidth() {
        float width = mPrintSettings.getPaperSize().getWidth();
        
        if (shouldDisplayLandscape()) {
            width = mPrintSettings.getPaperSize().getHeight();
        }
        
        return width;
    }
    
    private float getPaperDisplayHeight() {
        float height = mPrintSettings.getPaperSize().getHeight();
        
        if (shouldDisplayLandscape()) {
            height = mPrintSettings.getPaperSize().getWidth();
        }
        
        return height;
    }
    
    private void initializeCurlView() {
        mCurlView = new CurlView(getContext());
        mCurlView.setMargins(0, 0, 0, 0);
        mCurlView.setPageProvider(new PDFPageProvider());
        mCurlView.setViewMode(CurlView.SHOW_ONE_PAGE);
        mCurlView.setAllowLastPageCurl(false);
        mCurlView.setBackgroundColor(getResources().getColor(R.color.theme_light_2));
        mCurlView.setRenderLeftPage(false);
        
        addView(mCurlView);
    }
    
    private void fitCurlView(int l, int t, int r, int b) {
        mCurlView.layout(l, t, r, b);
        
        int w = r - l;
        int h = b - t;

        // Compute margins based on the paper size in preview settings.
        float paperWidth = getPaperDisplayWidth();
        float paperHeight = getPaperDisplayHeight();

        if (mPrintSettings.getBookletBinding() == PrintSettings.BookletBinding.FOLD_AND_STAPLE
                || mPrintSettings.isDuplex()) {
            paperWidth *= 2.0f;
        }
        
        int newDimensions[] = getFitToAspectRatioSize(paperWidth, paperHeight, w, h);
        
        float lrMargin  = ((w - newDimensions[0]) / (w * 2.0f));
        float tbMargin  = ((h - newDimensions[1]) / (h * 2.0f));
        
        mCurlView.setMargins(lrMargin, tbMargin, lrMargin, tbMargin);
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    private class PDFPageProvider implements CurlView.PageProvider {
        
        @Override
        public int getPageCount() {
            if (mPdfManager == null) {
                return 0;
            }
            
            return mPdfManager.getPageCount();
        }
        
        @Override
        public void updatePage(CurlPage page, int width, int height, int index) {
            Bitmap front = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            front.eraseColor(Color.WHITE);
            
            Bitmap back = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            back.eraseColor(Color.WHITE);

            page.setTexture(front, CurlPage.SIDE_FRONT);
            page.setTexture(back, CurlPage.SIDE_BACK);
            
            new PDFRenderTask(page, width, height, index, page.createNewHandler()).execute();
        }
    }

    private class PDFRenderTask extends AsyncTask<Void, Void, Void> {
        WeakReference<CurlPage> mCurlPageRef;
        WeakReference<Object> mHandlerRef;
        int mWidth;
        int mHeight;
        int mIndex;
        
        private Bitmap mFrontBmp;
        private Bitmap mBackBmp;
        
        public PDFRenderTask(CurlPage page, int width, int height, int index, Object handler) {
            mCurlPageRef = new WeakReference<CurlPage>(page);
            mHandlerRef = new WeakReference<Object>(handler);
            mWidth = width;
            mHeight = height;
            mIndex = index;
            
            mFrontBmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mFrontBmp.eraseColor(Color.WHITE);
            
            mBackBmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mBackBmp.eraseColor(Color.WHITE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Bitmap bmp = mPdfManager.getPageBitmap(mIndex);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (bmp != null) {
                Canvas canvas = new Canvas(mFrontBmp);
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                canvas.drawBitmap(bmp, new Rect(0, 0, bmp.getWidth(), bmp.getHeight()), new Rect(0, 0, mFrontBmp.getWidth(), mFrontBmp.getHeight()), paint);
            } else {
            }
            
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            if (mHandlerRef.get() != null && mCurlPageRef.get() != null) {
                mCurlPageRef.get().reset();
                mCurlPageRef.get().setTexture(mFrontBmp, CurlPage.SIDE_FRONT);
                mCurlPageRef.get().setTexture(mBackBmp, CurlPage.SIDE_BACK);
                mCurlView.requestRender();
            } else {
                mFrontBmp.recycle();
                mBackBmp.recycle();
            }
            
        }
    }
}

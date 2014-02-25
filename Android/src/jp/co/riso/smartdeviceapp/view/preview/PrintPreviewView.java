/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PrintPreviewView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.preview;

import java.lang.ref.WeakReference;
import java.util.Locale;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.model.PrintSettings;
import fi.harism.curl.CurlPage;
import fi.harism.curl.CurlView;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PrintPreviewView extends FrameLayout implements OnSeekBarChangeListener {
    public static final String TAG = "PrintPreviewView"; 
    
    CurlView mCurlView;
    PDFFileManager mPdfManager = null;
    PDFPageProvider mPdfPageProvider = new PDFPageProvider();
    PrintSettings mPrintSettings = new PrintSettings(); // Should not be null
    LruCache<String, Bitmap> mBmpCache = null;
    
    LinearLayout mPageControlLayout;
    SeekBar mSeekBar;
    TextView mPageLabel;
    
    private static final String FORMAT_CACHE_KEY = "%s-%d-%d"; // path; page; side
    private static final String FORMAT_PAGE_STATUS = "PAGE %d / %d";
    
    public PrintPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initializeCurlView();
        initializePageControls();
    }

    public PrintPreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrintPreviewView(Context context) {
        super(context);

        initializeCurlView();
        initializePageControls();
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        
        fitCurlView(l, t, r, b);
    }
    
    // ================================================================================
    // Public methods
    // ================================================================================
    
    public CurlView getCurlView() {
        return mCurlView;
    }
    
    public void refreshView() {
        invalidate();
        
        updateSeekBar();
        updatePageLabel();
    }
    
    public void setPdfManager(PDFFileManager pdfManager) {
        mPdfManager = pdfManager;
        
        updateSeekBar();
        updatePageLabel();
    }
    
    public void setPrintSettings(PrintSettings printSettings) {
        mPrintSettings = printSettings;
    }
    
    public void setBmpCache(LruCache<String, Bitmap> bmpCache) {
        mBmpCache = bmpCache;
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
    
    protected int[] getPageDimensions(int screenWidth, int screenHeight) {
        // Compute margins based on the paper size in preview settings.
        float paperWidth = getPaperDisplayWidth();
        float paperHeight = getPaperDisplayHeight();

        if (mPrintSettings.getBookletBinding() == PrintSettings.BookletBinding.FOLD_AND_STAPLE
                || mPrintSettings.isDuplex()) {
            paperWidth *= 2.0f;
        }
        
        return getFitToAspectRatioSize(paperWidth, paperHeight, screenWidth, screenHeight);
    }
    
    protected String getCacheKey(int index, int side) {
        return String.format(Locale.getDefault(), FORMAT_CACHE_KEY, mPdfManager.getPath(), index, side);
    }
    
    protected Bitmap[] getBitmapsFromCacheForPage(int index) {
        Bitmap front = null;
        Bitmap back = null;
        
        if (mBmpCache != null) {
            front = mBmpCache.get(getCacheKey(index, CurlPage.SIDE_FRONT));
            back = mBmpCache.get(getCacheKey(index, CurlPage.SIDE_BACK));
        }
        
        return new Bitmap[] {front, back};
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
        mCurlView.setPageProvider(mPdfPageProvider);
        mCurlView.setViewMode(CurlView.SHOW_ONE_PAGE);
        mCurlView.setAllowLastPageCurl(false);
        mCurlView.setBackgroundColor(getResources().getColor(R.color.theme_light_2));
        mCurlView.setRenderLeftPage(false);
        
        addView(mCurlView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }
    
    private void initializePageControls() {
        mSeekBar = new SeekBar(getContext());
        mSeekBar.setOnSeekBarChangeListener(this);
        
        mPageLabel = new TextView(getContext());
        mPageLabel.setTextColor(getResources().getColor(R.color.theme_dark_1));
        mPageLabel.setGravity(Gravity.CENTER);
        
        updateSeekBar();
        updatePageLabel();
        
        mPageControlLayout = new LinearLayout(getContext());
        
        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        
        if (isLandscape && !isTablet) {
            mPageControlLayout.setOrientation(LinearLayout.HORIZONTAL);
            
            int width = getResources().getDimensionPixelSize(R.dimen.preview_controls_text_width);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
            params.weight = 0.0f;
            params.gravity = Gravity.CENTER_VERTICAL;
            mPageControlLayout.addView(mPageLabel, params);
            
            params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;
            params.weight = 1.0f;
            mPageControlLayout.addView(mSeekBar, params);
        } else {
            
            mPageControlLayout.setOrientation(LinearLayout.VERTICAL);
            mPageControlLayout.addView(mSeekBar, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            mPageControlLayout.addView(mPageLabel, params);
        }

        FrameLayout.LayoutParams curlViewParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        curlViewParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.preview_controls_margin_side);
        curlViewParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.preview_controls_margin_side);
        curlViewParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.preview_controls_margin_bottom);
        curlViewParams.gravity = Gravity.BOTTOM;
        addView(mPageControlLayout, curlViewParams);
    }
    
    private void updateSeekBar() {
        int currentPage = getCurrentPage();
        int pageCount = mPdfPageProvider.getPageCount();
        
        mSeekBar.setMax(0);
        mSeekBar.setMax(pageCount);
        mSeekBar.setProgress(currentPage);
    }
    
    private void updatePageLabel() {
        int currentPage = getCurrentPage() + 1;
        int pageCount = mPdfPageProvider.getPageCount();
        mPageLabel.setText(String.format(Locale.getDefault(), FORMAT_PAGE_STATUS, currentPage, pageCount));
    }
    
    private void fitCurlView(int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;
        
        int marginSize = getResources().getDimensionPixelSize(R.dimen.preview_view_margin);
        
        MarginLayoutParams params = (MarginLayoutParams) mPageControlLayout.getLayoutParams();
        int pageControlSize = mPageControlLayout.getHeight() + params.bottomMargin;
        
        int newDimensions[] = getPageDimensions(w - (marginSize * 2), h - (marginSize * 2) - pageControlSize);
        
        float lrMargin = ((w - newDimensions[0]) / (w * 2.0f));
        float tbMargin = (((h - pageControlSize) - newDimensions[1]) / (h * 2.0f));
        
        lrMargin += (marginSize / (float)w);
        tbMargin += (marginSize / (float)h);
        
        mCurlView.setMargins(lrMargin, tbMargin, lrMargin, tbMargin + (pageControlSize / (float)h));
    }
    
    private static void drawBmpOnBmp(Bitmap target, Bitmap dest) {
        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        canvas.drawBitmap(target, new Rect(0, 0, target.getWidth(), target.getHeight()), new Rect(0, 0, dest.getWidth(), dest.getHeight()), paint);
    }

    // ================================================================================
    // INTERFACE - OnSeekBarChangeListener
    // ================================================================================
    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mCurlView.setCurrentIndex(progress);
            updatePageLabel();
        }
    }
    
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
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
            Bitmap cachedPage[] = getBitmapsFromCacheForPage(index);
            
            Bitmap front = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            front.eraseColor(Color.WHITE);
            
            Bitmap back = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            back.eraseColor(Color.WHITE);
            
            if (cachedPage[0] != null) {
                PrintPreviewView.drawBmpOnBmp(cachedPage[0], front);
            }
            if (cachedPage[1] != null) {
                PrintPreviewView.drawBmpOnBmp(cachedPage[1], front);
            }
            
            page.setTexture(front, CurlPage.SIDE_FRONT);
            page.setTexture(back, CurlPage.SIDE_BACK);
            
            if (cachedPage[0] == null || cachedPage[1] == null) {
                new PDFRenderTask(page, width, height, index, page.createNewHandler()).execute();
            }
        }

        @Override
        public void indexChanged(int index)
        {
            updateSeekBar();
            
            Handler mainHandler = new Handler(getContext().getMainLooper());

            Runnable myRunnable = new Runnable() {
                
                @Override
                public void run() {
                    updatePageLabel();
                }
            };
            mainHandler.post(myRunnable);
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
            Bitmap front = mPdfManager.getPageBitmap(mIndex);
            Bitmap back = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            
            if (mBmpCache != null) {
                mBmpCache.put(getCacheKey(mIndex, CurlPage.SIDE_FRONT), front);
                mBmpCache.put(getCacheKey(mIndex, CurlPage.SIDE_BACK), back);
            }

            PrintPreviewView.drawBmpOnBmp(front, mFrontBmp);
            
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

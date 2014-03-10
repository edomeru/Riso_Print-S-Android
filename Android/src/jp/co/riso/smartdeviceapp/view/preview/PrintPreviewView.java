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

import jp.co.riso.android.util.ImageUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.model.PrintSettings;
import jp.co.riso.smartdeviceapp.model.PrintSettings.ColorMode;
import fi.harism.curl.CurlPage;
import fi.harism.curl.CurlView;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.os.AsyncTask;
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
    
    private static final String FORMAT_CACHE_KEY = "%s-%d-%d-%d-%d"; // path; page; side
    
    private static Bitmap.Config BMP_CONFIG_TEXTURE = Config.ARGB_8888;
    
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
        
        // Re-adjust the margin of the CurlView
        fitCurlView(l, t, r, b);
    }
    
    public void onPause() {
        mCurlView.onPause();
    }
    
    public void onResume() {
        mCurlView.onResume();
    }
    
    // ================================================================================
    // Public methods
    // ================================================================================
    
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
        
        setupCurlPageView();
        setupCurlBind();
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
        
        if (mCurlView.getViewMode() == CurlView.SHOW_TWO_PAGES) {
            if (mCurlView.getBindPosition() == CurlView.BIND_TOP) {
                paperHeight *= 2.0f;
            } else {
                paperWidth *= 2.0f;
            }
        }
        
        return getFitToAspectRatioSize(paperWidth, paperHeight, screenWidth, screenHeight);
    }
    
    protected String getCacheKey(int index, int width, int height, int side) {
        return String.format(Locale.getDefault(), FORMAT_CACHE_KEY, mPdfManager.getPath(), index, width, height, side);
    }
    
    protected Bitmap[] getBitmapsFromCacheForPage(int index, int width, int height) {
        Bitmap front = null;
        Bitmap back = null;
        
        if (mBmpCache != null) {
            front = mBmpCache.get(getCacheKey(index, width, height, CurlPage.SIDE_FRONT));
            back = mBmpCache.get(getCacheKey(index, width, height, CurlPage.SIDE_BACK));
        }
        
        return new Bitmap[] {front, back};
    }
    
    // ================================================================================
    // PDF page methods
    // ================================================================================

    private int getPageCount() {
        if (mPdfManager == null) {
            return 0;
        }
        
        // will depend on PDF and pagination, always false for now
        int count = mPdfManager.getPageCount();
        
        if (mPrintSettings.isDuplex()) {
            count = (int) Math.ceil(count / 2.0f);
        }
        
        count = (int) Math.ceil(count / (double) mPrintSettings.getPagination().getPerPage());
        
        return count;
    }
    
    private boolean shouldDisplayLandscape() {
        if (mPdfManager == null) {
            return false;
        }
        
        // will depend on PDF and pagination, always false for now
        float pdfWidth = mPdfManager.getPageWidth();
        float pdfHeight = mPdfManager.getPageHeight();
        
        boolean flipToLandscape = (pdfWidth > pdfHeight);
        
        if (mPrintSettings.getPagination().isFlipLandscape()) {
            flipToLandscape = !flipToLandscape;
        }
        
        return flipToLandscape;
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
    
    private void setupCurlPageView() {
        boolean twoPage = false;
        boolean allowLastPageCurl = false;

        if (mPrintSettings.isDuplex()) {
            twoPage = true;

            if (getCurrentPage() % 2 == 0) {
                allowLastPageCurl = true;
            }
        }
        
        if (twoPage) {
            mCurlView.setViewMode(CurlView.SHOW_TWO_PAGES);
            mCurlView.setRenderLeftPage(true);
        } else {
            mCurlView.setViewMode(CurlView.SHOW_ONE_PAGE);
            mCurlView.setRenderLeftPage(false);
        }
        mCurlView.setAllowLastPageCurl(allowLastPageCurl);
    }
    
    private void setupCurlBind() {
        int bindPosition = CurlView.BIND_LEFT;

        switch (mPrintSettings.getBind()) {
            case LEFT:
                bindPosition = CurlView.BIND_LEFT;
                break;
            case RIGHT:
                bindPosition = CurlView.BIND_RIGHT;
                break;
            case TOP:
                bindPosition = CurlView.BIND_TOP;
                break;
        }
        
        mCurlView.setBindPosition(bindPosition);
    }
    
    private boolean shouldDisplayColor() {
        return (mPrintSettings.getColorMode() != ColorMode.MONOCHROME);
    }
    
    private void drawPDFPagesOnBitmap(Bitmap bmp, int beginIndex, boolean flipX, boolean flipY) {
        //get page then draw in bitmap
        PrintSettings.Pagination pagination = mPrintSettings.getPagination();
        
        int width = bmp.getWidth() / pagination.getCols();
        int height = bmp.getHeight() / pagination.getRows();
        
        int beginX = 0;
        int beginY = 0;
        
        switch (mCurlView.getBindPosition()) {
            case CurlView.BIND_LEFT:
                beginX = bmp.getWidth() - (pagination.getCols() * width);
                break;
            case CurlView.BIND_TOP:
                beginY = bmp.getHeight() - (pagination.getRows() * height);
                break;
        }
        
        int curX = beginX;
        int curY = beginY;
        
        for (int i = 0; i < pagination.getPerPage(); i++) {
            
            int left = curX;
            int top = curY;
            int right = curX + width;
            int bottom = curY + height;

            // Left to right
            curX += width;
            if (i % pagination.getCols() == pagination.getCols() - 1) {
                curX = beginX;
                curY += height;
            }
            
            int dim[] = getFitToAspectRatioSize(mPdfManager.getPageWidth(), mPdfManager.getPageHeight(), right - left, bottom - top);
            int x = left + ((right - left) - dim[0]) / 2;
            int y = top + ((bottom - top) - dim[1]) / 2;

            Rect destRect = new Rect(x, y, x + dim[0], y + dim[1]);
            
            float scale = 1.0f / pagination.getPerPage();
            
            Bitmap page = mPdfManager.getPageBitmap(i + beginIndex, scale, flipX, flipY);
            
            if (page != null) {
                ImageUtils.renderBmpToBmp(page, bmp, true, destRect);
                page.recycle();
            }
            
        }
        
        
        //TODO: draw page directly in bitmap
    }
    
    private Bitmap[] getRenderBitmaps(int index, int width, int height) {
        
        Bitmap front = Bitmap.createBitmap(width, height, BMP_CONFIG_TEXTURE);
        Bitmap back = Bitmap.createBitmap(width, height, BMP_CONFIG_TEXTURE);
        
        int pagePerScreen = 1;
        if (mCurlView.getViewMode() == CurlView.SHOW_TWO_PAGES) {
            pagePerScreen = 2;
        }
        
        pagePerScreen *= mPrintSettings.getPagination().getPerPage();
        
        int frontIndex = (index * pagePerScreen);
        drawPDFPagesOnBitmap(front, frontIndex, false, false);
        
        if (mCurlView.getViewMode() == CurlView.SHOW_TWO_PAGES) {
            int backIndex = (index * pagePerScreen) + mPrintSettings.getPagination().getPerPage();
            
            boolean verticalFlip = mCurlView.getBindPosition() == CurlView.BIND_TOP;
            drawPDFPagesOnBitmap(back, backIndex, !verticalFlip, verticalFlip);
        }
        
        if (mBmpCache != null) {
            mBmpCache.put(getCacheKey(index, width, height, CurlPage.SIDE_FRONT), front);
            mBmpCache.put(getCacheKey(index, width, height, CurlPage.SIDE_BACK), back);
        }
        
        return new Bitmap[] { front, back };
    }
    
    private void tryDrawRenderBitmaps(Bitmap renderBmps[], Bitmap destBmps[]) {
        for (int i = 0; i < renderBmps.length; i++) {
            if (renderBmps[i] != null) {
                ImageUtils.renderBmpToBmp(renderBmps[i], destBmps[i], shouldDisplayColor());
                
            }
        }
    }
    
    // ================================================================================
    // View-related methods
    // ================================================================================
    
    private void initializeCurlView() {
        mCurlView = new CurlView(getContext());
        mCurlView.setMargins(0, 0, 0, 0);
        mCurlView.setPageProvider(mPdfPageProvider);
        mCurlView.setBindPosition(CurlView.BIND_LEFT);
        mCurlView.setBackgroundColor(getResources().getColor(R.color.theme_light_2));
        
        setupCurlPageView();
        setupCurlBind();
        
        if (!isInEditMode()) {
            float percentage = getResources().getFraction(R.dimen.preview_view_drop_shadow_percentage, 1, 1);
            mCurlView.setDropShadowSize(percentage);
        }
        
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
        
        if (!mCurlView.getAllowLastPageCurl()) {
            pageCount--;
        }
        
        mSeekBar.setMax(0);
        mSeekBar.setMax(pageCount);
        mSeekBar.setProgress(currentPage);
    }
    
    private void updatePageLabel() {
        final String FORMAT_ONE_PAGE_STATUS = "PAGE %d / %d";
        final String FORMAT_TWO_PAGE_STATUS = "PAGE %d-%d / %d";
        
        int currentPage = getCurrentPage();
        int pageCount = mPdfPageProvider.getPageCount();
        
        if (mCurlView.getViewMode() == CurlView.SHOW_ONE_PAGE
                || getCurrentPage() == 0) {
            mPageLabel.setText(String.format(Locale.getDefault(), FORMAT_ONE_PAGE_STATUS, currentPage + 1, pageCount));
        } else if (getCurrentPage() == mPdfPageProvider.getPageCount()) {
            mPageLabel.setText(String.format(Locale.getDefault(), FORMAT_ONE_PAGE_STATUS, currentPage, pageCount));
        } else {
            mPageLabel.setText(String.format(Locale.getDefault(), FORMAT_TWO_PAGE_STATUS, currentPage, currentPage+1, pageCount));
        }
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
            return PrintPreviewView.this.getPageCount();
        }
        
        @Override
        public void updatePage(CurlPage page, int width, int height, int index) {
            boolean GET_FROM_CACHE = false;
            
            if (GET_FROM_CACHE) {
                Bitmap cachedPages[] = getBitmapsFromCacheForPage(index, width, height);
                /*
                int bmpDimensions[] = getPageDimensions(width, height);
                Bitmap bmps[] = { 
                        Bitmap.createBitmap(bmpDimensions[0], bmpDimensions[1], BMP_CONFIG_TEXTURE),
                        Bitmap.createBitmap(bmpDimensions[0], bmpDimensions[1], BMP_CONFIG_TEXTURE)
                };
                bmps[0].eraseColor(Color.WHITE);
                bmps[1].eraseColor(Color.WHITE);
                
                tryDrawRenderBitmaps(cachedPages, bmps);
                
                page.setTexture(bmps[0], CurlPage.SIDE_FRONT);
                page.setTexture(bmps[1], CurlPage.SIDE_BACK);
                */

                Bitmap bmps[] = { 
                    Bitmap.createBitmap(width, height, BMP_CONFIG_TEXTURE),
                    Bitmap.createBitmap(width, height, BMP_CONFIG_TEXTURE)
                };
                
                page.setTexture(bmps[0], CurlPage.SIDE_FRONT);
                page.setTexture(bmps[1], CurlPage.SIDE_BACK);
                
                if (cachedPages[0] == null || cachedPages[1] == null) {
                    new PDFRenderTask(page, width, height, index, page.createNewHandler()).execute();
                } else {
                    tryDrawRenderBitmaps(cachedPages, bmps);
                }
            } else {
                Bitmap renderedPages[] = getRenderBitmaps(index, width, height);
                
                page.setTexture(renderedPages[0], CurlPage.SIDE_FRONT);
                page.setTexture(renderedPages[1], CurlPage.SIDE_BACK);
            }
            
            page.setColor(Color.argb(255, 255, 255, 255), CurlPage.SIDE_FRONT);
            page.setColor(Color.argb(255, 255, 255, 255), CurlPage.SIDE_BACK);
        }

        @Override
        public void indexChanged(int index)
        {
            updateSeekBar();
            
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                public void run() {
                    updatePageLabel();
                }
            });
        }
    }

    private class PDFRenderTask extends AsyncTask<Void, Void, Void> {
        WeakReference<CurlPage> mCurlPageRef;
        WeakReference<Object> mHandlerRef;
        int mWidth;
        int mHeight;
        int mIndex;

        Bitmap mBmps[];
        
        public PDFRenderTask(CurlPage page, int width, int height, int index, Object handler) {
            mCurlPageRef = new WeakReference<CurlPage>(page);
            mHandlerRef = new WeakReference<Object>(handler);
            mWidth = width;
            mHeight = height;
            mIndex = index;

            int bmpDimensions[] = getPageDimensions(width, height);
            mBmps = new Bitmap[]{ 
                    Bitmap.createBitmap(bmpDimensions[0], bmpDimensions[1], BMP_CONFIG_TEXTURE),
                    Bitmap.createBitmap(bmpDimensions[0], bmpDimensions[1], BMP_CONFIG_TEXTURE)
            };
            mBmps[0].eraseColor(Color.WHITE);
            mBmps[1].eraseColor(Color.WHITE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Bitmap renderBmps[] = getRenderBitmaps(mIndex, mWidth, mHeight);
            tryDrawRenderBitmaps(renderBmps, mBmps);
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            if (mHandlerRef.get() != null && mCurlPageRef.get() != null) {
                mCurlPageRef.get().reset();
                mCurlPageRef.get().setTexture(mBmps[0], CurlPage.SIDE_FRONT);
                mCurlPageRef.get().setTexture(mBmps[1], CurlPage.SIDE_BACK);
                mCurlView.requestRender();
            } else {
                mBmps[0].recycle();
                mBmps[1].recycle();
            }
            
        }
    }
}

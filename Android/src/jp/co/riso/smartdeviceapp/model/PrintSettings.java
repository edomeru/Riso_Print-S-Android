package jp.co.riso.smartdeviceapp.model;

public class PrintSettings {
    
    private int mCopies;
    private ColorMode mColorMode;
    private boolean mZoom;
    private int mZoomRate;
    private PaperType mPaperType;
    private PaperSize mPaperSize;
    private boolean mDuplex;
    
    private Pagination mPagination;
    
    private ImageQuality mImageQuality;
    
    private Sort mSort;
    private BookletBinding mBookletBinding;
    private Bind mBind;
    private Staple mStaple;
    private Punch mPunch;
    private Tray mCatchTray;
    
    public PrintSettings() {
        mCopies = 1;
        mColorMode = ColorMode.AUTO;
        mZoom = false;
        mZoomRate = 100;
        mPaperType = PaperType.NORMAL;
        mPaperSize = PaperSize.A4;
        mDuplex = false;
        
        mPagination = Pagination.OFF;
        
        mImageQuality = ImageQuality.STANDARD;
        
        mSort = Sort.BETWEEN_PAGES;
        mBookletBinding = BookletBinding.OFF;
        mBind = Bind.LEFT;
        mStaple = Staple.OFF;
        mPunch = Punch.OFF;
        mCatchTray = Tray.AUTO;
    }
    
    // ================================================================================
    // Getter/Setters
    // ================================================================================
    
    public int getCopies() {
        return mCopies;
    }
    
    public void setCopies(int copies) {
        mCopies = copies;
    }
    
    public ColorMode getColorMode() {
        return mColorMode;
    }
    
    public void setColorMode(ColorMode colorMode) {
        mColorMode = colorMode;
    }
    
    public boolean getZoom() {
        return mZoom;
    }
    
    public void setZoom(boolean zoom) {
        mZoom = zoom;
    }
    
    public int getZoomRate() {
        return mZoomRate;
    }
    
    public void setZoomRate(int zoomRate) {
        mZoomRate = zoomRate;
    }
    
    public PaperType getPaperType() {
        return mPaperType;
    }
    
    public void setPaperType(PaperType paperType) {
        mPaperType = paperType;
    }
    
    public PaperSize getPaperSize() {
        return mPaperSize;
    }
    
    public void setPaperSize(PaperSize paperSize) {
        mPaperSize = paperSize;
    }
    
    public boolean isDuplex() {
        return mDuplex;
    }
    
    public void setDuplex(boolean duplex) {
        mDuplex = duplex;
    }
    
    public Pagination getPagination() {
        return mPagination;
    }
    
    public void setPagination(Pagination pagination) {
        mPagination = pagination;
    }
    
    public ImageQuality getImageQuality() {
        return mImageQuality;
    }
    
    public void setImageQuality(ImageQuality imageQuality) {
        mImageQuality = imageQuality;
    }
    
    public Sort getSort() {
        return mSort;
    }
    
    public void setSort(Sort sort) {
        mSort = sort;
    }
    
    public BookletBinding getBookletBinding() {
        return mBookletBinding;
    }
    
    public void setBookletBinding(BookletBinding bookletBinding) {
        mBookletBinding = bookletBinding;
    }
    
    public Bind getBind() {
        return mBind;
    }
    
    public void setBind(Bind bind) {
        mBind = bind;
    }
    
    public Staple getStaple() {
        return mStaple;
    }
    
    public void setStaple(Staple staple) {
        mStaple = staple;
    }
    
    public Punch getPunch() {
        return mPunch;
    }
    
    public void setPunch(Punch punch) {
        mPunch = punch;
    }
    
    public Tray getCatchTray() {
        return mCatchTray;
    }
    
    public void setCatchTray(Tray catchTray) {
        mCatchTray = catchTray;
    }
    
    // ================================================================================
    // Internal Enumaration Types
    // ================================================================================
    
    public enum ColorMode {
        AUTO, COLOR, MONOCHROME
    }
    
    public enum PaperType {
        NORMAL, THIN, THICK
    }
    
    // sizes from http://en.wikipedia.org/wiki/Paper_size
    public enum PaperSize {
        A3(297.0f, 420.0f), A4(210.0f, 297.0f), LETTER(215.9f, 279.4f), LEGAL(215.9f, 355.6f), ENVELOPE(110.0f, 220.0f);
        
        private final float mWidth;
        private final float mHeight;
        
        PaperSize(float width, float height) {
            mWidth = width;
            mHeight = height;
        }
        
        public float getWidth() {
            return mWidth;
        }
        
        public float getHeight() {
            return mHeight;
        }
    }
    
    public enum Pagination {
        OFF (1, 1, 1, false),
        TWO_IN_ONE (2, 2, 1, true),
        FOUR_IN_ONE (4, 2, 2, false),
        SIX_IN_ONE (6, 3, 2, true),
        NINE_IN_ONE (9, 3, 3, false),
        SIXTEEN_IN_ONE (16, 4, 4, false);
        
        private final int mPerPage;
        private final int mRows;
        private final int mCols;
        private final boolean mFlipLandscape;
        
        Pagination(int perPage, int x, int y, boolean flipLandscape) {
            mPerPage = perPage;
            mCols = x;
            mRows = y;
            mFlipLandscape = flipLandscape;
        }
        
        public int getPerPage() {
            return mPerPage;
        }
        
        public int getRows() {
            return mRows;
        }
        
        public int getCols() {
            return mCols;
        }
        
        public boolean isFlipLandscape() {
            return mFlipLandscape;
        }
        
    }
    
    public enum ImageQuality {
        DRAFT, STANDARD, HIGH_DEFINITION
    }
    
    public enum Sort {
        BETWEEN_PAGES, BETWEEN_SECTIONS
    }
    
    public enum BookletBinding {
        OFF, FOLD_AND_STAPLE
    }
    
    public enum Bind {
        LEFT, RIGHT, TOP
    }
    
    public enum Staple {
        OFF, LEFT_2, LEFT_TOP_1, RIGHT_2, RIGHT_TOP_1
    }
    
    public enum Punch {
        OFF, TWO_HOLES, FOUR_HOLES
    }
    
    public enum Tray {
        AUTO, TRAY_1, TRAY_2, TRAY_3, BYPASS_TRAY
    }
}

/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintSettings.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model;

import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.BookletFinish;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.BookletLayout;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.ColorMode;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.Duplex;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.FinishingSide;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.Imposition;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.ImpositionOrder;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.InputTray;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.Orientation;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.OutputTray;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.PaperSize;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.PaperType;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.Punch;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.Sort;
import jp.co.riso.smartdeviceapp.model.PrintSettingsConstants.Staple;

public class PrintSettings {
    
    private ColorMode mColorMode;
    private Orientation mOrientation;
    private int mCopies;
    private Duplex mDuplex;
    private PaperSize mPaperSize;
    private boolean mScaleToFit;
    private PaperType mPaperType;
    private InputTray mInputTray;
    
    private Imposition mImposition;
    private ImpositionOrder mImpositionOrder;
    
    private Sort mSort;
    private boolean mBooklet;
    private BookletFinish mBookletFinish;
    private BookletLayout mBookletLayout;
    private FinishingSide mFinishingSide;
    private Staple mStaple;
    private Punch mPunch;
    private OutputTray mOutputTray;
    
    public PrintSettings() {
        mColorMode = ColorMode.AUTO;
        mOrientation = Orientation.PORTRAIT;
        mCopies = 1;
        mDuplex = Duplex.OFF;
        mPaperSize = PaperSize.A4;
        mScaleToFit = false;
        mPaperType = PaperType.ANY;
        mInputTray = InputTray.AUTO;
        
        mImposition = Imposition.OFF;
        mImpositionOrder = ImpositionOrder.L_R;
        
        mSort = Sort.PER_PAGE;
        mBooklet = false;
        mBookletFinish = BookletFinish.PAPER_FOLDING;
        mBookletLayout = BookletLayout.L_R;
        mFinishingSide = FinishingSide.LEFT;
        mStaple = Staple.OFF;
        mPunch = Punch.OFF;
        mOutputTray = OutputTray.AUTO;
    }
    
    public PrintSettings(PrintSettings printSettings) {
        mColorMode = printSettings.mColorMode;
        mOrientation = printSettings.mOrientation;
        mCopies = printSettings.mCopies;
        mDuplex = printSettings.mDuplex;
        mPaperSize = printSettings.mPaperSize;
        mScaleToFit = printSettings.mScaleToFit;
        mPaperType = printSettings.mPaperType;
        mInputTray = printSettings.mInputTray;
        
        mImposition = printSettings.mImposition;
        mImpositionOrder = printSettings.mImpositionOrder;
        
        mSort = printSettings.mSort;
        mBooklet = printSettings.mBooklet;
        mBookletFinish = printSettings.mBookletFinish;
        mBookletLayout = printSettings.mBookletLayout;
        mFinishingSide = printSettings.mFinishingSide;
        mStaple = printSettings.mStaple;
        mPunch = printSettings.mPunch;
        mOutputTray = printSettings.mOutputTray;
    }
    
    // ================================================================================
    // Getter/Setters
    // ================================================================================
    
    /**
     * @return the ColorMode
     */
    public ColorMode getColorMode() {
        return mColorMode;
    }
    
    /**
     * @param colorMode
     *            the ColorMode to set
     */
    public void setColorMode(ColorMode colorMode) {
        mColorMode = colorMode;
    }
    
    /**
     * @return the Orientation
     */
    public Orientation getOrientation() {
        return mOrientation;
    }
    
    /**
     * @param orientation
     *            the mOrientation to set
     */
    public void setOrientation(Orientation orientation) {
        mOrientation = orientation;
    }
    
    /**
     * @return the mCopies
     */
    public int getCopies() {
        return mCopies;
    }
    
    /**
     * @param copies
     *            the mCopies to set
     */
    public void setCopies(int copies) {
        this.mCopies = copies;
    }
    
    /**
     * @return the mDuplex
     */
    public Duplex getDuplex() {
        return mDuplex;
    }
    
    /**
     * @param duplex
     *            the mDuplex to set
     */
    public void setDuplex(Duplex duplex) {
        mDuplex = duplex;
    }
    
    /**
     * @return the mPaperSize
     */
    public PaperSize getPaperSize() {
        return mPaperSize;
    }
    
    /**
     * @param paperSize
     *            the mPaperSize to set
     */
    public void setPaperSize(PaperSize paperSize) {
        mPaperSize = paperSize;
    }
    
    /**
     * @return the mScaleToFit
     */
    public boolean isScaleToFit() {
        return mScaleToFit;
    }
    
    /**
     * @param scaleToFit
     *            the mScaleToFitInt to set in int value
     */
    public void setScaleToFit(int scaleToFitInt) {
        mScaleToFit = (scaleToFitInt == 1);
    }
    
    /**
     * @param scaleToFit
     *            the mScaleToFit to set
     */
    public void setScaleToFit(boolean scaleToFit) {
        mScaleToFit = scaleToFit;
    }
    
    /**
     * @return the mPaperType
     */
    public PaperType getPaperType() {
        return mPaperType;
    }
    
    /**
     * @param paperType
     *            the mPaperType to set
     */
    public void setPaperType(PaperType paperType) {
        mPaperType = paperType;
    }
    
    /**
     * @return the mInputTray
     */
    public InputTray getInputTray() {
        return mInputTray;
    }
    
    /**
     * @param inputTray
     *            the mInputTray to set
     */
    public void setInputTray(InputTray inputTray) {
        mInputTray = inputTray;
    }
    
    /**
     * @return the mImposition
     */
    public Imposition getImposition() {
        return mImposition;
    }
    
    /**
     * @param imposition
     *            the mImposition to set
     */
    public void setImposition(Imposition imposition) {
        mImposition = imposition;
    }
    
    /**
     * @return the mImpositionOrder
     */
    public ImpositionOrder getImpositionOrder() {
        return mImpositionOrder;
    }
    
    /**
     * @param impositionOrder
     *            the mImpositionOrder to set
     */
    public void setImpositionOrder(ImpositionOrder impositionOrder) {
        mImpositionOrder = impositionOrder;
    }
    
    /**
     * @return the mSort
     */
    public Sort getSort() {
        return mSort;
    }
    
    /**
     * @param sort
     *            the mSort to set
     */
    public void setSort(Sort sort) {
        mSort = sort;
    }
    
    /**
     * @return the mBooklet
     */
    public boolean isBooklet() {
        return mBooklet;
    }
    
    /**
     * @param booklet
     *            the mBooklet to set int value
     */
    public void setBooklet(int bookletInt) {
        mBooklet = (bookletInt == 1);
    }
    
    /**
     * @param booklet
     *            the mBooklet to set
     */
    public void setBooklet(boolean booklet) {
        mBooklet = booklet;
    }
    
    /**
     * @return the mBookletFinish
     */
    public BookletFinish getBookletFinish() {
        return mBookletFinish;
    }
    
    /**
     * @param bookletFinish
     *            the mBookletFinish to set
     */
    public void setBookletFinish(BookletFinish bookletFinish) {
        mBookletFinish = bookletFinish;
    }
    
    /**
     * @return the mBookletLayout
     */
    public BookletLayout getBookletLayout() {
        return mBookletLayout;
    }
    
    /**
     * @param bookletLayout
     *            the mBookletLayout to set
     */
    public void setBookletLayout(BookletLayout bookletLayout) {
        mBookletLayout = bookletLayout;
    }
    
    /**
     * @return the mFinishingSide
     */
    public FinishingSide getFinishingSide() {
        return mFinishingSide;
    }
    
    /**
     * @param finishingSide
     *            the mFinishingSide to set
     */
    public void setFinishingSide(FinishingSide finishingSide) {
        mFinishingSide = finishingSide;
    }
    
    /**
     * @return the mStaple
     */
    public Staple getStaple() {
        return mStaple;
    }
    
    /**
     * @param staple
     *            the mStaple to set
     */
    public void setStaple(Staple staple) {
        mStaple = staple;
    }
    
    /**
     * @return the mPunch
     */
    public Punch getPunch() {
        return mPunch;
    }
    
    /**
     * @param punch
     *            the mPunch to set
     */
    public void setPunch(Punch punch) {
        mPunch = punch;
    }
    
    /**
     * @return the mOutputTray
     */
    public OutputTray getOutputTray() {
        return mOutputTray;
    }
    
    /**
     * @param outputTray
     *            the mOutputTray to set
     */
    public void setOutputTray(OutputTray outputTray) {
        mOutputTray = outputTray;
    }
    
    // ================================================================================
    // Getter/Setters by tag
    // ================================================================================
    
    public int getValue(String tag) {
        
        // Refresh view here
        PrintSettingsConstants.Setting setting = PrintSettingsConstants.SETTING_MAP.get(tag);
        
        switch (setting) {
            case COLOR_MODE:
                return getColorMode().ordinal();
            case ORIENTATION:
                return getOrientation().ordinal();
            case NUM_COPIES:
                return getCopies();
            case DUPLEX:
                return getDuplex().ordinal();
            case PAPER_SIZE:
                return getPaperSize().ordinal();
            case SCALE_TO_FIT:
                return isScaleToFit() ? 1 : 0;
            case PAPER_TYPE:
                return getPaperType().ordinal();
            case INPUT_TRAY:
                return getInputTray().ordinal();
            case IMPOSITION:
                return getImposition().ordinal();
            case IMPOSITION_ORDER:
                return getImpositionOrder().ordinal();
            case SORT:
                return getSort().ordinal();
            case BOOKLET:
                return isBooklet() ? 1 : 0;
            case BOOKLET_LAYOUT:
                return getBookletLayout().ordinal();
            case BOOKLET_FINISH:
                return getBookletFinish().ordinal();
            case FINISHING_SIDE:
                return getFinishingSide().ordinal();
            case STAPLE:
                return getStaple().ordinal();
            case PUNCH:
                return getPunch().ordinal();
            case OUTPUT_TRAY:
                return getOutputTray().ordinal();
        }
        
        return 0;
    }
    
    public boolean setValue(String tag, int value) {
        
        // Refresh view here
        PrintSettingsConstants.Setting setting = PrintSettingsConstants.SETTING_MAP.get(tag);
        
        switch (setting) {
            case COLOR_MODE:
                setColorMode(ColorMode.values()[value]);
                break;
            case ORIENTATION:
                setOrientation(Orientation.values()[value]);
                break;
            case NUM_COPIES:
                setCopies(value);
                break;
            case DUPLEX:
                setDuplex(Duplex.values()[value]);
                break;
            case PAPER_SIZE:
                setPaperSize(PaperSize.values()[value]);
                break;
            case SCALE_TO_FIT:
                setScaleToFit(value);
                break;
            case PAPER_TYPE:
                setPaperType(PaperType.values()[value]);
                break;
            case INPUT_TRAY:
                setInputTray(InputTray.values()[value]);
                break;
            case IMPOSITION:
                setImposition(Imposition.values()[value]);
                break;
            case IMPOSITION_ORDER:
                setImpositionOrder(ImpositionOrder.values()[value]);
                break;
            case SORT:
                setSort(Sort.values()[value]);
                break;
            case BOOKLET:
                setBooklet(value);
                break;
            case BOOKLET_LAYOUT:
                setBookletLayout(BookletLayout.values()[value]);
                break;
            case BOOKLET_FINISH:
                setBookletFinish(BookletFinish.values()[value]);
                break;
            case FINISHING_SIDE:
                setFinishingSide(FinishingSide.values()[value]);
                break;
            case STAPLE:
                setStaple(Staple.values()[value]);
                break;
            case PUNCH:
                setPunch(Punch.values()[value]);
                break;
            case OUTPUT_TRAY:
                setOutputTray(OutputTray.values()[value]);
                break;
        }
        
        return true;
    }
}

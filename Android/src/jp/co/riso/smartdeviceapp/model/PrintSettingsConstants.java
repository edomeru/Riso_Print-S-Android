/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintSettingsConstants.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model;

import java.util.HashMap;
import java.util.Map;

public class PrintSettingsConstants {
    
    // ================================================================================
    // Tags
    // ================================================================================
    
    public static final Map<String, Setting> SETTING_MAP;
    
    public enum Setting {
        COLOR_MODE,
        ORIENTATION,
        NUM_COPIES,
        DUPLEX,
        PAPER_SIZE,
        SCALE_TO_FIT,
        PAPER_TYPE,
        INPUT_TRAY,
        IMPOSITION,
        IMPOSITION_ORDER,
        SORT,
        BOOKLET,
        BOOKLET_FINISH,
        BOOKLET_LAYOUT,
        FINISHING_SIDE,
        STAPLE,
        PUNCH,
        OUTPUT_TRAY;
    };

    static {
        SETTING_MAP = new HashMap<String, Setting>();
        
        SETTING_MAP.put("colorMode", Setting.COLOR_MODE); 
        SETTING_MAP.put("orientation", Setting.ORIENTATION);
        SETTING_MAP.put("numCopies", Setting.NUM_COPIES);
        SETTING_MAP.put("duplex", Setting.DUPLEX);
        SETTING_MAP.put("paperSize", Setting.PAPER_SIZE);
        SETTING_MAP.put("scaleToFit", Setting.SCALE_TO_FIT);
        SETTING_MAP.put("paperType", Setting.PAPER_TYPE);
        SETTING_MAP.put("inputTray", Setting.INPUT_TRAY);
        SETTING_MAP.put("imposition", Setting.IMPOSITION);
        SETTING_MAP.put("impositionOrder", Setting.IMPOSITION_ORDER);
        SETTING_MAP.put("sort", Setting.SORT);
        SETTING_MAP.put("booklet", Setting.BOOKLET);
        SETTING_MAP.put("bookletFinish", Setting.BOOKLET_FINISH);
        SETTING_MAP.put("bookletLayout", Setting.BOOKLET_LAYOUT);
        SETTING_MAP.put("finishingSide", Setting.FINISHING_SIDE);
        SETTING_MAP.put("staple", Setting.STAPLE);
        SETTING_MAP.put("punch", Setting.PUNCH);
        SETTING_MAP.put("outputTray", Setting.OUTPUT_TRAY);
    }

    // ================================================================================
    // Enumaration Types
    // ================================================================================
    
    public enum ColorMode {
        AUTO, FULL_COLOR, MONOCHROME;
    }
    
    public enum Orientation {
        PORTRAIT, LANDSCAPE;
    }
    
    public enum Duplex {
        OFF, LONG_EDGE, SHORT_EDGE;
    }
    
    // sizes from http://en.wikipedia.org/wiki/Paper_size
    public enum PaperSize {
        A3W(297.0f, 420.0f),
        A4(210.0f, 297.0f),
        A5(3.0f, 4.0f),
        A6(3.0f, 4.0f),
        B4(3.0f, 4.0f),
        B5(3.0f, 4.0f),
        FOOLSCAP(3.0f, 4.0f),
        TABLOID(3.0f, 4.0f),
        LEGAL(215.9f, 355.6f),
        LETTER(215.9f, 279.4f),
        STATEMENT(3.0f, 4.0f);
        
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
    
    public enum PaperType {
        ANY, PLAIN, IJ_PAPER, MATTE, HIGH_QUALITY, CARD_IJ, LW_PAPER;
    }
    
    public enum InputTray {
        AUTO, STANDARD, TRAY_1, TRAY_2, TRAY_3;
    }
    
    public enum Imposition {
        OFF (1, 1, 1, false),
        TWO_UP (2, 2, 1, true),
        FOUR_UP (4, 2, 2, false);
        
        private final int mPerPage;
        private final int mRows;
        private final int mCols;
        private final boolean mFlipLandscape;
        
        Imposition(int perPage, int x, int y, boolean flipLandscape) {
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
    
    public enum ImpositionOrder {
        R_L, L_R, TL_B, TL_R, TR_B, TR_L;
    }
    
    public enum Sort {
        PER_PAGE, PER_COPY;
    }
    
    public enum BookletFinish {
        PAPER_FOLDING, FOLD_AND_STAPLE;
    }
    
    public enum BookletLayout {
        L_R, R_L, T_B;
    }
    
    public enum FinishingSide {
        LEFT, RIGHT, TOP;
    }
    
    public enum Staple {
        OFF, UL, UR, SIDE_2, SIDE_1;
    }
    
    public enum Punch {
        OFF, HOLES_2, HOLES_3, HOLES_4;
    }
    
    public enum OutputTray {
        AUTO, FACEDOWN, FACEUP, TOP, STACKING;
    }
}

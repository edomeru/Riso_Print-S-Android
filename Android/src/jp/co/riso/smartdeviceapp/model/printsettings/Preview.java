/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * Preview.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model.printsettings;

public class Preview {
    public enum ColorMode {
        AUTO, FULL_COLOR, MONOCHROME;
    }
    
    public enum Orientation {
        PORTRAIT, LANDSCAPE;
    }
    
    public enum Duplex {
        OFF, LONG_EDGE, SHORT_EDGE;
    }
    
    // sizes from IS1000CJ
    public enum PaperSize {
        A3(297.0f, 420.0f),
        A3W(316.0f, 460.0f),
        A4(210.0f, 297.0f),
        A5(148.0f, 210.0f),
        A6(105.0f, 148.0f),
        B4(257.0f, 364.0f),
        B5(182.0f, 257.0f),
        FOOLSCAP(216.0f, 340.0f),
        TABLOID(280.0f, 432.0f),
        LEGAL(216.0f, 356.0f),
        LETTER(216.0f, 280.0f),
        STATEMENT(140.0f, 216.0f);
        
        private final float mWidth;
        private final float mHeight;
        
        PaperSize(float width, float height) {
            mWidth = width;
            mHeight = height;
        }
        
        public static String getTag() {
            return "paperSize";
        }
        
        public float getWidth() {
            return mWidth;
        }
        
        public float getHeight() {
            return mHeight;
        }
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
        LEFT, TOP, RIGHT;
    }
    
    public enum Staple {
        OFF (0),
        ONE_UL (1),
        ONE_UR (1),
        ONE (1),
        TWO (2);
        
        private final int mCount;

        Staple(int count) {
            mCount = count;
        }
        
        public int getCount() {
            return mCount;
        }
    }
    
    public enum Punch {
        OFF (0),
        HOLES_2 (2),
        HOLES_4 (4);
        
        private final int mCount;

        Punch(int count) {
            mCount = count;
        }
        
        public int getCount() {
            return mCount;
        }
    }
}

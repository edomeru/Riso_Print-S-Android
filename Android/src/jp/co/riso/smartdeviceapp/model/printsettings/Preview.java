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
        B6(128.0f, 182.0f),
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
        
        /**
         * @return tag
         */
        public static String getTag() {
            return "paperSize";
        }
        
        /**
         * @return width
         */
        public float getWidth() {
            return mWidth;
        }
        
        /**
         * @return height
         */
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
        
        /**
         * @return perPage
         */
        public int getPerPage() {
            return mPerPage;
        }
        
        /**
         * @return rows
         */
        public int getRows() {
            return mRows;
        }
        
        /**
         * @return columns
         */
        public int getCols() {
            return mCols;
        }
        
        /**
         * @return flip landscape
         */
        public boolean isFlipLandscape() {
            return mFlipLandscape;
        }
    }
    
    public enum ImpositionOrder {
        L_R (true, true, true),
        R_L (false, false, true),
        TL_R (true, true, true),
        TR_L (false, true, true),
        TL_B (true, true, false),
        TR_B (false, true, false);

        private boolean mLeftToRight;
        private boolean mTopToBottom;
        private boolean mHorizontalFlow;

        ImpositionOrder(boolean leftToRight, boolean topToBottom, boolean horizontalFlow) {
            mLeftToRight = leftToRight;
            mTopToBottom = topToBottom;
            mHorizontalFlow = horizontalFlow;
        }
        
        /**
         * @return imposition order is left to right
         */
        public boolean isLeftToRight() {
            return mLeftToRight;
        }

        /**
         * @return imposition order is top to bottom
         */
        public boolean isTopToBottom() {
            return mTopToBottom;
        }
        
        /**
         * @return is horizontal flow
         */
        public boolean isHorizontalFlow() {
            return mHorizontalFlow;
        }
    }
    
    public enum Sort {
        PER_PAGE, PER_COPY;
    }
    
    public enum BookletFinish {
        OFF, PAPER_FOLDING, FOLD_AND_STAPLE;
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
        
        /**
         * @return staple count
         */
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
        
        /**
         * @param use3Holes
         *            Whether the Punch should use 3 holes instead of 4 holes
         * @return hole count
         */
        public int getCount(boolean use3Holes) {
            if (use3Holes && Punch.this == HOLES_4) {
                return 3;
            }
            return mCount;
        }
    }
    
    public enum OutputTray {
        AUTO, FACEDOWN, TOP, STACKING;
    }
}

/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * Preview.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model.printsettings;

/**
 * @class Preview
 * 
 * @brief Preview data class representing the settings to be displayed in PrintSettings.
 */
public class Preview {
    /**
     * @brief Color Mode print settings that defines the color mode of the print job.
     */
    public enum ColorMode {
        AUTO, FULL_COLOR, MONOCHROME;
    }
    
    /**
     * @brief Orientation print settings that defines the page orientation.
     */
    public enum Orientation {
        PORTRAIT, LANDSCAPE;
    }
    
    /**
     * @brief Duplex print settings that determines duplex printing mode.
     */
    public enum Duplex {
        OFF, LONG_EDGE, SHORT_EDGE;
    }
    
    
    /**
     * @brief PaperSize print settings that determines the paper sizes to be used during print.
     * @note sizes from IS1000CJ 
     */
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
        
        /**
         * @brief PaperSize enum constructor
         * 
         * @param width Width of the paper in mm
         * @param height height of the paper in mm
         */
        PaperSize(float width, float height) {
            mWidth = width;
            mHeight = height;
        }
        
        /**
         * @brief Retrieves width of the paper in mm.
         * 
         * @return width in mm
         */
        public float getWidth() {
            return mWidth;
        }
        
        /**
         * @brief Retrieves height of the paper in mm.
         * 
         * @return height in mm
         */
        public float getHeight() {
            return mHeight;
        }
    }
    
    /**
     * @brief Imposition print settings that determines the number of pages to print per sheet.
     */
    public enum Imposition {
        OFF (1, 1, 1, false),
        TWO_UP (2, 2, 1, true),
        FOUR_UP (4, 2, 2, false);
        
        private final int mPerPage;
        private final int mRows;
        private final int mCols;
        private final boolean mFlipLandscape;
        
        /**
         * @brief Imposition enum constructor 
         * 
         * @param perPage Number of pages to be displayed per sheet
         * @param x Number of columns to be displayed per sheet
         * @param y Number of rows to be displayed per sheet
         * @param flipLandscape Determines if sheet is to be rotated
         */
        Imposition(int perPage, int x, int y, boolean flipLandscape) {
            mPerPage = perPage;
            mCols = x;
            mRows = y;
            mFlipLandscape = flipLandscape;
        }
        
        /**
         * @brief Retrieves number of pages to be displayed per sheet
         * 
         * @return Number of pages per sheet
         */
        public int getPerPage() {
            return mPerPage;
        }
        
        /**
         * @brief Retrieves number of rows to be displayed per sheet
         * 
         * @return Number of rows per sheet
         */
        public int getRows() {
            return mRows;
        }
        
        /**
         * @brief Retrieves number of columns to be displayed per sheet
         * 
         * @return Number of columns per sheet
         */
        public int getCols() {
            return mCols;
        }
        
        /**
         * @brief Determines if the sheet is to be rotated for display
         * 
         * @retval true Sheet is to be rotated for display
         * @retval false No change in orientation/rotation of the sheet for display
         */
        public boolean isFlipLandscape() {
            return mFlipLandscape;
        }
    }
    
    /**
     * @brief ImpositionOrder print settings that determines the direction of the PDF pages printed in one sheet.
     */
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

        /**
         * @brief ImpositionOrder enum constructor 
         * 
         * @param leftToRight Horizontal direction of pages.
         * @param topToBottom Vertical direction of pages.
         * @param horizontalFlow true if pages are to be filled horizontally first
         */
        ImpositionOrder(boolean leftToRight, boolean topToBottom, boolean horizontalFlow) {
            mLeftToRight = leftToRight;
            mTopToBottom = topToBottom;
            mHorizontalFlow = horizontalFlow;
        }
        
        /**
         * @brief Retrieves horizontal direction of pages.
         * 
         * @retval true Pages to be displayed starts from left
         * @retval false Pages to be displayed starts from right
         */
        public boolean isLeftToRight() {
            return mLeftToRight;
        }

        /**
         * @brief Retrieves vertical direction of pages.
         * 
         * @retval true Pages to be displayed starts from top
         * @retval false Pages to be displayed starts from bottom
         */
        public boolean isTopToBottom() {
            return mTopToBottom;
        }
        
        /**
         * @brief Retrieves flow of pages to be displayed (horizontal or vertical).
         * 
         * @retval true Pages are filled horizontally first.
         * @retval false Pages are filled vertically first.
         */
        public boolean isHorizontalFlow() {
            return mHorizontalFlow;
        }
    }
    
    /**
     * @brief Sort print settings that defines how the print output will be sorted.
     */
    public enum Sort {
        PER_PAGE, PER_COPY;
    }
    
    /**
     * @brief BookletFinish print settings that defines the finishing options for when booklet is on.
     */
    public enum BookletFinish {
        OFF, PAPER_FOLDING, FOLD_AND_STAPLE;
    }
    
    /**
     * @brief BookletLayout print settings that determines the direction of pages when booklet is on.
     */
    public enum BookletLayout {
        FORWARD, REVERSE;
    }
    
    /**
     * @brief FinishingSide print settings that refers to the edge where the document will be bound.
     */
    public enum FinishingSide {
        LEFT, TOP, RIGHT;
    }
    
    /**
     * @brief Staple print settings that determines how the print job will be stapled.
     */
    public enum Staple {
        OFF (0),
        ONE_UL (1),
        ONE_UR (1),
        ONE (1),
        TWO (2);
        
        private final int mCount;

        /**
         * @brief Staple enum constructor
         * 
         * @param count Number of staples.
         */
        Staple(int count) {
            mCount = count;
        }
        
        /**
         * @brief Retrieves the count of staples.
         * 
         * @return Number of staples
         */
        public int getCount() {
            return mCount;
        }
    }
    
    /**
     * @brief Punch print settings that determines how the printer will make a punch in the print output. 
     */
    public enum Punch {
        OFF (0),
        HOLES_2 (2),
        HOLES_4 (4);
        
        private final int mCount;

        /**
         * @brief Punch enum constructor
         * 
         * @param count Number of punch holes.
         */
        Punch(int count) {
            mCount = count;
        }
        
        /**
         * @brief Retrieves the count of punch holes.
         * 
         * @param use3Holes Whether the Punch should use 3 holes instead of 4 holes.
         * 
         * @return Number of punch holes.
         */
        public int getCount(boolean use3Holes) {
            if (use3Holes && Punch.this == HOLES_4) {
                return 3;
            }
            return mCount;
        }
    }
    
    /**
     * @brief OutputTray print settings that refers to the tray location of the finished copies.
     */
    public enum OutputTray {
        AUTO, FACEDOWN, TOP, STACKING;
    }
}

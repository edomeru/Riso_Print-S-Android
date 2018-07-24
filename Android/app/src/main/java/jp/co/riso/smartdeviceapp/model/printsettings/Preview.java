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
        AUTO, ///< Auto color mode
        FULL_COLOR, ///< Colored mode
        MONOCHROME, ///< Gray scale color mode
        DUAL_COLOR ///2-color mode
    }
    
    /**
     * @brief Orientation print settings that defines the page orientation.
     */
    public enum Orientation {
        PORTRAIT, ///< Portrait page orientation
        LANDSCAPE ///< Landscape page orientation
    }
    
    /**
     * @brief Duplex print settings that determines duplex printing mode.
     */
    public enum Duplex {
        OFF,  ///< Duplex mode is OFF
        LONG_EDGE,  ///< Long Edge duplex mode
        SHORT_EDGE ///< Short Edge duplex mode
    }
    
    /**
     * @brief PaperSize print settings that determines the paper sizes to be used during print.
     * 
     * @note Sizes is based from IS1000CJ
     */
    public enum PaperSize {
        A3(297.0f, 420.0f), ///< 297mm x 420mm
        A3W(316.0f, 460.0f), ///< 316mm x 460mm
        A4(210.0f, 297.0f), ///< 210mm x 297mm
        A5(148.0f, 210.0f), ///< 148mm x 210mm
        A6(105.0f, 148.0f), ///< 105mm x 148mm
        B4(257.0f, 364.0f), ///< 257mm x 364mm
        B5(182.0f, 257.0f), ///< 182mm x 257mm
        B6(128.0f, 182.0f), ///< 128mm x 182mm
        FOOLSCAP(216.0f, 340.0f), ///< 216mm x 340mm
        TABLOID(280.0f, 432.0f), ///< 280mm x 432mm
        LEGAL(216.0f, 356.0f), ///< 216mm x 356mm
        LETTER(216.0f, 280.0f), ///< 216mm x 280mm
        STATEMENT(140.0f, 216.0f), ///< 140mm x 216mm
        LEGAL13(216.0f, 330.0f), ///< 216mm x 330mm
        HACHIKAI(267.0f, 388.0f), ///< 267mm x 388mm
        JUROKUKAI(194.0f, 267.0f); ///< 194mm x 267mm
        
        private final float mWidth;
        private final float mHeight;
        
        /**
         * @brief PaperSize enum constructor
         * 
         * @param width Width of the paper in mm
         * @param height Height of the paper in mm
         */
        PaperSize(float width, float height) {
            mWidth = width;
            mHeight = height;
        }
        
        /**
         * @brief Retrieves width of the paper in mm.
         * 
         * @return Width in mm
         */
        public float getWidth() {
            return mWidth;
        }
        
        /**
         * @brief Retrieves height of the paper in mm.
         * 
         * @return Height in mm
         */
        public float getHeight() {
            return mHeight;
        }
    }

    /**
     * @brief InputTray_RAG print settings for RAG series that refers to the tray location of input paper.
     */
    public enum InputTray_RAG {
        AUTO,  ///< Auto Tray
        STANDARD,  ///< Standard Tray
        TRAY1,  ///< Tray 1
        TRAY2, ///< Tray 2
        EXTERNAL_FEEDER, ///< External 2000 sheets
    }

    /**
     * @brief Imposition print settings that determines the number of pages to print per sheet.
     */
    public enum Imposition {
        OFF (1, 1, 1, false), ///< 1 page per sheet
        TWO_UP (2, 2, 1, true), ///< 2 pages per sheet
        FOUR_UP (4, 2, 2, false); ///< 4 pages per sheet
        
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
        L_R (true, true, true), ///< Left to right
        R_L (false, false, true), ///< Right to left
        TL_R (true, true, true), ///< Upper left to right
        TR_L (false, true, true), ///< Upper right to left
        TL_B (true, true, false), ///< Upper left to bottom
        TR_B (false, true, false); ///< Upper right to bottom

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
        PER_COPY, ///< To be sorted according to copy
        PER_PAGE ///< To be grouped according to page
    }
    
    /**
     * @brief BookletFinish print settings that defines the finishing options for when booklet is on.
     */
    public enum BookletFinish {
        OFF,  ///< Booklet Finish is OFF
        PAPER_FOLDING,  ///< Paper will be folded
        FOLD_AND_STAPLE ///< Paper will be folded and stapled
    }
    
    /**
     * @brief BookletLayout print settings that determines the direction of pages when booklet is on.
     */
    public enum BookletLayout {
        FORWARD,  ///< Retain direction of pages
        REVERSE ///< Reverse direction of pages
    }
    
    /**
     * @brief FinishingSide print settings that refers to the edge where the document will be bound.
     */
    public enum FinishingSide {
        LEFT,  ///< Document will be bound on the left edge
        TOP,  ///< Document will be bound on the top edge
        RIGHT ///< Document will be bound on the right edge
    }
    
    /**
     * @brief Staple print settings that determines how the print job will be stapled.
     */
    public enum Staple {
        OFF (0), ///< No staples
        ONE_UL (1), ///< Upper left staple
        ONE_UR (1), ///< Upper right staple
        ONE (1), ///< One staple
        TWO (2); ///< Two staples
        
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
        OFF (0), ///< No punch holes
        HOLES_2 (2), ///< 2 holes
        HOLES_3 (3), ///< 3 holes
        HOLES_4 (4); ///< 4 holes
        
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
        AUTO,  ///< Auto Tray 
        FACEDOWN,  ///< Face down Tray
        TOP,  ///< Top Tray
        STACKING ///< Stacking Tray
    }
}

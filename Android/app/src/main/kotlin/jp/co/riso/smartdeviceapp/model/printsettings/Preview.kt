/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * Preview.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.model.printsettings

/**
 * @class Preview
 * 
 * @brief Preview data class representing the settings to be displayed in PrintSettings.
 */
class Preview {
    /**
     * @brief Color Mode print settings that defines the color mode of the print job.
     */
    enum class ColorMode {
        AUTO,  ///< Auto color mode
        FULL_COLOR,  ///< Colored mode
        MONOCHROME,  ///< Gray scale color mode
        DUAL_COLOR ///2-color mode
    }

    /**
     * @brief Orientation print settings that defines the page orientation.
     */
    enum class Orientation {
        PORTRAIT,  ///< Portrait page orientation
        LANDSCAPE ///< Landscape page orientation
    }

    /**
     * @brief Duplex print settings that determines duplex printing mode.
     */
    enum class Duplex {
        OFF,  ///< Duplex mode is OFF
        LONG_EDGE,  ///< Long Edge duplex mode
        SHORT_EDGE ///< Short Edge duplex mode
    }

    /**
     * @brief PaperSize print settings that determines the paper sizes to be used during print.
     * @note Sizes is based from IS1000CJ
     *
     * @param width Width of the paper in mm
     * @param height Height of the paper in mm
     */
    enum class PaperSize (
        val width: Float,
        val height: Float
    ) {
        A3(297.0f, 420.0f),  ///< 297mm x 420mm
        A3W(316.0f, 460.0f),  ///< 316mm x 460mm
        SRA3(320.0f, 450.0f),  ///< 320mm x 450mm
        A4(210.0f, 297.0f),  ///< 210mm x 297mm
        A5(148.0f, 210.0f),  ///< 148mm x 210mm
        A6(105.0f, 148.0f),  ///< 105mm x 148mm
        B4(257.0f, 364.0f),  ///< 257mm x 364mm
        B5(182.0f, 257.0f),  ///< 182mm x 257mm
        B6(128.0f, 182.0f),  ///< 128mm x 182mm
        FOOLSCAP(216.0f, 340.0f),  ///< 216mm x 340mm
        TABLOID(280.0f, 432.0f),  ///< 280mm x 432mm
        LEGAL(216.0f, 356.0f),  ///< 216mm x 356mm
        LETTER(216.0f, 280.0f),  ///< 216mm x 280mm
        STATEMENT(140.0f, 216.0f),  ///< 140mm x 216mm
        LEGAL13(216.0f, 330.0f),  ///< 216mm x 330mm
        HACHIKAI(267.0f, 388.0f),  ///< 267mm x 388mm
        JUROKUKAI(194.0f, 267.0f); ///< 194mm x 267mm

        companion object {
            /**
             * @brief Retrieves an array of paper size default options
             *
             * @return Paper size options array
             */
            @JvmStatic
            fun valuesDefault(): Array<PaperSize> {
                return arrayOf(
                    A3,
                    A3W,
                    A4,
                    A5,
                    A6,
                    B4,
                    B5,
                    B6,
                    FOOLSCAP,
                    TABLOID,
                    LEGAL,
                    LETTER,
                    STATEMENT,
                    LEGAL13,
                    HACHIKAI,
                    JUROKUKAI
                )
            }

            /**
             * @brief Retrieves an array of paper size options for GL series
             *
             * @return Paper size options array
             */
            @JvmStatic
            fun valuesGl(): Array<PaperSize> {
                return arrayOf(
                    A3,
                    A3W,
                    SRA3,
                    A4,
                    A5,
                    A6,
                    B4,
                    B5,
                    B6,
                    FOOLSCAP,
                    TABLOID,
                    LEGAL,
                    LETTER,
                    STATEMENT,
                    LEGAL13,
                    HACHIKAI,
                    JUROKUKAI
                )
            }
        }
    }

    // Content Print - START
    /**
     * @brief PaperType print settings that determines the paper types to be used during print.
     */
    enum class PaperType {
        ANY,
        PLAIN,
        IJ_PAPER,
        MATT_COATED,
        HIGH_QUALITY,
        CARD_IJ,
        LW_PAPER,
        ROUGH_PAPER,
        PLAIN_PREMIUM
    }
    // Content Print - END

    /**
     * @brief InputTrayFtGlCerezonaS print settings for FT / GL / CEREZONA S / OGA series that refers to the tray location of input paper.
     */
    enum class InputTrayFtGlCerezonaSOga {
        AUTO,  ///< Auto Tray
        STANDARD,  ///< Standard Tray
        TRAY1,  ///< Tray 1
        TRAY2,  ///< Tray 2
        TRAY3,  ///< Tray 3
        EXTERNAL_FEEDER; ///< External 2000 sheets

        companion object {
            
            /**
             * @brief Retrieves an array of input tray options for FT and CEREZONA S series
             *
             * @return Input tray options array
             */
            @JvmStatic
            fun valuesFtCerezonaS(): Array<InputTrayFtGlCerezonaSOga> {
                return arrayOf(AUTO, STANDARD, TRAY1, TRAY2, EXTERNAL_FEEDER)
            }


            /**
             * @brief Retrieves an array of input tray options for GL and OGA series
             *
             * @return Input tray options array
             */
            @JvmStatic
            fun valuesGlOga(): Array<InputTrayFtGlCerezonaSOga> {
                return arrayOf(AUTO, STANDARD, TRAY1, TRAY2, TRAY3, EXTERNAL_FEEDER)
            }
        }
    }

    /**
     * @brief Imposition print settings that determines the number of pages to print per sheet.
     *
     * @param perPage Number of pages to be displayed per sheet
     * @param cols Number of columns to be displayed per sheet
     * @param rows Number of rows to be displayed per sheet
     * @param isFlipLandscape Determines if sheet is to be rotated
     */
    enum class Imposition (
        val perPage: Int,
        val cols: Int,
        val rows: Int,
        val isFlipLandscape: Boolean
    ) {
        OFF(1, 1, 1, false),  ///< 1 page per sheet
        TWO_UP(2, 2, 1, true),  ///< 2 pages per sheet
        FOUR_UP(4, 2, 2, false);  ///< 4 pages per sheet
    }

    /**
     * @brief ImpositionOrder print settings that determines the direction of the PDF pages printed in one sheet.
     * 
     * @param isLeftToRight Horizontal direction of pages.
     * @param isTopToBottom Vertical direction of pages.
     * @param isHorizontalFlow true if pages are to be filled horizontally first
     */
    enum class ImpositionOrder(
        val isLeftToRight: Boolean,
        val isTopToBottom: Boolean,
        val isHorizontalFlow: Boolean
    ) {
        L_R(true, true, true),  ///< Left to right
        R_L(false, false, true),  ///< Right to left
        TL_R(true, true, true),  ///< Upper left to right
        TR_L(false, true, true),  ///< Upper right to left
        TL_B(true, true, false),  ///< Upper left to bottom
        TR_B(false, true, false);  ///< Upper right to bottom
    }

    /**
     * @brief Sort print settings that defines how the print output will be sorted.
     */
    enum class Sort {
        PER_COPY,  ///< To be sorted according to copy
        PER_PAGE ///< To be grouped according to page
    }

    /**
     * @brief BookletFinish print settings that defines the finishing options for when booklet is on.
     */
    enum class BookletFinish {
        OFF,  ///< Booklet Finish is OFF
        PAPER_FOLDING,  ///< Paper will be folded
        FOLD_AND_STAPLE ///< Paper will be folded and stapled
    }

    /**
     * @brief BookletLayout print settings that determines the direction of pages when booklet is on.
     */
    enum class BookletLayout {
        FORWARD,  ///< Retain direction of pages
        REVERSE ///< Reverse direction of pages
    }

    /**
     * @brief FinishingSide print settings that refers to the edge where the document will be bound.
     */
    enum class FinishingSide {
        LEFT,  ///< Document will be bound on the left edge
        TOP,  ///< Document will be bound on the top edge
        RIGHT ///< Document will be bound on the right edge
    }

    /**
     * @brief Staple print settings that determines how the print job will be stapled.
     *
     * @param count Number of staples
     */
    enum class Staple (
        val count: Int
    ) {
        OFF(0),  ///< No staples
        ONE_UL(1),  ///< Upper left staple
        ONE_UR(1),  ///< Upper right staple
        ONE(1),  ///< One staple
        TWO (2); ///< Two staples

    }

    /**
     * @brief Punch print settings that determines how the printer will make a punch in the print output.
     *
     * @param mCount Number of punch holes
     */
    enum class Punch (
        private val mCount: Int
    ) {
        OFF(0),  ///< No punch holes
        HOLES_2(2),  ///< 2 holes
        HOLES_3(3),  ///< 3 holes
        HOLES_4(4);  ///< 4 holes

        /**
         * @brief Retrieves the count of punch holes.
         *
         * @param use3Holes Whether the Punch should use 3 holes instead of 4 holes.
         * @return Number of punch holes.
         */
        fun getCount(use3Holes: Boolean): Int {
            return if (use3Holes && this@Punch == HOLES_4) {
                3
            } else mCount
        }
    }

    /**
     * @brief OutputTray print settings that refers to the tray location of the finished copies.
     */
    enum class OutputTray {
        AUTO,  ///< Auto Tray 
        FACEDOWN,  ///< Face down Tray
        TOP,  ///< Top Tray
        STACKING ///< Stacking Tray
    }
}
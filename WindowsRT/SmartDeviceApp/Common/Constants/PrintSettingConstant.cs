//
//  PrintSettingConstant.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/07.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using Windows.Foundation;

namespace SmartDeviceApp.Common.Constants
{
    public static class PrintSettingConstant
    {
        // printsettings.xml keys
        public const string KEY_COLOR_MODE = "colorMode";
        public const string KEY_ORIENTATION = "orientation";
        public const string KEY_COPIES = "numCopies";
        public const string KEY_DUPLEX = "duplex";
        public const string KEY_PAPER_SIZE = "paperSize";
        public const string KEY_SCALE_TO_FIT = "scaleToFit";
        public const string KEY_PAPER_TYPE = "paperType";
        public const string KEY_INPUT_TRAY = "inputTray";
        public const string KEY_IMPOSITION = "imposition";
        public const string KEY_IMPOSITION_ORDER = "impositionOrder";
        public const string KEY_SORT = "sort";
        public const string KEY_BOOKLET = "booklet";
        public const string KEY_BOOKLET_FINISHING = "bookletFinish";
        public const string KEY_BOOKLET_LAYOUT = "bookletLayout";
        public const string KEY_FINISHING_SIDE = "finishingSide";
        public const string KEY_STAPLE = "staple";
        public const string KEY_PUNCH = "punch";
        public const string KEY_OUTPUT_TRAY = "outputTray";

        // In inches. The following paper sizes are in portrait mode
        public static Size PAPER_SIZE_A3W        = new Size(11.69, 16.54);
        public static Size PAPER_SIZE_A4         = new Size( 8.27, 11.69);
        public static Size PAPER_SIZE_A5         = new Size( 5.83,  8.27);
        public static Size PAPER_SIZE_A6         = new Size( 4.13,  5.83);
        public static Size PAPER_SIZE_B4         = new Size(10.12, 14.33);
        public static Size PAPER_SIZE_B5         = new Size( 7.17, 10.12);
        public static Size PAPER_SIZE_FOOLSCAP   = new Size( 8.50, 13.00);
        public static Size PAPER_SIZE_TABLOID    = new Size(11.00, 17.00);
        public static Size PAPER_SIZE_LEGAL      = new Size( 8.50, 14.00);
        public static Size PAPER_SIZE_LETTER     = new Size( 8.50, 11.00);
        public static Size PAPER_SIZE_STATEMENT  = new Size( 5.50,  8.50);

        // In inches
        public static double MARGIN_PAPER = 0;
        public static double MARGIN_BETWEEN_PAGES = 0.5;

    }
}

﻿//
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

        // printsettings.xml Keys
        public const string KEY_GROUP   = "group";
        public const string KEY_NAME    = "name";
        public const string KEY_TEXT    = "text";
        public const string KEY_SETTING = "setting";
        public const string KEY_ICON    = "icon";
        public const string KEY_TYPE    = "type";
        public const string KEY_DEFAULT = "default";
        public const string KEY_OPTION  = "option";

        // printsettings.xml Name values
        public const string NAME_VALUE_COLOR_MODE        = "colorMode";
        public const string NAME_VALUE_ORIENTATION       = "orientation";
        public const string NAME_VALUE_COPIES            = "numCopies";
        public const string NAME_VALUE_DUPLEX            = "duplex";
        public const string NAME_VALUE_PAPER_SIZE        = "paperSize";
        public const string NAME_VALUE_SCALE_TO_FIT      = "scaleToFit";
        public const string NAME_VALUE_PAPER_TYPE        = "paperType";
        public const string NAME_VALUE_INPUT_TRAY        = "inputTray";
        public const string NAME_VALUE_IMPOSITION        = "imposition";
        public const string NAME_VALUE_IMPOSITION_ORDER  = "impositionOrder";
        public const string NAME_VALUE_SORT              = "sort";
        public const string NAME_VALUE_BOOKLET           = "booklet";
        public const string NAME_VALUE_BOOKLET_FINISHING = "bookletFinish";
        public const string NAME_VALUE_BOOKLET_LAYOUT    = "bookletLayout";
        public const string NAME_VALUE_FINISHING_SIDE    = "finishingSide";
        public const string NAME_VALUE_STAPLE            = "staple";
        public const string NAME_VALUE_PUNCH             = "punch";
        public const string NAME_VALUE_OUTPUT_TRAY       = "outputTray";

        // Paper sizes in portrait mode; in inches
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

        // Margin; in inches
        public static double MARGIN_PAPER         = 0;
        public static double MARGIN_BETWEEN_PAGES = 0.5;

    }
}

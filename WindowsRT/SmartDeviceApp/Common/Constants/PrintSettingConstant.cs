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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Graphics.Display;

namespace SmartDeviceApp.Common.Constants
{
    public sealed class PrintSettingConstant
    {
        // In inches. The following paper sizes are in portrait mode
        public static Size PAPER_SIZE_A3         = new Size(11.69, 16.54);
        public static Size PAPER_SIZE_A4         = new Size( 8.27, 11.69);
        public static Size PAPER_SIZE_A6         = new Size( 4.10,  5.80);
        public static Size PAPER_SIZE_B4         = new Size(10.12, 14.33);
        public static Size PAPER_SIZE_B5         = new Size( 7.17, 10.12);
        public static Size PAPER_SIZE_B6         = new Size( 5.06,  7.17);
        public static Size PAPER_SIZE_FOOLSCAP   = new Size( 8.50, 13.00);
        public static Size PAPER_SIZE_TABLOID    = new Size(11.00, 17.00);
        public static Size PAPER_SIZE_LEGAL      = new Size( 8.50, 14.00);
        public static Size PAPER_SIZE_LETTER     = new Size( 8.50, 11.00);
        public static Size PAPER_SIZE_STATEMENT  = new Size( 5.50,  8.50);

        // In inches
        public static double MARGIN_PAPER = 0;
        public static double MARGIN_BETWEEN_PAGES = 0.5;

        private PrintSettingConstant() { }

    }
}

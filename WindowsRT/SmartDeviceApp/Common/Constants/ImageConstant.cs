//
//  ImageConstant.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/25.
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
using Windows.Graphics.Display;

namespace SmartDeviceApp.Common.Constants
{
    public class ImageConstant
    {
        public const double BASE_DPI = 96.0;  // 96.0 DPI = 1 DIP
        public static double DpiScaleFactor =
            (int)DisplayInformation.GetForCurrentView().LogicalDpi / BASE_DPI;

        private ImageConstant() { }
    }
}

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

using Windows.Graphics.Display;

namespace SmartDeviceApp.Common.Constants
{
    public static class ImageConstant
    {

        public const double BASE_DPI = 96.0;  // 96.0 DPI = 1 DIP
        public static double DpiScaleFactor =
            (int)DisplayInformation.GetForCurrentView().LogicalDpi / BASE_DPI;

    }
}

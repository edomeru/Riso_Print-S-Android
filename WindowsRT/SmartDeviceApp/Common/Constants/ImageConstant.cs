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

        #region Print Preview Page

        public const double BASE_DPI = 96.0;  // 96.0 DPI = 1 DIP
        public static double DpiScaleFactor =
            (int)DisplayInformation.GetForCurrentView().LogicalDpi / BASE_DPI;

        #endregion Print Preview Page

        #region Default Image Sizes

        private const int ICON_IMAGE_WIDTH_100 = 20;
        private const int ICON_IMAGE_WIDTH_140 = 28;
        private const int ICON_IMAGE_WIDTH_180 = 36;
        private const int ICON_IMAGE_HEIGHT_100 = 20;
        private const int ICON_IMAGE_HEIGHT_140 = 28;
        private const int ICON_IMAGE_HEIGHT_180 = 36;

        private const int RIGHT_BUTTON_IMAGE_WIDTH_100 = 14;
        private const int RIGHT_BUTTON_IMAGE_WIDTH_140 = 20;
        private const int RIGHT_BUTTON_IMAGE_WIDTH_180 = 26;
        private const int RIGHT_BUTTON_IMAGE_HEIGHT_100 = 20;
        private const int RIGHT_BUTTON_IMAGE_HEIGHT_140 = 28;
        private const int RIGHT_BUTTON_IMAGE_HEIGHT_180 = 36;

        public static int IconImageWidth
        {
            get
            {
                var width = 0;
                var resolution = DisplayInformation.GetForCurrentView().ResolutionScale;
                if (resolution == ResolutionScale.Scale100Percent)
                {
                    width = ICON_IMAGE_WIDTH_100;
                }
                if (resolution == ResolutionScale.Scale140Percent)
                {
                    width = ICON_IMAGE_WIDTH_140;
                }
                if (resolution == ResolutionScale.Scale180Percent)
                {
                    width = ICON_IMAGE_WIDTH_180;
                }
                return width;
            }
        }

        public static int IconImageHeight
        {
            get
            {
                var height = 0;
                var resolution = DisplayInformation.GetForCurrentView().ResolutionScale;
                if (resolution == ResolutionScale.Scale100Percent)
                {
                    height = ICON_IMAGE_HEIGHT_100;
                }
                if (resolution == ResolutionScale.Scale140Percent)
                {
                    height = ICON_IMAGE_HEIGHT_140;
                }
                if (resolution == ResolutionScale.Scale180Percent)
                {
                    height = ICON_IMAGE_HEIGHT_180;
                }
                return height;
            }
        }

        public static int RightButtonImageWidth
        {
            get
            {
                var width = 0;
                var resolution = DisplayInformation.GetForCurrentView().ResolutionScale;
                if (resolution == ResolutionScale.Scale100Percent)
                {
                    width = RIGHT_BUTTON_IMAGE_WIDTH_100;
                }
                if (resolution == ResolutionScale.Scale140Percent)
                {
                    width = RIGHT_BUTTON_IMAGE_WIDTH_140;
                }
                if (resolution == ResolutionScale.Scale180Percent)
                {
                    width = RIGHT_BUTTON_IMAGE_WIDTH_180;
                }
                return width;
            }
        }

        public static int RightButtonImageHeight
        {
            get
            {
                var height = 0;
                var resolution = DisplayInformation.GetForCurrentView().ResolutionScale;
                if (resolution == ResolutionScale.Scale100Percent)
                {
                    height = RIGHT_BUTTON_IMAGE_HEIGHT_100;
                }
                if (resolution == ResolutionScale.Scale140Percent)
                {
                    height = RIGHT_BUTTON_IMAGE_HEIGHT_140;
                }
                if (resolution == ResolutionScale.Scale180Percent)
                {
                    height = RIGHT_BUTTON_IMAGE_HEIGHT_180;
                }
                return height;
            }
        }

        #endregion

    }
}

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
using SmartDeviceApp.Controls;
using System;

namespace SmartDeviceApp.Common.Constants
{
    public static class ImageConstant
    {

        #region Print Preview Page

        public const double FACTOR_MM_TO_IN = 0.0393701;
        public const double BASE_DPI = 48;//96.0;  // 96.0 DPI = 1 DIP

        public static double GetDpiScaleFactor()
        {
            try
            {
                return (double)GetLogicalDpi() / BASE_DPI;
            }
            catch (Exception)
            {
                // Error handling
            }

            return 1.0;
        }

        public static int GetLogicalDpi()
        {
            return (int)DisplayInformation.GetForCurrentView().LogicalDpi;
        }

        #endregion Print Preview Page

        #region Default Image Sizes

        private const string JOB_STATUS_IMAGE = "img_btn_job_status";
        private const int STATUS_IMAGE_WIDTH_100  = 34;
        private const int STATUS_IMAGE_HEIGHT_100 = 34;
        private const int STATUS_IMAGE_WIDTH_140  = 48;
        private const int STATUS_IMAGE_HEIGHT_140 = 48;
        private const int STATUS_IMAGE_WIDTH_180  = 62;
        private const int STATUS_IMAGE_HEIGHT_180 = 62;

        private const string COLLAPSE_IMAGE = "img_btn_collapse";
        // TODO: Replace when print setting icons become available
        private const string PRINT_SETTING_IMAGE = "img_btn_dummy_print_setting";
        private const int ICON_IMAGE_WIDTH_100  = 20;
        private const int ICON_IMAGE_HEIGHT_100 = 20;
        private const int ICON_IMAGE_WIDTH_140  = 28;
        private const int ICON_IMAGE_HEIGHT_140 = 28;
        private const int ICON_IMAGE_WIDTH_180  = 36;
        private const int ICON_IMAGE_HEIGHT_180 = 36;

        private const int RIGHT_BUTTON_IMAGE_WIDTH_100  = 14;
        private const int RIGHT_BUTTON_IMAGE_HEIGHT_100 = 20;
        private const int RIGHT_BUTTON_IMAGE_WIDTH_140  = 20;
        private const int RIGHT_BUTTON_IMAGE_HEIGHT_140 = 28;
        private const int RIGHT_BUTTON_IMAGE_WIDTH_180  = 26;
        private const int RIGHT_BUTTON_IMAGE_HEIGHT_180 = 36;

        public static int GetIconImageWidth(object sender, bool? isIgnoreResolution = false)
        {
            int width = 0;
            var resolution = DisplayInformation.GetForCurrentView().ResolutionScale;
            if (isIgnoreResolution != null && isIgnoreResolution.Value == true)
            {
                resolution = ResolutionScale.Scale100Percent;
            }
            var type = sender.GetType();
            if (type == typeof(JobListItemControl) ||
                type == typeof(SelectPrinterItemControl))
            {
                switch (resolution)
                {
                    case ResolutionScale.Scale100Percent:
                        width = STATUS_IMAGE_WIDTH_100;
                        break;
                    case ResolutionScale.Scale140Percent:
                        width = STATUS_IMAGE_WIDTH_140;
                        break;
                    case ResolutionScale.Scale180Percent:
                        width = STATUS_IMAGE_WIDTH_180;
                        break;
                }
            }
            else if (type == typeof(GroupListControl) ||
                type == typeof(KeyValueControl) ||
                type == typeof(KeyRadioButtonControl) ||
                type == typeof(KeyTextBoxControl) ||
                type == typeof(KeyToggleSwitchControl) ||
                type == typeof(PrinterNameControl))
            {
                switch (resolution)
                {
                    case ResolutionScale.Scale100Percent:
                        width = ICON_IMAGE_WIDTH_100;
                        break;
                    case ResolutionScale.Scale140Percent:
                        width = ICON_IMAGE_WIDTH_140;
                        break;
                    case ResolutionScale.Scale180Percent:
                        width = ICON_IMAGE_WIDTH_180;
                        break;
                }
            }
            return width;
        }

        public static int GetRightButtonImageWidth()
        {
            int width = 0;
            var resolution = DisplayInformation.GetForCurrentView().ResolutionScale;
            switch (resolution)
            {
                case ResolutionScale.Scale100Percent:
                    width = RIGHT_BUTTON_IMAGE_WIDTH_100;
                    break;
                case ResolutionScale.Scale140Percent:
                    width = RIGHT_BUTTON_IMAGE_WIDTH_140;
                    break;
                case ResolutionScale.Scale180Percent:
                    width = RIGHT_BUTTON_IMAGE_WIDTH_180;
                    break;
            }
            return width;
        }

        #endregion

    }
}

﻿//
//  PrintSettingConverter.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/07.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Data;

namespace SmartDeviceApp.Converters
{
    public class PrintSettingConverter
    {

        public class ColorModeToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                ColorMode? convertedValue = null;

                if (Enum.IsDefined(typeof(ColorMode), intValue))
                {
                    convertedValue = (ColorMode)intValue;
                }

                return convertedValue;
            }
        }

        public class OrientationToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                Orientation? convertedValue = null;

                if (Enum.IsDefined(typeof(Orientation), intValue))
                {
                    convertedValue = (Orientation)intValue;
                }

                return convertedValue;
            }
        }

        public class DuplexToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                Duplex? convertedValue = null;

                if (Enum.IsDefined(typeof(Duplex), intValue))
                {
                    convertedValue = (Duplex)intValue;
                }

                return convertedValue;
            }
        }

        public class PaperSizeToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                PaperSize? convertedValue = null;

                if (Enum.IsDefined(typeof(PaperSize), intValue))
                {
                    convertedValue = (PaperSize)intValue;
                }

                return convertedValue;
            }
        }

        public class PaperTypeToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                PaperType? convertedValue = null;

                if (Enum.IsDefined(typeof(PaperType), intValue))
                {
                    convertedValue = (PaperType)intValue;
                }

                return convertedValue;
            }
        }

        public class InputTrayToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                InputTray? convertedValue = null;

                if (Enum.IsDefined(typeof(InputTray), intValue))
                {
                    convertedValue = (InputTray)intValue;
                }

                return convertedValue;
            }
        }

        public class ImpositionToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                Imposition? convertedValue = null;

                if (Enum.IsDefined(typeof(Imposition), intValue))
                {
                    convertedValue = (Imposition)intValue;
                }

                return convertedValue;
            }
        }

        public class ImpositionIntToNumberOfPagesConverter
        {

            public static int Convert(int value)
            {
                int pagesPerSheet = 1;
                switch (value)
                {
                    case (int)Imposition.TwoUp:
                        pagesPerSheet = 2;
                        break;
                    case (int)Imposition.FourUp:
                        pagesPerSheet = 4;
                        break;
                    case (int)Imposition.Off:
                    default:
                        pagesPerSheet = 1;
                        break;
                }

                return pagesPerSheet;
            }

        }

        public class ImpositionOrderToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                ImpositionOrder? convertedValue = null;

                if (Enum.IsDefined(typeof(ImpositionOrder), intValue))
                {
                    convertedValue = (ImpositionOrder)intValue;
                }

                return convertedValue;
            }
        }

        public class SortToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                Sort? convertedValue = null;

                if (Enum.IsDefined(typeof(Sort), intValue))
                {
                    convertedValue = (Sort)intValue;
                }

                return convertedValue;
            }
        }

        public class BookletFinishingToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                BookletFinishing? convertedValue = null;

                if (Enum.IsDefined(typeof(BookletFinishing), intValue))
                {
                    convertedValue = (BookletFinishing)intValue;
                }

                return convertedValue;
            }
        }

        public class BookletLayoutToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                BookletLayout? convertedValue = null;

                if (Enum.IsDefined(typeof(BookletLayout), intValue))
                {
                    convertedValue = (BookletLayout)intValue;
                }

                return convertedValue;
            }
        }

        public class FinishingSideToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                FinishingSide? convertedValue = null;

                if (Enum.IsDefined(typeof(FinishingSide), intValue))
                {
                    convertedValue = (FinishingSide)intValue;
                }

                return convertedValue;
            }
        }

        public class StapleToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                Staple? convertedValue = null;

                if (Enum.IsDefined(typeof(Staple), intValue))
                {
                    convertedValue = (Staple)intValue;
                }

                return convertedValue;
            }
        }

        public class PunchToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                Punch? convertedValue = null;

                if (Enum.IsDefined(typeof(Punch), intValue))
                {
                    convertedValue = (Punch)intValue;
                }

                return convertedValue;
            }
        }

        public class OutputTrayToIntConverter : BasePrintSettingConverter.BasePrintSettingToIntConverter
        {

            new public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                int intValue = (int)value;
                OutputTray? convertedValue = null;

                if (Enum.IsDefined(typeof(OutputTray), intValue))
                {
                    convertedValue = (OutputTray)intValue;
                }

                return convertedValue;
            }
        }

    }


}

/*
﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Data;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.Converters
{
    public class PageNumberFormatConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {   
            var pageNumber = value as PageNumberInfo;
            string formattedPageNumber = String.Empty;
            if (pageNumber.ViewMode == PageViewMode.SinglePageView)
            {
                formattedPageNumber = (pageNumber.RightPageIndex + 1).ToString();
            }
            else if (pageNumber.ViewMode == PageViewMode.TwoPageView)
            {
                formattedPageNumber = String.Format("{0} - {1}", pageNumber.LeftPageIndex + 1, pageNumber.RightPageIndex + 1);
            }
            
            var loader = new Windows.ApplicationModel.Resources.ResourceLoader();
            var pageNumberFormat = loader.GetString("IDS_LBL_PAGE_NUMBER");
            formattedPageNumber = String.Format(pageNumberFormat, formattedPageNumber, pageNumber.PageTotal);

            return formattedPageNumber;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
*/

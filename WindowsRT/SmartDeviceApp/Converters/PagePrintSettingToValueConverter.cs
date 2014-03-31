//
//  PagePrintSettingToValueConverter.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/07.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using Windows.UI.Xaml.Data;

namespace SmartDeviceApp.Converters
{
    public class PagePrintSettingToValueConverter : IValueConverter
    {

        private Dictionary<string, Type> _printSettingNameMap = new Dictionary<string, Type>()
        {
            {PrintSettingConstant.KEY_COLOR_MODE, typeof(ColorMode)},
            {PrintSettingConstant.KEY_ORIENTATION, typeof(Orientation)},
            {PrintSettingConstant.KEY_COPIES, typeof(int)},
            {PrintSettingConstant.KEY_DUPLEX, typeof(Duplex)},
            {PrintSettingConstant.KEY_PAPER_SIZE, typeof(PaperSize)},
            {PrintSettingConstant.KEY_SCALE_TO_FIT, typeof(bool)},
            {PrintSettingConstant.KEY_PAPER_TYPE, typeof(PaperType)},
            {PrintSettingConstant.KEY_INPUT_TRAY, typeof(InputTray)},
            {PrintSettingConstant.KEY_IMPOSITION, typeof(Imposition)},
            {PrintSettingConstant.KEY_IMPOSITION_ORDER, typeof(ImpositionOrder)},
            {PrintSettingConstant.KEY_SORT, typeof(Sort)},
            {PrintSettingConstant.KEY_BOOKLET, typeof(bool)},
            {PrintSettingConstant.KEY_BOOKLET_FINISHING, typeof(BookletFinishing)},
            {PrintSettingConstant.KEY_BOOKLET_LAYOUT, typeof(BookletLayout)},
            {PrintSettingConstant.KEY_FINISHING_SIDE, typeof(FinishingSide)},
            {PrintSettingConstant.KEY_STAPLE, typeof(Staple)},
            {PrintSettingConstant.KEY_PUNCH, typeof(Punch)},
            {PrintSettingConstant.KEY_OUTPUT_TRAY, typeof(OutputTray)}
        };

        public object Convert(object value, Type targetType, object parameter, string language)
        {
            string printSettingName = (string)parameter;
            if (value == null || parameter == null || string.IsNullOrEmpty(value.ToString()) ||
                string.IsNullOrEmpty(printSettingName.ToString()))
            {
                return null;
            }

            //PagePrintSetting pagePrintSettings = value as PagePrintSetting;

            int intValue;
            bool boolValue;

            int.TryParse((string)value, out intValue);
            bool.TryParse((string)value, out boolValue);

            // Convert to native type values
            if (printSettingName.Equals(PrintSettingConstant.KEY_COPIES))
            {
                return intValue;
            }
            else if (printSettingName.Equals(PrintSettingConstant.KEY_SCALE_TO_FIT))
            {
                return boolValue;
            }
            else if (printSettingName.Equals(PrintSettingConstant.KEY_BOOKLET))
            {
                return boolValue;
            }

            // Convert to enum values
            Type type = null;
            _printSettingNameMap.TryGetValue(printSettingName, out type);
            if (type != null)
            {
                if (type.Equals(typeof(ColorMode)))
                {
                    return intValue;
                }
                else if (type.Equals(typeof(Orientation)))
                {
                    return intValue;
                }
                else if (type.Equals(typeof(Duplex)))
                {
                    return intValue;
                }
                else if (type.Equals(typeof(PaperSize)))
                {
                    return intValue;
                }
                else if (type.Equals(typeof(PaperType)))
                {
                    return intValue;
                }
                else if (type.Equals(typeof(InputTray)))
                {
                    return intValue;
                }
                else if (type.Equals(typeof(Imposition)))
                {
                    return intValue;
                }
                else if (type.Equals(typeof(ImpositionOrder)))
                {
                    return intValue;
                }
                else if (type.Equals(typeof(Sort)))
                {
                    return intValue;
                }
                else if (type.Equals(typeof(BookletFinishing)))
                {
                    return intValue;
                }
                else if (type.Equals(typeof(BookletLayout)))
                {
                    return intValue;
                }
                else if (type.Equals(typeof(FinishingSide)))
                {
                    return intValue;
                }
                else if (type.Equals(typeof(Staple)))
                {
                    return intValue;
                }
                else if (type.Equals(typeof(Punch)))
                {
                    return intValue;
                }
                else if (type.Equals(typeof(OutputTray)))
                {
                    return intValue;
                }
            }

            // Unknown type
            return null;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }

    }
}

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
using SmartDeviceApp.Common.Enum;
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
            {PrintSettingConstant.NAME_VALUE_COLOR_MODE, typeof(ColorMode)},
            {PrintSettingConstant.NAME_VALUE_ORIENTATION, typeof(Orientation)},
            {PrintSettingConstant.NAME_VALUE_COPIES, typeof(int)},
            {PrintSettingConstant.NAME_VALUE_DUPLEX, typeof(Duplex)},
            {PrintSettingConstant.NAME_VALUE_PAPER_SIZE, typeof(PaperSize)},
            {PrintSettingConstant.NAME_VALUE_SCALE_TO_FIT, typeof(bool)},
            {PrintSettingConstant.NAME_VALUE_PAPER_TYPE, typeof(PaperType)},
            {PrintSettingConstant.NAME_VALUE_INPUT_TRAY, typeof(InputTray)},
            {PrintSettingConstant.NAME_VALUE_IMPOSITION, typeof(Imposition)},
            {PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER,  typeof(ImpositionOrder)},
            {PrintSettingConstant.NAME_VALUE_SORT, typeof(Sort)},
            {PrintSettingConstant.NAME_VALUE_BOOKLET, typeof(bool)},
            {PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING, typeof(BookletFinishing)},
            {PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT, typeof(BookletLayout)},
            {PrintSettingConstant.NAME_VALUE_FINISHING_SIDE, typeof(FinishingSide)},
            {PrintSettingConstant.NAME_VALUE_STAPLE, typeof(Staple)},
            {PrintSettingConstant.NAME_VALUE_PUNCH, typeof(Punch)},
            {PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY, typeof(OutputTray)}
        };

        public object Convert(object value, Type targetType, object parameter, string language)
        {
            string printSettingName = (string)parameter;
            if (value == null || parameter == null || string.IsNullOrEmpty(value.ToString()) ||
                string.IsNullOrEmpty(printSettingName.ToString()))
            {
                return null;
            }

            int intValue;
            bool boolValue;

            int.TryParse((string)value, out intValue);
            bool.TryParse((string)value, out boolValue);

            // Convert to native type values
            if (printSettingName.Equals(PrintSettingConstant.NAME_VALUE_COPIES))
            {
                return intValue;
            }
            else if (printSettingName.Equals(PrintSettingConstant.NAME_VALUE_SCALE_TO_FIT))
            {
                return boolValue;
            }
            else if (printSettingName.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET))
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

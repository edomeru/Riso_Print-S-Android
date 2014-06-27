//
//  PrintSettingToValueConverter.cs
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
    public class PrintSettingToValueConverter : IValueConverter
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
            {PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY, typeof(OutputTray)},
            {PrintSettingConstant.NAME_VALUE_SECURE_PRINT, typeof(bool)},
            {PrintSettingConstant.NAME_VALUE_PIN_CODE, typeof(string)}
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
            else if (printSettingName.Equals(PrintSettingConstant.NAME_VALUE_SCALE_TO_FIT) ||
                printSettingName.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET) ||
                printSettingName.Equals(PrintSettingConstant.NAME_VALUE_SECURE_PRINT))
            {
                return boolValue;
            }
            else if (printSettingName.Equals(PrintSettingConstant.NAME_VALUE_PIN_CODE))
            {
                return value.ToString();
            }

            // Convert to enum values
            Type type = null;
            _printSettingNameMap.TryGetValue(printSettingName, out type);

            object result = null;
            if (type != null)
            {
                if (type.Equals(typeof(ColorMode)) ||
                    type.Equals(typeof(Orientation)) ||
                    type.Equals(typeof(Duplex)) ||
                    type.Equals(typeof(PaperSize)) ||
                    type.Equals(typeof(PaperType)) ||
                    type.Equals(typeof(InputTray)) ||
                    type.Equals(typeof(Imposition)) ||
                    type.Equals(typeof(ImpositionOrder)) ||
                    type.Equals(typeof(Sort)) ||
                    type.Equals(typeof(BookletFinishing)) ||
                    type.Equals(typeof(BookletLayout)) ||
                    type.Equals(typeof(FinishingSide)) ||
                    type.Equals(typeof(Staple)) ||
                    type.Equals(typeof(Punch)) ||
                    type.Equals(typeof(OutputTray)))
                {
                    result = intValue;
                }
            }

            // Unknown type
            return result;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }

    }
}

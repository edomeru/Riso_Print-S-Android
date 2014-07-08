//
//  PrinterSearchItemButtonImageConveter.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/05/01
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
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media.Imaging;

namespace SmartDeviceApp.Converters
{
    public class PrinterSearchItemButtonImageConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is bool))
                return DependencyProperty.UnsetValue;            
            bool isInPrinterList = (bool)value;
            if (isInPrinterList)
            {
                return "ms-appx:///Resources/Images/img_btn_add_printer_search_ok.png";                
            }
            return "ms-appx:///Resources/Images/img_btn_add_printer_normal.png";
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}

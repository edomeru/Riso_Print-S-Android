//
//  SelectedPrinterIdToTextConverter.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/05/08
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;

namespace SmartDeviceApp.Converters
{
    public class SelectedPrinterIdToTextConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            try
            {
                int printerId = -1;
                if (value != null && (value is int))
                {
                    printerId = (int)value;
                }

                string printerName = new ViewModelLocator().PrintSettingsViewModel.PrinterName;
                if (printerId > -1 && !string.IsNullOrEmpty(printerName))
                {
                    return printerName;
                }

                string resourceId = (printerId == -1) ? "IDS_LBL_CHOOSE_PRINTER" : "IDS_LBL_NO_NAME";
                var loader = new Windows.ApplicationModel.Resources.ResourceLoader();
                var text = loader.GetString(resourceId);
                if (String.IsNullOrEmpty(text))
                {
                    throw new ArgumentException(LogUtility.ERROR_RESOURCE_STRING_NOT_FOUND,
                        value.ToString());
                }
                return text;
            }
            catch (Exception ex)
            {
                LogUtility.LogError(ex);
                return String.Empty;
            }
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}

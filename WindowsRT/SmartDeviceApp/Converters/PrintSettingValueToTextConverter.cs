using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media.Imaging;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.Converters
{
    public class PrintSettingValueToTextConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is PrintSetting)) return String.Empty;
            var printSetting = (PrintSetting)value;

            switch (printSetting.Type)
            {
                case PrintSettingType.boolean:
                    break;
                case PrintSettingType.numeric:
                    if (printSetting.Value == null || !(printSetting.Value is int)) return String.Empty;
                    return printSetting.Value.ToString();
                case PrintSettingType.list:
                    var options = printSetting.Options;
                    if (printSetting.Value == null || options == null) return String.Empty;
                    var optionText = options[(int)printSetting.Value].Text;
                    var textConverter = new ResourceStringToTextConverter();
                    return textConverter.Convert(optionText, null, null, null);
                case PrintSettingType.unknown:
                    break;
            }
            return String.Empty;
            
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            // TODO: 3/28 IMPLEMENT THIS: TWO-WAY BINDING FOR VALUE -> NEED TO SET IN VIEWMODEL!!

            throw new NotImplementedException();
        }
    }
}

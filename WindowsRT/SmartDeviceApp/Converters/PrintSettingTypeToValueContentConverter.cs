using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Data;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Models;

namespace SmartDeviceApp.Converters
{
    public class PrintSettingTypeToValueContentConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || String.IsNullOrEmpty(value.ToString())) return null;
            object control = null;
            object style;
            PrintSettingType type;
            var success = Enum.TryParse<PrintSettingType>(value.ToString(), out type);
            if (!success) return null;
            else
            {
                switch (type)
                {
                    case PrintSettingType.numeric:
                        var textBox = new TextBox();
                        style = Application.Current.Resources["STYLE_ListValue_TextBox"];
                        if (style != null && style.GetType() == typeof(Style))
                        {
                            textBox.Style = (Style)style;
                        }
                        control = textBox;
                        break;

                    case PrintSettingType.boolean:
                        var toggleSwitch = new ToggleSwitch();
                        style = Application.Current.Resources["STYLE_ListValue_ToggleSwitch"];
                        if (style != null && style.GetType() == typeof(Style))
                        {
                            toggleSwitch.Style = (Style)style;
                        }
                        control = toggleSwitch;
                        break;
                    case PrintSettingType.list:
                        break;
                }
            }

            return control;

        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}

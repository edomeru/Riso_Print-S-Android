using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using SmartDeviceApp.Views;
using SmartDeviceApp.Common.Base;
using SmartDeviceApp.Controls;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Models;

namespace SmartDeviceApp.Common.Utilities
{
    public class PrintSettingUtility : DependencyObject
    {
        public static readonly DependencyProperty PrintSettingValueChangedProperty =
            DependencyProperty.RegisterAttached("PrintSettingValueChanged", typeof(object), typeof(PrintSettingUtility),
            new PropertyMetadata(null, NotifyPrintSettingValueChanged));

        public static void SetPrintSettingValueChanged(DependencyObject obj, object value)
        {
            obj.SetValue(PrintSettingValueChangedProperty, value);
        }

        public static object GetPrintSettingValueChanged(DependencyObject obj)
        {
            return (object)obj.GetValue(PrintSettingValueChangedProperty);
        }

        private static void NotifyPrintSettingValueChanged(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (obj != null && e.NewValue != null)
            {
                PrintSetting printSetting = null;
                if (obj is KeyToggleSwitchControl)
                {
                    printSetting = (PrintSetting)((KeyToggleSwitchControl)obj).DataContext;
                }
                else if (obj is KeyValueControl)
                {
                    printSetting = (PrintSetting)((KeyValueControl)obj).DataContext;
                }
                else if (obj is KeyTextBoxControl)
                {
                    printSetting = (PrintSetting)((KeyTextBoxControl)obj).DataContext;
                }
                object value = e.NewValue;

                // LESTER'S TODO: Raise event for controller here
            }
        }
    }
}

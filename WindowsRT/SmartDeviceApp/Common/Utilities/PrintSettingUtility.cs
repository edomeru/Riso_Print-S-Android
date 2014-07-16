//
//  PrintSettingUtility.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/04/08.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Controllers;
using SmartDeviceApp.Controls;
using SmartDeviceApp.Models;
using Windows.UI.Xaml;

namespace SmartDeviceApp.Common.Utilities
{
    public class PrintSettingUtility : DependencyObject
    {
        public static event SmartDeviceApp.Controllers.PrintSettingsController.PrintSettingValueChangedEventHandler PrintSettingValueChangedEventHandler;

        public static readonly DependencyProperty PrintSettingValueChangedProperty =
            DependencyProperty.RegisterAttached("PrintSettingValueChanged", typeof(object),
            typeof(PrintSettingUtility), new PropertyMetadata(null, NotifyPrintSettingValueChanged));

        /// <summary>
        /// Sets the print setting value.
        /// </summary>
        /// <param name="obj">print setting</param>
        /// <param name="value">new value</param>
        public static void SetPrintSettingValueChanged(DependencyObject obj, object value)
        {
            obj.SetValue(PrintSettingValueChangedProperty, value);
        }

        /// <summary>
        /// Gets the print setting value
        /// </summary>
        /// <param name="obj">print setting</param>
        /// <returns>value of print setting</returns>
        public static object GetPrintSettingValueChanged(DependencyObject obj)
        {
            return (object)obj.GetValue(PrintSettingValueChangedProperty);
        }

        private static void NotifyPrintSettingValueChanged(DependencyObject obj,
            DependencyPropertyChangedEventArgs e)
        {
            if (obj != null && e.NewValue != null)
            {
                PrintSetting printSetting = null;
                if (obj is KeyValueControl)
                {
                    printSetting = (PrintSetting)((KeyValueControl)obj).DataContext;
                }
                object value = e.NewValue;

                if (PrintSettingValueChangedEventHandler != null)
                {
                    PrintSettingValueChangedEventHandler(printSetting, value);
                }
            }
        }
    }
}

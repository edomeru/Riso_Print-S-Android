//
//  MiddleTrimmedTextConverter.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/08/11.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.ViewModels;
using System;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;

namespace SmartDeviceApp.Converters
{

    public class TitleToMiddleTrimmedTextConverter : IValueConverter
    {
        /// <summary>
        /// Returns the middle trimmed text of a title text
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is string) || String.IsNullOrEmpty(value.ToString()) ||
                !(parameter is double))
            {
                return String.Empty;
            }

            String text = value as string;
            Style style = (Style)Application.Current.Resources["STYLE_TextHeader"];

            double actualWidth = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(text, style);
            double desiredWidth = (double)parameter;
            if (actualWidth > desiredWidth)
            {
                return ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(text,
                    style, desiredWidth);
            }

            return text; // Return as is
        }

        /// <summary>
        /// Not implemented.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }


    public class JobListItemKeyTextConverter : IValueConverter
    {
        /// <summary>
        /// Returns the trimmed text of a Job List Item
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is string) || String.IsNullOrEmpty(value.ToString()))
            {
                return String.Empty;
            }

            String text = value as string;
            Style style = (Style)Application.Current.Resources["STYLE_TextKeyNoTextTrim"];

            double actualWidth = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(text, style);
            double desiredWidth = new ViewModelLocator().JobsViewModel.KeyTextWidth;
            if (actualWidth <= desiredWidth)
            {
                return text; // No text trimming
            }

            text = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(text, style, desiredWidth);
            return text;
        }

        /// <summary>
        /// Not implemented.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }


    public class JobGroupListTextConverter : IValueConverter
    {
        /// <summary>
        /// Returns the trimmed text of a Group Job List Text
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is string) || !(parameter is double))
            {
                return String.Empty;
            }

            String text = (string)new PrinterNameToTextConverter().Convert(value, null, null, null);
            Style style = (Style)Application.Current.Resources["STYLE_TextListHeaderNoTextTrim"];

            double actualWidth = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(text, style);
            double desiredWidth = (double)parameter;
            if (actualWidth <= desiredWidth)
            {
                return text; // No text trimming
            }

            text = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(text, style, desiredWidth);
            return text;
        }

        /// <summary>
        /// Not implemented.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }


    public class JobGroupListSubTextConverter : IValueConverter
    {
        /// <summary>
        /// Returns the trimmed text of a Group Job List Sub Text
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is string) || String.IsNullOrEmpty(value.ToString()) ||
                !(parameter is double))
            {
                return String.Empty;
            }

            String text = value as string;
            Style style = (Style)Application.Current.Resources["STYLE_TextKeySubTextNoTextTrim"];

            double actualWidth = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(text, style);
            double desiredWidth = (double)parameter;
            if (actualWidth <= desiredWidth)
            {
                return text; // No text trimming
            }

            text = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(text, style, desiredWidth);
            return text;
        }

        /// <summary>
        /// Not implemented.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }


    public class PrinterValueTextMiddleTrimmedTextConverter : IValueConverter
    {
        /// <summary>
        /// Returns the trimmed text of a Value text (for Default/ Print Settings Screen)
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null)
            {
                return String.Empty;
            }

            bool isSelectPrinter = false;
            if (parameter != null)
            {
                isSelectPrinter = System.Convert.ToBoolean(parameter);
            }

            String text;
            if (isSelectPrinter)
            {
                if (!(value is int))
                {
                    return String.Empty;
                }

                text = (string)new SelectedPrinterIdToTextConverter().Convert(value, null, null, null);
            }
            else
            {
                if (!(value is string))
                {
                    return String.Empty;
                }

                text = (string)new PrinterNameToTextConverter().Convert(value, null, null, null);
            }

            Style style = (Style)Application.Current.Resources["STYLE_TextValueWithSubTextNoTextTrim"];

            double actualWidth = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(text, style);
            double desiredWidth = new ViewModelLocator().PrintSettingsViewModel.PrinterValueTextWidth;
            if (actualWidth <= desiredWidth)
            {
                return text; // No text trimming
            }

            text = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(text, style, desiredWidth);
            return text;
        }

        /// <summary>
        /// Not implemented.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }


    public class PrinterValueSubTextMiddleTrimmedTextConverter : IValueConverter
    {
        /// <summary>
        /// Returns the trimmed text of a ValueSubText (for Default/ Print Settings Screen)
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is string) || String.IsNullOrEmpty(value.ToString()))
            {
                return String.Empty;
            }

            String text = value as string;
            Style style = (Style)Application.Current.Resources["STYLE_TextValueSubTextNoTextTrim"];

            double actualWidth = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(text, style);
            double desiredWidth = new ViewModelLocator().PrintSettingsViewModel.PrinterValueTextWidth;
            if (actualWidth <= desiredWidth)
            {
                return text; // No text trimming
            }

            text = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(text, style, desiredWidth);
            return text;
        }

        /// <summary>
        /// Not implemented.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }


    public class PrinterNameMiddleTrimmedTextConverter : IValueConverter
    {
        /// <summary>
        /// Returns the trimmed text of a printer name (for Printers Screen)
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is string) || parameter == null || !(parameter is double))
            {
                return String.Empty;
            }

            String text = (string)value;// (string)new PrinterNameToTextConverter().Convert(value, null, null, null);
            Style style = (Style)Application.Current.Resources["STYLE_TextKeyNoTextTrim"];

            double actualWidth = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(text, style);
            double desiredWidth = (double)parameter;
            if (actualWidth <= desiredWidth)
            {
                return text; // No text trimming
            }

            text = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(text, style, desiredWidth);
            return text;
        }

        /// <summary>
        /// Not implemented.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }


    public class KeyTextMiddleTrimmedTextConverter : IValueConverter
    {
        /// <summary>
        /// Returns the trimmed text of a Key text (generic)
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is string) || String.IsNullOrEmpty(value.ToString()) ||
                parameter == null || !(parameter is double))
            {
                return String.Empty;
            }

            String text = (string)value;
            Style style = (Style)Application.Current.Resources["STYLE_TextKeyNoTextTrim"];

            double actualWidth = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(text, style);
            double desiredWidth = (double)parameter;
            if (actualWidth <= desiredWidth)
            {
                return text; // No text trimming
            }

            text = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(text, style, desiredWidth);
            return text;
        }

        /// <summary>
        /// Not implemented.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }


    public class KeySubTextMiddleTrimmedTextConverter : IValueConverter
    {
        /// <summary>
        /// Returns the trimmed text of a Key sub text (generic)
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is string) || String.IsNullOrEmpty(value.ToString()) ||
                parameter == null || !(parameter is double))
            {
                return String.Empty;
            }

            String text = (string)value;
            Style style = (Style)Application.Current.Resources["STYLE_TextKeySubTextNoTextTrim"];

            double actualWidth = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(text, style);
            double desiredWidth = (double)parameter;
            if (actualWidth <= desiredWidth)
            {
                return text; // No text trimming
            }

            text = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(text, style, desiredWidth);
            return text;
        }

        /// <summary>
        /// Not implemented.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }


    public class ValueTextMiddleTrimmedTextConverter : IValueConverter
    {
        /// <summary>
        /// Returns the trimmed text of a Value text (generic)
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is string) || String.IsNullOrEmpty(value.ToString()) ||
                parameter == null || !(parameter is double))
            {
                return String.Empty;
            }

            String text = (string)value;
            Style style = (Style)Application.Current.Resources["STYLE_TextValueNoTextTrim"];

            double actualWidth = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(text, style);
            double desiredWidth = (double)parameter;
            if (actualWidth <= desiredWidth)
            {
                return text; // No text trimming
            }

            text = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(text, style, desiredWidth);
            return text;
        }

        /// <summary>
        /// Not implemented.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }


}

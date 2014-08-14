//
//  PrinterSearchListItemControl.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/08/14.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;

namespace SmartDeviceApp.Controls
{
    public partial class PrinterSearchListItemControl : KeyValueControl
    {

        /// <summary>
        /// PrinterSearchListItemControl class constructor
        /// </summary>
        public PrinterSearchListItemControl()
        {
            this.InitializeComponent();

            this.Loaded += OnLoaded;
        }

        private void OnLoaded(object sender, RoutedEventArgs e)
        {
            if (Visibility == Visibility.Collapsed) return;

            var printerSearchListItemControl = (PrinterSearchListItemControl)sender;

            // Adjust widths
            var defaultMargin = (double)Application.Current.Resources["MARGIN_Default"];
            var smallMargin = (double)Application.Current.Resources["MARGIN_Small"];

            // Get text width by subtracting widths and margins of visible components
            var keyValueControlWidth = (int)printerSearchListItemControl.ActualWidth;
            if (keyValueControlWidth <= 0)
            {
                var parent = (FrameworkElement)printerSearchListItemControl.Parent;
                if (parent != null)
                {
                    keyValueControlWidth = (int)parent.ActualWidth;
                    if (keyValueControlWidth <= 0)
                    {
                        return;
                    }
                }
                else
                {
                    return;
                }
            }
            int maxTextWidth = keyValueControlWidth;

            // Left and right margins
            maxTextWidth -= ((int)defaultMargin * 2);

            // RightButton is visible
            if (RightButtonVisibility == Visibility.Visible)
            {
                var rightButtonImageWidth = ImageConstant.GetRightButtonImageWidth(sender);
                maxTextWidth -= rightButtonImageWidth;
                maxTextWidth -= (int)defaultMargin;
                maxTextWidth -= (int)smallMargin; // Additional width
            }
            
            KeyTextWidth = maxTextWidth;

            // Set separator start point
            if (SeparatorVisibility == Visibility.Visible)
            {
                SeparatorStartPoint = ViewControlUtility.GetSeparatorStartPoint(sender,
                                                                IsListItem, IconVisibility);
            }

            // Change style of KeyText and KeySubText to No Text Trimming
            TextBlock keyTextBlock = ViewControlUtility.GetTextBlockFromParent((UIElement)sender, "key"); // "key" as defined in KeyValueControl.xaml
            keyTextBlock.Style = (Style)Application.Current.Resources["STYLE_TextKeyNoTextTrim"];
            TextBlock keySubTextBlock = ViewControlUtility.GetTextBlockFromParent((UIElement)sender, "keySubText"); // "keySubText" as defined in KeyValueControl.xaml
            keySubTextBlock.Style = (Style)Application.Current.Resources["STYLE_TextKeySubTextNoTextTrim"];

            // Update displayed texts, not source properties
            keyTextBlock.Text = (string)new KeyTextMiddleTrimmedTextConverter().Convert(Text, null, KeyTextWidth, null);
            keySubTextBlock.Text = (string)new KeySubTextMiddleTrimmedTextConverter().Convert(SubText, null, KeyTextWidth, null);
        }

    }
}

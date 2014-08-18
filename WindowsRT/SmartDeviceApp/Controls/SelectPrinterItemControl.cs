//
//  SelectPrinterItemControl.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/05/26.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;

namespace SmartDeviceApp.Controls
{
    public sealed class SelectPrinterItemControl : KeyRadioButtonControl
    {
        /// <summary>
        /// Constructor for SelectPrinterItemControl.
        /// </summary>
        public SelectPrinterItemControl()
        {
            this.InitializeComponent();

            this.Loaded += OnLoaded;
        }

        private void OnLoaded(object sender, RoutedEventArgs e)
        {
            TextBlock keyTextBlock = (TextBlock)ViewControlUtility.GetControlFromParent<TextBlock>((UIElement)sender, "key"); // "key" as defined in KeyValueControl.xaml
            TextBlock keySubTextBlock = (TextBlock)ViewControlUtility.GetControlFromParent<TextBlock>((UIElement)sender, "keySubText"); // "keySubText" as defined in KeyValueControl.xaml

            // Change style of KeyText and KeySubText to No Text Trim
            if (SubTextVisibility == Visibility.Visible)
            {
                keyTextBlock.Style = (Style)Application.Current.Resources["STYLE_TextKeyWithSubTextNoTextTrim"];
            }
            else
            {
                keyTextBlock.Style = (Style)Application.Current.Resources["STYLE_TextKeyNoTextTrim"];
            }
            keySubTextBlock.Style = (Style)Application.Current.Resources["STYLE_TextKeySubTextNoTextTrim"];

            // Update displayed texts, not source properties
            keyTextBlock.Text = (string)new KeyTextMiddleTrimmedTextConverter().Convert(Text, null, KeyTextWidth, null);
            keySubTextBlock.Text = (string)new KeySubTextMiddleTrimmedTextConverter().Convert(SubText, null, KeyTextWidth, null);
        }
    }
}

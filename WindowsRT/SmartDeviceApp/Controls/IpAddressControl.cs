//
//  IpAddressControl.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/08/14.
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
    public partial class IpAddressControl : KeyValueControl
    {

        /// <summary>
        /// IpAddressControl class constructor
        /// </summary>
        public IpAddressControl()
        {
            this.InitializeComponent();

            this.SizeChanged += OnSizeChanged;
        }

        private void OnSizeChanged(object sender, SizeChangedEventArgs e)
        {
            var ipAddressControl = (IpAddressControl)sender;
            var ipAddressControlWidth = e.NewSize.Width;

            TextBlock valueTextBlock = (TextBlock)ViewControlUtility.GetControlFromParent<TextBlock>((UIElement)sender, "value"); // "key" as defined in KeyValueControl.xaml

            var defaultMargin = (double)Application.Current.Resources["MARGIN_Default"];
            var smallMargin = (double)Application.Current.Resources["MARGIN_Small"];

            // Compute length of "IP Address" title label
            var keyTextStyle = (Style)Application.Current.Resources["STYLE_TextKey"];
            var keyTextWidth = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(Text, keyTextStyle);

            int maxKeyTextWidth = (int)keyTextWidth + (int)smallMargin;
            int maxValueTextWidth = (int)ipAddressControlWidth - maxKeyTextWidth - ((int)defaultMargin * 2) - (int)smallMargin;

            KeyTextWidth = maxKeyTextWidth;
            ValueTextWidth = maxValueTextWidth;

            // Update displayed value text, not ValueText property
            valueTextBlock.Text = (string)new ValueTextMiddleTrimmedTextConverter().Convert(ValueText, null, ValueTextWidth, null);
        }

    }
}

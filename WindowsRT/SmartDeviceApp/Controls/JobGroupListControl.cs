//
//  JobGroupListControl.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/07/11.
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
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;

namespace SmartDeviceApp.Controls
{
    public partial class JobGroupListControl : GroupListControl
    {

        /// <summary>
        /// Constructor of JobGroupListControl.
        /// </summary>
        public JobGroupListControl()
        {
            this.InitializeComponent();

            this.SizeChanged += OnSizeChanged;
        }

        private void OnSizeChanged(object sender, SizeChangedEventArgs e)
        {
            if (e.PreviousSize != e.NewSize) // Workaround to avoid layout cycle (cause not found, maybe on base class OnSizeChanged)
            {
                // Change style of KeyText to No Text Trimming
                var keyTextBlock = (TextBlock)ViewControlUtility.GetControlFromParent<TextBlock>((UIElement)sender, "key"); // "key" as defined in GroupListControl.xaml
                keyTextBlock.Style = (Style)Application.Current.Resources["STYLE_TextListHeaderWithSubTextNoTextTrim"];

                // Change style of KeySubText to No Text Trimming
                var keySubTextBlock = (TextBlock)ViewControlUtility.GetControlFromParent<TextBlock>((UIElement)sender, "keySubText"); // "keySubText" as defined in GroupListControl.xaml
                keySubTextBlock.Style = (Style)Application.Current.Resources["STYLE_TextKeySubTextNoTextTrim"];

                // Trim texts
                keyTextBlock.Text = (string)new JobGroupListTextConverter().Convert(Text,
                    null, TextWidth, null);
                keySubTextBlock.Text = (string)new JobGroupListSubTextConverter().Convert(SubText,
                    null, TextWidth, null);
            }
        }

    }
}

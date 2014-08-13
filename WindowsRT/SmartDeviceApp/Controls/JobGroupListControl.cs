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

            // Override binding
            Binding binding = new Binding();
            binding.Path = new PropertyPath("IsCollapsed");
            Header.SetBinding(ToggleButton.IsCheckedProperty, binding);

            this.Loaded += OnLoaded;
        }

        public static readonly DependencyProperty IsCollapsedProperty =
            DependencyProperty.Register("IsCollapsed", typeof(bool), typeof(JobGroupListControl), null);

        /// <summary>
        /// Flag for checking whether the Group List is collapsed or not.
        /// </summary>
        public bool IsCollapsed
        {
            get { return (bool)GetValue(IsCollapsedProperty); }
            set { SetValue(IsCollapsedProperty, value); }
        }

        private void OnLoaded(object sender, RoutedEventArgs e)
        {
            // Change style of KeyText to No Text Trimming
            var keyTextBlock = ViewControlUtility.GetTextBlockFromParent((UIElement)sender, "key"); // "key" as defined in GroupListControl.xaml
            keyTextBlock.Style = (Style)Application.Current.Resources["STYLE_TextListHeaderNoTextTrim"];

            // Change style of KeySubText to No Text Trimming
            var keySubTextBlock = ViewControlUtility.GetTextBlockFromParent((UIElement)sender, "keySubText"); // "keySubText" as defined in GroupListControl.xaml
            keySubTextBlock.Style = (Style)Application.Current.Resources["STYLE_TextKeySubTextNoTextTrim"];

            // Trim texts
            keyTextBlock.Text = (string)new JobGroupListTextConverter().Convert(Text,
                null, TextWidth, null);
            keySubTextBlock.Text = (string)new JobGroupListSubTextConverter().Convert(SubText,
                null, TextWidth, null);
        }

    }
}

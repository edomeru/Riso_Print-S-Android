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

using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;

namespace SmartDeviceApp.Controls
{
    public partial class JobGroupListControl : GroupListControl
    {

        public JobGroupListControl()
        {
            this.InitializeComponent();

            // Override binding
            Binding binding = new Binding();
            binding.Path = new PropertyPath("IsCollapsed");
            Header.SetBinding(ToggleButton.IsCheckedProperty, binding);
        }

        public static readonly DependencyProperty IsCollapsedProperty =
            DependencyProperty.Register("IsCollapsed", typeof(bool), typeof(JobGroupListControl), null);

        public bool IsCollapsed
        {
            get { return (bool)GetValue(IsCollapsedProperty); }
            set { SetValue(IsCollapsedProperty, value); }
        }

    }
}

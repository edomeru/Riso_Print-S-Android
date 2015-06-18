//
//  HomePage.xaml.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/02/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using SmartDeviceApp.Common.Base;
using SmartDeviceApp.Controls;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Controllers;

namespace SmartDeviceApp.Views
{
    public sealed partial class HomePage : PageBase
    {
        /// <summary>
        /// Constructor. Initializes UI components.
        /// </summary>
        public HomePage()
        {
            this.InitializeComponent();
        }

        private void Grid_Loaded(object sender, RoutedEventArgs e)
        {
            ((Grid)sender).Visibility = Windows.UI.Xaml.Visibility.Collapsed;
            ViewModel.HomeGestureGrid = (Grid)sender;
            ViewModel.triggerOnLoaded();
        }
        
        /// <summary>
        /// Gets the data context of this xaml
        /// </summary>
        public HomeViewModel ViewModel
        {
            get
            {
                return (HomeViewModel)DataContext;
            }
        }
    }
}

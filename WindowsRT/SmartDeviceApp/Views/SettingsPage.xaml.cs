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
using SmartDeviceApp.Controls;
using SmartDeviceApp.Common.Base;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceApp.Views
{
    public sealed partial class SettingsPage : PageBase
    {
        /// <summary>
        /// Constructor. Initializes UI components.
        /// </summary>
        public SettingsPage()
        {
            this.InitializeComponent();
        }

        /// <summary>
        /// Gets the data context of this xaml
        /// </summary>
        public SettingsViewModel ViewModel
        {
            get
            {
                return (SettingsViewModel)DataContext;
            }
        }

        private void Grid_Loaded(object sender, RoutedEventArgs e)
        {
            ((Grid)sender).Visibility = Windows.UI.Xaml.Visibility.Collapsed;
            ViewModel.SettingsGestureGrid = (Grid)sender;
        }
    }
}

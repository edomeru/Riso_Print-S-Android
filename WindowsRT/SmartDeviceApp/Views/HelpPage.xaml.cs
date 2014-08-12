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
using Windows.Storage;
using SmartDeviceApp.Common.Utilities;

namespace SmartDeviceApp.Views
{
    public sealed partial class HelpPage : PageBase
    {
        /// <summary>
        /// Constructor for the HelpPage class
        /// </summary>
        public HelpPage()
        {
            this.InitializeComponent();
        }

        private void Grid_Loaded(object sender, RoutedEventArgs e)
        {
            ((Grid)sender).Visibility = Windows.UI.Xaml.Visibility.Collapsed;
            ViewModel.HelpGestureGrid = (Grid)sender;

        }

        /// <summary>
        /// Gets the data context of this xaml
        /// </summary>
        public HelpViewModel ViewModel
        {
            get
            {
                return (HelpViewModel)DataContext;
            }
        }

        private async void WebView_Loaded(object sender, RoutedEventArgs e)
        {
            StorageFile MyWebPageFile = await StorageFileUtility.GetFileFromAppResource("Assets/help.html");
            string MyWebPageString = await FileIO.ReadTextAsync(MyWebPageFile);
            ((WebView)sender).NavigateToString(MyWebPageString);
        }
    }
}

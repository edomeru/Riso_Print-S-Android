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
        private bool isAlreadyLoaded = false;
        private WebView webview;
        public HelpPage()
        {
            this.InitializeComponent();
        }

        private void Grid_Loaded(object sender, RoutedEventArgs e)
        {
           ((Grid)sender).Visibility = Windows.UI.Xaml.Visibility.Collapsed;
           //ViewModel.HelpGestureGrid = (Grid)sender;

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
        /// <summary>
        /// // Load HTML referring to image files
        /// </summary>
        private void WebView_Loaded(object sender, RoutedEventArgs e)
        {
            webview = ((WebView)sender);
            if (!isAlreadyLoaded)
            {
                isAlreadyLoaded = true;
                ((WebView)sender).Navigate(new Uri("ms-appx-web:///Assets/help.html"));
            }
            
        }

        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {
            webview.InvokeScriptAsync("eval", new string[] { "scroll(0, 0);" });
            webview.Refresh();
        }
    }
}

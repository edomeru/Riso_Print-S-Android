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
using Windows.Storage;
using SmartDeviceApp.Common.Utilities;
using Windows.ApplicationModel;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceApp.Views
{
    public sealed partial class LegalPage : PageBase
    {
        private bool isAlreadyLoaded = false;
        private WebView webview;
        /// <summary>
        /// Constructor. Initializes UI components.
        /// </summary>
        public LegalPage()
        {
            this.InitializeComponent();
        }

        /// <summary>
        /// // Load HTML referring to image files
        /// </summary>
        private void WebView_Loaded(object sender, RoutedEventArgs e)
        {
            webview = (WebView)sender;
            if (!isAlreadyLoaded)
            {
                isAlreadyLoaded = true;
                ((WebView)sender).Navigate(new Uri("ms-appx-web:///Assets/legal.html"));
            }
        }

        /// <summary>
        /// // Automatically set version
        /// </summary>
        private void WebView_DOMContentLoaded(WebView sender, WebViewDOMContentLoadedEventArgs e)
        {
            PackageVersion version = Package.Current.Id.Version;
            string versionString = string.Format("var v = document.getElementById(\"localize_version\"); if (v != null) v.innerHTML = \"Ver.{0}.{1}.{2}.{3}\"; ", version.Major, version.Minor, version.Build, version.Revision);

            webview = ((WebView)sender);
            webview.InvokeScriptAsync("eval", new string[] { versionString });
        }


        private void Grid_Loaded(object sender, RoutedEventArgs e)
        {
            ((Grid)sender).Visibility = Windows.UI.Xaml.Visibility.Collapsed;
            //ViewModel.LegalGestureGrid = (Grid)sender;

        }

        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {
            webview.InvokeScriptAsync("eval", new string[] { "scroll(0, 0);" });
            webview.Refresh();
        }

        /// <summary>
        /// Gets the data context of this xaml
        /// </summary>
        public LegalViewModel ViewModel
        {
            get
            {
                return (LegalViewModel)DataContext;
            }
        }
    }
}

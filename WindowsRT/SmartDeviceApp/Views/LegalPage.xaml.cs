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
        /// <summary>
        /// Constructor. Initializes UI components.
        /// </summary>
        public LegalPage()
        {
            this.InitializeComponent();
        }

        private async void WebView_Loaded(object sender, RoutedEventArgs e)
        {
            StorageFile MyWebPageFile = await StorageFileUtility.GetFileFromAppResource("Assets/legal.html");
            string MyWebPageString = await FileIO.ReadTextAsync(MyWebPageFile);
            string ScriptTagString = "<h2 id=\"localize_version\">Ver.";
            int IndexOfScriptTag = MyWebPageString.IndexOf(ScriptTagString);
            int LengthOfScriptTag = ScriptTagString.Length - 1;

            PackageVersion version = Package.Current.Id.Version;
            string versionString = string.Format("{0}.{1}.{2}.{3}", version.Major, version.Minor, version.Build, version.Revision);

            MyWebPageString = MyWebPageString.Insert(IndexOfScriptTag + LengthOfScriptTag + 1, versionString);
            ((WebView)sender).NavigateToString(MyWebPageString);

        }

        private void Grid_Loaded(object sender, RoutedEventArgs e)
        {
            ((Grid)sender).Visibility = Windows.UI.Xaml.Visibility.Collapsed;
            ViewModel.LegalGestureGrid = (Grid)sender;

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

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
using SmartDeviceApp.ViewModels;
using Microsoft.Practices.ServiceLocation;

namespace SmartDeviceApp.Views
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class PrintSettingsPane : UserControl
    {
        /// <summary>
        /// Constructor. Initializes UI components.
        /// </summary>
        public PrintSettingsPane()
        {
            this.InitializeComponent();
        }

        /// <summary>
        /// Holds the data context for this xaml.
        /// </summary>
        public PrintSettingsPaneViewModel ViewModel
        {
            get
            {
                return (PrintSettingsPaneViewModel)DataContext;
            }
        }

        private void printSettingsPaneLoaded(object sender, RoutedEventArgs e)
        {
            var viewControl = ServiceLocator.Current.GetInstance<ViewControlViewModel>();
            ViewModel.Height = viewControl.ScreenBound.Height;
           
        }
    }
}

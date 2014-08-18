using SmartDeviceApp.Controls;
using SmartDeviceApp.ViewModels;
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

namespace SmartDeviceApp.Views
{
    public sealed partial class PrintSettingsBox : Grid
    {
        /// <summary>
        /// Constructor. Initializes UI components.
        /// </summary>
        public PrintSettingsBox()
        {
            this.InitializeComponent();
        }

        /// <summary>
        /// Holds the data context for this xaml.
        /// </summary>
        public PrintSettingsViewModel ViewModel
        {
            get
            {
                return (PrintSettingsViewModel)DataContext;
            }
        }

        private void OnPrinterButtonSizeChanged(object sender, RoutedEventArgs e)
        {
            ViewModel.OnPrinterControlSizeChanged((KeyValueControl)sender);
        }
    }
}

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
using System.Collections.ObjectModel;
using SmartDeviceApp.Models;
using System.Windows.Input;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceApp.Views
{
    public sealed partial class SearchPrinterPane : UserControl
    {
        public SearchPrinterPane()
        {
            Messenger.Default.Register<PrinterSearchRefreshState>(this, (refreshState) => OnSetRefreshState(refreshState));

            this.InitializeComponent();

        }

        public SearchPrinterViewModel ViewModel
        {
            get
            {
                return (SearchPrinterViewModel)DataContext;
            }
        }

        private void OnSetRefreshState(PrinterSearchRefreshState refreshState)
        {
            if (refreshState == PrinterSearchRefreshState.RefreshingState)
            {
                VisualStateManager.GoToState(this, "RefreshingState", true);
            }
            else
            {
                VisualStateManager.GoToState(this, "NotRefreshingState", true);
            }
        }

        private void OnLoaded(object sender, RoutedEventArgs e)
        {
            var vBar = ((FrameworkElement)VisualTreeHelper.GetChild(printerSearchListView.ElementScrollViewer, 0)).FindName("VerticalScrollBar") as ScrollBar;
            
            this.printerSearchListView.SetVBar(vBar);
        }

    }
}

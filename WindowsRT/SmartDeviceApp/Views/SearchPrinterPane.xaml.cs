﻿using System;
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
using SmartDeviceApp.Converters;

namespace SmartDeviceApp.Views
{
    public sealed partial class SearchPrinterPane : UserControl
    {
        /// <summary>
        /// Constructor for SearchPrinterPane class.
        /// </summary>
        public SearchPrinterPane()
        {
            this.InitializeComponent();
            Messenger.Default.Register<PrinterSearchRefreshState>(this, (refreshState) => OnSetRefreshState(refreshState));
        }

        /// <summary>
        /// Holds the data context of this xaml.
        /// </summary>
        public SearchPrinterViewModel ViewModel
        {
            get
            {
                return (SearchPrinterViewModel)DataContext;
            }
        }

        private async void OnSetRefreshState(PrinterSearchRefreshState refreshState)
        {
            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
            Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
            {
                if (refreshState == PrinterSearchRefreshState.RefreshingState)
                {
                    VisualStateManager.GoToState(this, "RefreshingState", true);
                }
                else
                {
                    VisualStateManager.GoToState(this, "NotRefreshingState", true);
                }
            });
        }

        private void OnLoaded(object sender, RoutedEventArgs e)
        {
            var vBar = ((FrameworkElement)VisualTreeHelper.GetChild(printerSearchListView.ElementScrollViewer, 0)).FindName("VerticalScrollBar") as ScrollBar;
            var vBar2 = ((FrameworkElement)VisualTreeHelper.GetChild(NoPrintersFoundView.ElementScrollViewer, 0)).FindName("VerticalScrollBar") as ScrollBar;
            
            this.printerSearchListView.SetVBar(vBar);
            this.NoPrintersFoundView.SetVBar(vBar2);
            ViewModel.Height = (double)((new SidePanesHeightConverter()).Convert(this, null, null, null));
            if (!ViewModel.WillRefresh)
            {
                OnSetRefreshState(PrinterSearchRefreshState.NotRefreshingState);
            }
            
        }

    }
}

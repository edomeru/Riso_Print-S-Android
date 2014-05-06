﻿using SmartDeviceApp.Common;
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
using SmartDeviceApp.Models;
using System.Collections.ObjectModel;
using SmartDeviceApp.ViewModels;
using Windows.UI;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using Windows.UI.Popups;

using SmartDeviceApp.Common.Utilities;
using Windows.UI.Xaml.Media.Imaging;

using SmartDeviceApp.Controls;
using SmartDeviceApp.Common.Base;
using SmartDeviceApp.Controllers;


// The Basic Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234237

namespace SmartDeviceApp.Views
{
    /// <summary>
    /// A basic page that provides characteristics common to most applications.
    /// </summary>
    public sealed partial class PrintersPage : PageBase
    {
        public PrintersViewModel ViewModel
        {
            get
            {
                return (PrintersViewModel)DataContext;
            }
        }

        private PrintersGestureController _gestureController;

        public PrintersPage()
        {
            Messenger.Default.Register<MessageAlert>(
             this,
             msg => ShowDialog(msg));



            this.InitializeComponent();

            _gestureController = new PrintersGestureController();
            ViewModel.GestureController = _gestureController;
        }

        /// <summary>
        /// Populates the page with content passed during navigation. Any saved state is also
        /// provided when recreating a page from a prior session.
        /// </summary>
        /// <param name="sender">
        /// The source of the event; typically <see cref="NavigationHelper"/>
        /// </param>
        /// <param name="e">Event data that provides both the navigation parameter passed to
        /// <see cref="Frame.Navigate(Type, Object)"/> when this page was initially requested and
        /// a dictionary of state preserved by this page during an earlier
        /// session. The state will be null the first time a page is visited.</param>
        private void navigationHelper_LoadState(object sender, LoadStateEventArgs e)
        {
        }

        /// <summary>
        /// Preserves state associated with this page in case the application is suspended or the
        /// page is discarded from the navigation cache.  Values must conform to the serialization
        /// requirements of <see cref="SuspensionManager.SessionState"/>.
        /// </summary>
        /// <param name="sender">The source of the event; typically <see cref="NavigationHelper"/></param>
        /// <param name="e">Event data that provides an empty dictionary to be populated with
        /// serializable state.</param>
        private void navigationHelper_SaveState(object sender, SaveStateEventArgs e)
        {

        }

        #region NavigationHelper registration

        /// The methods provided in this section are simply used to allow
        /// NavigationHelper to respond to the page's navigation methods.
        /// 
        /// Page specific logic should be placed in event handlers for the  
        /// <see cref="GridCS.Common.NavigationHelper.LoadState"/>
        /// and <see cref="GridCS.Common.NavigationHelper.SaveState"/>.
        /// The navigation parameter is available in the LoadState method 
        /// in addition to page state preserved during an earlier session.



        #endregion

       
        private async void ShowDialog(MessageAlert msg)
        {
            MessageDialog md = new MessageDialog(msg.Content, msg.Caption);
            await md.ShowAsync();
        }

        private void printerInfoView_Loaded(object sender, RoutedEventArgs e)
        {
            _gestureController.TargetControl = (AdaptableGridView)sender;
            
        }

        private void PrintersGestureGrid_Loaded(object sender, RoutedEventArgs e)
        {
            _gestureController.Control = (Grid)sender;

            _gestureController.EnableGestures();
        }

        private void ScrollViewer_Loaded(object sender, RoutedEventArgs e)
        {
            _gestureController.ControlReference = (ScrollViewer)sender;

        }

    }
}

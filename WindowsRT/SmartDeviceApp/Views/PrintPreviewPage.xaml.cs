//
//  PrintPreviewPage.xaml.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/02/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

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
using System.Diagnostics;
using SmartDeviceApp.Common.Base;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Controls;
using Windows.UI.Input;
using SmartDeviceApp.Controllers;

namespace SmartDeviceApp.Views
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class PrintPreviewPage : PageBase
    {
        private GestureController _gestureController;
        
        public PrintPreviewPage()
        {
            this.InitializeComponent();
        }

        public PrintPreviewViewModel ViewModel
        {
            get
            {
                return (PrintPreviewViewModel)DataContext;
            }
        }

        // Note: Need to access TwoPageControl this way 
        // Otherwise, getting null value from PrintPreviewPage constructor
        // because TwoPageControl is a child of another user control
        private void OnPageAreaLoaded(object sender, RoutedEventArgs e)
        {
            // Initialize gesture controller
            var twoPageControl = (TwoPageControl)sender;
            var pageAreaGrid = twoPageControl.PageAreaGrid;
          
            // Save page height to be used in resizing page images
            var scalingFactor = pageAreaGrid.ActualHeight / ViewModel.RightPageActualSize.Height;

            var pageAreaScrollViewer = (UIElement)pageAreaGrid.Parent; // TODO create dependency property??
            _gestureController = new GestureController(pageAreaGrid, pageAreaScrollViewer, 
                ViewModel.RightPageActualSize, scalingFactor,
                new GestureController.SwipeRightDelegate(SwipeRight), 
                new GestureController.SwipeLeftDelegate(SwipeLeft));

            // TODO: Two-page view handling
        }

        private void SwipeRight()
        {
            ViewModel.GoToPreviousPage.Execute(null);
        }

        private void SwipeLeft()
        {
            ViewModel.GoToNextPage.Execute(null);
        }

        private void ResetTransforms(object sender, RoutedEventArgs e)
        {
            _gestureController.ResetTransforms();
        }
    }
}

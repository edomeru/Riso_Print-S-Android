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
using System.Diagnostics;
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
using Windows.UI.Input;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Base;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Controls;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Common.Enum;


namespace SmartDeviceApp.Views
{
    public sealed partial class PrintPreviewPage : PageBase
    {        
        public PrintPreviewPage()
        {
            // TODO: Verify if this is acceptable for MVVM
            Messenger.Default.Register<PreviewViewMode>(this, (previewViewMode) => OnSetPreviewViewMode(previewViewMode));
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
            ViewModel.SetPageAreaGrid(pageAreaGrid);
        }

        private void ResetTransforms(object sender, RoutedEventArgs e)
        {
            ViewModel._gestureController.ResetTransforms();
        }

        // Note: Cannot set this in ViewModel because need to get this object
        // for VisualStateManager.GoToState
        private void OnSetPreviewViewMode(PreviewViewMode previewViewMode)
        {
            switch (previewViewMode)
            {
                case PreviewViewMode.MainMenuPaneVisible:
                {
                    VisualStateManager.GoToState(this, "MainMenuPaneVisibleState", true);
                    break;
                }

                case PreviewViewMode.PreviewViewFullScreen:
                {
                    VisualStateManager.GoToState(this, "PreviewViewFullScreenState", true);
                    break;
                }

                case PreviewViewMode.RightPaneVisible:
                {
                    VisualStateManager.GoToState(this, "RightPaneVisibleState", true);
                    break;
                }
            }
        }
    }
}

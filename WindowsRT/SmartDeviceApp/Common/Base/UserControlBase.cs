﻿//
//  UserControlBase.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/02/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using Windows.Graphics.Display;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Threading;

namespace SmartDeviceApp.Common.Base
{
    public abstract class UserControlBase : UserControl
    {
        /// <summary>
        /// Maximum width for snap view.
        /// </summary>
        public abstract double SnapViewMaximumWidth
        {
            get;
        }

        /// <summary>
        /// Constructor for UserControlBase
        /// </summary>
        public UserControlBase()
        {
            if (!ViewModelBase.IsInDesignModeStatic)
            {
                Loaded += UserControlBaseLoaded;
            }
        }

        private void CheckOrientation()
        {
            if (!ViewModelBase.IsInDesignModeStatic)
            {
                if (ActualWidth < SnapViewMaximumWidth)
                {
                    if (DisplayInformation.GetForCurrentView().CurrentOrientation != DisplayOrientations.Portrait
                        && DisplayInformation.GetForCurrentView().CurrentOrientation
                        != DisplayOrientations.PortraitFlipped)
                    {
                        VisualStateManager.GoToState(this, "OrientationSnap", true);
                    }
                }
                else
                {
                    switch (DisplayInformation.GetForCurrentView().CurrentOrientation)
                    {
                        case DisplayOrientations.Portrait:
                        case DisplayOrientations.PortraitFlipped:
                            VisualStateManager.GoToState(this, "OrientationPortrait", true);
                            break;

                        default:
                            VisualStateManager.GoToState(this, "OrientationLandscape", true);
                            break;
                    }
                }
            }
        }

        private void UserControlBaseLoaded(object sender, RoutedEventArgs e)
        {
            if (!ViewModelBase.IsInDesignModeStatic)
            {
                SizeChanged += UserControlBaseSizeChanged;
                DisplayInformation.GetForCurrentView().OrientationChanged += UserControlBaseOrientationChanged;
                DispatcherHelper.RunAsync(CheckOrientation);
            }
        }

        private void UserControlBaseOrientationChanged(DisplayInformation sender, object args)
        {
            CheckOrientation();
        }

        private void UserControlBaseSizeChanged(object sender, SizeChangedEventArgs e)
        {
            if (!ViewModelBase.IsInDesignModeStatic)
            {
                CheckOrientation();
            }
        }
    }
}
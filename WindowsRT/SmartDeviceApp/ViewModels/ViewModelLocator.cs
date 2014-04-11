//
//  ViewModelLocator.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/02/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System.Diagnostics.CodeAnalysis;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Ioc;
using Microsoft.Practices.ServiceLocation;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Design;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceApp.ViewModel
{
    /// <summary>
    /// This class contains static references to all the view models in the
    /// application and provides an entry point for the bindings.
    /// <para>
    /// See http://www.galasoft.ch/mvvm
    /// </para>
    /// </summary>
    public class ViewModelLocator
    {
        /// <summary>
        /// Gets the Main property.
        /// </summary>
        [SuppressMessage("Microsoft.Performance",
            "CA1822:MarkMembersAsStatic",
            Justification = "This non-static member is needed for data binding purposes.")]
        public PrintPreviewViewModel PrintPreviewViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<PrintPreviewViewModel>();
            }
        }

        public PrintersViewModel PrintersViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<PrintersViewModel>();
            }
        }

        public PrintSettingsViewModel PrintSettingsViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<PrintSettingsViewModel>();
            }
        }

        public PrintSettingOptionsViewModel PrintSettingOptionsViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<PrintSettingOptionsViewModel>();
            }
        }

        static ViewModelLocator()
        {
            ServiceLocator.SetLocatorProvider(() => SimpleIoc.Default);

            if (ViewModelBase.IsInDesignModeStatic)
            {
                SimpleIoc.Default.Register<IDataService, DesignDataService>();
            }
            else
            {
                SimpleIoc.Default.Register<IDataService, DataService>();
            }

            SimpleIoc.Default.Register<IDialogService, DialogService>();
            SimpleIoc.Default.Register<INavigationService, NavigationService>();
            SimpleIoc.Default.Register<PrintPreviewViewModel>();
            SimpleIoc.Default.Register<PrintersViewModel>();
            SimpleIoc.Default.Register<PrintSettingsViewModel>();
            SimpleIoc.Default.Register<PrintSettingOptionsViewModel>();
        }

        /// <summary>
        /// Cleans up all the resources.
        /// </summary>
        public static void Cleanup()
        {
        }
    }
}
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

namespace SmartDeviceApp.ViewModels
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
        /// Gets the Main property
        /// </summary>
        [SuppressMessage("Microsoft.Performance",
            "CA1822:MarkMembersAsStatic",
            Justification = "This non-static member is needed for data binding purposes.")]
        public ViewControlViewModel ViewControlViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<ViewControlViewModel>();
            }
        }

        /// <summary>
        /// Gets the HomeViewModel instance
        /// </summary>
        public HomeViewModel HomeViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<HomeViewModel>();
            }
        }

        /// <summary>
        /// Gets the PrintPreviewViewModel instance
        /// </summary>
        public PrintPreviewViewModel PrintPreviewViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<PrintPreviewViewModel>();
            }
        }

        /// <summary>
        /// Gets the PrintersViewModel instance
        /// </summary>
        public PrintersViewModel PrintersViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<PrintersViewModel>();
            }
        }

        /// <summary>
        /// Gets the JobsViewModel instance
        /// </summary>
        public JobsViewModel JobsViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<JobsViewModel>();
            }
        }

        /// <summary>
        /// Gets the SettingsViewModel instance
        /// </summary>
        public SettingsViewModel SettingsViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<SettingsViewModel>();
            }
        }

        /// <summary>
        /// Gets the HelpViewModel instance
        /// </summary>
        public HelpViewModel HelpViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<HelpViewModel>();
            }
        }

        /// <summary>
        /// Gets the LegalViewModel instance
        /// </summary>
        public LegalViewModel LegalViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<LegalViewModel>();
            }
        }

        /// <summary>
        /// Gets the LicenseViewModel instance
        /// </summary>
        public LicenseViewModel LicenseViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<LicenseViewModel>();
            }
        }

        /// <summary>
        /// Gets the PrintSettingsPaneViewModel instance
        /// </summary>
        public PrintSettingsPaneViewModel PrintSettingsPaneViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<PrintSettingsPaneViewModel>();
            }
        }

        /// <summary>
        /// Gets the PrintSettingsViewModel instance
        /// </summary>
        public PrintSettingsViewModel PrintSettingsViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<PrintSettingsViewModel>();
            }
        }

        /// <summary>
        /// Gets the SelectPrinterViewModel instance
        /// </summary>
        public SelectPrinterViewModel SelectPrinterViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<SelectPrinterViewModel>();
            }
        }

        /// <summary>
        /// Gets the PrintSettingOptionsViewModel instance
        /// </summary>
        public PrintSettingOptionsViewModel PrintSettingOptionsViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<PrintSettingOptionsViewModel>();
            }
        }

        /// <summary>
        /// Gets the SearchPrinterViewModel instance
        /// </summary>
        public SearchPrinterViewModel SearchPrinterViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<SearchPrinterViewModel>();
            }
        }

        /// <summary>
        /// Gets the SearchSettingsViewModel instance
        /// </summary>
        public SearchSettingsViewModel SearchSettingsViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<SearchSettingsViewModel>();
            }
        }

        /// <summary>
        /// Gets the AddPrinterViewModel instance
        /// </summary>
        public AddPrinterViewModel AddPrinterViewModel
        {
            get
            {
                return ServiceLocator.Current.GetInstance<AddPrinterViewModel>();
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
            SimpleIoc.Default.Register<ViewControlViewModel>();
            SimpleIoc.Default.Register<HomeViewModel>();
            SimpleIoc.Default.Register<PrintPreviewViewModel>();
            SimpleIoc.Default.Register<PrintersViewModel>();
            SimpleIoc.Default.Register<JobsViewModel>();
            SimpleIoc.Default.Register<SettingsViewModel>();
            SimpleIoc.Default.Register<HelpViewModel>();
            SimpleIoc.Default.Register<LegalViewModel>();
            SimpleIoc.Default.Register<LicenseViewModel>();
            SimpleIoc.Default.Register<PrintSettingsPaneViewModel>();
            SimpleIoc.Default.Register<PrintSettingsViewModel>();
            SimpleIoc.Default.Register<SelectPrinterViewModel>();
            SimpleIoc.Default.Register<PrintSettingOptionsViewModel>();
            SimpleIoc.Default.Register<SearchPrinterViewModel>();
            SimpleIoc.Default.Register<SearchSettingsViewModel>();
            SimpleIoc.Default.Register<AddPrinterViewModel>();
        }

        /// <summary>
        /// Cleans up all the resources.
        /// </summary>
        public static void Cleanup()
        {
        }
    }
}
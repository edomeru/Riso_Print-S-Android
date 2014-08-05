//
//  PrintSettingsPaneViewModel.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/20.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Threading.Tasks;
using System.Xml.Serialization;
using System.Xml.Linq;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.ApplicationModel;
using GalaSoft.MvvmLight;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;
using GalaSoft.MvvmLight.Messaging;
using Windows.ApplicationModel.Resources;
using Windows.UI.Xaml;
using SmartDeviceApp.Converters;

namespace SmartDeviceApp.ViewModels
{
    public class PrintSettingsPaneViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private bool _isEnabled = true;
        private PrintSettingsPaneMode _printSettingsPaneMode;

        private ResourceLoader _resourceLoader;

        private String _paneTitle;

        private double _height;
        private ViewOrientation _viewOrientation;

        /// <summary>
        /// PrintSettingsPaneViewModel class constructor
        /// </summary>
        /// <param name="dataService">data service</param>
        /// <param name="navigationService">navigation service</param>
        public PrintSettingsPaneViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
             _resourceLoader = new ResourceLoader();
            Messenger.Default.Register<ViewMode>(this, (viewMode) => SetPrintSettingsPaneMode(viewMode));
            Messenger.Default.Register<ViewOrientation>(this, (viewOrientation) => ResetPrintSettingsPane(viewOrientation));
            SetPaneTitle();
            
        }

        /// <summary>
        /// Gets/sets the enabled state of this view
        /// </summary>
        public bool IsEnabled
        {
            get { return _isEnabled; }
            set
            {
                if (_isEnabled != value)
                {
                    _isEnabled = value;
                }
            }
        }

        /// <summary>
        /// Gets/sets the currently active PrintSettingsPaneMode
        /// </summary>
        public PrintSettingsPaneMode PrintSettingsPaneMode
        {
            get { return _printSettingsPaneMode; }
            set
            {
                if (_printSettingsPaneMode != value)
                {
                    _printSettingsPaneMode = value;
                    RaisePropertyChanged("PrintSettingsPaneMode");
                }
            }
        }

        private void SetPrintSettingsPaneMode(ViewMode viewMode)
        {
            var screenMode = new ViewModelLocator().ViewControlViewModel.ScreenMode;
            if (screenMode != ScreenMode.Home && // For Open-In: since during loading of page, Home screen is displayed
                screenMode != ScreenMode.PrintPreview &&
                screenMode != ScreenMode.Printers)
            {
                return;
            }
            if (viewMode == ViewMode.FullScreen && PrintSettingsPaneMode != PrintSettingsPaneMode.PrintSettings)
            {
                PrintSettingsPaneMode = PrintSettingsPaneMode.PrintSettings;
            }
            SetPaneTitle();
        }

        private void SetPaneTitle()
        {
            var screenMode = new ViewModelLocator().ViewControlViewModel.ScreenMode;
            if (screenMode == ScreenMode.Printers)
            {
                PaneTitle = _resourceLoader.GetString("IDS_LBL_DEFAULT_PRINT_SETTINGS");
            }

            if (screenMode == ScreenMode.PrintPreview)
            {
                PaneTitle = _resourceLoader.GetString("IDS_LBL_PRINT_SETTINGS");
            }
        }

        /// <summary>
        /// Gets/sets the title text of the view
        /// </summary>
        public String PaneTitle
        {
            get { return _paneTitle; }
            set
            {
                _paneTitle = value;
                RaisePropertyChanged("PaneTitle");
            }
        }

        /// <summary>
        /// Holds the value for the height of the PrintSettings pane.
        /// </summary>
        public double Height
        {
            get { return this._height; }
            set
            {
                _height = value;
                RaisePropertyChanged("Height");

            }
        }


        private void ResetPrintSettingsPane(ViewOrientation viewOrientation)
        {
            var titleHeight = ((GridLength)Application.Current.Resources["SIZE_TitleBarHeight"]).Value;
            Height = (double)((new HeightConverter()).Convert(viewOrientation, null, null, null)) - titleHeight;

            ViewOrientation = viewOrientation;
        }

        /// <summary>
        /// Gets/sets the current view orientation
        /// </summary>
        public ViewOrientation ViewOrientation
        {
            get { return _viewOrientation; }
            set
            {
                if (_viewOrientation != value)
                {
                    _viewOrientation = value;
                    RaisePropertyChanged("ViewOrientation");
                }
            }
        }
    }
}
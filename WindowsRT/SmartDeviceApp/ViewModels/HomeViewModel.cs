//
//  HomeViewModel.cs
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
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using Windows.UI.Xaml;
using Windows.Storage.Pickers;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Controllers;
using Windows.UI.Xaml.Controls;

namespace SmartDeviceApp.ViewModels
{
    public class HomeViewModel : ViewModelBase
    {
        private const string PDF_EXTENSION = ".pdf";

        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;
        private ViewControlViewModel _viewControlViewModel;

        private ICommand _openDocumentCommand;
        private bool _isProgressRingActive;

        /// <summary>
        /// HomeViewModel class constructor
        /// </summary>
        /// <param name="dataService">data service</param>
        /// <param name="navigationService">navigation service</param>
        public HomeViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
            IsProgressRingActive = false;
            Messenger.Default.Register<ViewMode>(this, (viewMode) => EnableMode(viewMode));
        }

        /// <summary>
        /// Command for open document
        /// </summary>
        public ICommand OpenDocumentCommand
        {
            get
            {
                if (_openDocumentCommand == null)
                {
                    _openDocumentCommand = new RelayCommand(
                        () => OpenDocumentCommandExecute(),
                        () => EnabledOpenDocumentCommand
                    );
                }
                return _openDocumentCommand;
            }
        }

        /// <summary>
        /// True when loading indicator is active, false otherwise
        /// </summary>
        public bool IsProgressRingActive
        {
            get { return _isProgressRingActive; }
            set
            {
                if (_isProgressRingActive != value)
                {
                    _isProgressRingActive = value;
                    RaisePropertyChanged("IsProgressRingActive");
                    // for change in open document command
                    RaisePropertyChanged("EnabledOpenDocumentCommand");
                }
            }
        }

        /// <summary>
        /// Enables/disables open document command from Home Screen
        /// Should be disabled when loading document from open-in function
        /// </summary>
        public bool EnabledOpenDocumentCommand
        {
            get
            {   /// this is the reverse value of progress ring active 
                /// if progress ring is active disable open document command 
                return !_isProgressRingActive; 
            }
        }

        private async void OpenDocumentCommandExecute()
        {
            try
            {
                FileOpenPicker openPicker = new FileOpenPicker();
                openPicker.SuggestedStartLocation = PickerLocationId.Desktop;
                openPicker.ViewMode = Windows.Storage.Pickers.PickerViewMode.List;

                openPicker.FileTypeFilter.Clear();
                openPicker.FileTypeFilter.Add(PDF_EXTENSION);

                Windows.Storage.StorageFile file = await openPicker.PickSingleFileAsync();
                if (file != null)
                {
                    IsProgressRingActive = true;
                    Windows.Storage.Streams.IRandomAccessStream fileStream =
                        await file.OpenAsync(Windows.Storage.FileAccessMode.Read);
                    await MainController.FileActivationHandler(file);
                    if (DocumentController.Instance.Result == LoadDocumentResult.Successful)
                    {
                        new ViewModelLocator().ViewControlViewModel.EnabledGoToHomeExecute = true;
                        new ViewModelLocator().ViewControlViewModel.GoToHomePage.Execute(null);
                        new ViewModelLocator().ViewControlViewModel.EnabledGoToHomeExecute = false;
                    }
                    IsProgressRingActive = false;
                }
            }
            catch (Exception ex)
            {
                IsProgressRingActive = false;
                LogUtility.LogError(ex);
                DialogService.Instance.ShowError("IDS_ERR_MSG_OPEN_FAILED", "IDS_APP_NAME", "IDS_LBL_OK", null);
            }
        }

        private void EnableMode(ViewMode viewMode)
        {
            if (viewMode == ViewMode.FullScreen)
            {
                if (HomeGestureGrid != null)
                {
                    HomeGestureGrid.Visibility = Windows.UI.Xaml.Visibility.Collapsed;
                }
            }
            else
            {
                if (HomeGestureGrid != null)
                {
                    HomeGestureGrid.Visibility = Windows.UI.Xaml.Visibility.Visible;
                }
            }
        }

        /// <summary>
        /// Grid control for enabling/disabling gestures on Home Screen
        /// </summary>
        public Grid HomeGestureGrid
        {
            get;
            set;
        }
    }
}
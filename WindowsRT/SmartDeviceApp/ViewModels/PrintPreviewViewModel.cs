//
//  PrintPreviewViewModel.cs
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
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using Windows.UI.Xaml.Media.Imaging;
using Windows.UI.Xaml;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.DummyControllers;
using Windows.UI.Xaml.Controls;
using Windows.Foundation;

namespace SmartDeviceApp.ViewModels
{
    public class PrintPreviewViewModel : ViewModelBase
    {
        private const bool IS_SINGLE_PAGE = true;

        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ICommand _goToPage;
        private ICommand _goToPreviousPage;
        private ICommand _goToNextPage;
        private ICommand _pageNumberSliderValueChange;
        private ObservableCollection<PreviewPageViewModel> _previewPages;
        private uint _pageTotal;
        private uint _currentPageIndex;
        private uint _rightPageIndex;
        private uint _leftPageIndex;
        private PageNumberInfo _pageNumber;

        private Size _pageAreaGridSize;
        private BitmapImage _rightPageImage;
        private BitmapImage _leftPageImage;
        private Size _rightPageActualSize;
        private Size _leftPageActualSize;
        private double _pageHeight;
        private PageViewMode _viewMode;
        private double _zoom;
        

        public PrintPreviewViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            _rightPageIndex = 0;
            Messenger.Default.Register<DummyPageMessage>(this, (pageMessage) => OnPageImageLoaded(pageMessage));
            Initialize();
        }

        // TODO: Remove dummy variables and unneeded initialization
        private void Initialize()
        {
            // Set pageAreaGrid Size
            

            PageTotal = DummyProvider.Instance.TOTAL_PAGES;
            ViewMode = DummyProvider.Instance.PAGE_VIEW_MODE;
            GoToPage(0); // Go to first page
        }

        public ObservableCollection<PreviewPageViewModel> PreviewPages
        {
            get
            {
                if (_previewPages == null)
                {
                    _previewPages = new ObservableCollection<PreviewPageViewModel>();
                }
                return _previewPages;
            }
        }
        
        #region PAGE_DISPLAY

        //public Size PageAreaGridSize
        //{
        //    get { return _pageAreaGridSize; }
        //    set
        //    {
        //        if (_pageAreaGridSize != value)
        //        {
        //            _pageAreaGridSize = value;
        //            RaisePropertyChanged("PageAreaGridSize");
        //        }
        //    }
        //}

        public BitmapImage RightPageImage
        {
            get { return _rightPageImage; }
            set
            {
                if (_rightPageImage != value)
                {
                    _rightPageImage = value;
                    RaisePropertyChanged("RightPageImage");
                }
            }
        }

        public BitmapImage LeftPageImage
        {
            get { return _leftPageImage; }
            set
            {
                if (_leftPageImage != value)
                {
                    _leftPageImage = value;
                    RaisePropertyChanged("LeftPageImage");
                }
            }
        }

        public Size RightPageActualSize
        {
            get { return _rightPageActualSize; }
            set { _rightPageActualSize = value; }
        }

        public Size LeftPageActualSize
        {
            get { return _leftPageActualSize; }
            set { _leftPageActualSize = value; }
        }
        
        public PageViewMode ViewMode
        {
            get { return _viewMode; }
            set
            {
                if (_viewMode != value)
                {
                    _viewMode = value;
                    RaisePropertyChanged("ViewMode");
                }
            }
        }

        public double Zoom
        {
            get { return _zoom; }
            set { _zoom = value; }
        }

        // TODO: Add handling for left and right pages
        // Note: For current right page only
        private void OnPageImageLoaded(DummyPageMessage pageMessage)
        {
            RightPageImage = pageMessage.PageImage;
            RightPageActualSize = pageMessage.ActualSize;
        }

        #endregion

        #region SINGLE-PAGE NAVIGATION

        public ICommand GoToPreviousPage
        {
            get
            {
                if (_goToPreviousPage == null)
                {
                    _goToPreviousPage = new RelayCommand(
                        () => GoToPreviousPageExecute(),
                        () => CanGoToPreviousPage()
                    );
                }
                return _goToPreviousPage;
            }
        }

        public ICommand GoToNextPage
        {
            get
            {
                if (_goToNextPage == null)
                {
                    _goToNextPage = new RelayCommand(
                        () => GoToNextPageExecute(),
                        () => CanGoToNextPage()
                    );
                }
                return _goToNextPage;
            }
        }

        public PageNumberInfo PageNumber
        {
            get { return _pageNumber; }
            set
            {
                if (_pageNumber != value)
                {
                    _pageNumber = value;
                    RaisePropertyChanged("PageNumber");
                }
            }
        }

        // TODO: Two-page view
        private void GoToPage(uint index)
        {
            DummyProvider.Instance.LoadPageImage(index);
            _rightPageIndex = index;
            SetPageIndexes();
        }
        
        // TODO: Two-page view
        private void GoToPreviousPageExecute()
        {
            DummyProvider.Instance.LoadPageImage(--_rightPageIndex);
            SetPageIndexes();
        }

        private bool CanGoToPreviousPage()
        {
            // Check if at least second page
            if (_rightPageIndex > 0) return true;
            return false;
        }

        // TODO: Two-page view
        private void GoToNextPageExecute()
        {
            DummyProvider.Instance.LoadPageImage(++_rightPageIndex);
            SetPageIndexes();
        }

        private bool CanGoToNextPage()
        {
            // Check if at least second to the last page
            if (_rightPageIndex < _pageTotal - 1) return true;
            return false;
        }
        
        private void SetPageIndexes()
        {
            if (_viewMode == PageViewMode.SinglePageView)
            {
                PageNumber = new PageNumberInfo(0, _rightPageIndex, _pageTotal, _viewMode);
                CurrentPageIndex = _rightPageIndex;
            }
            else if (_viewMode == PageViewMode.TwoPageView)
            {
                PageNumber = new PageNumberInfo(_leftPageIndex, _rightPageIndex, _pageTotal, _viewMode);
                if (_rightPageIndex == 0) // If first page
                {
                    CurrentPageIndex = _rightPageIndex;
                }
                else
                {
                    CurrentPageIndex = _leftPageIndex;
                }
            }
        }

        #endregion

        #region MULTI-PAGE NAVIGATION

        public ICommand PageNumberSliderValueChange
        {
            get
            {
                if (_pageNumberSliderValueChange == null)
                {
                    _pageNumberSliderValueChange = new RelayCommand(
                        () => PageNumberSliderValueChangeExecute(),
                        () => true
                    );
                }
                return _pageNumberSliderValueChange;
            }
        }

        public uint PageTotal
        {
            get { return _pageTotal; }
            set
            {
                if (_pageTotal != value)
                {
                    _pageTotal = value;
                    RaisePropertyChanged("PageTotal");
                }
            }
        }

        // Different handling depending on view mode
        public uint CurrentPageIndex
        {
            get 
            {
                return _currentPageIndex;
            }
            set
            {
                if (_currentPageIndex != value)
                {
                    _currentPageIndex = value;
                    RaisePropertyChanged("CurrentPageIndex");
                }
            }
        }

        private void PageNumberSliderValueChangeExecute()
        {
            var newValue = CurrentPageIndex; // verify 0-based
            GoToPage(newValue);
        }

        #endregion
    }
}
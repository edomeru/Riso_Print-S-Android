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
using Windows.UI.Xaml.Controls;
using Windows.Foundation;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;
// using SmartDeviceApp.DummyControllers;
using SmartDeviceApp.Controllers;

namespace SmartDeviceApp.ViewModels
{
    public class PrintPreviewViewModel : ViewModelBase
    {
        private const bool IS_SINGLE_PAGE = true;
        private const double SIDE_PANE_RATIO = 2;
        private const double PREVIEW_VIEW_RATIO = 5;

        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private Grid _pageAreaGrid;
        public GestureController _gestureController; // TODO: Set to private after removing easter egg!!
        private bool _isPageNumberSliderEnabled;
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

        private string _documentTitleText;
        private BitmapImage _rightPageImage;
        private BitmapImage _leftPageImage;
        private Size _rightPageActualSize;
        private Size _leftPageActualSize;
        private PageViewMode _pageViewMode;

        private ICommand _toggleMainMenuPane;
        private ICommand _showPreviewViewFullScreen;
        private ICommand _toggleRightPane;
        private PreviewViewMode _previewViewMode;
        
        public PrintPreviewViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            _rightPageIndex = 0;
            // Messenger.Default.Register<DummyPageMessage>(this, (pageMessage) => OnPageImageLoaded(pageMessage));
            Messenger.Default.Register<DocumentMessage>(this, (documentMessage) => OnDocumentLoaded(documentMessage));
            Messenger.Default.Register<PreviewInfoMessage>(this, (previewInfoMessage) => OnPreviewInfoUpdated(previewInfoMessage));
            Messenger.Default.Register<PreviewPageImage>(this, (previewPageImage) => OnPageImageLoaded(previewPageImage));
            Initialize();
        }

        // TODO: Remove dummy variables and unneeded initialization
        private void Initialize()
        {
            SetPreviewViewMode(PreviewViewMode.PreviewViewFullScreen);
            /*
            //DocumentTitleText = DummyProvider.Instance.PDF_FILENAME;
            DocumentTitleText = DocumentController.Instance.FileName;

            // PageTotal = DummyProvider.Instance.TOTAL_PAGES;
            PageTotal = PrintPreviewController.Instance.PageTotal;
            // PageViewMode = DummyProvider.Instance.PAGE_VIEW_MODE;
            PageViewMode = PrintPreviewController.Instance.PageViewMode;
            GoToPage(0); // Go to first page
             * */
        }


        public void SetPageAreaGrid(Grid pageAreaGrid)
        {
            _pageAreaGrid = pageAreaGrid;
        }

        public void InitializeGestures()
        {
            if (_pageAreaGrid != null && _gestureController == null)
            {
                // Save page height to be used in resizing page images
                var scalingFactor = _pageAreaGrid.ActualHeight / RightPageActualSize.Height;

                var pageAreaScrollViewer = (UIElement)_pageAreaGrid.Parent;
                _gestureController = new GestureController(_pageAreaGrid, pageAreaScrollViewer,
                    RightPageActualSize, scalingFactor,
                    new GestureController.SwipeRightDelegate(SwipeRight),
                    new GestureController.SwipeLeftDelegate(SwipeLeft));

                // TODO: Two-page view handling
            }
        }

        private void SwipeRight()
        {
            GoToPreviousPage.Execute(null);
        }

        private void SwipeLeft()
        {
            GoToNextPage.Execute(null);
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

        #region PANE_VISIBILITY

        public ICommand ToggleMainMenuPane
        {
            get
            {
                if (_toggleMainMenuPane == null)
                {
                    _toggleMainMenuPane = new RelayCommand(
                        () => ToggleMainMenuPaneExecute(),
                        () => true
                    );
                }
                return _toggleMainMenuPane;
            }
        }        

        public ICommand ShowPreviewViewFullScreen
        {
            get
            {
                if (_showPreviewViewFullScreen == null)
                {
                    _showPreviewViewFullScreen = new RelayCommand(
                        () => SetPreviewViewMode(PreviewViewMode.PreviewViewFullScreen),
                        () => true
                    );
                }
                return _showPreviewViewFullScreen;
            }
        }

        public ICommand ToggleRightPane
        {
            get
            {
                if (_toggleRightPane == null)
                {
                    _toggleRightPane = new RelayCommand(
                        () => ToggleRightPaneExecute(),
                        () => true
                    );
                }
                return _toggleRightPane;
            }
        }

        private void ToggleMainMenuPaneExecute()
        {
            switch (_previewViewMode)
            {
                case PreviewViewMode.MainMenuPaneVisible:
                {
                    SetPreviewViewMode(PreviewViewMode.PreviewViewFullScreen);
                    break;
                }

                case PreviewViewMode.PreviewViewFullScreen:
                {
                    SetPreviewViewMode(PreviewViewMode.MainMenuPaneVisible);
                    break;
                }

                case PreviewViewMode.RightPaneVisible:
                {
                    SetPreviewViewMode(PreviewViewMode.PreviewViewFullScreen);
                    SetPreviewViewMode(PreviewViewMode.MainMenuPaneVisible);
                    break;
                }
            }
        }

        private void ToggleRightPaneExecute()
        {
            switch (_previewViewMode)
            {
                case PreviewViewMode.MainMenuPaneVisible:
                    {
                        SetPreviewViewMode(PreviewViewMode.PreviewViewFullScreen);
                        SetPreviewViewMode(PreviewViewMode.RightPaneVisible);
                        break;
                    }

                case PreviewViewMode.PreviewViewFullScreen:
                    {
                        SetPreviewViewMode(PreviewViewMode.RightPaneVisible);
                        break;
                    }

                case PreviewViewMode.RightPaneVisible:
                    {
                        SetPreviewViewMode(PreviewViewMode.PreviewViewFullScreen);
                        break;
                    }
            }
        }

        private void SetPreviewViewMode(PreviewViewMode previewViewMode)
        {
            Messenger.Default.Send<PreviewViewMode>(previewViewMode);
            switch (previewViewMode)
            {
                case PreviewViewMode.MainMenuPaneVisible:
                {
                    DisablePreviewGestures();
                    break;
                }

                case PreviewViewMode.PreviewViewFullScreen:
                {
                    EnablePreviewGestures();
                    break;
                }

                case PreviewViewMode.RightPaneVisible:
                {
                    EnablePreviewGestures();
                    break;
                }
            }
            _previewViewMode = previewViewMode;
        }

        private void EnablePreviewGestures()
        {
            if (_gestureController != null)
            {
                _gestureController.EnableGestures();
            }
            IsPageNumberSliderEnabled = true;
        }

        private void DisablePreviewGestures()
        {
            if (_gestureController != null) _gestureController.DisableGestures();
            IsPageNumberSliderEnabled = false;
        }

        #endregion

        #region PAGE_DISPLAY

        public string DocumentTitleText
        {
            get { return _documentTitleText; }
            set
            {
                if (_documentTitleText != value)
                {
                    _documentTitleText = value;
                    RaisePropertyChanged("DocumentTitleText");
                }
            }
        }

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
        
        public PageViewMode PageViewMode
        {
            get { return _pageViewMode; }
            set
            {
                if (_pageViewMode != value)
                {
                    _pageViewMode = value;
                    RaisePropertyChanged("PageViewMode");
                }
            }
        }

        private async void OnPreviewInfoUpdated(PreviewInfoMessage printSettingMessage)
        {
            PageTotal = printSettingMessage.PageTotal;
            PageViewMode = printSettingMessage.PageViewMode;
            await GoToPage(0); // Go to first page
        }

        private void OnDocumentLoaded(DocumentMessage pdfStatusMessage)
        {
            if (pdfStatusMessage.IsLoaded)
            {
                DocumentTitleText = pdfStatusMessage.DocTitle;
            }
            else
            {
                // Signal error
            }
        }

        // TODO: Add handling for left and right pages
        // Note: For current right page only
        // private void OnPageImageLoaded(DummyPageMessage pageMessage)
        private void OnPageImageLoaded(PreviewPageImage previewPageImage)
        {
            RightPageImage = previewPageImage.PageImage;
            RightPageActualSize = previewPageImage.ActualSize;
            InitializeGestures();
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
        private async Task GoToPage(uint index)
        {
            //DummyProvider.Instance.LoadPageImage(index);
            Task task = PrintPreviewController.Instance.LoadPage((int)index);
            _rightPageIndex = index;
            SetPageIndexes();

            await task;
        }
        
        // TODO: Two-page view
        private async void GoToPreviousPageExecute()
        {
            // DummyProvider.Instance.LoadPageImage(--_rightPageIndex);
            await PrintPreviewController.Instance.LoadPage((int)--_rightPageIndex);
            SetPageIndexes();
        }

        private bool CanGoToPreviousPage()
        {
            // Check if at least second page
            if (_rightPageIndex > 0) return true;
            return false;
        }

        // TODO: Two-page view
        private async void GoToNextPageExecute()
        {
            // DummyProvider.Instance.LoadPageImage(++_rightPageIndex);
            await PrintPreviewController.Instance.LoadPage((int)++_rightPageIndex);
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
            if (_pageViewMode == PageViewMode.SinglePageView)
            {
                PageNumber = new PageNumberInfo(0, _rightPageIndex, _pageTotal, _pageViewMode);
                CurrentPageIndex = _rightPageIndex;
            }
            else if (_pageViewMode == PageViewMode.TwoPageView)
            {
                PageNumber = new PageNumberInfo(_leftPageIndex, _rightPageIndex, _pageTotal, _pageViewMode);
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

        public bool IsPageNumberSliderEnabled
        {
            get { return _isPageNumberSliderEnabled; }
            set
            {
                if (_isPageNumberSliderEnabled != value)
                {
                    _isPageNumberSliderEnabled = value;
                    RaisePropertyChanged("IsPageNumberSliderEnabled");
                }
            }
        }

        private void PageNumberSliderValueChangeExecute()
        {
            // TODO: Consider handling the event only when drag is released 
            var newValue = CurrentPageIndex; // verify 0-based
            GoToPage(newValue); // Not awaited ?? since crashing on await
        }

        #endregion

    }
}
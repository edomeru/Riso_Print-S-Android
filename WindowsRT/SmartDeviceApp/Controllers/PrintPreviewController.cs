//
//  PrintPreviewController.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/11.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using GalaSoft.MvvmLight.Threading;
using SmartDeviceApp.Common;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Controls;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Input;
using Windows.Foundation;
using Windows.System.Threading;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Media.Imaging;

namespace SmartDeviceApp.Controllers
{
    public sealed class PrintPreviewController
    {
        static readonly PrintPreviewController _instance = new PrintPreviewController();

        // Transition to Print Preview Screen
        public delegate void OnNavigateToEventHandler();
        private OnNavigateFromEventHandler _onNavigateFromEventHandler;

        // Transition from Print Preview Screen
        public delegate void OnNavigateFromEventHandler();
        private OnNavigateToEventHandler _onNavigateToEventHandler;

        // Update preview area based on updated print settings
        public delegate void UpdatePreviewEventHandler(PrintSetting printSetting);
        private UpdatePreviewEventHandler _updatePreviewEventHandler;

        // Slider value and turn page
        public delegate void GoToPageEventHandler(int pageIndex);
        private GoToPageEventHandler _goToPageEventHandler;

        // Determine which page is to be displayed at the back
        public delegate void TurnPageEventHandler(bool isForward);
        private TurnPageEventHandler _turnPageEventHandler;

        // Choose printer
        public delegate void SelectedPrinterChangedEventHandler(int printerId);
        private SelectedPrinterChangedEventHandler _selectedPrinterChangedEventHandler;

        public delegate void AddFirstPrinterEventHandler();
        private AddFirstPrinterEventHandler _addFirstPrinterEventHandler;

        // Print button
        public delegate void PrintEventHandler();
        private PrintEventHandler _printEventHandler;

        // Cancel print
        public delegate void CancelPrintEventHandler();
        private CancelPrintEventHandler _cancelPrintEventHandler;

        // PageAreaGrid loaded
        public delegate void PageAreaGridLoadedEventHandler();
        private PageAreaGridLoadedEventHandler _pageAreaGridLoadedEventHandler;

        // Constants
        private const int NO_SELECTED_PRINTER_ID = -1;
        private const int MAX_PREVIEW_PAGE_IMAGE_CACHE = 10;

        private PrintPreviewViewModel _printPreviewViewModel;
        private SelectPrinterViewModel _selectPrinterViewModel;
        private PrintSettingsViewModel _printSettingsViewModel;
        private DirectPrintController _directPrintController;
        private string _screenName;
        private Printer _selectedPrinter;
        private PrintSettings _currPrintSettings;
        private int _pagesPerSheet = 1;
        private bool _isReverseOrder;
        private bool _isSwipeLeft;
        private bool _isDuplex;
        private bool _isBooklet;
        private uint _previewPageTotal;
        private int _maxPreviewPageCount;
        private Size _previewPageImageSize;
        private static int _currSliderIndex;
        private static int _currLeftPageIndex;
        private static int _currRightPageIndex;
        private static int _currLeftBackPageIndex;
        private static int _currRightBackPageIndex;
        private bool _resetPrintSettings; // Flag used only when selected printer is deleted

        List<CancellationTokenSource> _cancellationTokenSourceQueue;
        LruCacheHelper<int, byte[]> _previewPageImages;

        private ICommand _cancelPrintingCommand;
        private Popup _printingPopup;
        private MessageProgressBarControl _printingProgress;

        private PageViewMode _pageViewMode;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static PrintPreviewController() { }

        private PrintPreviewController()
        {
            _printPreviewViewModel = new ViewModelLocator().PrintPreviewViewModel;
            _selectPrinterViewModel = new ViewModelLocator().SelectPrinterViewModel;
            _printSettingsViewModel = new ViewModelLocator().PrintSettingsViewModel;

            _screenName = ScreenMode.PrintPreview.ToString();

            _cancellationTokenSourceQueue = new List<CancellationTokenSource>();
            _previewPageImages = new LruCacheHelper<int, byte[]>(MAX_PREVIEW_PAGE_IMAGE_CACHE);

            _updatePreviewEventHandler = new UpdatePreviewEventHandler(UpdatePreview);
            _goToPageEventHandler = new GoToPageEventHandler(GoToPage);
            _turnPageEventHandler = new TurnPageEventHandler(LoadBackPage);
            _selectedPrinterChangedEventHandler = new SelectedPrinterChangedEventHandler(SelectedPrinterChanged);
            _addFirstPrinterEventHandler = new AddFirstPrinterEventHandler(FirstPrinterAdded);
            _printEventHandler = new PrintEventHandler(Print);
            _cancelPrintEventHandler = new CancelPrintEventHandler(CancelPrint);
            _onNavigateToEventHandler = new OnNavigateToEventHandler(RegisterPrintSettingValueChange);
            _onNavigateFromEventHandler = new OnNavigateFromEventHandler(UnregisterPrintSettingValueChange);
            _pageAreaGridLoadedEventHandler = new PageAreaGridLoadedEventHandler(InitializeGestures);
        }

        /// <summary>
        /// Singleton instance
        /// </summary>
        public static PrintPreviewController Instance
        {
            get { return _instance; }
        }

        /// <summary>
        /// Entry point for print preview and print settings.
        /// Starts query of printer and print settings.
        /// </summary>
        /// <returns>task</returns>
        public async Task Initialize()
        {
#if PREVIEW_PUNCH
            await PreviewPageImageUtility.LoadPunchBitmap();
#endif // PREVIEW_PUNCH
#if PREVIEW_STAPLE
            await PreviewPageImageUtility.LoadStapleBitmap();
#endif // PREVIEW_STAPLE

            // Get print settings if document is successfully loaded
            if (DocumentController.Instance.Result == LoadDocumentResult.Successful)
            {
                _previewPageTotal = DocumentController.Instance.PageCount;

                // Get initialize printer and print settings
                await GetDefaultPrinter();

                _printPreviewViewModel.SetInitialPageIndex(0);
                _printPreviewViewModel.DocumentTitleText = DocumentController.Instance.FileName;

                _selectPrinterViewModel.SelectPrinterEvent += _selectedPrinterChangedEventHandler;
                PrinterController.Instance.AddFirstPrinterEvent += _addFirstPrinterEventHandler;
                
                _printSettingsViewModel.ExecutePrintEventHandler += _printEventHandler;

                _printPreviewViewModel.OnNavigateFromEventHandler += _onNavigateFromEventHandler;
                _printPreviewViewModel.OnNavigateToEventHandler += _onNavigateToEventHandler;
                _printPreviewViewModel.PageAreaGridLoadedEventHandler += _pageAreaGridLoadedEventHandler;
                _printPreviewViewModel.TurnPageEventHandler += _turnPageEventHandler;

                PrinterController.Instance.DeletePrinterItemsEventHandler += PrinterDeleted;
            }
            else if (DocumentController.Instance.Result == LoadDocumentResult.UnsupportedPdf)
            {
                (new ViewModelLocator().HomeViewModel).IsProgressRingActive = false;
                await DialogService.Instance.ShowError("IDS_ERR_MSG_PDF_ENCRYPTED", "IDS_APP_NAME",
                    "IDS_LBL_OK", null);
            }
            else // DocumentController.Instance.Result == LoadDocumentResult.ErrorReadPdf or LoadDocumentResult.NotStarted
            {
                (new ViewModelLocator().HomeViewModel).IsProgressRingActive = false;
                await DialogService.Instance.ShowError("IDS_ERR_MSG_OPEN_FAILED", "IDS_APP_NAME",
                    "IDS_LBL_OK", null);
            }
        }

        /// <summary>
        /// Clean-up
        /// </summary>
        public void Cleanup()
        {
            _printPreviewViewModel.GoToPageEventHandler -= _goToPageEventHandler;
            _printPreviewViewModel.TurnPageEventHandler -= _turnPageEventHandler;
            _printPreviewViewModel.PageAreaGridLoadedEventHandler -= _pageAreaGridLoadedEventHandler;
            PrintSettingsController.Instance.UnregisterUpdatePreviewEventHandler(_updatePreviewEventHandler);
            _selectPrinterViewModel.SelectPrinterEvent -= _selectedPrinterChangedEventHandler;
            PrinterController.Instance.AddFirstPrinterEvent -= _addFirstPrinterEventHandler;
            
            _printSettingsViewModel.ExecutePrintEventHandler -= _printEventHandler;
            PrinterController.Instance.DeletePrinterItemsEventHandler -= PrinterDeleted;
            
            foreach (CancellationTokenSource token in _cancellationTokenSourceQueue)
            {
                token.Cancel();
            }
            _cancellationTokenSourceQueue.Clear();

            _resetPrintSettings = false;
            _selectedPrinter = null;

            _pagesPerSheet = 1;
            _isDuplex = false;
            _isBooklet = false;

            _currSliderIndex = 0;
            _currLeftPageIndex = 0;
            _currRightPageIndex = 0;

            _printPreviewViewModel.PageTotal = 0;

            _previewPageImages.Clear();
            _printPreviewViewModel.Cleanup();
        }

        #region Printer and Print Settings Initialization

        /// <summary>
        /// On navigate to Print Preview screen event handler
        /// </summary>
        public async void RegisterPrintSettingValueChange()
        {
            if (_resetPrintSettings)
            {
                _selectedPrinter = null;
                await SetSelectedPrinterAndPrintSettings(NO_SELECTED_PRINTER_ID);
                _resetPrintSettings = false;
            }
            else
            {
                PrintSettingsController.Instance.RegisterPrintSettingValueChanged(_screenName);
            }
        }

        /// <summary>
        /// On navigate from Print Preview screen event handler
        /// </summary>
        public void UnregisterPrintSettingValueChange()
        {
            PrintSettingsController.Instance.UnregisterPrintSettingValueChanged(_screenName);
        }

        /// <summary>
        /// Event handler when a printer is deleted
        /// </summary>
        /// <param name="printer">printer</param>
        public void PrinterDeleted(Printer printer)
        {
            if (_selectedPrinter.Id == printer.Id)
            {
                _resetPrintSettings = true;
            }
        }

        /// <summary>
        /// Event handler for selected printer
        /// </summary>
        /// <param name="printerId">printer ID</param>
        public async void SelectedPrinterChanged(int printerId)
        {
            if (_selectedPrinter.Id != printerId)
            {
                await SetSelectedPrinterAndPrintSettings(printerId);
            }
        }

        /// <summary>
        /// Event handler for first printer added
        /// </summary>
        public async void FirstPrinterAdded()
        {
            await GetDefaultPrinter();
            if (_resetPrintSettings) _resetPrintSettings = false;
        }

        /// <summary>
        /// Retrieves the default printer.
        /// If no default printer is found, a dummy printer (with ID = -1) is set as
        /// selected printer.
        /// </summary>
        /// <returns>task</returns>
        private async Task GetDefaultPrinter()
        {
            // Get default printer
            Printer defaultPrinter = PrinterController.Instance.GetDefaultPrinter();
            if (defaultPrinter != null)
            {
                await SetSelectedPrinterAndPrintSettings(defaultPrinter.Id);
            }
            else
            {
                _selectedPrinter = null;
                await SetSelectedPrinterAndPrintSettings(NO_SELECTED_PRINTER_ID);
            }
        }

        /// <summary>
        /// Initializes the selected ptinter and retrives its print settings.
        /// </summary>
        /// <param name="printerId">printer ID</param>
        /// <returns>task</returns>
        private async Task SetSelectedPrinterAndPrintSettings(int printerId)
        {
            if (printerId > -1)
            {
                _selectedPrinter = PrinterController.Instance.GetPrinter(printerId);
            }
            if (_selectedPrinter == null)
            {
                // Use dummy printer (meaning no selected printer)
                _selectedPrinter = new Printer();
            }

            PrintSettingsController.Instance.Uninitialize(_screenName);
            await PrintSettingsController.Instance.Initialize(_screenName, _selectedPrinter);
            _currPrintSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);
            PrintSettingsController.Instance.RegisterUpdatePreviewEventHandler(_updatePreviewEventHandler);
            ReloadCurrentPage();
        }

        #endregion Printer and Print Settings Initialization

        #region Print Preview

        /// <summary>
        /// Initializes the gesture of the preview area.
        /// Requires initial paper size and grid is already loaded before using this function.
        /// </summary>
        private void InitializeGestures()
        {
            if (DocumentController.Instance.Result == LoadDocumentResult.Successful &&
                _printPreviewViewModel.IsPageAreaGridLoaded)
            {
                bool isPortrait = PreviewPageImageUtility.IsPreviewPagePortrait(
                    _currPrintSettings.Orientation, _currPrintSettings.Imposition);

                // Update swipe direction/flow
                if (_isReverseOrder)
                {
                    _printPreviewViewModel.IsReverseSwipe = true;
                }
                else
                {
                    _printPreviewViewModel.IsReverseSwipe = false;
                }

                // Update swipe direction
                if ((_isBooklet && !isPortrait) ||
                    (_currPrintSettings.FinishingSide == (int)FinishingSide.Top)) 
                {
                    _printPreviewViewModel.IsHorizontalSwipeEnabled = false;
                }
                else
                {
                    _printPreviewViewModel.IsHorizontalSwipeEnabled = true;
                }

                // Update base page bitmap sizes
                _printPreviewViewModel.RightPageActualSize = _previewPageImageSize;
                if (_isBooklet || _isDuplex)
                {
                    _printPreviewViewModel.LeftPageActualSize = _previewPageImageSize;
                }
                else
                {
                    _printPreviewViewModel.LeftPageActualSize = new Size();
                }

                // Update page view mode
                _printPreviewViewModel.PageViewMode = _pageViewMode;

                _printPreviewViewModel.InitializeGestures();
            }
        }

        /// <summary>
        /// Event handler that receives modified print setting to update preview
        /// </summary>
        /// <param name="printSetting">affected print setting</param>
        public void UpdatePreview(PrintSetting printSetting)
        {
            if (printSetting == null)
            {
                return;
            }

            _currPrintSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);

            ReloadCurrentPage();
        }

        /// <summary>
        /// Checks for view related print setting and notifies view model
        /// </summary>
        private void UpdatePreviewInfo()
        {
            // Determine direction
            _isReverseOrder = ((_isBooklet && _currPrintSettings.BookletLayout == (int)BookletLayout.Reverse) ||
                          (!_isBooklet && _currPrintSettings.FinishingSide == (int)FinishingSide.Right));

            // Determine view mode
            _isBooklet = _currPrintSettings.Booklet;
            _isDuplex = (_currPrintSettings.Duplex != (int)Duplex.Off);
            if (_isBooklet)
            {
                if (_currPrintSettings.Orientation == (int)Orientation.Landscape)
                {
                    _pageViewMode = PageViewMode.TwoPageViewVertical;
                }
                else
                {
                    _pageViewMode = PageViewMode.TwoPageViewHorizontal;
                }
            }
            else if (_isDuplex && _currPrintSettings.FinishingSide != (int)FinishingSide.Top)
            {
                _pageViewMode = PageViewMode.TwoPageViewHorizontal;
            }
            else if (_isDuplex && _currPrintSettings.FinishingSide == (int)FinishingSide.Top)
            {
                _pageViewMode = PageViewMode.TwoPageViewVertical;
            }
            else
            {
                _pageViewMode = PageViewMode.SinglePageView;
            }

            // Update page count and page number
            _pagesPerSheet = PreviewPageImageUtility.GetPagesPerSheet(_currPrintSettings.Imposition);
            _previewPageTotal = (uint)Math.Ceiling((decimal)DocumentController.Instance.PageCount /
                                                    _pagesPerSheet);
            if (_isBooklet)
            {
                _previewPageTotal = ((_previewPageTotal / 4) * 2) + (uint)(((_previewPageTotal % 4) == 0) ? 1 : 3);
            }
            else if (_isDuplex)
            {
                _previewPageTotal = (_previewPageTotal / 2) + (_previewPageTotal % 2) + 1;
            }
            if (_printPreviewViewModel.PageTotal != _previewPageTotal)
            {
                _printPreviewViewModel.GoToPageEventHandler -= _goToPageEventHandler;
                _printPreviewViewModel.PageTotal = _previewPageTotal;
                _printPreviewViewModel.GoToPageEventHandler += _goToPageEventHandler;
                if (_currSliderIndex >= _previewPageTotal)
                {
                    _currSliderIndex = (int)_previewPageTotal - 1;
                }
                _printPreviewViewModel.UpdatePageIndexes((uint)_currSliderIndex);
            }

            _maxPreviewPageCount = (int)_previewPageTotal;
            if (_isBooklet || _isDuplex)
            {
                _maxPreviewPageCount = ((int)_previewPageTotal * 2) - 2;
            }

            Size paperSize = PreviewPageImageUtility.GetPaperSize(_currPrintSettings.PaperSize);
            bool isPortrait = PreviewPageImageUtility.IsPreviewPagePortrait(
                _currPrintSettings.Orientation, _currPrintSettings.Imposition);

            // Determine page size
            _previewPageImageSize = PreviewPageImageUtility.GetPreviewPageImageSize(paperSize,
                isPortrait);

            // Update source page bitmap
            if (_isBooklet || _isDuplex)
            {
                _printPreviewViewModel.LeftPageImage =
                    new WriteableBitmap((int)_previewPageImageSize.Width,
                        (int)_previewPageImageSize.Height);
                _printPreviewViewModel.LeftBackPageImage =
                    new WriteableBitmap((int)_previewPageImageSize.Width,
                        (int)_previewPageImageSize.Height);
                _printPreviewViewModel.LeftNextPageImage =
                    new WriteableBitmap((int)_previewPageImageSize.Width,
                        (int)_previewPageImageSize.Height);
            }
            _printPreviewViewModel.RightPageImage =
                new WriteableBitmap((int)_previewPageImageSize.Width,
                    (int)_previewPageImageSize.Height);
            _printPreviewViewModel.RightBackPageImage =
                new WriteableBitmap((int)_previewPageImageSize.Width,
                    (int)_previewPageImageSize.Height);
            _printPreviewViewModel.RightNextPageImage =
                new WriteableBitmap((int)_previewPageImageSize.Width,
                    (int)_previewPageImageSize.Height);
        }

        #endregion Print Preview

        #region Preview Page Navigation

        /// <summary>
        /// Event handler for page slider is changed
        /// </summary>
        /// <param name="sliderIndex">requested right page index based on slider value</param>
        public void GoToPage(int sliderIndex)
        {
            if (_isBooklet || _isDuplex)
            {
                _printPreviewViewModel.IsLoadLeftPageActive = true;
            }
            _printPreviewViewModel.IsLoadRightPageActive = true;
            _currSliderIndex = sliderIndex;
            LoadPage(_currSliderIndex);
        }

        /// <summary>
        /// Refreshes the preview area based on new print settings
        /// </summary>
        private void ReloadCurrentPage()
        {
            // Show loading indicators
            if (_isBooklet || _isDuplex)
            {
                _printPreviewViewModel.IsLoadLeftPageActive = true;
            }
            _printPreviewViewModel.IsLoadRightPageActive = true;

            // Cancel other processing if any
            foreach (CancellationTokenSource token in _cancellationTokenSourceQueue)
            {
                token.Cancel();
            }
            _cancellationTokenSourceQueue.Clear();
            _previewPageImages.Clear();

            UpdatePreviewInfo();
            InitializeGestures();
            LoadPage(_currSliderIndex);
        }

        /// <summary>
        /// Requests for LogicalPages and then applies print setting for the target page only.
        /// Assumes that requested page index is for right side page index
        /// </summary>
        /// <param name="sliderIndex">requested right page index based on slider value</param>
        private void LoadPage(int sliderIndex)
        {
            LogUtility.BeginTimestamp("LoadPage");

            foreach (CancellationTokenSource token in _cancellationTokenSourceQueue)
            {
                token.Cancel();
            }
            _cancellationTokenSourceQueue.Clear();

            CancellationTokenSource cancellationToken = new CancellationTokenSource();
            _cancellationTokenSourceQueue.Add(cancellationToken);

            // Determine right front page index
            int rightPageIndex = sliderIndex;
            if (_isBooklet || _isDuplex)
            {
                rightPageIndex = sliderIndex * 2;
            }

            // Determine page indices based on front right page index
            _currRightPageIndex = rightPageIndex;
            _currLeftPageIndex = -1;
            if (_isBooklet || _isDuplex)
            {
                _currLeftPageIndex = rightPageIndex - 1;
                if (_isReverseOrder)
                {
                    // Swap page index on reverse
                    _currRightPageIndex = rightPageIndex - 1;
                    _currLeftPageIndex = rightPageIndex;
                }
            }

            // Fill all white
            if (!_previewPageImages.ContainsKey(_currRightPageIndex))
            {
                if ((!_isReverseOrder && _currRightPageIndex > 0) && (_isReverseOrder && _currRightPageIndex < _previewPageTotal)) // added to prevent white pages appearing when it is the first or last page
                {
                    PreviewPageImageUtility.FillWhitePageImage(_printPreviewViewModel.RightBackPageImage,
                        _previewPageImageSize, cancellationToken);

                    PreviewPageImageUtility.FillWhitePageImage(_printPreviewViewModel.RightNextPageImage,
                        _previewPageImageSize, cancellationToken);
                }
    
                PreviewPageImageUtility.FillWhitePageImage(_printPreviewViewModel.RightPageImage,
                    _previewPageImageSize, cancellationToken);
            }
            if (!_previewPageImages.ContainsKey(_currLeftPageIndex))
            {

                if ((_isReverseOrder && _currLeftPageIndex > 0) && (!_isReverseOrder && _currLeftPageIndex < _previewPageTotal)) // added to prevent white pages appearing when it is the first or last page
                    PreviewPageImageUtility.FillWhitePageImage(_printPreviewViewModel.LeftBackPageImage,
                        _previewPageImageSize, cancellationToken);
            PreviewPageImageUtility.FillWhitePageImage(_printPreviewViewModel.LeftNextPageImage,
                _previewPageImageSize, cancellationToken);

                PreviewPageImageUtility.FillWhitePageImage(_printPreviewViewModel.LeftPageImage,
                    _previewPageImageSize, cancellationToken);
            }

            // Generate pages to send
            GenerateFrontPreviewPages(_currLeftPageIndex, _currRightPageIndex, cancellationToken);

            LogUtility.EndTimestamp("LoadPage");
        }

        /// <summary>
        /// Generates the front PreviewPage images on a single spread
        /// </summary>
        /// <param name="leftPageIndex">front left preview page index</param>
        /// <param name="rightPageIndex">front right preview page index</param>
        /// <param name="cancellationToken">cancellation token</param>
        private void GenerateFrontPreviewPages(int leftPageIndex, int rightPageIndex,
            CancellationTokenSource cancellationToken)
        {
            // Generate front pages

            if ((_isBooklet || _isDuplex) &&
                leftPageIndex > -1 && leftPageIndex < _maxPreviewPageCount)
            {
                // Generate left side
                GeneratePreviewPage(leftPageIndex, leftPageIndex * _pagesPerSheet, false,
                    false, cancellationToken);
            }
            else
            {
                SendPreviewPageImage(leftPageIndex, true, cancellationToken);
            }

            if (rightPageIndex > -1 && rightPageIndex < _maxPreviewPageCount)
            {
                // Generate right side
                GeneratePreviewPage(rightPageIndex, rightPageIndex * _pagesPerSheet, true,
                    false, cancellationToken);
            }
            else
            {
                SendPreviewPageImage(rightPageIndex, true, cancellationToken);
            }
        }

        /// <summary>
        /// Requests for LogicalPages and then applies print setting for the target pages only.
        /// Assumes that page index is relative from right side page index
        /// </summary>
        /// <param name="isPageTurnNext">true when swipe to left/top, false otherwise</param>
        public void LoadBackPage(bool isPageTurnNext)
        {
            LogUtility.BeginTimestamp("LoadBackPage");

            _isSwipeLeft = (_isReverseOrder) ? !isPageTurnNext : isPageTurnNext;

            if (_isBooklet || _isDuplex)
            {
                _printPreviewViewModel.IsLoadLeftBackPageActive = true;
                _printPreviewViewModel.IsLoadLeftNextPageActive = true;
                _printPreviewViewModel.IsLoadRightNextPageActive = true;
            }
            _printPreviewViewModel.IsLoadRightBackPageActive = true;

            // Determine page indices based on front right page index
            int basePageIndex = (_isReverseOrder && (_isBooklet || _isDuplex)) ? _currLeftPageIndex : _currRightPageIndex;
            if (isPageTurnNext)
            {
                _currLeftBackPageIndex = -1;
                _currRightBackPageIndex = basePageIndex + 1;
                if (_isBooklet || _isDuplex)
                {
                    _currLeftBackPageIndex = basePageIndex + 1;
                    _currRightBackPageIndex = basePageIndex + 2;
                    if (_isReverseOrder)
                    {
                        // Swap page index on reverse
                        _currLeftBackPageIndex = basePageIndex + 2;
                        _currRightBackPageIndex = basePageIndex + 1;
                    }
                }
            }
            else
            {
                _currLeftBackPageIndex = -1;
                _currRightBackPageIndex = basePageIndex - 1;
                if (_isBooklet || _isDuplex)
                {
                    _currLeftBackPageIndex = basePageIndex - 3;
                    _currRightBackPageIndex = basePageIndex - 2;
                    if (_isReverseOrder)
                    {
                        // Swap page index on reverse
                        _currLeftBackPageIndex = basePageIndex - 2;
                        _currRightBackPageIndex = basePageIndex - 3;
                    }
                }
            }

            CancellationTokenSource cancellationToken = new CancellationTokenSource();
            _cancellationTokenSourceQueue.Add(cancellationToken);

            // Fill white all back pages
            if (!_previewPageImages.ContainsKey(_currRightBackPageIndex))
            {
                PreviewPageImageUtility.FillWhitePageImage(_printPreviewViewModel.RightBackPageImage,
                    _previewPageImageSize, cancellationToken);
            }
            if (!_previewPageImages.ContainsKey(_currRightBackPageIndex))
            {
                PreviewPageImageUtility.FillWhitePageImage(_printPreviewViewModel.RightNextPageImage,
                    _previewPageImageSize, cancellationToken);
            }
            if (!_previewPageImages.ContainsKey(_currLeftBackPageIndex))
            {
                PreviewPageImageUtility.FillWhitePageImage(_printPreviewViewModel.LeftBackPageImage,
                    _previewPageImageSize, cancellationToken);
            }
            if (!_previewPageImages.ContainsKey(_currLeftBackPageIndex))
            {
                PreviewPageImageUtility.FillWhitePageImage(_printPreviewViewModel.LeftNextPageImage,
                    _previewPageImageSize, cancellationToken);
            }

            GenerateBackPreviewPages(_currLeftBackPageIndex, _currRightBackPageIndex,
                cancellationToken);

            LogUtility.EndTimestamp("LoadBackPage");
        }

        /// <summary>
        /// Generates the back PreviewPage images on a single spread
        /// </summary>
        /// <param name="leftBackPageIndex">back left preview page index</param>
        /// <param name="rightBackPageIndex">back left preview page index</param>
        /// <param name="isForward">true when forward, false otherwise</param>
        public void GenerateBackPreviewPages(int leftBackPageIndex, int rightBackPageIndex,
            CancellationTokenSource cancellationToken)
        {
            // Generate back pages
            if ((_isBooklet || _isDuplex) &&
                leftBackPageIndex > -1 && leftBackPageIndex < _maxPreviewPageCount)
            {
                // Generate left side
                GeneratePreviewPage(leftBackPageIndex, leftBackPageIndex * _pagesPerSheet, false,
                    true, cancellationToken);
            }
            else
            {
                SendPreviewPageImage(leftBackPageIndex, true, cancellationToken);
            }

            if (rightBackPageIndex > -1 && rightBackPageIndex < _maxPreviewPageCount)
            {
                // Generate right side
                GeneratePreviewPage(rightBackPageIndex, rightBackPageIndex * _pagesPerSheet, true,
                    true, cancellationToken);
            }
            else
            {
                SendPreviewPageImage(rightBackPageIndex, true, cancellationToken);
            }
        }

        /// <summary>
        /// Generate a preview page
        /// </summary>
        /// <param name="previewPageIndex">preview page index</param>
        /// <param name="logicalPageIndex">logical page index</param>
        /// <param name="isRightSide">true when image requested is for right side, false otherwise</param>
        /// <param name="isBackSide">true if the requested page is to be displayed at the back, false otherwise</param>
        /// <param name="cancellationToken">cancellation token</param>
        private async void GeneratePreviewPage(int previewPageIndex, int logicalPageIndex,
            bool isRightSide, bool isBackSide, CancellationTokenSource cancellationToken)
        {
            if (cancellationToken.IsCancellationRequested)
            {
                return;
            }

            if (!_previewPageImages.ContainsKey(previewPageIndex))
            {
                List<WriteableBitmap> logicalPageImages = null;
                // Get logical pages only when not for backside of single-page view
                logicalPageImages = await DocumentController.Instance
                    .GetLogicalPageImages(logicalPageIndex, _pagesPerSheet, cancellationToken);

                Size logicalPageSize = new Size(0, 0);
                if (logicalPageImages != null && logicalPageImages.Count > 0)
                {
                    logicalPageSize.Width = logicalPageImages[0].PixelWidth;
                    logicalPageSize.Height = logicalPageImages[0].PixelHeight;
                }

                WriteableBitmap canvasBitmap = new WriteableBitmap((int)_previewPageImageSize.Width,
                    (int)_previewPageImageSize.Height);

                await ThreadPool.RunAsync(
                    (workItem) =>
                    {
                        ApplyPrintSettings(canvasBitmap, _previewPageImageSize,
                            logicalPageImages, logicalPageSize,
                            previewPageIndex, isRightSide, isBackSide,
                            cancellationToken);

                        SendPreviewPageImage(previewPageIndex, false, cancellationToken);
                    });
            }
            else
            {
                SendPreviewPageImage(previewPageIndex, false, cancellationToken);
            }
        }

        #endregion Preview Page Navigation

        #region Apply Print Settings

        /// <summary>
        /// Applies print settings to logical page images to create a single preview page
        /// </summary>
        /// <param name="canvasBitmap">preview page image</param>
        /// <param name="previewPageSize">preview page size</param>
        /// <param name="logicalPageImages">logical page images</param>
        /// <param name="logicalPageSize">logical page size</param>
        /// <param name="previewPageIndex">preview page index</param>
        /// <param name="isRightSide">true when image requested is for right side, false otherwise</param>
        /// <param name="isBackSide">true when duplex is on and is for back side, false otherwise</param>
        /// <param name="cancellationToken">cancellation token</param>
        private void ApplyPrintSettings(WriteableBitmap canvasBitmap,
            Size previewPageSize, List<WriteableBitmap> logicalPageImages, Size logicalPageSize,
            int previewPageIndex, bool isRightSide, bool isBackSide,
            CancellationTokenSource cancellationToken)
        {
            LogUtility.BeginTimestamp("ApplyPrintSettings #" + previewPageIndex);

            Size paperSize = PreviewPageImageUtility.GetPaperSize(_currPrintSettings.PaperSize);

            bool isPdfPortait = DocumentController.Instance.IsPdfPortrait;
            bool isPreviewPagePortrait = PreviewPageImageUtility.IsPreviewPagePortrait(
                _currPrintSettings.Orientation);

            if (logicalPageImages != null && logicalPageImages.Count > 0)
            {
                // Check imposition value
                if (_pagesPerSheet > 1)
                {
                    if (cancellationToken.IsCancellationRequested)
                    {
                        return;
                    }

                    PreviewPageImageUtility.OverlayImagesForImposition(canvasBitmap, previewPageSize,
                        logicalPageImages, logicalPageSize,
                        _currPrintSettings.Orientation, _currPrintSettings.Imposition,
                        _currPrintSettings.ImpositionOrder, _currPrintSettings.ScaleToFit,
                        isPdfPortait, isPreviewPagePortrait, out isPreviewPagePortrait,
                        cancellationToken);
                }
                else if (_pagesPerSheet == 1)
                {
                    PreviewPageImageUtility.OverlayImage(canvasBitmap, previewPageSize,
                        logicalPageImages[0], logicalPageSize, isPdfPortait, isPreviewPagePortrait,
                        _currPrintSettings.ScaleToFit,
                        cancellationToken);
                }
            }
            else
            {
                // Create white page
                if (cancellationToken.IsCancellationRequested)
                {
                    return;
                }

                PreviewPageImageUtility.FillWhitePageImage(canvasBitmap, previewPageSize,
                    cancellationToken);
            }

            // Check color mode value
            if (_currPrintSettings.ColorMode.Equals((int)ColorMode.Mono))
            {
                PreviewPageImageUtility.GrayscalePageImage(canvasBitmap, cancellationToken);
            }

#if PREVIEW_STAPLE
            if (_isBooklet)
            {
                if (cancellationToken.IsCancellationRequested)
                {
                    return;
                }

                PreviewPageImageUtility.FormatPageImageForBooklet(canvasBitmap, previewPageSize,
                    _currPrintSettings.BookletFinishing, isPreviewPagePortrait, isRightSide,
                    isBackSide, cancellationToken);
            }
            else if (_isDuplex)
#else // PREVIEW_STAPLE
            if (_isDuplex && !_isBooklet)
#endif // PREVIEW_STAPLE
            {
                if (cancellationToken.IsCancellationRequested)
                {
                    return;
                }

                PreviewPageImageUtility.FormatPageImageForDuplex(canvasBitmap,
                    previewPageSize, _currPrintSettings.Duplex, _currPrintSettings.FinishingSide,
                    _currPrintSettings.Punch, _selectedPrinter.EnabledPunchFour,
                    _currPrintSettings.Staple, isPdfPortait, isPreviewPagePortrait,
                    isRightSide, isBackSide, cancellationToken);
            }
            else // Not duplex and not booket
            {
                int finishingSide = _currPrintSettings.FinishingSide;
                // Change finishing side on back pages
                if (isBackSide)
                {
                    if (finishingSide == (int)FinishingSide.Left)
                    {
                        finishingSide = (int)FinishingSide.Right;
                    }
                    else if (finishingSide == (int)FinishingSide.Right)
                    {
                        finishingSide = (int)FinishingSide.Left;
                    }
                    else if (finishingSide == (int)FinishingSide.Top)
                    {
                        finishingSide = -1; // Out of range number to denote bottom
                    }
                }

#if PREVIEW_PUNCH
                // Apply punch
                if (_currPrintSettings.Punch != (int)Punch.Off)
                {
                    if (cancellationToken.IsCancellationRequested)
                    {
                        return;
                    }

                    PreviewPageImageUtility.OverlayPunch(canvasBitmap, previewPageSize,
                        _currPrintSettings.Punch, _selectedPrinter.EnabledPunchFour,
                        finishingSide, cancellationToken);
                }
#endif // PREVIEW_PUNCH

#if PREVIEW_STAPLE
                // Apply staple
                if (_currPrintSettings.Staple != (int)Staple.Off)
                {
                    if (cancellationToken.IsCancellationRequested)
                    {
                        return;
                    }

                    PreviewPageImageUtility.OverlayStaple(canvasBitmap, previewPageSize,
                        _currPrintSettings.Staple, finishingSide, false,
                        cancellationToken);
                }
#endif // PREVIEW_STAPLE
            }

            DispatcherHelper.CheckBeginInvokeOnUI(
                () =>
                {
                    _previewPageImages.Add(previewPageIndex, canvasBitmap.ToByteArray());
                });

            LogUtility.EndTimestamp("ApplyPrintSettings #" + previewPageIndex);

            return;
        }

        /// <summary>
        /// Puts the preview page image to the image source
        /// </summary>
        /// <param name="previewPageIndex">preview page index</param>
        /// <param name="enableClearPage">true to clear the page image, false otherwise (retains white fill)</param>
        /// <param name="cancellationToken">cancellation token</param>
        private void SendPreviewPageImage(int previewPageIndex, bool enableClearPage,
            CancellationTokenSource cancellationToken)
        {
            if (cancellationToken.IsCancellationRequested)
            {
                return;
            }

            if ((_isBooklet || _isDuplex) && previewPageIndex == _currLeftPageIndex)
            {
                DispatcherHelper.CheckBeginInvokeOnUI(
                    () =>
                    {
                        if (_previewPageImages.ContainsKey(_currLeftPageIndex))
                        {
                            WriteableBitmapExtensions.FromByteArray(
                                _printPreviewViewModel.LeftPageImage,
                                _previewPageImages.GetValue(_currLeftPageIndex));
                        }
                        else if (enableClearPage)
                        {
                            _printPreviewViewModel.LeftPageImage.Clear();
                        }
                        _printPreviewViewModel.IsLoadLeftPageActive = false;
                    });
            }
            else if (previewPageIndex == _currRightPageIndex)
            {
                DispatcherHelper.CheckBeginInvokeOnUI(
                () =>
                {
                    if (_previewPageImages.ContainsKey(_currRightPageIndex))
                    {
                        WriteableBitmapExtensions.FromByteArray(
                            _printPreviewViewModel.RightPageImage,
                            _previewPageImages.GetValue(_currRightPageIndex));
                    }
                    else if (enableClearPage)
                    {
                        _printPreviewViewModel.RightPageImage.Clear();
                    }
                    _printPreviewViewModel.IsLoadRightPageActive = false;
                });
            }
            else if ((_isBooklet || _isDuplex) && previewPageIndex == _currLeftBackPageIndex)
            {
                DispatcherHelper.CheckBeginInvokeOnUI(
                    () =>
                    {
                        if (_previewPageImages.ContainsKey(_currLeftBackPageIndex))
                        {
                            WriteableBitmapExtensions.FromByteArray(
                                _printPreviewViewModel.LeftBackPageImage,
                                _previewPageImages.GetValue(_currLeftBackPageIndex));
                            if (_isSwipeLeft && !_isReverseOrder) // NOTE: This condition is a workaround until back curl is properly implemented
                            {
                                WriteableBitmapExtensions.FromByteArray(
                                            _printPreviewViewModel.LeftNextPageImage,
                                            _previewPageImages.GetValue(_currLeftBackPageIndex));
                                
                            }
                            else if (!_isSwipeLeft && !_isReverseOrder) 
                            {
                                WriteableBitmapExtensions.FromByteArray(
                                            _printPreviewViewModel.LeftNextPageImage,
                                            _previewPageImages.GetValue(_currLeftBackPageIndex + 2));
                            }
                            else if (!_isSwipeLeft && _isReverseOrder)
                            {
                                WriteableBitmapExtensions.FromByteArray(
                                            _printPreviewViewModel.LeftNextPageImage,
                                            _previewPageImages.GetValue(_currLeftBackPageIndex - 2));
                            }
                            else if (_isSwipeLeft && _isReverseOrder)
                            {
                                WriteableBitmapExtensions.FromByteArray(
                                            _printPreviewViewModel.LeftNextPageImage,
                                            _previewPageImages.GetValue(_currLeftBackPageIndex ));
                            }
                        }
                        else if (!_isSwipeLeft && _currLeftBackPageIndex < 0) // NOTE: This block is a workaround until back curl is properly implemented
                        {

                            _printPreviewViewModel.LeftBackPageImage.Clear();
                            if (_currLeftBackPageIndex == -1)
                            {
                            WriteableBitmapExtensions.FromByteArray(
                                _printPreviewViewModel.LeftNextPageImage,
                                _previewPageImages.GetValue(_currLeftBackPageIndex + 2));  // NG for reverse booklet
                            }
                        }
                        else if (enableClearPage)
                        {
                            _printPreviewViewModel.LeftBackPageImage.Clear();
                            //if (_isSwipeLeft)
                            if (_isSwipeLeft && _currLeftBackPageIndex < 0)
                                _printPreviewViewModel.LeftNextPageImage.Clear();
                            else if (_currLeftBackPageIndex > 0)
                                WriteableBitmapExtensions.FromByteArray(
                                            _printPreviewViewModel.LeftNextPageImage,
                                            _previewPageImages.GetValue(_currLeftBackPageIndex - 2));
                        }
                        _printPreviewViewModel.IsLoadLeftBackPageActive = false;
                        _printPreviewViewModel.IsLoadLeftNextPageActive = false;
                    });
            }
            else if (previewPageIndex == _currRightBackPageIndex)
            {
                DispatcherHelper.CheckBeginInvokeOnUI(
                () =>
                {
                    if (_previewPageImages.ContainsKey(_currRightBackPageIndex))
                    {
                        WriteableBitmapExtensions.FromByteArray(
                            _printPreviewViewModel.RightBackPageImage,
                            _previewPageImages.GetValue(_currRightBackPageIndex));
                        if (_isBooklet || _isDuplex)
                        {
                            WriteableBitmapExtensions.FromByteArray(
                                _printPreviewViewModel.RightNextPageImage,
                                _previewPageImages.GetValue(_currRightBackPageIndex));
                        }
                    }
                    else if (enableClearPage)
                    {
                        _printPreviewViewModel.RightBackPageImage.Clear();
                        if(_isDuplex)
                            _printPreviewViewModel.RightNextPageImage.Clear();
                    }
                    _printPreviewViewModel.IsLoadRightBackPageActive = false;
                    _printPreviewViewModel.IsLoadRightNextPageActive = false;
                });
            }
        }

        #endregion Apply Print Settings

        #region Print

        public ICommand CancelPrintingCommand
        {
            get
            {
                if (_cancelPrintingCommand == null)
                {
                    _cancelPrintingCommand = new RelayCommand(
                        () => CancelPrint(),
                        () => true
                    );
                }
                return _cancelPrintingCommand;
            }
        }

        /// <summary>
        /// Event handler for Print button
        /// </summary>
        public async void Print()
        {
            if (_selectedPrinter.Id > -1)
            {
                if (NetworkController.IsConnectedToNetwork)
                {
                    // Get latest print settings since non-preview related print settings may be updated
                    _currPrintSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);

                    // Display progress dialog
                    _printingProgress = new MessageProgressBarControl("IDS_INFO_MSG_PRINTING");
                    _printingProgress.CancelCommand = CancelPrintingCommand;
                    _printingPopup = new Popup();
                    _printingPopup.Child = _printingProgress;
                    _printingPopup.IsOpen = true;

                    if (_directPrintController != null)
                    {
                        _directPrintController.UnsubscribeEvents();
                    }
                    _directPrintController = new DirectPrintController(
                        DocumentController.Instance.FileName,
                        DocumentController.Instance.PdfFile,
                        _selectedPrinter.IpAddress,
                        _currPrintSettings,
                        UpdatePrintJobProgress,
                        UpdatePrintJobResult);
                
                    _directPrintController.SendPrintJob();
                }
                else
                {
                    await DialogService.Instance.ShowError("IDS_ERR_MSG_NETWORK_ERROR",
                        "IDS_APP_NAME", "IDS_LBL_OK", null);
                }
            }
        }

        /// <summary>
        /// Event handler for Cancel button
        /// </summary>
        public async void CancelPrint()
        {
            if (_directPrintController != null)
            {
                _directPrintController.CancelPrintJob();
                _directPrintController.UnsubscribeEvents();
                _directPrintController = null;
            }
            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal,
                () =>
                {
                    _printingPopup.IsOpen = false;
                });
        }

        /// <summary>
        /// Update progress value
        /// </summary>
        /// <param name="progress">progress value</param>
        public async void UpdatePrintJobProgress(float progress)
        {
            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal,
                () =>
                {
                    _printingProgress.ProgressValue = progress;
                });
        }

        /// <summary>
        /// Processes print job result and saves the print job item to database
        /// </summary>
        /// <param name="name">print job name</param>
        /// <param name="date">date</param>
        /// <param name="result">result</param>
        public async void UpdatePrintJobResult(string name, DateTime date, int result)
        {
            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal,
                async () =>
                {
                    PrintJob printJob = new PrintJob()
                    {
                        PrinterId = _selectedPrinter.Id,
                        Name = name,
                        Date = date,
                        Result = result
                    };

                    JobController.Instance.SavePrintJob(printJob);

                    _printingPopup.IsOpen = false;
                    if (result == (int)PrintJobResult.Success)
                    {
                        await DialogService.Instance.ShowMessage("IDS_INFO_MSG_PRINT_JOB_SUCCESSFUL",
                            "IDS_APP_NAME");
                    }
                    else if (result == (int)PrintJobResult.Error)
                    {
                        await DialogService.Instance.ShowError("IDS_INFO_MSG_PRINT_JOB_FAILED",
                            "IDS_APP_NAME", "IDS_LBL_OK", null);
                    }

                    if (_directPrintController != null)
                    {
                        _directPrintController.UnsubscribeEvents();
                        _directPrintController = null;
                    }
                });
        }

        #endregion Print

    }

}

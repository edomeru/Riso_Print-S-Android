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

        // Choose printer
        public delegate void SelectedPrinterChangedEventHandler(int printerId);
        private SelectedPrinterChangedEventHandler _selectedPrinterChangedEventHandler;

        // Print button
        public delegate void PrintEventHandler();
        private PrintEventHandler _printEventHandler;

        // Cancel print
        public delegate void CancelPrintEventHandler();
        private CancelPrintEventHandler _cancelPrintEventHandler;

        // PageAreaGrid loaded
        public delegate void PageAreaGridLoadedEventHandler();
        public static PageAreaGridLoadedEventHandler PageAreaGridLoaded;

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
        private bool _isDuplex = false;
        private bool _isBooklet = false;
        private uint _previewPageTotal;
        private int _maxPreviewPageCount;
        private Size _previewPageImageSize;
        private static int _currSliderIndex;
        private static int _currLeftPageIndex;
        private static int _currRightPageIndex;
        private bool _resetPrintSettings; // Flag used only when selected printer is deleted

        private int _requestPageImageCounter = 0;

        byte[] _dummyPixels;
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
            _selectedPrinterChangedEventHandler = new SelectedPrinterChangedEventHandler(SelectedPrinterChanged);
            _printEventHandler = new PrintEventHandler(Print);
            _cancelPrintEventHandler = new CancelPrintEventHandler(CancelPrint);
            _onNavigateToEventHandler = new OnNavigateToEventHandler(RegisterPrintSettingValueChange);
            _onNavigateFromEventHandler = new OnNavigateFromEventHandler(UnregisterPrintSettingValueChange);
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
            PageAreaGridLoaded += InitializeGestures;

            // Get print settings if document is successfully loaded
            if (DocumentController.Instance.Result == LoadDocumentResult.Successful)
            {
                _previewPageTotal = DocumentController.Instance.PageCount;

                // Get initialize printer and print settings
                await GetDefaultPrinter();

                _printPreviewViewModel.SetInitialPageIndex(0);
                _printPreviewViewModel.DocumentTitleText = DocumentController.Instance.FileName;

                _selectPrinterViewModel.SelectPrinterEvent += _selectedPrinterChangedEventHandler;

                _printSettingsViewModel.ExecutePrintEventHandler += _printEventHandler;

                _printPreviewViewModel.OnNavigateFromEventHandler += _onNavigateFromEventHandler;
                _printPreviewViewModel.OnNavigateToEventHandler += _onNavigateToEventHandler;

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
            PageAreaGridLoaded -= InitializeGestures;
            _printPreviewViewModel.GoToPageEventHandler -= _goToPageEventHandler;
            PrintSettingsController.Instance.UnregisterUpdatePreviewEventHandler(_updatePreviewEventHandler);
            _selectPrinterViewModel.SelectPrinterEvent -= _selectedPrinterChangedEventHandler;
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
            // Workaround: Reload printer list on when on Print Preview screen
            new ViewModelLocator().SelectPrinterViewModel.PrinterList = PrinterController.Instance.PrinterList;

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
            // Workaround: Reload printer list on when on Print Preview screen
            new ViewModelLocator().SelectPrinterViewModel.PrinterList = null;

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
        /// Requires initial paper size before using this function.
        /// </summary>
        private void InitializeGestures()
        {
            if (DocumentController.Instance.Result == LoadDocumentResult.Successful)
            {
                Size paperSize = PreviewPageImageUtility.GetPaperSize(_currPrintSettings.PaperSize);
                bool isPortrait = PreviewPageImageUtility.IsPreviewPagePortrait(
                    _currPrintSettings.Orientation, _currPrintSettings.Imposition);

                if (_isBooklet && _currPrintSettings.BookletLayout == (int)BookletLayout.Reverse)
                {
                    _printPreviewViewModel.IsReverseSwipe = true;
                }
                else
                {
                    _printPreviewViewModel.IsReverseSwipe = false;
                }

                if ((_isBooklet && !isPortrait) ||
                    (_isDuplex && _currPrintSettings.FinishingSide == (int)FinishingSide.Top)) 
                {
                    _printPreviewViewModel.IsHorizontalSwipeEnabled = false;
                }
                else
                {
                    _printPreviewViewModel.IsHorizontalSwipeEnabled = true;
                }

                _previewPageImageSize = PreviewPageImageUtility.GetPreviewPageImageSize(paperSize,
                    isPortrait);
                _printPreviewViewModel.RightPageImage =
                    new WriteableBitmap((int)_previewPageImageSize.Width,
                        (int)_previewPageImageSize.Height);
                _printPreviewViewModel.RightPageActualSize = _previewPageImageSize;

                if (_isBooklet || _isDuplex)
                {
                    _printPreviewViewModel.LeftPageActualSize = _previewPageImageSize;
                    _printPreviewViewModel.LeftPageImage =
                        new WriteableBitmap((int)_previewPageImageSize.Width,
                            (int)_previewPageImageSize.Height);
                }
                else
                {
                    _printPreviewViewModel.LeftPageActualSize = new Size();
                }
                _printPreviewViewModel.PageViewMode = _pageViewMode;
                _printPreviewViewModel.InitializeGestures();

                _dummyPixels = _printPreviewViewModel.RightPageImage.ToByteArray();
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

            _pagesPerSheet = PreviewPageImageUtility.GetPagesPerSheet(_currPrintSettings.Imposition);

            _previewPageTotal = (uint)Math.Ceiling((decimal)DocumentController.Instance.PageCount /
                                                    _pagesPerSheet);

            if (_isBooklet)
            {
                _previewPageTotal = (_previewPageTotal / 2) + (_previewPageTotal % 2) + 2;
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
        }

        #endregion Print Preview

        #region Preview Page Navigation

        /// <summary>
        /// Event handler for page slider is changed
        /// </summary>
        /// <param name="sliderIndex">requested right page index based on slider value</param>
        public void GoToPage(int sliderIndex)
        {
            _printPreviewViewModel.IsLoadPageActive = true;
            _currSliderIndex = sliderIndex;
            LoadPage(_currSliderIndex);
        }

        /// <summary>
        /// Refreshes the preview area based on new print settings
        /// </summary>
        private void ReloadCurrentPage()
        {
            _printPreviewViewModel.IsLoadPageActive = true;
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

            _requestPageImageCounter = 2;
            int rightPageIndex = sliderIndex;
            if (_isBooklet || _isDuplex)
            {
                rightPageIndex = sliderIndex * 2;
            }

            _currLeftPageIndex = rightPageIndex - 1;
            _currRightPageIndex = rightPageIndex;
            if (_isBooklet && _currPrintSettings.BookletLayout == (int)BookletLayout.Reverse)
            {
                _currLeftPageIndex = rightPageIndex;
                _currRightPageIndex = rightPageIndex - 1;
            }

            // Generate pages to send
            GenerateSpread(_currLeftPageIndex, _currRightPageIndex, true, cancellationToken);

            //// Near pages
            //if (_currRightPageIndex + 1 < _maxPreviewPageCount)
            //{
            //    if (_isDuplex || _isBooklet)
            //    {
            //        GenerateSpread(_currRightPageIndex + 1, _currRightPageIndex + 2, false,
            //            cancellationToken);
            //    }
            //    else
            //    {
            //        GenerateSpread(-1, _currRightPageIndex + 1, false, cancellationToken);
            //    }
            //}
            //if (_currLeftPageIndex - 1 > -1)
            //{
            //    if (_isDuplex || _isBooklet)
            //    {
            //        GenerateSpread(_currLeftPageIndex - 2, _currLeftPageIndex - 1, false,
            //            cancellationToken);
            //    }
            //    else
            //    {
            //        GenerateSpread(-1, _currRightPageIndex - 1, false, cancellationToken);
            //    }
            //}

            LogUtility.EndTimestamp("LoadPage");
        }

        /// <summary>
        /// Generates PreviewPage images on a single spread
        /// </summary>
        /// <param name="leftPageIndex">left preview page index</param>
        /// <param name="rightPageIndex">right preview page index</param>
        /// <param name="enableSend">true when needs to send to preview, false otherwise</param>
        /// <param name="cancellationToken">cancellation token</param>
        private void GenerateSpread(int leftPageIndex, int rightPageIndex, bool enableSend,
            CancellationTokenSource cancellationToken)
        {
            if ((_isBooklet || _isDuplex) && leftPageIndex > -1 && leftPageIndex < _maxPreviewPageCount)
            {
                // Generate left side
                GeneratePreviewPage(leftPageIndex, leftPageIndex * _pagesPerSheet, false,
                    false, enableSend, cancellationToken);
            }
            else if (enableSend)
            {
                SendPreviewPageImage(leftPageIndex, cancellationToken);
            }

            if (rightPageIndex > -1 && rightPageIndex < _maxPreviewPageCount)
            {
                // Generate right side
                GeneratePreviewPage(rightPageIndex, rightPageIndex * _pagesPerSheet, true,
                    false, enableSend, cancellationToken);
            }
            else if (enableSend)
            {
                SendPreviewPageImage(rightPageIndex, cancellationToken);
            }
        }

        /// <summary>
        /// Generate a preview page
        /// </summary>
        /// <param name="previewPageIndex">preview page index</param>
        /// <param name="logicalPageIndex">logical page index</param>
        /// <param name="isRightSide">true when image requested is for right side, false otherwise</param>
        /// <param name="isBackSide">true if the requested page is to be displayed at the back, false otherwise</param>
        /// <param name="enableSend">true when needs to send to preview, false otherwise</param>
        /// <param name="cancellationToken">cancellation token</param>
        private async void GeneratePreviewPage(int previewPageIndex, int logicalPageIndex,
            bool isRightSide, bool isBackSide, bool enableSend, CancellationTokenSource cancellationToken)
        {
            if (cancellationToken.IsCancellationRequested)
            {
                return;
            }

            if (!_previewPageImages.ContainsKey(previewPageIndex))
            {
                List<WriteableBitmap> logicalPageImages = await DocumentController.Instance
                    .GetLogicalPageImages(logicalPageIndex, _pagesPerSheet, cancellationToken);

                Size logicalPageSize = new Size();
                logicalPageSize.Width = (logicalPageImages.Count > 0) ? logicalPageImages[0].PixelWidth : 0;
                logicalPageSize.Height = (logicalPageImages.Count > 0) ? logicalPageImages[0].PixelHeight : 0;

                WriteableBitmap canvasBitmap = new WriteableBitmap((int)_previewPageImageSize.Width,
                    (int)_previewPageImageSize.Height);

                await ThreadPool.RunAsync(
                    (workItem) =>
                    {
                        ApplyPrintSettings(canvasBitmap, _previewPageImageSize,
                            logicalPageImages, logicalPageSize,
                            previewPageIndex, isRightSide, isBackSide, enableSend,
                            cancellationToken);

                        if (enableSend)
                        {
                            SendPreviewPageImage(previewPageIndex, cancellationToken);
                        }
                    });
            }
            else
            {
                if (enableSend)
                {
                    SendPreviewPageImage(previewPageIndex, cancellationToken);
                }
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
        /// <param name="enableSend">true when needs to send to preview, false otherwise</param>
        /// <param name="cancellationToken">cancellation token</param>
        private void ApplyPrintSettings(WriteableBitmap canvasBitmap,
            Size previewPageSize, List<WriteableBitmap> logicalPageImages, Size logicalPageSize,
            int previewPageIndex, bool isRightSide, bool isBackSide, bool enableSend,
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
                        isPdfPortait, isPreviewPagePortrait, out isPreviewPagePortrait, cancellationToken);
                }
                else if (_pagesPerSheet == 1)
                {
                    PreviewPageImageUtility.OverlayImage(canvasBitmap, previewPageSize,
                        logicalPageImages[0], logicalPageSize, isPdfPortait, isPreviewPagePortrait,
                        _currPrintSettings.ScaleToFit,
                        cancellationToken);
                }

                // Check color mode value
                if (_currPrintSettings.ColorMode.Equals((int)ColorMode.Mono))
                {
                    PreviewPageImageUtility.GrayscalePageImage(canvasBitmap, cancellationToken);
                }

                //if (_isBooklet)
                //{
                //    if (cancellationToken.IsCancellationRequested)
                //    {
                //        return;
                //    }

                //    await PreviewPageImageUtility.FormatPageImageForBooklet(canvasBitmap,
                //        _currPrintSettings.BookletFinishing, isPortrait, isRightSide, isBackSide,
                //        cancellationToken);
                //}
                //else if (_isDuplex)
                if (_isDuplex && !_isBooklet)
                {
                    if (cancellationToken.IsCancellationRequested)
                    {
                        return;
                    }

                    PreviewPageImageUtility.FormatPageImageForDuplex(canvasBitmap,
                        previewPageSize,  _currPrintSettings.Duplex, _currPrintSettings.FinishingSide,
                        _currPrintSettings.Punch, _selectedPrinter.EnabledPunchFour,
                        _currPrintSettings.Staple, isPdfPortait, isPreviewPagePortrait,
                        isRightSide, isBackSide, cancellationToken);
                }
                //else // Not duplex and not booket
                //{
                //    // Apply punch
                //    if (_currPrintSettings.Punch != (int)Punch.Off)
                //    {
                //        if (cancellationToken.IsCancellationRequested)
                //        {
                //            return;
                //        }

                //        await PreviewPageImageUtility.OverlayPunch(canvasBitmap,
                //            _currPrintSettings.Punch, _selectedPrinter.EnabledPunchFour,
                //            _currPrintSettings.FinishingSide, cancellationToken);
                //    }

                //    // Apply staple
                //    if (_currPrintSettings.Staple != (int)Staple.Off)
                //    {
                //        if (cancellationToken.IsCancellationRequested)
                //        {
                //            return;
                //        }

                //        await PreviewPageImageUtility.OverlayStaple(canvasBitmap,
                //            _currPrintSettings.Staple, _currPrintSettings.FinishingSide,
                //            false, false, cancellationToken);
                //    }
                //}
            }
            else
            {
                // Create white page
                if (cancellationToken.IsCancellationRequested)
                {
                    return;
                }

                PreviewPageImageUtility.FillWhitePageImage(canvasBitmap);
            }

            if (_isBooklet || _isDuplex)
            {
                PreviewPageImageUtility.OverlayDashLineToEdge(canvasBitmap, previewPageSize,
                    !isRightSide, isPreviewPagePortrait, cancellationToken);
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
        /// <param name="cancellationToken">cancellation token</param>
        private void SendPreviewPageImage(int previewPageIndex, CancellationTokenSource cancellationToken)
        {
            if (cancellationToken.IsCancellationRequested)
            {
                return;
            }

            if (_currLeftPageIndex == previewPageIndex || _currRightPageIndex == previewPageIndex)
            {
                --_requestPageImageCounter;
            }

            if (_requestPageImageCounter == 0)
            {
                if (cancellationToken.IsCancellationRequested)
                {
                    return;
                }

                DispatcherHelper.CheckBeginInvokeOnUI(
                    () =>
                    {
                        if (_isBooklet || _isDuplex)
                        {
                            if (_previewPageImages.ContainsKey(_currLeftPageIndex))
                            {
                                WriteableBitmapExtensions.FromByteArray(
                                    _printPreviewViewModel.LeftPageImage,
                                    _previewPageImages.GetValue(_currLeftPageIndex));
                            }
                            else
                            {
                                WriteableBitmapExtensions.FromByteArray(
                                    _printPreviewViewModel.LeftPageImage, _dummyPixels);
                            }
                            _printPreviewViewModel.LeftPageImage.Invalidate();
                        }

                        if (_previewPageImages.ContainsKey(_currRightPageIndex))
                        {
                            WriteableBitmapExtensions.FromByteArray(
                                _printPreviewViewModel.RightPageImage,
                                _previewPageImages.GetValue(_currRightPageIndex));
                        }
                        else
                        {
                            WriteableBitmapExtensions.FromByteArray(
                                _printPreviewViewModel.RightPageImage, _dummyPixels);
                        }
                        _printPreviewViewModel.RightPageImage.Invalidate();

                        _printPreviewViewModel.IsLoadPageActive = false;
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
                        new ViewModelLocator().ViewControlViewModel.GoToJobsPage.Execute(null);
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

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

using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Graphics.Imaging;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.UI.Xaml.Media.Imaging;
using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using Windows.ApplicationModel.Resources;
using System.Windows.Input;
using Windows.UI.Xaml.Controls.Primitives;
using SmartDeviceApp.Controls;
using SmartDeviceApp.Common;

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

        // Authentication PIN Code
        public delegate void PinCodeValueChangedEventHandler(string pinCode);
        private PinCodeValueChangedEventHandler _pinCodeValueChangedEventHandler;

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
        private const string PREFIX_PREVIEW_PAGE_IMAGE = "previewpage";
        private const string FORMAT_PREFIX_PREVIEW_PAGE_IMAGE_WITH_INDEX =
            PREFIX_PREVIEW_PAGE_IMAGE + "{0:0000}";
        private const string FORMAT_FILE_NAME_PREVIEW_PAGE_IMAGE =
            FORMAT_PREFIX_PREVIEW_PAGE_IMAGE_WITH_INDEX + "-{1:yyyyMMddHHmmssffff}.jpg";
        private const string FILE_PATH_RES_IMAGE_STAPLE = "Resources/Images/img_staple.png";
        private const string FILE_PATH_RES_IMAGE_PUNCH = "Resources/Images/img_punch.png";

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
        private bool _isReversePages = false;
        private Dictionary<int, PreviewPage> _previewPages; // Generated PreviewPages from the start
        private uint _previewPageTotal;
        private static int _currPreviewPageIndex;
        private bool _resetPrintSettings; // Flag used only when selected printer is deleted

        private ICommand _cancelPrintingCommand;
        private Popup _printingPopup;
        private MessageProgressBarControl _printingProgress;

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

            _previewPages = new Dictionary<int, PreviewPage>();

            _updatePreviewEventHandler = new UpdatePreviewEventHandler(UpdatePreview);
            _goToPageEventHandler = new GoToPageEventHandler(GoToPage);
            _selectedPrinterChangedEventHandler = new SelectedPrinterChangedEventHandler(SelectedPrinterChanged);
            _pinCodeValueChangedEventHandler = new PinCodeValueChangedEventHandler(PinCodeValueChanged);
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

                new ViewModelLocator().SelectPrinterViewModel.PrinterList = PrinterController.Instance.PrinterList;

                // Get initialize printer and print settings
                await GetDefaultPrinter();

                _resetPrintSettings = false;
                _currPreviewPageIndex = 0;
                _printPreviewViewModel.SetInitialPageIndex(0);
                _printPreviewViewModel.DocumentTitleText = DocumentController.Instance.FileName;

                _selectPrinterViewModel.SelectPrinterEvent += _selectedPrinterChangedEventHandler;

                _printSettingsViewModel.PinCodeValueChangedEventHandler += _pinCodeValueChangedEventHandler;
                _printSettingsViewModel.ExecutePrintEventHandler += _printEventHandler;

                _printPreviewViewModel.OnNavigateFromEventHandler += _onNavigateFromEventHandler;
                _printPreviewViewModel.OnNavigateToEventHandler += _onNavigateToEventHandler;

                PrinterController.Instance.DeletePrinterItemsEventHandler += PrinterDeleted;
            }
            else if (DocumentController.Instance.Result == LoadDocumentResult.UnsupportedPdf)
            {
                (new ViewModelLocator().HomeViewModel).IsProgressRingActive = false;
                await DialogService.Instance.ShowError("IDS_ERR_MSG_PDF_ENCRYPTED", "IDS_APP_NAME", "IDS_LBL_OK", null);
            }
            else // DocumentController.Instance.Result == LoadDocumentResult.ErrorReadPdf or LoadDocumentResult.NotStarted
            {
                (new ViewModelLocator().HomeViewModel).IsProgressRingActive = false;
                await DialogService.Instance.ShowError("IDS_ERR_MSG_OPEN_FAILED", "IDS_APP_NAME", "IDS_LBL_OK", null);
            }
        }

        /// <summary>
        /// Clean-up
        /// </summary>
        /// <returns>task</returns>
        public async Task Cleanup()
        {
            _printPreviewViewModel.GoToPageEventHandler -= _goToPageEventHandler;
            PrintSettingsController.Instance.UnregisterUpdatePreviewEventHandler(_updatePreviewEventHandler);
            _selectPrinterViewModel.SelectPrinterEvent -= _selectedPrinterChangedEventHandler;
            _printSettingsViewModel.PinCodeValueChangedEventHandler -= _pinCodeValueChangedEventHandler;
            _printSettingsViewModel.ExecutePrintEventHandler -= _printEventHandler;
            PrinterController.Instance.DeletePrinterItemsEventHandler -= PrinterDeleted;

            _resetPrintSettings = false;
            _selectedPrinter = null;
            await ClearPreviewPageListAndImages();

            _pagesPerSheet = 1;
            _isDuplex = false;
            _isBooklet = false;
            _isReversePages = false;

            _printPreviewViewModel.Cleanup();
        }

        /// <summary>
        /// Resets the generated PreviewPage(s) list and removed the page images from AppData
        /// temporary store.
        /// </summary>
        /// <returns>task</returns>
        private async Task ClearPreviewPageListAndImages()
        {
            StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;
            await StorageFileUtility.DeleteFiles(PREFIX_PREVIEW_PAGE_IMAGE, tempFolder);

            _previewPages.Clear();
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
                await SetSelectedPrinterAndPrintSettings(-1);
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
        /// <param name="printerId"></param>
        public async void SelectedPrinterChanged(int printerId)
        {
            await SetSelectedPrinterAndPrintSettings(printerId);
        }

        /// <summary>
        /// Event handler for PIN code text
        /// </summary>
        /// <param name="pinCode"></param>
        public void PinCodeValueChanged(string pinCode)
        {
            PrintSettingsController.Instance.SetPinCode(_screenName, pinCode);
        }

        /// <summary>
        /// Retrieves the default printer.
        /// If no default printer is found, a dummy printer (with ID = -1) is set as
        /// selected printer.
        /// </summary>
        /// <returns>task</returns>
        private async Task GetDefaultPrinter()
        {
            // TODo: Check if this function should use PrinterController (avoid conflicting roles)
            // Get default printer
            DefaultPrinter defaultPrinter = await DatabaseController.Instance.GetDefaultPrinter();

            if (defaultPrinter != null)
            {
                await SetSelectedPrinterAndPrintSettings((int)defaultPrinter.PrinterId);
            }
            else
            {
                _selectedPrinter = null;
                await SetSelectedPrinterAndPrintSettings(-1);
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
                _selectedPrinter = await DatabaseController.Instance.GetPrinter(printerId);
            }
            if (_selectedPrinter == null)
            {
                // Use dummy printer (meaning no selected printer)
                _selectedPrinter = new Printer();
            }

            _printSettingsViewModel.PrinterName = _selectedPrinter.Name;
            _printSettingsViewModel.PrinterId = _selectedPrinter.Id;
            _printSettingsViewModel.PrinterIpAddress = _selectedPrinter.IpAddress;

            PrintSettingsController.Instance.Uninitialize(_screenName);
            _currPrintSettings = await PrintSettingsController.Instance.Initialize(_screenName, _selectedPrinter);
            PrintSettingsController.Instance.RegisterUpdatePreviewEventHandler(_updatePreviewEventHandler);
            await ReloadCurrentPage();

            _printSettingsViewModel.PrinterId = _selectedPrinter.Id;
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
                Size paperSize = GetPaperSize(_currPrintSettings.PaperSize);
                bool isPortrait = IsPortrait(_currPrintSettings.Orientation,
                    _currPrintSettings.BookletLayout);

                Size sampleSize = GetPreviewPageImageSize(paperSize, isPortrait);
                _printPreviewViewModel.RightPageActualSize = sampleSize;
                if (_isBooklet)
                {
                    _printPreviewViewModel.LeftPageActualSize = sampleSize;
                }
                else
                {
                    _printPreviewViewModel.LeftPageActualSize = new Size();
                }
                _printPreviewViewModel.InitializeGestures();
            }
        }

        /// <summary>
        /// Event handler that receives modified print setting to update preview
        /// </summary>
        /// <param name="printSetting">affected print setting</param>
        public async void UpdatePreview(PrintSetting printSetting)
        {
            if (printSetting == null)
            {
                return;
            }

            _currPrintSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);

            string name = printSetting.Name;

            if (printSetting.Name.Equals(PrintSettingConstant.NAME_VALUE_DUPLEX) ||
                printSetting.Name.Equals(PrintSettingConstant.NAME_VALUE_IMPOSITION) ||
                printSetting.Name.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT) ||
                printSetting.Name.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET))
            {
                _currPreviewPageIndex = 0; // TODO: Proper handling when total page count changes
            }

            await ReloadCurrentPage();
        }

        /// <summary>
        /// Checks for view related print setting and notifies view model
        /// </summary>
        private void UpdatePreviewInfo()
        {
            // Send UI related items
            if (_currPrintSettings.Booklet)
            {
                _isBooklet = true;
                _printPreviewViewModel.PageViewMode = PageViewMode.TwoPageView;
            }
            else
            {
                _isBooklet = false;
                _printPreviewViewModel.PageViewMode = PageViewMode.SinglePageView;
            }

            _isDuplex = (_currPrintSettings.Duplex != (int)Duplex.Off) ||
                (_isBooklet && _currPrintSettings.BookletFinishing == (int)BookletFinishing.Off);

            _isReversePages = _isBooklet &&
                _currPrintSettings.BookletLayout == (int)BookletLayout.RightToLeft;

            _pagesPerSheet = PrintSettingsController.Instance.GetPagesPerSheet(_screenName);

            _previewPageTotal = (uint)Math.Ceiling((decimal)DocumentController.Instance.PageCount /
                                                    _pagesPerSheet);
            uint sliderMaxValue = _previewPageTotal;
            if (_isDuplex)
            {
                sliderMaxValue = (_previewPageTotal / 2) + (_previewPageTotal % 2);
            }
            else if (_isBooklet)
            {
                sliderMaxValue = (_previewPageTotal / 2) + 1;
            }
            if (_printPreviewViewModel.PageTotal != sliderMaxValue)
            {
                _printPreviewViewModel.GoToPageEventHandler -= _goToPageEventHandler;
                if (_currPreviewPageIndex > _previewPageTotal)
                {
                    _currPreviewPageIndex = (int)_previewPageTotal - 1;
                }
                _printPreviewViewModel.PageTotal = sliderMaxValue;
                _printPreviewViewModel.GoToPageEventHandler += _goToPageEventHandler;
            }
        }

        /// <summary>
        /// Checks if the orientation is portrait based on selected orientation (when booklet is off)
        /// or based on selected booklet layout (when booklet is on)
        /// </summary>
        /// <param name="orientation">orientation</param>
        /// <param name="bookletLayout">booklet layout</param>
        /// <returns>true when portrait, false otherwise</returns>
        private bool IsPortrait(int orientation, int bookletLayout)
        {
            bool isPortrait = (orientation == (int)Orientation.Portrait);
            if (_isBooklet)
            {
                isPortrait = (bookletLayout != (int)BookletLayout.TopToBottom);
            }

            return isPortrait;
        }

        #endregion Print Preview

        #region Preview Page Navigation

        /// <summary>
        /// Event handler for page slider is changed
        /// </summary>
        /// <param name="rightPageIndex">requested right page index based on slider value</param>
        public async void GoToPage(int rightPageIndex)
        {
            _currPreviewPageIndex = (_isDuplex || _isBooklet) ? rightPageIndex * 2 :
                                                      rightPageIndex;
            await LoadPage(_currPreviewPageIndex);
        }

        /// <summary>
        /// Refreshes the preview area based on new print settings
        /// </summary>
        /// <returns>task</returns>
        private async Task ReloadCurrentPage()
        {
            // Generate PreviewPages again
            await ClearPreviewPageListAndImages();
            UpdatePreviewInfo();
            _printPreviewViewModel.UpdatePageIndexes((uint)_currPreviewPageIndex);
            InitializeGestures();
            await LoadPage(_currPreviewPageIndex);
        }

        /// <summary>
        /// Requests for LogicalPages and then applies print setting for the target page only.
        /// Assumes that requested page index is for right side page index
        /// </summary>
        /// <param name="rightPageIndex">requested right page index based on slider value</param>
        /// <returns>task</returns>
        private async Task LoadPage(int rightPageIndex)
        {
            // TODO: Add current page logic
            _printPreviewViewModel.IsLoadPageActive = true;

            _printPreviewViewModel.RightPageImage = new BitmapImage();
            _printPreviewViewModel.LeftPageImage = new BitmapImage();

            // Generate pages to send
            await GenerateSingleSpread(rightPageIndex, true);

            // TODO: Add current page logic
            _printPreviewViewModel.IsLoadPageActive = false;

            GenerateNearPreviewPages(rightPageIndex);
        }

        /// <summary>
        /// Generates PreviewPage images on a single spread
        /// </summary>
        /// <param name="rightPageIndex">right page index based on slider value</param>
        /// <param name="enableSend"></param>
        /// <returns></returns>
        private async Task GenerateSingleSpread(int rightPageIndex, bool enableSend)
        {
            // When booklet is on and booklet finishing is off, act like as duplex (short edge)
            // so no need for left side
            if (_isBooklet) // && _currPrintSettings.BookletFinishing != (int)BookletFinishing.Off)
            {
                // Compute left side page index
                int leftSidePreviewPageIndex = rightPageIndex - 1;
                if (leftSidePreviewPageIndex > 0)
                {
                    // Generate left side
                    await GenerateSingleLeaf(leftSidePreviewPageIndex, false, enableSend);
                }
            }

            if (rightPageIndex < _previewPageTotal)
            {
                // Generate right side
                await GenerateSingleLeaf(rightPageIndex, true, enableSend);
            }
        }

        /// <summary>
        /// Generates a single leaf page
        /// </summary>
        /// <param name="pageIndex">page index</param>
        /// <param name="isRightSide">true when image requested is for right side, false otherwise</param>
        /// <param name="enableSend">true when needs to send to preview, false otherwise</param>
        /// <returns>task</returns>
        private async Task GenerateSingleLeaf(int pageIndex, bool isRightSide, bool enableSend)
        {
            // Compute for logical page index based on imposition
            int logicalPageIndex = pageIndex * _pagesPerSheet;
            if (_isReversePages)
            {
                logicalPageIndex = (int)DocumentController.Instance.PageCount - 1 - logicalPageIndex;
            }

            if (enableSend)
            {
                // Front
                await SendPreviewPage(pageIndex, logicalPageIndex, isRightSide, false);
            }
            else
            {
                await GenerateNearPreviewPage(pageIndex, logicalPageIndex, isRightSide, false);
            }

            if (_isDuplex || _isBooklet)
            {
                int backPreviewPageIndex;

                if (isRightSide) // Back page is next page
                {
                    backPreviewPageIndex = pageIndex + 1;
                }
                else // Back page is previous page
                {
                    backPreviewPageIndex = pageIndex - 1;
                }

                if (backPreviewPageIndex > 0 || backPreviewPageIndex < _previewPageTotal)
                {
                    // Compute for next logical page index based on imposition
                    int nextLogicalPageIndex = backPreviewPageIndex * _pagesPerSheet;

                    // Back
                    if (enableSend)
                    {
                        await SendPreviewPage(backPreviewPageIndex, nextLogicalPageIndex, isRightSide, true);
                    }
                    else
                    {
                        await GenerateNearPreviewPage(backPreviewPageIndex, nextLogicalPageIndex, isRightSide, false);
                    }
                }
            }
        }

        /// <summary>
        /// Sends the PreviewPage image
        /// </summary>
        /// <param name="previewPageIndex">target preview page image</param>
        /// <param name="logicalPageIndex">target logical page image</param>
        /// <param name="isRightSide">true when image requested is for right side, false otherwise</param>
        /// <param name="isBackSide">true when duplex is on and is for back side, false otherwise</param>
        /// <returns>task</returns>
        private async Task SendPreviewPage(int previewPageIndex, int logicalPageIndex,
            bool isRightSide, bool isBackSide)
        {
            bool sent = await SendExistingPreviewImage(previewPageIndex, isRightSide, isBackSide);
            if (!sent)
            {
                // Generate pages, apply print settings then send
                await DocumentController.Instance.GenerateLogicalPages(logicalPageIndex, _pagesPerSheet);
                List<LogicalPage> logicalPages = await DocumentController.Instance
                    .GetLogicalPages(logicalPageIndex, _pagesPerSheet);
                await ApplyPrintSettings(logicalPages, previewPageIndex, isRightSide, isBackSide, true);
            }
        }

        /// <summary>
        /// Checks if a PreviewPage image already exists then opens and sends the page image
        /// </summary>
        /// <param name="targetPreviewPageIndex">target page index</param>
        /// <param name="isRightSide">true when image requested is for right side, false otherwise</param>
        /// <param name="isBackSide">true if the requested page is to be displayed at the back, false otherwise</param>
        /// <returns>task; true if the PreviewPage image already exists, false otherwise</returns>
        private async Task<bool> SendExistingPreviewImage(int targetPreviewPageIndex, bool isRightSide,
            bool isBackSide)
        {
            PreviewPage previewPage = null;
            if (_previewPages.TryGetValue(targetPreviewPageIndex, out previewPage))
            {
                // Get existing file from AppData temporary store
                StorageFile jpegFile = await StorageFileUtility.GetExistingFile(previewPage.Name,
                    ApplicationData.Current.TemporaryFolder);
                if (jpegFile != null)
                {
                    // Open the bitmap
                    BitmapImage bitmapImage = new BitmapImage(new Uri(jpegFile.Path));

                    if (isRightSide && !isBackSide)
                    {
                        _printPreviewViewModel.RightPageImage = bitmapImage;
                        _printPreviewViewModel.RightPageActualSize = previewPage.ActualSize;
                        return true;
                    }
                    if (!isRightSide && !isBackSide)
                    {
                        _printPreviewViewModel.LeftPageImage = bitmapImage;
                        _printPreviewViewModel.LeftPageActualSize = previewPage.ActualSize;
                        return true;
                    }
                    if (isRightSide && isBackSide)
                    {
                        // TODO: Send to appropriate page image side
                        return true;
                    }
                    if (!isRightSide && isBackSide)
                    {
                        // TODO: Send to appropriate page image side
                        return true;
                    }
                }
            }
            return false;
        }

        /// <summary>
        /// Generates next and previous PreviewPage images if not exist
        /// </summary>
        /// <param name="rightPageIndex"></param>
        private async void GenerateNearPreviewPages(int rightPageIndex)
        {
            await GenerateSingleSpread(rightPageIndex + 1, false);
            await GenerateSingleSpread(rightPageIndex - 1, false);
        }

        /// <summary>
        /// Generate near pages
        /// </summary>
        /// <param name="previewPageIndex">PreviewPage index</param>
        /// <param name="logicalPageIndex">LogicalPage index</param>
        /// <param name="isRightSide">true when image requested is for right side, false otherwise</param>
        /// <param name="isBackSide">true if the requested page is to be displayed at the back, false otherwise</param>
        /// <returns>task</returns>
        private async Task GenerateNearPreviewPage(int previewPageIndex, int logicalPageIndex,
            bool isRightSide, bool isBackSide)
        {
            if (!_previewPages.ContainsKey(previewPageIndex))
            {
                await DocumentController.Instance.GenerateLogicalPages(logicalPageIndex, _pagesPerSheet);
                List<LogicalPage> logicalPages = await DocumentController.Instance
                    .GetLogicalPages(logicalPageIndex, _pagesPerSheet);
                await ApplyPrintSettings(logicalPages, previewPageIndex, isRightSide, isBackSide, false);
            }
        }

        #endregion Preview Page Navigation

        #region Apply Print Settings

        /// <summary>
        /// Applies print settings to LogicalPage images then creates a PreviewPage
        /// </summary>
        /// <param name="logicalPages">source LogicalPage images</param>
        /// <param name="previewPageIndex">target preview page index</param>
        /// <param name="isRightSide">true when image requested is for right side, false otherwise</param>
        /// <param name="isBackSide">true when duplex is on and is for back side, false otherwise</param>
        /// <param name="enableSend">true when needs to send to preview, false otherwise</param>
        /// <returns>task</returns>
        private async Task ApplyPrintSettings(List<LogicalPage> logicalPages, int previewPageIndex,
            bool isRightSide, bool isBackSide, bool enableSend)
        {
            if (logicalPages != null && logicalPages.Count > 0)
            {
                StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;

                WriteableBitmap finalBitmap = new WriteableBitmap(1, 1); // Size does not matter yet
                List<WriteableBitmap> pageImages = new List<WriteableBitmap>(); // Ordered list

                Size paperSize = GetPaperSize(_currPrintSettings.PaperSize);

                bool isPortrait = IsPortrait(_currPrintSettings.Orientation,
                    _currPrintSettings.BookletLayout);

                // Loop to each LogicalPage(s) to selected paper size and orientation
                foreach (LogicalPage logicalPage in logicalPages)
                {
                    // Open PreviewPage image from AppData temporary store
                    string pageImageFileName = logicalPage.Name;
                    try
                    {
                        StorageFile jpgFile = await tempFolder.GetFileAsync(pageImageFileName);
                        using (IRandomAccessStream raStream = await jpgFile.OpenReadAsync())
                        {
                            // Put LogicalPage image to a bitmap
                            WriteableBitmap pageBitmap = await WriteableBitmapExtensions.FromStream(
                                null, raStream);

                            if (_pagesPerSheet > 1)
                            {
                                pageImages.Add(pageBitmap);
                            }
                            else if (_pagesPerSheet == 1)
                            {
                                WriteableBitmap canvasBitmap = ApplyPaperSizeAndOrientation(paperSize,
                                    isPortrait);
                                ApplyPageImageToPaper(_currPrintSettings.ScaleToFit,
                                    canvasBitmap, pageBitmap);
                                pageImages.Add(canvasBitmap);
                            }
                        }
                    }
                    catch (Exception)
                    {
                        // Error handling (UnauthorizedAccessException)
                    }
                }

                bool isFinalPortrait = isPortrait;
                // Check imposition value
                if (_pagesPerSheet > 1)
                {
                    finalBitmap = ApplyImposition(paperSize, pageImages, isPortrait,
                        _currPrintSettings.ImpositionOrder, out isFinalPortrait);
                }
                else if (_pagesPerSheet == 1)
                {
                    finalBitmap = WriteableBitmapExtensions.Clone(pageImages[0]);
                }

                // Check color mode value
                if (_currPrintSettings.ColorMode.Equals((int)ColorMode.Mono))
                {
                    ApplyMonochrome(finalBitmap);
                }

                int finishingSide = _currPrintSettings.FinishingSide;
                int holeCount = GetPunchHoleCount(_currPrintSettings.Punch);
                int staple = _currPrintSettings.Staple;

                if (_isDuplex) // Also hit when booklet is on and booklet finishing is off
                {
                    await ApplyDuplex(finalBitmap, _currPrintSettings.Duplex,
                        finishingSide, holeCount, staple, isFinalPortrait, isBackSide);
                }
                else if (_isBooklet)
                {
                    await ApplyBooklet(finalBitmap, _currPrintSettings.BookletFinishing,
                        isFinalPortrait, isBackSide, isRightSide);
                }
                else // Not duplex and not booket
                {
                    // Apply punch
                    if (holeCount > 0)
                    {
                        await ApplyPunch(finalBitmap, holeCount, finishingSide);
                    }

                    // Apply staple
                    if (staple != (int)Staple.Off)
                    {
                        await ApplyStaple(finalBitmap, staple, finishingSide);
                    }
                }

                try
                {
                    // Save PreviewPage into AppData temporary store
                    StorageFile tempPageImage = await tempFolder.CreateFileAsync(
                        String.Format(FORMAT_FILE_NAME_PREVIEW_PAGE_IMAGE, previewPageIndex, DateTime.UtcNow),
                        CreationCollisionOption.GenerateUniqueName);
                    using (var destinationStream =
                        await tempPageImage.OpenAsync(FileAccessMode.ReadWrite))
                    {
                        BitmapEncoder newEncoder = await BitmapEncoder.CreateAsync(
                            BitmapEncoder.JpegEncoderId, destinationStream);
                        byte[] pixels = WriteableBitmapExtensions.ToByteArray(finalBitmap);
                        newEncoder.SetPixelData(BitmapPixelFormat.Bgra8, BitmapAlphaMode.Ignore,
                            (uint)finalBitmap.PixelWidth, (uint)finalBitmap.PixelHeight,
                            ImageConstant.BASE_DPI, ImageConstant.BASE_DPI, pixels);
                        await newEncoder.FlushAsync();
                    }

                    PreviewPage previewPage = new PreviewPage((uint)previewPageIndex,
                        tempPageImage.Name, new Size(finalBitmap.PixelWidth, finalBitmap.PixelHeight));

                    // Check if needs to send the page image
                    // Don't bother to send the old requests
                    if (enableSend &&
                        ((isRightSide && _currPreviewPageIndex == previewPageIndex) ||
                         (!isRightSide && _currPreviewPageIndex - 1 == previewPageIndex)))
                    {
                        // Open the bitmap
                        BitmapImage bitmapImage = new BitmapImage(new Uri(tempPageImage.Path));

                        if (isRightSide && !isBackSide)
                        {
                            _printPreviewViewModel.RightPageImage = bitmapImage;
                            _printPreviewViewModel.RightPageActualSize = previewPage.ActualSize;
                        }
                        else if (!isRightSide && !isBackSide)
                        {
                            _printPreviewViewModel.LeftPageImage = bitmapImage;
                            _printPreviewViewModel.LeftPageActualSize = previewPage.ActualSize;
                        }
                        else if (isRightSide && isBackSide)
                        {
                            // TODO: Send to appropriate side
                        }
                        else if (!isRightSide && isBackSide)
                        {
                            // TODO: Send to appropriate side
                        }
                    }

                    // Update PreviewPage list
                    if (_previewPages.ContainsKey(previewPageIndex))
                    {
                        // Delete old images
                        await StorageFileUtility.DeleteFilesExcept(
                            string.Format(FORMAT_PREFIX_PREVIEW_PAGE_IMAGE_WITH_INDEX, previewPageIndex),
                            previewPage.Name, tempFolder);
                        // Overwrite the new entry from the list
                        _previewPages[previewPageIndex] = previewPage;
                    }
                    else
                    {
                        _previewPages.Add(previewPageIndex, previewPage);
                    }
                }
                catch (Exception)
                {
                    // Error handling
                }
            }
        }

        /// <summary>
        /// Creates a bitmap based on target paper size and orientation
        /// </summary>
        /// <param name="paperSize">target paper size</param>
        /// <param name="isPortrait">orientation</param>
        /// <returns>bitmap filled with white</returns>
        private WriteableBitmap ApplyPaperSizeAndOrientation(Size paperSize, bool isPortrait)
        {
            Size pageImageSize = GetPreviewPageImageSize(paperSize, isPortrait);

            // Create canvas based on paper size and orientation
            WriteableBitmap canvasBitmap = new WriteableBitmap((int)pageImageSize.Width,
                (int)pageImageSize.Height);
            // Fill all white
            WriteableBitmapExtensions.FillRectangle(canvasBitmap, 0, 0, (int)pageImageSize.Width,
                (int)pageImageSize.Height, Windows.UI.Colors.White);

            return canvasBitmap;
        }

        /// <summary>
        /// Retrieves the target PreviewPage image size
        /// </summary>
        /// <param name="paperSize">selected paper size</param>
        /// <param name="isPortrait">true if portrait, false otherwise</param>
        /// <returns>size of the PreviewPage image</returns>
        private Size GetPreviewPageImageSize(Size paperSize, bool isPortrait)
        {
            // Get paper size and apply DPI
            double length1 = (paperSize.Width * ImageConstant.FACTOR_MM_TO_IN) * ImageConstant.BASE_DPI;
            double length2 = (paperSize.Height * ImageConstant.FACTOR_MM_TO_IN) * ImageConstant.BASE_DPI;

            Size pageImageSize = new Size();
            // Check orientation
            if (isPortrait)
            {
                pageImageSize.Width = length1;
                pageImageSize.Height = length2;
            }
            else
            {
                pageImageSize.Width = length2;
                pageImageSize.Height = length1;
            }

            return pageImageSize;
        }

        /// <summary>
        /// Puts LogicalPage image into the bitmap
        /// </summary>
        /// <param name="enableScaleToFit">scale to fit setting</param>
        /// <param name="canvasBitmap">PreviewPage image</param>
        /// <param name="pageBitmap">LogicalPage image</param>
        private void ApplyPageImageToPaper(bool enableScaleToFit, WriteableBitmap canvasBitmap,
            WriteableBitmap pageBitmap)
        {
            if (enableScaleToFit)
            {
                ApplyScaleToFit(canvasBitmap, pageBitmap, false);
            }
            else
            {
                ApplyImageToPaper(canvasBitmap, pageBitmap);
            }
        }

        /// <summary>
        /// Scales the LogicalPage image into the PreviewPage image
        /// </summary>
        /// <param name="canvasBitmap">target page image placement</param>
        /// <param name="pageBitmap">page image to be fitted</param>
        /// <param name="addBorder">true when border is added to fitted image, false otherwise</param>
        private void ApplyScaleToFit(WriteableBitmap canvasBitmap, WriteableBitmap pageBitmap,
            bool addBorder)
        {
            double scaleX = (double)canvasBitmap.PixelWidth / pageBitmap.PixelWidth;
            double scaleY = (double)canvasBitmap.PixelHeight / pageBitmap.PixelHeight;
            double targetScaleFactor = (scaleX < scaleY) ? scaleX : scaleY;

            // Scale the LogicalPage image
            WriteableBitmap scaledBitmap = WriteableBitmapExtensions.Resize(pageBitmap,
                (int)(pageBitmap.PixelWidth * targetScaleFactor),
                (int)(pageBitmap.PixelHeight * targetScaleFactor),
                WriteableBitmapExtensions.Interpolation.Bilinear);
            if (addBorder)
            {
                ApplyBorder(scaledBitmap, 0, 0, scaledBitmap.PixelWidth,
                    scaledBitmap.PixelHeight);
            }

            // Compute position in PreviewPage image
            Rect srcRect = new Rect(0, 0, scaledBitmap.PixelWidth, scaledBitmap.PixelHeight);
            Rect destRect = new Rect(
                (canvasBitmap.PixelWidth - scaledBitmap.PixelWidth) / 2,    // Puts the image to the center X
                (canvasBitmap.PixelHeight - scaledBitmap.PixelHeight) / 2,  // Puts the image to the center Y
                scaledBitmap.PixelWidth, scaledBitmap.PixelHeight);
            WriteableBitmapExtensions.Blit(canvasBitmap, destRect, scaledBitmap, srcRect);
        }

        /// <summary>
        /// Applies border to the image
        /// </summary>
        /// <param name="canvasBitmap">bitmap image</param>
        /// <param name="xOrigin">starting position</param>
        /// <param name="yOrigin">starting position</param>
        /// <param name="width">length along x-axis</param>
        /// <param name="height">length along y-axis</param>
        private void ApplyBorder(WriteableBitmap canvasBitmap, int xOrigin, int yOrigin, int width,
            int height)
        {
            WriteableBitmapExtensions.DrawRectangle(canvasBitmap, xOrigin, xOrigin,
                    width, height, Windows.UI.Colors.Black);
        }

        /// <summary>
        /// Puts the LogicalPage image into PreviewPage as is (cropping the excess area)
        /// </summary>
        /// <param name="canvasBitmap">PreviewPage image</param>
        /// <param name="pageBitmap">LogicalPage image</param>
        private void ApplyImageToPaper(WriteableBitmap canvasBitmap, WriteableBitmap pageBitmap)
        {
            // Determine LogicalPage sizes if cropping is needed
            // If not cropped, LogicalPage just fits into paper
            int cropWidth = canvasBitmap.PixelWidth;
            if (canvasBitmap.PixelWidth > pageBitmap.PixelWidth)
            {
                cropWidth = pageBitmap.PixelWidth;
            }
            int cropHeight = canvasBitmap.PixelHeight;
            if (canvasBitmap.PixelHeight > pageBitmap.PixelHeight)
            {
                cropHeight = pageBitmap.PixelHeight;
            }

            // Source and destination rectangle are the same since
            // LogicalPage is cropped using the rectangle and put as in into the paper
            Rect rect = new Rect(0, 0, cropWidth, cropHeight);

            // Place image into paper
            WriteableBitmapExtensions.Blit(canvasBitmap, rect, pageBitmap, rect);
        }

        /// <summary>
        /// Applies imposition (uses selected imposition order).
        /// Imposition images are assumed to be applied with selected paper size and orientation.
        /// The page images are assumed to be in order based on LogicalPage index.
        /// </summary>
        /// <param name="paperSize">selected paper size used in scaling the imposition page images</param>
        /// <param name="pageImages">imposition page images</param>
        /// <param name="isPortrait">selected orientation; true if portrait, false otherwise</param>
        /// <param name="impositionOrder">direction of imposition</param>
        /// <returns>page image with applied imposition value
        /// Final output page image is
        /// * portrait when imposition value is 4-up
        /// * otherwise landscape</returns>
        private WriteableBitmap ApplyImposition(Size paperSize, List<WriteableBitmap> pageImages,
            bool isPortrait, int impositionOrder, out bool isImpositionPortrait)
        {
            // Determine final orientation based on imposition
            bool isPagesPerSheetPerfectSquare = (Math.Sqrt(_pagesPerSheet) % 1) == 0;
            isImpositionPortrait = (isPagesPerSheetPerfectSquare) ? isPortrait : !isPortrait;
            // Create target page image based on imposition
            WriteableBitmap canvasBitmap = ApplyPaperSizeAndOrientation(paperSize, isImpositionPortrait);

            // Compute number of pages per row and column
            int pagesPerRow = 0;
            int pagesPerColumn = 0;
            if (isImpositionPortrait)
            {
                pagesPerColumn = (int)Math.Sqrt(_pagesPerSheet);
                pagesPerRow = _pagesPerSheet / pagesPerColumn;
            }
            else
            {
                pagesPerRow = (int)Math.Sqrt(_pagesPerSheet);
                pagesPerColumn = _pagesPerSheet / pagesPerRow;
            }

            // Compute page area size and margin
            double marginPaper = PrintSettingConstant.MARGIN_IMPOSITION_EDGE * ImageConstant.BASE_DPI;
            double marginBetweenPages = PrintSettingConstant.MARGIN_IMPOSITION_BETWEEN_PAGES * ImageConstant.BASE_DPI;
            Size impositionPageAreaSize = GetImpositionSinglePageAreaSize(canvasBitmap.PixelWidth,
                canvasBitmap.PixelHeight, pagesPerRow, pagesPerColumn,
                marginBetweenPages, marginPaper);

            // Set initial positions
            double initialOffsetX = 0;
            double initialOffsetY = 0;
            if (impositionOrder == (int)ImpositionOrder.FourUpUpperRightToBottom ||
                impositionOrder == (int)ImpositionOrder.FourUpUpperRightToLeft ||
                impositionOrder == (int)ImpositionOrder.TwoUpRightToLeft)
            {
                initialOffsetX = (marginBetweenPages * (pagesPerColumn - 1)) +
                    (impositionPageAreaSize.Width * (pagesPerColumn - 1));
            }
            if (impositionOrder == (int)ImpositionOrder.TwoUpRightToLeft &&
                isImpositionPortrait)
            {
                initialOffsetY = (marginBetweenPages * (pagesPerRow - 1)) +
                    (impositionPageAreaSize.Height * (pagesPerRow - 1));
            }

            // Loop each imposition page
            int impositionPageIndex = 0;
            double pageImageOffsetX = initialOffsetX;
            double pageImageOffsetY = initialOffsetY;
            foreach (WriteableBitmap impositionPageBitmap in pageImages)
            {
                // Put imposition page image in center of imposition page area
                double x = marginPaper + pageImageOffsetX;
                double y = marginPaper + pageImageOffsetY;

                // Scale imposition page
                WriteableBitmap scaledImpositionPageBitmap =
                    new WriteableBitmap((int)impositionPageAreaSize.Width,
                        (int)impositionPageAreaSize.Height);
                WriteableBitmapExtensions.FillRectangle(scaledImpositionPageBitmap, 0, 0,
                    scaledImpositionPageBitmap.PixelWidth, scaledImpositionPageBitmap.PixelHeight,
                    Windows.UI.Colors.White);
                ApplyScaleToFit(scaledImpositionPageBitmap, impositionPageBitmap, false); // No border

                // Put imposition page image to target page image
                Rect destRect = new Rect(x, y, scaledImpositionPageBitmap.PixelWidth,
                    scaledImpositionPageBitmap.PixelHeight);
                Rect srcRect = new Rect(0, 0, scaledImpositionPageBitmap.PixelWidth,
                    scaledImpositionPageBitmap.PixelHeight);
                WriteableBitmapExtensions.Blit(canvasBitmap, destRect, scaledImpositionPageBitmap,
                    srcRect);

                // Update offset/postion based on direction
                if (impositionOrder == (int)ImpositionOrder.TwoUpLeftToRight ||
                    impositionOrder == (int)ImpositionOrder.FourUpUpperLeftToRight)
                {
                    // Upper left to right
                    pageImageOffsetX += marginBetweenPages + impositionPageAreaSize.Width;
                    if (((impositionPageIndex + 1) % pagesPerColumn) == 0)
                    {
                        pageImageOffsetX = initialOffsetX;
                        pageImageOffsetY += marginBetweenPages + impositionPageAreaSize.Height;
                    }
                }
                else if (impositionOrder == (int)ImpositionOrder.TwoUpRightToLeft &&
                    isImpositionPortrait)
                {
                    // Lower left to right
                    pageImageOffsetX -= marginBetweenPages + impositionPageAreaSize.Width;
                    if (((impositionPageIndex + 1) % pagesPerColumn) == 0)
                    {
                        pageImageOffsetX = initialOffsetX;
                        pageImageOffsetY -= marginBetweenPages + impositionPageAreaSize.Height;
                    }
                }
                else if (impositionOrder == (int)ImpositionOrder.FourUpUpperLeftToBottom)
                {
                    // Upper left to bottom
                    pageImageOffsetY += marginBetweenPages + impositionPageAreaSize.Height;
                    if (((impositionPageIndex + 1) % pagesPerRow) == 0)
                    {
                        pageImageOffsetY = initialOffsetY;
                        pageImageOffsetX += marginBetweenPages + impositionPageAreaSize.Width;
                    }
                }
                else if (impositionOrder == (int)ImpositionOrder.FourUpUpperRightToBottom)
                {
                    // Upper right to bottom
                    pageImageOffsetY += marginBetweenPages + impositionPageAreaSize.Height;
                    if (((impositionPageIndex + 1) % pagesPerRow) == 0)
                    {
                        pageImageOffsetY = initialOffsetY;
                        pageImageOffsetX -= marginBetweenPages + impositionPageAreaSize.Width;
                    }
                }
                else if ((impositionOrder == (int)ImpositionOrder.TwoUpRightToLeft && !isImpositionPortrait) ||
                    impositionOrder == (int)ImpositionOrder.FourUpUpperRightToLeft)
                {
                    // Upper right to left
                    pageImageOffsetX -= marginBetweenPages + impositionPageAreaSize.Width;
                    if (((impositionPageIndex + 1) % pagesPerColumn) == 0)
                    {
                        pageImageOffsetX = initialOffsetX;
                        pageImageOffsetY += marginBetweenPages + impositionPageAreaSize.Height;
                    }
                }

                ++impositionPageIndex;
            }

            return canvasBitmap;
        }

        /// <summary>
        /// Computes the page area for imposition
        /// </summary>
        /// <param name="width">PreviewPage image width</param>
        /// <param name="height">PreviewPage image height</param>
        /// <param name="numRows">number of rows based on imposition</param>
        /// <param name="numColumns">number of columns based on imposition</param>
        /// <param name="marginBetween">margin between pages (in pixels)</param>
        /// <param name="marginOuter">margin of the PreviewPage image (in pixels)</param>
        /// <returns>size of a page for imposition</returns>
        private Size GetImpositionSinglePageAreaSize(int width, int height, int numRows, int numColumns,
            double marginBetween, double marginOuter)
        {
            Size pageAreaSize = new Size();
            if (width > 0 && height > 0 && numRows > 0 && numColumns > 0)
            {
                pageAreaSize.Width = (width - (marginBetween * (numColumns - 1)) - (marginOuter * 2))
                    / numColumns;
                pageAreaSize.Height = (height - (marginBetween * (numRows - 1)) - (marginOuter * 2))
                    / numRows;
            }
            return pageAreaSize;
        }

        /// <summary>
        /// Changes the bitmap to grayscale
        /// </summary>
        /// <param name="canvasBitmap">bitmap to change</param>
        private void ApplyMonochrome(WriteableBitmap canvasBitmap)
        {
            byte[] pixelBytes = WriteableBitmapExtensions.ToByteArray(canvasBitmap);

            // From http://social.msdn.microsoft.com/Forums/windowsapps/en-US/5ff10c14-51d4-4760-afe6-091624adc532/sample-code-for-making-a-bitmapimage-grayscale
            for (int i = 0; i < pixelBytes.Length; i += 4)
            {
                double b = (double)pixelBytes[i] / 255.0;
                double g = (double)pixelBytes[i + 1] / 255.0;
                double r = (double)pixelBytes[i + 2] / 255.0;
                byte a = pixelBytes[i + 3];

                // Altered color factor to be equal
                double bwPixel = (0.3 * r + 0.59 * g + 0.11 * b) * 255;
                byte bwPixelByte = Convert.ToByte(bwPixel);

                pixelBytes[i] = bwPixelByte;
                pixelBytes[i + 1] = bwPixelByte;
                pixelBytes[i + 2] = bwPixelByte;
                pixelBytes[i + 3] = a;
            }

            // Copy pixels to bitmap
            WriteableBitmapExtensions.FromByteArray(canvasBitmap, pixelBytes);
        }

        /// <summary>
        /// Applies duplex into image with staple and punch as needed
        /// </summary>
        /// <param name="canvasBitmap">destination image</param>
        /// <param name="duplexType">duplex setting</param>
        /// <param name="finishingSide">finishing side</param>
        /// <param name="holeCount">hole punch count; 0 if punch is off</param>
        /// <param name="staple">staple type</param>
        /// <param name="isPortrait">true when portrait, false, otherwise</param>
        /// <param name="isBackSide">true if for backside (duplex), false otherwise</param>
        /// <returns>task</returns>
        private async Task ApplyDuplex(WriteableBitmap canvasBitmap, int duplexType,
            int finishingSide, int holeCount, int staple, bool isPortrait, bool isBackSide)
        {
            bool needsRotate = (duplexType == (int)Duplex.LongEdge && !isPortrait) ||
                                    (duplexType == (int)Duplex.ShortEdge && isPortrait);

            // Determine actual finishing side based on duplex
            if (isBackSide && !needsRotate)
            {
                // Change the side of the staple if letf or right
                if (finishingSide == (int)FinishingSide.Left)
                {
                    finishingSide = (int)FinishingSide.Right;
                }
                else if (finishingSide == (int)FinishingSide.Right)
                {
                    finishingSide = (int)FinishingSide.Left;
                }
            }

            // Apply punch
            if (holeCount > 0)
            {
                await ApplyPunch(canvasBitmap, holeCount, finishingSide);
            }

            // Apply staple
            if (staple != (int)Staple.Off)
            {
                await ApplyStaple(canvasBitmap, staple, finishingSide);
            }

            // Apply duplex as the back page image
            if (isBackSide && needsRotate)
            {
                canvasBitmap = WriteableBitmapExtensions.Rotate(canvasBitmap, 180);
            }
        }

        /// <summary>
        /// Applies booklet settings into a single page image
        /// </summary>
        /// <param name="canvasBitmap">destination image</param>
        /// <param name="bookletFinishing">booklet finishing</param>
        /// <param name="isPortrait">true when portrait, false otherwise</param>
        /// <param name="isBackSide">true if for backside (booklet), false otherwise</param>
        /// <param name="isRightSide">true when page is on right side, false otherwise</param>
        /// <returns>task</returns>
        private async Task ApplyBooklet(WriteableBitmap canvasBitmap, int bookletFinishing,
            bool isPortrait, bool isBackSide, bool isRightSide)
        {
            // Determine finishing side
            int bindingSide = -1; // Out of range number to denote bottom
            if (isPortrait && !isBackSide)
            {
                bindingSide = (int)FinishingSide.Left;
            }
            else if (isPortrait && isBackSide)
            {
                bindingSide = (int)FinishingSide.Right;
            }
            else if (!isPortrait && !isBackSide)
            {
                bindingSide = (int)FinishingSide.Top;
            }

            // Determine booklet type
            bool applyStaple = (bookletFinishing == (int)BookletFinishing.FoldAndStaple);

            // Apply staple at the edge based on finishing side
            if (applyStaple)
            {
                await ApplyStaple(canvasBitmap, 0, bindingSide, _isBooklet,
                    isRightSide);
            }
        }

        /// <summary>
        /// Adds staple wire image into target page image.
        /// This function ignores the booklet setting.
        /// </summary>
        /// <param name="canvasBitmap">destination image</param>
        /// <param name="stapleType">type indicating number of staple</param>
        /// <param name="finishingSide">position of staple</param>
        /// <returns>task</returns>
        private async Task ApplyStaple(WriteableBitmap canvasBitmap, int stapleType,
            int finishingSide)
        {
            await ApplyStaple(canvasBitmap, stapleType, finishingSide, false, false);
        }

        /// <summary>
        /// Adds staple wire image into target page image specifying the booklet setting.
        /// </summary>
        /// <param name="canvasBitmap">destination image</param>
        /// <param name="stapleType">type indicating number of staple (not used when booklet is on)</param>
        /// <param name="finishingSide">position of staple</param>
        /// <param name="isBooklet">true when booklet is on, false otherwise</param>
        /// <param name="isRightSide">true when page is on right side, false otherwise</param>
        /// <returns>true</returns>
        private async Task ApplyStaple(WriteableBitmap canvasBitmap, int stapleType,
            int finishingSide, bool isBooklet, bool isRightSide)
        {
            // Get staple image
            WriteableBitmap stapleBitmap = new WriteableBitmap(1, 1); // Size doesn't matter here yet
            StorageFile stapleFile = await StorageFileUtility.GetFileFromAppResource(FILE_PATH_RES_IMAGE_STAPLE);
            using (IRandomAccessStream raStream = await stapleFile.OpenReadAsync())
            {
                // Put staple image to a bitmap
                stapleBitmap = await WriteableBitmapExtensions.FromStream(null, raStream);
            }
            double targetScaleFactor =
                (double)(PrintSettingConstant.STAPLE_CROWN_LENGTH * ImageConstant.BASE_DPI)
                / stapleBitmap.PixelWidth;
            // Scale the staple image
            WriteableBitmap scaledStapleBitmap = WriteableBitmapExtensions.Resize(stapleBitmap,
                (int)(stapleBitmap.PixelWidth * targetScaleFactor),
                (int)(stapleBitmap.PixelHeight * targetScaleFactor),
                WriteableBitmapExtensions.Interpolation.Bilinear);

            if (isBooklet)
            {
                // Crop staple; only half of the staple is visible to each page
                Rect region;
                double halfStapleWidth = (double)scaledStapleBitmap.PixelWidth / 2;
                double halfStapleHeight = (double)scaledStapleBitmap.PixelHeight / 2;
                if (isRightSide)
                {
                    region = new Rect(halfStapleWidth, halfStapleHeight, halfStapleWidth, halfStapleHeight);
                }
                else
                {
                    region = new Rect(0, 0, halfStapleWidth, halfStapleHeight);
                }
                WriteableBitmap halfStapleBitmap =
                    WriteableBitmapExtensions.Crop(scaledStapleBitmap, region);

                // Determine finishing side
                if (finishingSide == (int)FinishingSide.Top)
                {
                    ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 0, false, false,
                        canvasBitmap.PixelWidth, true, 0.25, false);
                    ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 0, true, false,
                        canvasBitmap.PixelWidth, true, 0.75, false);
                }
                else if (finishingSide == (int)FinishingSide.Left)
                {
                    ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 90, false, false,
                        canvasBitmap.PixelHeight, false, 0.25, false);
                    ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 90, false, true,
                        canvasBitmap.PixelHeight, false, 0.75, false);
                }
                else if (finishingSide == (int)FinishingSide.Right)
                {
                    ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 270, true, false,
                            canvasBitmap.PixelHeight, false, 0.25, false);
                    ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 270, true, true,
                        canvasBitmap.PixelHeight, false, 0.75, false);
                }
                else
                {
                    ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 0, false, true,
                        canvasBitmap.PixelWidth, true, 0.25, false);
                    ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 0, true, true,
                        canvasBitmap.PixelWidth, true, 0.75, false);
                }
            }
            else
            {
                // Determine finishing side
                if (finishingSide == (int)FinishingSide.Top)
                {
                    if (stapleType == (int)Staple.OneUpperLeft)
                    {
                        ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 135, false, false);
                    }
                    else if (stapleType == (int)Staple.OneUpperRight)
                    {
                        ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 45, true, false);
                    }
                    else if (stapleType == (int)Staple.Two)
                    {
                        ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 0, false, false,
                            canvasBitmap.PixelWidth, true, 0.25);
                        ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 0, true, false,
                            canvasBitmap.PixelWidth, true, 0.75);
                    }
                }
                else if (finishingSide == (int)FinishingSide.Left)
                {
                    if (stapleType == (int)Staple.One)
                    {
                        ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 135, false, false);
                    }
                    else if (stapleType == (int)Staple.Two)
                    {
                        ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 90, false, false,
                            canvasBitmap.PixelHeight, false, 0.25);
                        ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 90, false, true,
                            canvasBitmap.PixelHeight, false, 0.75);
                    }
                }
                else if (finishingSide == (int)FinishingSide.Right)
                {
                    if (stapleType == (int)Staple.One)
                    {
                        ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 45, true, false);
                    }
                    else if (stapleType == (int)Staple.Two)
                    {
                        ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 270, true, false,
                            canvasBitmap.PixelHeight, false, 0.25);
                        ApplyRotateStaple(canvasBitmap, scaledStapleBitmap, 270, true, true,
                            canvasBitmap.PixelHeight, false, 0.75);
                    }
                }
            }

        }

        /// <summary>
        /// Adds a staple image. Requires that the staple image is already scaled.
        /// </summary>
        /// <param name="canvasBitmap">destination image</param>
        /// <param name="stapleBitmap">staple image; required to be scaled beforehand</param>
        /// <param name="angle">angle for rotation</param>
        /// <param name="isXEnd">true when staple is to be placed near the end along X-axis</param>
        /// <param name="isYEnd">true when staple is to be placed near the end along Y-axis</param>
        private void ApplyRotateStaple(WriteableBitmap canvasBitmap, WriteableBitmap stapleBitmap,
            int angle, bool isXEnd, bool isYEnd)
        {
            ApplyRotateStaple(canvasBitmap, stapleBitmap, angle, isXEnd, isYEnd, 0, false, 0, true);
        }

        /// <summary>
        /// Adds a staple image. Requires that the staple image is already scaled.
        /// </summary>
        /// <param name="canvasBitmap">destination image</param>
        /// <param name="stapleBitmap">staple image; required to be scaled beforehand</param>
        /// <param name="angle">angle for rotation</param>
        /// <param name="isXEnd">true when staple is to be placed near the end along X-axis</param>
        /// <param name="isYEnd">true when staple is to be placed near the end along Y-axis</param>
        /// <param name="edgeLength">length of page image edge where staples will be placed; used with dual staple</param>
        /// <param name="isAlongXAxis">location of punch holes; used with dual staple</param>
        /// <param name="positionPercentage">relative location from edge length; used with dual staple</param>
        private void ApplyRotateStaple(WriteableBitmap canvasBitmap, WriteableBitmap stapleBitmap,
            int angle, bool isXEnd, bool isYEnd, int edgeLength, bool isAlongXAxis,
            double positionPercentage)
        {
            // Right side only when booklet is ON
            ApplyRotateStaple(canvasBitmap, stapleBitmap, angle, isXEnd, isYEnd, edgeLength, isAlongXAxis,
                positionPercentage, true);
        }

        /// <summary>
        /// Adds a staple image. Requires that the staple image is already scaled.
        /// </summary>
        /// <param name="canvasBitmap">destination image</param>
        /// <param name="stapleBitmap">staple image; required to be scaled beforehand</param>
        /// <param name="angle">angle for rotation</param>
        /// <param name="isXEnd">true when staple is to be placed near the end along X-axis</param>
        /// <param name="isYEnd">true when staple is to be placed near the end along Y-axis</param>
        /// <param name="edgeLength">length of page image edge where staples will be placed; used with dual staple</param>
        /// <param name="isAlongXAxis">location of punch holes; used with dual staple</param>
        /// <param name="positionPercentage">relative location from edge length; used with dual staple</param>
        /// <param name="hasStapleMargin">true when staple is put slightly off the edge (with margin), false otherwise</param>
        private void ApplyRotateStaple(WriteableBitmap canvasBitmap, WriteableBitmap stapleBitmap,
            int angle, bool isXEnd, bool isYEnd, int edgeLength, bool isAlongXAxis,
            double positionPercentage, bool hasStapleMargin)
        {
            // Rotate
            WriteableBitmap rotatedStapleBitmap = stapleBitmap;
            if (angle > 0)
            {
                rotatedStapleBitmap = WriteableBitmapExtensions.RotateFree(stapleBitmap, angle, false);
            }

            // Put into position
            double marginStaple = (hasStapleMargin) ?
                PrintSettingConstant.MARGIN_STAPLE * ImageConstant.BASE_DPI : 0;
            double destXOrigin = marginStaple;
            if (positionPercentage > 0 && isAlongXAxis)
            {
                destXOrigin = (edgeLength * positionPercentage) - (rotatedStapleBitmap.PixelWidth / 2);
            }
            else if (isXEnd)
            {
                destXOrigin = canvasBitmap.PixelWidth - rotatedStapleBitmap.PixelWidth - marginStaple;
            }
            double destYOrigin = marginStaple;
            if (positionPercentage > 0 && !isAlongXAxis)
            {
                destYOrigin = (edgeLength * positionPercentage) - (rotatedStapleBitmap.PixelHeight / 2);
            }
            else if (isYEnd)
            {
                destYOrigin = canvasBitmap.PixelHeight - rotatedStapleBitmap.PixelHeight - marginStaple;
            }

            Rect destRect = new Rect(destXOrigin, destYOrigin, rotatedStapleBitmap.PixelWidth,
                rotatedStapleBitmap.PixelHeight);
            Rect srcRect = new Rect(0, 0, rotatedStapleBitmap.PixelWidth, rotatedStapleBitmap.PixelHeight);
            WriteableBitmapExtensions.Blit(canvasBitmap, destRect, rotatedStapleBitmap, srcRect);
        }

        /// <summary>
        /// Adds punch hole image into page image
        /// </summary>
        /// <param name="canvasBitmap">destination image</param>
        /// <param name="holeCount">number of punch holes</param>
        /// <param name="finishingSide">postion/edge of punch</param>
        /// <returns>task</returns>
        private async Task ApplyPunch(WriteableBitmap canvasBitmap, int holeCount, int finishingSide)
        {
            // Get punch image
            WriteableBitmap punchBitmap = new WriteableBitmap(1, 1); // Size doesn't matter here yet
            StorageFile stapleFile = await StorageFileUtility.GetFileFromAppResource(FILE_PATH_RES_IMAGE_PUNCH);
            using (IRandomAccessStream raStream = await stapleFile.OpenReadAsync())
            {
                // Put staple image to a bitmap
                punchBitmap = await WriteableBitmapExtensions.FromStream(null, raStream);
            }
            double targetScaleFactor =
                (double)(PrintSettingConstant.PUNCH_HOLE_DIAMETER * ImageConstant.BASE_DPI)
                / punchBitmap.PixelWidth;
            // Scale the staple image
            WriteableBitmap scaledPunchBitmap = WriteableBitmapExtensions.Resize(punchBitmap,
                (int)(punchBitmap.PixelWidth * targetScaleFactor),
                (int)(punchBitmap.PixelHeight * targetScaleFactor),
                WriteableBitmapExtensions.Interpolation.Bilinear);

            // Determine punch
            double diameterPunch = PrintSettingConstant.PUNCH_HOLE_DIAMETER * ImageConstant.BASE_DPI;
            double marginPunch = PrintSettingConstant.MARGIN_PUNCH * ImageConstant.BASE_DPI;
            double distanceBetweenHoles = GetDistanceBetweenHoles(_currPrintSettings.Punch);
            if (finishingSide == (int)FinishingSide.Top)
            {
                double startPos = GetPunchStartPosition(canvasBitmap.PixelWidth, true, holeCount,
                    diameterPunch, marginPunch, distanceBetweenHoles);
                ApplyPunch(canvasBitmap, scaledPunchBitmap, holeCount, startPos, false, true,
                    diameterPunch, marginPunch, distanceBetweenHoles);
            }
            else if (finishingSide == (int)FinishingSide.Left)
            {
                double startPos = GetPunchStartPosition(canvasBitmap.PixelHeight, false, holeCount,
                    diameterPunch, marginPunch, distanceBetweenHoles);
                ApplyPunch(canvasBitmap, scaledPunchBitmap, holeCount, startPos, false, false,
                    diameterPunch, marginPunch, distanceBetweenHoles);
            }
            else if (finishingSide == (int)FinishingSide.Right)
            {
                double startPos = GetPunchStartPosition(canvasBitmap.PixelHeight, false, holeCount,
                    diameterPunch, marginPunch, distanceBetweenHoles);
                ApplyPunch(canvasBitmap, scaledPunchBitmap, holeCount, startPos, true, false,
                    diameterPunch, marginPunch, distanceBetweenHoles);
            }
        }

        /// <summary>
        /// Computes the starting position of the punch hole image
        /// </summary>
        /// <param name="edgeLength">length of page image edge where punch will be placed</param>
        /// <param name="isAlongXAxis">direction of punch holes</param>
        /// <param name="holeCount">number of punch holes</param>
        /// <param name="diameterPunch">size of punch hole</param>
        /// <param name="marginPunch">margin of punch hole against edge of page image</param>
        /// <param name="distanceBetweenHoles">distance between punch holes</param>
        /// <returns>starting position of the first punch hole</returns>
        private double GetPunchStartPosition(double edgeLength, bool isAlongXAxis, int holeCount,
            double diameterPunch, double marginPunch, double distanceBetweenHoles)
        {
            double startPos = (edgeLength - (holeCount * diameterPunch) -
                                ((holeCount - 1) * distanceBetweenHoles)) / 2;
            return startPos;
        }

        /// <summary>
        /// Adds punch hole images
        /// </summary>
        /// <param name="canvasBitmap">destination image</param>
        /// <param name="punchBitmap">punch hole image</param>
        /// <param name="holeCount">number of punch holes</param>
        /// <param name="startPos">starting position</param>
        /// <param name="isXEnd">true when punch holes are to be placed near the end along X-axis</param>
        /// <param name="isAlongXAxis">true when punch holes are to be placed horizontally</param>
        /// <param name="diameterPunch">size of punch hole</param>
        /// <param name="marginPunch">margin of punch hole against edge of page image</param>
        /// <param name="distanceBetweenHoles">distance between punch holes</param>
        private void ApplyPunch(WriteableBitmap canvasBitmap, WriteableBitmap punchBitmap,
            int holeCount, double startPos, bool isXEnd, bool isAlongXAxis, double diameterPunch,
            double marginPunch, double distanceBetweenHoles)
        {
            double endMarginPunch = (isXEnd) ? canvasBitmap.PixelWidth - diameterPunch - marginPunch : marginPunch;

            double currPos = startPos;
            for (int index = 0; index < holeCount; ++index, currPos += diameterPunch + distanceBetweenHoles)
            {
                // Do not put punch hole image when it is outside the page image size
                if (currPos < 0 || (isAlongXAxis && currPos > canvasBitmap.PixelWidth) ||
                    (!isAlongXAxis && currPos > canvasBitmap.PixelHeight))
                {
                    continue;
                }

                double destXOrigin = (isAlongXAxis) ? currPos : endMarginPunch;
                double destYOrigin = (isAlongXAxis) ? marginPunch : currPos;
                Rect destRect = new Rect(destXOrigin, destYOrigin, punchBitmap.PixelWidth,
                    punchBitmap.PixelHeight);
                Rect srcRect = new Rect(0, 0, punchBitmap.PixelWidth, punchBitmap.PixelHeight);
                WriteableBitmapExtensions.Blit(canvasBitmap, destRect, punchBitmap, srcRect);
            }
        }

        /// <summary>
        /// Gets the target size based on paper size
        /// </summary>
        /// <param name="paperSize">paper size</param>
        /// <returns>size</returns>
        private Size GetPaperSize(int paperSize)
        {
            Size targetSize;
            switch (paperSize)
            {
                case (int)PaperSize.A3:
                    targetSize = PrintSettingConstant.PAPER_SIZE_A3;
                    break;
                case (int)PaperSize.A3W:
                    targetSize = PrintSettingConstant.PAPER_SIZE_A3W;
                    break;
                case (int)PaperSize.A5:
                    targetSize = PrintSettingConstant.PAPER_SIZE_A5;
                    break;
                case (int)PaperSize.A6:
                    targetSize = PrintSettingConstant.PAPER_SIZE_A6;
                    break;
                case (int)PaperSize.B4:
                    targetSize = PrintSettingConstant.PAPER_SIZE_B4;
                    break;
                case (int)PaperSize.B5:
                    targetSize = PrintSettingConstant.PAPER_SIZE_B5;
                    break;
                case (int)PaperSize.B6:
                    targetSize = PrintSettingConstant.PAPER_SIZE_B6;
                    break;
                case (int)PaperSize.Foolscap:
                    targetSize = PrintSettingConstant.PAPER_SIZE_FOOLSCAP;
                    break;
                case (int)PaperSize.Tabloid:
                    targetSize = PrintSettingConstant.PAPER_SIZE_TABLOID;
                    break;
                case (int)PaperSize.Legal:
                    targetSize = PrintSettingConstant.PAPER_SIZE_LEGAL;
                    break;
                case (int)PaperSize.Letter:
                    targetSize = PrintSettingConstant.PAPER_SIZE_LETTER;
                    break;
                case (int)PaperSize.Statement:
                    targetSize = PrintSettingConstant.PAPER_SIZE_STATEMENT;
                    break;
                case (int)PaperSize.A4:
                default:
                    targetSize = PrintSettingConstant.PAPER_SIZE_A4;
                    break;
            }

            return targetSize;
        }

        /// <summary>
        /// Gets the number of punch holes based on punch type
        /// </summary>
        /// <param name="punch">punch type</param>
        /// <returns>number of punch holes</returns>
        private int GetPunchHoleCount(int punch)
        {
            int numberOfHoles = 0;
            switch (punch)
            {
                case (int)Punch.TwoHoles:
                    numberOfHoles = 2;
                    break;
                case (int)Punch.FourHoles:
                    //numberOfHoles = (GlobalizationUtility.IsJapaneseLocale()) ? 3 : 4;
                    if (_selectedPrinter.EnabledPunchThree)
                    {
                        numberOfHoles = 3;
                    }
                    else if (_selectedPrinter.EnabledPunchFour)
                    {
                        numberOfHoles = 4;
                    }
                    break;
                case (int)Punch.Off:
                default:
                    // Do nothing
                    break;
            }

            return numberOfHoles;
        }

        /// <summary>
        /// Computes the distance between punch holes based on number of punches
        /// </summary>
        /// <param name="punch">punch type</param>
        /// <returns>distance</returns>
        private double GetDistanceBetweenHoles(int punch)
        {
            double distance = 0;
            switch (punch)
            {
                case (int)Punch.TwoHoles:
                    distance = PrintSettingConstant.PUNCH_BETWEEN_TWO_HOLES_DISTANCE;
                    break;
                case (int)Punch.FourHoles:
                    //distance = (GlobalizationUtility.IsJapaneseLocale()) ?
                    distance = (_selectedPrinter.EnabledPunchFour) ?
                        PrintSettingConstant.PUNCH_BETWEEN_FOUR_HOLES_DISTANCE :
                        PrintSettingConstant.PUNCH_BETWEEN_THREE_HOLES_DISTANCE;
                    break;
                case (int)Punch.Off:
                default:
                    // Do nothing
                    break;
            }

            return distance * ImageConstant.BASE_DPI;
        }

        #endregion Apply Print Settings

        #region Print

        /// <summary>
        /// Event handler for Print button
        /// </summary>
        public async void Print()
        {
            if (_selectedPrinter.Id > -1)
            {
                //// TODO: Check network
                //NetworkController.Instance.networkControllerPingStatusCallback =
                //    new Action<string, bool>(GetPrinterStatus);
                //await NetworkController.Instance.pingDevice(_selectedPrinter.IpAddress);

                // TODO: Remove this when ping is working
                GetPrinterStatus(null, true);
            }
        }

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
        /// Checks the printer status before sending print job
        /// </summary>
        /// <param name="ipAddress">printer IP address</param>
        /// <param name="isOnline">true when online, false otherwise</param>
        public void GetPrinterStatus(string ipAddress, bool isOnline)
        {
            if (isOnline)
            {
                // Get latest print settings since non-preview related print settings may be updated
                _currPrintSettings = PrintSettingsController.Instance.GetCurrentPrintSettings(_screenName);


                // Display progress dialog
                _printingProgress = new MessageProgressBarControl("IDS_LBL_PRINTING");
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

                //// TODO: Remove the following line. This is for testing only.
                //UpdatePrintJobResult(DocumentController.Instance.FileName, DateTime.Now, 0);
            }
            else
            {
                DialogService.Instance.ShowError("IDS_ERR_MSG_NETWORK_ERROR", "IDS_APP_NAME", "IDS_LBL_OK", null);
            }
        }

        /// <summary>
        /// Event handler for Cancel button
        /// </summary>
        public void CancelPrint()
        {
            if (_directPrintController != null)
            {
                _directPrintController.CancelPrintJob();
                _directPrintController.UnsubscribeEvents();
                _directPrintController = null;
            }
            Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal,
            () =>
            {
                _printingPopup.IsOpen = false;
            });
        }

        /// <summary>
        /// Update progress value
        /// </summary>
        /// <param name="progress">progress value</param>
        public void UpdatePrintJobProgress(float progress)
        {
            //_printingProgress.ProgressValue = progress;
            
            Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal,
            () =>
            {
                // Your UI update code goes here!
                _printingProgress.ProgressValue = progress;
            });
        }

        /// <summary>
        /// Processes print job result and saves the print job item to database.
        /// </summary>
        /// <param name="name">print job name</param>
        /// <param name="date">date</param>
        /// <param name="result">result</param>
        public void UpdatePrintJobResult(string name, DateTime date, int result)
        {
            Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal,
            () =>
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
                    DialogService.Instance.ShowMessage("IDS_LBL_PRINT_JOB_SUCCESSFUL", "IDS_APP_NAME");
                    new ViewModelLocator().ViewControlViewModel.GoToJobsPage.Execute(null);
                }
                else if (result == (int)PrintJobResult.Error)
                {
                    DialogService.Instance.ShowError("IDS_LBL_PRINT_JOB_FAILED", "IDS_APP_NAME", "IDS_LBL_OK", null);
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

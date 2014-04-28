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

using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Converters;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Xml.Linq;
using Windows.ApplicationModel;
using Windows.Foundation;
using Windows.Graphics.Imaging;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.UI.Xaml.Media.Imaging;

namespace SmartDeviceApp.Controllers
{
    public sealed class PrintPreviewController
    {
        static readonly PrintPreviewController _instance = new PrintPreviewController();

        public delegate Task GoToPageEventHandler(int pageIndex);
        public delegate void PrintSettingValueChangedEventHandler(PrintSetting printSetting,
            object value);
        public delegate void PrintEventHandler();
        private GoToPageEventHandler _goToPageEventHandler;
        private PrintSettingValueChangedEventHandler _printSettingValueChangedEventHandler;
        private PrintEventHandler _printEventHandler;

        public delegate void PageAreaGridLoadedEventHandler();
        public static PageAreaGridLoadedEventHandler PageAreaGridLoaded;

        private const string FORMAT_PREFIX_PREVIEW_PAGE_IMAGE = "previewpage";
        private const string FORMAT_FILE_NAME_PREVIEW_PAGE_IMAGE =
            FORMAT_PREFIX_PREVIEW_PAGE_IMAGE + "{0:0000}-{1:yyyyMMddHHmmssffff}.jpg";
        private const string FILE_PATH_ASSET_PRINT_SETTINGS_XML = "Assets/printsettings.xml";
        private const string FILE_PATH_RES_IMAGE_STAPLE = "Resources/Images/img_staple.png";
        private const string FILE_PATH_RES_IMAGE_PUNCH = "Resources/Images/img_punch.png";

        private PrintPreviewViewModel _printPreviewViewModel;
        private PrintSettingsViewModel _printSettingsViewModel;
        private Printer _selectedPrinter;
        private int _pagesPerSheet = 1;
        private bool _isDuplex = false;
        private bool _isBooklet = false;
        private bool _isReversePages = false;
        private Dictionary<int, PreviewPage> _previewPages; // Generated PreviewPages from the start
        private uint _previewPageTotal;
        private PageViewMode _pageViewMode;
        private static int _currPreviewPageIndex;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static PrintPreviewController() { }

        private PrintPreviewController()
        {
            _printPreviewViewModel = new ViewModelLocator().PrintPreviewViewModel;
            _printSettingsViewModel = new ViewModelLocator().PrintSettingsViewModel;
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
            _goToPageEventHandler = new GoToPageEventHandler(GoToPage);
            _printSettingValueChangedEventHandler = new PrintSettingValueChangedEventHandler(PrintSettingValueChanged);
            _printEventHandler = new PrintEventHandler(Print);

            PageAreaGridLoaded += InitializeGestures;
            _previewPages = new Dictionary<int, PreviewPage>();

            // Get print settings if document is successfully loaded
            if (DocumentController.Instance.Result == LoadDocumentResult.Successful)
            {
                _previewPageTotal = DocumentController.Instance.PageCount;

                // Get print settings
                LoadPrintSettingsOptions();
                await GetDefaultPrinterAndPrintSetting();

                _printSettingsViewModel.PrinterName = _selectedPrinter.Name;

                UpdatePreviewInfo();
                _printPreviewViewModel.SetInitialPageIndex(0);
                _printPreviewViewModel.DocumentTitleText = DocumentController.Instance.FileName;

                PrintSettingUtility.PrintSettingValueChangedEventHandler += _printSettingValueChangedEventHandler;

                // TODO: Register to print button event only when there is a selected printer
                _printSettingsViewModel.ExecutePrintEventHandler += _printEventHandler;

                await GoToPage(0);
            }
            else
            {
                // TODO: Notify ViewModel regarding error
            }
        }

        /// <summary>
        /// Clean-up
        /// </summary>
        /// <returns>task</returns>
        public async Task Cleanup()
        {
            if (_printPreviewViewModel != null)
            {
                _printPreviewViewModel.GoToPageEventHandler -= _goToPageEventHandler;
            }
            PrintSettingUtility.PrintSettingValueChangedEventHandler -= _printSettingValueChangedEventHandler;
            _printSettingsViewModel.ExecutePrintEventHandler -= _printEventHandler;
            _selectedPrinter = null;
            await ClearPreviewPageListAndImages();
            _previewPages = null;

            _pagesPerSheet = 1;
            _isDuplex = false;
            _isBooklet = false;
            _isReversePages = false;
        }

        /// <summary>
        /// Resets the generated PreviewPage(s) list and removed the page images from AppData
        /// temporary store.
        /// </summary>
        /// <returns>task</returns>
        private async Task ClearPreviewPageListAndImages()
        {
            StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;
            await StorageFileUtility.DeleteFiles(FORMAT_PREFIX_PREVIEW_PAGE_IMAGE, tempFolder);

            if (_previewPages != null)
            {
                _previewPages.Clear();
            }
        }

        #region Database and Default Values Operations

        /// <summary>
        /// Retrieves the default printer and its print settings.
        /// If no default printer is found, a dummy printer (with ID = -1) is set as
        /// selected printer and default print settings are assigned.
        /// </summary>
        /// <returns>task</returns>
        private async Task GetDefaultPrinterAndPrintSetting()
        {
            // Get default printer and print settings from database
            DefaultPrinter defaultPrinter = await DatabaseController.Instance.GetDefaultPrinter();

            if (defaultPrinter != null)
            {
                await GetPrinterAndPrintSetting((int)defaultPrinter.PrinterId);
            }
            else
            {
                // Full default capabilities
                await GetPrinterAndPrintSetting(-1);
            }
        }

        /// <summary>
        /// Retrieves the selected printer and its print settings.
        /// Assigns full default print settings when no printer is selected or failed
        /// in retrieving printer or print settings from the database.
        /// </summary>
        /// <param name="printerId">printer ID; specify -1 as no selected printer</param>
        /// <returns>task</returns>
        private async Task GetPrinterAndPrintSetting(int printerId)
        {
            if (printerId > -1)
            {
                // Get printer from database
                _selectedPrinter = await DatabaseController.Instance.GetPrinter(printerId);
                if (_selectedPrinter != null)
                {
                    // Get print settings from database
                    _selectedPrinter.PrintSettings =
                        await DatabaseController.Instance.GetPrintSettings(_selectedPrinter.PrintSettingId);
                    if (_selectedPrinter.PrintSettings == null)
                    {
                        _selectedPrinter.PrintSettings =
                            DefaultsUtility.GetDefaultPrintSettings(_printSettingsViewModel.PrintSettingsList);
                    }

                    FilterPrintSettingsUsingCapabilities();
                    MergePrintSettings();
                    ApplyPrintSettingConstraints();

                    return;
                }
            }

            // Create dummy Printer as current printer
            _selectedPrinter = new Printer();
            _selectedPrinter.PrintSettings = DefaultsUtility.GetDefaultPrintSettings(_printSettingsViewModel.PrintSettingsList);

            // No need to filter using printer capabilities
            MergePrintSettings();
            ApplyPrintSettingConstraints();
        }

        #endregion Database and Default Values Operations

        #region Print Preview

        /// <summary>
        /// Initializes the gesture of the preview area.
        /// Requires initial paper size before using this function.
        /// </summary>
        private void InitializeGestures()
        {
            Size paperSize = GetPaperSize(_selectedPrinter.PrintSettings.PaperSize);
            bool isPortrait = IsPortrait(_selectedPrinter.PrintSettings.Orientation,
                _selectedPrinter.PrintSettings.BookletLayout);

            _printPreviewViewModel.RightPageActualSize = GetPreviewPageImageSize(paperSize, isPortrait);
            _printPreviewViewModel.InitializeGestures();
        }

        /// <summary>
        /// Receives modified print setting and its new value
        /// </summary>
        /// <param name="printSetting">affected print setting</param>
        /// <param name="value">updated value</param>
        public async void PrintSettingValueChanged(PrintSetting printSetting, object value)
        {
            if (printSetting == null || value == null)
            {
                return;
            }

            switch (printSetting.Type)
            {
                case PrintSettingType.boolean:
                    await UpdatePrintSettings(printSetting, (bool)value);
                    break;
                case PrintSettingType.list:
                case PrintSettingType.numeric:
                    await UpdatePrintSettings(printSetting, (int)value);
                    break;
                case PrintSettingType.unknown:
                default:
                    // Do nothing
                    break;
            }
        }

        /// <summary>
        /// Receiver of the selected print setting option (selected index or numeric value).
        /// Updates the print settings list (PrintSettingList) and cache (PrintSettings),
        /// updates value and enabled options based on constraints, and applies
        /// changes to the PreviewPage image.
        /// </summary>
        /// <param name="printSetting">affected print setting</param>
        /// <param name="selected">updated value</param>
        /// <returns>task</returns>
        private async Task UpdatePrintSettings(PrintSetting printSetting, int value)
        {
            if (printSetting == null)
            {
                return;
            }

            string name = printSetting.Name;

            // Manual check here what is changed
            bool isPreviewPageAffected = false;
            bool isPageCountAffected = false;
            bool isConstraintAffected = false;
            if (name.Equals(PrintSettingConstant.NAME_VALUE_COLOR_MODE))
            {
                int prevColorMode = _selectedPrinter.PrintSettings.ColorMode;
                if (_selectedPrinter.PrintSettings.ColorMode != value)
                {
                    _selectedPrinter.PrintSettings.ColorMode = value;
                    // Matters only if changed to/from Black
                    if (prevColorMode == (int)ColorMode.Mono ||
                        value == (int)ColorMode.Mono)
                    {
                        isPreviewPageAffected = true;
                    }
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_COPIES))
            {
                bool isValid;
                value = CheckIfCopiesValid(value, out isValid);
                if (!isValid)
                {
                    printSetting.Value = value;
                }
                if (_selectedPrinter.PrintSettings.Copies != value)
                {
                    _selectedPrinter.PrintSettings.Copies = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_ORIENTATION))
            {
                if (_selectedPrinter.PrintSettings.Orientation != value)
                {
                    // isConstraintAffected = UpdateConstraintBookletLayoutUsingOrientation(value);
                    _selectedPrinter.PrintSettings.Orientation = value;
                    isPreviewPageAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_DUPLEX))
            {
                if (_selectedPrinter.PrintSettings.Duplex != value)
                {
                    _selectedPrinter.PrintSettings.Duplex = value;
                    isPreviewPageAffected = true;

                    _currPreviewPageIndex = 0; // TODO: Proper handling when total page count changes
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_PAPER_SIZE))
            {
                if (_selectedPrinter.PrintSettings.PaperSize != value)
                {
                    _selectedPrinter.PrintSettings.PaperSize = value;
                    isPreviewPageAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_PAPER_TYPE))
            {
                if (_selectedPrinter.PrintSettings.PaperType != value)
                {
                    _selectedPrinter.PrintSettings.PaperType = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_INPUT_TRAY))
            {
                if (_selectedPrinter.PrintSettings.InputTray != value)
                {
                    _selectedPrinter.PrintSettings.InputTray = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_IMPOSITION))
            {
                if (_selectedPrinter.PrintSettings.Imposition != value)
                {
                    //isConstraintAffected = UpdateConstraintImpositionOrderUsingImposition(value);
                    isConstraintAffected = UpdateConstraintsBasedOnImposition(value);
                    _selectedPrinter.PrintSettings.Imposition = value;
                    isPreviewPageAffected = true;
                    isPageCountAffected = true;

                    _currPreviewPageIndex = 0; // TODO: Proper handling when total page count changes
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER))
            {
                if (_selectedPrinter.PrintSettings.ImpositionOrder != value)
                {
                    _selectedPrinter.PrintSettings.ImpositionOrder = value;
                    if (_pagesPerSheet > 1) // Matters only if pages per sheet is more than 1
                    {
                        isPreviewPageAffected = true;
                    }
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_SORT))
            {
                if (_selectedPrinter.PrintSettings.Sort != value)
                {
                    _selectedPrinter.PrintSettings.Sort = value;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING))
            {
                if (_selectedPrinter.PrintSettings.BookletFinishing != value)
                {
                    _selectedPrinter.PrintSettings.BookletFinishing = value;
                    // Matters only when booklet is ON
                    if (_selectedPrinter.PrintSettings.Booklet == true)
                    {
                        isPreviewPageAffected = true;
                    }
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT))
            {
                if (_selectedPrinter.PrintSettings.BookletLayout != value)
                {
                    _selectedPrinter.PrintSettings.BookletLayout = value;
                    // Matters only when booklet is ON
                    if (_selectedPrinter.PrintSettings.Booklet == true)
                    {
                        isPreviewPageAffected = true;

                        _currPreviewPageIndex = 0; // TODO: Proper handling when total page count changes
                    }
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_FINISHING_SIDE))
            {
                if (_selectedPrinter.PrintSettings.FinishingSide != value)
                {
                    //isConstraintAffected = UpdateConstraintStapleUsingFinishingSide(value);
                    //isConstraintAffected = UpdateConstaintPunchUsingFinishingSide(value) ||
                    //    isConstraintAffected;
                    isConstraintAffected = UpdateConstraintsBasedOnFinishingSide(value);
                    _selectedPrinter.PrintSettings.FinishingSide = value;
                    // Matters only when staple or punch is ON
                    if (_selectedPrinter.PrintSettings.Staple != (int)Staple.Off ||
                        _selectedPrinter.PrintSettings.Punch != (int)Punch.Off)
                    {
                        isPreviewPageAffected = true;
                    }
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_STAPLE))
            {
                if (_selectedPrinter.PrintSettings.Staple != value)
                {
                    isConstraintAffected = UpdateConstraintsBasedOnStaple(value);
                    _selectedPrinter.PrintSettings.Staple = value;
                    isPreviewPageAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_PUNCH))
            {
                if (_selectedPrinter.PrintSettings.Punch != value)
                {
                    //isConstraintAffected = UpdateConstraintFinishingSideUsingPunch(value);
                    isConstraintAffected = UpdateConstraintsBasedOnPunch(value);
                    _selectedPrinter.PrintSettings.Punch = value;
                    isPreviewPageAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY))
            {
                if (_selectedPrinter.PrintSettings.OutputTray != value)
                {
                    _selectedPrinter.PrintSettings.OutputTray = value;
                }
            }

            // Generate PreviewPages again
            if (isPreviewPageAffected || isPageCountAffected || isConstraintAffected)
            {
                await ClearPreviewPageListAndImages();
                UpdatePreviewInfo();
                //InitializeGestures();
                _printPreviewViewModel.UpdatePageIndexes((uint)_currPreviewPageIndex);
                await LoadPage(_currPreviewPageIndex, false);

                // Update preview page
                // TODO: To be removed since this causes the preview area to zoom-in
                //InitializeGestures();
            }
        }

        /// <summary>
        /// Receiver of the selected print setting option (must be a switch).
        /// Updates the print settings list (PrintSettingList) and cache (PrintSettings),
        /// updates value and enabled options based on constraints, and applies
        /// changes to the PreviewPage image.
        /// </summary>
        /// <param name="printSetting">affected print setting</param>
        /// <param name="state">updated value</param>
        /// <returns>task</returns>
        private async Task UpdatePrintSettings(PrintSetting printSetting, bool state)
        {
            if (printSetting == null)
            {
                return;
            }

            string name = printSetting.Name;

            bool isPreviewPageAffected = false;
            bool isConstraintAffected = false;
            if (name.Equals(PrintSettingConstant.NAME_VALUE_SCALE_TO_FIT))
            {
                if (_selectedPrinter.PrintSettings.ScaleToFit != state)
                {
                    _selectedPrinter.PrintSettings.ScaleToFit = state;
                    isPreviewPageAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET))
            {
                if (_selectedPrinter.PrintSettings.Booklet != state)
                {
                    //isConstraintAffected = UpdateConstraintsUsingBooklet(state);
                    isConstraintAffected = UpdateConstraintsBasedOnBooklet(state);
                    _selectedPrinter.PrintSettings.Booklet = state;
                    isPreviewPageAffected = true;

                    _currPreviewPageIndex = 0; // TODO: Proper handling when total page count changes
                }
            }

            if (isPreviewPageAffected || isConstraintAffected)
            {
                await ClearPreviewPageListAndImages();
                UpdatePreviewInfo();
                _printPreviewViewModel.UpdatePageIndexes((uint)_currPreviewPageIndex);
                await LoadPage(_currPreviewPageIndex, false);

                // Update preview page
                // TODO: To be removed since this causes the preview area to zoom-in
                //InitializeGestures();
            }
        }

        /// <summary>
        /// Checks for view related print setting and notifies view model
        /// </summary>
        private void UpdatePreviewInfo()
        {
            // Send UI related items
            if (_selectedPrinter.PrintSettings.Booklet)
            {
                _isBooklet = true;
                //_pageViewMode = PageViewMode.TwoPageView; // TODO: Enable on two-page view
            }
            else
            {
                _isBooklet = false;
                _pageViewMode = PageViewMode.SinglePageView;
            }

            _isDuplex = (_selectedPrinter.PrintSettings.Duplex != (int)Duplex.Off) ||
                (_isBooklet && _selectedPrinter.PrintSettings.BookletFinishing == (int)BookletFinishing.Off);

            _isReversePages = _isBooklet &&
                _selectedPrinter.PrintSettings.BookletLayout == (int)BookletLayout.RightToLeft;

            if (!_isBooklet)
            {
                _pagesPerSheet = GetPagesPerSheet(_selectedPrinter.PrintSettings.Imposition);
            }
            else
            {
                _pagesPerSheet = 1;
            }

            if (_printPreviewViewModel.PageViewMode != _pageViewMode)
            {
                _printPreviewViewModel.PageViewMode = _pageViewMode;
            }

            _previewPageTotal = (uint)Math.Ceiling((decimal)DocumentController.Instance.PageCount /
                                                    _pagesPerSheet);
            if (_isDuplex || _isBooklet)
            {
                _previewPageTotal = (_previewPageTotal / 2) + (_previewPageTotal % 2);
            }
            if (_printPreviewViewModel.PageTotal != _previewPageTotal)
            {
                _printPreviewViewModel.GoToPageEventHandler -= _goToPageEventHandler;
                _printPreviewViewModel.PageTotal = _previewPageTotal;
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
                isPortrait = (bookletLayout == (int)BookletLayout.TopToBottom);
            }

            return isPortrait;
        }

        #endregion Print Preview

        #region Preview Page Navigation

        /// <summary>
        /// Event handler for page slider is changed
        /// </summary>
        /// <param name="rightPageIndex">requested right page index based on slider value</param>
        /// <returns>task</returns>
        private async Task GoToPage(int rightPageIndex)
        {
            await LoadPage(rightPageIndex, true);
        }

        /// <summary>
        /// Requests for LogicalPages and then applies print setting for the target page only.
        /// Assumes that requested page index is for right side page index
        /// </summary>
        /// <param name="rightPageIndex">requested right page index based on slider value</param></param>
        /// <param name="isFromSlider">true when request is from page slider, false otherwise</param>
        /// <returns>task</returns>
        private async Task LoadPage(int rightPageIndex, bool isSliderEvent)
        {
            // TODO: Add current page logic
            _printPreviewViewModel.IsLoadPageActive = true;

            if (isSliderEvent)
            {
                _currPreviewPageIndex = (_isDuplex || _isBooklet) ? rightPageIndex * 2 :
                                                                    rightPageIndex;
            }

            // Generate right side
            await GenerateSingleLeaf(_currPreviewPageIndex, true);

            // When booklet is on and booklet finishing is off, act like as duplex (short edge)
            // so no need for left side
            if (_isBooklet && _selectedPrinter.PrintSettings.BookletFinishing != (int)BookletFinishing.Off)
            {
                // Compute left side page index
                int leftSidePreviewPageIndex = _currPreviewPageIndex - 1;
                if (leftSidePreviewPageIndex > 0)
                {
                    // Generate left side
                    await GenerateSingleLeaf(leftSidePreviewPageIndex, false);
                }
            }

            GenerateNearPreviewPages();
            // TODO: Add current page logic
            _printPreviewViewModel.IsLoadPageActive = false;
        }

        /// <summary>
        /// Generates a single leaf page(s)
        /// </summary>
        /// <param name="pageIndex">page index</param>
        /// <param name="isRightSide">true when image requested is for right side, false otherwise</param>
        /// <returns>task</returns>
        private async Task GenerateSingleLeaf(int pageIndex, bool isRightSide)
        {
            // Compute for logical page index based on imposition
            int logicalPageIndex = pageIndex * _pagesPerSheet;
            if (_isReversePages)
            {
                logicalPageIndex = (int)DocumentController.Instance.PageCount - 1 - logicalPageIndex;
            }

            // Front
            await SendPreviewPage(pageIndex, logicalPageIndex, isRightSide, false);

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

                // Compute for next logical page index based on imposition
                int nextLogicalPageIndex = backPreviewPageIndex * _pagesPerSheet;

                // Back
                await SendPreviewPage(backPreviewPageIndex, nextLogicalPageIndex, isRightSide, true);
            }
        }

        /// <summary>
        /// Sends the preview page image
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
                DocumentController.Instance.GenerateLogicalPages(logicalPageIndex, _pagesPerSheet);
                Task<List<LogicalPage>> getLogicalPagesTask =
                    DocumentController.Instance.GetLogicalPages(logicalPageIndex, _pagesPerSheet);

                List<LogicalPage> logicalPages = await getLogicalPagesTask;
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

                    // TODO: Duplex and Booklet print settings
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
                        // Send to appropriate page image side
                    }
                    else if (!isRightSide && isBackSide)
                    {
                        // Send to appropriate page image side
                    }
                    return true;
                }
            }
            return false;
        }

        /// <summary>
        /// Generates next and previous PreviewPage images if not exist.
        /// It is assumed here that required LogicalPage images are already done
        /// by DocumentController.
        /// </summary>
        private async void GenerateNearPreviewPages()
        {
            PreviewPage previewPage = null;
            if (!_previewPages.TryGetValue(_currPreviewPageIndex + 1, out previewPage))
            {
                int nextLogicalPageIndex = ((_currPreviewPageIndex + 1) * _pagesPerSheet);
                List<LogicalPage> nextLogicalPages =
                    await DocumentController.Instance.GetLogicalPages(nextLogicalPageIndex,
                    _pagesPerSheet);

                // Next page
                int nextPreviewPageIndex = _currPreviewPageIndex + 1;
                await ApplyPrintSettings(nextLogicalPages, nextPreviewPageIndex,
                    false, (nextPreviewPageIndex % 2 != 0), false);
            }

            if (!_previewPages.TryGetValue(_currPreviewPageIndex - 1, out previewPage))
            {
                int prevLogicalPageIndex = ((_currPreviewPageIndex - 1) * _pagesPerSheet);
                List<LogicalPage> prevLogicalPages =
                    await DocumentController.Instance.GetLogicalPages(prevLogicalPageIndex,
                    _pagesPerSheet);

                // Previous page
                int prevPreviewPageIndex = _currPreviewPageIndex - 1;
                await ApplyPrintSettings(prevLogicalPages, prevPreviewPageIndex,
                    false, (prevPreviewPageIndex % 2 != 0), false);
            }

        }

        #endregion Preview Page Navigation

        #region Print Settings

        /// <summary>
        /// Loads the initial print settings file
        /// </summary>
        private void LoadPrintSettingsOptions()
        {
            PrintSettingToValueConverter valueConverter = new PrintSettingToValueConverter();

            string xmlPath = Path.Combine(Package.Current.InstalledLocation.Path,
                FILE_PATH_ASSET_PRINT_SETTINGS_XML);
            XDocument data = XDocument.Load(xmlPath);

            var printSettingsData = from groupData in data.Descendants(PrintSettingConstant.KEY_GROUP)
                select new PrintSettingGroup
                {
                    Name = (string)groupData.Attribute(PrintSettingConstant.KEY_NAME),
                    Text = (string)groupData.Attribute(PrintSettingConstant.KEY_TEXT),
                    PrintSettings = (
                        from settingData in groupData.Elements(PrintSettingConstant.KEY_SETTING)
                        select new PrintSetting
                        {
                            Name = (string)settingData.Attribute(PrintSettingConstant.KEY_NAME),
                            Text = (string)settingData.Attribute(PrintSettingConstant.KEY_TEXT),
                            Icon = (string)settingData.Attribute(PrintSettingConstant.KEY_ICON),
                            Type = (PrintSettingType)Enum.Parse(typeof(PrintSettingType),
                                (string)settingData.Attribute(PrintSettingConstant.KEY_TYPE)),
                            Value = valueConverter.Convert(
                                (string)settingData.Attribute(PrintSettingConstant.KEY_DEFAULT),
                                null, (string)settingData.Attribute(PrintSettingConstant.KEY_NAME),
                                null),
                            Default = valueConverter.Convert(
                                (string)settingData.Attribute(PrintSettingConstant.KEY_DEFAULT),
                                null, (string)settingData.Attribute(PrintSettingConstant.KEY_NAME),
                                null),
                            Options = (
                                from optionData in settingData.Elements(PrintSettingConstant.KEY_OPTION)
                                select new PrintSettingOption
                                {
                                    Text = (string)optionData.Value,
                                    Index = optionData.ElementsBeforeSelf().Count(),
                                    IsEnabled = true // To be updated later upon apply constraints
                                }).ToList<PrintSettingOption>(),
                            IsEnabled = true,        // To be updated later upon apply constraints
                            IsValueDisplayed = true  // To be updated later upon apply constraints
                        }).ToList<PrintSetting>()
                };
            
            // Construct the PrintSettingList
            _printSettingsViewModel.PrintSettingsList = new PrintSettingList();
            var tempList = printSettingsData.Cast<PrintSettingGroup>().ToList<PrintSettingGroup>();
            foreach (PrintSettingGroup group in tempList)
            {
                _printSettingsViewModel.PrintSettingsList.Add(group);
            }
        }

        /// <summary>
        /// Merges print settings cache (PrintSettings) to print settings list (PrintSettingList)
        /// to reflect actual values from database
        /// </summary>
        private void MergePrintSettings()
        {
            foreach (var group in _printSettingsViewModel.PrintSettingsList)
            {
                foreach (var printSetting in group.PrintSettings)
                {
                    switch (printSetting.Name)
                    {
                        case PrintSettingConstant.NAME_VALUE_COLOR_MODE:
                            printSetting.Value = _selectedPrinter.PrintSettings.ColorMode;
                            break;
                        case PrintSettingConstant.NAME_VALUE_ORIENTATION:
                            printSetting.Value = _selectedPrinter.PrintSettings.Orientation;
                            break;
                        case PrintSettingConstant.NAME_VALUE_COPIES:
                            printSetting.Value = _selectedPrinter.PrintSettings.Copies;
                            break;
                        case PrintSettingConstant.NAME_VALUE_DUPLEX:
                            printSetting.Value = _selectedPrinter.PrintSettings.Duplex;
                            break;
                        case PrintSettingConstant.NAME_VALUE_PAPER_SIZE:
                            printSetting.Value = _selectedPrinter.PrintSettings.PaperSize;
                            break;
                        case PrintSettingConstant.NAME_VALUE_SCALE_TO_FIT:
                            printSetting.Value = _selectedPrinter.PrintSettings.ScaleToFit;
                            break;
                        case PrintSettingConstant.NAME_VALUE_PAPER_TYPE:
                            printSetting.Value = _selectedPrinter.PrintSettings.PaperType;
                            break;
                        case PrintSettingConstant.NAME_VALUE_INPUT_TRAY:
                            printSetting.Value = _selectedPrinter.PrintSettings.InputTray;
                            break;
                        case PrintSettingConstant.NAME_VALUE_IMPOSITION:
                            printSetting.Value = _selectedPrinter.PrintSettings.Imposition;
                            break;
                        case PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER:
                            printSetting.Value = _selectedPrinter.PrintSettings.ImpositionOrder;
                            break;
                        case PrintSettingConstant.NAME_VALUE_SORT:
                            printSetting.Value = _selectedPrinter.PrintSettings.Sort;
                            break;
                        case PrintSettingConstant.NAME_VALUE_BOOKLET:
                            printSetting.Value = _selectedPrinter.PrintSettings.Booklet;
                            break;
                        case PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING:
                            printSetting.Value = _selectedPrinter.PrintSettings.BookletFinishing;
                            break;
                        case PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT:
                            printSetting.Value = _selectedPrinter.PrintSettings.BookletLayout;
                            break;
                        case PrintSettingConstant.NAME_VALUE_FINISHING_SIDE:
                            printSetting.Value = _selectedPrinter.PrintSettings.FinishingSide;
                            break;
                        case PrintSettingConstant.NAME_VALUE_STAPLE:
                            printSetting.Value = _selectedPrinter.PrintSettings.Staple;
                            break;
                        case PrintSettingConstant.NAME_VALUE_PUNCH:
                            printSetting.Value = _selectedPrinter.PrintSettings.Punch;
                            break;
                        case PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY:
                            printSetting.Value = _selectedPrinter.PrintSettings.OutputTray;
                            break;
                        default:
                            // Do nothing
                            break;
                    } // switch-case
                } // foreach printSetting
            } // foreach group
        }

        /// <summary>
        /// Removes print setting and options depending on printer capabilities
        /// </summary>
        private void FilterPrintSettingsUsingCapabilities()
        {
            if (!_selectedPrinter.EnabledBooklet)
            {
                _selectedPrinter.PrintSettings.Booklet = false;

                PrintSetting bookletPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET);
                if (bookletPrintSetting != null)
                {
                    RemovePrintSetting(bookletPrintSetting);
                    
                }
                PrintSetting bookletLayoutPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT);
                if (bookletLayoutPrintSetting != null)
                {
                    RemovePrintSetting(bookletLayoutPrintSetting);

                }
                PrintSetting bookletFinishPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
                if (bookletFinishPrintSetting != null)
                {
                    RemovePrintSetting(bookletFinishPrintSetting);

                }
            }
            if (!_selectedPrinter.EnabledStapler)
            {
                PrintSetting staplePrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_STAPLE);
                if (staplePrintSetting != null)
                {
                    RemovePrintSetting(staplePrintSetting);
                    _selectedPrinter.PrintSettings.Staple = (int)Staple.Off;
                }
                PrintSetting bookletFinishPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
                if (bookletFinishPrintSetting != null)
                {
                    RemovePrintSettingOption(bookletFinishPrintSetting, (int)BookletFinishing.FoldAndStaple);
                }

            }
            // TODO: 3 or 4 Punch capabilities
            if (!_selectedPrinter.EnabledTrayFacedown)
            {
                PrintSetting outputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);
                if (outputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceDown);
                }
            }
            if (!_selectedPrinter.EnabledTrayAutostack)
            {
                PrintSetting outputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);
                if (outputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(outputTrayPrintSetting, (int)OutputTray.Auto);
                }
            }
            if (!_selectedPrinter.EnabledTrayTop)
            {
                PrintSetting outputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);
                if (outputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(outputTrayPrintSetting, (int)OutputTray.Top);
                }
            }
            if (!_selectedPrinter.EnabledTrayStack)
            {
                PrintSetting outputTrayPrintSetting =
                    GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);
                if (outputTrayPrintSetting != null)
                {
                    RemovePrintSettingOption(outputTrayPrintSetting, (int)OutputTray.Stacking);
                }
            }
        }

        /// <summary>
        /// Removes a print setting from the group
        /// </summary>
        /// <param name="printSetting"></param>
        private void RemovePrintSetting(PrintSetting printSetting)
        {
            PrintSettingGroup printSettingGroup = GetPrintSettingGroup(printSetting);
            if (printSettingGroup != null)
            {
                printSettingGroup.PrintSettings.Remove(printSetting);
            }
        }

        /// <summary>
        /// Removes a print setting option
        /// </summary>
        /// <param name="printSetting">print setting</param>
        /// <param name="index">option index</param>
        private void RemovePrintSettingOption(PrintSetting printSetting, int index)
        {
            PrintSettingOption printSettingOption = printSetting.Options
                .FirstOrDefault(setting => setting.Index == index);
            if (printSettingOption != null)
            {
                printSetting.Options.Remove(printSettingOption);
            }
        }

        /// <summary>
        /// Updates constraints (value/enable state) on print settings list (PrintSettingList)
        /// and cache (PagePrintSetting)
        /// </summary>
        private void ApplyPrintSettingConstraints()
        {
            /*
            UpdateConstraintsUsingBooklet(_selectedPrinter.PrintSettings.Booklet);
            UpdateConstraintStapleUsingFinishingSide(_selectedPrinter.PrintSettings.FinishingSide);
            UpdateConstaintPunchUsingFinishingSide(_selectedPrinter.PrintSettings.FinishingSide);
            UpdateConstraintFinishingSideUsingPunch(_selectedPrinter.PrintSettings.Punch);
            UpdateConstraintImpositionOrderUsingImposition(_selectedPrinter.PrintSettings.Imposition);
            UpdateConstraintBookletLayoutUsingOrientation(_selectedPrinter.PrintSettings.Orientation);
             * */

            UpdateConstraintsBasedOnBooklet(_selectedPrinter.PrintSettings.Booklet);
            UpdateConstraintsBasedOnImposition(_selectedPrinter.PrintSettings.Imposition);
            UpdateConstraintsBasedOnFinishingSide(_selectedPrinter.PrintSettings.FinishingSide);
            UpdateConstraintsBasedOnStaple(_selectedPrinter.PrintSettings.Staple);
            UpdateConstraintsBasedOnPunch(_selectedPrinter.PrintSettings.Punch);
        }

        /// <summary>
        /// Queries the print setting group based on print setting
        /// </summary>
        /// <param name="printSetting">print setting</param>
        /// <returns>PrintSettingGroup if founf, else null</returns>
        private PrintSettingGroup GetPrintSettingGroup(PrintSetting printSetting)
        {
            return _printSettingsViewModel.PrintSettingsList.FirstOrDefault(group => group.PrintSettings.Contains(printSetting));
        }

        /// <summary>
        /// Queries the print settings list based on name.
        /// </summary>
        /// <param name="name">print setting name</param>
        /// <returns>PrintSetting if found, else null</returns>
        private PrintSetting GetPrintSetting(string name)
        {
            var query = _printSettingsViewModel.PrintSettingsList.SelectMany(printSettingGroup => printSettingGroup.PrintSettings)
                .Where(ps => ps.Name == name);
            return query.FirstOrDefault();
        }

        /// <summary>
        /// Queries the print setting option based on print setting and the option index.
        /// Option index is fixed (should be mapped with Common\Enum\PrintSettingsOptions class)
        /// </summary>
        /// <param name="printSetting">print setting</param>
        /// <param name="index">option index</param>
        /// <returns>PrintSettingOption if found, else null</returns>
        private PrintSettingOption GetPrintSettingOption(PrintSetting printSetting, int index)
        {
            return printSetting.Options.FirstOrDefault(option => option.Index == index);
        }

        #endregion Print Settings

        #region Print Settings Constraints

        /// <summary>
        /// Checks if the copies value is within acceptable range
        /// </summary>
        /// <param name="value">input value</param>
        /// <param name="result">true if input value is accepted, false otherwise</param>
        /// <returns>the copies value if input value is not accepted</returns>
        private int CheckIfCopiesValid(int value, out bool result)
        {
            result = true;

            // Note: Number received from event is 0 for non-numeric input
            if (value < PrintSettingConstant.COPIES_MIN)
            {
                result = false;
                return PrintSettingConstant.COPIES_MIN;
            }
            else if (value > PrintSettingConstant.COPIES_MAX)
            {
                result = false;
                return PrintSettingConstant.COPIES_MAX;
            }

            return value;
        }

        /// <summary>
        /// Updates print settings dependent on imposition constraints
        /// </summary>
        /// <param name="value">new imposition value</param>
        /// <returns>true when update is done, false otherwise</returns>
        private bool UpdateConstraintsBasedOnImposition(int value)
        {
            bool isUpdated = false;

            PrintSetting impositionOrderPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER);
            /* TODO: Delete commented out line if Booklet related settings
             * does not necessarily required to be updated
             * since in order to update Imposition, Booklet must be Off.
            PrintSetting bookletPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET);
            PrintSetting bookletFinishPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
            PrintSetting bookletLayoutPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT);
             */

            if (value == (int)Imposition.Off)
            {
                if (impositionOrderPrintSetting != null)
                {
                    impositionOrderPrintSetting.IsEnabled = false;
                    impositionOrderPrintSetting.Value = impositionOrderPrintSetting.Default;
                    _selectedPrinter.PrintSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Default;

                    isUpdated = true;
                }
            }
            else if (value == (int)Imposition.TwoUp)
            {
                if (impositionOrderPrintSetting != null)
                {
                    PrintSettingOption twoUpLR = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.TwoUpLeftToRight);
                    if (twoUpLR != null)
                    {
                        twoUpLR.IsEnabled = true;
                    }
                    PrintSettingOption twoUpRL = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.TwoUpRightToLeft);
                    if (twoUpRL != null)
                    {
                        twoUpRL.IsEnabled = true;
                    }
                    PrintSettingOption fourUpLR = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperLeftToRight);
                    if (fourUpLR != null)
                    {
                        fourUpLR.IsEnabled = false;
                    }
                    PrintSettingOption fourUpLB = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperLeftToBottom);
                    if (fourUpLB != null)
                    {
                        fourUpLB.IsEnabled = false;
                    }
                    PrintSettingOption fourUpRL = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperRightToLeft);
                    if (fourUpRL != null)
                    {
                        fourUpRL.IsEnabled = false;
                    }
                    PrintSettingOption fourUpRB = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperRightToBottom);
                    if (fourUpRB != null)
                    {
                        fourUpRB.IsEnabled = false;
                    }
                    impositionOrderPrintSetting.IsEnabled = true;

                    impositionOrderPrintSetting.Value = impositionOrderPrintSetting.Default;
                    _selectedPrinter.PrintSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Default;

                    isUpdated = true;
                }
                /* TODO: Delete commented out lines if Booklet related settings
                 * does not necessarily required to be updated
                 * since in order to update Imposition, Booklet must be Off.
                if (bookletPrintSetting != null)
                {
                    bookletPrintSetting.Value = false;
                    _selectedPrinter.PrintSettings.Booklet = false;

                    isUpdated = true;
                }
                if (bookletFinishPrintSetting != null)
                {
                    bookletFinishPrintSetting.IsEnabled = false;
                    bookletFinishPrintSetting.IsValueDisplayed = false;
                    bookletFinishPrintSetting.Value = bookletFinishPrintSetting.Default;
                    _selectedPrinter.PrintSettings.BookletFinishing = (int)bookletFinishPrintSetting.Default;

                    isUpdated = true;
                }
                if (bookletLayoutPrintSetting != null)
                {
                    bookletLayoutPrintSetting.IsEnabled = false;
                    bookletLayoutPrintSetting.IsValueDisplayed = false;
                    bookletLayoutPrintSetting.Value = bookletLayoutPrintSetting.Default;
                    _selectedPrinter.PrintSettings.BookletLayout = (int)bookletLayoutPrintSetting.Default;

                    isUpdated = true;
                }
                 */
            }
            else if (value == (int)Imposition.FourUp)
            {
                if (impositionOrderPrintSetting != null)
                {
                    PrintSettingOption twoUpLR = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.TwoUpLeftToRight);
                    if (twoUpLR != null)
                    {
                        twoUpLR.IsEnabled = false;
                    }
                    PrintSettingOption twoUpRL = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.TwoUpRightToLeft);
                    if (twoUpRL != null)
                    {
                        twoUpRL.IsEnabled = false;
                    }
                    PrintSettingOption fourUpLR = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperLeftToRight);
                    if (fourUpLR != null)
                    {
                        fourUpLR.IsEnabled = true;
                    }
                    PrintSettingOption fourUpLB = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperLeftToBottom);
                    if (fourUpLB != null)
                    {
                        fourUpLB.IsEnabled = true;
                    }
                    PrintSettingOption fourUpRL = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperRightToLeft);
                    if (fourUpRL != null)
                    {
                        fourUpRL.IsEnabled = true;
                    }
                    PrintSettingOption fourUpRB = GetPrintSettingOption(impositionOrderPrintSetting,
                        (int)ImpositionOrder.FourUpUpperRightToBottom);
                    if (fourUpRB != null)
                    {
                        fourUpRB.IsEnabled = true;
                    }
                    impositionOrderPrintSetting.IsEnabled = true;

                    impositionOrderPrintSetting.Value = impositionOrderPrintSetting.Default;
                    if ((int)impositionOrderPrintSetting.Default == (int)ImpositionOrder.TwoUpLeftToRight)
                    {
                        impositionOrderPrintSetting.Value = (int)ImpositionOrder.FourUpUpperLeftToRight;
                    }
                    else if ((int)impositionOrderPrintSetting.Default == (int)ImpositionOrder.TwoUpRightToLeft)
                    {
                        impositionOrderPrintSetting.Value = (int)ImpositionOrder.FourUpUpperRightToLeft;
                    }
                    _selectedPrinter.PrintSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Default;

                    isUpdated = true;
                }
                /* TODO: Delete commented out lines if Booklet related settings
                 * does not necessarily required to be updated
                 * since in order to update Imposition, Booklet must be Off.
                if (bookletPrintSetting != null)
                {
                    bookletPrintSetting.Value = false;
                    _selectedPrinter.PrintSettings.Booklet = false;

                    isUpdated = true;
                }
                if (bookletFinishPrintSetting != null)
                {
                    bookletFinishPrintSetting.IsEnabled = false;
                    bookletFinishPrintSetting.IsValueDisplayed = false;
                    bookletFinishPrintSetting.Value = bookletFinishPrintSetting.Default;
                    _selectedPrinter.PrintSettings.BookletFinishing = (int)bookletFinishPrintSetting.Default;

                    isUpdated = true;
                }
                if (bookletLayoutPrintSetting != null)
                {
                    bookletLayoutPrintSetting.IsEnabled = false;
                    bookletLayoutPrintSetting.IsValueDisplayed = false;
                    bookletLayoutPrintSetting.Value = bookletLayoutPrintSetting.Default;
                    _selectedPrinter.PrintSettings.BookletLayout = (int)bookletLayoutPrintSetting.Default;

                    isUpdated = true;
                }
                 */
            }

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on booklet constraints
        /// </summary>
        /// <param name="value">new booklet value</param>
        /// <returns>true when update is done, false otherwise</returns>
        private bool UpdateConstraintsBasedOnBooklet(bool value)
        {
            bool isUpdated = false;

            PrintSetting duplexPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_DUPLEX);
            PrintSetting impositionPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_IMPOSITION);
            PrintSetting impositionOrderPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER);
            PrintSetting bookletFinishPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
            PrintSetting bookletLayoutPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT);
            PrintSetting finishingSidePrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_FINISHING_SIDE);
            PrintSetting staplePrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_STAPLE);
            PrintSetting punchPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_PUNCH);
            PrintSetting outputTrayPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);

            if (value)
            {
                if (duplexPrintSetting != null)
                {
                    duplexPrintSetting.IsEnabled = false;
                    duplexPrintSetting.Value = Duplex.ShortEdge;
                    _selectedPrinter.PrintSettings.Duplex = (int)Duplex.ShortEdge;

                    isUpdated = true;
                }
                if (impositionPrintSetting != null)
                {
                    impositionPrintSetting.IsEnabled = false;
                    impositionPrintSetting.IsValueDisplayed = false;
                    impositionPrintSetting.Value = Imposition.Off;
                    _selectedPrinter.PrintSettings.Imposition = (int)Imposition.Off;

                    isUpdated = true;
                }
                if (impositionOrderPrintSetting != null)
                {
                    impositionOrderPrintSetting.IsEnabled = false;
                    impositionOrderPrintSetting.IsValueDisplayed = false;
                    impositionOrderPrintSetting.Value = impositionOrderPrintSetting.Default;
                    _selectedPrinter.PrintSettings.ImpositionOrder = (int)impositionOrderPrintSetting.Default;

                    isUpdated = true;
                }
                if (finishingSidePrintSetting != null)
                {
                    finishingSidePrintSetting.IsEnabled = false;
                    finishingSidePrintSetting.IsValueDisplayed = false;
                    finishingSidePrintSetting.Value = (int)FinishingSide.Left;
                    _selectedPrinter.PrintSettings.FinishingSide = (int)FinishingSide.Left;
                }
                if (staplePrintSetting != null)
                {
                    staplePrintSetting.IsEnabled = false;
                    staplePrintSetting.IsValueDisplayed = false;
                    staplePrintSetting.Value = (int)Staple.Off;
                    _selectedPrinter.PrintSettings.Staple = (int)Staple.Off;

                    isUpdated = true;
                }
                if (punchPrintSetting != null)
                {
                    punchPrintSetting.IsEnabled = false;
                    punchPrintSetting.IsValueDisplayed = false;
                    punchPrintSetting.Value = (int)Punch.Off;
                    _selectedPrinter.PrintSettings.Punch = (int)Punch.Off;

                    isUpdated = true;
                }
                /* Constraints for Output Tray
                if (outputTrayPrintSetting != null)
                {
                    outputTrayPrintSetting.IsEnabled = false;
                    outputTrayPrintSetting.IsValueDisplayed = false;
                    outputTrayPrintSetting.Value = (int)OutputTray.Auto;
                    _selectedPrinter.PrintSettings.OutputTray = (int)OutputTray.Auto;

                    isUpdated = true;
                }
                 */
                if (bookletFinishPrintSetting != null)
                {
                    bookletFinishPrintSetting.IsEnabled = true;
                    bookletFinishPrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
                if (bookletLayoutPrintSetting != null)
                {
                    bookletLayoutPrintSetting.IsEnabled = true;
                    bookletLayoutPrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
            }
            else
            {
                if (duplexPrintSetting != null)
                {
                    duplexPrintSetting.IsEnabled = true;
                    duplexPrintSetting.Value = (int)Duplex.Off; // TODO: Verify if needs to set to Off since this is not indicated in specifications
                    _selectedPrinter.PrintSettings.Duplex = (int)Duplex.Off;

                    isUpdated = true;
                }
                if (impositionPrintSetting != null)
                {
                    impositionPrintSetting.IsEnabled = true;
                    impositionPrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
                if (impositionOrderPrintSetting != null)
                {
                    // impositionOrderPrintSetting.IsEnabled is not set to true since Imposition is Off
                    impositionOrderPrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
                if (finishingSidePrintSetting != null)
                {
                    finishingSidePrintSetting.IsEnabled = true;
                    finishingSidePrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
                if (staplePrintSetting != null)
                {
                    staplePrintSetting.IsEnabled = true;
                    staplePrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
                if (punchPrintSetting != null)
                {
                    punchPrintSetting.IsEnabled = true;
                    punchPrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
                if (outputTrayPrintSetting != null)
                {
                    outputTrayPrintSetting.IsEnabled = true;
                    outputTrayPrintSetting.IsValueDisplayed = true;

                    isUpdated = true;
                }
                if (bookletFinishPrintSetting != null)
                {
                    bookletFinishPrintSetting.IsEnabled = false;
                    bookletFinishPrintSetting.IsValueDisplayed = false;
                    bookletFinishPrintSetting.Value = bookletFinishPrintSetting.Default;
                    _selectedPrinter.PrintSettings.BookletFinishing = (int)bookletFinishPrintSetting.Default;

                    isUpdated = true;
                }
                if (bookletLayoutPrintSetting != null)
                {
                    bookletLayoutPrintSetting.IsEnabled = false;
                    bookletLayoutPrintSetting.IsValueDisplayed = false;
                    bookletLayoutPrintSetting.Value = bookletLayoutPrintSetting.Default;
                    _selectedPrinter.PrintSettings.BookletLayout = (int)bookletLayoutPrintSetting.Default;

                    isUpdated = true;
                }
            }

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on finishing side constraints
        /// </summary>
        /// <param name="value">new finishing side value</param>
        /// <returns>true when update is done, false otherwise</returns>
        private bool UpdateConstraintsBasedOnFinishingSide(int value)
        {
            bool isUpdated = false;

            PrintSetting staplePrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_STAPLE);

            if (staplePrintSetting == null)
            {
                return isUpdated;
            }

            int currFinishingSide = _selectedPrinter.PrintSettings.FinishingSide;
            int currStaple = _selectedPrinter.PrintSettings.Staple;

            if (value == (int)FinishingSide.Left || value == (int)FinishingSide.Right)
            {
                if (currStaple == (int)Staple.OneUpperLeft || currStaple == (int)Staple.OneUpperLeft)
                {
                    staplePrintSetting.Value = (int)Staple.One;
                    _selectedPrinter.PrintSettings.Staple = (int)Staple.One;

                    isUpdated = true;
                }

                PrintSettingOption oneUL = GetPrintSettingOption(staplePrintSetting, (int)Staple.OneUpperLeft);
                if (oneUL != null)
                {
                    oneUL.IsEnabled = false;

                    isUpdated = true;
                }
                PrintSettingOption oneUR = GetPrintSettingOption(staplePrintSetting, (int)Staple.OneUpperRight);
                if (oneUR != null)
                {
                    oneUR.IsEnabled = false;

                    isUpdated = true;
                }
                PrintSettingOption one = GetPrintSettingOption(staplePrintSetting, (int)Staple.One);
                if (one != null)
                {
                    one.IsEnabled = true;

                    isUpdated = true;
                }
            }
            else if (value == (int)FinishingSide.Top)
            {
                // Change selected value
                if (currFinishingSide == (int)FinishingSide.Left && currStaple == (int)Staple.One)
                {
                    staplePrintSetting.Value = (int)Staple.OneUpperLeft;
                    _selectedPrinter.PrintSettings.Staple = (int)Staple.OneUpperLeft;
                }
                else if (currFinishingSide == (int)FinishingSide.Right && currStaple == (int)Staple.One)
                {
                    staplePrintSetting.Value = (int)Staple.OneUpperRight;
                    _selectedPrinter.PrintSettings.Staple = (int)Staple.OneUpperRight;
                }

                PrintSettingOption oneUL = GetPrintSettingOption(staplePrintSetting, (int)Staple.OneUpperLeft);
                if (oneUL != null)
                {
                    oneUL.IsEnabled = true;

                    isUpdated = true;
                }
                PrintSettingOption oneUR = GetPrintSettingOption(staplePrintSetting, (int)Staple.OneUpperRight);
                if (oneUR != null)
                {
                    oneUR.IsEnabled = true;

                    isUpdated = true;
                }
                PrintSettingOption one = GetPrintSettingOption(staplePrintSetting, (int)Staple.One);
                if (one != null)
                {
                    one.IsEnabled = false;

                    isUpdated = true;
                }

            }

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on staple constraints
        /// </summary>
        /// <param name="value">new staple value</param>
        /// <returns>true when update is done, false otherwise</returns>
        private bool UpdateConstraintsBasedOnStaple(int value)
        {
            bool isUpdated = false;

            PrintSetting outputTrayPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);

            if (outputTrayPrintSetting == null)
            {
                return isUpdated;
            }

            int currOutputTray = _selectedPrinter.PrintSettings.OutputTray;

            if (value == (int)Staple.OneUpperLeft || value == (int)Staple.OneUpperRight ||
                value == (int)Staple.One || value == (int)Staple.Two)
            {
                /* Constraints for Output Tray
                if (currOutputTray == (int)OutputTray.FaceUp)
                {
                    int newOutputTray = (int)outputTrayPrintSetting.Default;
                    outputTrayPrintSetting.Value = newOutputTray;
                    _selectedPrinter.PrintSettings.OutputTray = newOutputTray;

                    PrintSettingOption faceUp = GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceUp);
                    if (faceUp != null)
                    {
                        faceUp.IsEnabled = false;
                    }

                    isUpdated = true;
                }
                 */
            }
            else if (value == (int)Staple.Off)
            {
                /* Constraints for Output Tray
                PrintSettingOption faceUp = GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceUp);
                if (faceUp != null)
                {
                    faceUp.IsEnabled = true;

                    isUpdated = true;
                }
                 */
            }

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings dependent on punch constraints
        /// </summary>
        /// <param name="value">new punch value</param>
        /// <returns>true when update is done, false otherwise</returns>
        private bool UpdateConstraintsBasedOnPunch(int value)
        {
            bool isUpdated = false;

            PrintSetting outputTrayPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY);

            if (outputTrayPrintSetting == null)
            {
                return isUpdated;
            }

            int currOutputTray = _selectedPrinter.PrintSettings.OutputTray;

            if (value == (int)Punch.TwoHoles || value == (int)Punch.FourHoles)
            {
                if (currOutputTray == (int)OutputTray.FaceDown) // ||
                    //currOutputTray == (int)OutputTray.FaceUp)
                {
                    int newOutputTray = (int)outputTrayPrintSetting.Default;
                    outputTrayPrintSetting.Value = newOutputTray;
                    _selectedPrinter.PrintSettings.OutputTray = newOutputTray;

                    isUpdated = true;
                }

                PrintSettingOption faceDown = GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceDown);
                if (faceDown != null)
                {
                    faceDown.IsEnabled = false;

                    isUpdated = true;
                }
                /* Constraints for Output Tray
                PrintSettingOption faceUp = GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceUp);
                if (faceUp != null)
                {
                    faceUp.IsEnabled = false;

                    isUpdated = true;
                }
                 */

                isUpdated = true;
            }
            else if (value == (int)Punch.Off)
            {
                PrintSettingOption faceDown = GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceDown);
                if (faceDown != null)
                {
                    faceDown.IsEnabled = true;

                    isUpdated = true;
                }
                /* Constraints for Output Tray
                PrintSettingOption faceUp = GetPrintSettingOption(outputTrayPrintSetting, (int)OutputTray.FaceUp);
                if (faceUp != null)
                {
                    faceUp.IsEnabled = true;

                    isUpdated = true;
                }
                 */

                isUpdated = true;
            }

            return isUpdated;
        }

        /* TODO: Delete unused code
        /// <summary>
        /// Changes the enable state of a print setting and its options
        /// </summary>
        /// <param name="printSetting">print setting</param>
        /// <param name="state">desired state</param>
        private void UpdateAllOptionState(PrintSetting printSetting, bool state)
        {
            foreach (PrintSettingOption option in printSetting.Options)
            {
                option.IsEnabled = state;
            }
            printSetting.IsEnabled = state;
        }

        /// <summary>
        /// Disables all options in a print setting, except the specified option index.
        /// Also disables the print setting and updates the visibility of its value.
        /// Specifying the excluded option index will enable the state of the print setting option.
        /// </summary>
        /// <param name="printSetting">print setting</param>
        /// <param name="selectedValueState">desired state of print setting value</param>
        /// <param name="exceptionIndex">option index to enable</param>
        private void UpdatePrintSettingStateAndDisableAllOptionsExcept(PrintSetting printSetting,
            bool selectedValueState, int exceptionIndex)
        {
            foreach (PrintSettingOption option in printSetting.Options)
            {
                if (option.Index != exceptionIndex)
                {
                    option.IsEnabled = false;
                }
                else
                {
                    option.IsEnabled = true;
                }
            }

            printSetting.IsEnabled = false;
            printSetting.IsValueDisplayed = selectedValueState;
        }
         * */

        /* Old print settings constraints code based on Func Specs diagrams
        //====================================================================================================

        /// <summary>
        /// Updates print settings list (PrintSettingList) and cache (PrintSettings)
        /// for its values and enabled state based on constraints with Booklet print setting.
        /// </summary>
        /// <param name="value">selected Booklet print setting state</param>
        /// <returns>true if constraints are applied, false otherwise</returns>
        private bool UpdateConstraintsUsingBooklet(bool value)
        {
            bool isUpdated = false;

            PrintSetting duplexPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_DUPLEX);
            PrintSetting finishingSidePrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_FINISHING_SIDE);
            PrintSetting staplePrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_STAPLE);
            PrintSetting punchPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_PUNCH);
            PrintSetting impositionPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_IMPOSITION);
            PrintSetting impositionOrderPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER);
            PrintSetting bookletFinishPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
            PrintSetting bookletLayoutPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT);

            if (duplexPrintSetting == null || finishingSidePrintSetting == null ||
                staplePrintSetting == null || punchPrintSetting == null ||
                impositionPrintSetting == null || impositionOrderPrintSetting == null ||
                bookletFinishPrintSetting == null || bookletLayoutPrintSetting == null)
            {
                return isUpdated;
            }

            int defaultFinishingSide = (int)finishingSidePrintSetting.Default;
            int defaultStaple = (int)staplePrintSetting.Default;
            int defaultPunch = (int)punchPrintSetting.Default;

            if (value)
            {
                // Disable views
                duplexPrintSetting.IsEnabled = false;
                finishingSidePrintSetting.IsEnabled = false;
                staplePrintSetting.IsEnabled = false;
                punchPrintSetting.IsEnabled = false;
                impositionPrintSetting.IsEnabled = false;
                impositionOrderPrintSetting.IsEnabled = false;

                // Enable views
                bookletFinishPrintSetting.IsEnabled = true;
                bookletLayoutPrintSetting.IsEnabled = true;

                // Reset values to default
                finishingSidePrintSetting.Value = defaultFinishingSide;
                _selectedPrinter.PrintSettings.FinishingSide = defaultFinishingSide;
                staplePrintSetting.Value = defaultStaple;
                _selectedPrinter.PrintSettings.Staple = defaultStaple;
                punchPrintSetting.Value = defaultPunch;
                _selectedPrinter.PrintSettings.Punch = defaultPunch;

                isUpdated = true;
            }
            else
            {
                // Enable views
                duplexPrintSetting.IsEnabled = true;
                finishingSidePrintSetting.IsEnabled = true;
                staplePrintSetting.IsEnabled = true;
                punchPrintSetting.IsEnabled = true;
                impositionPrintSetting.IsEnabled = true;
                impositionOrderPrintSetting.IsEnabled = true;

                // Disable views
                bookletFinishPrintSetting.IsEnabled = false;
                bookletLayoutPrintSetting.IsEnabled = false;

                isUpdated = true;
            }

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings list (PrintSettingList) and cache (PrintSettings)
        /// for its values and enabled state based on constraints with Finishing Side print setting
        /// against Staple print setting.
        /// </summary>
        /// <param name="value">selected Finishing Side print setting option</param>
        /// <returns>true if constraints are applied, false otherwise</returns>
        private bool UpdateConstraintStapleUsingFinishingSide(int value)
        {
            bool isUpdated = false;

            PrintSetting staplePrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_STAPLE);

            if (staplePrintSetting == null)
            {
                return isUpdated;
            }

            int newStaple = -1;
            int currStaple = _selectedPrinter.PrintSettings.Staple;
            int currFinishingSide = _selectedPrinter.PrintSettings.FinishingSide;

            if (value == (int)FinishingSide.Left ||
                value == (int)FinishingSide.Right)
            {
                if (currStaple == (int)Staple.OneUpperLeft ||
                    currStaple == (int)Staple.OneUpperRight)
                {
                    newStaple = (int)Staple.One;
                }

                // Disable controls
                staplePrintSetting.Options[(int)Staple.OneUpperLeft].IsEnabled = false;
                staplePrintSetting.Options[(int)Staple.OneUpperRight].IsEnabled = false;

                // Enable controls
                staplePrintSetting.Options[(int)Staple.One].IsEnabled = true;
                isUpdated = true;
            }
            else if (value == (int)FinishingSide.Top)
            {
                if (currStaple == (int)Staple.One)
                {
                    if (currFinishingSide == (int)FinishingSide.Left)
                    {
                        newStaple = (int)Staple.OneUpperLeft;
                    }
                    else if (currFinishingSide == (int)FinishingSide.Right)
                    {
                        newStaple = (int)Staple.OneUpperRight;
                    }
                }

                // Disable controls
                staplePrintSetting.Options[(int)Staple.One].IsEnabled = false;

                // Enable controls
                staplePrintSetting.Options[(int)Staple.OneUpperLeft].IsEnabled = true;
                staplePrintSetting.Options[(int)Staple.OneUpperRight].IsEnabled = true;
                isUpdated = true;
            }

            if (newStaple > -1)
            {
                _selectedPrinter.PrintSettings.Staple = newStaple;
                staplePrintSetting.Value = newStaple;
                isUpdated = true;
            }

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings list (PrintSettingList) and cache (PrintSettings)
        /// for its values and enabled state based on constraints with Finishing Side print setting
        /// against Punch print setting.
        /// </summary>
        /// <param name="value">selected Finishing Side print setting option</param>
        /// <returns>true if constraints are applied, false otherwise</returns>
        private bool UpdateConstaintPunchUsingFinishingSide(int value)
        {
            bool isUpdated = false;

            PrintSetting punchPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_PUNCH);
            PrintSetting finishingSidePrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_FINISHING_SIDE);

            if (punchPrintSetting == null || finishingSidePrintSetting == null)
            {
                return isUpdated;
            }

            int newPunch = -1;
            int currPunch = _selectedPrinter.PrintSettings.Punch;
            int defaultPunch = (int)punchPrintSetting.Default;
            int defaultFinishingSide = (int)finishingSidePrintSetting.Default;

            if (currPunch == (int)Punch.FourHoles)
            {
                if (value != defaultFinishingSide)
                {
                    if (defaultFinishingSide != (int)FinishingSide.Left ||
                        (defaultFinishingSide == (int)FinishingSide.Left &&
                         value != (int)FinishingSide.Right))
                    {
                        newPunch = defaultPunch;
                    }
                }
            }

            if (newPunch > -1)
            {
                _selectedPrinter.PrintSettings.Punch = newPunch;
                punchPrintSetting.Value = newPunch;
                isUpdated = true;
            }

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings list (PrintSettingList) and cache (PrintSettings)
        /// for its values and enabled state based on constraints with Punch print setting
        /// against Finishing Side print setting.
        /// </summary>
        /// <param name="value">selected Punch print setting option</param>
        /// <returns>true if constraints are applied, false otherwise</returns>
        private bool UpdateConstraintFinishingSideUsingPunch(int value)
        {
            bool isUpdated = false;

            PrintSetting finishingSidePrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_FINISHING_SIDE);

            if (finishingSidePrintSetting == null)
            {
                return isUpdated;
            }

            int newFinishingSide = -1;
            int currFinishingSide = _selectedPrinter.PrintSettings.FinishingSide;
            int defaultFinishingSide = (int)finishingSidePrintSetting.Default;
            int currPunch = _selectedPrinter.PrintSettings.Punch;

            if (value == (int)Punch.FourHoles)
            {
                if (currFinishingSide != defaultFinishingSide)
                {
                    if (defaultFinishingSide != (int)FinishingSide.Left ||
                        (defaultFinishingSide == (int)FinishingSide.Left &&
                         currFinishingSide != (int)FinishingSide.Right))
                    {
                        newFinishingSide = defaultFinishingSide;
                    }
                }
            }

            if (newFinishingSide > -1)
            {
                _selectedPrinter.PrintSettings.FinishingSide = newFinishingSide;
                finishingSidePrintSetting.Value = newFinishingSide;
                isUpdated = true;
            }

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings list (PrintSettingList) and cache (PrintSettings)
        /// for its values and enabled state based on constraints with Imposition Order print setting
        /// against Imposition print setting.
        /// </summary>
        /// <param name="value">selected Imposition print setting option</param>
        /// <returns>true if constraints are applied, false otherwise</returns>
        private bool UpdateConstraintImpositionOrderUsingImposition(int value)
        {
            bool isUpdated = false;

            PrintSetting impositionOrderPrintSetting =
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER);

            if (impositionOrderPrintSetting == null)
            {
                return isUpdated;
            }

            int newImpositionOrder = -1;
            int currImposition = _selectedPrinter.PrintSettings.Imposition;
            int currImpositionOrder = _selectedPrinter.PrintSettings.ImpositionOrder;

            if (value == (int)Imposition.FourUp)
            {
                if (currImposition == (int)Imposition.Off ||
                    (currImposition == (int)Imposition.TwoUp &&
                    currImpositionOrder == (int)ImpositionOrder.TwoUpLeftToRight))
                {
                    newImpositionOrder = (int)ImpositionOrder.FourUpUpperLeftToRight;
                }
                else if (currImposition == (int)Imposition.TwoUp &&
                    currImpositionOrder != (int)ImpositionOrder.TwoUpLeftToRight)
                {
                    newImpositionOrder = (int)ImpositionOrder.FourUpUpperRightToLeft;
                }

                // Enable control
                impositionOrderPrintSetting.IsEnabled = true;
                isUpdated = true;
            }
            else if (value == (int)Imposition.TwoUp)
            {
                if (currImposition == (int)Imposition.Off ||
                    (currImposition == (int)Imposition.FourUp &&
                    (currImpositionOrder == (int)ImpositionOrder.FourUpUpperLeftToRight ||
                    currImpositionOrder == (int)ImpositionOrder.FourUpUpperLeftToBottom)))
                {
                    newImpositionOrder = (int)ImpositionOrder.TwoUpLeftToRight;
                }
                else if (currImposition == (int)Imposition.FourUp &&
                    (currImpositionOrder != (int)ImpositionOrder.FourUpUpperLeftToRight &&
                    currImpositionOrder != (int)ImpositionOrder.FourUpUpperLeftToBottom))
                {
                    newImpositionOrder = (int)ImpositionOrder.TwoUpRightToLeft;
                }

                // Enable control
                impositionOrderPrintSetting.IsEnabled = true;
                isUpdated = true;
            }
            else if (value == (int)Imposition.Off)
            {
                newImpositionOrder = (int)ImpositionOrder.TwoUpLeftToRight;

                // Disable control
                impositionOrderPrintSetting.IsEnabled = false;
                isUpdated = true;
            }

            if (newImpositionOrder > -1)
            {
                _selectedPrinter.PrintSettings.ImpositionOrder = newImpositionOrder;
                impositionOrderPrintSetting.Value = newImpositionOrder;
                isUpdated = true;
            }

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings list (PrintSettingList) and cache (PrintSettings)
        /// for its values based on constraints with Booklet Layout print setting
        /// against Orientation print setting.
        /// </summary>
        /// <param name="value">selected Orientation print setting option</param>
        /// <returns>true if constraints are applied, false otherwise</returns>
        private bool UpdateConstraintBookletLayoutUsingOrientation(int value)
        {
            bool isUpdated = false;

            PrintSetting bookletLayoutPrintSetting =
                 GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT);

            if (bookletLayoutPrintSetting == null)
            {
                return isUpdated;
            }

            int newBookletLayout = -1;
            int currBookletLayout = _selectedPrinter.PrintSettings.BookletLayout;
            int defaultBookletLayout = (int)bookletLayoutPrintSetting.Default;

            if ((value == (int)Orientation.Landscape &&
                (currBookletLayout == (int)BookletLayout.LeftToRight ||
                currBookletLayout == (int)BookletLayout.RightToLeft)) ||
                (value == (int)Orientation.Portrait &&
                currBookletLayout == (int)BookletLayout.TopToBottom))
            {
                newBookletLayout = defaultBookletLayout;
            }

            if (newBookletLayout > -1)
            {
                _selectedPrinter.PrintSettings.BookletLayout = newBookletLayout;
                bookletLayoutPrintSetting.Value = newBookletLayout;
                isUpdated = true;
            }

            return isUpdated;
        }
        */

        #endregion Print Settings Constraints

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

                Size paperSize = GetPaperSize(_selectedPrinter.PrintSettings.PaperSize);

                bool isPortrait = IsPortrait(_selectedPrinter.PrintSettings.Orientation,
                    _selectedPrinter.PrintSettings.BookletLayout);

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
                                ApplyPageImageToPaper(_selectedPrinter.PrintSettings.ScaleToFit,
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
                        _selectedPrinter.PrintSettings.ImpositionOrder, out isFinalPortrait);
                }
                else if (_pagesPerSheet == 1)
                {
                    finalBitmap = WriteableBitmapExtensions.Clone(pageImages[0]);
                }

                // Check color mode value
                if (_selectedPrinter.PrintSettings.ColorMode.Equals((int)ColorMode.Mono))
                {
                    ApplyMonochrome(finalBitmap);
                }

                int finishingSide = _selectedPrinter.PrintSettings.FinishingSide;
                int holeCount = GetPunchHoleCount(_selectedPrinter.PrintSettings.Punch);
                int staple = _selectedPrinter.PrintSettings.Staple;

                if (_isDuplex) // Also hit when booklet is on and booklet finishing is off
                {
                    await ApplyDuplex(finalBitmap, _selectedPrinter.PrintSettings.Duplex,
                        finishingSide, holeCount, staple, isFinalPortrait, isBackSide);
                }
                else if (_isBooklet)
                {
                    await ApplyBooklet(finalBitmap, _selectedPrinter.PrintSettings.BookletFinishing,
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

                    // Add to PreviewPage list
                    PreviewPage previewPage = new PreviewPage((uint)previewPageIndex,
                        tempPageImage.Name, new Size(finalBitmap.PixelWidth, finalBitmap.PixelHeight));
                    if (_previewPages.ContainsKey(previewPageIndex))
                    {
                        // Delete actual image file using existing entry
                        await StorageFileUtility.DeleteFile(_previewPages[previewPageIndex].Name, tempFolder);
                        // Overwrite the new entry from the list
                        _previewPages[previewPageIndex] = previewPage;
                    }
                    else
                    {
                        _previewPages.Add(previewPageIndex, previewPage);
                    }

                    // Check if needs to send the page image
                    // Don't bother to send the old requests
                    if (enableSend && _currPreviewPageIndex == previewPageIndex)
                    {
                        // Open the bitmap
                        BitmapImage bitmapImage = new BitmapImage(new Uri(tempPageImage.Path));

                        // TODO: Duplex and Booklet on the actual side of the two page view
                        if (isRightSide && !isBackSide)
                        {
                            _printPreviewViewModel.RightPageImage = bitmapImage;
                            _printPreviewViewModel.RightPageActualSize = previewPage.ActualSize;

                            _printPreviewViewModel.InitializeGestures();
                        }
                        else if (!isRightSide && !isBackSide)
                        {
                            _printPreviewViewModel.LeftPageImage = bitmapImage;
                            _printPreviewViewModel.LeftPageActualSize = previewPage.ActualSize;
                        }
                        else if (isRightSide && isBackSide)
                        {
                            // Send to appropriate side
                        }
                        else if (!isRightSide && isBackSide)
                        {
                            // Send to appropriate side
                        }
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
            double length1 = paperSize.Width * ImageConstant.BASE_DPI;
            double length2 = paperSize.Height * ImageConstant.BASE_DPI;

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
            double distanceBetweenHoles = GetDistanceBetweenHoles(_selectedPrinter.PrintSettings.Punch);
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
        /// Computes the number of pages based on imposition
        /// </summary>
        /// <param name="imposition">imposition type</param>
        /// <returns>number of pages per sheet</returns>
        private int GetPagesPerSheet(int imposition)
        {
            int pagesPerSheet = 1;
            switch (imposition)
            {
                case (int)Imposition.TwoUp:
                    pagesPerSheet = 2;
                    break;
                case (int)Imposition.FourUp:
                    pagesPerSheet = 4;
                    break;
                case (int)Imposition.Off:
                default:
                    // Do nothing
                    break;
            }

            return pagesPerSheet;
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
                    numberOfHoles = (GlobalizationUtility.IsJapaneseLocale()) ? 3 : 4;
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
                    distance = (GlobalizationUtility.IsJapaneseLocale()) ?
                        PrintSettingConstant.PUNCH_BETWEEN_THREE_HOLES_DISTANCE :
                        PrintSettingConstant.PUNCH_BETWEEN_FOUR_HOLES_DISTANCE;
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
        public void Print()
        {
            // TODO: Check if printer is online (NetworkController)
            bool isOnline = true;

            if (isOnline)
            {
                // TODO: Prepare for printing (DirectPrintController)
                // Pass DocumentController.Instance.FileName, DocumentController.Instance.PdfFile and _selectedPrinter

                // Save to Jobs
                PrintJob printJob = new PrintJob()
                {
                    PrinterId = _selectedPrinter.Id,
                    Name = DocumentController.Instance.FileName,
                    Date = DateTime.Now,
                    Result = 0 // TODO: Set actual result
                };
                JobController.Instance.SavePrintJob(printJob);
            }
            else
            {
                // TODO: Display error message
            }
        }

        #endregion Print

    }

}

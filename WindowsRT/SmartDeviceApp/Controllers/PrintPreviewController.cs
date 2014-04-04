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
        public delegate void SelectedPrintSettingOptionEventHandler(PrintSetting printSetting,
            int selectedIndex);
        private GoToPageEventHandler _goToPageEventHandler;
        private SelectedPrintSettingOptionEventHandler _selectedPrintOptionEventHandler;

        private const string FORMAT_PREFIX_PREVIEW_PAGE_IMAGE = "previewpage";
        private const string FORMAT_FILE_NAME_PREVIEW_PAGE_IMAGE =
            FORMAT_PREFIX_PREVIEW_PAGE_IMAGE + "{0:0000}-{1:yyyyMMddHHmmssffff}.jpg";
        private const string FILE_NAME_EMPTY_IMAGE = FORMAT_PREFIX_PREVIEW_PAGE_IMAGE + "_empty.jpg";
        private const string FILE_PATH_ASSET_PRINT_SETTINGS_XML = "Assets/printsettings.xml";
        private const string FILE_PATH_RES_IMAGE_STAPLE = "Resources/Images/img_staple.png";
        private const string FILE_PATH_RES_IMAGE_PUNCH = "Resources/Images/img_punch.png";

        private PrintPreviewViewModel _printPreviewViewModel;
        private PrintSettingsViewModel _printSettingsViewModel;
        private PrintSettingOptionsViewModel _printSettingOptionsViewModel;
        private PrintSettingList _printSettingList;
        private Printer _selectedPrinter;
        private int _pagesPerSheet = 1;
        private bool _isDuplex = false;
        private bool _isBooklet = false;
        private Dictionary<int, PreviewPage> _previewPages; // Generated PreviewPages from the start
        private uint _previewPageTotal;
        private PageViewMode _pageViewMode;
        private int _currPreviewPageIndex;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static PrintPreviewController() { }

        private PrintPreviewController() { }

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
            _goToPageEventHandler = new GoToPageEventHandler(LoadPage);
            _selectedPrintOptionEventHandler = new SelectedPrintSettingOptionEventHandler(UpdateSelectedPrintSettingOption);

            _printPreviewViewModel = new ViewModelLocator().PrintPreviewViewModel;
            _printSettingsViewModel = new ViewModelLocator().PrintSettingsViewModel;
            _printSettingOptionsViewModel = new ViewModelLocator().PrintSettingOptionsViewModel;

            await Cleanup(); // Ensure to clean up previous

            _selectedPrinter = null;
            _previewPages = new Dictionary<int, PreviewPage>();

            // Get print settings if document is successfully loaded
            if (DocumentController.Instance.IsFileLoaded)
            {
                _previewPageTotal = DocumentController.Instance.PageCount;

                // Get print settings
                LoadPrintSettingsOptions();
                await GetPrinterAndPrintSetting();
                ApplyPrintSettingConstraints();

                // Send list to view model
                _printSettingsViewModel.PrintSettingsList = _printSettingList;

                await UpdatePreviewInfo(true, true, true);

                InitializeGestures();
                _printPreviewViewModel.GoToPageEventHandler += _goToPageEventHandler;
                _printPreviewViewModel.SetInitialPageIndex(0);
                _printPreviewViewModel.DocumentTitleText = DocumentController.Instance.FileName;

                _printSettingOptionsViewModel.SelectedPrintSettingOptionEventHandler += _selectedPrintOptionEventHandler;
                await LoadPage(0);
            }
            else
            {
                // Notify error
            }
        }

        /// <summary>
        /// Clean-up
        /// </summary>
        /// <returns>task</returns>
        public async Task Cleanup()
        {
            _printPreviewViewModel.GoToPageEventHandler -= _goToPageEventHandler;
            _printSettingOptionsViewModel.SelectedPrintSettingOptionEventHandler -= _selectedPrintOptionEventHandler;
            _selectedPrinter = null;
            await ClearPreviewPageListAndImages();
            _previewPages = null;
        }

        /// <summary>
        /// Resets the generated PreviewPage(s) list and removed the page images from AppData
        /// temporary store.
        /// </summary>
        /// <returns>task</returns>
        private async Task ClearPreviewPageListAndImages()
        {
            if (_previewPages != null)
            {
                _previewPages.Clear();
            }

            await StorageFileUtility.DeleteFiles(FORMAT_PREFIX_PREVIEW_PAGE_IMAGE,
                ApplicationData.Current.TemporaryFolder);
        }

        #region Database and Default Values Operations

        /// <summary>
        /// Retrieves printer and print settings.
        /// If no default printer is found, a dummy printer (with ID = -1) is set as
        /// selected printer and default print settings are assigned.
        /// </summary>
        /// <returns>task</returns>
        public async Task GetPrinterAndPrintSetting()
        {
            // Get default printer and print settings from database
            _selectedPrinter = await DatabaseController.Instance.GetDefaultPrinter();
            if (_selectedPrinter.Id < 0 || !_selectedPrinter.IsDefault)
            {
                // No default printer
                _selectedPrinter.PrintSetting = DefaultsUtility.GetDefaultPrintSetting(_printSettingList);
            }
            else
            {
                // Get print settings
                _selectedPrinter.PrintSetting =
                    await DatabaseController.Instance.GetPrintSetting(_selectedPrinter.Id);
                if (_selectedPrinter.PrintSetting == null)
                {
                    _selectedPrinter.PrintSetting = DefaultsUtility.GetDefaultPrintSetting(_printSettingList);
                }
            }
        }

        #endregion Database and Default Values Operations

        #region Print Preview

        /// <summary>
        /// Initializes the gesture of the preview area.
        /// Final paper size must be known before using this function.
        /// </summary>
        private void InitializeGestures()
        {
            Size paperSize = PrintSettingConverter.PaperSizeIntToSizeConverter.Convert(
                _selectedPrinter.PrintSetting.PaperSize);
            bool isPortrait = PrintSettingConverter.OrientationIntToBoolConverter.Convert(
                        _selectedPrinter.PrintSetting.Orientation);

            _printPreviewViewModel.RightPageActualSize = GetPreviewPageImageSize(paperSize, isPortrait);
            _printPreviewViewModel.InitializeGestures();
        }

        /// <summary>
        /// Receiver of the selected print setting option.
        /// Updates the print settings list (PrintSettingList) and cache (PagePrintSettings),
        /// updates value and enabled options based on constraints, and applies
        /// changes to the PreviewPage image.
        /// </summary>
        /// <param name="printSetting">affected print setting</param>
        /// <param name="selected">updated value</param>
        private async void UpdateSelectedPrintSettingOption(PrintSetting printSetting, int selectedIndex)
        {
            if (printSetting == null)
            {
                return;
            }

            string name = printSetting.Name;
            PrintSetting result = GetPrintSetting(name);
            if (result == null)
            {
                return;
            }
            result.Value = selectedIndex;

            // Manual check here what is changed
            bool isPreviewPageAffected = false;
            bool isPageCountAffected = false;
            bool isConstraintAffected = false;
            if (name.Equals(PrintSettingConstant.NAME_VALUE_COLOR_MODE))
            {
                int prevColorMode = _selectedPrinter.PrintSetting.ColorMode;
                if (_selectedPrinter.PrintSetting.ColorMode != selectedIndex)
                {
                    _selectedPrinter.PrintSetting.ColorMode = selectedIndex;
                    // Matters only if changed to/from Black
                    if (prevColorMode == (int)ColorMode.Mono ||
                        selectedIndex == (int)ColorMode.Mono)
                    {
                        isPreviewPageAffected = true;
                    }
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_ORIENTATION))
            {
                if (_selectedPrinter.PrintSetting.Orientation != selectedIndex)
                {
                    _selectedPrinter.PrintSetting.Orientation = selectedIndex;
                    isPreviewPageAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_DUPLEX))
            {
                if (_selectedPrinter.PrintSetting.Duplex != selectedIndex)
                {
                    _selectedPrinter.PrintSetting.Duplex = selectedIndex;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_PAPER_SIZE))
            {
                if (_selectedPrinter.PrintSetting.PaperSize != selectedIndex)
                {
                    _selectedPrinter.PrintSetting.PaperSize = selectedIndex;
                    isPreviewPageAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_PAPER_TYPE))
            {
                if (_selectedPrinter.PrintSetting.PaperType != selectedIndex)
                {
                    _selectedPrinter.PrintSetting.PaperType = selectedIndex;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_INPUT_TRAY))
            {
                if (_selectedPrinter.PrintSetting.InputTray != selectedIndex)
                {
                    _selectedPrinter.PrintSetting.InputTray = selectedIndex;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_IMPOSITION))
            {
                if (_selectedPrinter.PrintSetting.Imposition != selectedIndex)
                {
                    _selectedPrinter.PrintSetting.Imposition = selectedIndex;
                    isPreviewPageAffected = true;
                    isPageCountAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_IMPOSITION_ORDER))
            {
                if (_selectedPrinter.PrintSetting.ImpositionOrder != selectedIndex)
                {
                    _selectedPrinter.PrintSetting.ImpositionOrder = selectedIndex;
                    if (_pagesPerSheet > 1) // Matters only if pages per sheet is more than 1
                    {
                        isPreviewPageAffected = true;
                    }
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_SORT))
            {
                if (_selectedPrinter.PrintSetting.Sort != selectedIndex)
                {
                    _selectedPrinter.PrintSetting.Sort = selectedIndex;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING))
            {
                if (_selectedPrinter.PrintSetting.BookletFinishing != selectedIndex)
                {
                    _selectedPrinter.PrintSetting.BookletFinishing = selectedIndex;
                    // Matters only when staple is ON
                    if (_selectedPrinter.PrintSetting.Staple != (int)Staple.Off)
                    {
                        isPreviewPageAffected = true;
                    }
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET_LAYOUT))
            {
                if (_selectedPrinter.PrintSetting.BookletLayout != selectedIndex)
                {
                    _selectedPrinter.PrintSetting.BookletLayout = selectedIndex;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_FINISHING_SIDE))
            {
                if (_selectedPrinter.PrintSetting.FinishingSide != selectedIndex)
                {
                    isConstraintAffected = UpdateConstraintStapleUsingFinishingSide(selectedIndex);
                    isConstraintAffected = UpdateConstaintPunchUsingFinishingSide(selectedIndex) ||
                        isConstraintAffected;
                    _selectedPrinter.PrintSetting.FinishingSide = selectedIndex;
                    // Matters only when staple or punch is ON
                    if (_selectedPrinter.PrintSetting.Staple != (int)Staple.Off ||
                        _selectedPrinter.PrintSetting.Punch != (int)Punch.Off)
                    {
                        isPreviewPageAffected = true;
                    }
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_STAPLE))
            {
                if (_selectedPrinter.PrintSetting.Staple != selectedIndex)
                {
                    _selectedPrinter.PrintSetting.Staple = selectedIndex;
                    isPreviewPageAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_PUNCH))
            {
                if (_selectedPrinter.PrintSetting.Punch != selectedIndex)
                {
                    isConstraintAffected = UpdateConstraintFinishingSideUsingPunch(selectedIndex);
                    _selectedPrinter.PrintSetting.Punch = selectedIndex;
                    isPreviewPageAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_OUTPUT_TRAY))
            {
                if (_selectedPrinter.PrintSetting.OutputTray != selectedIndex)
                {
                    _selectedPrinter.PrintSetting.OutputTray = selectedIndex;
                }
            }

            if (isConstraintAffected)
            {
                // Send to UI here
                _printSettingsViewModel.PrintSettingsList = _printSettingList;
            }
            // Generate PreviewPages again
            if (isPreviewPageAffected || isPageCountAffected || isConstraintAffected)
            {
                await UpdatePreviewInfo(false, false, isPageCountAffected);
                //InitializeGestures();
                _printPreviewViewModel.GoToPage((uint)_currPreviewPageIndex);
            }
        }

        /// <summary>
        /// Receiver of the selected print setting option (must be a switch).
        /// Updates the print settings list (PrintSettingList) and cache (PagePrintSettings),
        /// updates value and enabled options based on constraints, and applies
        /// changes to the PreviewPage image.
        /// </summary>
        /// <param name="printSetting">affected print setting</param>
        /// <param name="state">updated value</param>
        private async void UpdatePrintSettingState(PrintSetting printSetting, bool state)
        {
            if (printSetting == null)
            {
                return;
            }

            string name = printSetting.Name;
            PrintSetting result = GetPrintSetting(name);
            if (result == null)
            {
                return;
            }
            result.Value = state;

            bool isPreviewPageAffected = false;
            bool isConstraintAffected = false;
            if (name.Equals(PrintSettingConstant.NAME_VALUE_SCALE_TO_FIT))
            {
                if (_selectedPrinter.PrintSetting.ScaleToFit != state)
                {
                    _selectedPrinter.PrintSetting.ScaleToFit = state;
                    isPreviewPageAffected = true;
                }
            }
            else if (name.Equals(PrintSettingConstant.NAME_VALUE_BOOKLET))
            {
                if (_selectedPrinter.PrintSetting.Booklet != state)
                {
                    isConstraintAffected = UpdateConstraintsUsingBooklet(state);
                    _selectedPrinter.PrintSetting.Booklet = state;
                    isPreviewPageAffected = true;
                }
            }

            await UpdatePreviewInfo(isPreviewPageAffected, false, false);

            if (isConstraintAffected)
            {
                // Send to UI here
                _printSettingsViewModel.PrintSettingsList = _printSettingList;
            }
            if (isPreviewPageAffected || isConstraintAffected)
            {
                _printPreviewViewModel.GoToPage((uint)_currPreviewPageIndex);
            }
        }

        /// <summary>
        /// Checks for view related print setting and notifies view model
        /// </summary>
        /// <param name="updatePageBind">true when view mode is needed to update in view model</param>
        /// <param name="updateDuplex"></param>
        /// <param name="updatePageCount">true when page count is needed to update in view model</param>
        /// <returns>task</returns>
        private async Task UpdatePreviewInfo(bool updatePageBind, bool updateDuplex, bool updatePageCount)
        {
            // Clean-up generated PreviewPages
            await ClearPreviewPageListAndImages();

            // Send UI related items
            if (_selectedPrinter.PrintSetting.Booklet)
            {
                _isBooklet = true;
                //_pageViewMode = PageViewMode.TwoPageView;
            }
            else
            {
                _isBooklet = false;
                _pageViewMode = PageViewMode.SinglePageView;
            }

            _isDuplex = PrintSettingConverter.DuplexIntToBoolConverter.Convert(
                _selectedPrinter.PrintSetting.Duplex);

            _pagesPerSheet = PrintSettingConverter.ImpositionIntToNumberOfPagesConverter
                .Convert(_selectedPrinter.PrintSetting.Imposition);

            if (updatePageBind)
            {
                _printPreviewViewModel.PageViewMode = _pageViewMode;

            }
            if (updatePageCount)
            {
                _previewPageTotal = (uint)Math.Ceiling(
                    (decimal)DocumentController.Instance.PageCount / _pagesPerSheet);
                _printPreviewViewModel.PageTotal = _previewPageTotal;
            }
        }

        #endregion Print Preview

        #region Preview Page Navigation

        /// <summary>
        /// Requests for LogicalPages and then applies print setting for the target page only
        /// </summary>
        /// <param name="targetPreviewPageIndex">requested page index</param>
        /// <returns>task</returns>
        public async Task LoadPage(int targetPreviewPageIndex)
        {
            _currPreviewPageIndex = targetPreviewPageIndex;

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
                    _printPreviewViewModel.RightPageImage = bitmapImage;
                    _printPreviewViewModel.RightPageActualSize = previewPage.ActualSize;

                    GenerateNearPreviewPages();

                    return;
                }
            }

            // Else, generate pages, apply print setting and send
            int targetLogicalPageIndex = targetPreviewPageIndex * _pagesPerSheet;
            DocumentController.Instance.GenerateLogicalPages(targetLogicalPageIndex,
                _pagesPerSheet);
            Task<List<LogicalPage>> getLogicalPagesTask =
                DocumentController.Instance.GetLogicalPages(targetLogicalPageIndex,
                    _pagesPerSheet);

            List<LogicalPage> logicalPages = await getLogicalPagesTask;
            await ApplyPrintSettings(logicalPages, targetPreviewPageIndex, true);

            GenerateNearPreviewPages();
        }

        #endregion Preview Page Navigation

        #region Print Settings

        /// <summary>
        /// Loads the initial print settings file
        /// </summary>
        private void LoadPrintSettingsOptions()
        {
            PagePrintSettingToValueConverter valueConverter = new PagePrintSettingToValueConverter();

            string xmlPath = Path.Combine(Package.Current.InstalledLocation.Path,
                FILE_PATH_ASSET_PRINT_SETTINGS_XML);
            XDocument data = XDocument.Load(xmlPath);

            var printSettingsData = from groupData in data.Descendants(PrintSettingConstant.KEY_GROUP)
                select new PrintSettingGroup
                {
                    Name = (string)groupData.Attribute(PrintSettingConstant.KEY_NAME),
                    Text = (string)groupData.Attribute(PrintSettingConstant.KEY_TEXT),
                    PrintSettings =
                    (
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
                            Options =
                            (
                                from optionData in settingData.Elements(PrintSettingConstant.KEY_OPTION)
                                select new PrintSettingOption
                                {
                                    Text = (string)optionData.Value,
                                    Index = optionData.ElementsBeforeSelf().Count(),
                                    IsEnabled = true // To be updated later upon apply constraints
                                }).ToList<PrintSettingOption>(),
                            IsEnabled = true // To be updated later upon apply constraints
                        }).ToList<PrintSetting>()
                };
            
            // Construct the PrintSettingList
            _printSettingList = new PrintSettingList();
            var tempList = printSettingsData.Cast<PrintSettingGroup>().ToList<PrintSettingGroup>();
            foreach (PrintSettingGroup group in tempList)
            {
                _printSettingList.Add(group);
            }
        }

        /// <summary>
        /// Updates constraints (value/enable state) on print settings list (PrintSettingList)
        /// and cache (PagePrintSetting)
        /// </summary>
        private void ApplyPrintSettingConstraints()
        {
            UpdateConstraintsUsingBooklet(_selectedPrinter.PrintSetting.Booklet);
            UpdateConstraintStapleUsingFinishingSide(_selectedPrinter.PrintSetting.FinishingSide);
            UpdateConstaintPunchUsingFinishingSide(_selectedPrinter.PrintSetting.FinishingSide);
            UpdateConstraintFinishingSideUsingPunch(_selectedPrinter.PrintSetting.Punch);
        }

        /// <summary>
        /// Queries the print settings list based on name.
        /// </summary>
        /// <param name="name">print setting name</param>
        /// <returns>PrintSetting if found, else null</returns>
        private PrintSetting GetPrintSetting(string name)
        {
            var query = _printSettingList.SelectMany(printSettingGroup => printSettingGroup.PrintSettings)
                .Where(ps => ps.Name == name);
            PrintSetting result = query.First();

            return result;
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
                await ApplyPrintSettings(nextLogicalPages, _currPreviewPageIndex + 1, false);
            }

            if (!_previewPages.TryGetValue(_currPreviewPageIndex - 1, out previewPage))
            {
                int prevLogicalPageIndex = ((_currPreviewPageIndex - 1) * _pagesPerSheet);
                List<LogicalPage> prevLogicalPages =
                    await DocumentController.Instance.GetLogicalPages(prevLogicalPageIndex,
                    _pagesPerSheet);

                // Previous page
                await ApplyPrintSettings(prevLogicalPages, _currPreviewPageIndex - 1, false);
            }

        }

        #endregion Print Settings

        #region Print Settings Constraints

        /// <summary>
        /// Updates print settings list (PrintSettingList) and cache (PagePrintSettings)
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
                GetPrintSetting(PrintSettingConstant.NAME_VALUE_BOOKLET_FINISHING);
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
                _selectedPrinter.PrintSetting.FinishingSide = defaultFinishingSide;
                staplePrintSetting.Value = defaultStaple;
                _selectedPrinter.PrintSetting.Staple = defaultStaple;
                punchPrintSetting.Value = defaultPunch;
                _selectedPrinter.PrintSetting.Punch = defaultPunch;

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
        /// Updates print settings list (PrintSettingList) and cache (PagePrintSettings)
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
            int currStaple = _selectedPrinter.PrintSetting.Staple;
            int currFinishingSide = _selectedPrinter.PrintSetting.FinishingSide;

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
                _selectedPrinter.PrintSetting.Staple = newStaple;
                staplePrintSetting.Value = newStaple;
                isUpdated = true;
            }

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings list (PrintSettingList) and cache (PagePrintSettings)
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
            int currPunch = _selectedPrinter.PrintSetting.Punch;
            int defaultPunch = (int)punchPrintSetting.Default;
            int defaultFinishingSide = (int)finishingSidePrintSetting.Default;

            if (currPunch == (int)Punch.FourHoles)
            {
                if (value != defaultFinishingSide)
                {
                    if (defaultFinishingSide == (int)FinishingSide.Top ||
                        (defaultFinishingSide == (int)FinishingSide.Left &&
                         value != (int)FinishingSide.Right))
                    {
                        newPunch = defaultPunch;
                    }
                }
            }

            if (newPunch > -1)
            {
                _selectedPrinter.PrintSetting.Punch = newPunch;
                punchPrintSetting.Value = newPunch;
                isUpdated = true;
            }

            return isUpdated;
        }

        /// <summary>
        /// Updates print settings list (PrintSettingList) and cache (PagePrintSettings)
        /// for its values and enabled state based on constraints with Punch print setting
        /// against Finishing Side print setting.
        /// </summary>
        /// <param name="value">selected Punch print setting option</param>
        /// <returns>true if constraints are applies, false otherwise</returns>
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
            int currFinishingSide = _selectedPrinter.PrintSetting.FinishingSide;
            int defaultFinishingSide = (int)finishingSidePrintSetting.Default;
            int currPunch = _selectedPrinter.PrintSetting.Punch;

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
                _selectedPrinter.PrintSetting.FinishingSide = newFinishingSide;
                finishingSidePrintSetting.Value = newFinishingSide;
                isUpdated = true;
            }

            return isUpdated;
        }

        #endregion Print Settings Constraints

        #region Apply Print Settings

        /// <summary>
        /// Applies print settings to LogicalPage images then creates a PreviewPage
        /// </summary>
        /// <param name="logicalPages">source LogicalPage images</param>
        /// <param name="previewPageIndex">target page index</param>
        /// <param name="enableSend">true when needs to send to preview, false otherwise</param>
        /// <returns></returns>
        private async Task ApplyPrintSettings(List<LogicalPage> logicalPages, int previewPageIndex,
            bool enableSend)
        {
            if (logicalPages != null && logicalPages.Count > 0)
            {
                StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;

                WriteableBitmap finalBitmap = new WriteableBitmap(1, 1); // Size does not matter yet
                List<WriteableBitmap> pageImages = new List<WriteableBitmap>(); // Ordered list

                Size paperSize = PrintSettingConverter.PaperSizeIntToSizeConverter.Convert(
                    _selectedPrinter.PrintSetting.PaperSize);

                bool isPortrait = PrintSettingConverter.OrientationIntToBoolConverter.Convert(
                    _selectedPrinter.PrintSetting.Orientation);

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

                            WriteableBitmap canvasBitmap = ApplyPaperSizeAndOrientation(paperSize,
                                isPortrait);

                            if (_pagesPerSheet > 1)
                            {
                                ApplyPageImageToPaper(true, canvasBitmap, pageBitmap);
                            }
                            else
                            {
                                ApplyPageImageToPaper(_selectedPrinter.PrintSetting.ScaleToFit,
                                    canvasBitmap, pageBitmap);
                            }

                            pageImages.Add(canvasBitmap);
                        }
                    }
                    catch (Exception)
                    {
                        // Error handling (UnauthorizedAccessException)
                    }
                }

                // Check imposition value
                if (_pagesPerSheet > 1)
                {
                    finalBitmap = ApplyImposition(paperSize, pageImages, isPortrait,
                        _selectedPrinter.PrintSetting.ImpositionOrder);
                }
                else
                {
                    finalBitmap = pageImages[0];
                }

                // Check color mode value
                if (_selectedPrinter.PrintSetting.ColorMode.Equals((int)ColorMode.Mono))
                {
                    ApplyMonochrome(finalBitmap);
                }

                // Apply punch
                int holeCount = PrintSettingConverter.PunchIntToNumberOfHolesConverter.Convert(
                        _selectedPrinter.PrintSetting.Punch);
                if (holeCount > 0)
                {
                    await ApplyPunch(finalBitmap, holeCount, _selectedPrinter.PrintSetting.FinishingSide);
                }

                // Apply staple
                if ((_selectedPrinter.PrintSetting.Booklet &&
                    _selectedPrinter.PrintSetting.BookletFinishing == (int)BookletFinishing.FoldAndStaple) ||
                    _selectedPrinter.PrintSetting.Staple != (int)Staple.Off)
                {
                    await ApplyStaple(finalBitmap, _selectedPrinter.PrintSetting.Staple,
                        _selectedPrinter.PrintSetting.FinishingSide);
                }

                try
                {
                    // Save PreviewPage into AppData temporary store
                    StorageFile tempPageImage = await tempFolder.CreateFileAsync(
                        String.Format(FORMAT_FILE_NAME_PREVIEW_PAGE_IMAGE, previewPageIndex, DateTime.UtcNow),
                        CreationCollisionOption.ReplaceExisting);
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

                        // TODO: Duplex and Booklet print settings
                        _printPreviewViewModel.RightPageImage = bitmapImage;
                        _printPreviewViewModel.RightPageActualSize = previewPage.ActualSize;
                    }
                }
                catch (UnauthorizedAccessException)
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
                ApplyScaleToFit(canvasBitmap, pageBitmap);
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
        private void ApplyScaleToFit(WriteableBitmap canvasBitmap, WriteableBitmap pageBitmap)
        {
            double scaleX = (double)canvasBitmap.PixelWidth / pageBitmap.PixelWidth;
            double scaleY = (double)canvasBitmap.PixelHeight / pageBitmap.PixelHeight;
            double targetScaleFactor = (scaleX < scaleY) ? scaleX : scaleY;

            // Scale the LogicalPage image
            WriteableBitmap scaledBitmap = WriteableBitmapExtensions.Resize(pageBitmap,
                (int)(pageBitmap.PixelWidth * targetScaleFactor),
                (int)(pageBitmap.PixelHeight * targetScaleFactor),
                WriteableBitmapExtensions.Interpolation.Bilinear);

            // Compute position in PreviewPage image
            Rect srcRect = new Rect(0, 0, scaledBitmap.PixelWidth, scaledBitmap.PixelHeight);
            Rect destRect = new Rect(
                (canvasBitmap.PixelWidth - scaledBitmap.PixelWidth) / 2,    // Puts the image to the center X
                (canvasBitmap.PixelHeight - scaledBitmap.PixelHeight) / 2,  // Puts the image to the center Y
                scaledBitmap.PixelWidth, scaledBitmap.PixelHeight);
            WriteableBitmapExtensions.Blit(canvasBitmap, destRect, scaledBitmap, srcRect);
        }

        /*
        /// <summary>
        /// Applies border to the image
        /// </summary>
        /// <param name="canvasBitmap">bitmap image</param>
        /// <param name="xOrigin">starting position</param>
        /// <param name="yOrigin">starting position</param>
        /// <param name="width">length</param>
        /// <param name="height">length</param>
        private void ApplyBorder(WriteableBitmap canvasBitmap, int xOrigin, int yOrigin, int width,
            int height)
        {
            WriteableBitmapExtensions.DrawRectangle(canvasBitmap, xOrigin, xOrigin,
                    width, height, Windows.UI.Colors.Black);
        }
         * */

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
            // LogicalPage is cropped using the rectangle
            // and put as in into the paper
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
            bool isPortrait, int impositionOrder)
        {
            // Determine final orientation based on imposition
            bool isPagesPerSheetPerfectSquare = (Math.Sqrt(_pagesPerSheet) % 1) == 0;
            bool isImpositionPortrait = (isPagesPerSheetPerfectSquare) ? isPortrait : !isPortrait;
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
                ApplyScaleToFit(scaledImpositionPageBitmap, impositionPageBitmap);

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
            byte[] pixelBytes = canvasBitmap.ToByteArray();

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
        /// Adds staple wire image into target page image
        /// </summary>
        /// <param name="canvasBitmap">destination image</param>
        /// <param name="stapleType">type indicating number of staple</param>
        /// <param name="finishingSide">position of staple</param>
        /// <returns>task</returns>
        private async Task ApplyStaple(WriteableBitmap canvasBitmap, int stapleType, int finishingSide)
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

            // Determine finishing side
            if (finishingSide == (int)FinishingSide.Top)
            {
                if (stapleType == (int)Staple.OneUpperLeft)
                {
                    ApplyStaple(canvasBitmap, scaledStapleBitmap, 135, false, false);
                }
                else if (stapleType == (int)Staple.OneUpperRight)
                {
                    ApplyStaple(canvasBitmap, scaledStapleBitmap, 45, true, false);
                }
                else if (stapleType == (int)Staple.Two)
                {
                    ApplyStaple(canvasBitmap, scaledStapleBitmap, 0, false, false,
                        canvasBitmap.PixelWidth, true, 0.25);
                    ApplyStaple(canvasBitmap, scaledStapleBitmap, 0, true, false,
                        canvasBitmap.PixelWidth, true, 0.75);
                }
            }
            else if (finishingSide == (int)FinishingSide.Left)
            {
                if (stapleType == (int)Staple.One)
                {
                    ApplyStaple(canvasBitmap, scaledStapleBitmap, 135, false, false);
                }
                else if (stapleType == (int)Staple.Two)
                {
                    ApplyStaple(canvasBitmap, scaledStapleBitmap, 90, false, false,
                        canvasBitmap.PixelHeight, false, 0.25);
                    ApplyStaple(canvasBitmap, scaledStapleBitmap, 90, false, true,
                        canvasBitmap.PixelHeight, false, 0.75);
                }
            }
            else if (finishingSide == (int)FinishingSide.Right)
            {
                if (stapleType == (int)Staple.One)
                {
                    ApplyStaple(canvasBitmap, scaledStapleBitmap, 45, true, false);
                }
                else if (stapleType == (int)Staple.Two)
                {
                    ApplyStaple(canvasBitmap, scaledStapleBitmap, 270, true, false,
                        canvasBitmap.PixelHeight, false, 0.25);
                    ApplyStaple(canvasBitmap, scaledStapleBitmap, 270, true, true,
                        canvasBitmap.PixelHeight, false, 0.75);
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
        private void ApplyStaple(WriteableBitmap canvasBitmap, WriteableBitmap stapleBitmap,
            int angle, bool isXEnd, bool isYEnd)
        {
            ApplyStaple(canvasBitmap, stapleBitmap, angle, isXEnd, isYEnd, 0, false, 0);
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
        private void ApplyStaple(WriteableBitmap canvasBitmap, WriteableBitmap stapleBitmap,
            int angle, bool isXEnd, bool isYEnd, int edgeLength, bool isAlongXAxis,
            double positionPercentage)
        {
            // Rotate
            WriteableBitmap rotatedStapleBitmap = stapleBitmap;
            if (angle > 0)
            {
                rotatedStapleBitmap = WriteableBitmapExtensions.RotateFree(stapleBitmap, angle, false);
            }

            // Put into position
            double marginStaple = PrintSettingConstant.MARGIN_STAPLE * ImageConstant.BASE_DPI;
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
                destYOrigin = canvasBitmap.PixelHeight;
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
            double distanceBetweenHoles =
                PrintSettingConverter.PunchIntToDistanceBetweenHolesConverter.Convert(
                _selectedPrinter.PrintSetting.Punch);
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

        #endregion Apply Print Settings

    }

}

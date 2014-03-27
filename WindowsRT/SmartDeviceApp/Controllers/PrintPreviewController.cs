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

using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Converters;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModel;
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

        private const string FORMAT_PREFIX_PREVIEW_PAGE_IMAGE = "previewpage";
        private const string FORMAT_FILE_NAME_PREVIEW_PAGE_IMAGE =
            FORMAT_PREFIX_PREVIEW_PAGE_IMAGE + "{0:0000}.jpg";
        private const string FILE_NAME_EMPTY_IMAGE = FORMAT_PREFIX_PREVIEW_PAGE_IMAGE + "_empty.jpg";
        private const string FILE_PATH_ASSET_PRINT_SETTINGS_XML = "Assets/printsettings.xml";

        private PrintSettingOptionsViewModel _printSettingOptionsViewModel;
        private PrintSettingList _printSettingList;
        private Printer _selectedPrinter;
        private int _pagesPerSheet = 1;
        private List<LogicalPage> _logicalPages; // LogicalPages in the requested PreviewPage, ordered list
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
        /// Notifies view model of PDF load result and starts query of printer and print settings
        /// </summary>
        /// <returns>Task</returns>
        public async Task Initialize()
        {
            _printSettingOptionsViewModel = new ViewModelLocator().PrintSettingOptionsViewModel;
            await Cleanup(); // Ensure to clean up previouse

            _selectedPrinter = null;
            _previewPages = new Dictionary<int, PreviewPage>();

            // Get print settings if document is successfully loaded
            if (DocumentController.Instance.IsFileLoaded)
            {
                _previewPageTotal = DocumentController.Instance.PageCount;
                Messenger.Default.Send<DocumentMessage>(new DocumentMessage(true,
                    DocumentController.Instance.FileName));

                // Get print settings
                await GetPrintAndPrintSetting();
                await OnPrintSettingUpdated(true);

                LoadPrintSettingsOptions();

                // Send dummy page
                await GenerateEmptyPage();
                await SendEmptyPage();
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
            _selectedPrinter = null;
            await ClearPreviewPageListAndImages();
            _previewPages = null;
        }

        /// <summary>
        /// Resets the generated PreviewPage(s) list and removed the page images from AppData
        /// temporary store.
        /// </summary>
        /// <returns>Task</returns>
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
        public async Task GetPrintAndPrintSetting()
        {
            // Get default printer and print settings from database
            _selectedPrinter = await DatabaseController.Instance.GetDefaultPrinter();
            if (_selectedPrinter.Id < 0 || !_selectedPrinter.IsDefault)
            {
                // No default printer
                _selectedPrinter.PrintSetting = DefaultsUtility.CreateDefaultPrintSetting();
            }
            else
            {
                // Get print settings
                _selectedPrinter.PrintSetting =
                    await DatabaseController.Instance.GetPrintSetting(_selectedPrinter.Id);
                if (_selectedPrinter.PrintSetting == null)
                {
                    _selectedPrinter.PrintSetting = DefaultsUtility.CreateDefaultPrintSetting();
                }
            }
        }

        #endregion Database and Default Values Operations

        #region Print Preview

        /// <summary>
        /// 
        /// </summary>
        /// <param name="printSetting"></param>
        /// <param name="selected"></param>
        public async void UpdatePrintSetting(PrintSetting printSetting, PrintSettingOption selected)
        {
            if (printSetting == null || selected == null)
            {
                return;
            }

            var query = _printSettingList.SelectMany(printSettingGroup => printSettingGroup.PrintSettings)
                .Where(ps => ps.Name == printSetting.Name);
            PrintSetting result = query.First();
            result.Value = selected.Index;

            // Manual check here what is changed
            bool isPreviewPageAffected = false;
            bool isPageCountAffected = false;
            if (result.Name.Equals(PrintSettingConstant.KEY_COLOR_MODE))
            {
                if (_selectedPrinter.PrintSetting.ColorMode != selected.Index)
                {
                    _selectedPrinter.PrintSetting.ColorMode = selected.Index;
                    isPreviewPageAffected = true;
                }
            }
            else if (result.Name.Equals(PrintSettingConstant.KEY_ORIENTATION))
            {
                if (_selectedPrinter.PrintSetting.Orientation != selected.Index)
                {
                    _selectedPrinter.PrintSetting.Orientation = selected.Index;
                    isPreviewPageAffected = true;
                }
            }
            else if (result.Name.Equals(PrintSettingConstant.KEY_DUPLEX))
            {
                if (_selectedPrinter.PrintSetting.Duplex != selected.Index)
                {
                    _selectedPrinter.PrintSetting.Duplex = selected.Index;
                }
            }
            else if (result.Name.Equals(PrintSettingConstant.KEY_PAPER_SIZE))
            {
                if (_selectedPrinter.PrintSetting.PaperSize != selected.Index)
                {
                    _selectedPrinter.PrintSetting.PaperSize = selected.Index;
                    isPreviewPageAffected = true;
                }
            }
            else if (result.Name.Equals(PrintSettingConstant.KEY_PAPER_TYPE))
            {
                if (_selectedPrinter.PrintSetting.PaperType != selected.Index)
                {
                    _selectedPrinter.PrintSetting.PaperType = selected.Index;
                }
            }
            else if (result.Name.Equals(PrintSettingConstant.KEY_INPUT_TRAY))
            {
                if (_selectedPrinter.PrintSetting.InputTray != selected.Index)
                {
                    _selectedPrinter.PrintSetting.InputTray = selected.Index;
                }
            }
            else if (result.Name.Equals(PrintSettingConstant.KEY_IMPOSITION))
            {
                if (_selectedPrinter.PrintSetting.Imposition != selected.Index)
                {
                    _selectedPrinter.PrintSetting.Imposition = selected.Index;
                    isPreviewPageAffected = true;
                    isPageCountAffected = true;
                }
            }
            else if (result.Name.Equals(PrintSettingConstant.KEY_IMPOSITION_ORDER))
            {
                if (_selectedPrinter.PrintSetting.ImpositionOrder != selected.Index)
                {
                    _selectedPrinter.PrintSetting.ImpositionOrder = selected.Index;
                    isPreviewPageAffected = true;
                }
            }
            else if (result.Name.Equals(PrintSettingConstant.KEY_SORT))
            {
                if (_selectedPrinter.PrintSetting.Sort != selected.Index)
                {
                    _selectedPrinter.PrintSetting.Sort = selected.Index;
                }
            }
            else if (result.Name.Equals(PrintSettingConstant.KEY_BOOKLET_FINISHING))
            {
                if (_selectedPrinter.PrintSetting.BookletFinishing != selected.Index)
                {
                    _selectedPrinter.PrintSetting.BookletFinishing = selected.Index;
                }
            }
            else if (result.Name.Equals(PrintSettingConstant.KEY_BOOKLET_LAYOUT))
            {
                if (_selectedPrinter.PrintSetting.BookletLayout != selected.Index)
                {
                    _selectedPrinter.PrintSetting.BookletLayout = selected.Index;
                }
            }
            else if (result.Name.Equals(PrintSettingConstant.KEY_FINISHING_SIDE))
            {
                if (_selectedPrinter.PrintSetting.FinishingSide != selected.Index)
                {
                    _selectedPrinter.PrintSetting.FinishingSide = selected.Index;
                }
            }
            else if (result.Name.Equals(PrintSettingConstant.KEY_STAPLE))
            {
                if (_selectedPrinter.PrintSetting.Staple != selected.Index)
                {
                    _selectedPrinter.PrintSetting.Staple = selected.Index;
                    isPreviewPageAffected = true;
                }
            }
            else if (result.Name.Equals(PrintSettingConstant.KEY_PUNCH))
            {
                if (_selectedPrinter.PrintSetting.Punch != selected.Index)
                {
                    _selectedPrinter.PrintSetting.Punch = selected.Index;
                    isPreviewPageAffected = true;
                }
            }
            else if (result.Name.Equals(PrintSettingConstant.KEY_OUTPUT_TRAY))
            {
                if (_selectedPrinter.PrintSetting.OutputTray != selected.Index)
                {
                    _selectedPrinter.PrintSetting.OutputTray = selected.Index;
                }
            }

            // Generate PreviewPages again
            if (isPreviewPageAffected || isPageCountAffected)
            {
                await OnPrintSettingUpdated(isPageCountAffected);
                await GenerateEmptyPage();
                await SendEmptyPage();
                await LoadPage(_currPreviewPageIndex);
            }
        }

        /// <summary>
        /// Checks for view related print setting and notifies view model
        /// </summary>
        /// <param name="sendPageCountInfo">flag when page count is needed to update in view model</param>
        /// <returns>task</returns>
        private async Task OnPrintSettingUpdated(bool sendPageCountInfo)
        {
            // Send UI related items
            if (_selectedPrinter.PrintSetting.Booklet)
            {
                _pageViewMode = PageViewMode.TwoPageView;
            }
            else
            {
                _pageViewMode = PageViewMode.SinglePageView;
            }

            _pagesPerSheet = PrintSettingConverter.ImpositionIntToNumberOfPagesConverter
                .Convert(_selectedPrinter.PrintSetting.Imposition);
            if (sendPageCountInfo)
            {
                _previewPageTotal = (uint)Math.Ceiling(
                    (decimal)DocumentController.Instance.PageCount / _pagesPerSheet);
                Messenger.Default.Send<PreviewInfoMessage>(new PreviewInfoMessage(_previewPageTotal,
                    _pageViewMode));
            }

            // Clean-up generated PreviewPages
            await ClearPreviewPageListAndImages();
        }

        /// <summary>
        /// Generates an empty page for the initial display of Preview Area.
        /// This page will be displayed while the PreviePage(s) are being generated.
        /// </summary>
        /// <returns>task</returns>
        private async Task GenerateEmptyPage()
        {
            // Create a blank white page for Preview (temporary)
            Size paperSize = PrintSettingConverter.PaperSizeIntToSizeConverter.Convert(
                _selectedPrinter.PrintSetting.PaperSize);
            bool isPortrait = PrintSettingConverter.OrientationIntToBoolConverter.Convert(
                        _selectedPrinter.PrintSetting.Orientation);

            // Override selected orientation based on imposition value
            if (_pagesPerSheet == 4)
            {
                isPortrait = true;
            } else if (_pagesPerSheet == 2)
            {
                isPortrait = false;
            }
            WriteableBitmap emptyBitmap = ApplyPaperSizeAndOrientation(paperSize,
                        isPortrait);

            // Save PreviewPage into AppData temporary store
            StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;
            StorageFile whitePageImage = await tempFolder.CreateFileAsync(FILE_NAME_EMPTY_IMAGE,
                CreationCollisionOption.ReplaceExisting);
            using (var destinationStream =
                await whitePageImage.OpenAsync(FileAccessMode.ReadWrite))
            {
                BitmapEncoder newEncoder = await BitmapEncoder.CreateAsync(
                    BitmapEncoder.JpegEncoderId, destinationStream);
                byte[] pixels = WriteableBitmapExtensions.ToByteArray(emptyBitmap);
                newEncoder.SetPixelData(BitmapPixelFormat.Bgra8, BitmapAlphaMode.Ignore,
                    (uint)emptyBitmap.PixelWidth, (uint)emptyBitmap.PixelHeight,
                    ImageConstant.BASE_DPI, ImageConstant.BASE_DPI, pixels);
                await newEncoder.FlushAsync();
            }
        }

        private async Task SendEmptyPage()
        {
            // Get existing file from AppData temporary store
            StorageFile whitePageImage = await StorageFileUtility.GetExistingFile(FILE_NAME_EMPTY_IMAGE,
                ApplicationData.Current.TemporaryFolder);
            if (whitePageImage != null)
            {
                using (var sourceStream = await whitePageImage.OpenReadAsync())
                {
                    // Open the bitmap
                    BitmapImage bitmapImage = new BitmapImage(new Uri(whitePageImage.Path));
                    BitmapDecoder decoder = await BitmapDecoder.CreateAsync(sourceStream);

                    // Create message and send
                    Messenger.Default.Send<PreviewPageImage>(new PreviewPageImage(bitmapImage,
                        new Size(decoder.PixelWidth, decoder.PixelHeight)));
                }
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

                    // Create message and send
                    Messenger.Default.Send<PreviewPageImage>(new PreviewPageImage(bitmapImage,
                        previewPage.ActualSize));

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

            await SendEmptyPage();

            _logicalPages = await getLogicalPagesTask;
            await ApplyPrintSettings(targetPreviewPageIndex);
        }

        #endregion Preview Page Navigation

        #region Apply Print Settings

        /// <summary>
        /// Applies print settings to LogicalPage(s) then creates a PreviewPage
        /// </summary>
        /// <param name="previewPageIndex">target page</param>
        /// <returns>task</returns>
        private async Task ApplyPrintSettings(int previewPageIndex)
        {
            if (_logicalPages != null && _logicalPages.Count > 0)
            {
                StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;

                WriteableBitmap finalBitmap = new WriteableBitmap(1, 1); // Size does not matter yet
                List<WriteableBitmap> pageImages = new List<WriteableBitmap>(); // Ordered list

                Size paperSize = PrintSettingConverter.PaperSizeIntToSizeConverter.Convert(
                    _selectedPrinter.PrintSetting.PaperSize);

                // Loop to each LogicalPage(s) to selected paper size and orientation
                foreach (LogicalPage logicalPage in _logicalPages)
                {
                    // Open PreviewPage image from AppData temporary store
                    string pageImageFileName = logicalPage.Name;
                    StorageFile jpgFile = await tempFolder.GetFileAsync(pageImageFileName);
                    using (IRandomAccessStream raStream = await jpgFile.OpenReadAsync())
                    {
                        // Put LogicalPage image to a bitmap
                        WriteableBitmap pageBitmap = await WriteableBitmapExtensions.FromStream(
                            null, raStream);

                        bool isPortrait =
                            PrintSettingConverter.OrientationIntToBoolConverter.Convert(
                            _selectedPrinter.PrintSetting.Orientation);
                        WriteableBitmap canvasBitmap = ApplyPaperSizeAndOrientation(paperSize,
                            isPortrait);

                        ApplyPageImageToPaper(_selectedPrinter.PrintSetting.ScaleToFit,
                            canvasBitmap, pageBitmap);

                        pageImages.Add(canvasBitmap);
                    }
                }

                // Check imposition value and generated bitmaps
                if (_pagesPerSheet > 1 && pageImages.Count > 1)
                {
                    finalBitmap = ApplyImposition(paperSize, pageImages);
                }
                else if (pageImages.Count == 1)
                {
                    finalBitmap = pageImages[0];
                }

                // Check color mode value
                if (_selectedPrinter.PrintSetting.ColorMode.Equals((int)ColorMode.Mono))
                {
                    byte[] pixelBytes = finalBitmap.ToByteArray();
                    pixelBytes = ApplyMonochrome(pixelBytes);

                    // Write out to the stream
                    WriteableBitmapExtensions.FromByteArray(finalBitmap, pixelBytes);
                }

                try
                {
                    // Save PreviewPage into AppData temporary store
                    StorageFile tempPageImage = await tempFolder.CreateFileAsync(
                        String.Format(FORMAT_FILE_NAME_PREVIEW_PAGE_IMAGE, previewPageIndex),
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

                    // Open the bitmap
                    BitmapImage bitmapImage = new BitmapImage(new Uri(tempPageImage.Path));

                    // Create message and send
                    Messenger.Default.Send<PreviewPageImage>(new PreviewPageImage(bitmapImage,
                        previewPage.ActualSize));
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
            // Get paper size and apply DPI
            double length1 = paperSize.Width * ImageConstant.BASE_DPI;
            double length2 = paperSize.Height * ImageConstant.BASE_DPI;

            Size pageImageSize;
            // Check orientation
            if (isPortrait)
            {
                pageImageSize = new Size(length1, length2);
            }
            else
            {
                pageImageSize = new Size(length2, length1);
            }

            // Create canvas based on paper size and orientation
            WriteableBitmap canvasBitmap = new WriteableBitmap((int)pageImageSize.Width,
                (int)pageImageSize.Height);
            // Fill all white
            WriteableBitmapExtensions.FillRectangle(canvasBitmap, 0, 0, (int)pageImageSize.Width,
                (int)pageImageSize.Height, Windows.UI.Colors.White);

            return canvasBitmap;
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
        /// <param name="drawBorder">flag to draw border on fitted page image</param>
        private void ApplyScaleToFit(WriteableBitmap canvasBitmap, WriteableBitmap pageBitmap,
            bool drawBorder)
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
            if (drawBorder)
            {
                WriteableBitmapExtensions.DrawRectangle(scaledBitmap, 0, 0,
                    (int)scaledBitmap.PixelWidth, (int)scaledBitmap.PixelHeight,
                        Windows.UI.Colors.Black);
            }
            WriteableBitmapExtensions.Blit(canvasBitmap, destRect, scaledBitmap, srcRect);
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
        /// <returns>page image with applied imposition value
        /// Final output page image is
        /// * portrait when imposition value is 4-up
        /// * otherwise landscape</returns>
        private WriteableBitmap ApplyImposition(Size paperSize, List<WriteableBitmap> pageImages)
        {
            // Create target page image based on imposition
            bool isPortrait = _pagesPerSheet == 4; // Portrait if 4-Up
            WriteableBitmap canvasBitmap = ApplyPaperSizeAndOrientation(paperSize, isPortrait);

            // Compute number of pages per row and column
            int pagesPerRow = (int)Math.Sqrt(_pagesPerSheet);
            int pagesPerColumn = _pagesPerSheet / pagesPerRow;

            // Compute page area size and margin
            double marginPaper = PrintSettingConstant.MARGIN_PAPER * ImageConstant.BASE_DPI;
            double marginBetweenPages = PrintSettingConstant.MARGIN_BETWEEN_PAGES * ImageConstant.BASE_DPI;
            if (marginPaper == 0)
            {
                marginPaper = marginBetweenPages;
            }
            Size impositionPageAreaSize = ComputePageArea(canvasBitmap.PixelWidth,
                canvasBitmap.PixelHeight, pagesPerRow, pagesPerColumn,
                marginBetweenPages, marginPaper);

            // Set initial positions
            double initialOffsetX = 0;
            double initialOffsetY = 0;
            switch (_selectedPrinter.PrintSetting.ImpositionOrder)
            {
                case (int)ImpositionOrder.TwoUpLeftToRight:
                case (int)ImpositionOrder.FourUpUpperLeftToRight:
                case (int)ImpositionOrder.FourUpUpperLeftToBottom:
                    initialOffsetX = 0;
                    break;
                case (int)ImpositionOrder.FourUpUpperRightToBottom:
                case (int)ImpositionOrder.FourUpUpperRightToLeft:
                case (int)ImpositionOrder.TwoUpRightToLeft:
                default:
                    initialOffsetX = (marginBetweenPages * (pagesPerColumn - 1)) +
                        (impositionPageAreaSize.Width * (pagesPerColumn - 1));
                    break;
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
                ApplyScaleToFit(scaledImpositionPageBitmap, impositionPageBitmap, true); // Apply border

                // Put imposition page image to target page image
                Rect destRect = new Rect(x, y, scaledImpositionPageBitmap.PixelWidth,
                    scaledImpositionPageBitmap.PixelHeight);
                Rect srcRect = new Rect(0, 0, scaledImpositionPageBitmap.PixelWidth,
                    scaledImpositionPageBitmap.PixelHeight);
                WriteableBitmapExtensions.Blit(canvasBitmap, destRect, scaledImpositionPageBitmap,
                    srcRect);

                // Update offset/postion based on direction
                switch (_selectedPrinter.PrintSetting.ImpositionOrder)
                {
                    case (int)ImpositionOrder.TwoUpLeftToRight:
                    case (int)ImpositionOrder.FourUpUpperLeftToRight:
                        pageImageOffsetX += marginBetweenPages + impositionPageAreaSize.Width;
                        if (((impositionPageIndex + 1) % pagesPerColumn) == 0)
                        {
                            pageImageOffsetX = initialOffsetX;
                            pageImageOffsetY += marginBetweenPages + impositionPageAreaSize.Height;
                        }
                        break;
                    case (int)ImpositionOrder.FourUpUpperLeftToBottom:
                        pageImageOffsetY += marginBetweenPages + impositionPageAreaSize.Height;
                        if (((impositionPageIndex + 1) % pagesPerRow) == 0)
                        {
                            pageImageOffsetY = initialOffsetY;
                            pageImageOffsetX += marginBetweenPages + impositionPageAreaSize.Width;
                        }
                        break;
                    case (int)ImpositionOrder.FourUpUpperRightToBottom:
                        pageImageOffsetY += marginBetweenPages + impositionPageAreaSize.Height;
                        if (((impositionPageIndex + 1) % pagesPerRow) == 0)
                        {
                            pageImageOffsetY = initialOffsetY;
                            pageImageOffsetX -= marginBetweenPages + impositionPageAreaSize.Width;
                        }
                        break;
                    case (int)ImpositionOrder.FourUpUpperRightToLeft:
                    case (int)ImpositionOrder.TwoUpRightToLeft:
                    default:
                        pageImageOffsetX -= marginBetweenPages + impositionPageAreaSize.Width;
                        if (((impositionPageIndex + 1) % pagesPerColumn) == 0)
                        {
                            pageImageOffsetX = initialOffsetX;
                            pageImageOffsetY += marginBetweenPages + impositionPageAreaSize.Height;
                        }
                        break;
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
        private Size ComputePageArea(int width, int height, int numRows, int numColumns,
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
        /// <param name="pixelBytes">pixel bytes</param>
        /// <returns>pixel bytes with altered color to monochrome</returns>
        private byte[] ApplyMonochrome(byte[] pixelBytes)
        {
            byte[] newPixelBytes = new byte[pixelBytes.Length];

            // From http://social.msdn.microsoft.com/Forums/windowsapps/en-US/5ff10c14-51d4-4760-afe6-091624adc532/sample-code-for-making-a-bitmapimage-grayscale
            for (int i = 0; i < pixelBytes.Length; i += 4)
            {
                double b = (double)pixelBytes[i] / 255.0;
                double g = (double)pixelBytes[i + 1] / 255.0;
                double r = (double)pixelBytes[i + 2] / 255.0;

                byte a = pixelBytes[i + 3];

                // Altered color factor to be equal
                double e = (0.33 * r + 0.33 * g + 0.33 * b) * 255;
                byte f = Convert.ToByte(e);

                newPixelBytes[i] = f;
                newPixelBytes[i + 1] = f;
                newPixelBytes[i + 2] = f;
                newPixelBytes[i + 3] = a;
            }

            return newPixelBytes;
        }

        #endregion Apply Print Settings

        #region Print Settings

        private void LoadPrintSettingsOptions()
        {
            PagePrintSettingToValueConverter valueConverter = new PagePrintSettingToValueConverter();

            string xmlPath = Path.Combine(Package.Current.InstalledLocation.Path,
                FILE_PATH_ASSET_PRINT_SETTINGS_XML);
            XDocument data = XDocument.Load(xmlPath);

            var printSettingsData = from groupData in data.Descendants("group")
                                    select new PrintSettingGroup
                                    {
                                        Name = (string)groupData.Attribute("name"),
                                        Text = (string)groupData.Attribute("text"),
                                        PrintSettings =
                                        (
                                            from settingData in groupData.Elements("setting")
                                            select new PrintSetting
                                            {
                                                Name = (string)settingData.Attribute("name"),
                                                Text = (string)settingData.Attribute("text"),
                                                Icon = (string)settingData.Attribute("icon"),
                                                Type = (PrintSettingType)Enum.Parse(typeof(PrintSettingType),
                                                    (string)settingData.Attribute("type")),
                                                Value = valueConverter.Convert(_selectedPrinter.PrintSetting,
                                                    null, (string)settingData.Attribute("name"), null),
                                                Options =
                                                (
                                                    from optionData in settingData.Elements("option")
                                                    select new PrintSettingOption
                                                    {
                                                        Text = (string)optionData.Value,
                                                        Index = optionData.ElementsBeforeSelf().Count()
                                                    }).ToList<PrintSettingOption>()
                                            }).ToList<PrintSetting>()
                                    };
            
            // Construct the PrintSettingList
            _printSettingList = new PrintSettingList();
            var tempList = printSettingsData.Cast<PrintSettingGroup>().ToList<PrintSettingGroup>();
            foreach (PrintSettingGroup group in tempList)
            {
                _printSettingList.Add(group);
            }

            // Send to view model
            var printSettingsViewModel = new ViewModelLocator().PrintSettingsViewModel;
            printSettingsViewModel.PrintSettingsList = _printSettingList;
        }

        #endregion Print Settings

    }

    public sealed class DocumentMessage
    {
        public bool IsLoaded { get; private set; }
        public string DocTitle { get; private set; }

        public DocumentMessage(bool isPdfLoaded, string docTitle)
        {
            IsLoaded = isPdfLoaded;
            DocTitle = docTitle;
        }
    }

    public sealed class PreviewInfoMessage
    {
        public uint PageTotal { get; private set; }
        public PageViewMode PageViewMode { get; private set; }

        public PreviewInfoMessage(uint pageTotal, PageViewMode pageViewMode)
        {
            PageTotal = pageTotal;
            PageViewMode = pageViewMode;
        }
    }

    public sealed class PreviewPageImage
    {
        public BitmapImage PageImage { get; private set; }
        public Size ActualSize { get; private set; }

        public PreviewPageImage(BitmapImage pageImage, Size actualSize)
        {
            PageImage = pageImage;
            ActualSize = actualSize;
        }
    }

}

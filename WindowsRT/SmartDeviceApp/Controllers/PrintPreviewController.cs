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
using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Graphics.Display;
using Windows.Graphics.Imaging;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.UI.Xaml.Media.Imaging;

namespace SmartDeviceApp.Controllers
{
    public sealed class PrintPreviewController
    {
        static readonly PrintPreviewController _instance = new PrintPreviewController();

        private const string FORMAT_PREVIEW_PAGE_IMAGE_PREFIX = "previewpage";
        private const string FORMAT_PREVIEW_PAGE_IMAGE_FILENAME =
            FORMAT_PREVIEW_PAGE_IMAGE_PREFIX + "{0:0000}.jpg";

        private Printer _selectedPrinter;
        private int _pagesPerSheet = 1;
        private List<LogicalPage> _logicalPages; // LogicalPages in the requested PreviewPage, ordered list
        private Dictionary<int, PreviewPage> _previewPages; // Generated PreviewPages from the start
        private uint _previewPageTotal;
        private PageViewMode _pageViewMode;
        private int _currPreviewPageIndex;
        private int _logicalDpi = (int)DisplayInformation.GetForCurrentView().LogicalDpi;

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
                await OnPrintSettingUpdated();
            }
            else
            {
                // Notify error
            }
        }

        /// <summary>
        /// Clean up
        /// </summary>
        public async Task Cleanup()
        {
            _selectedPrinter = null;
            if (_previewPages != null)
            {
                _previewPages.Clear();
            }
            _previewPages = null;

            await StorageFileUtility.DeleteFiles(FORMAT_PREVIEW_PAGE_IMAGE_PREFIX,
                ApplicationData.Current.TemporaryFolder);
        }

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

        /// <summary>
        /// Checks for view related print setting and notifies view model
        /// </summary>
        private async Task OnPrintSettingUpdated()
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

            _previewPageTotal = (uint)Math.Ceiling(
                (decimal)DocumentController.Instance.PageCount / _pagesPerSheet);
            Messenger.Default.Send<PreviewInfoMessage>(new PreviewInfoMessage(_previewPageTotal,
                _pageViewMode));

            // Clean-up generated PreviewPages
            await StorageFileUtility.DeleteFiles(FORMAT_PREVIEW_PAGE_IMAGE_PREFIX,
                ApplicationData.Current.TemporaryFolder);
        }

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
            _logicalPages =
                await DocumentController.Instance.GenerateLogicalPages(targetPreviewPageIndex,
                    _pagesPerSheet);
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
            if (_logicalPages.Count > 0)
            {
                StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;

                WriteableBitmap finalBitmap = new WriteableBitmap(1, 1);
                List<WriteableBitmap> pageImages = new List<WriteableBitmap>(); // Ordered list

                Size paperSize = PrintSettingConverter.PaperSizeIntToSizeConverter.Convert(
                    _selectedPrinter.PrintSetting.PaperSize);

                // Loop to each LogicalPage(s) here
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

                // TODO: Imposition and Imposition Order
                #region ApplyImposition
                /*
                // Check Imposition value and generated bitmaps
                if (_pagesPerSheet > 1 && pageImages.Count > 1 && (_pagesPerSheet == pageImages.Count))
                {
                    // Apply imposition based on number and order

                    // landscape if 2-in-1
                    // portrait if 4-in-1
                    bool isPortrait = _pagesPerSheet == 4;
                    WriteableBitmap draftBitmap = ApplyPaperSizeAndOrientation(paperSize, isPortrait);

                    // Row and column counters
                    // Compute number of pages per row and column
                    int pagesPerRow = (int)Math.Sqrt(_pagesPerSheet);
                    int pagesPerColumn = _pagesPerSheet / pagesPerRow;

                    // Compute page area size and margin
                    double prevImageWidth = 0;
                    double prevImageHeight = 0;
                    double marginPaper = PrintSettingConstant.MARGIN_PAPER * _logicalDpi;
                    double marginBetweenPages = PrintSettingConstant.MARGIN_BETWEEN_PAGES * _logicalDpi;
                    Size pageAreaSize = ComputePageArea(pageImages[0].PixelWidth,
                        pageImages[0].PixelHeight, pagesPerRow, pagesPerColumn,
                        marginBetweenPages, marginPaper);

                    // Compute center of page area
                    double widthDiff = pageAreaSize.Width - pageImages[0].PixelWidth;
                    double heightDiff = pageAreaSize.Height - pageImages[0].PixelHeight;
                    double xOffset = 0;
                    double yOffset = 0;
                    if (widthDiff > 0)
                    {
                        xOffset = widthDiff / 2;
                    }
                    if (heightDiff > 0)
                    {
                        yOffset = heightDiff / 2;
                    }

                    // Loop each pages
                    int impositionIndex = 0;
                    foreach (WriteableBitmap pageBitmap in pageImages)
                    {
                        // Put page in center of page area
                        double x = 0;
                        double y = 0;
                        x = marginPaper + prevImageWidth + xOffset;
                        y = marginPaper + prevImageHeight + yOffset;

                        // Draw border in page if needed
                        {
                            pageBitmap.DrawRectangle(0, 0, pageBitmap.PixelWidth,
                                pageBitmap.PixelHeight, Windows.UI.Colors.Black);
                        }

                        // Scale image and put into canvas
                        Rect destRect = new Rect(x, y, x + pageAreaSize.Width, y + pageAreaSize.Height);
                        Rect srcRect = new Rect(0, 0, pageBitmap.PixelWidth, pageBitmap.PixelHeight);
                        draftBitmap.Blit(destRect, pageBitmap, srcRect);

                        // Position
                        // Update rectangles/offset
                        switch(_selectedPrinter.PrintSetting.ImpositionOrder)
                        {
                            case (int)ImpositionOrder.LeftToRight:
                            case (int)ImpositionOrder.TopLeftToRight:
                                prevImageWidth += marginBetweenPages + pageAreaSize.Width;
                                if (((impositionIndex + 1) % pagesPerColumn) == 0)
                                {
                                    prevImageWidth = 0;
                                    prevImageHeight += marginBetweenPages + pageAreaSize.Height;
                                }
                                break;
                            case (int)ImpositionOrder.TopLeftToBottom:
                                prevImageHeight += marginBetweenPages + pageAreaSize.Height;
                                if (((impositionIndex + 1) % pagesPerRow) == 0)
                                {
                                    prevImageHeight = 0;
                                    prevImageWidth += marginBetweenPages + pageAreaSize.Width;
                                }
                                break;
                            case (int)ImpositionOrder.TopRightToBottom:
                                break;
                            case (int)ImpositionOrder.TopRightToLeft:
                                break;
                            case (int)ImpositionOrder.RightToLeft:
                            default:
                                break;

                        }

                        ++impositionIndex;
                    }

                }
                else if (pageImages.Count == 1)
                    */
                #endregion ApplyImposition
                {
                    finalBitmap = pageImages[0];
                }

                byte[] pixelBytes = finalBitmap.ToByteArray();

                if (_selectedPrinter.PrintSetting.ColorMode.Equals((int)ColorMode.Mono))
                {
                    pixelBytes = ApplyMonochrome(pixelBytes);

                    // Write out to the stream
                    WriteableBitmapExtensions.FromByteArray(finalBitmap, pixelBytes);
                }

                // Save PreviewPage into AppData temporary store
                StorageFile tempPageImage = await tempFolder.CreateFileAsync(
                    String.Format(FORMAT_PREVIEW_PAGE_IMAGE_FILENAME, previewPageIndex),
                    CreationCollisionOption.ReplaceExisting);
                using (var destinationStream =
                    await tempPageImage.OpenAsync(FileAccessMode.ReadWrite))
                {
                    BitmapEncoder newEncoder = await BitmapEncoder.CreateAsync(
                        BitmapEncoder.JpegEncoderId, destinationStream);
                    byte[] pixels = WriteableBitmapExtensions.ToByteArray(finalBitmap);
                    newEncoder.SetPixelData(BitmapPixelFormat.Bgra8, BitmapAlphaMode.Ignore,
                        (uint)finalBitmap.PixelWidth, (uint)finalBitmap.PixelHeight, _logicalDpi,
                        _logicalDpi, pixels);
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
            double length1 = paperSize.Width * _logicalDpi;
            double length2 = paperSize.Height * _logicalDpi;

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
                // TODO: Still has issue with scaling and center alignment
                ApplyScaleToFit(canvasBitmap, pageBitmap);
            }
            else
            {
                ApplyImageToPaper(canvasBitmap, pageBitmap);
            }
        }

        // TODO: Still has issue with scaling and center alignment
        /// <summary>
        /// Scales the LogicalPage image into the PreviewPage image
        /// </summary>
        /// <param name="canvasBitmap">PreviewPage image</param>
        /// <param name="pageBitmap">LogicalPage image</param>
        private void ApplyScaleToFit(WriteableBitmap canvasBitmap, WriteableBitmap pageBitmap)
        {
            // Compute the ratio for scaling
            float canvasRatio = (float)canvasBitmap.PixelWidth / canvasBitmap.PixelHeight;
            float pageRatio = (float)pageBitmap.PixelWidth / pageBitmap.PixelHeight;
            float newRatio = 0f;
            if (pageRatio < canvasRatio)
            {
                newRatio = (float)canvasBitmap.PixelHeight / pageBitmap.PixelHeight;
            }
            else
            {
                newRatio = (float)pageBitmap.PixelHeight / canvasBitmap.PixelHeight;
            }

            // Compute scaled size
            int scaledWidth = (int)(pageBitmap.PixelWidth * newRatio);
            int scaledHeight = (int)(pageBitmap.PixelHeight * newRatio);

            // Position
            Rect destRect = new Rect(
                (canvasBitmap.PixelWidth - scaledWidth) / 2,   // Puts the image to the center X
                (canvasBitmap.PixelHeight - scaledHeight) / 2, // Puts the image to the center Y
                scaledWidth, scaledHeight);
            Rect srcRect = new Rect(0, 0, pageBitmap.PixelWidth, pageBitmap.PixelHeight);

            // Place image into paper
            WriteableBitmapExtensions.Blit(canvasBitmap, destRect, pageBitmap, srcRect);
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

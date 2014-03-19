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
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Converters;
using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;
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

        private const string TEMP_IMAGE_NAME = "tempPage.jpg";

        private Printer _selectedPrinter;
        private int _pagesPerSheet = 1;
        private List<LogicalPage> _logicalPages;
        private uint _previewPageTotal;
        private PageViewMode _pageViewMode;

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
            _selectedPrinter = null;

            // Get print settings if document is successfully loaded
            if (DocumentController.Instance.IsFileLoaded)
            {
                _previewPageTotal = DocumentController.Instance.PageCount;
                Messenger.Default.Send<DocumentMessage>(new DocumentMessage(true, DocumentController.Instance.FileName) );

                // Get print settings
                await GetPrintAndPrintSetting();
                OnPrintSettingUpdated();
            }
            else
            {
                // Notify error
            }
        }

        /// <summary>
        /// Clean up
        /// </summary>
        public void Cleanup()
        {
            _selectedPrinter = null;
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
        private void OnPrintSettingUpdated()
        {
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
        }

        #region Preview Page Navigation

        /// <summary>
        /// Requests for LogicalPages and then applies print setting for the target page only
        /// </summary>
        /// <param name="targetPreviewPageIndex">requested page index</param>
        /// <returns>task</returns>
        public async Task LoadPage(int targetPreviewPageIndex)
        {
            int targetLogicalPageIndex = targetPreviewPageIndex * _pagesPerSheet;
            _logicalPages =
                await DocumentController.Instance.GenerateLogicalPages(targetPreviewPageIndex,
                    _pagesPerSheet);
            await ApplyPrintSettings();
        }

        /*
        /// <summary>
        /// Requests for LogicalPages near the next page
        /// and then applies print settings for the target pages only
        /// </summary>
        /// <param name="targetPreviewPageIndex"></param>
        /// <returns>task</returns>
        public async Task NextPage(int targetPreviewPageIndex)
        {
            int targetLogicalPageIndex = _currLogicalPageIndex + _pagesPerSheet;
            if (targetPreviewPageIndex == targetLogicalPageIndex)
            {
                _logicalPages = await DocumentController.Instance.GenerateLogicalPages(
                    targetLogicalPageIndex, _pagesPerSheet);
            }
            await ApplyPrintSettings();
        }

        public async Task PreviousPage(int targetPreviewPageIndex)
        {
            int targetLogicalPageIndex = _currLogicalPageIndex - _pagesPerSheet;
            if (targetPreviewPageIndex == targetLogicalPageIndex)
            {
                _logicalPages = await DocumentController.Instance.GenerateLogicalPages(
                    targetLogicalPageIndex, _pagesPerSheet);
            }
            await ApplyPrintSettings();
        }
         * */

        #endregion Preview Page Navigation

        #region Apply Print Settings

        /// <summary>
        /// Applies print settings to LogicalPage(s) then creates a PreviewPage (BitmapImage)
        /// </summary>
        /// <returns>task</returns>
        private async Task ApplyPrintSettings()
        {
            BitmapImage pageImage = new BitmapImage();

            string pageImageFileName = _logicalPages[0].Name; // TODO: Update for Imposition
            StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;
            StorageFile jpgFile = await tempFolder.GetFileAsync(pageImageFileName);
            using (IRandomAccessStream raStream = await jpgFile.OpenReadAsync())
            {
                BitmapDecoder decoder = await BitmapDecoder.CreateAsync(raStream);

                // Create a new stream and encoder for the new image
                InMemoryRandomAccessStream imrasStream = new InMemoryRandomAccessStream();
                BitmapEncoder encoder = await BitmapEncoder.CreateForTranscodingAsync(imrasStream, decoder);

                // TODO: bitmap manipulations

                /*
                // convert the entire bitmap to a 100px by 100px bitmap
                enc.BitmapTransform.ScaledHeight = 100;
                enc.BitmapTransform.ScaledWidth = 100;

                BitmapBounds bounds = new BitmapBounds();
                bounds.Height = 50;
                bounds.Width = 50;
                bounds.X = 50;
                bounds.Y = 50;
                enc.BitmapTransform.Bounds = bounds;
                */

                // Apply Paper Size

                // Apply Scale to Fit

                // Apply Imposition and Imposition Order

                // Apply Staple

                // Apply Punch

                PixelDataProvider pixelData = await decoder.GetPixelDataAsync(
                    decoder.BitmapPixelFormat,
                    decoder.BitmapAlphaMode,
                    new BitmapTransform(),
                    ExifOrientationMode.IgnoreExifOrientation,
                    ColorManagementMode.DoNotColorManage);

                byte[] pixelBytes = pixelData.DetachPixelData();

                if (_selectedPrinter.PrintSetting.ColorMode.Equals((int)ColorMode.Mono))
                {
                    pixelBytes = ApplyMonochrome(pixelBytes);

                    // Write out to the stream
                    encoder.SetPixelData(decoder.BitmapPixelFormat, decoder.BitmapAlphaMode,
                        decoder.PixelWidth, decoder.PixelHeight, decoder.DpiX, decoder.DpiY,
                        pixelBytes);
                    await encoder.FlushAsync();
                }

                // Save output into AppData temporary store
                StorageFile tempPageImage = await tempFolder.CreateFileAsync(TEMP_IMAGE_NAME,
                    CreationCollisionOption.ReplaceExisting);
                using (var destinationStream = await tempPageImage.OpenAsync(FileAccessMode.ReadWrite))
                {
                    BitmapEncoder newEncoder = await BitmapEncoder.CreateAsync(BitmapEncoder.JpegEncoderId, destinationStream);
                    newEncoder.SetPixelData(decoder.BitmapPixelFormat, decoder.BitmapAlphaMode,
                        decoder.PixelWidth, decoder.PixelHeight, decoder.DpiX, decoder.DpiY, pixelBytes);
                    await newEncoder.FlushAsync();
                }

                // Open the bitmap
                BitmapImage bitmapImage = new BitmapImage(new Uri(tempPageImage.Path));

                // Create message and send
                Messenger.Default.Send<PreviewPage>(new PreviewPage(bitmapImage, _logicalPages[0].Size));  // TODO: Update size
            }
        }

        /// <summary>
        /// Changes the bitmap to grayscale
        /// </summary>
        /// <param name="pixelBytes">pixel bytes</param>
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

}

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

using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media.Imaging;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Graphics.Imaging;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Converters;

namespace SmartDeviceApp.Controllers
{
    public sealed class PrintPreviewController
    {
        static readonly PrintPreviewController _instance = new PrintPreviewController();

        private bool _isInitPageLoaded;
        private bool _isPrintSettingsAvailable;

        private int _currLogicalPageIndex;
        private int _currPreviewPageIndex;

        private Printer _selectedPrinter;
        private int _requestPageCount;
        private int _pagesPerSheet;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static PrintPreviewController() { }

        private PrintPreviewController() { }

        public static PrintPreviewController Instance
        {
            get { return _instance; }
        }

        public void Initialize()
        {
            // TODO: Not sure what to do

            _isInitPageLoaded = false;
            _isPrintSettingsAvailable = false;
            _currLogicalPageIndex = 0;
            _currPreviewPageIndex = 0;
            _selectedPrinter = null;
            _requestPageCount = 0;
        }

        public void Cleanup()
        {
            // TODO: Unregister stuff???

            _isInitPageLoaded = false;
            _isPrintSettingsAvailable = false;
            _currLogicalPageIndex = 0;
            _currPreviewPageIndex = 0;
            _selectedPrinter = null;
            _requestPageCount = 0;
        }

        /// <summary>
        /// Handles load document processing result
        /// </summary>
        /// <param name="message">message containing the result</param>
        public async void OnLoadDocumentFinished(bool isPdfLoaded)
        {
            if (isPdfLoaded)
            {
                _isInitPageLoaded = true;

                // Get default printer and print settings from database
                _selectedPrinter = await DatabaseController.Instance.GetDefaultPrinter();
                if (_selectedPrinter.Id < 0 || !_selectedPrinter.IsDefault)
                {
                    // No default printer
                    _selectedPrinter.PrintSetting = DefaultsUtility.createDefaultPrintSetting();
                    OnReceivePrintSettings();
                }
                else
                {
                    // Get print settings
                    _selectedPrinter.PrintSetting =
                       await DatabaseController.Instance.GetPrintSetting(_selectedPrinter.Id);
                    if (_selectedPrinter.PrintSetting == null)
                    {
                        _selectedPrinter.PrintSetting = DefaultsUtility.createDefaultPrintSetting();
                    }
                    OnReceivePrintSettings();
                }
            }
            else
            {
                // Show error message to PrintPreviewPage
            }
        }

        /// <summary>
        /// Handle print settings
        /// </summary>
        public void OnReceivePrintSettings()
        {
            _isPrintSettingsAvailable = true;
            _requestPageCount = 0;

            // Save print settings to cache (internal variable only)
            // Check pagination settings then request pages
            _pagesPerSheet = PrintSettingConverter.ImpositionIntToNumberOfPagesConverter
                .Convert(_selectedPrinter.PrintSetting.Imposition);
            for (int i = 0; i < _pagesPerSheet; ++i)
            {
                DocumentController.Instance.GenerateLogicalPages(_currLogicalPageIndex, true);
                _currLogicalPageIndex += i;
            }
        }

        /// <summary>
        /// Handles load document processing result
        /// </summary>
        /// <param name="message">message containing the result</param>
        public async void OnReceiveLogicalPage(LogicalPage logicalPage)
        {
            if (logicalPage == null)
            {
                return;
            }

            ++_requestPageCount;
            _currLogicalPageIndex = (int)logicalPage.PageIndex;

            if (_requestPageCount == _pagesPerSheet)
            {
                await ApplyPrintSettings(logicalPage);
            }
        }

        #region Preview Page Navigation

        public void NextPage()
        {
            // TODO: Consider pagination
            DocumentController.Instance.GenerateLogicalPages(_currLogicalPageIndex + 1, true);
        }

        public void PreviousPage()
        {
            // TODO: Consider pagination
            DocumentController.Instance.GenerateLogicalPages(_currLogicalPageIndex - 1, true);
        }

        #endregion Preview Page Navigation

        #region Apply Print Settings

        /// <summary>
        /// Applies print settings to logical page then creates a preview page
        /// </summary>
        /// <param name="pageIndex">page index</param>
        /// <returns>task with a bitmap image as a result</returns>
        private async Task<BitmapImage> ApplyPrintSettings(LogicalPage logicalPage)
        {
            BitmapImage pageImage = new BitmapImage();

            string pageImagePath = logicalPage.Name; // TODO: Update
            StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;
            StorageFile jpgFile = await tempFolder.GetFileAsync(pageImagePath);
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

                // Render the stream to the screen
                BitmapImage bImg = new BitmapImage();
                bImg.SetSource(imrasStream);

                #region TEST - For Deletion ------------------------------------------------------------------------------------------------------------

                // TEST for dumping into file
                StorageFile testFile = await tempFolder.CreateFileAsync("sampleOutput.jpg");

                using (var destinationStream = await testFile.OpenAsync(FileAccessMode.ReadWrite))
                {
                    BitmapEncoder testEncoder = await BitmapEncoder.CreateAsync(BitmapEncoder.PngEncoderId, destinationStream);
                    testEncoder.SetPixelData(decoder.BitmapPixelFormat, decoder.BitmapAlphaMode,
                        decoder.PixelWidth, decoder.PixelHeight, decoder.DpiX, decoder.DpiY, pixelBytes);
                    await testEncoder.FlushAsync();
                }
                /// END OF TEST
                /// 
                #endregion TEST - For Deletion ---------------------------------------------------------------------------------------------------------
            }

            return pageImage;
        }

        /// <summary>
        /// Changes the bitmap to grayscale
        /// </summary>
        /// <param name="pixelBytes"></param>
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

                double e = (0.21 * r + 0.71 * g + 0.07 * b) * 255;
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
}

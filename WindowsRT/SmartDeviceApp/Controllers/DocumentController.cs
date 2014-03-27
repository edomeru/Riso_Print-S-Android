//
//  DocumentController.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/05.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using Windows.Data.Pdf;
using Windows.Foundation;
using Windows.Graphics.Imaging;
using Windows.Storage;
using Windows.Storage.Streams;

namespace SmartDeviceApp.Controllers
{
    public sealed class DocumentController
    {
        static readonly DocumentController _instance = new DocumentController();

        private const int MAX_PAGES = 5;
        private const string TEMP_PDF_NAME = "tempDoc.pdf";
        private const string FORMAT_LOGICAL_PAGE_IMAGE_FILENAME = "logicalpage{0:0000}.jpg";

        private Document _document;

        /// <summary>
        /// Number of pages of the actual PDF file
        /// </summary>
        public uint PageCount { get; private set; }

        /// <summary>
        /// File name of the actual PDF file
        /// </summary>
        public string FileName { get; private set; }

        /// <summary>
        /// Status if PDF file is successfully loaded
        /// </summary>
        public bool IsFileLoaded { get; private set; }

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static DocumentController() { }

        private DocumentController() {}

        /// <summary>
        /// Singleton instance
        /// </summary>
        public static DocumentController Instance
        {
            get { return _instance; }
        }

        /// <summary>
        /// Copies the PDF to AppData temporary store and opens it
        /// </summary>
        /// <param name="file">source file path of the PDF file</param>
        /// <returns>task</returns>
        public async Task Load(StorageFile file)
        {
            if (file == null)
            {
                return;
            }

            await Unload(); // Ensure to clean up previous

            try
            {
                // Copy to AppData temporary store
                StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;
                StorageFile tempPdfFile =  await file.CopyAsync(tempFolder, TEMP_PDF_NAME,
                    NameCollisionOption.ReplaceExisting);

                // Open and load PDF
                PdfDocument pdfDocument = await PdfDocument.LoadFromFileAsync(tempPdfFile);
                _document = new Document(file.Path, tempPdfFile.Path, pdfDocument);
                PageCount = pdfDocument.PageCount;
                FileName = file.Name;
                IsFileLoaded = true;

                GenerateLogicalPages(0, 0); // Pre-load LogicalPages
            }
            catch (FileNotFoundException)
            {
                // File cannot be loaded
                IsFileLoaded = false;
                return;
            }
        }

        /// <summary>
        /// Clean up loaded PDF and images
        /// </summary>
        /// <returns>task</returns>
        public async Task Unload()
        {
            await StorageFileUtility.DeleteAllTempFiles();
            if (_document != null)
            {
                _document = null;
            }
            IsFileLoaded = false;
        }

        /// <summary>
        /// Get generated LogicalPage(s) from the list.
        /// If target page is not found in the list,
        /// LogicalPage image is created then added to list.
        /// </summary>
        /// <param name="basePageIndex">base page index</param>
        /// <param name="numPages">number of pages needed (for imposition)</param>
        /// <returns>task with generated LogicalPage(s)</returns>
        public async Task<List<LogicalPage>> GetLogicalPages(int basePageIndex, int numPages)
        {
            List<LogicalPage> logicalPages = new List<LogicalPage>();

            int offset = 0;
            do
            {
                int key = basePageIndex + offset;

                // Check page bounds
                int pageCount = (int)_document.PdfDocument.PageCount;
                if (key < 0 || key > pageCount - 1)
                {
                    break;
                }

                if (_document.LogicalPages.ContainsKey(key))
                {
                    logicalPages.Add(_document.LogicalPages[key]);
                    ++offset;
                }
                else
                {
                    LogicalPage logicalPage = await GenerateLogicalPage(key);
                    if (logicalPage != null)
                    {
                        logicalPages.Add(logicalPage);
                        ++offset;
                    }
                }
            } while (offset < numPages);

            return logicalPages;
        }

        /// <summary>
        /// Generates N pages to JPEG then saves in AppData temporary store.
        /// If pages per sheet count is provided, this count is used instead of the defined max
        /// pages.
        /// </summary>
        /// <param name="basePageIndex">base page index</param>
        /// <param name="numPages">number of pages needed (for imposition)</param>
        public async void GenerateLogicalPages(int basePageIndex, int numPages)
        {
            if (!IsFileLoaded)
            {
                return;
            }

            int pageCount = (int)_document.PdfDocument.PageCount;
            if (basePageIndex < 0 || basePageIndex > pageCount - 1)
            {
                return;
            }

            // Compute for start page index
            int maxPages = MAX_PAGES;
            if (numPages > 1)
            {
                maxPages = (numPages * 2) + 1; // Generate on both sides plus 1 (itself)
            }
            int midPt = maxPages / 2; // Round down to the nearest whole number
            int endPageIndex = basePageIndex + midPt;
            int startPageIndex = endPageIndex - (midPt * 2);
            // Compute start page based on end page
            if ((startPageIndex + MAX_PAGES) > pageCount)
            {
                startPageIndex = startPageIndex - ((startPageIndex + MAX_PAGES) - pageCount);
            }
            // Reset to 0 when out of bounds
            if (startPageIndex < 0)
            {
                startPageIndex = 0;
            }

            int generatedPageCount = 0;
            int currPageIndex = startPageIndex;
            do
            {
                await GenerateLogicalPage(currPageIndex);
                ++generatedPageCount;
                ++currPageIndex;
            } while ((generatedPageCount < maxPages) && currPageIndex < pageCount);
        }

        /// <summary>
        /// Generates a page to JPEG then saved to AppData temporary store
        /// </summary>
        /// <param name="pageIndex">page index</param>
        /// <returns>task</returns>
        private async Task<LogicalPage> GenerateLogicalPage(int pageIndex)
        {
            var pageCount = _document.PdfDocument.PageCount;
            if (pageIndex < 0 || pageIndex > pageCount - 1)
            {
                return null;
            }

            // Convert page to JPEG
            LogicalPage logicalPage = null;
            try
            {
                using (PdfPage pdfPage = _document.PdfDocument.GetPage((uint)pageIndex))
                {
                    await pdfPage.PreparePageAsync();

                    if (!_document.LogicalPages.TryGetValue(pageIndex, out logicalPage))
                    {
                        StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;
                        string fileName = String.Format(FORMAT_LOGICAL_PAGE_IMAGE_FILENAME, pageIndex);

                        BitmapDecoder decoder = null;
                        StorageFile jpegFile = await tempFolder.CreateFileAsync(fileName,
                                CreationCollisionOption.ReplaceExisting);
                        using (IRandomAccessStream raStream =
                            await jpegFile.OpenAsync(FileAccessMode.ReadWrite))
                        {
                            // Scale to destination condering the device's DPI
                            PdfPageRenderOptions opt = new PdfPageRenderOptions();
                            double dpiScaleFactor = ImageConstant.DpiScaleFactor;
                            if (dpiScaleFactor > 1.0)
                            {
                                opt.DestinationWidth = (uint)(pdfPage.Size.Width / dpiScaleFactor);
                                opt.DestinationHeight = (uint)(pdfPage.Size.Height / dpiScaleFactor);
                            }
                            else
                            {
                                opt.DestinationWidth = (uint)(pdfPage.Size.Width * dpiScaleFactor);
                                opt.DestinationHeight = (uint)(pdfPage.Size.Height * dpiScaleFactor);
                            }
                            opt.BitmapEncoderId = BitmapEncoder.JpegEncoderId;
                            opt.BackgroundColor = Windows.UI.Colors.White;

                            // Write to stream
                            raStream.Seek(0);
                            await pdfPage.RenderToStreamAsync(raStream, opt);

                            // Get actual size
                            decoder = await BitmapDecoder.CreateAsync(raStream);
                            await raStream.FlushAsync();
                        }

                        // Add to LogicalPage list
                        logicalPage = new LogicalPage((uint)pageIndex, jpegFile.Name,
                            new Size(decoder.PixelWidth, decoder.PixelHeight), pdfPage.Rotation);
                        _document.AddLogicalPage(pageIndex, logicalPage);
                    }
                }
            }
            catch (UnauthorizedAccessException)
            {
                // Error in reading PDF
                // But usually UnauthorizedAccessException is thrown here due to CreateFileAsync
            }

            return logicalPage;
        }

    }
}

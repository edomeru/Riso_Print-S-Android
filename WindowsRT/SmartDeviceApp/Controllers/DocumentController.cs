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
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using Windows.Data.Pdf;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.UI.Xaml.Media.Imaging;

namespace SmartDeviceApp.Controllers
{

    public sealed class DocumentController
    {

        static readonly DocumentController _instance = new DocumentController();

        private const int MAX_LOGICAL_PAGE_IMAGE_CACHE = 5;
        private const string TEMP_PDF_NAME = "tempDoc.pdf";
        private const string PDF_PASSWORD_EMPTY = "";

        private LruCacheHelper<int, WriteableBitmap> _logicalPageImages =
            new LruCacheHelper<int, WriteableBitmap>(MAX_LOGICAL_PAGE_IMAGE_CACHE);

        private Document _document;

        /// <summary>
        /// Number of pages of the actual PDF file
        /// </summary>
        public uint PageCount { get; private set; }

        /// <summary>
        /// PDF File
        /// </summary>
        public StorageFile PdfFile { get; private set; }

        /// <summary>
        /// File name of the actual PDF file
        /// </summary>
        public string FileName { get; private set; }

        /// <summary>
        /// True when PDF is portrait, false otherwise
        /// </summary>
        public bool IsPdfPortrait { get; private set; }

        /// <summary>
        /// PDF loading result
        /// </summary>
        public LoadDocumentResult Result { get; private set; }

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
        /// <param name="file">source PDF file</param>
        /// <returns>task</returns>
        public async Task Load(StorageFile file)
        {
            if (file == null)
            {
                return;
            }

            try
            {
                // Copy to AppData temporary store
                StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;
                PdfFile =  await file.CopyAsync(tempFolder, TEMP_PDF_NAME,
                    NameCollisionOption.ReplaceExisting);

                // Open and load PDF.
                // Attempt to open PDF using empty string to determine whether the PDF requires
                // password to open the file. If the PDF file requires a password,
                // invalid password error will be thrown.
                PdfDocument pdfDocument = await PdfDocument.LoadFromFileAsync(PdfFile,
                    PDF_PASSWORD_EMPTY);

                _document = new Document(file.Path, PdfFile.Path, pdfDocument);
                PageCount = pdfDocument.PageCount;
                FileName = file.Name;
                Result = LoadDocumentResult.Successful;
                GetPdfOrientation();

                GenerateLogicalPageImages(0, MAX_LOGICAL_PAGE_IMAGE_CACHE, new CancellationTokenSource());
            }
            catch (Exception ex)
            {
                LogUtility.LogError(ex);
                Result = LoadDocumentResult.ErrorReadPdf;

                // Check HResult value since LoadFromFileAsync returns error for
                // either open PDF with incorrect password or error in reading the PDF
                unchecked
                {
                    // http://www.symantec.com/business/support/index?page=content&id=TECH12638
                    if (ex.HResult == (int)0x8007052B) // Hex error code for incorrect password
                    {
                        Result = LoadDocumentResult.UnsupportedPdf;
                    }
                }
            }
        }

        /// <summary>
        /// Clean up loaded PDF and images
        /// </summary>
        /// <returns>task</returns>
        public async Task Unload()
        {
            await StorageFileUtility.DeleteAllTempFiles();
            _document = null;
            PageCount = 0;
            PdfFile = null;
            FileName = null;
            Result = LoadDocumentResult.NotStarted;
        }

        /// <summary>
        /// Fetches generated logical page images
        /// </summary>
        /// <param name="basePageIndex">start page index</param>
        /// <param name="numPages">number of pages needed</param>
        /// <param name="cancellationToken">cancellation token</param>
        /// <returns>task; list of logical page images</returns>
        public async Task<List<WriteableBitmap>> GetLogicalPageImages(int basePageIndex,
            int numPages, CancellationTokenSource cancellationToken)
        {
            LogUtility.BeginTimestamp("GetLogicalPages #" + basePageIndex);

            List<WriteableBitmap> logicalPages = new List<WriteableBitmap>();

            int pageIndex = basePageIndex;
            for (int i = 0; i < numPages && pageIndex < PageCount; ++i, ++pageIndex)
            {
                if (cancellationToken.IsCancellationRequested)
                {
                    LogUtility.EndTimestamp("GetLogicalPages #" + basePageIndex);
                    return logicalPages;
                }

                if (_logicalPageImages.ContainsKey(pageIndex))
                {
                    logicalPages.Add(_logicalPageImages.GetValue(pageIndex));
                }
                else
                {
                    WriteableBitmap pageBitmap = await GenerateLogicalPageImage(pageIndex, cancellationToken);

                    if (cancellationToken.IsCancellationRequested)
                    {
                        LogUtility.EndTimestamp("GetLogicalPages #" + basePageIndex);
                        return logicalPages;
                    }

                    logicalPages.Add(pageBitmap);
                    _logicalPageImages.Add(pageIndex, pageBitmap);
                }
            }

            GenerateLogicalPageImages(pageIndex, MAX_LOGICAL_PAGE_IMAGE_CACHE, cancellationToken);

            LogUtility.EndTimestamp("GetLogicalPages #" + basePageIndex);

            return logicalPages;
        }

        /// <summary>
        /// Generates logical page images and adds them to cache
        /// </summary>
        /// <param name="basePageIndex">start page index</param>
        /// <param name="numPages">number of pages needed</param>
        /// <param name="cancellationToken">cancellation token</param>
        private async void GenerateLogicalPageImages(int basePageIndex, int numPages,
            CancellationTokenSource cancellationToken)
        {
            LogUtility.BeginTimestamp("GenerateLogicalPageImages #" + basePageIndex);

            if (Result != LoadDocumentResult.Successful)
            {
                LogUtility.EndTimestamp("GenerateLogicalPageImages #" + basePageIndex);
                return;
            }

            for (int i = 0, pageIndex = basePageIndex;
                 i < numPages && pageIndex + i < PageCount;
                 ++i, ++pageIndex)
            {
                if (cancellationToken.IsCancellationRequested)
                {
                    LogUtility.EndTimestamp("GenerateLogicalPageImages #" + basePageIndex);
                    return;
                }

                _logicalPageImages.Add(pageIndex,
                    await GenerateLogicalPageImage(pageIndex, cancellationToken));
            }

            LogUtility.EndTimestamp("GenerateLogicalPageImages #" + basePageIndex);

            return;
        }

        /// <summary>
        /// Generates a single logical page image
        /// </summary>
        /// <param name="pageIndex">page index</param>
        /// <param name="cancellationToken">cancellation token</param>
        /// <returns>task; logical page image</returns>
        private async Task<WriteableBitmap> GenerateLogicalPageImage(int pageIndex,
            CancellationTokenSource cancellationToken)
        {
            using (PdfPage pdfPage = _document.PdfDocument.GetPage((uint)(pageIndex)))
            {
                await pdfPage.PreparePageAsync();

                using (IRandomAccessStream raStream = new MemoryStream().AsRandomAccessStream())
                {
                    PdfPageRenderOptions options = new PdfPageRenderOptions();
                    double dpiScaleFactor = ImageConstant.GetDpiScaleFactor();
                    if (dpiScaleFactor > 1.0)
                    {
                        options.DestinationWidth = (uint)(pdfPage.Size.Width / dpiScaleFactor);
                        options.DestinationHeight = (uint)(pdfPage.Size.Height / dpiScaleFactor);
                    }
                    else
                    {
                        options.DestinationWidth = (uint)(pdfPage.Size.Width * dpiScaleFactor);
                        options.DestinationHeight = (uint)(pdfPage.Size.Height * dpiScaleFactor);
                    }
                    options.BackgroundColor = Windows.UI.Colors.White;
                    await pdfPage.RenderToStreamAsync(raStream, options);
                    WriteableBitmap pageBitmap = new WriteableBitmap((int)options.DestinationWidth,
                        (int)options.DestinationHeight);
                    pageBitmap = await WriteableBitmapExtensions.FromStream(pageBitmap, raStream);

                    await raStream.FlushAsync();

                    return pageBitmap;
                }
            }
        }

        /// <summary>
        /// Determines the orientation of the PDF document based on its initial page
        /// </summary>
        private void GetPdfOrientation()
        {
            using (PdfPage pdfPage = _document.PdfDocument.GetPage(0))
            {
                IsPdfPortrait = pdfPage.Size.Width <= pdfPage.Size.Height;
            }
        }

    }

}

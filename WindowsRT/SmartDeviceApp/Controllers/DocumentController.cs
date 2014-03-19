﻿//
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

using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using Windows.Data.Pdf;
using Windows.Storage;
using Windows.Storage.Streams;

namespace SmartDeviceApp.Controllers
{
    public sealed class DocumentController
    {
        static readonly DocumentController _instance = new DocumentController();

        private const int MAX_PAGES = 5;
        private const string TEMP_PDF_NAME = "tempDoc.pdf";
        private const string FORMAT_IMAGE_FILENAME = "image{0:0000}.jpg";

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
            await DeleteTempFiles();
            if (_document != null)
            {
                _document = null;
            }
            IsFileLoaded = false;
        }

        /// <summary>
        /// Generates N pages to JPEG then saves in AppData temporary store
        /// </summary>
        /// <param name="basePageIndex">requested page number</param>
        /// <param name="numPages">number of pages to generate</param>
        /// <returns>task</returns>
        public async Task<List<LogicalPage>> GenerateLogicalPages(int basePageIndex, int numPages)
        {
            if (!IsFileLoaded)
            {
                return null;
            }

            int pageCount = (int)_document.PdfDocument.PageCount;
            if (basePageIndex < 0 || basePageIndex > pageCount - 1)
            {
                return null;
            }

            List<LogicalPage> logicalPages = new List<LogicalPage>();

            // Compute for start page index
            int midPt = MAX_PAGES / 2; // Round down to the nearest whole number
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
                LogicalPage logicalPage = await GenerateLogicalPage(currPageIndex);
                if (logicalPage != null)
                {
                    if (currPageIndex >= basePageIndex && numPages > 0)
                    {
                        // Add only to result if requested
                        logicalPages.Add(logicalPage);
                        --numPages;
                    }
                    ++generatedPageCount;
                }
                ++currPageIndex;
            } while (generatedPageCount < MAX_PAGES && currPageIndex < pageCount);

            return logicalPages;
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
                    StorageFile jpegFile = null;
                    bool jpegFileExists = false;

                    StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;
                    string fileName = String.Format(FORMAT_IMAGE_FILENAME, pageIndex);

                    // Check if page image exists in AppData
                    try
                    {
                        StorageFile testJpegFile = await tempFolder.GetFileAsync(fileName);
                        jpegFileExists = true;
                        jpegFile = testJpegFile;
                    }
                    catch (Exception)
                    {
                        // JPEG file does not exist
                    }

                    if (!jpegFileExists)
                    {
                        try
                        {
                            jpegFile = await tempFolder.CreateFileAsync(fileName,
                                CreationCollisionOption.ReplaceExisting);
                            using (IRandomAccessStream raStream =
                                await jpegFile.OpenAsync(FileAccessMode.ReadWrite))
                            {
                                await pdfPage.RenderToStreamAsync(raStream);
                            }
                        }
                        catch (Exception)
                        {
                            // Bizzare error
                        }
                    }

                    if (jpegFile != null)
                    {
                        logicalPage = new LogicalPage((uint)pageIndex, jpegFile.Name, pdfPage.Size,
                            pdfPage.Rotation);
                        if (!_document.LogicalPages.ContainsKey(pageIndex))
                        {
                            _document.AddLogicalPage(pageIndex, logicalPage);
                        }
                    }
                }
            }
            catch (Exception)
            {
                // Error in reading PDF
            }

            return logicalPage;
        }

        /// <summary>
        /// Deletes all files in AppData temporary store
        /// </summary>
        /// <returns>task</returns>
        private async Task DeleteTempFiles()
        {
            StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;
            var files = await tempFolder.GetFilesAsync();
            foreach (var file in files)
            {
                await file.DeleteAsync(StorageDeleteOption.PermanentDelete);
            }
        }
    }
}

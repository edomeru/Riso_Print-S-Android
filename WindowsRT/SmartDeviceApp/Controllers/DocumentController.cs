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

using SmartDeviceApp.Models;
using System;
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

        private const int MAX_PAGES = 7;
        private const string TEMP_PDF_NAME = "temp.pdf";
        private const string FORMAT_IMAGE_FILENAME = "image{0:0000}.jpg";

        private Document _document;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static DocumentController() { }

        private DocumentController() {}

        public static DocumentController Instance
        {
            get { return _instance; }
        }

        /// <summary>
        /// Marks the entry point of PDF processing
        /// </summary>
        /// <param name="filePath">File path of the PDF file</param>
        /// <returns>true when loading is successful. Otherwise, false.</returns>
        public async void Load(StorageFile file)
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
                StorageFile tempPdfFile =  await file.CopyAsync(tempFolder, TEMP_PDF_NAME, NameCollisionOption.ReplaceExisting);

                // Load PDF
                PdfDocument pdfDocument = await PdfDocument.LoadFromFileAsync(tempPdfFile);
                _document = new Document(file.Path, tempPdfFile.Path, pdfDocument);
            }
            catch (FileNotFoundException)
            {
                // File cannot be loaded
                PrintPreviewController.Instance.OnLoadDocumentFinished(false);
                return;
            }

            // Send notification after PDF is opened
            PrintPreviewController.Instance.OnLoadDocumentFinished(true);

            GenerateLogicalPages(0, false); // Initial page number is 0
        }

        /// <summary>
        /// Marks the end of PDF processing
        /// </summary>
        public async Task Unload()
        {
            await DeleteTempFiles();
            if (_document != null)
            {
                _document = null;
            }
        }

        /// <summary>
        /// Generates N pages to JPEG then saves in AppData/temp
        /// </summary>
        /// <param name="basePageIndex">requested page number</param>
        /// <param name="enableSendPage">enable send page to caller</param>
        public async void GenerateLogicalPages(int basePageIndex, bool enableSendPage)
        {
            int pageCount = (int) _document.PdfDocument.PageCount;
            if (basePageIndex < 0 || basePageIndex > pageCount - 1)
            {
                return;
            }

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
                await GenerateLogicalPage(currPageIndex);
                ++currPageIndex;
                ++generatedPageCount;
            } while (generatedPageCount < MAX_PAGES && currPageIndex < pageCount);

            if (enableSendPage)
            {
                PrintPreviewController.Instance.OnReceiveLogicalPage(_document.LogicalPages[basePageIndex]);
            }
        }

        /// <summary>
        /// Generates a page to JPEG then saved to AppData/temp
        /// </summary>
        /// <param name="pageIndex">page index</param>
        private async Task GenerateLogicalPage(int pageIndex)
        {
            var pageCount = _document.PdfDocument.PageCount;
            if (pageIndex < 0 || pageIndex > pageCount - 1)
            {
                return;
            }

            // Convert page to JPEG
            try
            {
                using (PdfPage pdfPage = _document.PdfDocument.GetPage((uint)pageIndex))
                {
                    await pdfPage.PreparePageAsync();
                    try
                    {
                        StorageFolder tempFolder = ApplicationData.Current.TemporaryFolder;
                        // TODO: Check if saving image to AppData is necessary
                        StorageFile jpgFile = await tempFolder.CreateFileAsync(String.Format(FORMAT_IMAGE_FILENAME, pageIndex),
                            CreationCollisionOption.FailIfExists);
                        using (IRandomAccessStream raStream = await jpgFile.OpenAsync(FileAccessMode.ReadWrite))
                        {
                            await pdfPage.RenderToStreamAsync(raStream);
                            LogicalPage logicalPage = new LogicalPage((uint)pageIndex, jpgFile.Name, pdfPage.Size, pdfPage.Rotation);
                            _document.AddLogicalPage(pageIndex, logicalPage);
                        }
                    }
                    catch (Exception)
                    {
                        // JPEG already exists in temp folder
                    }
                }
            }
            catch (Exception)
            {
                // Error in reading PDF
            }
        }

        /// <summary>
        /// Deletes all files in AppData temporary store
        /// </summary>
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

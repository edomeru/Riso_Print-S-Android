//
//  Document.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/05.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using Windows.Data.Pdf;
using Windows.Foundation;
using Windows.UI.Xaml.Media.Imaging;

namespace SmartDeviceApp.Models
{
    /// <summary>
    /// Represents the PDF file
    /// </summary>
    public class Document
    {

        /// <summary>
        /// File path of the PDF file opened by the user
        /// </summary>
        public string OrigSource { get; private set; }

        /// <summary>
        /// File path of the PDF file saved in AppData
        /// </summary>
        public string TempSource { get; private set; }

        /// <summary>
        /// PDF document object
        /// </summary>
        public PdfDocument PdfDocument { get; private set; }

        /// <summary>
        /// Document class constructor
        /// </summary>
        /// <param name="sourceFilePath">file path of the actual PDF</param>
        /// <param name="tempSource">file path of the PDF in AppData</param>
        /// <param name="pdfDocument">PDF document</param>
        /// <param name="name">file name</param>
        public Document(string sourceFilePath, string tempSource, PdfDocument pdfDocument)
        {
            OrigSource = sourceFilePath;
            TempSource = tempSource;
            PdfDocument = pdfDocument;
        }

    }

    /// <summary>
    /// Represents a single page from the PDF
    /// </summary>
    public class LogicalPage
    {

        /// <summary>
        /// Page image
        /// </summary>
        public WriteableBitmap Image { get; private set; }

        /// <summary>
        /// Actual image size in pixels
        /// </summary>
        public Size ActualSize { get; private set; }

        /// <summary>
        /// Flag to determine the orientation of the page.
        /// True when portrait, false otherwise (landscape).
        /// </summary>
        public bool IsPortrait { get; private set; }

        /// <summary>
        /// LogicalPage class constructor
        /// </summary>
        /// <param name="image">page image</param>
        /// <param name="actualSize">actual image size</param>
        /// <param name="isPortrait">true when portrait, false otherwise</param>
        public LogicalPage(WriteableBitmap image, Size actualSize, bool isPortrait)
        {
            Image = image;
            ActualSize = actualSize;
            IsPortrait = isPortrait;
        }

    }
}

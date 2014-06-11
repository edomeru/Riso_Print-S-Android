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

namespace SmartDeviceApp.Models
{
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
}

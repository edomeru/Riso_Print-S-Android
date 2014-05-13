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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Data.Pdf;

namespace SmartDeviceApp.Models
{
    public class Document
    {

        // TODO: Check if this is still necessary
        private Dictionary<int, LogicalPage> _logicalPages;

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
        /// Generated logical pages of this document
        /// </summary>
        public Dictionary<int, LogicalPage> LogicalPages
        {
            get { return _logicalPages; }
        }

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
            _logicalPages = new Dictionary<int, LogicalPage>();
        }

        public void AddLogicalPage(int pageIndex, LogicalPage logicalPage)
        {
            _logicalPages.Add(pageIndex, logicalPage);
        }

    }
}

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
        private string _origSource;
        private string _tempSource;
        private PdfDocument _pdfDocument;
        private Dictionary<int, LogicalPage> _logicalPages;

        public Document(string sourceFilePath, string tempSource, PdfDocument pdfDocument)
        {
            _origSource = sourceFilePath;
            _tempSource = tempSource;
            _pdfDocument = pdfDocument;
            _logicalPages = new Dictionary<int, LogicalPage>();
        }

        public string OrigSource
        {
            get { return _origSource; }
        }

        public string TempSource
        {
            get { return _tempSource; }
        }

        public PdfDocument PdfDocument
        {
            get { return _pdfDocument; }
        }

        public Dictionary<int, LogicalPage> LogicalPages
        {
            get { return _logicalPages; }
        }

        public void AddLogicalPage(int pageIndex, LogicalPage logicalPage)
        {
            _logicalPages.Add(pageIndex, logicalPage);
        }

    }
}

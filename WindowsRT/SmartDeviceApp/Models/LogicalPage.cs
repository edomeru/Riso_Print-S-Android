//
//  LogicalPage.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/07.
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
using Windows.Foundation;

namespace SmartDeviceApp.Models
{
    public class LogicalPage
    {
        private uint _pageIndex;
        private string _imageId;
        private Size _size;
        private PdfPageRotation _rotation;

        public LogicalPage(uint pageIndex, string imageId, Size size, PdfPageRotation rotation)
        {
            _pageIndex = pageIndex;
            _imageId = imageId;
            _size = size;
            _rotation = rotation;
        }

        public uint PageIndex
        {
            get { return _pageIndex; }
        }

        public string ImageId
        {
            get { return _imageId; }
        }

        public Size Size
        {
            get { return _size; }
        }

        public PdfPageRotation Rotation
        {
            get { return _rotation; }
        }
    }
}

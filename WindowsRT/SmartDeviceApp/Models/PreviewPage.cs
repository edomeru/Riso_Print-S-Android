//
//  PreviewPage.cs
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
using Windows.UI.Xaml.Media.Imaging;

namespace SmartDeviceApp.Models
{
    public class PreviewPage
    {
        private uint _pageIndex;
        private BitmapImage _pageImage;
        private Size _size;
        private PdfPageRotation _rotation;

        public PreviewPage(uint pageIndex, BitmapImage pageImage, Size size, PdfPageRotation rotation)
        {
            _pageIndex = pageIndex;
            _pageImage = pageImage;
            _size = size;
            _rotation = rotation;
        }

        public uint PageIndex
        {
            get { return _pageIndex; }
        }

        public BitmapImage PageImage
        {
            get { return _pageImage; }
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

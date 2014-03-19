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
        public BitmapImage PageImage { get; private set; }
        public Size ActualSize { get; private set; }

        public PreviewPage(BitmapImage pageImage, Size actualSize)
        {
            PageImage = pageImage;
            ActualSize = actualSize;
        }
    }
}

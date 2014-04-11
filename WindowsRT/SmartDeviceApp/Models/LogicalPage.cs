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

        public uint PageIndex { get; private set; }
        public string Name { get; private set; }
        public Size ActualSize { get; private set; }
        public PdfPageRotation Rotation { get; private set; }

        public LogicalPage(uint pageIndex, string name, Size actualSize, PdfPageRotation rotation)
        {
            PageIndex = pageIndex;
            Name = name;
            ActualSize = actualSize;
            Rotation = rotation;
        }

    }
}

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

using Windows.Data.Pdf;
using Windows.Foundation;

namespace SmartDeviceApp.Models
{
    public class LogicalPage
    {

        /// <summary>
        /// Page index; zero-based
        /// </summary>
        public uint PageIndex { get; private set; }

        /// <summary>
        /// File name of saved LogicalPage image in AppData TempStore
        /// </summary>
        public string Name { get; private set; }

        /// <summary>
        /// Actual size of LogicalPage image in pixels
        /// </summary>
        public Size ActualSize { get; private set; }

        /// <summary>
        /// PDF rotation (if needed)
        /// </summary>
        public PdfPageRotation Rotation { get; private set; }

        /// <summary>
        /// Flag to determine page orientation. True for portrait, else false.
        /// </summary>
        public bool IsPortrait { get; private set; }

        public LogicalPage(uint pageIndex, string name, Size actualSize, PdfPageRotation rotation)
        {
            PageIndex = pageIndex;
            Name = name;
            ActualSize = actualSize;
            Rotation = rotation;
            IsPortrait = (actualSize.Width <= actualSize.Height) ? true : false;
        }

    }
}

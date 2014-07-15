//
//  DataItem.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/02/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

namespace SmartDeviceApp.Models
{
    public class DataItem
    {
        /// <summary>
        /// Gets/sets the title of a data item
        /// </summary>
        public string Title
        {
            get;
            private set;
        }

        /// <summary>
        /// DataItem class constructor
        /// </summary>
        /// <param name="title">title</param>
        public DataItem(string title)
        {
            Title = title;
        }
    }
}
//
//  DataService.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/02/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System.Threading.Tasks;

namespace SmartDeviceApp.Models
{
    public class DataService : IDataService
    {
        /// <summary>
        /// Retrieves a data from data service
        /// </summary>
        /// <returns></returns>
        public async Task<DataItem> GetData()
        {
            // Use this to connect to the actual data service

            // Simulate by returning a DataItem
            var item = new DataItem("Welcome to MVVM Light");
            return item;
        }
    }
}
//
//  IDataService.cs 
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
    public interface IDataService
    {
        Task<DataItem> GetData();
    }
}
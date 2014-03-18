//
//  HomeViewModel.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/02/25.
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
using GalaSoft.MvvmLight;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;

namespace SmartDeviceApp.ViewModels
{
    public class HomeViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;
        
        public HomeViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            Initialize();
        }

        ////public override void Cleanup()
        ////{
        ////    // Clean up if needed

        ////    base.Cleanup();
        ////}
        public void Load(DateTime lastVisit)
        {
            if (lastVisit > DateTime.MinValue)
            {
                // TODO
            }
        }

        private async Task Initialize()
        {
            try
            {
                var item = await _dataService.GetData();
                // TODO
            }
            catch (Exception ex)
            {
                // Report error here
            }
        }
    }
}
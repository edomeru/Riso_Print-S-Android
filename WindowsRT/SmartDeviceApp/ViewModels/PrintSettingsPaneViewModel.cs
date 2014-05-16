﻿//
//  PrintSettingsPaneViewModel.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/20.
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
using System.IO;
using System.Threading.Tasks;
using System.Xml.Serialization;
using System.Xml.Linq;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.ApplicationModel;
using GalaSoft.MvvmLight;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.ViewModels
{
    public class PrintSettingsPaneViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private PrintSettingsPaneMode _printSettingsPaneMode;

        public PrintSettingsPaneViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
        }

        public PrintSettingsPaneMode PrintSettingsPaneMode
        {
            get { return _printSettingsPaneMode; }
            set
            {
                if (_printSettingsPaneMode != value)
                {
                    _printSettingsPaneMode = value;
                    RaisePropertyChanged("PrintSettingsPaneMode");
                }
            }
        }
    }
}
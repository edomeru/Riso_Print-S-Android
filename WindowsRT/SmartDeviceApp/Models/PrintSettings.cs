//
//  PrintSettings.cs
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
using System.Collections.ObjectModel;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Serialization;
using GalaSoft.MvvmLight;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.Models
{
    public class PrintSettingOption
    {
        public string Text { get; set; }
        public int Index { get; set; }
        public bool IsEnabled { get; set; }
    }

    public class PrintSetting : ObservableObject
    {
        private object _value;
        private PrintSettingOption _selectedOption;

        public string Name { get; set; }
        public string Text { get; set; }
        public string Icon { get; set; }
        public PrintSettingType Type { get; set; }
        public object Value
        {
            get { return _value; }
            set
            {
                if (_value != value)
                {
                    _value = value;
                    RaisePropertyChanged("Value");
                    RaisePropertyChanged("SelectedOption");
                }
            }
        }
        // Should be used only if PrintSettingType = list
        public PrintSettingOption SelectedOption
        {
            get 
            {
                if ((int)Value < Options.Count) _selectedOption = Options[(int)Value];
                else _selectedOption = null;
                return _selectedOption;
            }
            set
            {
                if (_selectedOption != value)
                {
                    _selectedOption = value;
                    RaisePropertyChanged("SelectedOption");
                }
            }
        }
        public object Default { get; set; }
        public List<PrintSettingOption> Options { get; set; }
        public bool IsEnabled { get; set; }
    }

    public class PrintSettingGroup
    {
        public string Name { get; set; }
        public string Text { get; set; }
        public List<PrintSetting> PrintSettings { get; set; }
    }

    public class PrintSettingList : ObservableCollection<PrintSettingGroup>
    {
        public List<PrintSettingGroup> Groups { get; set; }
    }
}

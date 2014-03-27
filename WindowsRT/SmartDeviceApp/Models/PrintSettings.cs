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
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.Models
{
    public class PrintSettingOption
    {
        public string Text { get; set; }
        public int Index { get; set; }
    }

    public class PrintSetting
    {
        public string Name { get; set; }
        public string Text { get; set; }
        public string Icon { get; set; }
        public PrintSettingType Type { get; set; }
        public object Value { get; set; }
        public List<PrintSettingOption> Options { get; set; }
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

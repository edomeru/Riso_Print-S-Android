//
//  SelectPrinterBox.xaml.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/04/29.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

namespace SmartDeviceApp.Views
{
    public sealed partial class SelectPrinterBox : Grid
    {
        /// <summary>
        /// Constructor. Initializes UI components.
        /// </summary>
        public SelectPrinterBox()
        {
            this.InitializeComponent();
        }
    }
}

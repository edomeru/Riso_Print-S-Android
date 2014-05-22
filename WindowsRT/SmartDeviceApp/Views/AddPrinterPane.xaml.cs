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
using SmartDeviceApp.Controls;
using GalaSoft.MvvmLight.Messaging;

namespace SmartDeviceApp.Views
{
    public sealed partial class AddPrinterPane : UserControl
    {
        public AddPrinterPane()
        {
            this.InitializeComponent();

            //Messenger.Default.Register<string>(this, (tapped) => HideKeyboard(tapped));
        }

        //private void HideKeyboard(string tapped)
        //{
        //    if (tapped == "HideKeyboard")
        //    { 
        //        this.IPTextBlock.Focus(FocusState.Programmatic);
        //    }
        //}
    }
}

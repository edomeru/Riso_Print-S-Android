using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Windows.Input;
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
    public sealed partial class PrintSettingOptionsBox : StackPanel
    {
        public PrintSettingOptionsBox()
        {
            this.InitializeComponent();
        }

        public static readonly DependencyProperty PrintSettingTextProperty =
            DependencyProperty.Register("PrintSettingText", typeof(string), typeof(PrintSettingOptionsBox), null);

        public string PrintSettingText
        {
            get { return (string)GetValue(PrintSettingTextProperty); }
            set { SetValue(PrintSettingTextProperty, value); }
        }
    }
}

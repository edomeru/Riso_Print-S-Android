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

// The User Control item template is documented at http://go.microsoft.com/fwlink/?LinkId=234236

namespace SmartDeviceApp.Controls
{
    public partial class KeyImageControl : UserControl
    {
        /// <summary>
        /// Constructor for KeyImageControl
        /// </summary>
        public KeyImageControl()
        {
            this.InitializeComponent();
        }


        public static readonly DependencyProperty PrinterImageProperty =
            DependencyProperty.Register("PrinterImage", typeof(ImageSource), typeof(KeyImageControl), null);

        /// <summary>
        /// ImageSource property for the image to be displayed in this control
        /// </summary>
        public ImageSource PrinterImage
        {
            get { return (ImageSource)GetValue(PrinterImageProperty); }
            set { SetValue(PrinterImageProperty, value); }
        }
    }
}

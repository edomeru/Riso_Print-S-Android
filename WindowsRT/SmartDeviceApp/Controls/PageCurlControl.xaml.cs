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

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234238

namespace SmartDeviceApp.Controls
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class PageCurlControl : UserControl
    {
        public PageCurlControl()
        {
            this.InitializeComponent();
        }

        public static readonly DependencyProperty RightBackPageImageProperty =
            DependencyProperty.Register("RightBackPageImage", typeof(ImageSource), typeof(TwoPageControl), null);

        public static readonly DependencyProperty LeftBackPageImageProperty =
            DependencyProperty.Register("LeftBackPageImage", typeof(ImageSource), typeof(TwoPageControl), null);

       
        public ImageSource RightBackPageImage
        {
            get { return (ImageSource)GetValue(RightBackPageImageProperty); }
            set { SetValue(RightBackPageImageProperty, value); }
        }

        public ImageSource LeftBackPageImage
        {
            get { return (ImageSource)GetValue(LeftBackPageImageProperty); }
            set { SetValue(LeftBackPageImageProperty, value); }
        }

        public TranslateTransform Page2TranslateTransform
        {
            get { return Page2ClipTranslateTransform; }
        }

        public RotateTransform Page2RotateTransform
        {
            get { return Page2ClipRotateTransform; }
        }

        public Grid PageAreaGrid
        {
            get { return pageAreaGrid; }
        }

    }
}

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
using Windows.UI.Xaml.Markup;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

namespace SmartDeviceApp.Controls
{
    [ContentProperty(Name = "Children")]
    public partial class ViewControl : UserControl
    {
        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(ViewControl), null);

        public static readonly DependencyProperty ChildrenProperty = DependencyProperty.Register(
            "Children", typeof(UIElementCollection), typeof(ViewControl), null);

        public static readonly DependencyProperty Button1CommandProperty =
            DependencyProperty.Register("Button1Command", typeof(ICommand), typeof(ViewControl), null);

        public static readonly DependencyProperty Button2CommandProperty =
            DependencyProperty.Register("Button2Command", typeof(ICommand), typeof(ViewControl), null);

        public static readonly DependencyProperty Button3CommandProperty =
            DependencyProperty.Register("Button3Command", typeof(ICommand), typeof(ViewControl), null);

        public static readonly DependencyProperty Button2ImageProperty =
           DependencyProperty.Register("Button2Image", typeof(ImageSource), typeof(ViewControl), null);

        public static readonly DependencyProperty Button3ImageProperty =
           DependencyProperty.Register("Button3Image", typeof(ImageSource), typeof(ViewControl), null);
        
        public static readonly DependencyProperty Button2VisibilityProperty =
           DependencyProperty.Register("Button2Visibility", typeof(ImageSource), typeof(ViewControl), null);

        public static readonly DependencyProperty Button3VisibilityProperty =
           DependencyProperty.Register("Button3Visibility", typeof(ImageSource), typeof(ViewControl), null);
        
        public static new readonly DependencyProperty WidthProperty =
            DependencyProperty.Register("Width", typeof(double), typeof(ViewControl), new PropertyMetadata(0, SetWidth));
        
        public ViewControl()
        {
            this.InitializeComponent();
            Children = contentGrid.Children;
        }

        public string Text
        {
            get { return (string)GetValue(TextProperty); }
            set { SetValue(TextProperty, value); }
        }

        public UIElementCollection Children
        {
            get { return (UIElementCollection)GetValue(ChildrenProperty); }
            private set { SetValue(ChildrenProperty, value); }
        }

        public ICommand Button1Command
        {
            get { return (ICommand)GetValue(Button1CommandProperty); }
            set { SetValue(Button1CommandProperty, value); }
        }

        public ICommand Button2Command
        {
            get { return (ICommand)GetValue(Button2CommandProperty); }
            set { SetValue(Button2CommandProperty, value); }
        }

        public ICommand Button3Command
        {
            get { return (ICommand)GetValue(Button3CommandProperty); }
            set { SetValue(Button3CommandProperty, value); }
        }

        public ImageSource Button2Image
        {
            get { return (ImageSource)GetValue(Button2ImageProperty); }
            set { SetValue(Button2ImageProperty, value); }
        }

        public ImageSource Button3Image
        {
            get { return (ImageSource)GetValue(Button3ImageProperty); }
            set { SetValue(Button3ImageProperty, value); }
        }

        public Visibility Button2Visibility
        {
            get { return (Visibility)GetValue(Button2VisibilityProperty); }
            set { SetValue(Button2VisibilityProperty, value); }
        }

        public Visibility Button3Visibility
        {
            get { return (Visibility)GetValue(Button3VisibilityProperty); }
            set { SetValue(Button3VisibilityProperty, value); }
        }

        public new double Width
        {
            get { return (double)GetValue(WidthProperty); }
            set { SetValue(WidthProperty, value); }
        }

        private static void SetWidth(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue != null && e.NewValue is double)
            {
                ((ViewControl)obj).viewControlGrid.Width = (double)e.NewValue;
            }
        }
    }
}

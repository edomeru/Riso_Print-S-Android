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
    public sealed partial class ViewControl : UserControl
    {
        public static readonly DependencyProperty TitleProperty =
            DependencyProperty.Register("Title", typeof(string), typeof(ViewControl),
            new PropertyMetadata(String.Empty, new PropertyChangedCallback(SetTitle)));

        public static readonly DependencyProperty ChildrenProperty = DependencyProperty.Register(
            "Children", typeof(UIElementCollection), typeof(ViewControl), null);
        
        //public static new readonly DependencyProperty WidthProperty =
        //    DependencyProperty.Register("Width", typeof(double), typeof(ViewControl),
        //    new PropertyMetadata(Visibility.Visible, new PropertyChangedCallback(SetWidth)));

        public static readonly DependencyProperty Button1CommandProperty =
            DependencyProperty.Register("Button1Command", typeof(ICommand), typeof(ViewControl),
            new PropertyMetadata(null, new PropertyChangedCallback(SetButton1Command)));

        public static readonly DependencyProperty Button2CommandProperty =
            DependencyProperty.Register("Button2Command", typeof(ICommand), typeof(ViewControl),
            new PropertyMetadata(null, new PropertyChangedCallback(SetButton2Command)));

        public static readonly DependencyProperty Button3CommandProperty =
            DependencyProperty.Register("Button3Command", typeof(ICommand), typeof(ViewControl),
            new PropertyMetadata(null, new PropertyChangedCallback(SetButton3Command)));

        public static readonly DependencyProperty Button1ImageProperty =
            DependencyProperty.Register("Button1ImageSource", typeof(ImageSource), typeof(ViewControl),
            new PropertyMetadata(null, new PropertyChangedCallback(SetButton1ImageSource)));  

        public static readonly DependencyProperty Button2ImageProperty =
            DependencyProperty.Register("Button2ImageSource", typeof(ImageSource), typeof(ViewControl),
            new PropertyMetadata(null, new PropertyChangedCallback(SetButton2ImageSource)));        
        
        public static readonly DependencyProperty Button3ImageProperty =
            DependencyProperty.Register("Button3ImageSource", typeof(ImageSource), typeof(ViewControl),
            new PropertyMetadata(null, new PropertyChangedCallback(SetButton3ImageSource)));
        

        public ViewControl()
        {
            this.InitializeComponent();
            Children = contentGrid.Children;
        }


        public string Title
        {
            get { return (string)GetValue(TitleProperty); }
            set { SetValue(TitleProperty, value); }
        }

        public ImageSource Button1ImageSource
        {
            get { return (ImageSource)GetValue(Button1ImageProperty); }
            set { SetValue(Button1ImageProperty, value); }
        }


        public ImageSource Button2ImageSource
        {
            get { return (ImageSource)GetValue(Button2ImageProperty); }
            set { SetValue(Button2ImageProperty, value); }
        }


        public ImageSource Button3ImageSource
        {
            get { return (ImageSource)GetValue(Button3ImageProperty); }
            set { SetValue(Button3ImageProperty, value); }
        }

        public UIElementCollection Children
        {
            get { return (UIElementCollection)GetValue(ChildrenProperty); }
            private set { SetValue(ChildrenProperty, value); }
        }
        
        //public new double Width
        //{
        //    get { return (double)GetValue(WidthProperty); }
        //    set { SetValue(WidthProperty, value); }
        //}

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

        #region PRIVATE METHODS

        private static void SetTitle(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            ((ViewControl)obj).viewTitle.Text = (string)e.NewValue;
        }

        private static void SetButton1ImageSource(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            ImageBrush ib = new ImageBrush();
            ib.ImageSource = (ImageSource)e.NewValue;
            ((ViewControl)obj).button1.Background = ib;
        }

        private static void SetButton2ImageSource(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            ImageBrush ib = new ImageBrush();
            ib.ImageSource = (ImageSource)e.NewValue;
            ((ViewControl)obj).button2.Background = ib;
        }

        private static void SetButton3ImageSource(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            ImageBrush ib = new ImageBrush();
            ib.ImageSource = (ImageSource)e.NewValue;
            ((ViewControl)obj).button3.Background = ib; 
        }

        //private static void SetWidth(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        //{
        //    ((ViewControl)obj).viewControl.Width = (double)e.NewValue;
        //}

        private static void SetButton1Command(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if ((ICommand)e.NewValue != null)
            {
                ((ViewControl)obj).button1.Command = (ICommand)e.NewValue;
            }
        }

        private static void SetButton2Command(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if ((ICommand)e.NewValue != null)
            {
                ((ViewControl)obj).button2.Command = (ICommand)e.NewValue;
            }
        }

        private static void SetButton3Command(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if ((ICommand)e.NewValue != null)
            {
                ((ViewControl)obj).button3.Command = (ICommand)e.NewValue;
            }
        }

        #endregion
    }
}

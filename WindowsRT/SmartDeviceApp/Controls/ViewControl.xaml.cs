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

        public static readonly DependencyProperty ChildrenDataContextProperty = 
            DependencyProperty.Register("ChildrenDataContext", typeof(object), typeof(ViewControl), null);

        public static readonly DependencyProperty RightPaneContentProperty =
            DependencyProperty.Register("RightPaneContent", typeof(object), typeof(ViewControl), null);

        public static readonly DependencyProperty RightPaneContentTemplateSelectorProperty =
            DependencyProperty.Register("RightPaneContentTemplateSelector", typeof(DataTemplateSelector), typeof(ViewControl), null);
        
        public static readonly DependencyProperty Button1ImageProperty =
           DependencyProperty.Register("Button1Image", typeof(ImageSource), typeof(ViewControl), null);

        public static readonly DependencyProperty Button2ImageProperty =
           DependencyProperty.Register("Button2Image", typeof(ImageSource), typeof(ViewControl), null);
        
        public static readonly DependencyProperty Button1VisibilityProperty =
           DependencyProperty.Register("Button1Visibility", typeof(Visibility), typeof(ViewControl), null);
        
        public static readonly DependencyProperty Button2VisibilityProperty =
           DependencyProperty.Register("Button2Visibility", typeof(Visibility), typeof(ViewControl), null);

        //public static new readonly DependencyProperty WidthProperty =
        //    DependencyProperty.Register("Width", typeof(double), typeof(ViewControl), new PropertyMetadata(0, SetWidth));
        
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

        public object ChildrenDataContext
        {
            get { return (object)GetValue(ChildrenDataContextProperty); }
            set { SetValue(ChildrenDataContextProperty, value); }
        }

        public object RightPaneContent
        {
            get { return (object)GetValue(RightPaneContentProperty); }
            set { SetValue(RightPaneContentProperty, value); }
        }

        public DataTemplateSelector RightPaneContentTemplateSelector
        {
            get { return (DataTemplateSelector)GetValue(RightPaneContentTemplateSelectorProperty); }
            set { SetValue(RightPaneContentTemplateSelectorProperty, value); }
        }

        public ImageSource Button1Image
        {
            get { return (ImageSource)GetValue(Button1ImageProperty); }
            set { SetValue(Button1ImageProperty, value); }
        }

        public ImageSource Button2Image
        {
            get { return (ImageSource)GetValue(Button2ImageProperty); }
            set { SetValue(Button2ImageProperty, value); }
        }

        public Visibility Button1Visibility
        {
            get { return (Visibility)GetValue(Button1VisibilityProperty); }
            set { SetValue(Button1VisibilityProperty, value); }
        }

        public Visibility Button2Visibility
        {
            get { return (Visibility)GetValue(Button2VisibilityProperty); }
            set { SetValue(Button2VisibilityProperty, value); }
        }

        //public new double Width
        //{
        //    get { return (double)GetValue(WidthProperty); }
        //    set { SetValue(WidthProperty, value); }
        //}

        //private static void SetWidth(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        //{
        //    if (e.NewValue != null && e.NewValue is double)
        //    {
        //        ((ViewControl)obj).viewRoot.Width = (double)e.NewValue;
        //    }
        //}
    }
}

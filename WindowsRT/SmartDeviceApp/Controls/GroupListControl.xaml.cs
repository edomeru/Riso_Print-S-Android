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

namespace SmartDeviceApp.Controls
{
    public sealed partial class GroupListControl : UserControl
    {
        public GroupListControl()
        {
            this.InitializeComponent();
        }

        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(GroupListControl), null);

        public static new readonly DependencyProperty ContentProperty =
           DependencyProperty.Register("Content", typeof(object), typeof(GroupListControl), null);

        public static readonly DependencyProperty DeleteCommandProperty =
            DependencyProperty.Register("DeleteCommand", typeof(ICommand), typeof(GroupListControl), null);

        public static readonly DependencyProperty DeleteCommandParameterProperty =
           DependencyProperty.Register("DeleteCommandParameter", typeof(object), typeof(GroupListControl), null);

        public static readonly DependencyProperty DeleteButtonVisibilityProperty =
           DependencyProperty.Register("DeleteButtonVisibility", typeof(Visibility), typeof(GroupListControl), null);

        public string Text
        {
            get { return (string)GetValue(TextProperty); }
            set { SetValue(TextProperty, value); }
        }

        public new object Content
        {
            get { return (object)GetValue(ContentProperty); }
            set { SetValue(ContentProperty, value); }
        }

        public ICommand DeleteCommand
        {
            get { return (ICommand)GetValue(DeleteCommandProperty); }
            set { SetValue(DeleteCommandProperty, value); }
        }

        public object DeleteCommandParameter
        {
            get { return (object)GetValue(DeleteCommandParameterProperty); }
            set { SetValue(DeleteCommandParameterProperty, value); }
        }

        public Visibility DeleteButtonVisibility
        {
            get { return (Visibility)GetValue(DeleteButtonVisibilityProperty); }
            set { SetValue(DeleteButtonVisibilityProperty, value); }
        }
    }
}

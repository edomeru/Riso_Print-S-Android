using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Input;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using System.Windows.Input;

namespace SmartDeviceApp.Views
{
    public sealed partial class RightPane : UserControl
    {
        public RightPane()
        {
            // TODO: Verify if this is acceptable for MVVM
            Messenger.Default.Register<RightPaneMode>(this, (rightPaneMode) => OnSetRightPaneMode(rightPaneMode));
            this.InitializeComponent();
        }

        public static readonly DependencyProperty TitleProperty =
            DependencyProperty.Register("Title", typeof(string), typeof(RightPane), null);

        public static new readonly DependencyProperty ContentProperty =
            DependencyProperty.Register("Content", typeof(object), typeof(RightPane), null);

        public static readonly DependencyProperty Button2CommandProperty =
            DependencyProperty.Register("Button2Command", typeof(ICommand), typeof(RightPane),
            new PropertyMetadata(null, new PropertyChangedCallback(SetButton2Command)));

        public string Title
        {
            get { return (string)GetValue(TitleProperty); }
            set { SetValue(TitleProperty, value); }
        }

        public new object Content
        {
            get { return (object)GetValue(ContentProperty); }
            set { SetValue(ContentProperty, value); }
        }

        private void OnSetRightPaneMode(RightPaneMode rightPaneMode)
        {
            switch (rightPaneMode)
            {
                case RightPaneMode.PrintSettingsVisible:
                {
                    VisualStateManager.GoToState(this, "PrintSettingsVisibleState", true);
                    break;
                }

                case RightPaneMode.PrintSettingOptionsVisible:
                {
                    VisualStateManager.GoToState(this, "PrintSettingOptionsVisibleState", true);
                    break;
                }
            }
        }

        public ICommand Button2Command
        {
            get { return (ICommand)GetValue(Button2CommandProperty); }
            set { SetValue(Button2CommandProperty, value); }
        }

        private static void SetButton2Command(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if ((ICommand)e.NewValue != null)
            {
                ((RightPane)obj).button2.Command = (ICommand)e.NewValue;
            }
        }
    }
}

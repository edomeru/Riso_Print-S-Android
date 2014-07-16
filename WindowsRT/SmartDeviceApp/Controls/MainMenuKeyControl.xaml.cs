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
    public sealed partial class MainMenuKeyControl : UserControl
    {
        /// <summary>
        /// Constructor for MainMenuKeyControl.
        /// </summary>
        public MainMenuKeyControl()
        {
            this.InitializeComponent();
        }

        public static readonly DependencyProperty GroupNameProperty =
            DependencyProperty.Register("GroupName", typeof(string), typeof(MainMenuKeyControl), null);

        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(MainMenuKeyControl), null);

        public static readonly DependencyProperty SeparatorVisibilityProperty =
            DependencyProperty.Register("SeparatorVisibility", typeof(Visibility), typeof(MainMenuKeyControl), null);

        public static readonly DependencyProperty CommandProperty =
            DependencyProperty.Register("Command", typeof(ICommand), typeof(MainMenuKeyControl), null);

        public static readonly DependencyProperty IsCheckedProperty =
           DependencyProperty.Register("IsChecked", typeof(bool), typeof(MainMenuKeyControl), null);

        /// <summary>
        /// Name of the group property.
        /// </summary>
        public string GroupName
        {
            get { return (string)GetValue(GroupNameProperty); }
            set { SetValue(GroupNameProperty, value); }
        }

        /// <summary>
        /// Key Text property of the control.
        /// </summary>
        public string Text
        {
            get { return (string)GetValue(TextProperty); }
            set { SetValue(TextProperty, value); }
        }

        /// <summary>
        /// Visibility property for the separator line.
        /// </summary>
        public Visibility SeparatorVisibility
        {
            get { return (Visibility)GetValue(SeparatorVisibilityProperty); }
            set { SetValue(SeparatorVisibilityProperty, value); }
        }

        /// <summary>
        /// Command to be executed when the control is pressed.
        /// </summary>
        public ICommand Command
        {
            get { return (ICommand)GetValue(CommandProperty); }
            set { SetValue(CommandProperty, value); }
        }

        /// <summary>
        /// Flag to check whether the control is checked.
        /// </summary>
        public bool IsChecked
        {
            get { return (bool)GetValue(IsCheckedProperty); }
            set { SetValue(IsCheckedProperty, value); }
        }
    }
}

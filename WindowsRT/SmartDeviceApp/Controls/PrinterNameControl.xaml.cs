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

// The User Control item template is documented at http://go.microsoft.com/fwlink/?LinkId=234236

namespace SmartDeviceApp.Controls
{
    public sealed partial class PrinterNameControl : UserControl
    {
        public PrinterNameControl()
        {
            this.InitializeComponent();
        }

        public static readonly DependencyProperty LeftButtonVisibilityProperty =
            DependencyProperty.Register("LeftButtonVisibility", typeof(Visibility), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty RightButtonVisibilityProperty =
            DependencyProperty.Register("RightButtonVisibility", typeof(Visibility), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty IconVisibilityProperty =
            DependencyProperty.Register("IconVisibility", typeof(Visibility), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty ValueVisibilityProperty =
            DependencyProperty.Register("ValueVisibility", typeof(Visibility), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty ValueContentProperty =
            DependencyProperty.Register("ValueContent", typeof(object), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty IconImageProperty =
            DependencyProperty.Register("IconImage", typeof(ImageSource), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty ValueTextProperty =
            DependencyProperty.Register("ValueText", typeof(string), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty SetFocusProperty =
            DependencyProperty.Register("SetFocus", typeof(bool), typeof(PrinterNameControl),
            new PropertyMetadata(false, FocusChanged));

        public static readonly DependencyProperty IsDefaultProperty =
            DependencyProperty.Register("IsDefault", typeof(bool), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty IsOnlineProperty =
            DependencyProperty.Register("IsOnline", typeof(bool), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty DeleteCommandProperty =
            DependencyProperty.Register("DeleteCommand", typeof(ICommand), typeof(PrinterNameControl), null);

        //public static readonly DependencyProperty SetVisualStateProperty =
        //    DependencyProperty.Register("SetVisualState", typeof(ICommand), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty PrinterIpProperty =
            DependencyProperty.Register("PrinterIp", typeof(string), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty WillPerformDeleteProperty =
            DependencyProperty.Register("WillPerformDelete", typeof(bool), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty WillBeDeletedProperty =
            DependencyProperty.Register("WillBeDeleted", typeof(bool), typeof(PrinterNameControl), null);

        //public int Index
        //{
        //    get { return (int)GetValue(IndexProperty); }
        //    set { SetValue(IndexProperty, value); }
        //}

        public string LeftButtonVisibility
        {
            get { return (string)GetValue(LeftButtonVisibilityProperty); }
            set { SetValue(LeftButtonVisibilityProperty, value); }
        }

        public string RightButtonVisibility
        {
            get { return (string)GetValue(RightButtonVisibilityProperty); }
            set { SetValue(RightButtonVisibilityProperty, value); }
        }

        public string IconVisibility
        {
            get { return (string)GetValue(IconVisibilityProperty); }
            set { SetValue(IconVisibilityProperty, value); }
        }

        public string ValueVisibility
        {
            get { return (string)GetValue(ValueVisibilityProperty); }
            set { SetValue(ValueVisibilityProperty, value); }
        }

        public object ValueContent
        {
            get { return (object)GetValue(ValueContentProperty); }
            set { SetValue(ValueContentProperty, value); }
        }

        public ImageSource IconImage
        {
            get { return (ImageSource)GetValue(IconImageProperty); }
            set { SetValue(IconImageProperty, value); }
        }

        public string Text
        {
            get { return (string)GetValue(TextProperty); }
            set { SetValue(TextProperty, value); }
        }

        public string ValueText
        {
            get { return (string)GetValue(ValueTextProperty); }
            set { SetValue(ValueTextProperty, value); }
        }

        public bool SetFocus
        {
            get { return (bool)GetValue(SetFocusProperty); }
            set { SetValue(SetFocusProperty, value); }
        }


        public bool IsDefault
        {
            get { return (bool)GetValue(IsDefaultProperty); }
            set { SetValue(IsDefaultProperty, value); }
        }

        public bool IsOnline
        {
            get { return (bool)GetValue(IsOnlineProperty); }
            set { SetValue(IsOnlineProperty, value); }
        }

        private static void FocusChanged(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            var targetElement = obj.GetValue(ValueContentProperty) as Control;
            if (targetElement == null || e.NewValue == null || (!((bool)e.NewValue)))
            {
                return;
            }
            targetElement.Focus(FocusState.Programmatic);
            obj.SetValue(SetFocusProperty, false);
        }

        private void ContentPresenter_GotFocus(object sender, RoutedEventArgs e)
        {

        }

        public ICommand DeleteCommand
        {
            get { return (ICommand)GetValue(DeleteCommandProperty); }
            set { 
                SetValue(DeleteCommandProperty, value); 
            }
        }

        private ICommand _setVisualState;
        public ICommand SetVisualState
        {
            get
            {
                if (_setVisualState == null)
                {
                    _setVisualState = new SmartDeviceApp.Common.RelayCommand(
                        () => SetVisualStateExecute(),
                        () => true
                    );
                }
                return _setVisualState;
            }
        }

        private void SetVisualStateExecute()
        {
            if (IsDefault)
            {
                VisualStateManager.GoToState(this, "DefaultPrinterState", true);
            }
            else
            {
                VisualStateManager.GoToState(this, "NormalState", true);
            }
        }

        private ICommand _updateDeletionPerform;
        public ICommand UpdateDeletionPerform
        {
            get
            {
                if (_updateDeletionPerform == null)
                {
                    _updateDeletionPerform = new SmartDeviceApp.Common.RelayCommand(
                        () => UpdateDeletionPerformExecute(),
                        () => true
                    );
                }
                return _updateDeletionPerform;
            }
        }

        private void UpdateDeletionPerformExecute()
        {
            WillPerformDelete = !WillPerformDelete;
        }
        
        public string PrinterIp
        {
            get { return (string)GetValue(PrinterIpProperty); }
            set { SetValue(PrinterIpProperty, value); }
        }

        public bool WillPerformDelete
        {
            get { return (bool)GetValue(WillPerformDeleteProperty); }
            set { SetValue(WillPerformDeleteProperty, value); }
        }

        public bool WillBeDeleted
        {
            get { return (bool)GetValue(WillBeDeletedProperty); }
            set { SetValue(WillBeDeletedProperty, value); }
        }

    }
}

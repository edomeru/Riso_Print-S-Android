﻿using System;
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

namespace SmartDeviceApp.Controls
{
    public sealed partial class KeyTextBoxControl : KeyValueControl
    {
        public KeyTextBoxControl()
        {
            this.InitializeComponent();
            KeyTextBoxControl context = this;
            textBox.TextChanged += (sender, e) => TextChanged(sender, context);
        }

        public new static readonly DependencyProperty ValueTextProperty =
            DependencyProperty.Register("ValueText", typeof(string), typeof(KeyTextBoxControl), new PropertyMetadata(false, SetValueText));

        public new string ValueText
        {
            get { return (string)GetValue(ValueTextProperty); }
            set { SetValue(ValueTextProperty, value); }
        }

        // Updates the value source binding every time the text is changed
        private static void TextChanged(object sender, KeyTextBoxControl control)
        {
            control.SetValue(ValueTextProperty, ((TextBox)sender).Text);
        }  

        private static void SetValueText(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            ((KeyTextBoxControl)obj).textBox.Text = e.NewValue.ToString();
        }
    }
}

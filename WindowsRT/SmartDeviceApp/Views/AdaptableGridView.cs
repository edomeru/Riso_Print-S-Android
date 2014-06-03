﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;

namespace SmartDeviceApp.Views
{
    public class AdaptableGridView : GridView
    {
        // default itemWidth
        private const double itemWidth = 1000.00;
        public double ItemWidth
        {
            get { return (double)GetValue(ItemWidthProperty); }
            set { SetValue(ItemWidthProperty, value);
            ItemWidthWithMargin = ItemWidth + 20;
            }
        }
        public double ItemWidthWithMargin
        {
            get { return (double)GetValue(ItemWidthWithMarginProperty); }
            set { SetValue(ItemWidthWithMarginProperty, value); }
        }
        public static readonly DependencyProperty ItemWidthProperty =
            DependencyProperty.Register("ItemWidth", typeof(double), typeof(AdaptableGridView), null); //new PropertyMetadata(itemWidth)

        public static readonly DependencyProperty ItemWidthWithMarginProperty =
            DependencyProperty.Register("ItemWidthWithMargin", typeof(double), typeof(AdaptableGridView), null); //new PropertyMetadata(itemWidth)

        // default max number of rows or columns
        private const int maxRowsOrColumns = 3;
        public int MaxRowsOrColumns
        {
            get { return (int)GetValue(MaxRowColProperty); }
            set { SetValue(MaxRowColProperty, value); }
        }
        public static readonly DependencyProperty MaxRowColProperty =
            DependencyProperty.Register("MaxRowsOrColumns", typeof(int), typeof(AdaptableGridView), new PropertyMetadata(maxRowsOrColumns));


        public AdaptableGridView()
        {
            this.SizeChanged += MyGridViewSizeChanged;
            
        }

        private void MyGridViewSizeChanged(object sender, SizeChangedEventArgs e)
        {
            // Calculate the proper max rows or columns based on new size 
            this.MaxRowsOrColumns = this.ItemWidth > 0 ? Convert.ToInt32(Math.Floor(e.NewSize.Width / this.ItemWidth)) : maxRowsOrColumns;
        }
    }
}

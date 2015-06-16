using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using GalaSoft.MvvmLight.Command;
using Microsoft.Practices.ServiceLocation;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceApp.Views
{
    public class AdaptableGridView : GridView
    {
        // default itemWidth
        private const double itemWidth = 100.00;
        /// <summary>
        /// Holds the width of the griditem without margins.
        /// </summary>
        public double ItemWidth
        {
            get { return (double)GetValue(ItemWidthProperty); }
            set { SetValue(ItemWidthProperty, value);
            ItemWidthWithMargin = ItemWidth + 20;
            }
        }

        /// <summary>
        /// Holds the width of the griditem plus margins.
        /// </summary>
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

        /// <summary>
        /// Holds the Maximum number of rows and/or columns. 
        /// </summary>
        public int MaxRowsOrColumns
        {
            get { return (int)GetValue(MaxRowColProperty); }
            set { SetValue(MaxRowColProperty, value); }
        }
        public static readonly DependencyProperty MaxRowColProperty =
            DependencyProperty.Register("MaxRowsOrColumns", typeof(int), typeof(AdaptableGridView), new PropertyMetadata(maxRowsOrColumns));


        /// <summary>
        /// Default constructor of AdaptableGridView.
        /// </summary>
        public AdaptableGridView()
        {
            this.SizeChanged += MyGridViewSizeChanged;
        }

        private void MyGridViewSizeChanged(object sender, SizeChangedEventArgs e)
        {
            //guess the default max rows and columns based on current window height/width
            this.ItemWidth = this.ItemWidth <= 0 ? 430 : this.ItemWidth;
            var viewControl = ServiceLocator.Current.GetInstance<ViewControlViewModel>();

            this.MaxRowsOrColumns = Convert.ToInt32(Math.Floor(viewControl.ScreenBound.Width / this.ItemWidth));
        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.Views
{
    public class PullToRefreshListView : ListView
    {
        private bool isPulling = false;
        private ScrollViewer ElementScrollViewer;
        private UIElement ElementRelease;
        private bool isHOlding = false;

        private Point initialpoint;

        protected override void OnApplyTemplate()
        {
            base.OnApplyTemplate();

            System.Diagnostics.Debug.WriteLine("Applying template");
            if (ElementScrollViewer != null)
            {
                //ElementScrollViewer.PointerMoved -= viewer_MouseMove;
                
                ElementScrollViewer.ManipulationCompleted -= viewer_ManipulationCompleted;
            }
            ElementScrollViewer = GetTemplateChild("ScrollViewer") as ScrollViewer;
            if (ElementScrollViewer != null)
            {

                ElementScrollViewer.ManipulationMode = Windows.UI.Xaml.Input.ManipulationModes.All;
                //ElementScrollViewer.ManipulationStarting += viewer_ManipulationStarting;
                //ElementScrollViewer.ViewChanging += viewer_Changing;
                //ElementScrollViewer.ViewChanged += viewer_ViewChanged;
                //ElementScrollViewer.PointerMoved += viewer_MouseMove;
                //ElementScrollViewer.PointerPressed += viewer_MousePressed;
                //ElementScrollViewer.PointerReleased += viewer_MouseReleased;
                ElementScrollViewer.ManipulationCompleted += viewer_ManipulationCompleted;
                //ElementScrollViewer.Holding += viewer_Holding;
                ElementScrollViewer.ManipulationDelta += viewer_ManipulationDelta;
                ElementScrollViewer.ManipulationStarted += viewer_ManipulationStarted;
            }

            ElementRelease = GetTemplateChild("ReleaseElement") as UIElement;


           
            //ChangeVisualState(false);
        }

        private void viewer_ManipulationStarted(object sender, Windows.UI.Xaml.Input.ManipulationStartedRoutedEventArgs e)
        {
            initialpoint = e.Position;
            System.Diagnostics.Debug.WriteLine("Manipulation Started");
        }

        private void viewer_ManipulationDelta(object sender, Windows.UI.Xaml.Input.ManipulationDeltaRoutedEventArgs e)
        {
            System.Diagnostics.Debug.WriteLine("Manipulation Delta");
            Point currentpoint = e.Position;
            if (currentpoint.Y > initialpoint.Y) { 
            this.Margin = new Thickness(0, e.Position.Y, 0, 0);
                
            if (e.IsInertial)
            {
                
                if (currentpoint.Y - initialpoint.Y >= 10)//500 is the threshold value, where you want to trigger the swipe right event
                {
                    System.Diagnostics.Debug.WriteLine("Swipe Down");
                    isPulling = true;
                    e.Complete();
                }

                
            }
            }
        }

        //private void viewer_Holding(object sender, Windows.UI.Xaml.Input.HoldingRoutedEventArgs e)
        //{
        //    System.Diagnostics.Debug.WriteLine("List View Holding");
        //    isHOlding = true;
        //}

        //private void viewer_MousePressed(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs e)
        //{
        //    System.Diagnostics.Debug.WriteLine("List View Mouse Pressed");
        //    isPulling = true;
        //}

        //private void viewer_MouseReleased(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs e)
        //{
        //    //System.Diagnostics.Debug.WriteLine("List View Mouse Released");
        //    isPulling = false;
        //    //isHOlding = false;
        //}



        //protected override void OnPointerPressed(Windows.UI.Xaml.Input.PointerRoutedEventArgs e)
        //{
        //    base.OnPointerPressed(e);
        //    System.Diagnostics.Debug.WriteLine("Pointer pressed");
        //}


        //protected override void OnManipulationStarting(Windows.UI.Xaml.Input.ManipulationStartingRoutedEventArgs e)
        //{
        //    base.OnManipulationStarting(e);
        //    System.Diagnostics.Debug.WriteLine("ListView Manipulation starting");

        //}


        //protected override void OnManipulationStarted(Windows.UI.Xaml.Input.ManipulationStartedRoutedEventArgs e)
        //{
        //    base.OnManipulationStarted(e);
        //    System.Diagnostics.Debug.WriteLine("ListView Manipulation started");
        //}

        

        //protected override void OnManipulationCompleted(Windows.UI.Xaml.Input.ManipulationCompletedRoutedEventArgs e)
        //{
        //    base.OnManipulationCompleted(e);
        //    System.Diagnostics.Debug.WriteLine("ListView Manipulation completed");
        //}

        //protected override void OnPointerMoved(Windows.UI.Xaml.Input.PointerRoutedEventArgs e)
        //{
        //    base.OnPointerMoved(e);
        //    System.Diagnostics.Debug.WriteLine("ListView Pointer Moved");
        //    System.Diagnostics.Debug.WriteLine(e.GetCurrentPoint(ElementRelease).Position);
        //}

        private void viewer_ManipulationCompleted(object sender, Windows.UI.Xaml.Input.ManipulationCompletedRoutedEventArgs e)
        {
            System.Diagnostics.Debug.WriteLine("Pull to refresh Manipulation Completed");

            this.Margin = new Thickness(0, 0, 0, 0);
            if (isPulling)
            {
                Messenger.Default.Send<PrinterSearchRefreshState>(PrinterSearchRefreshState.RefreshingState);
            }
        }

    }
}

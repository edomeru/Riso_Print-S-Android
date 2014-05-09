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
using Windows.UI.Xaml.Shapes;
using Windows.UI.Xaml.Controls.Primitives;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Common.Utilities;

namespace SmartDeviceApp.Views
{
    public class PullToRefreshListView : ListView
    {
        public ScrollViewer ElementScrollViewer;
        private Rectangle ElementRelease;
        
        private bool canRefresh;
        private bool isPulling;

        private double offset;

        private Point initialpoint;

        private ScrollBar verticalScrollBar;

        protected override void OnApplyTemplate()
        {
            base.OnApplyTemplate();

            System.Diagnostics.Debug.WriteLine("Applying template");
            if (ElementScrollViewer != null)
            {
                ElementScrollViewer.ManipulationStarted -= viewer_ManipulationStarted;
                ElementScrollViewer.ManipulationDelta -= viewer_ManipulationDelta;
                ElementScrollViewer.ManipulationCompleted -= viewer_ManipulationCompleted;
            }

            ElementRelease = GetTemplateChild("ReleaseElement") as Rectangle;
            ElementScrollViewer = GetTemplateChild("ScrollViewer") as ScrollViewer;
            
            if (ElementScrollViewer != null)
            {

                ElementScrollViewer.ManipulationMode = Windows.UI.Xaml.Input.ManipulationModes.TranslateY;

                ElementScrollViewer.ManipulationStarted += viewer_ManipulationStarted;
                ElementScrollViewer.ManipulationDelta += viewer_ManipulationDelta;
                ElementScrollViewer.ManipulationCompleted += viewer_ManipulationCompleted;
                
            }

        }

        //Sets the VerticalScrollBar of this listview. Taken when the pane is loaded. Called from the code behind of SearchPrinterPane.
        public void SetVBar(ScrollBar vBar)
        {
            if (vBar != null)
            {
                verticalScrollBar = vBar;
                
                offset = verticalScrollBar.Minimum;
            }
        }

        private void viewer_ManipulationStarted(object sender, Windows.UI.Xaml.Input.ManipulationStartedRoutedEventArgs e)
        {
            initialpoint = e.Position;
            System.Diagnostics.Debug.WriteLine("Manipulation Started");

            if (offset == verticalScrollBar.Minimum)
            {
                canRefresh = true;
            }
            else
            {
                canRefresh = false;
            }
        }

        
        private void viewer_ManipulationDelta(object sender, Windows.UI.Xaml.Input.ManipulationDeltaRoutedEventArgs e)
        {
            System.Diagnostics.Debug.WriteLine("Manipulation Delta");
            Point currentpoint = e.Position;

            System.Diagnostics.Debug.WriteLine("Initial Point");
            System.Diagnostics.Debug.WriteLine(initialpoint);
            System.Diagnostics.Debug.WriteLine("Current Point");
            System.Diagnostics.Debug.WriteLine(currentpoint);


            //manually scroll the scrollviewer
            if (currentpoint.Y > initialpoint.Y)
            {

                if (canRefresh)
                {
                    //pull to refresh
                    this.Margin = new Thickness(0, e.Position.Y, 0, 0);
                    
                    if (currentpoint.Y - initialpoint.Y >= 10)
                    {
                        System.Diagnostics.Debug.WriteLine("Swipe Down");
                        isPulling = true;
                        e.Complete();
                    }

                }
                else
                { 

                    //downwards
                    double addOffset = currentpoint.Y - initialpoint.Y;

                    offset -= addOffset;

                    if (offset < verticalScrollBar.Minimum)
                    {
                        offset = verticalScrollBar.Minimum;
                    }

                    isPulling = false;
                    ElementScrollViewer.ChangeView(0, offset, null);
                    ElementScrollViewer.UpdateLayout();
                }
            }
            else
            {
                //upwards
                double subOffset = initialpoint.Y - currentpoint.Y;

                offset += subOffset;

                

                if (offset > verticalScrollBar.Maximum)
                {
                    offset = verticalScrollBar.Maximum;
                }

                isPulling = false;
                ElementScrollViewer.ChangeView(0, offset, null);
                ElementScrollViewer.UpdateLayout();
            }
        }



        private void viewer_ManipulationCompleted(object sender, Windows.UI.Xaml.Input.ManipulationCompletedRoutedEventArgs e)
        {
            this.Margin = new Thickness(0, 0, 0, 0);   
            if (isPulling)
            {
                if (NetworkController.IsConnectedToNetwork)
                {
                    Messenger.Default.Send<PrinterSearchRefreshState>(PrinterSearchRefreshState.RefreshingState);
                }
                else
                {
                    var loader = new Windows.ApplicationModel.Resources.ResourceLoader();
                    DisplayMessage(loader.GetString("IDS_LBL_SEARCH_PRINTERS"), loader.GetString("IDS_ERR_MSG_NETWORK_ERROR"));
                }
            }
        }

        public void DisplayMessage(string caption, string content)
        {
            MessageAlert ma = new MessageAlert();
            ma.Caption = caption;
            ma.Content = content;
            Messenger.Default.Send<MessageAlert>(ma);
        }


    }
}

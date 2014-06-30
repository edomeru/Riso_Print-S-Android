using System;
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

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234238

namespace SNMP
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MainPage : Page
    {
        //SNMP snmp = new SNMP();

        public MainPage()
        {
            this.InitializeComponent();
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
            //UDPHelper u = new UDPHelper();
            //u.process();

            

            //SNMPDevice testdevice = new SNMPDevice("192.168.1.24");
            //testdevice.beginRetrieveCapabilities();

            SNMPDiscovery testdiscovery = new SNMPDiscovery(SNMPConstants.DEFAULT_COMMUNITY_NAME,"255.255.255.255");
            testdiscovery.startDiscover();

            return;
        }
    }
}

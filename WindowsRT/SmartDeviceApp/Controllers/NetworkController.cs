using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Net.NetworkInformation;
using Windows.Networking.Sockets;
using Windows.Networking;
using SNMP;
using Windows.Storage.Streams;
using System.Threading;
using Windows.Networking.Connectivity;

namespace SmartDeviceApp.Controllers
{
    public class NetworkController
    {

        public Action<string, bool> networkControllerPingStatusCallback { get; set; } //PrintersModule

        static readonly NetworkController _instance = new NetworkController();

        static NetworkController() { }

        private NetworkController() { }

        public static NetworkController Instance
        {
            get { return _instance; }
        }

        StreamSocket tcpClient;

        public async Task pingDevice(string ip)
        {
           try
           {
                tcpClient = new StreamSocket();

                string port = "80";
               
                await tcpClient.ConnectAsync(new Windows.Networking.HostName(ip), port, SocketProtectionLevel.PlainSocket);
                   
                networkControllerPingStatusCallback(ip, true);
                   
           }
           catch(Exception e)
           {
               networkControllerPingStatusCallback(ip, false);
           }
        }
       
        public static bool IsConnectedToNetwork
        {
            get
            {
                var profile = NetworkInformation.GetInternetConnectionProfile();
                if (profile != null)
                {
                    var interfaceType = profile.NetworkAdapter.IanaInterfaceType;
                    return interfaceType == 71 || interfaceType == 6;
                }
                return false;
            }
        }


    }
}

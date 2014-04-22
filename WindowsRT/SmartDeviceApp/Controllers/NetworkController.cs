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

namespace SmartDeviceApp.Controllers
{
    class NetworkController
    {

        public Action<string, bool> networkControllerPingStatusCallback { get; set; } //PrintersModule
        private IOutputStream outputStream;

        static readonly NetworkController _instance = new NetworkController();

        static NetworkController() { }

        private NetworkController() { }

        public static NetworkController Instance
        {
            get { return _instance; }
        }

        DatagramSocket udpClient;
        public void Initialize()
        {
            
        }

        StreamSocket tcpClient;

        public async Task pingDevice(string ip)
        {
           try
           {
                   


                   //await tcpClient.ConnectAsync(
                   //    new Windows.Networking.HostName(ip),
                   //    "161",
                   ////    SocketProtectionLevel.PlainSocket);
                   //udpClient = new DatagramSocket();
                   //udpClient.MessageReceived += handlePing;

                    tcpClient = new StreamSocket();
                    
                   await tcpClient.ConnectAsync(new Windows.Networking.HostName(ip), "8080");
                   //await startTimer();
                   
                   //await udpClient.ConnectAsync(new Windows.Networking.HostName("192.168.0.199"), "161");
                   //var localIp = udpClient.Information.LocalAddress.DisplayName;
                   //var remoteIp = udpClient.Information.RemoteAddress.DisplayName;
                   //outputStream = await udpClient.GetOutputStreamAsync(new Windows.Networking.HostName(ip), "161");
                   //DataWriter writer = new DataWriter(tcpClient.OutputStream);
                   //writer.WriteString("Hello");
                   //await writer.StoreAsync();
                   //writer.DetachStream();
                   //writer.Dispose();


                   //DataReader reader = new DataReader(tcpClient.InputStream);
                   
                   //ConnectionAttemptInformation = String.Format("Success, remote server contacted at IP address {0}",
                   //                                             remoteIp);
                   //udpClient.Dispose();
                       networkControllerPingStatusCallback(ip, true);
                   
           }
           catch(Exception e)
           {
               



               networkControllerPingStatusCallback(ip, false);
           }
        }

        private bool isTimerRunning;
        private async Task startTimer()
        {
            this.isTimerRunning = true;

            ////while (this.isTimerRunning)
            //{
                await Task.Delay(3000);
                if (tcpClient != null) tcpClient.Dispose();
                throw new TimeoutException();
                //stopTimer();

            //}
        }

        private void stopTimer()
        {
            this.isTimerRunning = false;
        }

        private void handlePing(DatagramSocket sender, DatagramSocketMessageReceivedEventArgs args)
        {
            //if there is a reply, it is online. 0.199 did not reply.
            System.Diagnostics.Debug.WriteLine("HandlePing");
            //networkControllerPingStatusCallback(ip, true);
        }


    }
}

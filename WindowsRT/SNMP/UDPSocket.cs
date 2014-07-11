using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Networking;
using Windows.Networking.Connectivity;
using Windows.Networking.Sockets;
using Windows.Storage.Streams;

namespace SNMP
{
    class UDPSocket
    {
        private ConnectionProfile connectionProfile = NetworkInformation.GetInternetConnectionProfile();
        private DataWriter writer;
        private IOutputStream outputStream;

        private DatagramSocket udpSocket;

        Windows.Foundation.TypedEventHandler<HostName, byte[]> dataReceivedHandler = null;
        Windows.Foundation.TypedEventHandler<HostName, byte[]> timeoutHandler = null;

        internal void assignDelegate(Windows.Foundation.TypedEventHandler<HostName, byte[]> d)
        {
            dataReceivedHandler = d;
        }

        internal void assignTimeoutDelegate(Windows.Foundation.TypedEventHandler<HostName, byte[]> t)
        {
            timeoutHandler = t;
        }

        private void socket_MessageReceived(DatagramSocket sender, DatagramSocketMessageReceivedEventArgs args)
        {
            stopTimer();
            datacounter++;

            var r = args.GetDataReader();
            uint l = r.UnconsumedBufferLength;
            var buff = new byte[l];
            r.ReadBytes(buff);
            //NotifyUserFromAsyncThread("received : " + l + " bytes");            

            if (dataReceivedHandler != null) dataReceivedHandler(args.RemoteAddress, buff);
        }

        internal void close()
        {
            if (udpSocket != null)
                udpSocket.Dispose();
            //throw new NotImplementedException();
        }

        internal void beginReceiving()
        {
            //throw new NotImplementedException();
        }

        private int datacounter = 0;
        internal async void sendData(byte[] data, string ipAddress, byte port, byte timeout, int p3)
        {
            datacounter = 0;
            HostName host = null;
            try
            {
                host = new HostName(ipAddress);
            }
            catch (Exception e)
            {
                if (timeoutHandler != null) timeoutHandler(host, null);
                return;
            }
            //HostName host = new HostName("255.255.255.255");
            //HostName host = new HostName("224.0.0.1");            
            //byte[] msg = getSNMPPacket("GET", "public", "1.3.6.1.2.1.1.1.0");//desc 1.3.6.1.2.1.1.1.0 get desc
            //byte[] msg = getSNMPPacket("GET", "public", "1.3.6.1.2.1.43.15.1.1.2");


            udpSocket = new DatagramSocket();
            udpSocket.MessageReceived += socket_MessageReceived;


            string p = port.ToString();
            //await udpSocket.BindServiceNameAsync("", connectionProfile.NetworkAdapter);



            if (true)
            {
                //EndpointPair endpoint = new EndpointPair(null, "", host, p);
                try
                {
                    outputStream = await udpSocket.GetOutputStreamAsync(host, p);
                }
                catch (Exception e)
                {
                    if (timeoutHandler != null) timeoutHandler(host, null);
                    return;
                }

                //outputStream = await udpSocket.GetOutputStreamAsync(endpoint);
                writer = new DataWriter(outputStream);
                writer.WriteBytes(data);
                await writer.StoreAsync();
            }
            else
            {
                //await udpSocket.BindEndpointAsync(new HostName("127.0.0.1"), "0");
                await udpSocket.ConnectAsync(host, p);
                writer = new DataWriter(udpSocket.OutputStream);
                writer.WriteBytes(data);
                await writer.StoreAsync();
            }
            
            startTimer(timeout, host);
        }


        private bool isTimerRunning;
        private async void startTimer(byte timeout, HostName host)
        {
            this.isTimerRunning = true;

            await Task.Delay(timeout * 1000);
            if (this.isTimerRunning)
            {
                //HostName host = udpSocket.Information.RemoteAddress;
                //if (writer != null) writer.Dispose();
                //if (outputStream != null) outputStream.Dispose();
                //if (udpSocket != null) udpSocket.Dispose();
                stopTimer();

                if (timeoutHandler != null) timeoutHandler(host, null);
            }
        }

        private void stopTimer()
        {
            this.isTimerRunning = false;
        }
    }
}

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

        private bool errorOccurred = false;

        Windows.Foundation.TypedEventHandler<HostName, byte[]> dataReceivedHandler = null;
        Windows.Foundation.TypedEventHandler<HostName, byte[]> timeoutHandler = null;
        Windows.Foundation.TypedEventHandler<HostName, byte[]> errorHandler = null;


        internal void assignDelegate(Windows.Foundation.TypedEventHandler<HostName, byte[]> d)
        {
            dataReceivedHandler = d;
        }

        internal void assignTimeoutDelegate(Windows.Foundation.TypedEventHandler<HostName, byte[]> t)
        {
            timeoutHandler = t;
        }

        internal void assignErrorDelegate(Windows.Foundation.TypedEventHandler<HostName, byte[]> e)
        {
            errorHandler = e;
        }

        private void socket_MessageReceived(DatagramSocket sender, DatagramSocketMessageReceivedEventArgs args)
        {
            datacounter++;
            try
            {
                var r = args.GetDataReader();
                uint l = r.UnconsumedBufferLength;
                var buff = new byte[l];
                r.ReadBytes(buff);
                
                if (dataReceivedHandler != null) 
                    dataReceivedHandler(args.RemoteAddress, buff);
            }
            catch(Exception e)
            {
                if (errorHandler != null)
                {
                    var host = sender.Information.RemoteAddress;
                    errorOccurred = true;
                    errorHandler(host, null);
                }
            }
            
        }

        internal void close()
        {
            if (outputStream != null)
            {
                outputStream.Dispose();
                outputStream = null;
            }
                

            if (writer != null)
            {
                writer.Dispose();
                writer = null;
            }

            if (udpSocket != null)
                udpSocket.Dispose();
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

            udpSocket = new DatagramSocket();
            udpSocket.MessageReceived += socket_MessageReceived;
            
            string p = port.ToString();

            try
            {
                outputStream = await udpSocket.GetOutputStreamAsync(host, p);
            }
            catch (Exception e)
            {
                if (timeoutHandler != null) timeoutHandler(host, null);
                return;
            }

            try
            {
                writer = new DataWriter(outputStream);
                writer.WriteBytes(data);
                await writer.StoreAsync();
            }
            catch (Exception e)
            { 
                //When adding self, sendData is called even before a message is received, which will call errorHandler.
                //Thus this exception will be called when close() is called and sendData is still writing to the outputStream.
            }
        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Networking;
using Windows.Networking.Connectivity;
using Windows.Networking.Sockets;
using Windows.Storage.Streams;

namespace DirectPrint
{
    public class TCPSocket
    {

        private StreamSocket socket = null;

        private Windows.Foundation.TypedEventHandler<HostName, byte> dataReceivedHandler = null;
        private Windows.Foundation.TypedEventHandler<HostName, byte> timeoutHandler = null;

        private HostName h = null;
        private string port = "0";

        public TCPSocket(string _host, string _port, Windows.Foundation.TypedEventHandler<HostName, byte> d)
        {            
            socket = new StreamSocket();
            setHost(_host, _port);
            this.dataReceivedHandler = d;
        }

        //internal void assignDataReceivedDelegate(Windows.Foundation.TypedEventHandler<HostName, byte> d)
        //{
        //    dataReceivedHandler = d;
        //}

        internal void setHost(string _host, string _port)
        {
           h = new HostName(_host);
           port = _port;
        }

        internal async Task connect()
        {
            if (socket != null)
            {
                try
                {
                    await socket.ConnectAsync(h, port);
                }
                catch
                {                    
                    throw;
                }
            }
        }

        internal void disconnect()
        {
            if (socket != null)
            {
                socket.Dispose();
                socket = null;
            }
        }

        internal async Task read()
        {
            if (socket != null)
            try
            {
                DataReader reader = new DataReader(socket.InputStream);
                // Set inputstream options so that we don't have to know the data size
                await reader.LoadAsync(1);
                byte responseData = 0;
                responseData = reader.ReadByte();

                if (dataReceivedHandler != null)
                {
                    dataReceivedHandler(socket.Information.RemoteHostName, responseData);
                }

                reader.DetachStream();
            }
            catch (Exception exception)
            {
                /*
                // If this is an unknown status, 
                // it means that the error is fatal and retry will likely fail.
                if (SocketError.GetStatus(exception.HResult) == SocketErrorStatus.Unknown)
                {
                    throw;
                }

                StatusText.Text = "Receive failed with error: " + exception.Message;
                // Could retry, but for this simple example
                // just close the socket.

                closing = true;
                clientSocket.Dispose();
                clientSocket = null;
                connected = false;
                */
                return;
            }
        }

        internal async Task write(byte[] data, int a,int b)
        {
            try
            {
                if (socket != null)
                {
                    //if (socket.)

                    DataWriter writer = new DataWriter(socket.OutputStream);

                    byte[] data2 = new byte[b - a];
                    Array.Copy(data, a, data2, 0, b - a);

                    string test = System.Text.Encoding.UTF8.GetString(data2, 0, data2.Length);

                    writer.WriteBytes(data2);
                    //await writer.FlushAsync();
                    await writer.StoreAsync();
                    // detach the stream and close it
                    writer.DetachStream();
                    writer.Dispose();
                }
            }
            catch
            {
                throw;
            }
        }    
    }
}


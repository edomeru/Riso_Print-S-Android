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
    class TCPSocket
    {

        private StreamSocket socket = null;

        Windows.Foundation.TypedEventHandler<HostName, byte[]> dataReceivedHandler = null;
        Windows.Foundation.TypedEventHandler<HostName, byte[]> timeoutHandler = null;

        public TCPSocket()
        {
            socket = new StreamSocket();
        }

        public TCPSocket(string host, string port, Windows.Foundation.TypedEventHandler<HostName, byte[]> d)
        {            
            socket = new StreamSocket();
            this.connect(host, port);
            this.dataReceivedHandler = d;
        }

        internal void assignDelegate(Windows.Foundation.TypedEventHandler<HostName, byte[]> d)
        {
            dataReceivedHandler = d;
        }

        internal async void connect(string host, string port)
        {
            HostName h = new HostName(host);
            await socket.ConnectAsync(h, port);
        }

        internal async void write(byte[] data, int a,int b, bool waitresponse = true)
        {
            if (socket != null)
            {
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

                // Now try to receive data from server
                if (waitresponse)
                try
                {
                    DataReader reader = new DataReader(socket.InputStream);
                    // Set inputstream options so that we don't have to know the data size
                    await reader.LoadAsync(1);
                    byte[] responseData = new byte[1];
                    reader.ReadBytes(responseData);

                    if (dataReceivedHandler != null){
                        dataReceivedHandler(socket.Information.RemoteHostName,responseData);
                    }
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
            
        }    
    }
}


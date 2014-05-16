using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using DirectPrint;
using Windows.Storage;
using SmartDeviceApp.Common.Utilities;
using DirectPrint;
using Windows.Networking;

namespace SmartDeviceAppTests.DirectPrintTest
{
    [TestClass]
    public class TCPSocketTest
    {
        private TCPSocket _tcpSocket;

        [TestMethod]
        public void Test_TCPSocket()
        {
            var host = "192.168.1.206";
            var port = "515";
            var directPrint = new DirectPrint.DirectPrint();
            var receiveData = new Windows.Foundation.TypedEventHandler<HostName, byte>(directPrint.receiveData);
            _tcpSocket = new TCPSocket(host, port, receiveData);
            Assert.IsNotNull(_tcpSocket);
        }

    }
}

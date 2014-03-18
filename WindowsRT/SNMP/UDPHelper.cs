using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Networking;
using Windows.Networking.Connectivity;
using Windows.Networking.Sockets;
using Windows.Storage.Streams;
using Windows.UI.Core;

namespace SNMP
{
    public class UDPHelper
    {
        private DatagramSocket socket;
        private DataWriter writer;
        private IOutputStream outputStream;

        public UDPHelper()
        {

        }


        ConnectionProfile connectionProfile = NetworkInformation.GetInternetConnectionProfile();
        public async void process()
        {
            //HostName host = new HostName("192.168.1.216");
            //HostName host = new HostName("192.168.1.24");
            //HostName host = new HostName("192.168.1.255");
            //HostName host = new HostName("192.168.255.255");
            //HostName host = new HostName("255.255.255.255");
            //HostName host = new HostName("224.0.0.1");//multicast ip
            //HostName host = new HostName("FF01:0:0:0:0:0:0:1");//multicast ipv6
            HostName host = new HostName("ff02::1");//multicast ipv6            
            //HostName host = new HostName("fe80::220:6bff:fedd:48f3");//multicast ipv6
            

            byte[] msg = getSNMPPacket("GET", "public", "1.3.6.1.2.1.1.1.0");//desc 1.3.6.1.2.1.1.1.0 get desc
            //byte[] msg = getSNMPPacket("GET", "public", "1.3.6.1.2.1.1.6");// Send a sysLocation SNMP request "1.3.6.1.2.1.1.6"
            //byte[] msg = getSNMPPacket("GET", "public", "1.3.6.1.2.1.1.5.0");
            //byte[] msg = getSNMPPacket("GET", "public", "1.3.6.1.2.1.1.5");
            
            socket = new DatagramSocket();
            socket.MessageReceived += socket_MessageReceived;
            
            /*
            await socket.ConnectAsync(host, "161");
            writer = new DataWriter(socket.OutputStream);
            writer.WriteBytes(msg);
            await writer.StoreAsync();
             */


            await socket.BindServiceNameAsync("", connectionProfile.NetworkAdapter);

            //multicast ip 224.0.0.1
            //socket.JoinMulticastGroup(host);
            outputStream = await socket.GetOutputStreamAsync(host, "161");

            writer = new DataWriter(outputStream);
            writer.WriteBytes(msg);
            await writer.StoreAsync();
        }


        private void socket_MessageReceived(DatagramSocket sender, DatagramSocketMessageReceivedEventArgs args)
        {
            var r = args.GetDataReader();
            uint l = r.UnconsumedBufferLength;
            var buff = new byte[l];
            r.ReadBytes(buff);
            NotifyUserFromAsyncThread("received : " + l + " bytes");
            writer.Dispose();
            //socket.Dispose();            

            

            

            //parse MIB
            int commlength, miblength, datatype, datalength, datastart;
            int uptime = 0;

            // If response, get the community name and MIB lengths
            commlength = Convert.ToInt16(buff[6]);
            miblength = Convert.ToInt16(buff[23 + commlength]);

            // Extract the MIB data from the SNMP response
            datatype = Convert.ToInt16(buff[24 + commlength + miblength]);
            datalength = Convert.ToInt16(buff[25 + commlength + miblength]);
            datastart = 26 + commlength + miblength;

            /*output = Encoding.UTF8.GetString(response, datastart, datalength);
            Debug.WriteLine("  sysName - Datatype: {0}, Value: {1}",
                    datatype, output);
            */

            string response = System.Text.Encoding.UTF8.GetString(buff, datastart, datalength);
            //string response = System.Text.Encoding.UTF8.GetString(buff, 0, (int)l);
            System.Diagnostics.Debug.WriteLine(sender.Information.RemoteAddress);
            System.Diagnostics.Debug.WriteLine(response);
            
        }


        private void NotifyUserFromAsyncThread(string strMessage)
        {
            
        }


        public byte[] getSNMPPacket(string request, string community, string mibstring)
        {
            byte[] packet = new byte[1024];
            byte[] mib = new byte[1024];
            int snmplen;
            int comlen = community.Length;
            string[] mibvals = mibstring.Split('.');
            int miblen = mibvals.Length;
            int cnt = 0, temp, i;
            int orgmiblen = miblen;
            int pos = 0;

            // Convert the string MIB into a byte array of integer values
            // Unfortunately, values over 128 require multiple bytes
            // which also increases the MIB length
            for (i = 0; i < orgmiblen; i++)
            {
                temp = Convert.ToInt16(mibvals[i]);
                if (temp > 127)
                {
                    mib[cnt] = Convert.ToByte(128 + (temp / 128));
                    mib[cnt + 1] = Convert.ToByte(temp - ((temp / 128) * 128));
                    cnt += 2;
                    miblen++;
                }
                else
                {
                    mib[cnt] = Convert.ToByte(temp);
                    cnt++;
                }
            }
            snmplen = 29 + comlen + miblen - 1;  //Length of entire SNMP packet

            //The SNMP sequence start
            packet[pos++] = 0x30; //Sequence start
            packet[pos++] = Convert.ToByte(snmplen - 2);  //sequence size

            //SNMP version
            packet[pos++] = 0x02; //Integer type
            packet[pos++] = 0x01; //length
            packet[pos++] = 0x00; //SNMP version 1

            //Community name
            packet[pos++] = 0x04; // String type
            packet[pos++] = Convert.ToByte(comlen); //length
            //Convert community name to byte array
            byte[] data = Encoding.UTF8.GetBytes(community);
            for (i = 0; i < data.Length; i++)
            {
                packet[pos++] = data[i];
            }

            //Add GetRequest or GetNextRequest value
            if (request == "get")
                packet[pos++] = 0xA0;
            else
                packet[pos++] = 0xA1;

            packet[pos++] = Convert.ToByte(20 + miblen - 1); //Size of total MIB

            //Request ID
            packet[pos++] = 0x02; //Integer type
            packet[pos++] = 0x04; //length
            packet[pos++] = 0x00; //SNMP request ID
            packet[pos++] = 0x00;
            packet[pos++] = 0x00;
            packet[pos++] = 0x01;

            //Error status
            packet[pos++] = 0x02; //Integer type
            packet[pos++] = 0x01; //length
            packet[pos++] = 0x00; //SNMP error status

            //Error index
            packet[pos++] = 0x02; //Integer type
            packet[pos++] = 0x01; //length
            packet[pos++] = 0x00; //SNMP error index

            //Start of variable bindings
            packet[pos++] = 0x30; //Start of variable bindings sequence

            packet[pos++] = Convert.ToByte(6 + miblen - 1); // Size of variable binding

            packet[pos++] = 0x30; //Start of first variable bindings sequence
            packet[pos++] = Convert.ToByte(6 + miblen - 1 - 2); // size
            packet[pos++] = 0x06; //Object type
            packet[pos++] = Convert.ToByte(miblen - 1); //length

            //Start of MIB
            packet[pos++] = 0x2b;
            //Place MIB array in packet
            for (i = 2; i < miblen; i++)
                packet[pos++] = Convert.ToByte(mib[i]);
            packet[pos++] = 0x05; //Null object value
            packet[pos++] = 0x00; //Null


            //Send packet to destination
            //
            ///

            return packet;
        }

    }
}

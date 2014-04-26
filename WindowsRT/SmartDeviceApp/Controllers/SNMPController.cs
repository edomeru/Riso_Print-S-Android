using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.System.Threading;
using SNMP;
using SmartDeviceApp.Models;

namespace SmartDeviceApp.Controllers
{
    class SNMPController
    {
        public Action<string, bool> printerControllerGetStatusCallback { get; set; } //PrintersModule
        public Action<string, string, bool, List<string>> printerControllerAddPrinterCallback { get; set; }
        public Action<PrinterSearchItem> printerControllerDiscoverCallback { get; set; }
        public Action<string> printerControllerTimeout { get; set; }

        public List<Printer> printerList {get; set;}
        private bool waiting;


        /**
         * Get Online Status functions
         * 
         * */

        public void getStatus(string ip)
        {
            System.Diagnostics.Debug.WriteLine("SNMPController get status of: ");
            System.Diagnostics.Debug.WriteLine(ip);
            SNMPDevice device = new SNMPDevice(ip);
            waiting = true;
            device.snmpControllerCallBackGetStatus = new Action<string, bool>(handlePrinterStatus);
            device.beginRetrieveCapabilities();            
        }

        private void handlePrinterStatus(string ip, bool isOnline)
        {
            waiting = false;
            System.Diagnostics.Debug.WriteLine("SNMPController callback for ip: ");
            System.Diagnostics.Debug.WriteLine(ip);
            printerControllerGetStatusCallback(ip, isOnline);
        }

        /**
         * Add functions
         * 
         * */

        public void getDevice(string ip)
        {
            //SNMPDevice device = new SNMPDevice(ip);
            //waiting = true;
            //device.snmpControllerCallBackGetCapability = new Action<string, string, bool, List<string>>(handleGetCapability);
            //device.beginRetrieveCapabilities();

            SNMPDiscovery discovery = new SNMPDiscovery("public", ip);
            discovery.FromPrinterSearch = false;
            discovery.snmpControllerDiscoverCallback = new Action<SNMPDevice>(handleGetDevice);
            discovery.snmpControllerDiscoverTimeOut = new Action<string>(handleTimeout);
            discovery.startDiscover();
        }

        private void handleGetDevice(SNMPDevice device)
        {
            System.Diagnostics.Debug.WriteLine("Name(in SNMPController): ");
            System.Diagnostics.Debug.WriteLine(device.getSysName());
            printerControllerAddPrinterCallback(device.getIpAddress(), device.getSysName(), true, device.getCapabilities());
        }

        


        /**
         * Scan functions
         * 
         * */

        public void  startDiscover()
        {
            SNMPDiscovery discovery = new SNMPDiscovery("public", "192.168.0.255");
            //add callback
            discovery.FromPrinterSearch = true;
            discovery.snmpControllerDiscoverCallback = new Action<SNMPDevice>(handleDeviceDiscovered);
            discovery.snmpControllerDiscoverTimeOut = new Action<string>(handleTimeout);
            discovery.startDiscover();
        }

        private void handleDeviceDiscovered(SNMPDevice device)
        {
            //update from SNMPDevice to Printer 
            PrinterSearchItem printer = new PrinterSearchItem();

            printer.Ip_address = device.getIpAddress();
            printer.Name = device.getSysName();
            //call callback function to PrinterController
            printerControllerDiscoverCallback(printer);
        }

        /**
         * Timeout
         * 
         * */
        private void handleTimeout(string ip)
        {
            printerControllerTimeout(ip);
        }

        

    }
}

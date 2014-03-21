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

        public void getCapability(string ip)
        {
            SNMPDevice device = new SNMPDevice(ip);
            waiting = true;
            device.snmpControllerCallBackGetCapability = new Action<string, string, bool, List<string>>(handleGetCapability);
            device.beginRetrieveCapabilities();
        }

        private void handleGetCapability(string ip, string name, bool isOnline, List<string> capabilities)
        {
            printerControllerAddPrinterCallback(ip, name, isOnline, capabilities);
        }


        /**
         * Scan functions
         * 
         * */

        public void  startDiscover()
        {
            SNMPDiscovery discovery = new SNMPDiscovery("public", "192.168.0.255");
            //add callback
            discovery.snmpControllerDiscoverCallback = new Action<SNMPDevice>(handleDeviceDiscovered);
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


    }
}

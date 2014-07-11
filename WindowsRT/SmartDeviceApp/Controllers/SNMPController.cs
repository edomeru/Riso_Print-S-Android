﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.System.Threading;
using SNMP;
using SmartDeviceApp.Models;

namespace SmartDeviceApp.Controllers
{
    public class SNMPController
    {
        public Action<string, bool> printerControllerGetStatusCallback { get; set; } //PrintersModule
        public Action<string, string, bool, bool, List<string>> printerControllerAddPrinterCallback { get; set; }
        public Action<PrinterSearchItem> printerControllerDiscoverCallback { get; set; }
        public Action<string> printerControllerTimeout { get; set; }
        public Action<string, string, List<string>> printerControllerAddTimeout { get; set; }

        public List<Printer> printerList {get; set;}
        private bool waiting;

        SNMPDiscovery _discovery;

        public SNMPDiscovery Discovery
        {
            get { return _discovery;}
            set {_discovery = value;}
        }

        static SNMPController() { }

        static readonly SNMPController _instance = new SNMPController();

        /// <summary>
        /// Singleton instance
        /// </summary>
        public static SNMPController Instance
        {
            get { return _instance; }
        }

        public void Initialize()
        {
            Discovery = new SNMPDiscovery("public", SNMPConstants.BROADCAST_ADDRESS);
            Discovery.snmpControllerDiscoverCallback = new Action<SNMPDevice>(handleDeviceDiscovered);
            Discovery.snmpControllerDiscoverTimeOut = new Action<string>(handleTimeout);
        }

        /**
         * Add functions
         * 
         * */

        public void getDevice(string ip)
        {
            SNMPDevice device = new SNMPDevice(ip);
            //waiting = true;
            device.snmpControllerCallBackGetCapability = new Action<SNMPDevice>(handleGetDevice);
            device.snmpControllerDeviceTimeOut = new Action<SNMPDevice>(handleAddTimeout);
            device.beginRetrieveCapabilities();
        }

        private void handleGetDevice(SNMPDevice device)
        {
            System.Diagnostics.Debug.WriteLine("Name(in SNMPController): ");
            System.Diagnostics.Debug.WriteLine(device.Description);
            printerControllerAddPrinterCallback(device.IpAddress, device.Description, true, device.isSupportedDevice, device.CapabilitiesList);
        }

        


        /**
         * Scan functions
         * 
         * */

        public void  startDiscover()
        {
            Discovery.FromPrinterSearch = true;
            Discovery.startDiscover();
        }

        private void handleDeviceDiscovered(SNMPDevice device)
        {
            //update from SNMPDevice to Printer 
            PrinterSearchItem printer = new PrinterSearchItem();

            printer.Ip_address = device.IpAddress;
            printer.Name = device.Description;
            //call callback function to PrinterController
            printerControllerDiscoverCallback(printer);
        }

        public Printer getPrinterFromSNMPDevice(string ip)
        {
            if (Discovery.SnmpDevices.Count > 0)
            {
                SNMPDevice device = Discovery.SnmpDevices.FirstOrDefault(x => x.IpAddress == ip);

                Printer printer = new Printer();
                printer.IpAddress = ip;
                printer.Name = device.Description;
                printer.IsOnline = true;
                if (device.CapabilitiesList.Count > 0)
                {
                    List<string> capabilitesList = device.CapabilitiesList;
                    printer.EnabledBookletFinishing = (capabilitesList.ElementAt(0) == "true");
                    // multifunction finisher 2/3 and 2/4 also has staple, so enable stapler when the multifunction finisher is available
                    printer.EnabledStapler = (capabilitesList.ElementAt(1) == "true") || (capabilitesList.ElementAt(2) == "true") || (capabilitesList.ElementAt(3) == "true");
                    printer.EnabledPunchFour = (capabilitesList.ElementAt(2) == "true");
                    printer.EnabledPunchThree = (capabilitesList.ElementAt(3) == "true");
                    printer.EnabledTrayFacedown = (capabilitesList.ElementAt(4) == "true");
                    //printer.EnabledTrayAutostack = (capabilitesList.ElementAt(5) == "true")? true : false;
                    printer.EnabledTrayTop = (capabilitesList.ElementAt(6) == "true");
                    printer.EnabledTrayStack = (capabilitesList.ElementAt(7) == "true");
                    printer.EnabledPaperLW = (capabilitesList.ElementAt(8) == "true");
                    printer.EnabledFeedTrayOne = (capabilitesList.ElementAt(9) == "true");
                    printer.EnabledFeedTrayTwo = (capabilitesList.ElementAt(10) == "true");
                    printer.EnabledFeedTrayThree = (capabilitesList.ElementAt(11) == "true");
                }

                return printer;
            }
            else
            {
                return null;
            }
        }

        /**
         * Timeout
         * 
         * */
        private void handleAddTimeout(SNMPDevice device)
        {
            printerControllerAddTimeout(device.IpAddress, device.Description, device.CapabilitiesList);
        }

        private void handleTimeout(string ip)
        {
            printerControllerTimeout(ip);
        }
    }
}

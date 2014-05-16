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
    class SNMPController
    {
        public Action<string, bool> printerControllerGetStatusCallback { get; set; } //PrintersModule
        public Action<string, string, bool, List<string>> printerControllerAddPrinterCallback { get; set; }
        public Action<PrinterSearchItem> printerControllerDiscoverCallback { get; set; }
        public Action<string> printerControllerTimeout { get; set; }
        public Action<string, string, List<string>> printerControllerAddTimeout { get; set; }

        public List<Printer> printerList {get; set;}
        private bool waiting;

        SNMPDiscovery discovery;
        /**
         * Get Online Status functions
         * 
         * */

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
            discovery = new SNMPDiscovery("public", SNMPConstants.BROADCAST_ADDRESS);
        }

        public void getStatus(string ip)
        {
            System.Diagnostics.Debug.WriteLine("SNMPController get status of: ");
            System.Diagnostics.Debug.WriteLine(ip);
            SNMPDevice device = new SNMPDevice(ip);
            waiting = true;
            //device.snmpControllerCallBackGetStatus = new Action<string, bool>(handlePrinterStatus);
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
            SNMPDevice device = new SNMPDevice(ip);
            //waiting = true;
            device.snmpControllerCallBackGetCapability = new Action<SNMPDevice>(handleGetDevice);
            device.snmpControllerDeviceTimeOut = new Action<SNMPDevice>(handleAddTimeout);
            device.beginRetrieveCapabilities();

            //SNMPDiscovery discovery = new SNMPDiscovery("public", ip);
            //discovery.FromPrinterSearch = false;
            //discovery.snmpControllerDiscoverCallback = new Action<SNMPDevice>(handleGetDevice);
            //discovery.snmpControllerDiscoverTimeOut = new Action<string>(handleAddTimeout);
            //discovery.startDiscover();
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
            //SNMPDiscovery discovery = new SNMPDiscovery("public", SNMPConstants.BROADCAST_ADDRESS);
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

        public Printer getPrinterFromSNMPDevice(string ip)
        {
            SNMPDevice device = discovery.SnmpDevices.First(x => x.getIpAddress() == ip);

            Printer printer = new Printer();
            printer.IpAddress = ip;
            printer.Name = device.getSysName();
            printer.IsOnline = true;
            if (device.getCapabilities().Count > 0)
            {
                List<string> capabilitesList = device.getCapabilities();
                printer.EnabledBooklet = (capabilitesList.ElementAt(0) == "true") ? true : false;
                printer.EnabledStapler = (capabilitesList.ElementAt(1) == "true") ? true : false;
                printer.EnabledPunchFour = (capabilitesList.ElementAt(2) == "true") ? true : false;
                printer.EnabledTrayFacedown = (capabilitesList.ElementAt(4) == "true") ? true : false;
                printer.EnabledTrayAutostack = (capabilitesList.ElementAt(5) == "true") ? true : false;
                printer.EnabledTrayTop = (capabilitesList.ElementAt(6) == "true") ? true : false;
                printer.EnabledTrayStack = (capabilitesList.ElementAt(7) == "true") ? true : false;
            }
            else
            {
                printer.EnabledBooklet = true;
                printer.EnabledStapler = true;
                printer.EnabledPunchFour = true;
                printer.EnabledTrayFacedown = true;
                printer.EnabledTrayAutostack = true;
                printer.EnabledTrayTop = true;
                printer.EnabledTrayStack = true;
            }

            return printer;
        }

        /**
         * Timeout
         * 
         * */
        private void handleAddTimeout(SNMPDevice device)
        {
            printerControllerAddTimeout(device.getIpAddress(), device.getSysName(), device.getCapabilities());
        }

        private void handleTimeout(string ip)
        {
            printerControllerTimeout(ip);
        }

        

    }
}
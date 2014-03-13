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
        public List<Printer> printerList {get; set;}
        private bool waiting;

        //public void startPolling()
        //{
        //    int period = 500;
        //    var timerHandler = new TimerElapsedHandler(getStatus);

        //    ThreadPoolTimer periodicTimer = ThreadPoolTimer.CreatePeriodicTimer(timerHandler, TimeSpan.FromMilliseconds(period));
        //}

        public void getStatus(string ip)
        {
            System.Diagnostics.Debug.WriteLine("SNMPController get status of: ");
            System.Diagnostics.Debug.WriteLine(ip);
            SNMPDevice device = new SNMPDevice(ip);
            waiting = true;
            device.snmpControllerCallBackGetStatus = new Action<string, bool>(handlePrinterStatus);
            device.beginRetrieveCapabilities();
            //get result TODO
            
        }

        private void handlePrinterStatus(string ip, bool isOnline)
        {
            waiting = false;
            System.Diagnostics.Debug.WriteLine("SNMPController callback for ip: ");
            System.Diagnostics.Debug.WriteLine(ip);
            printerControllerGetStatusCallback(ip, isOnline);
            
        }



    }
}

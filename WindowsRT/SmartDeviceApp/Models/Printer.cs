using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceApp.Models
{
    public class Printer
    {
        //class members
        private int _id;                //id in database
        private string _ip_address;     //ip address of the printer
        private string _name;           //printer name
        private int _port_setting;
        private bool _enabled_lpr;
        private bool _enabled_raw;
        private bool _enabled_pagination;
        private bool _enabled_duplex;
        private bool _enabled_booklet_binding;
        private bool _enabled_staple;
        private bool _enabled_bind;

        private bool _isDefaultPrinter;
        ////for pooling
        private bool _isOnline;



        //Getters and Setters
        public int Id
        {
            get;
            set;
        }

        public string Ip_address
        {
            get;
            set;
        }

        public string Name
        {
            get;
            set;
        }

        public bool Enabled_LPR
        {
            get;
            set;
        }

        public bool Enabled_Raw
        {
            get;
            set;
        }

        public bool Enabled_Pagination
        {
            get;
            set;
        }

        public bool Enabled_Duplex
        {
            get;
            set;
        }

        public bool Enabled_BooketBinding
        {
            get;
            set;
        }

        public bool Enabled_Staple
        {
            get;
            set;
        }

        public bool Enabled_Bind
        {
            get;
            set;
        }

        public bool isDefaultPrinter
        {
            get
            {
                return this._isDefaultPrinter;
            }
            set
            {
                this._isDefaultPrinter = value;
                if (PropertyChanged != null)
                {
                    PropertyChanged(this, new PropertyChangedEventArgs("isDefaultPrinter"));
                }
            }
        }

        public bool isOnline
        {
            get
            {
                return this._isOnline;
            }
            set
            {
                this._isOnline = value;
                if (PropertyChanged != null)
                {
                    PropertyChanged(this, new PropertyChangedEventArgs("isOnline"));
                }
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;
    }
}

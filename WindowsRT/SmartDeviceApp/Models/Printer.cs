//
//  Printer.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/17.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;

namespace SmartDeviceApp.Models
{
    public class Printer : INotifyPropertyChanged
    {

        #region Properties

        private string _ipAddress;
        private string _name;
        private int _portSetting;
        private bool _isOnline;
        private bool _isDefault;
        private bool _willBeDeleted;
        private bool _willPerformDelete;
        private string _visualState;
        /// <summary>
        /// Printer ID, used by Printer table as primary key
        /// </summary>
        [SQLite.Column("prn_id"), SQLite.NotNull, SQLite.PrimaryKey, SQLite.AutoIncrement]
        public int Id { get; set; }

        /// <summary>
        /// Print setting ID, used by Printer table
        /// </summary>
        [SQLite.Column("pst_id")]
        public int? PrintSettingId { get; set; }

        /// <summary>
        /// Printer IP address, used by Printer table and allowed only upto 20 characters
        /// </summary>
        [SQLite.Column("prn_ip_address"), SQLite.MaxLength(20)]
        public string IpAddress {
            get { return this._ipAddress; }
            set
            {
                _ipAddress = value;
                OnPropertyChanged("IpAddress");
            }
        }

        /// <summary>
        /// Printer name, used by Printer table and allowed only upto 255 characters
        /// </summary>
        [SQLite.Column("prn_name"), SQLite.MaxLength(255)]
        public string Name {
            get { return this._name; }
            set
            {
                _name = value;
                OnPropertyChanged("Name");
            }
        }

        /// <summary>
        /// Printer post setting, used by Printer table
        /// </summary>
        [SQLite.Column("prn_port_setting"), SQLite.NotNull]
        public int PortSetting
        {
            get { return this._portSetting; }
            set
            {
                if (_portSetting != value)
                {
                    _portSetting = value;
                    OnPropertyChanged("PortSetting");
                }
            }
        }

        /// <summary>
        /// Printer capability for LPR, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_lpr"), SQLite.NotNull]
        public bool EnabledLpr { get; set; }

        /// <summary>
        /// Printer capability for RAW, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_raw"), SQLite.NotNull]
        public bool EnabledRaw { get; set; }

        /// <summary>
        /// Printer capability for LW Paper, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_paper_lw"), SQLite.NotNull]
        public bool EnabledPaperLW { get; set; }

        /// <summary>
        /// Printer capability for Input Tray 1, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_feed_tray1"), SQLite.NotNull]
        public bool EnabledFeedTrayOne { get; set; }

        /// <summary>
        /// Printer capability for Input Tray 2 used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_feed_tray2"), SQLite.NotNull]
        public bool EnabledFeedTrayTwo { get; set; }

        /// <summary>
        /// Printer capability for Input Tray 3, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_feed_tray3"), SQLite.NotNull]
        public bool EnabledFeedTrayThree { get; set; }

        /// <summary>
        /// Printer capability for booklet finishing, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_booklet_finishing"), SQLite.NotNull]
        public bool EnabledBookletFinishing { get; set; }

        /// <summary>
        /// Printer capability for stapler, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_stapler"), SQLite.NotNull]
        public bool EnabledStapler { get; set; }

        /// <summary>
        /// Printer capability for three-hole punch, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_punch3"), SQLite.NotNull]
        public bool EnabledPunchThree { get; set; }

        /// <summary>
        /// Printer capability for four-hole punch, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_punch4"), SQLite.NotNull]
        public bool EnabledPunchFour { get; set; }

        /// <summary>
        /// Printer capability for facedown tray, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_tray_facedown"), SQLite.NotNull]
        public bool EnabledTrayFacedown { get; set; }

        /// <summary>
        /// Printer capability for top tray, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_tray_top"), SQLite.NotNull]
        public bool EnabledTrayTop { get; set; }

        /// <summary>
        /// Printer capability for stack tray, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_tray_stack"), SQLite.NotNull]
        public bool EnabledTrayStack { get; set; }

        /// <summary>
        /// Flag that denotes that the printer is the default printer
        /// </summary>
        [SQLite.Ignore]
        public bool IsDefault {
            get { return this._isDefault; }
            set
            {
                _isDefault = value;
                OnPropertyChanged("IsDefault");
            }
        }

        /// <summary>
        /// Flag that denotes that the printer is online
        /// </summary>
        [SQLite.Ignore]
        public bool IsOnline {
            get { return this._isOnline; }
            set
            {
                if (_isOnline != value)
                {
                    _isOnline = value;
                    OnPropertyChanged("IsOnline");
                }
            }
        }

        /// <summary>
        /// Flag that denotes that the printer will be deleted
        /// </summary>
        [SQLite.Ignore]
        public bool WillBeDeleted
        {
            get { return this._willBeDeleted; }
            set
            {
                _willBeDeleted = value;
                OnPropertyChanged("WillBeDeleted");
            }
        }

        [SQLite.Ignore]
        public string VisualState
        {
            get { return this._visualState; }
            set
            {
                _visualState = value;
                OnPropertyChanged("VisualState");
            }
        }

        #endregion Properties

        
        /// <summary>
        /// Event handler for property change.
        /// </summary>
        public event PropertyChangedEventHandler PropertyChanged;

        /// <summary>
        /// Notifies classes that a property has been changed.
        /// </summary>
        public void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
        }


        /// <summary>
        /// Printer default class constructor
        /// </summary>
        public Printer()
        {
            Id = -1;
            PrintSettingId = null;
            IpAddress = null;
            Name = null;
            PortSetting = (int)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PRN_PORT_SETTING, ListValueType.Int);
            EnabledLpr = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PRN_ENABLED_LPR, ListValueType.Boolean);
            EnabledRaw = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PRN_ENABLED_RAW, ListValueType.Boolean);
            EnabledPaperLW = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PRN_ENABLED_PAPER_LW, ListValueType.Boolean);
            EnabledFeedTrayOne = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PRN_ENABLED_FEED_TRAY1, ListValueType.Boolean);
            EnabledFeedTrayTwo = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PRN_ENABLED_FEED_TRAY2, ListValueType.Boolean);
            EnabledFeedTrayThree = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PRN_ENABLED_FEED_TRAY3, ListValueType.Boolean);
            EnabledBookletFinishing = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PRN_ENABLED_BOOKLET_FINISHING, ListValueType.Boolean);
            EnabledStapler = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PRN_ENABLED_STAPLER, ListValueType.Boolean);
            EnabledPunchThree = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PRN_ENABLED_PUNCH3, ListValueType.Boolean);
            EnabledPunchFour = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PRN_ENABLED_PUNCH4, ListValueType.Boolean);
            EnabledTrayFacedown = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PRN_ENABLED_TRAY_FACEDOWN, ListValueType.Boolean);
            EnabledTrayTop = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PRN_ENABLED_TRAY_TOP, ListValueType.Boolean);
            EnabledTrayStack = (bool)DefaultsUtility.GetDefaultValueFromSqlScript(DefaultsUtility.KEY_COLUMN_NAME_PRN_ENABLED_TRAY_STACK, ListValueType.Boolean);
            IsDefault = false;
            IsOnline = false;
        }

    }
}

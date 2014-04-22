﻿//
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
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceApp.Models
{
    public class Printer
    {

        #region Properties

        /// <summary>
        /// Printer ID, used by Printer table as primary key
        /// </summary>
        [SQLite.Column("prn_id"), SQLite.NotNull, SQLite.PrimaryKey, SQLite.AutoIncrement]
        public int Id { get; set; }

        /// <summary>
        /// Print setting ID, used by Printer table
        /// </summary>
        [SQLite.Column("pst_id")]
        public int PrintSettingId { get; set; }

        /// <summary>
        /// Printer IP address, used by Printer table and allowed only upto 20 characters
        /// </summary>
        [SQLite.Column("prn_ip_address"), SQLite.MaxLength(20)]
        public string IpAddress { get; set; }

        /// <summary>
        /// Printer name, used by Printer table and allowed only upto 255 characters
        /// </summary>
        [SQLite.Column("prn_name"), SQLite.MaxLength(255)]
        public string Name { get; set; }

        /// <summary>
        /// Printer post setting, used by Printer table
        /// </summary>
        [SQLite.Column("prn_port_setting"), SQLite.NotNull]
        public int PortSetting { get; set; }

        /// <summary>
        /// Printer support for LPR, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_lpr"), SQLite.NotNull]
        public bool EnabledLpr { get; set; }

        /// <summary>
        /// Printer support for RAW, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_raw"), SQLite.NotNull]
        public bool EnabledRaw { get; set; }

        /// <summary>
        /// Printer support for booklet, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_booklet"), SQLite.NotNull]
        public bool EnabledBooklet { get; set; }

        /// <summary>
        /// Printer support for stapler, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_stapler"), SQLite.NotNull]
        public bool EnabledStapler { get; set; }

        /// <summary>
        /// Printer support for four-hole punch, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_punch4"), SQLite.NotNull]
        public bool EnabledPunchFour { get; set; }

        /// <summary>
        /// Printer support for facedown tray, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_tray_facedown"), SQLite.NotNull]
        public bool EnabledTrayFacedown { get; set; }

        /// <summary>
        /// Printer support for autostack tray, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_tray_autostack"), SQLite.NotNull]
        public bool EnabledTrayAutostack { get; set; }

        /// <summary>
        /// Printer support for top tray, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_tray_top"), SQLite.NotNull]
        public bool EnabledTrayTop { get; set; }

        /// <summary>
        /// Printer support for stack tray, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_tray_stack"), SQLite.NotNull]
        public bool EnabledTrayStack { get; set; }

        /// <summary>
        /// Flag that denotes that the printer is the default printer
        /// </summary>
        [SQLite.Ignore]
        public bool IsDefault { get; set; }

        /// <summary>
        /// Flag that denotes that the printer is online
        /// </summary>
        [SQLite.Ignore]
        public bool IsOnline { get; set; }

        /// <summary>
        /// Print settings associated to the printer
        /// </summary>
        [SQLite.Ignore]
        public PrintSettings PrintSettings { get; set; }

        #endregion Properties

        /// <summary>
        /// Printer default class constructor
        /// </summary>
        public Printer()
        {
            Id = -1;
            PrintSettingId = -1;
            IpAddress = null;
            Name = null;
            PortSetting = -1;
            EnabledLpr = false;
            EnabledRaw = false;
            EnabledPagination = false;
            EnabledDuplex = false;
            EnabledBookletBinding = false;
            EnabledStaple = false;
            EnabledBind = false;
            IsDefault = false;
            IsOnline = false;
            PrintSettings = null;
        }

        /// <summary>
        /// Print class constructor
        /// </summary>
        /// <param name="id">printer ID</param>
        /// <param name="printSettingId">print setting ID</param>
        /// <param name="ipAddress">IP address</param>
        /// <param name="name">printer name</param>
        /// <param name="portSetting">port setting</param>
        /// <param name="enabledLpr">enabled LPR</param>
        /// <param name="enabledRaw">enabled RAW</param>
        /// <param name="enabledPagination">enabled pagination</param>
        /// <param name="enabledDuplex">enabled duplex</param>
        /// <param name="enabledBookletBinding">enabled booklet binding</param>
        /// <param name="enabledStaple">enabled staple</param>
        /// <param name="enabledBind">enabled bind</param>
        public Printer(int id, int printSettingId, string ipAddress, string name, int portSetting,
            bool enabledLpr, bool enabledRaw, bool enabledPagination, bool enabledDuplex,
            bool enabledBookletBinding, bool enabledStaple, bool enabledBind)
        {
            Id = id;
            PrintSettingId = printSettingId;
            IpAddress = ipAddress;
            Name = name;
            PortSetting = portSetting;
            EnabledLpr = enabledLpr;
            EnabledRaw = enabledRaw;
            EnabledPagination = enabledPagination;
            EnabledDuplex = enabledDuplex;
            EnabledBookletBinding = enabledBookletBinding;
            EnabledStaple = enabledStaple;
            EnabledBind = enabledBind;
            IsDefault = false;
            IsOnline = false;
            PrintSettings = null;
        }

    }
}

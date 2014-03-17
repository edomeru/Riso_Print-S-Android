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
        [SQLite.Column("prn_id"), SQLite.PrimaryKey]
        public int Id { get; set; }

        /// <summary>
        /// Print setting ID, used by Printer table and is indexed
        /// </summary>
        [SQLite.Column("pst_id"), SQLite.Indexed(Name = "Printer_FKIndex1")]
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
        [SQLite.Column("prn_port_setting")]
        public int PortSetting { get; set; }

        /// <summary>
        /// Printer support for LPR, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_lpr")]
        public bool EnabledLpr { get; set; }

        /// <summary>
        /// Printer support for RAW, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_raw")]
        public bool EnabledRaw { get; set; }

        /// <summary>
        /// Printer support for pagination, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_pagination")]
        public bool EnabledPagination { get; set; }

        /// <summary>
        /// Printer support for duplex, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_duplex")]
        public bool EnabledDuplex { get; set; }

        /// <summary>
        /// Printer support for booklet binding, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_booklet_binding")]
        public bool EnabledBookletBinding { get; set; }

        /// <summary>
        /// Printer support for staple, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_staple")]
        public bool EnabledStaple { get; set; }

        /// <summary>
        /// Printer support for bind, used by Printer table
        /// </summary>
        [SQLite.Column("prn_enabled_bind")]
        public bool EnabledBind { get; set; }

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

        #endregion Properties

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
        }

    }
}

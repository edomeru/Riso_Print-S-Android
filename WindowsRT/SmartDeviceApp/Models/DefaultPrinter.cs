//
//  DefaultPrinter.cs
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
    public class DefaultPrinter
    {

        #region Properties

        /// <summary>
        /// Printer ID, used by DefaultPrinter table and is indexed
        /// </summary>
        [SQLite.Column("prn_id"), SQLite.Indexed(Name = "DefaultPrinter_FKIndex1")]
        public int PrinterId { get; set; }

        #endregion Properties

        /// <summary>
        /// DefaultPrinter default class constructor
        /// </summary>
        public DefaultPrinter()
        {
            PrinterId = -1;
        }

        /// <summary>
        /// DefaultPrinter class constructor
        /// </summary>
        /// <param name="printerId">Printer id of the selected default printer</param>
        public DefaultPrinter(int printerId)
        {
            PrinterId = printerId;
        }

    }
}

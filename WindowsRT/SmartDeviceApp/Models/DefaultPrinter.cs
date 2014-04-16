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
        [SQLite.Column("prn_id"), SQLite.NotNull, SQLite.PrimaryKey, SQLite.Indexed(Name = "DefaultPrinter_FKIndex1")]
        public uint PrinterId { get; set; }

        #endregion Properties

    }
}

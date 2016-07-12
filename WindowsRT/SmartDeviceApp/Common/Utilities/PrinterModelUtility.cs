//
//  PrinterModelUtility.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2016/07/29.
//  Copyright 2016 RISO KAGAKU CORPORATION. All Rights Reserved.
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

namespace SmartDeviceApp.Common.Utilities
{
    public static class PrinterModelUtility
    {
        /// <summary>
        /// Checks if printer name belongs to the FW printer series
        /// </summary>
        /// <param name="printerName">printer name</param>
        /// <returns>true when printer name is FW series, false otherwise</returns>
        public static bool isFWSeries(string printerName)
        {
            HashSet<string> fwNames = new HashSet<string> {
                                "ORPHIS FW5230",
                                "ORPHIS FW5230A",
                                "ORPHIS FW5231",
                                "ORPHIS FW2230",
                                "ORPHIS FW1230",
                                "ComColor FW5230",
                                "ComColor FW5230R",
                                "ComColor FW5231",
                                "ComColor FW5231R",
                                "ComColor FW5000",
                                "ComColor FW5000R",
                                "ComColor FW2230",
                                "ComColor black FW1230",
                                "ComColor black FW1230R",
                                // China:
                                "Shan Cai Yin Wang FW5230",
                                "Shan Cai Yin Wang FW5230R",
                                "Shan Cai Yin Wang FW5231",
                                "Shan Cai Yin Wang FW2230 Wenjianhong",
                                "Shan Cai Yin Wang FW2230 Lan",
                                "Shan Cai Yin Wang black FW1230",
                                "Shan Cai Yin Wang black FW1230R" };

            bool isFW = fwNames.Contains(printerName);
            fwNames.Clear();

            return isFW;
        }

        /// <summary>
        /// Checks if printer name belongs to the IS printer series
        /// </summary>
        /// <param name="printerName">printer name</param>
        /// <returns>true when printer name is IS series, false otherwise</returns>
        public static bool isISSeries(string printerName)
        {
            HashSet<string> isNames = new HashSet<string> {
                                "RISO IS1000C-J",
                                "RISO IS1000C-G",
                                "RISO IS950C-G", };

            bool isIS = isNames.Contains(printerName);
            isNames.Clear();

            return isIS;
        }
    }
}

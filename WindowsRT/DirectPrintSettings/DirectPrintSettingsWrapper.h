//
//  DirectPrintSettingsWrapper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

#pragma once

using Platform::String;

namespace DirectPrint
{
    public ref class DirectPrintSettingsWrapper sealed
    {
    public:
		DirectPrintSettingsWrapper();
		static String^ create_pjl_wrapper(String^ settings);
		static String^ create_pjl_fw_wrapper(String^ settings, String^ printerName, String ^appVersion);
		static String^ create_pjl_gd_wrapper(String^ settings, String^ printerName, String ^appVersion);
    };
}
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
    };
}
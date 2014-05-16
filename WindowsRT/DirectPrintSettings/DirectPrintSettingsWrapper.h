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
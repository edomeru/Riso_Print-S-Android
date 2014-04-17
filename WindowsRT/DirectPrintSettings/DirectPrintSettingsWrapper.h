#pragma once

using Platform::String;

namespace DirectPrint
{
    public ref class DirectPrintSettingsWrapper sealed
    {
    public:
		DirectPrintSettingsWrapper();
		static void create_pjl_wrapper(String^ pjl, String^ settings);
    };
}
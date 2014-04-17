// Class1.cpp
#include "pch.h"
#include "DirectPrintSettingsWrapper.h"
extern "C"
{
#include "../../CommonLibrary/src/printsettings.h"
}

using namespace DirectPrint;
using namespace Platform;

DirectPrintSettingsWrapper::DirectPrintSettingsWrapper()
{

};

void DirectPrintSettingsWrapper::create_pjl_wrapper(String^ pjl, String^ settings){

	//create_pjl(0, 0);
	create_pjl(0, 0);
};

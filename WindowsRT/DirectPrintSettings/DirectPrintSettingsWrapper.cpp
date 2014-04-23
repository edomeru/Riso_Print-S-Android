// Class1.cpp
#include "pch.h"
#include "DirectPrintSettingsWrapper.h"
extern "C"
{
#include "../../CommonLibrary/src/printsettings.h"
}

using namespace DirectPrint;
using namespace Platform;

#define BUFFER_SIZE 2048

DirectPrintSettingsWrapper::DirectPrintSettingsWrapper()
{

};

char* convertWCharToCharArray(const wchar_t* input)
{
    size_t length = wcslen(input) + 1;
    size_t convertedChars = 0;
    char cString[BUFFER_SIZE];
    wcstombs_s(&convertedChars, cString, length, input, _TRUNCATE);

    return cString;
}

wchar_t* convertCharArrayToWChar(char* input)
{
    size_t length = strlen(input) + 1;
    const size_t newsize = BUFFER_SIZE;
    size_t convertedChars = 0;
    wchar_t wcString[BUFFER_SIZE];
    mbstowcs_s(&convertedChars, wcString, length, input, _TRUNCATE);

    return wcString;
}

String^ DirectPrintSettingsWrapper::create_pjl_wrapper(String^ settings)
{
    char pjl[BUFFER_SIZE];
    memset(pjl, 0, BUFFER_SIZE);

    create_pjl(pjl, convertWCharToCharArray(settings->Data()));

    return ref new String(convertCharArrayToWChar(pjl));
};

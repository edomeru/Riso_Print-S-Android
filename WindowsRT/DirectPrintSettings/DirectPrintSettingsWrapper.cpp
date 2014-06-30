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

void convertWCharToCharArray(char** output, const wchar_t* input)
{
    size_t length = wcslen(input) + 1;
    size_t convertedChars = 0;
    wcstombs_s(&convertedChars, *output, length, input, _TRUNCATE);
}

void convertCharArrayToWChar(wchar_t** output, const char* input)
{
    size_t length = strlen(input) + 1;
    size_t convertedChars = 0;
    mbstowcs_s(&convertedChars, *output, length, input, _TRUNCATE);
}

String^ DirectPrintSettingsWrapper::create_pjl_wrapper(String^ settings)
{
    char pjl[BUFFER_SIZE];
    memset(pjl, 0, BUFFER_SIZE);

    char *strSettings = (char *)calloc(BUFFER_SIZE, sizeof(char));
    memset(strSettings, 0, BUFFER_SIZE * sizeof(char));

    convertWCharToCharArray(&strSettings, settings->Data());
    create_pjl(pjl, strSettings);

    wchar_t *wcPjl = (wchar_t *)calloc(BUFFER_SIZE, sizeof(wchar_t));
    memset(wcPjl, 0, BUFFER_SIZE * sizeof(wchar_t));

    convertCharArrayToWChar(&wcPjl, pjl);

    String^ printSettingsPjl = ref new String(wcPjl);

    free(strSettings);
    free(wcPjl);

    return printSettingsPjl;
};

//
//  DirectPrintSettingsWrapper.cpp
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

#include "pch.h"
#include "DirectPrintSettingsWrapper.h"
extern "C"
{
#include "../../CommonLibrary/src/printsettings.h"
}

using namespace DirectPrint;
using namespace Platform;

#define BUFFER_SIZE 2048
#define APP_NAME_SIZE 128
#define APP_VERSION_SIZE 16

DirectPrintSettingsWrapper::DirectPrintSettingsWrapper()
{

};

/*
 * Converts a wchar string to character array string
 */
void convertWCharToCharArray(char** output, const wchar_t* input)
{
    size_t length = wcslen(input) + 1;
    size_t convertedChars = 0;

	// First call to get length
	int utf8_length;
	utf8_length = WideCharToMultiByte(
		CP_UTF8,           // Convert to UTF-8
		0,                 // No special character conversions required 
						   // (UTF-16 and UTF-8 support the same characters)
		input,             // UTF-16 string to convert
		-1,                // utf16 is NULL terminated (if not, use length)
		NULL,              // Determining correct output buffer size
		0,                 // Determining correct output buffer size
		NULL,              // Must be NULL for CP_UTF8
		NULL);             // Must be NULL for CP_UTF8

	utf8_length = WideCharToMultiByte(
		CP_UTF8,           // Convert to UTF-8
		0,                 // No special character conversions required 
		                   // (UTF-16 and UTF-8 support the same characters)
		input,             // UTF-16 string to convert
		-1,                // utf16 is NULL terminated (if not, use length)
		*output,           // UTF-8 output buffer
		utf8_length,       // UTF-8 output buffer size
		NULL,              // Must be NULL for CP_UTF8
		NULL);             // Must be NULL for CP_UTF8
}

/*
 * Converts a character array string to wchar string
 */
void convertCharArrayToWChar(wchar_t** output, const char* input)
{
    size_t length = strlen(input) + 1;
    size_t convertedChars = 0;
	// First call to get length
	int size_needed = MultiByteToWideChar(CP_UTF8, 0, input, length, NULL, 0);
	MultiByteToWideChar(CP_UTF8, 0, input, length, *output, size_needed);
}

/*
 * Creates a string of PJL commands for IS printers based from print settings
 */
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
}

/*
 * Creates a string of PJL commands for FW printers based from print settings
 */
String^ DirectPrintSettingsWrapper::create_pjl_fw_wrapper(String^ settings, String^ printerName, String ^appVersion)
{
	char pjl[BUFFER_SIZE];
	memset(pjl, 0, BUFFER_SIZE);

	char *strSettings = (char *)calloc(BUFFER_SIZE, sizeof(char));
	memset(strSettings, 0, BUFFER_SIZE * sizeof(char));

	convertWCharToCharArray(&strSettings, settings->Data());

	char *strPrinterName = (char *)calloc(APP_NAME_SIZE, sizeof(char));
	memset(strPrinterName, 0, APP_NAME_SIZE * sizeof(char));
	convertWCharToCharArray(&strPrinterName, printerName->Data());

	char *strAppVersion = (char *)calloc(APP_VERSION_SIZE, sizeof(char));
	memset(strAppVersion, 0, APP_VERSION_SIZE * sizeof(char));
	convertWCharToCharArray(&strAppVersion, appVersion->Data());

	create_pjl_fw(pjl, strSettings, strPrinterName, strAppVersion);

	wchar_t *wcPjl = (wchar_t *)calloc(BUFFER_SIZE, sizeof(wchar_t));
	memset(wcPjl, 0, BUFFER_SIZE * sizeof(wchar_t));

	convertCharArrayToWChar(&wcPjl, pjl);

	String^ printSettingsPjl = ref new String(wcPjl);

	free(strSettings);
	free(strPrinterName);
	free(strAppVersion);
	free(wcPjl);

	return printSettingsPjl;
}

/*
 * Creates a string of PJL commands for GD printers based from print settings
 */
String^ DirectPrintSettingsWrapper::create_pjl_gd_wrapper(String^ settings, String^ printerName, String ^appVersion)
{
	char pjl[BUFFER_SIZE];
	memset(pjl, 0, BUFFER_SIZE);

	char *strSettings = (char *)calloc(BUFFER_SIZE, sizeof(char));
	memset(strSettings, 0, BUFFER_SIZE * sizeof(char));

	convertWCharToCharArray(&strSettings, settings->Data());

	char *strPrinterName = (char *)calloc(APP_NAME_SIZE, sizeof(char));
	memset(strPrinterName, 0, APP_NAME_SIZE * sizeof(char));
	convertWCharToCharArray(&strPrinterName, printerName->Data());

	char *strAppVersion = (char *)calloc(APP_VERSION_SIZE, sizeof(char));
	memset(strAppVersion, 0, APP_VERSION_SIZE * sizeof(char));
	convertWCharToCharArray(&strAppVersion, appVersion->Data());

	create_pjl_gd(pjl, strSettings, strPrinterName, strAppVersion);

	wchar_t *wcPjl = (wchar_t *)calloc(BUFFER_SIZE, sizeof(wchar_t));
	memset(wcPjl, 0, BUFFER_SIZE * sizeof(wchar_t));

	convertCharArrayToWChar(&wcPjl, pjl);

	String^ printSettingsPjl = ref new String(wcPjl);

	free(strSettings);
	free(strPrinterName);
	free(strAppVersion);
	free(wcPjl);

	return printSettingsPjl;
}
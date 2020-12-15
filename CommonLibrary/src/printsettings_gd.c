//
//  printsettings_gd.c
//  SmartDeviceApp
//
//  Created by RISO KAGAKU CORPORATION.
//  Copyright (c) 2016 RISO KAGAKU CORPORATION. All rights reserved.
//
#ifdef _MSC_VER
#define _CRT_SECURE_NO_WARNINGS
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "printsettings.h"


#define PJL_COMMAND_STR "@PJL SET %s=%s\x0d\x0a"
#define PJL_COMMAND_INT "@PJL SET %s=%d\x0d\x0a"

#define PJL_COMMAND_VERSKION "2.10"
#define PJL_IDENTIFIER "RISO_IJ_PJL"
#define PJL_RIPCONTROL_FLAG "10"
// End
#define PJL_SOFTWERENAME "RISO PRINT-S"

typedef enum
{
    kPrintSettingsColorMode,
    kPrintSettingsOrientation,
    kPrintSettingsCopies,
    kPrintSettingsDuplex,
    kPrintSettingsPaperSize,
    kPrintSettingsScaleToFit,
    kPrintSettingsPaperType,
    kPrintSettingsInputTray,
    kPrintSettingsImposition,
    kPrintSettingsImpositionOrder,
    kPrintSettingsSort,
    kPrintSettingsBooklet,
    kPrintSettingsBookletFinish,
    kPrintSettingsBookletLayout,
    kPrintSettingsFinishingSide,
    kPrintSettingsStaple,
    kPrintSettingsPunch,
    kPrintSettingsOutputTray,
    kPrintSettingsLoginId,
    kPrintSettingsPinCode,
    kPrintSettingsSecurePrint,
    kPrintSettingsCount
} kPrintSettings;

typedef enum
{
    kPjlCommandPjlVersion,
    kPjlCommandPjlIdentifier,
    kPjlCommandRipControlFlag,
	// Ver.2.0.4.2 Start
	kPjlCommandHostName,
	// End
    kPjlCommandSoftwereName,
    kPjlCommandSoftwereVersion,
    kPjlCommandColorMode,
    kPjlCommandOrientation,
    // Ver.2.0.2.3 Start
    //kPjlCommandCopies,
    //kPjlCommandQuantity,
    kPjlCommandQuantity,
    kPjlCommandCopies,
    // End
    kPjlCommandDuplex,
    kPjlCommandDuplexBinding,
    kPjlCommandOutputPaperSize,
    kPjlCommandScaleToFit,
    kPjlCommandZoomRate,
    kPJLCommandPaperType,
    kPjlCommandInputTray,
    kPjlCommandInputTrayMediaSource,
    kPjlCommandMultiup,
    kPjlCommandBooklet,
    kPjlCommandBookletFinishing,
    kPjlCommandFinishingSide,
    kPjlCommandStaple,
    kPjlCommandPunch,
    kPjlCommandOutputTray,
    kPjlCommandOwner,
    kPjlCommandPrivatePrinting,
    kPjlCommandPrivatePrintingBoxNumber,
    kPjlCommandCount
} kPjlCommand;

/*const static char *printsetting_names[kPrintSettingsCount] =
{
    "colorMode",
    "orientation",
    "copies",
    "duplex",
    "paperSize",
    "scaleToFit",
    "paperType",
    "inputTray",
    "imposition",
    "impositionOrder",
    "sort",
    "booklet",
    "bookletFinish",
    "bookletLayout",
    "finishingSide",
    "staple",
    "punch",
    "outputTray",
    "loginId",
    "pinCode",
    "securePrint"
};*/

const static char *color_mode[] =
{
    "AUTO",
    "FIVECOLOR",
    "GRAYSCALE",
    "DUAL"
};

const static char *orientation[] =
{
    "PORTRAIT",
    "LANDSCAPE"
};

const static char *duplex_binding[] =
{
    "LONGEDGE",
    "SHORTEDGE"
};

const static char *paper_size[] =
{
    "A3",
    "A3W",
    "A4",
    "A5",
    "A6",
    "B4",
    "B5",
    "B6",
    "FOOLSCAP",
    "TABLOID",
    "LEGAL",
    "LETTER",
    "STATEMENT",
    "PHLEGAL",
    "HACHIKAI",
    "JUROKUKAI",
};

const static char *paper_type[] =
{
    "AUTO",
    "STANDARD",
    "HC",
    "HCMATTED",
    "HIGHQUALITY2",
    "POSTCARDJ",
    "LIGHT",
    "RECYCLED",
    //"CRIMPED"
};

const static char *input_tray_media_source[] =
{
    "STANDARD",
    "TRAY1",
    "TRAY2",
    "TRAY3"
};

const static char *multiup[] =
{
    "OFF",
    "2PAGESLEFTRIGHT",
    "2PAGESRIGHTLEFT",
    "4PAGESLEFTTOPRIGHT",
    "4PAGESLEFTTOPBOTTOM",
    "4PAGESRIGHTTOPLEFT",
    "4PAGESRIGHTTOPBOTTOM",
};

const static char *booklet[] =
{
    "OFF",
    "LEFTBINDING",
    "RIGHTBINDING"
};

const static char *booklet_finishing[] =
{
    "OFF",
    "2-FOLD",
    "2-FOLDANDSTAPLE"
};

const static char *finishing_side[] =
{
    "LEFTBINDING",
    "TOPBINDING",
    "RIGHTBINDING"
};

const static char *staple[] =
{
    "OFF",
    "1STAPLE",
    "1STAPLELEFT",
    "1STAPLERIGHT",
    "2STAPLES"
};

const static char *punch[] =
{
    "OFF",
    "2HOLES",
    "4HOLES",
    "3HOLES",
};

const static char *output_tray[] =
{
    "AUTO",
    "FACEDOWN",
    "UPPER",
    "STACKER"
};

const static char *off_on[] =
{
    "OFF",
    "ON"
};

/*const static char *false_true[] =
{
    "FALSE",
    "TRUE"
};*/

const static char **pjl_values[kPjlCommandCount] =
{
    0,
    0,
    0,
	// Ver.2.0.4.2 Start
	0,
	// End
    0,
    0,
    color_mode,
    orientation,
    0,
    0,
    off_on,  // duplex
    duplex_binding,
    paper_size,
    off_on, // scale to fit
    0,
    paper_type,
    off_on, // input tray
    input_tray_media_source,
    multiup,
    booklet,
    booklet_finishing,
    finishing_side,
    staple,
    punch,
    output_tray,
    0,
    off_on,
    0
};


const static char *pjl_commands[kPjlCommandCount] =
{
    "RKPJLVERSION",
    "RKPJLIDENTIFIER",
    "RKRIPCONTROLFLAG",
	// Ver.2.0.4.2 Start
	"RKHOSTNAME",
	// End
    "RKSOFTWARENAME",
    "RKSOFTWAREVERSION",
    "RKOUTPUTMODE",
    "ORIENTATION",
    // Ver.2.0.2.3 Start
	//"COPIES",
    //"QTY",
	"QTY",
	"COPIES",
    // End
	"DUPLEX",
    "BINDING",
    "PAPER",
    "RKMANUALZOOM",
    "RKZOOMRATE",
    "MEDIATYPE",
    "AUTOSELECT",
    "MEDIASOURCE",
    "RKMULTIUP",
    "RKBOOKLET",
    "RKBOOKLETBINDING",
    "RKBINDING",
    "RKSTAPLEMODE",
    "RKPUNCHMODE",
    "RKOUTPUTTRAY",
    "USERNAME",
    "RKSECRETNUM",
    "PASSWORD"
};

typedef struct
{
    int set;
    int int_value;
    char *str_value;
} setting_value;

extern void parse(char *settings, setting_value values[]);
extern void parse_line(char *line, char *name, char *value);
extern int get_setting_index(const char *name);

void add_pjl_gd(char *pjl, char *printerName, char *hostName, char *appVersion, setting_value values[], int command);

// Ver.2.0.0.3 start
//void create_pjl_gd(char *pjl, char *settings, char *appName, char *appVersion)
// Ver.2.0.4.2 Start
//void create_pjl_gd(char *pjl, char *settings, char *printerName, char *appVersion)
void create_pjl_gd(char *pjl, char *settings, char *printerName, char *hostName, char *appVersion)
// Ver.2.0.4.2 End
//void create_pjl_gd(char *pjl, char *settings, char *printerName)
// Ver.2.0.0.3 end
{
    // RM 496 Fix: Check input if null, return
    if (!settings || strlen(settings) == 0 || !printerName || !hostName || !appVersion)
    {
        return;
    }
    
    setting_value values[kPrintSettingsCount];
    for (int i = 0; i < kPrintSettingsCount; i++)
    {
        values[i].set = 0;
        values[i].str_value = 0;
    }
    
    parse(settings, values);
    
    for (int i = 0; i < kPjlCommandCount; i++)
    {
        // Ver.2.0.0.3 start
        //add_pjl_gd(pjl, appName, appVersion, values, i);
        add_pjl_gd(pjl, printerName, hostName, appVersion, values, i);
        //add_pjl(pjl, printerName, values, i);
        // Ver.2.0.0.3 end
    }
    
    for (int i = 0; i < kPrintSettingsCount; i++)
    {
        if (values[i].str_value != 0)
        {
            free(values[i].str_value);
        }
    }
}

/*
void parse(char *settings, setting_value values[])
{
    char *current_line = settings;
    char *next = strchr(current_line, '\n');
    while (next != 0)
    {
        long line_length = next - current_line + 1;
        char *line = (char *)calloc(line_length, sizeof(char));
        strncpy(line, current_line, line_length - 1);
        
        char name[64];
        char value[1024];
        parse_line(line, name, value);
        
        int setting_index = get_setting_index(name);
        if (setting_index != -1)
        {
            values[setting_index].set = 1;
            if (setting_index == kPrintSettingsLoginId || setting_index == kPrintSettingsPinCode)
            {
                values[setting_index].str_value = strdup(value);
            }
            else
            {
                int value_index;
                sscanf(value, "%d", &value_index);
                values[setting_index].int_value = value_index;
            }
        }
        
        free(line);
        
        current_line = next + 1;
        next = strchr(current_line, '\n');
    }
}

void parse_line(char *line, char *name, char *value)
{
    char *eq = strchr(line, '=');
    if (eq == 0)
    {
        return;
    }
    long name_length = eq - line + 1;
    unsigned long value_length = strlen(eq + 1) + 1;
    
    memset(name, 0, 64);
    strncpy(name, line, name_length - 1);
    name[name_length] = 0;
    
    memset(value, 0, 1024);
    strncpy(value, eq + 1, value_length - 1);
    value[value_length] = 0;
}

int get_setting_index(const char *name)
{
    for (int i = 0; i < kPrintSettingsCount; i++)
    {
        if (strcmp(name, printsetting_names[i]) == 0)
        {
            return i;
        }
    }
    return -1;
}
*/
// Ver.2.0.0.3 start
//void add_pjl_gd(char *pjl, char *appName, char *appVersion, setting_value values[], int command)
// Ver.2.0.4.2 Start
//void add_pjl_gd(char *pjl, char *printerName, char *appVersion, setting_value values[], int command)
void add_pjl_gd(char *pjl, char *printerName, char *hostName, char *appVersion, setting_value values[], int command)
// Ver.2.0.4.2 End
//void add_pjl(char *pjl, char *printerName, setting_value values[], int command)
// Ver.2.0.0.3 end
{
    char pjl_line[1024];
    switch (command)
    {
        case kPjlCommandPjlVersion:
        {
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], PJL_COMMAND_VERSKION);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandPjlIdentifier:
        {
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], PJL_IDENTIFIER);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandRipControlFlag:
        {
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], PJL_RIPCONTROL_FLAG);
            strcat(pjl, pjl_line);
            break;
        }
		// Ver.2.0.4.2 Start
		case kPjlCommandHostName:
		{
			sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], hostName);
            strcat(pjl, pjl_line);
            break;
		}
		// End
        case kPjlCommandSoftwereName:
        {
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], PJL_SOFTWERENAME);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandSoftwereVersion:
        {
			// Ver.2.0.0.3 start
            //sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], PJL_COMMAND_VERSKION);
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], appVersion);
			// Ver.2.0.0.3 end
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandColorMode:
        {
            setting_value value = values[kPrintSettingsColorMode];
            if (value.set == 0)
            {
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][value.int_value]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandOrientation:
        {
            setting_value value = values[kPrintSettingsOrientation];
            if (value.set == 0)
            {
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][value.int_value]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandDuplex:
        {
            setting_value value = values[kPrintSettingsDuplex];
            if (value.set == 0)
            {
                return;
            }
            int duplex = (value.int_value == 0 ? 0 : 1);
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][duplex]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandDuplexBinding:
        {
            setting_value value = values[kPrintSettingsDuplex];
            if (value.set == 0 || value.int_value == 0)
            {
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][value.int_value - 1]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandCopies:
        {
            setting_value value = values[kPrintSettingsCopies];
            setting_value sort_value = values[kPrintSettingsSort];
            // Ver.2.0.2.3 Start
            //if (value.set == 0 || sort_value.set == 0 || sort_value.int_value == 1)
            if (value.set == 0 || sort_value.set == 0 || sort_value.int_value == 0)
            // End
            {
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_INT, pjl_commands[command], value.int_value);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandQuantity:
        {
            setting_value value = values[kPrintSettingsCopies];
            setting_value sort_value = values[kPrintSettingsSort];
            // Ver.2.0.2.3 Start
            //if (value.set == 0 || sort_value.set == 0 || sort_value.int_value == 0)
            if (value.set == 0 || sort_value.set == 0 || sort_value.int_value == 1)
            // End
            {
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_INT, pjl_commands[command], value.int_value);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandOutputPaperSize:
        {
            setting_value value = values[kPrintSettingsPaperSize];
            if (value.set == 0)
            {
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][value.int_value]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandScaleToFit:
        {
            setting_value value = values[kPrintSettingsScaleToFit];
            if (value.set == 0)
            {
                return;
            }
            int index = value.int_value == 1 ? 0 : 1;
            
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][index]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandZoomRate:
        {
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], "100");
            strcat(pjl, pjl_line);
            break;
        }
        case kPJLCommandPaperType:
        {
            setting_value value = values[kPrintSettingsPaperType];
            if (value.set == 0)
            {
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][value.int_value]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandInputTray:
        {
            setting_value value = values[kPrintSettingsInputTray];
            if (value.set == 0)
            {
                return;
            }
            int auto_select = (value.int_value == 0 ? 1 : 0);
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][auto_select]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandInputTrayMediaSource:
        {
            setting_value value = values[kPrintSettingsInputTray];
            if (value.set == 0 || value.int_value == 0)
            {
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][value.int_value - 1]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandMultiup:
        {
            setting_value imposition = values[kPrintSettingsImposition];
            setting_value booklet = values[kPrintSettingsBooklet];
            setting_value imposition_order = values[kPrintSettingsImpositionOrder];
            if (imposition.set == 0)
            {
                return;
            }
            
            if ((imposition.int_value == 1 || imposition.int_value == 2) && booklet.int_value == 0)
            {
                int pagePerSheet = imposition.int_value - 1;    // 0:2UP, 1:4UP
                int direction = 0;
                if ((imposition_order.int_value % 2) > 0)
                {
                    direction = 1;
                }
                int order = 0;
                if (imposition_order.int_value == 4 || imposition_order.int_value == 5)
                {
                    order = 1;
                }
                
                if (pagePerSheet == 0 && direction == 0)
                {
                    // Nup 2LeftToRight
                    sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][1]);
                    strcat(pjl, pjl_line);
                }
                else if (pagePerSheet == 0 && direction == 1)
                {
                    // Nup 2RightToLeft
                    sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][2]);
                    strcat(pjl, pjl_line);
                }
                else if (pagePerSheet == 1 && direction == 0 && order == 0)
                {
                    // Nup 4LeftTopToRight
                    sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][3]);
                    strcat(pjl, pjl_line);
                }
                else if (pagePerSheet == 1 && direction == 0 && order == 1)
                {
                    // Nup 4LeftTopToBottom
                    sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][4]);
                    strcat(pjl, pjl_line);
                }
                else if (pagePerSheet == 1 && direction == 1 && order == 0)
                {
                    // Nup 4RightTopToLeft
                    sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][5]);
                    strcat(pjl, pjl_line);
                }
                else
                {
                    // Nup 4RightTopToBottom
                    sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][6]);
                    strcat(pjl, pjl_line);
                }
            }
            else
            {
                // Nup OFF
                sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][0]);
                strcat(pjl, pjl_line);
            }
            break;
        }
        case kPjlCommandBooklet:
        {
            setting_value imposition = values[kPrintSettingsImposition];
            setting_value booklet = values[kPrintSettingsBooklet];
            setting_value booklet_layout = values[kPrintSettingsBookletLayout];
            
            if (booklet.set == 0)
            {
                return;
            }
            
            if (imposition.int_value == 0 && booklet.int_value == 1)
            {
                // booklet ON
                sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][booklet_layout.int_value + 1]);
                strcat(pjl, pjl_line);
            }
            else
            {
                // booklet OFF
                sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][0]);
                strcat(pjl, pjl_line);
            }
            break;
        }
        case kPjlCommandBookletFinishing:
        {
            setting_value booklet = values[kPrintSettingsBooklet];
            setting_value booklet_finishing = values[kPrintSettingsBookletFinish];
            if (booklet.set == 0 || booklet_finishing.set == 0)
            {
                return;
            }
            if (booklet.int_value == 0)
            {
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][booklet_finishing.int_value]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandFinishingSide:
        {
            setting_value booklet = values[kPrintSettingsBooklet];
            setting_value finishing_side = values[kPrintSettingsFinishingSide];
            setting_value staple = values[kPrintSettingsStaple];
            setting_value punch = values[kPrintSettingsPunch];
            if (booklet.set == 0 || finishing_side.set == 0 || staple.set == 0 || punch.set == 0)
            {
                return;
            }
            if (staple.int_value == 0 && punch.int_value == 0)
            {
                return;
            }
            int side = finishing_side.int_value;
            if (booklet.int_value == 1)
            {
                side = 0;
            }
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][side]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandStaple:
        {
            setting_value staple = values[kPrintSettingsStaple];
            setting_value finishing_side = values[kPrintSettingsFinishingSide];
            if (finishing_side.set == 0 || staple.set == 0)
            {
                return;
            }
            
            if (staple.int_value == 0)
            {
                sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][0]);
            }
            else
            {
                if (finishing_side.int_value == 0)
                {
                    if (staple.int_value == 3)
                    {
                        sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][1]);
                    }
                    else if (staple.int_value == 4)
                    {
                        sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][4]);
                    }
                }
                else if (finishing_side.int_value == 2)
                {
                    if (staple.int_value == 3)
                    {
                        sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][1]);
                    }
                    else if (staple.int_value == 4)
                    {
                        sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][4]);
                    }
                }
                else if (finishing_side.int_value == 1)
                {
                    if (staple.int_value == 1)
                    {
                        sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][2]);
                    }
                    else if (staple.int_value == 2)
                    {
                        sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][3]);
                    }
                    else if (staple.int_value == 4)
                    {
                        sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][4]);
                    }
                }
            }
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandPunch:
        {
            setting_value punch = values[kPrintSettingsPunch];
            if (punch.set == 0)
            {
                return;
            }
            
            // 3holes Condition
            if (strstr(printerName, "ORPHIS") == NULL)
            {
                //if (punch.int_value == 1)
                if (punch.int_value == 2) // 4holes
                {
                    punch.int_value += 1;
                }
            }
            
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][punch.int_value]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandOutputTray:
        {
            setting_value outputTray = values[kPrintSettingsOutputTray];
            if (outputTray.set == 0)
            {
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][outputTray.int_value]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandOwner:
        {
            setting_value loginId = values[kPrintSettingsLoginId];
            if (loginId.set == 0)
            {
                
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], loginId.str_value);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandPrivatePrinting:
        {
            setting_value securePrint = values[kPrintSettingsSecurePrint];
            if (securePrint.set == 0)
            {
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][securePrint.int_value]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandPrivatePrintingBoxNumber:
        {
            setting_value securePrint = values[kPrintSettingsSecurePrint];
            setting_value pinCode = values[kPrintSettingsPinCode];
            if (pinCode.set == 0 || securePrint.set == 0 || securePrint.int_value == 0)
            {
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pinCode.str_value);
            strcat(pjl, pjl_line);
            break;
        }
    }
}

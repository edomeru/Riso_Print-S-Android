//
//  printsettings.c
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
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
    kPjlCommandColorMode,
    kPjlCommandOrientation,
    kPjlCommandCopies,
    kPjlCommandQuantity,
    kPjlCommandDuplex,
    kPjlCommandDuplexBinding,
    kPjlCommandOutputPaperSize,
    kPjlCommandScaleToFit,
    kPJLCommandPaperType,
    kPjlCommandInputTray,
    kPjlCommandInputTrayMediaSource,
    kPjlCommandImposition,
    kPjlCommandNupPagePerSheet,
    kPjlCommandImpositionDirection,
    kPjlCommandNupPageOrder,
    kPjlCommandCollate,
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

const char *printsetting_names[kPrintSettingsCount] =
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
};

const char *color_mode[] =
{
    "AUTO",
    "COLOR",
    "MONOCHROME"
};

const char *orientation[] =
{
    "PORTRAIT",
    "LANDSCAPE"
};

const char *duplex_binding[] =
{
    "LONGEDGE",
    "SHORTEDGE"
};

const char *paper_size[] =
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
    "STATEMENT"
};

const char *paper_type[] =
{
    "ANY",
    "PLAIN",
    "IJPAPER",
    "MATTCOATED",
    "HIGH-QUALITY",
    "CARD-IJ",
    "LWPAPER"
};

const char *input_tray_media_source[] =
{
    "STANDARD",
    "TRAY1",
    "TRAY2",
    "TRAY3"
};

const char *imposition[] =
{
    "SIMPLE",
    "BOOKLET",
    "NUP"
};

const char *nup_page_per_sheet[] =
{
    "2",
    "4"
};

const char *imposition_direction[] =
{
    "FORWARD",
    "REVERSE"
};

const char *nup_page_order[] =
{
    "HORIZONTAL",
    "VERTICAL"
};

const char *booklet_finishing[] =
{
    "NONFOLD",
    "HALF",
    "HALFSTAPLE"
};

const char *finishing_side[] =
{
    "LEFT",
    "TOP",
    "RIGHT"
};

const char *staple[] =
{
    "NONSTAPLE",
    "1STAPLELEFT",
    "1STAPLERIGHT",
    "2STAPLES"
};

const char *punch[] =
{
    "NONPUNCH",
    "2HOLES",
    "3-4HOLES"
};

const char *output_tray[] =
{
    "AUTO",
    "FACEDOWN",
    "TOP",
    "STACKING"
};

const char *off_on[] =
{
    "OFF",
    "ON"
};

const char *false_true[] =
{
    "FALSE",
    "TRUE"
};

const char **pjl_values[kPjlCommandCount] =
{
    color_mode,
    orientation,
    0,
    0,
    off_on,  // duplex
    duplex_binding,
    paper_size,
    false_true, // scale to fit
    paper_type,
    off_on, // input tray
    input_tray_media_source,
    imposition,
    nup_page_per_sheet,
    imposition_direction,
    nup_page_order,
    false_true, // collate
    booklet_finishing,
    finishing_side,
    staple,
    punch,
    output_tray,
    0,
    false_true,
    0
};


const char *pjl_commands[kPjlCommandCount] =
{
    "RKOUTPUTMODE",
    "ORIENTATION",
    "COPIES",
    "QTY",
    "DUPLEX",
    "BINDING",
    "PAPER",
    "RKSCALETOFIT",
    "MEDIATYPE",
    "AUTOSELECT",
    "MEDIASOURCE",
    "RKIMPOSITION",
    "RKNUPPAGESPERSHEET",
    "RKIMPOSITIONDIRECTION",
    "RKNUPPAGEORDER",
    "RKCOLLATE",
    "RKFOLDMODE",
    "RKFINISHSIDE",
    "RKSTAPLEMODE",
    "RKPUNCHMODE",
    "OUTBIN",
    "RKOWNERNAME",
    "RKPRIVATEPRINTING",
    "RKPRIVATEPRINTINGBOXNUMBER"
};

typedef struct
{
    int set;
    int int_value;
    char *str_value;
} setting_value;

void parse(char *settings, setting_value values[]);
void parse_line(char *line, char *name, char *value);
int get_setting_index(const char *name);
void add_pjl(char *pjl, setting_value values[], int command);

void create_pjl(char *pjl, char *settings)
{
    if (strlen(settings) == 0)
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
        add_pjl(pjl, values, i);
    }
    
    for (int i = 0; i < kPrintSettingsCount; i++)
    {
        if (values[i].str_value != 0)
        {
            free(values[i].str_value);
        }
    }
}

void parse(char *settings, setting_value values[])
{
    char *current_line = settings;
    char *next = strchr(current_line, '\n');
    while (next != 0)
    {
        int line_length = next - current_line + 1;
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
    int name_length = eq - line + 1;
    int value_length = strlen(eq + 1) + 1;
    
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

void add_pjl(char *pjl, setting_value values[], int command)
{
    char pjl_line[1024];
    switch (command)
    {
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
            if (value.set == 0 || sort_value.set == 0 || sort_value.int_value == 1)
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
            if (value.set == 0 || sort_value.set == 0 || sort_value.int_value == 0)
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
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][value.int_value]);
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
        case kPjlCommandImposition:
        {
            setting_value imposition = values[kPrintSettingsImposition];
            setting_value booklet = values[kPrintSettingsBooklet];
            if (imposition.set == 0 || booklet.set == 0)
            {
                return;
            }
            if (imposition.int_value == 0 && booklet.int_value == 0)
            {
                sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][0]);
                strcat(pjl, pjl_line);
            }
            else if (imposition.int_value == 0 && booklet.int_value == 1)
            {
                sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][1]);
                strcat(pjl, pjl_line);
            }
            else if ((imposition.int_value == 1 || imposition.int_value == 2) && booklet.int_value == 0)
            {
                sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][2]);
                strcat(pjl, pjl_line);
            }
            break;
        }
        case kPjlCommandNupPagePerSheet:
        {
            setting_value imposition = values[kPrintSettingsImposition];
            setting_value booklet = values[kPrintSettingsBooklet];
            if (imposition.set == 0 || booklet.set == 0)
            {
                return;
            }
            if (booklet.int_value == 1 || (imposition.int_value == 0 && booklet.int_value == 0))
            {
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][imposition.int_value - 1]);
            strcat(pjl, pjl_line);
            break;
        }
        case kPjlCommandImpositionDirection:
        {
            setting_value imposition = values[kPrintSettingsImposition];
            setting_value booklet = values[kPrintSettingsBooklet];
            setting_value imposition_order = values[kPrintSettingsImpositionOrder];
            setting_value booklet_layout = values[kPrintSettingsBookletLayout];
            if (imposition.set == 0 || booklet.set == 0 || imposition_order.set == 0 || booklet_layout.set == 0)
            {
                return;
            }
            if (imposition.int_value == 1 && booklet.int_value == 0)
            {
                // 2-up
                sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][imposition_order.int_value]);
                strcat(pjl, pjl_line);
            }
            else if (imposition.int_value == 2 && booklet.int_value == 0)
            {
                // 4-up
                int direction = 0;
                if ((imposition_order.int_value % 2) > 0)
                {
                    direction = 1;
                }
                sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][direction]);
                strcat(pjl, pjl_line);
            }
            else if (booklet.int_value == 1 && imposition.int_value == 0)
            {
                // Booklet
                int direction = 0;
                if (booklet_layout.int_value == 1)
                {
                    direction = 1;
                }
                sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][direction]);
                strcat(pjl, pjl_line);
            }
            break;
        }
        case kPjlCommandNupPageOrder:
        {
            setting_value imposition = values[kPrintSettingsImposition];
            setting_value booklet = values[kPrintSettingsBooklet];
            setting_value imposition_order = values[kPrintSettingsImpositionOrder];
            setting_value booklet_layout = values[kPrintSettingsBookletLayout];
            if (imposition.set == 0 || booklet.set == 0 || imposition_order.set == 0 || booklet_layout.set == 0)
            {
                return;
            }
            if (imposition.int_value == 2 && booklet.int_value == 0)
            {
                // 4-up
                int direction = 0;
                if (imposition_order.int_value == 4 || imposition_order.int_value == 5)
                {
                    direction = 1;
                }
                sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][direction]);
                strcat(pjl, pjl_line);
            }
            if (booklet.int_value == 1 && imposition.int_value == 0)
            {
                // Booklet
                if (booklet_layout.int_value == 2)
                {
                    sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][1]);
                    strcat(pjl, pjl_line);
                }
            }
            break;
        }
        case kPjlCommandCollate:
        {
            setting_value sort = values[kPrintSettingsSort];
            if (sort.set == 0)
            {
                return;
            }
            sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][sort.int_value]);
            strcat(pjl, pjl_line);
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
                        sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][3]);
                    }
                }
                else if (finishing_side.int_value == 2)
                {
                    if (staple.int_value == 3)
                    {
                        sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][2]);
                    }
                    else if (staple.int_value == 4)
                    {
                        sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][3]);
                    }
                }
                else if (finishing_side.int_value == 1)
                {
                    if (staple.int_value == 1)
                    {
                        sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][1]);
                    }
                    else if (staple.int_value == 2)
                    {
                        sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][2]);
                    }
                    else if (staple.int_value == 4)
                    {
                        sprintf(pjl_line, PJL_COMMAND_STR, pjl_commands[command], pjl_values[command][3]);
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

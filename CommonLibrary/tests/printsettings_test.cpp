#include "gtest/gtest.h"

extern "C"
{
#include "printsettings.h"
}

const char *SETTING_NAMES[] = 
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
    "outputTray"
};

// Utitilty
void setup_settings(char *settings, int values[])
{
    strcpy(settings, "");
    char item[64];
    for (int i = 0; i < 18; i++)
    {
        sprintf(item, "%s=%d\n", SETTING_NAMES[i], values[i]);
        strcat(settings, item);
    }
}

// Normal values

TEST(PrintSettingsTest, Empty)
{
    char input[1024];
    char output[1024];

    // initialize input and outputs
    strcpy(input, "");
    strcpy(output, "");

    create_pjl(output, input);

    EXPECT_EQ(0, strlen(output));
}

TEST(PrintSettingsTest, Default)
{
    char input[1024];
    char output[1024];

    // initialize output
    strcpy(output, "");

    // setup input
    int values[] = 
    {
        0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0 ,0 ,0 ,0 ,0, 0 ,0
    };
    setup_settings(input, values);

    create_pjl(output, input);

    EXPECT_TRUE(strlen(output) > 0);
}

TEST(PrintSettingsTest, InvalidFormat)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(input, "invalid\n");
    strcpy(output, "");

    create_pjl(output, input);
    
    char pjl[64];
    sprintf(pjl, "@PJL SET RKPRIVATEPRINTING=FALSE\x0d\x0a");
    EXPECT_TRUE(strstr(output, pjl) != 0);
}

// Color Mode

TEST(PrintSettingsTest, ColorMode)
{
    char input[1024];
    char output[1024];

    const char *colorModes[] = 
    {
        "AUTO", "COLOR", "MONOCHROME"
    };
    
    for (int i = 0; i < 3; i++)
    {
        // initialize input and output
        strcpy(output, "");
        sprintf(input, "colorMode=%d\n", i);

        create_pjl(output, input);

        char pjl[64];
        sprintf(pjl, "@PJL SET RKOUTPUTMODE=%s\x0d\x0a", colorModes[i]);
        EXPECT_TRUE(strstr(output, pjl) != 0);
    }
}

// Orientation

TEST(PrintSettingsTest, Orientation)
{
    char input[1024];
    char output[1024];

    const char *orientations[] =
    {
        "PORTRAIT", "LANDSCAPE"
    };
    
    for (int i = 0; i < 2; i++)
    {
        // initialize input and output
        strcpy(output, "");
        sprintf(input, "orientation=%d\n", i);

        create_pjl(output, input);

        char pjl[64];
        sprintf(pjl, "@PJL SET ORIENTATION=%s\x0d\x0a", orientations[i]);
        EXPECT_TRUE(strstr(output, pjl) != 0);
    }
}

// Copies

TEST(PrintSettingsTest, CopiesSort)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "copies=1\n");
    strcat(input, "sort=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET COPIES=1\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKCOLLATE=FALSE\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, CopiesCollate)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "copies=1\n");
    strcat(input, "sort=1\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET QTY=1\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKCOLLATE=TRUE\x0d\x0a") != 0);
}

// Duplex

TEST(PrintSettingsTest, DuplexIsOnBindingIsOff)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "duplex=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET DUPLEX=OFF\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, DuplexIsOnBindingIsOn)
{
    char input[1024];
    char output[1024];

    const char *duplexModes[] =
    {
        "LONGEDGE", "SHORTEDGE"
    };
    for (int i = 0; i < 2; i++)
    {
        // initialize input and output
        strcpy(output, "");
        sprintf(input, "duplex=%d\n", i + 1);

        create_pjl(output, input);

        char pjl[64];
        sprintf(pjl, "@PJL SET BINDING=%s\x0d\x0a", duplexModes[i]);
        EXPECT_TRUE(strstr(output, "@PJL SET DUPLEX=ON\x0d\x0a") != 0);
        EXPECT_TRUE(strstr(output, pjl) != 0);
    }
}

// Output paper size

TEST(PrintSettingsTest, OutputPaperSize)
{
    char input[1024];
    char output[1024];

    const char *paperSizes[] =
    {
        "A3", "A3W", "A4", "A5", "A6", "B4", "B5", "B6", "FOOLSCAP", "TABLOID", "LEGAL", "LETTER", "STATEMENT"
    };
   
    for (int i = 0; i < 13; i++)
    {
        // initialize input and output
        strcpy(output, "");
        sprintf(input, "paperSize=%d\n", i);

        create_pjl(output, input);

        char pjl[64];
        sprintf(pjl, "@PJL SET PAPER=%s\x0d\x0a", paperSizes[i]);
        EXPECT_TRUE(strstr(output, pjl) != 0);
    }
}

// Scale to fit

TEST(PrintSettingsTest, ScaleToFit)
{
    char input[1024];
    char output[1024];

    const char *scaleToFit[] =
    {
        "FALSE", "TRUE"
    };
   
    for (int i = 0; i < 2; i++)
    {
        // initialize input and output
        strcpy(output, "");
        sprintf(input, "scaleToFit=%d\n", i);

        create_pjl(output, input);

        char pjl[64];
        sprintf(pjl, "@PJL SET RKSCALETOFIT=%s\x0d\x0a", scaleToFit[i]);
        EXPECT_TRUE(strstr(output, pjl) != 0);
    }
}

// Paper type

TEST(PrintSettingsTest, PaperType)
{
    char input[1024];
    char output[1024];

    const char *paperTypes[] =
    {
        "ANY", "PLAIN", "IJPAPER", "MATTCOATED", "HIGH-QUALITY", "CARD-IJ", "LWPAPER"
    };
   
    for (int i = 0; i < 7; i++)
    {
        // initialize input and output
        strcpy(output, "");
        sprintf(input, "paperType=%d\n", i);

        create_pjl(output, input);

        char pjl[64];
        sprintf(pjl, "@PJL SET MEDIATYPE=%s\x0d\x0a", paperTypes[i]);
        EXPECT_TRUE(strstr(output, pjl) != 0);
    }
}

// Input Tray

TEST(PrintSettingsTest, InputTrayIsAuto)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(input, "inputTray=0\n");
    strcpy(output, "");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET AUTOSELECT=ON\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, InputTrayIsNotAuto)
{
    char input[1024];
    char output[1024];

    const char *inputTrays[] = 
    {
        "STANDARD", "TRAY1", "TRAY2", "TRAY3"
    };

    for (int i = 0; i < 4; i++)
    {
        // initialize input and output
        sprintf(input, "inputTray=%d\n", i + 1);
        strcpy(output, "");

        create_pjl(output, input);

        char pjl[64];
        sprintf(pjl, "@PJL SET MEDIASOURCE=%s\x0d\x0a", inputTrays[i]);
        EXPECT_TRUE(strstr(output, "@PJL SET AUTOSELECT=OFF\x0d\x0a") != 0);
        EXPECT_TRUE(strstr(output, pjl) != 0);
    }
}

// Imposition, Imposition order

TEST(PrintSettingsTest, 2upLR)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "imposition=1\n");
    strcat(input, "impositionOrder=0\n");
    strcat(input, "booklet=0\n");
    strcat(input, "bookletLayout=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITION=NUP\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKNUPPAGESPERSHEET=2\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITIONDIRECTION=FORWARD\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, 2upRL)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "imposition=1\n");
    strcat(input, "impositionOrder=1\n");
    strcat(input, "booklet=0\n");
    strcat(input, "bookletLayout=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITION=NUP\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKNUPPAGESPERSHEET=2\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITIONDIRECTION=REVERSE\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, 4upULR)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "imposition=2\n");
    strcat(input, "impositionOrder=2\n");
    strcat(input, "booklet=0\n");
    strcat(input, "bookletLayout=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITION=NUP\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKNUPPAGESPERSHEET=4\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITIONDIRECTION=FORWARD\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKNUPPAGEORDER=HORIZONTAL\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, 4upURL)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "imposition=2\n");
    strcat(input, "impositionOrder=3\n");
    strcat(input, "booklet=0\n");
    strcat(input, "bookletLayout=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITION=NUP\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKNUPPAGESPERSHEET=4\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITIONDIRECTION=REVERSE\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKNUPPAGEORDER=HORIZONTAL\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, 4upULB)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "imposition=2\n");
    strcat(input, "impositionOrder=4\n");
    strcat(input, "booklet=0\n");
    strcat(input, "bookletLayout=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITION=NUP\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKNUPPAGESPERSHEET=4\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITIONDIRECTION=FORWARD\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKNUPPAGEORDER=VERTICAL\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, 4upURB)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "imposition=2\n");
    strcat(input, "impositionOrder=5\n");
    strcat(input, "booklet=0\n");
    strcat(input, "bookletLayout=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITION=NUP\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKNUPPAGESPERSHEET=4\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITIONDIRECTION=REVERSE\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKNUPPAGEORDER=VERTICAL\x0d\x0a") != 0);
}

// Booklet, booklet layout

TEST(PrintSettingsTest, BookletLR)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(input, "imposition=0\n");
    strcat(input, "impositionOrder=0\n");
    strcat(input, "booklet=1\n");
    strcat(input, "bookletFinish=0\n");
    strcat(input, "bookletLayout=0\n");
    strcpy(output, "");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITION=BOOKLET\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITIONDIRECTION=FORWARD\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, BookletRL)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(input, "imposition=0\n");
    strcat(input, "impositionOrder=0\n");
    strcat(input, "booklet=1\n");
    strcat(input, "bookletFinish=0\n");
    strcat(input, "bookletLayout=1\n");
    strcpy(output, "");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITION=BOOKLET\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITIONDIRECTION=REVERSE\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, BookletTB)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(input, "imposition=0\n");
    strcat(input, "impositionOrder=0\n");
    strcat(input, "booklet=1\n");
    strcat(input, "bookletFinish=0\n");
    strcat(input, "bookletLayout=2\n");
    strcpy(output, "");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITION=BOOKLET\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKIMPOSITIONDIRECTION=FORWARD\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKNUPPAGEORDER=VERTICAL\x0d\x0a") != 0);
}

// Finishing side

TEST(PrintSettingsTest, FinishingLeft)
{
    char input[1024];
    char output[1024];

    const char* finishingSides[] =
    {
        "LEFT", "TOP", "RIGHT"
    };

    for (int i = 0; i < 3; i++)
    {
        // initialize input and output
        strcpy(output, "");
        sprintf(input, "booklet=0\nfinishingSide=%d\nstaple=1\npunch=1\n", i);

        create_pjl(output, input);

        char pjl[64];
        sprintf(pjl, "@PJL SET RKFINISHSIDE=%s\x0d\x0a", finishingSides[i]);
        EXPECT_TRUE(strstr(output, pjl) != 0);
    }
}

// Booklet finishing

TEST(PrintSettingsTest, BookletFinishing)
{
    char input[1024];
    char output[1024];

    const char *bookletFinishing[] = 
    {
        "NONFOLD", "HALF", "HALFSTAPLE"
    };

    for (int i = 0; i < 3; i++)
    {
        // initialize input and output
        strcpy(output, "");
        sprintf(input, "booklet=1\nbookletFinish=%d\n", i);

        create_pjl(output, input);

        char pjl[64];
        sprintf(pjl, "@PJL SET RKFOLDMODE=%s\x0d\x0a", bookletFinishing[i]);
        EXPECT_TRUE(strstr(output, pjl) != 0);
    }
}

// Combinations

TEST(PrintSettingsTest, CopiesWithoutSort)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "copies=1\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "COPIES") == 0);
    EXPECT_TRUE(strstr(output, "QTY") == 0);
}

TEST(PrintSettingsTest, ImpositionWithoutBooklet)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "imposition=1\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "RKIMPOSITION") == 0);
}

TEST(PrintSettingsTest, BookletWithStaplePunch)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "booklet=1\n");
    strcat(input, "finishingSide=0\n");
    strcat(input, "staple=1\n");
    strcat(input, "punch=1\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKFINISHSIDE=LEFT\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, BookletWithoutStaple)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "booklet=1\n");
    strcat(input, "finishingSide=0\n");
    strcat(input, "punch=1\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKFINISHSIDE=LEFT\x0d\x0a") == 0);
}

TEST(PrintSettingsTest, BookletWithoutPunch)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "booklet=1\n");
    strcat(input, "finishingSide=0\n");
    strcat(input, "staple=1\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKFINISHSIDE=LEFT\x0d\x0a") == 0);
}

TEST(PrintSettingsTest, BookletStapleOnPunchOff)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "booklet=1\n");
    strcat(input, "finishingSide=0\n");
    strcat(input, "staple=1\n");
    strcat(input, "punch=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKFINISHSIDE=LEFT\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, BookletStapleOffPunchOn)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "booklet=1\n");
    strcat(input, "finishingSide=0\n");
    strcat(input, "staple=0\n");
    strcat(input, "punch=1\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKFINISHSIDE=LEFT\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, StapleLeft1)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "booklet=0\n");
    strcat(input, "finishingSide=0\n");
    strcat(input, "staple=3\n");
    strcat(input, "punch=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKFINISHSIDE=LEFT\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKSTAPLEMODE=1STAPLELEFT\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, StapleLeft2)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "booklet=0\n");
    strcat(input, "finishingSide=0\n");
    strcat(input, "staple=4\n");
    strcat(input, "punch=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKFINISHSIDE=LEFT\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKSTAPLEMODE=2STAPLES\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, StapleTop1Left)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "booklet=0\n");
    strcat(input, "finishingSide=1\n");
    strcat(input, "staple=1\n");
    strcat(input, "punch=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKFINISHSIDE=TOP\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKSTAPLEMODE=1STAPLELEFT\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, StapleTop1Right)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "booklet=0\n");
    strcat(input, "finishingSide=1\n");
    strcat(input, "staple=2\n");
    strcat(input, "punch=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKFINISHSIDE=TOP\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKSTAPLEMODE=1STAPLERIGHT\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, StapleTop2)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "booklet=0\n");
    strcat(input, "finishingSide=1\n");
    strcat(input, "staple=4\n");
    strcat(input, "punch=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKFINISHSIDE=TOP\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKSTAPLEMODE=2STAPLES\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, StapleRight1)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "booklet=0\n");
    strcat(input, "finishingSide=2\n");
    strcat(input, "staple=3\n");
    strcat(input, "punch=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKFINISHSIDE=RIGHT\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKSTAPLEMODE=1STAPLERIGHT\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, StapleRight2)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "booklet=0\n");
    strcat(input, "finishingSide=2\n");
    strcat(input, "staple=4\n");
    strcat(input, "punch=0\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKFINISHSIDE=RIGHT\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKSTAPLEMODE=2STAPLES\x0d\x0a") != 0);
}

// Authentication

TEST(PrintSettingsTest, OwnerOK_PinEmpty)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "loginId=ownername\n");
    strcat(input, "pinCode=\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKOWNERNAME=ownername\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKPRIVATEPRINTING=TRUE\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKPRIVATEPRINTINGBOXNUMBER=\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, OwnerOK_PinOK)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "loginId=ownername\n");
    strcat(input, "pinCode=12345678\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKOWNERNAME=ownername\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKPRIVATEPRINTING=TRUE\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKPRIVATEPRINTINGBOXNUMBER=12345678\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, OwnerNG_PinOK)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "loginId=\n");
    strcat(input, "pinCode=12345678\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKOWNERNAME=\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKPRIVATEPRINTING=TRUE\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKPRIVATEPRINTINGBOXNUMBER=12345678\x0d\x0a") != 0);
}

TEST(PrintSettingsTest, OwnerNG_PinNG)
{
    char input[1024];
    char output[1024];

    // initialize input and output
    strcpy(output, "");
    strcpy(input, "loginId=\n");
    strcat(input, "pinCode=\n");

    create_pjl(output, input);

    EXPECT_TRUE(strstr(output, "@PJL SET RKOWNERNAME=\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKPRIVATEPRINTING=TRUE\x0d\x0a") != 0);
    EXPECT_TRUE(strstr(output, "@PJL SET RKPRIVATEPRINTINGBOXNUMBER=\x0d\x0a") != 0);
}

//
//  PreviewSetting.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

#define KEY_ORIENTATION         @"orientation"
#define KEY_COPIES              @"copies"
#define KEY_DUPLEX              @"duplex"
#define KEY_PAPER_SIZE          @"paperSize"
#define KEY_IMPOSITION          @"imposition"
#define KEY_IMPOSITION_ORDER    @"impositionOrder"
#define KEY_BOOKLET             @"booklet"
#define KEY_BOOKLET_FINISH      @"bookletFinish"
#define KEY_BOOKLET_LAYOUT      @"bookletLayout"
#define KEY_FINISHING_SIDE      @"finishingSide"
#define KEY_STAPLE              @"staple"
#define KEY_PUNCH               @"punch"
#define KEY_OUTPUT_TRAY         @"outputTray"
#define KEY_INPUT_TRAY          @"inputTray"
#define KEY_SORT                @"sort"
#define KEY_PAPER_TYPE          @"paperType"
#define KEY_SECURE_PRINT        @"securePrint"
#define KEY_PIN_CODE            @"pinCode"
#define KEY_LOGIN_ID            @"loginId"

/**
 * Model for the print settings currently applied to the PDF document object.
 * This also includes the Secure Print settings.\n
 * This is the data used when displaying the print preview content and when
 * generating the PJL command string.
 * 
 * @see PrintSetting
 * @see PrintPreviewHelper
 */
@interface PreviewSetting : NSObject

/**
 * Color Mode print setting.
 * Defines the color mode of the print job.
 *
 * @see kColorMode
 */
@property (nonatomic) NSInteger colorMode;

/**
 * Orientation print setting.
 * Defines the page orientation (will depend on the first page of the PDF).
 *
 * @see kOrientation
 */
@property (nonatomic) NSInteger orientation;

/**
 * Copies print setting.
 * Number of copies to be printed.
 *  - range: 1 to 9999
 *  - default: 1
 */
@property (nonatomic) NSInteger copies;

/**
 * Duplex print setting.
 * Determines the duplex printing mode.
 *
 * @see kDuplexSetting
 */
@property (nonatomic) NSInteger duplex;

/**
 * Paper Size print setting.
 * Sets the paper type to be used during print.
 *
 * @see kPaperSize
 */
@property (nonatomic) NSInteger paperSize;

/**
 * Scale-to-Fit print setting.
 * Whether the PDF page will be scaled to fit the whole page.
 *  - Off
 *  - On [DEFAULT]
 */
@property (nonatomic) BOOL scaleToFit;

/**
 * Paper Type print setting.
 * Determines the type of paper during print (depends on the machine configuration).
 *  - Any [DEFAULT]
 *  - Plain
 *  - IJ Paper
 *  - Matt Coated
 *  - High Quality
 *  - Card-IJ
 *  - LW Paper
 */
@property (nonatomic) NSInteger paperType;

/**
 * Input Tray print setting
 * Selects the tray location of the paper input.
 *  - Auto [DEFAULT]
 *  - Standard
 *  - Tray 1
 *  - Tray 2 
 *  - Tray 3
 */
@property (nonatomic) NSInteger inputTray;

/**
 * Imposition print setting
 * Number of pages to print per sheet.
 *
 * @see kImposition
 */
@property (nonatomic) NSInteger imposition;

/**
 * Imposition Order print setting
 * Direction of the PDF pages printed in one sheet.
 *
 * @see kImpositionOrder
 */
@property (nonatomic) NSInteger impositionOrder;

/**
 * Sort print setting
 * Defines how the print output will be sorted.
 *  - Group [DEFAULT]
 *  - Sort
 */
@property (nonatomic) NSInteger sort;

/**
 * Booklet print setting
 * Print the pages in booklet format.
 *  - Off [DEFAULT]
 *  - On
 */
@property (nonatomic) BOOL booklet;

/**
 * Booklet Binding print setting
 * Finishing options for when booklet is on.
 *
 * @see kBookletType
 */
@property (nonatomic) NSInteger bookletFinish;

/**
 * Booklet Layout print setting
 * Direction of pages when booklet is on.
 *
 * @see kBookletLayout
 */
@property (nonatomic) NSInteger bookletLayout;

/**
 * Binding Side print setting
 * Refers to the edge where the document will be bound.
 *
 * @see kFinishingSide
 */
@property (nonatomic) NSInteger finishingSide;

/**
 * Staple print setting
 * The print job will be stapled on the specified position.
 *
 * @see kStapleType
 */
@property (nonatomic) NSInteger staple;

/**
 * Punch print setting
 * The printer will make a punch in the print output.
 *
 * @see kPunchType
 */
@property (nonatomic) NSInteger punch;

/**
 * Output Tray print setting
 * Selects the tray location of the finished copies (depends on the machine configuration).
 *
 * @see kOutputTray
 */
@property (nonatomic) NSInteger outputTray;

/**
 * Flag that indicates whether Secure Printing is enabled.
 */
@property (nonatomic) BOOL securePrint;

/**
 * Stores the Pin Code specified by the user in the "Print Settings" screen.
 */
@property (nonatomic) NSString  *pinCode;

/**
 * Formats the values of the PreviewSetting properties into a single string
 * for use in PJL generation.
 */
@property (nonatomic, weak, readonly) NSString *formattedString;

/**
 * Flag that indicates whether Punch 3 Hole is selected.
 */
@property (nonatomic) BOOL isPunch3Selected;

@end

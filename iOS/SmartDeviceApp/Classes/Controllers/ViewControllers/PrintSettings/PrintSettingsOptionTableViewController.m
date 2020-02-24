//
//  PrintSettingsOptionTableViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintSettingsOptionTableViewController.h"
#import "PrintSettingsOptionsHeaderCell.h"
#import "PrintSettingsOptionsItemCell.h"
#import "UIView+Localization.h"
#import "PDFFileManager.h"
#import "PrintDocument.h"
#import "PreviewSetting.h"
#import "PrintPreviewHelper.h"
#import "PrintSettingsHelper.h"

#define OPTIONS_HEADER_CELL @"OptionsHeaderCell"
#define OPTIONS_ITEM_CELL @"OptionsItemCell"

@interface PrintSettingsOptionTableViewController ()

/**
 * Stores the currently selected print setting value.
 */
@property (nonatomic) NSInteger selectedIndex;

/**
 * The name of the print setting.
 * This corresponds to the <name> tag in the printsettings.xml.
 */
@property (nonatomic, strong) NSString *key;

/**
 * The defined possible values of the print setting.
 * This corresponds to the <option> tags in the printsettings.xml.
 */
@property (nonatomic, strong) NSMutableArray *options;

/**
 * The applicable possible values of the print setting.
 * This depends on the other print settings set in the PreviewSetting object.
 */
@property (nonatomic, strong) NSMutableArray *optionValues;

/**
 * Sets-up the contents of {@link options} and {@link optionValues}.
 */
- (void)fillOptions;

/**
 * Checks if the print setting option is applicable based on the current PreviewSetting set.
 * 
 * @param option the print setting option
 * @return YES if applicable, NO otherwise
 */
- (BOOL)isApplicableOption:(NSString *)option;

@end

@implementation PrintSettingsOptionTableViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    // RQM-1902-003: allow multiple selection so when the header cell is selected, the selected setting will not be deselected (animation issue only)
    self.tableView.allowsMultipleSelection = YES;

    if (@available(iOS 13.0, *)) {
        self.tableView.backgroundColor = [UIColor colorNamed:@"color_gray2_gray5"];
    }

    self.key = [self.setting objectForKey:@"name"];
    
    [self fillOptions];
    NSNumber *value = [self.previewSetting valueForKey:self.key];   
    self.selectedIndex = [self.optionValues indexOfObject:value];
    
    // Add empty footer
    UIView *footer = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, 1.0f, 20.0f)];
    footer.backgroundColor = [UIColor clearColor];
    self.tableView.tableFooterView = footer;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    return [self.options count] + 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell;
    
    if (indexPath.row == 0)
    {
        PrintSettingsOptionsHeaderCell *headerCell = [tableView dequeueReusableCellWithIdentifier:OPTIONS_HEADER_CELL forIndexPath:indexPath];
        headerCell.settingLabel.localizationId = [self.setting objectForKey:@"text"];
        headerCell.selectionStyle = UITableViewCellSelectionStyleNone;
        cell = headerCell;
    }
    else
    {
        PrintSettingsOptionsItemCell *itemCell = [tableView dequeueReusableCellWithIdentifier:OPTIONS_ITEM_CELL forIndexPath:indexPath];
        itemCell.optionLabel.localizationId = [[self.options objectAtIndex:indexPath.row - 1] objectForKey:@"content-body"];
        if ((indexPath.row - 1) == self.selectedIndex)
        {
            [tableView selectRowAtIndexPath:indexPath animated:NO scrollPosition:UITableViewScrollPositionNone];
        }
        itemCell.separator.hidden = NO;
        if (indexPath.row == self.options.count)
        {
            itemCell.separator.hidden = YES;
        }
        cell = itemCell;
    }
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row == 0)
    {
        [self.navigationController popToRootViewControllerAnimated:YES];
        return;
    }
    
    int index = (int)indexPath.row - 1;
    if (index != self.selectedIndex)
    {
        // RQM-1902-003: deselect previously selected setting since multiple selection has been allowed
        [tableView deselectRowAtIndexPath:[tableView.indexPathsForSelectedRows firstObject] animated:YES];

        self.selectedIndex = index;

        // BUG #7237 Fix: Set isPunch3Selected flag
        if ([self.key isEqualToString:KEY_PUNCH] && [[[self.options objectAtIndex:index] objectForKey:@"content-body"] isEqualToString:[IDS_LBL_PUNCH_3HOLES lowercaseString]]) {
            [self.previewSetting setValue:[NSNumber numberWithBool:YES] forKey:@"isPunch3Selected"];
        } else {
            [self.previewSetting setValue:[NSNumber numberWithBool:NO] forKey:@"isPunch3Selected"];
        }
        [self.previewSetting setValue:[self.optionValues objectAtIndex:index] forKey:self.key];
    }
}

- (NSIndexPath *)tableView:(UITableView *)tableView willDeselectRowAtIndexPath:(NSIndexPath *)indexPath
{
    // disable deselection
    return nil;
}

-(BOOL) isApplicableOption:(NSString *)option
{
    if([self.key isEqualToString:KEY_STAPLE] == YES)
    {
        if(self.previewSetting.finishingSide == kFinishingSideTop)
        {
            if([option isEqualToString:@"ids_lbl_staple_1"] == YES)
            {
                return NO;
            }
        }
        else
        {
            if([option isEqualToString:@"ids_lbl_staple_upperleft"] == YES ||
               [option isEqualToString:@"ids_lbl_staple_upperright"] == YES)
            {
                return NO;
            }
        }
    }
    
    if([self.key isEqualToString:KEY_IMPOSITION_ORDER] == YES)
    {
        if([option isEqualToString:@"ids_lbl_imposition_order_2up_lr"] == YES ||
           [option isEqualToString:@"ids_lbl_imposition_order_2up_rl"] == YES )
        {
            if(self.previewSetting.imposition == kImposition4pages)
            {
                return NO;
            }
        }
        else
        {
            if(self.previewSetting.imposition == kImposition2Pages)
            {
                return NO;
            }
        }
    }
    
#if OUTPUT_TRAY_CONSTRAINT_ENABLED
    if([self.key isEqualToString:KEY_OUTPUT_TRAY] == YES)
    {
        if(self.previewSetting.bookletFinish != kBookletTypeOff)
        {
            if([option isEqualToString:@"ids_lbl_outputtray_auto"] == NO)
            {
                return NO;
            }
        }
        if(self.previewSetting.punch != kPunchTypeNone)
        {
            if([option isEqualToString:@"ids_lbl_outputtray_facedown"] == YES)
            {
                return NO;
            }
        }
    }
#endif //OUTPUT_TRAY_CONSTRAINT_ENABLED

    // Enum adjustments for printers where some options in GL are not available (e.g. FT)
    long adjustment = 0;
    if ([PrintSettingsHelper isFTSeries:self.printerName] == YES)
    {
        adjustment = 1;
    }
    
    // Input Tray (External Feeder) Constraint
    if ([PrintSettingsHelper isFTorGLSeries:self.printerName] == YES && [self.key isEqualToString:KEY_INPUT_TRAY] == YES)
    {
        if (self.previewSetting.paperSize + adjustment != kPaperSizeA4 &&
            self.previewSetting.paperSize + adjustment != kPaperSizeB5 &&
            self.previewSetting.paperSize + adjustment != kPaperSizeLetter &&
            self.previewSetting.paperSize + adjustment != kPaperSize16K) {
            if ([option isEqualToString:@"ids_lbl_inputtray_external"]) {
                return NO;
            }
        }
    }

    // Input Tray (External Feeder) Constraint
    if ([PrintSettingsHelper isFTorGLSeries:self.printerName] == YES && [self.key isEqualToString:KEY_PAPER_SIZE] == YES)
    {
        if (self.previewSetting.inputTray + adjustment == kInputTrayFTGLExternal) {
            if (![option isEqualToString:@"ids_lbl_papersize_a4"] &&
                ![option isEqualToString:@"ids_lbl_papersize_b5"] &&
                ![option isEqualToString:@"ids_lbl_papersize_letter"] &&
                ![option isEqualToString:@"ids_lbl_papersize_16K"]) {
                return NO;
            }
        }
    }
    return YES;
}

-(void) fillOptions
{
    self.options = [[NSMutableArray array] mutableCopy];
    self.optionValues = [[NSMutableArray array] mutableCopy];
    NSArray *options = [self.setting objectForKey:@"option"];
    NSInteger count =options.count;
    for(int index = 0; index < count; index++)
    {
        if([self isApplicableOption:[[options objectAtIndex:index] objectForKey:@"content-body"] ] == YES)
        {
            [self.options addObject:[options objectAtIndex:index]];
            [self.optionValues addObject:[NSNumber numberWithInt:index]];
        }
    }
}


@end

//
//  PrintSettingsTableViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintSettingsTableViewController.h"
#import "PrintSettingsOptionTableViewController.h"
#import "PrintSettingsPrinterItemCell.h"
#import "PrintSettingsHeaderCell.h"
#import "PrintSettingsItemOptionCell.h"
#import "PrintSettingsItemInputCell.h"
#import "PrintSettingsItemSwitchCell.h"
#import "PrintSettingsPrinterListTableViewController.h"
#import "PrintSettingsHelper.h"
#import "UIView+Localization.h"
#import "PDFFileManager.h"
#import "PrintDocument.h"
#import "PreviewSetting.h"
#import "PrinterManager.h"
#import "Printer.h"
#import "PrintPreviewHelper.h"
#import "PrintSetting.h"
#import "DirectPrintManager.h"
#import "AlertHelper.h"
#import "UIViewController+Segue.h"
#import "PrintJobHistoryViewController.h"
#import "NetworkManager.h"

#define PRINTER_HEADER_CELL @"PrinterHeaderCell"
#define PRINTER_ITEM_CELL @"PrinterItemCell"
#define PRINTER_ITEM_DEFAULT_CELL @"PrinterItemDefaultCell"
#define SETTING_HEADER_CELL @"SettingHeaderCell"
#define SETTING_ITEM_OPTION_CELL @"SettingItemOptionCell"
#define SETTING_ITEM_INPUT_CELL @"SettingItemInputCell"
#define SETTING_ITEM_SWITCH_CELL @"SettingItemSwitchCell"
#define PINCODE_INPUT_CELL @"PincodeInputCell"

#define PRINTER_SECTION  0
#define PRINTER_SECTION_HEADER_ROW 0
#define PRINTER_SECTION_ITEM_ROW 1

#define ROW_HEIGHT_SINGLE 44
#define ROW_HEIGHT_DOUBLE 55

static NSString *printSettingsPrinterContext = @"PrintSettingsPrinterContext";

@interface PrintSettingsTableViewController ()<DirectPrintManagerDelegate>

@property (nonatomic) BOOL isDefaultSettingsMode;

@property (nonatomic, strong) PreviewSetting *previewSetting;
@property (nonatomic, strong) PrintDocument *printDocument;
@property (nonatomic, weak) Printer *printer;
@property (nonatomic, weak) NSDictionary *printSettingsTree;
@property (nonatomic, strong) NSMutableArray *expandedSections;
@property (nonatomic, weak) NSDictionary *currentSetting;
@property (nonatomic, weak) UITapGestureRecognizer *tapRecognizer;
@property (nonatomic, strong) NSMutableDictionary *textFieldBindings;
@property (nonatomic, strong) NSMutableDictionary *switchBindings;
@property (nonatomic, strong) NSMutableArray *supportedSettings;

@property (nonatomic, strong) NSMutableDictionary *indexPathsForSettings;
@property (nonatomic, strong) NSMutableArray *indexPathsToUpdate;
@property (nonatomic) BOOL isRedrawFullSettingsTable;

- (void)executePrint;

@end

@implementation PrintSettingsTableViewController

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
    
    // Get printer and print settings data to display
    if (self.printerIndex == nil)
    {
        // Launched from preview - load current print settings and selected printer
        self.printDocument = [[PDFFileManager sharedManager] printDocument];
        self.printer = self.printDocument.printer;
        self.previewSetting = self.printDocument.previewSetting;
        [self.printDocument addObserver:self forKeyPath:@"printer" options:NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld context:&printSettingsPrinterContext];
        self.isDefaultSettingsMode = NO;
    }
    else
    {
        // Launched from printers - load from default print settings and selected printer
        self.printDocument = nil;
        self.printer = [[PrinterManager sharedPrinterManager] getPrinterAtIndex:[self.printerIndex unsignedIntegerValue]];
        PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
        [PrintSettingsHelper copyPrintSettings:self.printer.printsetting toPreviewSetting:&previewSetting];
        self.previewSetting = previewSetting;
        self.isDefaultSettingsMode = YES;
    }

    
    // Get print settings tree
    self.printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    
    // Prepare expansion
    self.expandedSections = [[NSMutableArray alloc] init];
    NSArray *sections = [self.printSettingsTree objectForKey:@"group"];
    for (id section in sections)
    {
        [self.expandedSections addObject:[NSNumber numberWithBool:YES]];
    }
    
    if(self.isDefaultSettingsMode == NO)
    {
        [self.expandedSections addObject:[NSNumber numberWithBool:YES]]; //add pincode section
    }
    
    [self fillSupportedSettings];
    
    PreviewSetting *previewSetting = self.previewSetting;
    [PrintSettingsHelper addObserver:self toPreviewSetting: &previewSetting];
    
    // Add tap recognizer to close keyboard on tap
    UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapAction:)];
    tapRecognizer.cancelsTouchesInView = NO;
    [self.tableView addGestureRecognizer:tapRecognizer];
    self.tapRecognizer = tapRecognizer;
    
    // Add handler for keyboard notifications
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardDidShow:) name:UIKeyboardDidShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardDidHide:) name:UIKeyboardDidHideNotification object:nil];
    
    // Create binding dictionaries
    self.textFieldBindings = [[NSMutableDictionary alloc] init];
    self.switchBindings = [[NSMutableDictionary alloc] init];
    
    // Add empty footer
    UIView *footer = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 1, 20)];
    footer.backgroundColor = [UIColor clearColor];
    self.tableView.tableFooterView = footer;
    
    self.indexPathsForSettings = [NSMutableDictionary dictionary];
    self.indexPathsToUpdate = [[NSMutableArray array] mutableCopy];
}

-(void)dealloc
{
    PreviewSetting *previewSetting = self.previewSetting;
    if (self.printerIndex == nil && self.printDocument != nil)
    {
        //PrintDocument *printDocument = [[PDFFileManager sharedManager] printDocument];
        [self.printDocument removeObserver:self forKeyPath:@"printer"];
    }
    [PrintSettingsHelper removeObserver:self fromPreviewSetting:&previewSetting];
}

- (void)viewDidAppear:(BOOL)animated
{
    if(self.isRedrawFullSettingsTable == YES)
    {
        [self fillSupportedSettings];
        [self.indexPathsToUpdate removeAllObjects];
        [self.indexPathsForSettings removeAllObjects];
        [self.tableView reloadData];
        self.isRedrawFullSettingsTable = NO;
    }
    else
    {
        [self reloadRowsForIndexPathsToUpdate];
    }

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
    NSInteger sections = [[self.printSettingsTree objectForKey:@"group"] count];
    if(self.isDefaultSettingsMode == NO)
    {
        sections += 1; // + pincode section
    }
    return sections + 1;//print settings sections + the printer section
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    NSInteger logicalSection = section;
    NSInteger totalSections = [[self.printSettingsTree objectForKey:@"group"] count] + 1;
    if (logicalSection == 0)
    {
        if(self.isDefaultSettingsMode == YES)
        {
            return 1; //show only printer name not printer button header
        }
        else
        {
            return 2;
        }
    }
    else if(logicalSection == totalSections && self.isDefaultSettingsMode == NO) //pincode section
    {
        if ([[self.expandedSections objectAtIndex:logicalSection - 1] boolValue] == NO)
        {
            return 1;
        }
        return 2;
    }
    else if (logicalSection >= 1)
    {
        if ([[self.expandedSections objectAtIndex:logicalSection - 1] boolValue] == NO)
        {
            return 1;
        }
        
        NSArray *settings =[self.supportedSettings objectAtIndex:logicalSection - 1];
        NSInteger rowCount = [settings count];
        if(rowCount > 0)
        {
            rowCount++; //add the header row
        }
        return rowCount;
    }
    return 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell;
    NSInteger section = indexPath.section;
    NSInteger row = indexPath.row;
    
    if (section == 0)
    {
        if (row == 0 && self.isDefaultSettingsMode == NO)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:PRINTER_HEADER_CELL forIndexPath:indexPath];
        }
        else
        {
            NSString *cellIdentifier = PRINTER_ITEM_CELL;
            if (self.isDefaultSettingsMode == YES)
            {
                cellIdentifier = PRINTER_ITEM_DEFAULT_CELL;
            }
            PrintSettingsPrinterItemCell *printerItemCell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier forIndexPath:indexPath];
            
            if(self.printer != nil)
            {
                printerItemCell.printerNameLabel.hidden = NO;
                printerItemCell.printerIPLabel.hidden = NO;
                printerItemCell.selectPrinterLabel.hidden = YES;
                if(self.printer.name == nil || [self.printer.name isEqualToString:@""] == YES)
                {
                     printerItemCell.printerNameLabel.text = NSLocalizedString(@"IDS_LBL_NO_NAME", @"No name");
                }
                else
                {
                    printerItemCell.printerNameLabel.text = self.printer.name;
                }
                printerItemCell.printerIPLabel.text = self.printer.ip_address;
            }
            else
            {
                printerItemCell.printerNameLabel.hidden = YES;
                printerItemCell.printerIPLabel.hidden = YES;
                printerItemCell.selectPrinterLabel.hidden = NO;
            }
            
            cell = printerItemCell;
        }
    }
    else if(section == [self.supportedSettings count] + 1 && self.isDefaultSettingsMode == NO)
    {
        if (row == 0)
        {
            PrintSettingsHeaderCell *headerCell = [tableView dequeueReusableCellWithIdentifier:SETTING_HEADER_CELL forIndexPath:indexPath];
            headerCell.groupLabel.localizationId = @"IDS_LBL_SECURE_PRINT";
            headerCell.expanded = [[self.expandedSections objectAtIndex:section - 1] boolValue];
            cell = headerCell;
        }
        else
        {
            PrintSettingsItemInputCell *itemInputCell = [tableView dequeueReusableCellWithIdentifier:PINCODE_INPUT_CELL forIndexPath:indexPath];
            itemInputCell.settingLabel.localizationId =  @"IDS_LBL_AUTHENTICATION_PINCODE";
            itemInputCell.valueTextField.secureTextEntry = YES;
            itemInputCell.valueTextField.text = [[self.previewSetting valueForKey:KEY_PIN_CODE] stringValue];
            itemInputCell.valueTextField.tag = indexPath.section * 10 + indexPath.row;
            itemInputCell.valueTextField.delegate = self;
            [self.textFieldBindings setObject:KEY_PIN_CODE forKey:[NSNumber numberWithInteger:itemInputCell.valueTextField.tag]];
            itemInputCell.separator.hidden = YES;
            cell = itemInputCell;
        }
    }
    else
    {
        NSDictionary *group = [[self.printSettingsTree objectForKey:@"group"] objectAtIndex:section - 1];
        if (row == 0)
        {
            PrintSettingsHeaderCell *headerCell = [tableView dequeueReusableCellWithIdentifier:SETTING_HEADER_CELL forIndexPath:indexPath];
            headerCell.groupLabel.localizationId = [group objectForKey:@"text"];
            headerCell.expanded = [[self.expandedSections objectAtIndex:section - 1] boolValue];
            cell = headerCell;
        }
        else
        {
            NSArray *settings = [self.supportedSettings objectAtIndex:section - 1]; 

            NSDictionary *setting = [settings objectAtIndex:row - 1];
            
            NSString *type = [setting objectForKey:@"type"];
            NSString *key = [setting objectForKey:@"name"];
            
	    //keep track of index of each setting for easy access
            [self.indexPathsForSettings setObject:indexPath forKey:key];
            if ([type isEqualToString:@"list"])
            {
                PrintSettingsItemOptionCell *itemOptionCell = [tableView dequeueReusableCellWithIdentifier:SETTING_ITEM_OPTION_CELL forIndexPath:indexPath];
                itemOptionCell.settingLabel.localizationId = [setting objectForKey:@"text"];
                
                // Get value
                NSArray *options = [setting objectForKey:@"option"];
                NSNumber *index = [self.previewSetting valueForKey:key];
                NSString *selectedOption = [[options objectAtIndex:[index integerValue]] objectForKey:@"content-body"];
                itemOptionCell.valueLabel.localizationId = selectedOption;
                
                itemOptionCell.separator.hidden = NO;
                if (row == settings.count)
                {
                    itemOptionCell.separator.hidden = YES;
                }
                
                [itemOptionCell setHideValue:![self isSettingApplicable:key]];
                
                cell = itemOptionCell;
                [cell setUserInteractionEnabled:[self isSettingEnabled:key]];
                
            }
            else if ([type isEqualToString:@"numeric"])
            {
                PrintSettingsItemInputCell *itemInputCell = [tableView dequeueReusableCellWithIdentifier:SETTING_ITEM_INPUT_CELL forIndexPath:indexPath];
                itemInputCell.settingLabel.localizationId = [setting objectForKey:@"text"];
                itemInputCell.valueTextField.text = [[self.previewSetting valueForKey:key] stringValue];
                itemInputCell.valueTextField.tag = indexPath.section * 10 + indexPath.row;
                itemInputCell.valueTextField.delegate = self;
                [self.textFieldBindings setObject:key forKey:[NSNumber numberWithInteger:itemInputCell.valueTextField.tag]];
                
                itemInputCell.separator.hidden = NO;
                if (row == settings.count)
                {
                    itemInputCell.separator.hidden = YES;
                }
                cell = itemInputCell;
            }
            else
            {
                PrintSettingsItemSwitchCell *itemSwitchCell = [tableView dequeueReusableCellWithIdentifier:SETTING_ITEM_SWITCH_CELL forIndexPath:indexPath];
                itemSwitchCell.settingLabel.localizationId = [setting objectForKey:@"text"];
                itemSwitchCell.valueSwitch.on = NO;
                if ([[self.previewSetting valueForKey:key] boolValue] == YES)
                {
                    itemSwitchCell.valueSwitch.on = YES;
                }
                itemSwitchCell.valueSwitch.tag = indexPath.section * 10 + indexPath.row;
                [self.switchBindings setObject:key forKey:[NSNumber numberWithInteger:itemSwitchCell.valueSwitch.tag]];
                [itemSwitchCell.valueSwitch addTarget:self action:@selector(switchAction:) forControlEvents:UIControlEventValueChanged];
                
                itemSwitchCell.separator.hidden = NO;
                if (row == settings.count)
                {
                    itemSwitchCell.separator.hidden = YES;
                }
                cell = itemSwitchCell;
            }
        }
    }
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSInteger section = indexPath.section;
    NSInteger row = indexPath.row;

    if(section == [self.supportedSettings count] + 1 && self.isDefaultSettingsMode == NO)
    {
         PrintSettingsHeaderCell *headerCell = (PrintSettingsHeaderCell *)[tableView cellForRowAtIndexPath:indexPath];
         headerCell.expanded = !headerCell.expanded;
         [self.expandedSections replaceObjectAtIndex:section - 1 withObject:[NSNumber numberWithBool:headerCell.expanded]];
        NSIndexPath *pinCodeRowIndexPath = [NSIndexPath indexPathForRow:indexPath.row + 1 inSection:indexPath.section];
        if(headerCell.expanded)
        {
            [tableView insertRowsAtIndexPaths:@[pinCodeRowIndexPath]  withRowAnimation:UITableViewRowAnimationTop];
        }
        else
        {
            [tableView deleteRowsAtIndexPaths:@[pinCodeRowIndexPath]  withRowAnimation:UITableViewRowAnimationTop];
        }
    }
    else if (section > 0)
    {
        NSArray *settings = [self.supportedSettings objectAtIndex:section - 1];
        if (row == 0)
        {
            BOOL isExpanded = [[self.expandedSections objectAtIndex:section - 1] boolValue];
            [self.expandedSections replaceObjectAtIndex:section - 1 withObject:[NSNumber numberWithBool:!isExpanded]];
            
            NSUInteger settingsCount = [settings count];
            NSMutableArray *indexPaths = [[NSMutableArray alloc] init];
            for (int i = 0; i < settingsCount; i++)
            {
                [indexPaths addObject:[NSIndexPath indexPathForRow:i + 1 inSection:indexPath.section]];
            }
            
            PrintSettingsHeaderCell *headerCell = (PrintSettingsHeaderCell *)[tableView cellForRowAtIndexPath:indexPath];
            if (isExpanded)
            {
                [tableView deleteRowsAtIndexPaths:indexPaths withRowAnimation:UITableViewRowAnimationTop];
                headerCell.expanded = NO;
            }
            else
            {
                [tableView insertRowsAtIndexPaths:indexPaths withRowAnimation:UITableViewRowAnimationTop];
                headerCell.expanded = YES;
            }
        }
        else
        {
            UITableViewCell *cell = [tableView cellForRowAtIndexPath:indexPath];
            if ([cell isKindOfClass:[PrintSettingsItemOptionCell class]])
            {
                [self addToIndexToUpdate:indexPath];
                self.currentSetting = [settings objectAtIndex:row - 1];
                [self performSegueWithIdentifier:@"PrintSettings-PrintOptions" sender:self];
            }
        }
    }
    else
    {
        if (self.isDefaultSettingsMode == NO)
        {
            if (row == 0)
            {
                [self executePrint];
            }
            else
            {
                [self performSegueWithIdentifier:@"PrintSettings-PrinterList" sender:self];
            }
        }
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 0)
    {
        if (self.isDefaultSettingsMode == YES || indexPath.row == 1)
        {
            return ROW_HEIGHT_DOUBLE;
        }
    }
    return ROW_HEIGHT_SINGLE;
}

- (IBAction)tapAction:(id)sender
{
    [self.tableView endEditing:YES];
}

- (IBAction)switchAction:(id)sender
{
    UISwitch *switchView = sender;
    NSString *key = [self.switchBindings objectForKey:[NSNumber numberWithInteger:switchView.tag]];
    [self.previewSetting setValue:[NSNumber numberWithBool:switchView.on] forKey:key];
}

- (void)textFieldDidEndEditing:(UITextField *)textField
{
    NSString *key = [self.textFieldBindings objectForKey:[NSNumber numberWithInteger:textField.tag]];
    NSInteger value = [textField.text integerValue];
    if([key isEqualToString:KEY_COPIES] == YES)
    {
        if(value == 0)
        {
            //if value in text field will be zero auto correct to minimum
            value = 1;
            textField.text = @"1";
        }
        else
        {
            //if value in text field has leading zeros, remove leading zeros
            if([textField.text hasPrefix:@"0"] == YES)
            {
                textField.text = [NSString stringWithFormat:@"%ld", (long)value];
            }
        }
    }
    [self.previewSetting setValue:[NSNumber numberWithInteger:value] forKey:key];
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    if([string isEqualToString:@""] == YES) //always accept back space
    {
        return YES;
    }
    
    NSString *key = [self.textFieldBindings objectForKey:[NSNumber numberWithInteger:textField.tag]];
    if([key isEqualToString:KEY_COPIES] == YES)
    {
        if((textField.text.length + string.length) > 4)
        {
            return NO;
        }
        
        NSCharacterSet *validCharacters = [NSCharacterSet characterSetWithCharactersInString:@"0123456789"];
        if([string stringByTrimmingCharactersInSet:validCharacters].length > 0)
        {
            return NO;
        }
    }
    
    if([key isEqualToString:KEY_PIN_CODE] == YES)
    {
        if((textField.text.length + string.length) > 8)
        {
            return NO;
        }
        
        NSCharacterSet *validCharacters = [NSCharacterSet characterSetWithCharactersInString:@"0123456789"];
        if([string stringByTrimmingCharactersInSet:validCharacters].length > 0)
        {
            return NO;
        }
    }
    return YES;
}

- (void)keyboardDidShow:(NSNotification *)notification
{
    self.tapRecognizer.cancelsTouchesInView = YES;
}

- (void)keyboardDidHide:(NSNotification *)notification
{
    self.tapRecognizer.cancelsTouchesInView = NO;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"PrintSettings-PrintOptions"])
    {
        PrintSettingsOptionTableViewController *optionsController = segue.destinationViewController;
        optionsController.setting = self.currentSetting;
        optionsController.previewSetting = self.previewSetting;
    }
    /*if ([segue.identifier isEqualToString:@"PrintSettings-PrinterList"])
    {
        PrintSettingsPrinterListTableViewController *printerListController = segue.destinationViewController;
        printerListController.selectedPrinter = self.printer;
    }*/
}

- (IBAction)unwindToPrintSettings:(UIStoryboardSegue *)sender
{
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if (context == PREVIEWSETTING_CONTEXT)
    {
        if(self.isDefaultSettingsMode == YES)
        {
            NSNumber *value = [self.previewSetting valueForKey:keyPath];
            if(value != nil)
            {
                [self.printer.printsetting setValue:[self.previewSetting valueForKey:keyPath] forKey:keyPath];
                [[PrinterManager sharedPrinterManager] savePrinterChanges];
            }
        }
        
        NSInteger previousVal = (NSInteger)[[change objectForKey:NSKeyValueChangeOldKey] integerValue];
        [self applySettingsConstraintForKey:keyPath withPreviousValue:previousVal];
    }
    else if (context == &printSettingsPrinterContext)
    {
        int changeKind = [[change objectForKey:NSKeyValueChangeKindKey] intValue];
        if (changeKind == NSKeyValueChangeSetting)
        {
            Printer* previous = [change objectForKey:NSKeyValueChangeOldKey];
            Printer* current = [change objectForKey:NSKeyValueChangeNewKey];
            if (![previous isEqual:current])
            {
                PrintDocument *printDocument = [[PDFFileManager sharedManager] printDocument];
                self.printer = printDocument.printer;

                //set to redraw the whole settings table to remove settings that are not supported by new printer
                self.isRedrawFullSettingsTable = YES;
            }
        }
    }
}

- (void)applySettingsConstraintForKey:(NSString*)key withPreviousValue:(NSInteger)previousValue
{
    if([key isEqualToString:KEY_BOOKLET] == YES)
    {
        [self applyBookletConstraints];
    }
    if([key isEqualToString:KEY_FINISHING_SIDE] == YES)
    {
        [self applyFinishingConstraintsWithPreviousValue:previousValue];
        [self applyFinishingWithOrientationConstraint];
    }
    if([key isEqualToString:KEY_ORIENTATION] == YES)
    {
        [self applyFinishingWithOrientationConstraint];
        //[self applyOrientationConstraint];
    }
    if([key isEqualToString:KEY_PUNCH] == YES)
    {
        [self applyPunchConstraint];
    }
    
    if([key isEqualToString:KEY_IMPOSITION] == YES)
    {
        [self applyImpositionConstraintWithPreviousValue:previousValue];
        [self applyFinishingWithOrientationConstraint];
    }
}

- (void)applyBookletConstraints
{
    if(self.previewSetting.booklet == YES)
    {
        [self setOptionSettingWithKey:KEY_DUPLEX toValue:(NSInteger)kDuplexSettingShortEdge];
        [self setOptionSettingWithKey:KEY_IMPOSITION toValue:(NSInteger)kImpositionOff];
#if OUTPUT_TRAY_CONSTRAINT_ENABLED
        [self setOptionSettingWithKey:KEY_OUTPUT_TRAY toValue:(NSInteger)kOutputTrayAuto];
#endif //OUTPUT_TRAY_CONSTRAINT_ENABLED
    }
    else
    {
        [self setState:YES forSettingKey:KEY_IMPOSITION];
        [self setOptionSettingWithKey:KEY_DUPLEX toValue:(NSInteger)kDuplexSettingOff];
    }
    
    [self setOptionSettingToDefaultValue:KEY_FINISHING_SIDE];
    [self setOptionSettingToDefaultValue:KEY_STAPLE];
    [self setOptionSettingToDefaultValue:KEY_PUNCH];
    [self setOptionSettingToDefaultValue:KEY_BOOKLET_FINISH];
    [self setOptionSettingToDefaultValue:KEY_BOOKLET_LAYOUT];
    [self reloadRowsForIndexPathsToUpdate];
}

- (void)applyFinishingConstraintsWithPreviousValue:(NSInteger)previousFinishingSide
{
    kFinishingSide currentFinishingSide = (kFinishingSide)self.previewSetting.finishingSide;
    kStapleType staple = (kStapleType)self.previewSetting.staple;
    switch(currentFinishingSide)
    {
        case kFinishingSideLeft:
        case kFinishingSideRight:
            if(staple == kStapleTypeUpperLeft || staple == kStapleTypeUpperRight)
            {
                [self setOptionSettingWithKey:KEY_STAPLE toValue:(NSInteger)kStapleType1Pos];
            }
            break;
        case kFinishingSideTop:
            if(staple == kStapleType1Pos)
            {
                if(previousFinishingSide == kFinishingSideLeft)
                {
                    [self setOptionSettingWithKey:KEY_STAPLE toValue:(NSInteger)kStapleTypeUpperLeft];
                }
                else if(previousFinishingSide == kFinishingSideRight)
                {
                    [self setOptionSettingWithKey:KEY_STAPLE toValue:(NSInteger)kStapleTypeUpperRight];
                }
            }
            break;
        default:
            break;
    }
}

-(void) applyFinishingWithOrientationConstraint
{
    kPunchType punch = (kPunchType)self.previewSetting.punch;
    BOOL isPaperLandscape = [PrintPreviewHelper isPaperLandscapeForPreviewSetting:self.previewSetting];
    kFinishingSide finishingSide = (kFinishingSide)self.previewSetting.finishingSide;
    
    if(punch == kPunchType3or4Holes)
    {
        if((finishingSide != kFinishingSideTop  && isPaperLandscape == YES) ||
           (finishingSide == kFinishingSideTop && isPaperLandscape == NO))
        {
            [self setOptionSettingToDefaultValue:KEY_PUNCH];
        }
    }
}

- (void)applyPunchConstraint
{
    kPunchType punch = (kPunchType)self.previewSetting.punch;
    kFinishingSide finishingSide = (kFinishingSide)self.previewSetting.finishingSide;
    BOOL isPaperLandscape = [PrintPreviewHelper isPaperLandscapeForPreviewSetting:self.previewSetting];
    
    if(punch == kPunchType3or4Holes)
    {
        if(finishingSide != kFinishingSideTop  && isPaperLandscape == YES)
        {
            [self setOptionSettingWithKey:KEY_FINISHING_SIDE toValue:(NSInteger)kFinishingSideTop];
        }
        if(finishingSide == kFinishingSideTop  && isPaperLandscape == NO)
        {
            [self setOptionSettingWithKey:KEY_FINISHING_SIDE toValue:(NSInteger)kFinishingSideLeft];
        }
    }
#if OUTPUT_TRAY_CONSTRAINT_ENABLED
    if(punch != kPunchTypeNone && self.previewSetting.outputTray == kOutputTrayFaceDownTray &&
       [self.printer.enabled_tray_face_down boolValue] == YES)
    {
        [self setOptionSettingWithKey:KEY_OUTPUT_TRAY toValue:(NSInteger) kOutputTrayAuto];
    }
#endif //OUTPUT_TRAY_CONSTRAINT_ENABLED
}

-(void) applyImpositionConstraintWithPreviousValue:(NSInteger)previousImpositionValue
{
    kImposition currentImpositionValue  = (kImposition) self.previewSetting.imposition;
    kImpositionOrder impositionOrder = (kImpositionOrder) self.previewSetting.impositionOrder;
    switch(currentImpositionValue)
    {
        case kImpositionOff:
            [self setOptionSettingWithKey:KEY_IMPOSITION_ORDER toValue:kImpositionOrderLeftToRight];
            break;
        case kImposition2Pages:
            if(previousImpositionValue == kImposition4pages)
            {
                if(impositionOrder == kImpositionOrderUpperLeftToRight || impositionOrder == kImpositionOrderUpperLeftToBottom)
                {
                    [self setOptionSettingWithKey:KEY_IMPOSITION_ORDER toValue:(NSInteger) kImpositionOrderLeftToRight];
                }
                else
                {
                    [self setOptionSettingWithKey:KEY_IMPOSITION_ORDER toValue:(NSInteger) kImpositionOrderRightToLeft];
                }
            }
            if(previousImpositionValue == kImpositionOff)
            {
                [self setState:YES forSettingKey:KEY_IMPOSITION_ORDER];
            }
            break;
        case kImposition4pages:
            if(previousImpositionValue == kImposition2Pages)
            {
                if(impositionOrder == kImpositionOrderLeftToRight)
                {
                    [self setOptionSettingWithKey:KEY_IMPOSITION_ORDER toValue:(NSInteger)kImpositionOrderUpperLeftToRight];
                }
                else
                {
                    [self setOptionSettingWithKey:KEY_IMPOSITION_ORDER toValue:(NSInteger)kImpositionOrderUpperRightToLeft];
                }
            }
            if(previousImpositionValue == kImpositionOff)
            {
                [self setOptionSettingWithKey:KEY_IMPOSITION_ORDER toValue:kImpositionOrderUpperLeftToRight];
            }
            break;
        default:
            break;
    }
}

- (void)setState:(BOOL)isEnabled forSettingKey:(NSString*)key
{
    NSIndexPath *indexPath = [self.indexPathsForSettings objectForKey:key];
    if(indexPath != nil)
    {
        UITableViewCell *cell = [self.tableView cellForRowAtIndexPath:indexPath];
        [cell setUserInteractionEnabled:isEnabled];
        if([cell isKindOfClass:[PrintSettingsItemOptionCell class]] == YES)
        {
            [(PrintSettingsItemOptionCell *)cell setHideValue:![self isSettingApplicable:key]];
        }
    }
}

-(void) setOptionSettingToDefaultValue: (NSString*)key
{
    PreviewSetting *defaultPreviewSetting = [PrintSettingsHelper defaultPreviewSetting];
    NSNumber *defaultValue = [defaultPreviewSetting valueForKey:key];
    [self.previewSetting setValue:defaultValue forKey:key];
    
    NSIndexPath *indexPath = [self.indexPathsForSettings objectForKey:key];
    if(indexPath != nil)
    {
        [self addToIndexToUpdate:indexPath];
    }
}

-(void) setOptionSettingWithKey:(NSString*)key toValue:(NSInteger)value
{
    NSNumber *num = [NSNumber numberWithInteger:value];
    [self.previewSetting setValue:num forKey:key];
    
    NSIndexPath *indexPath = [self.indexPathsForSettings objectForKey:key];
    if(indexPath != nil)
    {
        [self addToIndexToUpdate:indexPath];
    }
}

- (BOOL)isSettingEnabled:(NSString*)settingKey
{
    if([settingKey isEqualToString:KEY_DUPLEX])
    {
        if(self.previewSetting.booklet == YES)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_FINISHING_SIDE])
    {
        if(self.previewSetting.booklet == YES)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_STAPLE])
    {
        if(self.previewSetting.booklet == YES)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_PUNCH])
    {
        if(self.previewSetting.booklet == YES)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_IMPOSITION])
    {
        if(self.previewSetting.booklet == YES)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_IMPOSITION_ORDER])
    {
        if(self.previewSetting.booklet == YES ||
           self.previewSetting.imposition == kImpositionOff)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_BOOKLET_LAYOUT] ||
       [settingKey isEqualToString:KEY_BOOKLET_FINISH])
    {
        return self.previewSetting.booklet;
    }
    
    return YES;
}

- (BOOL)isSettingApplicable:(NSString*)settingKey
{
    if([settingKey isEqualToString:KEY_FINISHING_SIDE])
    {
        if(self.previewSetting.booklet == YES)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_STAPLE])
    {
        if(self.previewSetting.booklet == YES)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_PUNCH])
    {
        if(self.previewSetting.booklet == YES)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_IMPOSITION])
    {
        if(self.previewSetting.booklet == YES)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_IMPOSITION_ORDER])
    {
        if(self.previewSetting.imposition == kImpositionOff)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_BOOKLET_LAYOUT] ||
       [settingKey isEqualToString:KEY_BOOKLET_FINISH])
    {
        return self.previewSetting.booklet;
    }
    
    return YES;
}

- (BOOL)isSettingSupported:(NSString*)settingKey
{
    if(self.printer == nil)
    {
        return YES;
    }

    if([settingKey isEqualToString:KEY_BOOKLET] ||
       [settingKey isEqualToString:KEY_BOOKLET_LAYOUT] ||
       [settingKey isEqualToString:KEY_BOOKLET_FINISH])
    {
        return [self.printer.enabled_booklet boolValue];
    }
    
    if([settingKey isEqualToString:KEY_PUNCH])
    {
        return ([self.printer.enabled_finisher_2_3_holes boolValue] || [self.printer.enabled_finisher_2_4_holes boolValue]);
    }
    
    if([settingKey isEqualToString:KEY_STAPLE])
    {
        return [self.printer.enabled_staple boolValue];
    }
    
    return YES;
}

- (void)addToIndexToUpdate:(NSIndexPath *)indexPath
{
    if([self.indexPathsToUpdate containsObject:indexPath] == NO && [[self.expandedSections objectAtIndex:indexPath.section - 1] boolValue] == YES)
    {
        [self.indexPathsToUpdate addObject:indexPath];
    }
}

- (void)reloadRowsForIndexPathsToUpdate
{
    if([self.indexPathsToUpdate count] > 0)
    {
        [self.tableView reloadRowsAtIndexPaths:self.indexPathsToUpdate withRowAnimation:UITableViewRowAnimationFade];
        [self.indexPathsToUpdate removeAllObjects];
    }
}

-(void) fillSupportedSettings
{
    self.supportedSettings = [[NSMutableArray alloc] init];
    NSArray *sections = [self.printSettingsTree objectForKey:@"group"];
    
    for(NSDictionary *section in sections)
    {
        NSMutableArray *settings =[[section objectForKey:@"setting"] mutableCopy];
        NSMutableArray *effectiveSettings = [[NSMutableArray alloc] init];

        for(NSDictionary *setting in settings)
        {
            NSString *key = [setting objectForKey:@"name"];
            if([self isSettingSupported:key] == YES)
            {
                if([key isEqualToString:KEY_PUNCH] == YES || [key isEqualToString:KEY_OUTPUT_TRAY] == YES)
                {
                    NSMutableDictionary *tempSetting = [NSMutableDictionary dictionaryWithDictionary:setting];
                    NSArray *options = [setting objectForKey:@"option"];
                    NSMutableArray *tempOptions = [[NSMutableArray alloc] init];
                    for(NSDictionary *option in options)
                    {
                        if([self isSettingOptionSupported:[option objectForKey:@"content-body"]] == YES)
                        {
                            [tempOptions addObject:option];
                        }
                    }
                    [tempSetting setValue:tempOptions forKey:@"option"];
                    [effectiveSettings addObject:tempSetting];
                }
                else
                {
                    [effectiveSettings addObject:setting];
                }
            }
        }
        [self.supportedSettings addObject:effectiveSettings];
    }
}

-(BOOL) isSettingOptionSupported:(NSString *) option
{
    if([option isEqualToString:@"ids_lbl_punch_2holes"])
    {
        return ([self.printer.enabled_finisher_2_3_holes boolValue] || [self.printer.enabled_finisher_2_4_holes boolValue]);
    }
    
    if([option isEqualToString:@"ids_lbl_punch_3holes"])
    {
        return [self.printer.enabled_finisher_2_3_holes boolValue];
    }
    
    if([option isEqualToString:@"ids_lbl_punch_4holes"])
    {
        return [self.printer.enabled_finisher_2_4_holes boolValue];
    }
    
    if ([option isEqualToString:@"ids_lbl_outputtray_facedown"])
    {
        return  [self.printer.enabled_tray_face_down boolValue];
    }
    
    if ([option isEqualToString:@"ids_lbl_outputtray_top"])
    {
        return [self.printer.enabled_tray_top boolValue];
    }
    
    if ([option isEqualToString:@"ids_lbl_outputtray_stacking"])
    {
        return [self.printer.enabled_tray_stacking boolValue];
    }
    
    return YES;
}

- (void)executePrint
{
    // Check if printer is selected
    if (self.printer == nil)
    {
        [AlertHelper displayResult:kAlertResultErrDefault withTitle:kAlertTitleDefault withDetails:nil];
        return;
    }
    
    if (![NetworkManager isConnectedToLocalWifi])
    {
        [AlertHelper displayResult:kAlertResultErrNoNetwork withTitle:kAlertTitleDefault withDetails:nil];
        return;
    }
    
    DirectPrintManager *manager = [[DirectPrintManager alloc] init];
    if ([self.printer.port integerValue] == 0)
    {
        [manager printDocumentViaLPR];
    }
    else
    {
        [manager printDocumentViaRaw];
    }
    manager.delegate = self;
}

- (void)documentDidFinishPrinting:(BOOL)successful
{
    if (successful)
    {
        [self performSegueTo:[PrintJobHistoryViewController class]];
    }
}

@end

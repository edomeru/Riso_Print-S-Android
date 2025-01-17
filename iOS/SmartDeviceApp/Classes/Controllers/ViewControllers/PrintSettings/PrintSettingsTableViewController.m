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

#define SETTING_HEADER_CELL @"SettingHeaderCell"
#define SETTING_ITEM_OPTION_CELL @"SettingItemOptionCell"
#define SETTING_ITEM_INPUT_CELL @"SettingItemInputCell"
#define SETTING_ITEM_SWITCH_CELL @"SettingItemSwitchCell"
#define PINCODE_INPUT_CELL @"PincodeInputCell"

#define ROW_HEIGHT_SINGLE 44

static NSString *printSettingsPrinterContext = @"PrintSettingsPrinterContext";

@interface PrintSettingsTableViewController ()

/**
 * Flag that indicates whether this is for the "Default Print Settings" screen
 * or the "Print Settings" screen.
 */
@property (nonatomic) BOOL isDefaultSettingsMode;

/**
 * Reference to the set of currently applied preview settings.
 */
@property (nonatomic, strong) PreviewSetting *previewSetting;

/**
 * Reference to the PDF document object from PDFFileManager.
 */
@property (nonatomic, strong) PrintDocument *printDocument;

/**
 * Reference to the selected printer.
 */
@property (nonatomic, weak) Printer *printer;

/**
 * Contains the contents of the printsettings.xml.
 */
@property (nonatomic, weak) NSDictionary *printSettingsTree;

/**
 * Stores the flags indicating which print setting sections are expanded.
 * The flag is YES if it is expanded, NO if collapsed.
 */
@property (nonatomic, strong) NSMutableArray *expandedSections;

/**
 * Reference to the currently selected print setting.
 * This is used when transitioning to the PrintSettingsOptionTableViewController.
 */
@property (nonatomic, weak) NSDictionary *currentSetting;

/**
 * Tap gesture recognizer.
 */
@property (nonatomic, weak) UITapGestureRecognizer *tapRecognizer;

/**
 * Indicates which text field corresponds to a particular tag.
 */
@property (nonatomic, strong) NSMutableDictionary *textFieldBindings;

/**
 * Indicates which switch corresponds to a particular tag.
 */
@property (nonatomic, strong) NSMutableDictionary *switchBindings;

/**
 * Indicates the currently supported print settings.
 */
@property (nonatomic, strong) NSMutableArray *supportedSettings;

/**
 * Indicates which print setting corresponds to a particular UITableView index path.
 */
@property (nonatomic, strong) NSMutableDictionary *indexPathsForSettings;

/**
 * Indicates the which print settings need to be updated.
 */
@property (nonatomic, strong) NSMutableArray *indexPathsToUpdate;

/**
 * Flag that indicates whether the entire print settings list needs to be updated.
 */
@property (nonatomic) BOOL isRedrawFullSettingsTable;

/**
 * Adds the specified index path to {@link indexPathsToUpdate}.
 *
 * @param indexPath the index path to update
 */
- (void)addToIndexToUpdate:(NSIndexPath *)indexPath;

/**
 * Updates the other print settings based on the Booklet setting.
 */
- (void)applyBookletConstraints;

/**
 * Updates affected print settings based on the Booklet Finishing setting.
 */
- (void)applyBookletFinishConstraints;

/**
 * Updates the Finishing print setting based on the Finishing Side setting.
 *
 * @param previousFinishingSide the previous Finishing Side value
 */
- (void)applyFinishingConstraintsWithPreviousValue:(NSInteger)previousFinishingSide;

/**
 * Updates affected print settings based on the Imposition setting.
 *
 * @param previousImpositionValue the previous Imposition value
 */
- (void)applyImpositionConstraintWithPreviousValue:(NSInteger)previousImpositionValue;

/**
 * Updates affected print settings based on the Punch setting.
 */
- (void)applyPunchConstraint;

/**
 * Determines which print settings are to be updated because of the specified setting.
 *
 * @param key the updated print setting
 * @param previousValue the previous value of the updated print setting
 */
- (void)applySettingsConstraintForKey:(NSString*)key withPreviousValue:(NSInteger)previousValue;

/**
 * Sets-up the contents of {@link supportedSettings}.
 */
- (void)fillSupportedSettings;

/**
 * Checks if the specified print setting is applicable.
 *
 * @param settingKey the string name of the print setting
 * @return YES if applicable, NO otherwise
 */
- (BOOL)isSettingApplicable:(NSString*)settingKey;

/**
 * Checks if the specified print setting is enabled.
 *
 * @param settingKey the string name of the print setting
 * @return YES if enabled, NO otherwise
 */
- (BOOL)isSettingEnabled:(NSString*)settingKey;

/**
 * Checks if the specified print setting value is supported.
 *
 * @param settingKey the string name of the print setting
 * @return YES if supported, NO otherwise
 */
- (BOOL)isSettingOptionSupported:(NSString *)option;

// for ORPHIS FW start
/**
 * Checks if the specified print setting value is supported for IS/FW/GD Series.
 *
 * @param settingKey the string name of the print setting
 * @return YES if supported, NO otherwise
 */
-(BOOL)isSettingSupportedISFWGD:(NSString *)option;

/**
 * Checks if the printer name is IS Series.
 *
 * @return YES if IS Series, NO otherwise
 */
- (BOOL)isISSeries;

/**
 * Checks if the printer name is FW Series.
 *
 * @return YES if FW Series, NO otherwise
 */
- (BOOL)isFWSeries;
// for ORPHIS FW end

/**
 * Checks if the specified print setting is supported.
 *
 * @param settingKey the string name of the print setting
 * @return YES if supported, NO otherwise
 */
- (BOOL)isSettingSupported:(NSString*)settingKey;

/**
 * Reloads the print settings as specified in the {@link indexPathsToUpdate}.
 */
- (void)reloadRowsForIndexPathsToUpdate;

/**
 * Reverts the print setting to its default value.
 *
 * @param key the string name of the print setting
 */
- (void)setOptionSettingToDefaultValue:(NSString*)key;

/**
 * Sets the print setting to the specified value.
 *
 * @param key the string name of the print setting
 * @param value the print setting value
 */
- (void)setOptionSettingWithKey:(NSString*)key toValue:(NSInteger)value;

/**
 * Sets the print setting to be enabled/disabled.
 *
 * @param isEnabled YES if setting is enabled; NO if disabled
 * @param key the string name of the print setting
 */
- (void)setState:(BOOL)isEnabled forSettingKey:(NSString*)key;

/**
 * Responds to the print setting switches.
 */
- (IBAction)switchAction:(id)sender;

/**
 * Responds to tapping anywhere on the print settings list.
 */
- (IBAction)tapAction:(id)sender;

/**
 * Called when the keypad is shown.
 *
 * @param notification the notification object
 */
- (void)keyboardDidShow:(NSNotification *)notification;

/**
 * Called when the keypad is dismissed.
 *
 * @param notification the notification object
 */
- (void)keyboardDidHide:(NSNotification *)notification;

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
        //check if printer of the printdocument has already been deleted from DB
        if(self.printDocument.printer.managedObjectContext == nil)
        {
            self.printDocument.printer = [[PrinterManager sharedPrinterManager] getDefaultPrinter];
        }
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

    //// Get print settings tree
    self.printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    
    // Prepare expansion
    self.expandedSections = [[NSMutableArray alloc] init];
    NSArray *sections = [self.printSettingsTree objectForKey:@"group"];
    for (int i = 0; i < sections.count; i++)
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
        [self.printDocument removeObserver:self forKeyPath:@"printer"];
    }
    [PrintSettingsHelper removeObserver:self fromPreviewSetting:&previewSetting];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
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

#pragma mark - UITableView

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    NSInteger sections = [[self.printSettingsTree objectForKey:@"group"] count];
    if(self.isDefaultSettingsMode == NO)
    {
        sections += 1; // + pincode section
    }
    return sections;//print settings sections
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    NSInteger logicalSection = section;
    NSInteger totalSections = [[self.printSettingsTree objectForKey:@"group"] count];
    
    if ([[self.expandedSections objectAtIndex:logicalSection] boolValue] == NO)
    {
        return 1;
    }
    
    if(logicalSection == totalSections && self.isDefaultSettingsMode == NO) //pincode section
    {
        return 3; //header + secure print switch + pin code
    }
    else
    {
        NSArray *settings =[self.supportedSettings objectAtIndex:logicalSection];
        NSInteger rowCount = [settings count];
        if(rowCount > 0)
        {
            rowCount++; //add the header row
        }
        return rowCount;
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell;
    NSInteger section = indexPath.section;
    NSInteger row = indexPath.row;
    
    if(section == [self.supportedSettings count] && self.isDefaultSettingsMode == NO)//pincode section
    {
        if (row == 0)
        {
            PrintSettingsHeaderCell *headerCell = [tableView dequeueReusableCellWithIdentifier:SETTING_HEADER_CELL forIndexPath:indexPath];
            headerCell.groupLabel.localizationId = IDS_LBL_AUTHENTICATION;
            headerCell.expanded = [[self.expandedSections objectAtIndex:section] boolValue];
            
            if(headerCell.expanded || section == [self.supportedSettings count])
            {
                headerCell.separator.hidden = YES;
            }
            else
            {
                headerCell.separator.hidden = NO;
            }
            
            cell = headerCell;
        }
        else if (row == 1)
        {
            // Secure Print switch
            
            PrintSettingsItemSwitchCell *itemSwitchCell =
                [tableView dequeueReusableCellWithIdentifier:SETTING_ITEM_SWITCH_CELL forIndexPath:indexPath];
            itemSwitchCell.settingLabel.localizationId = IDS_LBL_SECURE_PRINT;
            itemSwitchCell.valueSwitch.on = self.previewSetting.securePrint;
            itemSwitchCell.separator.hidden = NO;
            itemSwitchCell.valueSwitch.tag = indexPath.section * 10 + indexPath.row;
            [self.switchBindings setObject:KEY_SECURE_PRINT
                                    forKey:[NSNumber numberWithInteger:itemSwitchCell.valueSwitch.tag]];
            [itemSwitchCell.valueSwitch addTarget:self
                                           action:@selector(switchAction:)
                                 forControlEvents:UIControlEventValueChanged];
            cell = itemSwitchCell;
        }
        else
        {
            // Pin Code textfield
            
            PrintSettingsItemInputCell *itemInputCell = [tableView dequeueReusableCellWithIdentifier:PINCODE_INPUT_CELL forIndexPath:indexPath];
            itemInputCell.settingLabel.localizationId = IDS_LBL_PIN_CODE;
            itemInputCell.valueTextField.secureTextEntry = YES;
            itemInputCell.valueTextField.text = [self.previewSetting valueForKey:KEY_PIN_CODE];
            itemInputCell.valueTextField.tag = indexPath.section * 10 + indexPath.row;
            itemInputCell.valueTextField.delegate = self;
            [self.textFieldBindings setObject:KEY_PIN_CODE forKey:[NSNumber numberWithInteger:itemInputCell.valueTextField.tag]];
            itemInputCell.separator.hidden = YES;
            [itemInputCell setEnabled:self.previewSetting.securePrint];
            [self.indexPathsForSettings setObject:indexPath forKey:KEY_PIN_CODE];
            cell = itemInputCell;
        }
    }
    else
    {
        NSDictionary *group = [[self.printSettingsTree objectForKey:@"group"] objectAtIndex:section];
        if (row == 0)
        {
            PrintSettingsHeaderCell *headerCell = [tableView dequeueReusableCellWithIdentifier:SETTING_HEADER_CELL forIndexPath:indexPath];
            headerCell.groupLabel.localizationId = [group objectForKey:@"text"];
            headerCell.expanded = [[self.expandedSections objectAtIndex:section] boolValue];
            
            if(headerCell.expanded || section == [self.supportedSettings count])
            {
                headerCell.separator.hidden = YES;
            }
            else
            {
                headerCell.separator.hidden = NO;
            }
            
            cell = headerCell;
        }
        else
        {
            NSArray *settings = [self.supportedSettings objectAtIndex:section];

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

    if(section == [self.supportedSettings count] && self.isDefaultSettingsMode == NO)
    {
        if (row == 0)
        {
            PrintSettingsHeaderCell *headerCell = (PrintSettingsHeaderCell *)[tableView cellForRowAtIndexPath:indexPath];
            headerCell.expanded = ![[self.expandedSections objectAtIndex:section] boolValue];
            [self.expandedSections replaceObjectAtIndex:section withObject:[NSNumber numberWithBool:headerCell.expanded]];
            NSIndexPath *securePrintRowIndexPath = [NSIndexPath indexPathForRow:(indexPath.row + 1)
                                                                      inSection:indexPath.section];
            NSIndexPath *pinCodeRowIndexPath = [NSIndexPath indexPathForRow:(indexPath.row + 2)
                                                                  inSection:indexPath.section];
            if(headerCell.expanded)
            {
                [tableView insertRowsAtIndexPaths:@[securePrintRowIndexPath, pinCodeRowIndexPath]
                                 withRowAnimation:UITableViewRowAnimationFade];
                headerCell.separator.hidden = YES;
            }
            else
            {
                [tableView deleteRowsAtIndexPaths:@[securePrintRowIndexPath, pinCodeRowIndexPath]
                                 withRowAnimation:UITableViewRowAnimationFade];
                if(section != [self.supportedSettings count])
                {
                    headerCell.separator.hidden = NO;
                }
            }
        }
    }
    else
    {
        NSArray *settings = [self.supportedSettings objectAtIndex:section];
        if (row == 0)
        {
            BOOL isExpanded = [[self.expandedSections objectAtIndex:section] boolValue];
            [self.expandedSections replaceObjectAtIndex:section withObject:[NSNumber numberWithBool:!isExpanded]];
            
            NSUInteger settingsCount = [settings count];
            NSMutableArray *indexPaths = [[NSMutableArray alloc] init];
            for (int i = 0; i < settingsCount; i++)
            {
                [indexPaths addObject:[NSIndexPath indexPathForRow:i + 1 inSection:indexPath.section]];
            }
            
            PrintSettingsHeaderCell *headerCell = (PrintSettingsHeaderCell *)[tableView cellForRowAtIndexPath:indexPath];
            if (isExpanded)
            {
                [tableView deleteRowsAtIndexPaths:indexPaths withRowAnimation:UITableViewRowAnimationFade];
                headerCell.expanded = NO;
                if(section != [self.supportedSettings count])
                {
                    headerCell.separator.hidden = NO;
                }
            }
            else
            {
                [tableView insertRowsAtIndexPaths:indexPaths withRowAnimation:UITableViewRowAnimationFade];
                headerCell.expanded = YES;
                headerCell.separator.hidden = YES;
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
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return ROW_HEIGHT_SINGLE;
}

- (IBAction)tapAction:(id)sender
{
    [self.tableView endEditing:YES];
}

#pragma mark - Setting:Switch

- (IBAction)switchAction:(id)sender
{
    UISwitch *switchView = sender;
    NSString *key = [self.switchBindings objectForKey:[NSNumber numberWithInteger:switchView.tag]];
    [self.previewSetting setValue:[NSNumber numberWithBool:switchView.on] forKey:key];

    if ([key isEqualToString:KEY_SECURE_PRINT])
    {
        [self setState:switchView.on forSettingKey:KEY_PIN_CODE];
    }
}

#pragma mark - Setting:TextField

- (void)endEditing
{
    [self.tableView endEditing:YES];
}

- (void)textFieldDidEndEditing:(UITextField *)textField
{
    NSString *key = [self.textFieldBindings objectForKey:[NSNumber numberWithInteger:textField.tag]];
    if([key isEqualToString:KEY_COPIES] == YES)
    {
        NSInteger value = [textField.text integerValue];
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
        [self.previewSetting setValue:[NSNumber numberWithInteger:value] forKey:key];
    }
    else if ([key isEqualToString:KEY_PIN_CODE] == YES)
    {
        [self.previewSetting setValue:textField.text forKey:key];
    }
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
        if((textField.text.length + string.length - range.length) > 4)
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
        if((textField.text.length + string.length - range.length) > 8)
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

#pragma mark - Setting:Options

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"PrintSettings-PrintOptions"])
    {
        PrintSettingsOptionTableViewController *optionsController = segue.destinationViewController;
        optionsController.setting = self.currentSetting;
        optionsController.previewSetting = self.previewSetting;
    }
}

#pragma mark - Settings KVO

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

#pragma mark - Apply Settings

- (void)applySettingsConstraintForKey:(NSString*)key withPreviousValue:(NSInteger)previousValue
{
    if (self.printDocument.disableContraints == YES)
    {
        return;
    }
    
    if([key isEqualToString:KEY_BOOKLET] == YES)
    {
        [self applyBookletConstraints];
    }
    if([key isEqualToString:KEY_BOOKLET_FINISH] == YES)
    {
        [self applyBookletFinishConstraints];
    }
    if([key isEqualToString:KEY_FINISHING_SIDE] == YES)
    {
        [self applyFinishingConstraintsWithPreviousValue:previousValue];
#if PUNCH_3_4_BINDING_SIDE_CONSTRAINT_ENABLED
        [self applyFinishingWithOrientationConstraint];
#endif
    }
#if PUNCH_3_4_BINDING_SIDE_CONSTRAINT_ENABLED
    if([key isEqualToString:KEY_ORIENTATION] == YES)
    {
        [self applyFinishingWithOrientationConstraint];
    }
#endif
    if([key isEqualToString:KEY_PUNCH] == YES)
    {
        [self applyPunchConstraint];
    }
    
    if([key isEqualToString:KEY_IMPOSITION] == YES)
    {
        [self applyImpositionConstraintWithPreviousValue:previousValue];
#if PUNCH_3_4_BINDING_SIDE_CONSTRAINT_ENABLED
        [self applyFinishingWithOrientationConstraint];
#endif
    }
}

- (void)applyBookletConstraints
{
    if(self.previewSetting.booklet == YES)
    {
        [self setOptionSettingWithKey:KEY_DUPLEX toValue:(NSInteger)kDuplexSettingShortEdge];
        [self setOptionSettingWithKey:KEY_IMPOSITION toValue:(NSInteger)kImpositionOff];
        [self setOptionSettingWithKey:KEY_IMPOSITION_ORDER toValue:(NSInteger)kImpositionOrderLeftToRight];
        [self setOptionSettingWithKey:KEY_FINISHING_SIDE toValue:(NSInteger)kFinishingSideLeft];
        [self setOptionSettingWithKey:KEY_STAPLE toValue:(NSInteger)kStapleTypeNone];
        [self setOptionSettingWithKey:KEY_PUNCH toValue:(NSInteger)kPunchTypeNone];
    }
    else
    {
        [self setOptionSettingToDefaultValue:KEY_DUPLEX];
        [self setOptionSettingToDefaultValue:KEY_IMPOSITION_ORDER];
        [self setOptionSettingToDefaultValue:KEY_FINISHING_SIDE];
        [self setOptionSettingToDefaultValue:KEY_STAPLE];
        [self setOptionSettingToDefaultValue:KEY_PUNCH];
    }
    
    [self setOptionSettingToDefaultValue:KEY_BOOKLET_LAYOUT];
    [self setOptionSettingToDefaultValue:KEY_BOOKLET_FINISH];
    [self reloadRowsForIndexPathsToUpdate];
}

- (void)applyBookletFinishConstraints
{
    if (self.previewSetting.bookletFinish != kBookletTypeOff)
    {
        [self setOptionSettingWithKey:KEY_OUTPUT_TRAY toValue:(NSInteger)kOutputTrayAuto];
    }
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
            [self.previewSetting setValue:[NSNumber numberWithInteger:previousFinishingSide] forKey:KEY_FINISHING_SIDE];
            break;
    }
}

#if PUNCH_3_4_BINDING_SIDE_CONSTRAINT_ENABLED
- (void)applyFinishingWithOrientationConstraint
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
#endif

- (void)applyPunchConstraint
{
    kPunchType punch = (kPunchType)self.previewSetting.punch;
    
#if PUNCH_3_4_BINDING_SIDE_CONSTRAINT_ENABLED
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
#endif
    
#if OUTPUT_TRAY_CONSTRAINT_ENABLED
    if(punch != kPunchTypeNone && self.previewSetting.outputTray == kOutputTrayFaceDownTray &&
       [self.printer.enabled_tray_face_down boolValue] == YES)
    {
        [self setOptionSettingWithKey:KEY_OUTPUT_TRAY toValue:(NSInteger) kOutputTrayAuto];
    }
#endif //OUTPUT_TRAY_CONSTRAINT_ENABLED
}

- (void)applyImpositionConstraintWithPreviousValue:(NSInteger)previousImpositionValue
{
    kImposition currentImpositionValue  = (kImposition) self.previewSetting.imposition;
    kImpositionOrder impositionOrder = (kImpositionOrder) self.previewSetting.impositionOrder;
    switch(currentImpositionValue)
    {
        case kImpositionOff:
            if([self.indexPathsForSettings objectForKey:KEY_IMPOSITION] != nil)
            {
                [self addToIndexToUpdate:[self.indexPathsForSettings objectForKey:KEY_IMPOSITION]];
            }
            [self setOptionSettingWithKey:KEY_IMPOSITION_ORDER toValue:kImpositionOrderLeftToRight];
            break;
        case kImposition2Pages:
            if (self.previewSetting.booklet == YES)
            {
                [self setOptionSettingWithKey:KEY_BOOKLET toValue:(NSInteger)kBookletTypeOff];
            }
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
            if (self.previewSetting.booklet == YES)
            {
                [self setOptionSettingWithKey:KEY_BOOKLET toValue:(NSInteger)kBookletTypeOff];
            }
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
            [self.previewSetting setValue:[NSNumber numberWithInteger:previousImpositionValue] forKey:KEY_IMPOSITION];
            break;
    }
}

#pragma mark - Update Settings

- (void)setState:(BOOL)isEnabled forSettingKey:(NSString*)key
{
    NSIndexPath *indexPath = [self.indexPathsForSettings objectForKey:key];
    if(indexPath != nil)
    {
        UITableViewCell *cell = [self.tableView cellForRowAtIndexPath:indexPath];
        [cell setUserInteractionEnabled:isEnabled];
        if([cell isKindOfClass:[PrintSettingsItemOptionCell class]])
        {
            [(PrintSettingsItemOptionCell *)cell setHideValue:![self isSettingApplicable:key]];
        }
        else if ([cell isKindOfClass:[PrintSettingsItemInputCell class]])
        {
            [(PrintSettingsItemInputCell *)cell setEnabled:isEnabled];
        }
    }
}

- (void)setOptionSettingToDefaultValue:(NSString*)key
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

- (void)setOptionSettingWithKey:(NSString*)key toValue:(NSInteger)value
{
    NSNumber *num = [NSNumber numberWithInteger:value];
    [self.previewSetting setValue:num forKey:key];
    
    NSIndexPath *indexPath = [self.indexPathsForSettings objectForKey:key];
    if(indexPath != nil)
    {
        [self addToIndexToUpdate:indexPath];
    }
}

#pragma mark - Check Settings

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
    if([settingKey isEqualToString:KEY_IMPOSITION_ORDER])
    {
        if(self.previewSetting.imposition == kImpositionOff &&
           self.previewSetting.booklet == NO)
        {
            return NO;
        }
    }
    
    return YES;
}

- (BOOL)isSettingOptionSupported:(NSString *)option
{
    if(self.printer == nil)
    {
        if([option isEqualToString:@"ids_lbl_punch_3holes"])
        {
            return NO;
        }
        
        return YES;
        
    }
    
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
        // Ver.2.0.0.4 Start
        //return  [self.printer.enabled_tray_face_down boolValue];
        return true; // face down tray is enables always.
        // Ver.2.0.0.4 End
    }
    
    if ([option isEqualToString:@"ids_lbl_outputtray_top"])
    {
        return [self.printer.enabled_tray_top boolValue];
    }
    
    if ([option isEqualToString:@"ids_lbl_outputtray_stacking"])
    {
        return [self.printer.enabled_tray_stacking boolValue];
    }
    
    // for ORPHIS FW start
    return [self isSettingSupportedISFWGD:option];
    // for ORPHIS FW end
    
    return YES;
}

- (BOOL)isISSeries
{
    if([self.printer.name isEqualToString:@"RISO IS1000C-J"] ||
       [self.printer.name isEqualToString:@"RISO IS1000C-G"] ||
       [self.printer.name isEqualToString:@"RISO IS950C-G"])
    {
        return YES;
    }
    return NO;
}

- (BOOL)isFWSeries
{
    if ([self.printer.name isEqualToString:@"ORPHIS FW5230"] ||
        [self.printer.name isEqualToString:@"ORPHIS FW5230A"] ||
        [self.printer.name isEqualToString:@"ORPHIS FW5231"] ||
        [self.printer.name isEqualToString:@"ORPHIS FW2230"] ||
        [self.printer.name isEqualToString:@"ORPHIS FW1230"] ||
        [self.printer.name isEqualToString:@"ComColor FW5230"] ||
        [self.printer.name isEqualToString:@"ComColor FW5230R"] ||
        [self.printer.name isEqualToString:@"ComColor FW5231"] ||
        [self.printer.name isEqualToString:@"ComColor FW5231R"] ||
        [self.printer.name isEqualToString:@"ComColor FW5000"] ||
        [self.printer.name isEqualToString:@"ComColor FW5000R"] ||
        [self.printer.name isEqualToString:@"ComColor FW2230"] ||
        [self.printer.name isEqualToString:@"ComColor black FW1230"] ||
        [self.printer.name isEqualToString:@"ComColor black FW1230R"] ||
        [self.printer.name isEqualToString:@"Shan Cai Yin Wang FW5230"] ||
        [self.printer.name isEqualToString:@"Shan Cai Yin Wang FW5230R"] ||
        [self.printer.name isEqualToString:@"Shan Cai Yin Wang FW5231"] ||
        [self.printer.name isEqualToString:@"Shan Cai Yin Wang FW2230 Wenjianhong"] ||
        [self.printer.name isEqualToString:@"Shan Cai Yin Wang FW2230 Lan"] ||
        [self.printer.name isEqualToString:@"Shan Cai Yin Wang black FW1230"] ||
        [self.printer.name isEqualToString:@"Shan Cai Yin Wang black FW1230R"]
        )
    {
        return YES;
    }
    return NO;
}

- (BOOL)isSettingSupportedISFWGD:(NSString *)option
{
    // for ORPHIS FW start
    if ([self isISSeries])
    {
    if([option isEqualToString:@"ids_lbl_papertype_roughpaper"] ||
       [option isEqualToString:@"ids_lbl_papersize_legal13"] ||
       [option isEqualToString:@"ids_lbl_papersize_8K"] ||
       [option isEqualToString:@"ids_lbl_papersize_16K"] ||
       [option isEqualToString:@"ids_lbl_colormode_2color"] )
        {
            return NO;
        }
    }
    else if ([self isFWSeries])
    {
        if([option isEqualToString:@"ids_lbl_inputtray_tray3"])
        {
            return NO;
        }
    }
    else // GD Series
    {
        if([option isEqualToString:@"ids_lbl_papertype_roughpaper"] ||
           [option isEqualToString:@"ids_lbl_colormode_2color"] )
        {
            return NO;
        }
    }
    return YES;
}
// for ORPHIS FW end

- (BOOL)isSettingSupported:(NSString*)settingKey
{
    if(self.printer == nil)
    {
        return YES;
    }

    if([settingKey isEqualToString:KEY_BOOKLET_FINISH])
    {
        return [self.printer.enabled_booklet_finishing boolValue];
    }
    
    if([settingKey isEqualToString:KEY_PUNCH])
    {
        return ([self.printer.enabled_finisher_2_3_holes boolValue] || [self.printer.enabled_finisher_2_4_holes boolValue]);
    }
    
    if([settingKey isEqualToString:KEY_STAPLE])
    {
        return [self.printer.enabled_staple boolValue];
    }
    
#if GET_ORIENTATION_FROM_PDF_ENABLED
    if ([settingKey isEqualToString:KEY_ORIENTATION])
    {
        return NO;
    }
#endif
    
    return YES;
}

#pragma mark - Utilities

- (void)addToIndexToUpdate:(NSIndexPath *)indexPath
{
    if([self.indexPathsToUpdate containsObject:indexPath] == NO && [[self.expandedSections objectAtIndex:indexPath.section] boolValue] == YES)
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

- (void)fillSupportedSettings
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
                // for ORPHIS FW start
                //if([key isEqualToString:KEY_PUNCH] == YES || [key isEqualToString:KEY_OUTPUT_TRAY] == YES)
                
                if([key isEqualToString:KEY_PUNCH] == YES || [key isEqualToString:KEY_OUTPUT_TRAY] == YES ||
                   [key isEqualToString:@"paperSize"] == YES || [key isEqualToString:KEY_PAPER_TYPE] == YES ||
                   [key isEqualToString:KEY_INPUT_TRAY] == YES || [key isEqualToString:@"colorMode"] == YES)
                 // for ORPHIS FW end
                    
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

@end

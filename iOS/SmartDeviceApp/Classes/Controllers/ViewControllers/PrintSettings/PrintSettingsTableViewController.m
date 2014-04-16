//
//  PrintSettingsTableViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
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

#define PRINTER_HEADER_CELL @"PrinterHeaderCell"
#define PRINTER_ITEM_CELL @"PrinterItemCell"
#define SETTING_HEADER_CELL @"SettingHeaderCell"
#define SETTING_ITEM_OPTION_CELL @"SettingItemOptionCell"
#define SETTING_ITEM_INPUT_CELL @"SettingItemInputCell"
#define SETTING_ITEM_SWITCH_CELL @"SettingItemSwitchCell"

#define PRINTER_SECTION  0
#define PRINTER_SECTION_HEADER_ROW 0
#define PRINTER_SECTION_ITEM_ROW 1

#define ROW_HEIGHT_SINGLE 44
#define ROW_HEIGHT_DOUBLE 66

@interface PrintSettingsTableViewController ()

@property (nonatomic) BOOL showPrinterSelection;

@property (nonatomic, weak) NSDictionary *printSettingsTree;
@property (nonatomic, strong) NSMutableArray *expandedSections;
@property (nonatomic, weak) NSDictionary *currentSetting;
@property (nonatomic, weak) UITapGestureRecognizer *tapRecognizer;
@property (nonatomic, strong) NSMutableDictionary *textFieldBindings;
@property (nonatomic, strong) NSMutableDictionary *switchBindings;

@property (nonatomic, strong) NSMutableDictionary *indexPathsForSettings;
@property (nonatomic, strong) NSMutableArray *indexPathsToUpdate;
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

    self.showPrinterSelection = YES;
        
    // Get print settings tree
    self.printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    
    // Prepare expansion
    self.expandedSections = [[NSMutableArray alloc] init];
    NSArray *sections = [self.printSettingsTree objectForKey:@"group"];
    for (id section in sections)
    {
        [self.expandedSections addObject:[NSNumber numberWithBool:YES]];
    }
    
    if(self.isDefaultSettingsMode == YES)
    {
        self.showPrinterSelection = NO;
    }
    
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
    [PrintSettingsHelper removeObserver:self fromPreviewSetting:&previewSetting];
}

- (void)viewDidAppear:(BOOL)animated
{
    [self reloadRowsForIndexPathsToUpdate];
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
    /*if (self.showPrinterSelection)
    {
        sections++;
    }*/
    return sections + 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    NSInteger logicalSection = section;
    
    /*if (self.showPrinterSelection == NO)
    {
        logicalSection++;
    }*/
    
    if (logicalSection == 0)
    {
        if(self.showPrinterSelection == NO)
        {
            return 1; //show only printer name not printer button header

        }
        else
        {
            return 2;
        }
    }
    else if (logicalSection >= 1)
    {
        if ([[self.expandedSections objectAtIndex:logicalSection - 1] boolValue] == NO)
        {
            return 1;
        }
        
        NSDictionary *group = [[self.printSettingsTree objectForKey:@"group"] objectAtIndex:logicalSection - 1];
        return [[group objectForKey:@"setting"] count] + 1;
    }
    return 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell;
    NSInteger section = indexPath.section;
    NSInteger row = indexPath.row;
    
    /*if (self.showPrinterSelection == NO)
    {
        section++;
    }*/
    
    if (section == 0)
    {
        if (row == 0 && self.showPrinterSelection == YES)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:PRINTER_HEADER_CELL forIndexPath:indexPath];
        }
        else
        {
            PrintSettingsPrinterItemCell *printerItemCell = [tableView dequeueReusableCellWithIdentifier:PRINTER_ITEM_CELL forIndexPath:indexPath];
            
            if(self.printer != nil)
            {
                printerItemCell.printerNameLabel.hidden = NO;
                printerItemCell.printerIPLabel.hidden = NO;
                printerItemCell.selectPrinterLabel.hidden = YES;
                printerItemCell.printerNameLabel.text = self.printer.name;
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
    else
    {
        NSDictionary *group = [[self.printSettingsTree objectForKey:@"group"] objectAtIndex:section - 1];
        if (row == 0)
        {
            PrintSettingsHeaderCell *headerCell = [tableView dequeueReusableCellWithIdentifier:SETTING_HEADER_CELL forIndexPath:indexPath];
            headerCell.groupLabel.localizationId = [group objectForKey:@"text"];
            headerCell.expanded = YES;
            cell = headerCell;
        }
        else
        {
            NSArray *settings = [group objectForKey:@"setting"];
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
    /*if (self.showPrinterSelection == NO)
    {
        section++;
    }*/
    
    if (section > 0)
    {
        NSArray *settings = [[[self.printSettingsTree objectForKey:@"group"] objectAtIndex:section - 1] objectForKey:@"setting"];
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
        if(row > 0 && self.showPrinterSelection == YES)
        {
            [self performSegueWithIdentifier:@"PrintSettings-PrinterList" sender:self];
        }
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 0)
    {
        NSInteger printerDisplayRow = (self.showPrinterSelection == YES) ? 1: 0;
        if(indexPath.row == printerDisplayRow)
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
    [self.previewSetting setValue:[NSNumber numberWithInteger:[textField.text integerValue]] forKey:key];
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
    if ([segue.identifier isEqualToString:@"PrintSettings-PrinterList"])
    {
        PrintSettingsPrinterListTableViewController *printerListController = segue.destinationViewController;
        printerListController.selectedPrinter = self.printer;
    }
}

- (IBAction)unwindToPrintSettings:(UIStoryboardSegue *)sender
{
    if([sender.sourceViewController isKindOfClass:[PrintSettingsPrinterListTableViewController class]])
    {
        self.printer = ((PrintSettingsPrinterListTableViewController *)sender.sourceViewController).selectedPrinter;
        
        if(self.printer != nil)
        {
            [[[PDFFileManager sharedManager] printDocument] setPrinter:self.printer];
        }
        
        NSIndexPath *printerIndexPath = [NSIndexPath indexPathForRow:PRINTER_SECTION_ITEM_ROW inSection:PRINTER_SECTION];
        if(self.printer != nil)
        {
            PrintSettingsPrinterItemCell * printerItemCell = (PrintSettingsPrinterItemCell *)[self.tableView cellForRowAtIndexPath:printerIndexPath];
        
            printerItemCell.printerNameLabel.hidden = NO;
            printerItemCell.printerIPLabel.hidden = NO;
            printerItemCell.selectPrinterLabel.hidden = YES;
            printerItemCell.printerNameLabel.text = self.printer.name;
            printerItemCell.printerIPLabel.text = self.printer.ip_address;
        }
    }
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
        [self applyOrientationConstraint];
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
    [self setState:[self isSettingEnabled:KEY_DUPLEX] forSettingKey:KEY_DUPLEX];
    [self setState:[self isSettingEnabled:KEY_IMPOSITION] forSettingKey:KEY_IMPOSITION];
    [self setState:[self isSettingEnabled:KEY_IMPOSITION_ORDER] forSettingKey:KEY_IMPOSITION_ORDER];

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
    
    if(punch == kPunchType3Holes || punch == kPunchType4Holes)
    {
        if((finishingSide != kFinishingSideTop  && isPaperLandscape == YES) ||
           (finishingSide == kFinishingSideTop && isPaperLandscape == NO))
        {
            [self setOptionSettingToDefaultValue:KEY_PUNCH];
        }
    }
}

-(void) applyOrientationConstraint
{
    if(self.previewSetting.booklet == YES)
    {
        kOrientation orientation = (kOrientation) self.previewSetting.orientation;
        kBookletLayout bookletLayout = (kBookletLayout) self.previewSetting.bookletLayout;
        if(orientation == kOrientationPortrait)
        {
            if(bookletLayout == kBookletLayoutTopToBottom)
            {
                [self setOptionSettingToDefaultValue:KEY_BOOKLET_LAYOUT];
            }
        }
        else
        {
            if(bookletLayout != kBookletLayoutTopToBottom)
            {
                [self setOptionSettingWithKey:KEY_BOOKLET_LAYOUT toValue:(NSInteger) kBookletLayoutTopToBottom];
            }
        }
    }
}

-(void) applyPunchConstraint
{
    kPunchType punch = (kPunchType)self.previewSetting.punch;
    kFinishingSide finishingSide = (kFinishingSide)self.previewSetting.finishingSide;
    BOOL isPaperLandscape = [PrintPreviewHelper isPaperLandscapeForPreviewSetting:self.previewSetting];
    
    if(punch == kPunchType3Holes || punch == kPunchType4Holes)
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
    NSNumber *num = [NSNumber numberWithInt:value];
    [self.previewSetting setValue:num forKey:key];
    
    NSIndexPath *indexPath = [self.indexPathsForSettings objectForKey:key];
    if(indexPath != nil)
    {
        [self addToIndexToUpdate:indexPath];
    }
}

-(BOOL) isSettingEnabled:(NSString*) settingKey
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

- (void)addToIndexToUpdate:(NSIndexPath *)indexPath
{
    if([self.indexPathsToUpdate containsObject:indexPath] == NO)
    {
        [self.indexPathsToUpdate addObject:indexPath];
    }
}

- (void) reloadRowsForIndexPathsToUpdate
{
    if([self.indexPathsToUpdate count] > 0)
    {
        [self.tableView reloadRowsAtIndexPaths:self.indexPathsToUpdate withRowAnimation:UITableViewRowAnimationFade];
        [self.indexPathsToUpdate removeAllObjects];
    }
}

@end

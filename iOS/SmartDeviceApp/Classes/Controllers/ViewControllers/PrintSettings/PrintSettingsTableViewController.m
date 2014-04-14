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

#define PRINTER_HEADER_CELL @"PrinterHeaderCell"
#define PRINTER_ITEM_CELL @"PrinterItemCell"
#define SETTING_HEADER_CELL @"SettingHeaderCell"
#define SETTING_ITEM_OPTION_CELL @"SettingItemOptionCell"
#define SETTING_ITEM_INPUT_CELL @"SettingItemInputCell"
#define SETTING_ITEM_SWITCH_CELL @"SettingItemSwitchCell"
#define PRINTSETTING_CONTEXT @"PreviewSettingContext"

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
@property (nonatomic, weak) PrintDocument *printDocument;
@property (nonatomic, weak) UITapGestureRecognizer *tapRecognizer;
@property (nonatomic, strong) NSMutableDictionary *textFieldBindings;
@property (nonatomic, strong) NSMutableDictionary *switchBindings;
@property (nonatomic, weak) Printer *printer;
@property (nonatomic, strong) NSArray *settingsWithConstraints;
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

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    // DEBUG
    self.showPrinterSelection = YES;
    
    // Get Document
    self.printDocument = [[PDFFileManager sharedManager] printDocument];
    
    // Get print settings tree
    self.printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    
    // Prepare expansion
    self.expandedSections = [[NSMutableArray alloc] init];
    NSArray *sections = [self.printSettingsTree objectForKey:@"group"];
    for (id section in sections)
    {
        [self.expandedSections addObject:[NSNumber numberWithBool:YES]];
    }
    
    //get default printer
    PrinterManager *printerManager = [PrinterManager sharedPrinterManager];
    self.printer = [printerManager getDefaultPrinter];
    
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
    //add observer for settings that have constraint
    self.settingsWithConstraints =[NSArray arrayWithObjects:KEY_BOOKLET, KEY_PUNCH, KEY_FINISHING_SIDE, KEY_IMPOSITION, KEY_ORIENTATION, nil];
    self.indexPathsForSettings = [NSMutableDictionary dictionary];
    self.indexPathsToUpdate = [[NSMutableArray array] mutableCopy];
    [self addObserversForSettingsWithConstraints];
}

-(void)dealloc
{
    [self removeObserversForSettingsWithConstraints];
}

- (void)viewDidAppear:(BOOL)animated
{
    if([self.indexPathsToUpdate count] > 0)
    {
        [self.tableView reloadRowsAtIndexPaths:self.indexPathsToUpdate withRowAnimation:UITableViewRowAnimationFade];
        [self.indexPathsToUpdate removeAllObjects];
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
    if (self.showPrinterSelection)
    {
        sections++;
    }
    return sections;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    NSInteger logicalSection = section;
    if (self.showPrinterSelection == NO)
    {
        logicalSection++;
    }
    
    if (logicalSection == 0)
    {
        return 2;
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
    
    if (self.showPrinterSelection == NO)
    {
        section++;
    }
    
    if (section == 0)
    {
        if (row == 0)
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
                NSNumber *index = [self.printDocument.previewSetting valueForKey:key];
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
                itemInputCell.valueTextField.text = [[self.printDocument.previewSetting valueForKey:key] stringValue];
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
                if ([[self.printDocument.previewSetting valueForKey:key] boolValue] == YES)
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
    if (self.showPrinterSelection == NO)
    {
        section++;
    }
    
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
                [self.indexPathsToUpdate addObject:indexPath];
                self.currentSetting = [settings objectAtIndex:row - 1];
                [self performSegueWithIdentifier:@"PrintSettings-PrintOptions" sender:self];
            }
        }
    }
    else
    {
        if(row > 0)
        {
            [self performSegueWithIdentifier:@"PrintSettings-PrinterList" sender:self];
        }
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 0 && indexPath.row == 1)
    {
        return ROW_HEIGHT_DOUBLE;
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
    [self.printDocument.previewSetting setValue:[NSNumber numberWithBool:switchView.on] forKey:key];
}

- (void)textFieldDidEndEditing:(UITextField *)textField
{
    NSString *key = [self.textFieldBindings objectForKey:[NSNumber numberWithInteger:textField.tag]];
    [self.printDocument.previewSetting setValue:[NSNumber numberWithInteger:[textField.text integerValue]] forKey:key];
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


-(void) addObserversForSettingsWithConstraints
{
    for(NSString *key in self.settingsWithConstraints)
    {
        [self.printDocument.previewSetting addObserver:self forKeyPath:key options:NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld context:PRINTSETTING_CONTEXT];
    }
}
-(void) removeObserversForSettingsWithConstraints
{
    for(NSString *key in self.settingsWithConstraints)
    {
        [self.printDocument.previewSetting removeObserver:self forKeyPath:key context:PRINTSETTING_CONTEXT];
    }
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if (context == PRINTSETTING_CONTEXT) {
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
    [self setState:[self isSettingEnabled:KEY_FINISHING_SIDE] forSettingKey:KEY_FINISHING_SIDE];
    [self setState:[self isSettingEnabled:KEY_PUNCH] forSettingKey:KEY_PUNCH];
    [self setState:[self isSettingEnabled:KEY_STAPLE] forSettingKey:KEY_STAPLE];
    [self setState:[self isSettingEnabled:KEY_IMPOSITION] forSettingKey:KEY_IMPOSITION];
    [self setState:[self isSettingEnabled:KEY_IMPOSITION_ORDER] forSettingKey:KEY_IMPOSITION_ORDER];
    [self setState:[self isSettingEnabled:KEY_BOOKLET_FINISH] forSettingKey:KEY_BOOKLET_FINISH];
    [self setState:[self isSettingEnabled:KEY_BOOKLET_LAYOUT] forSettingKey:KEY_BOOKLET_LAYOUT];

    [self setOptionSettingToDefaultValue:KEY_FINISHING_SIDE];
    [self setOptionSettingToDefaultValue:KEY_STAPLE];
    [self setOptionSettingToDefaultValue:KEY_PUNCH];
    [self setOptionSettingToDefaultValue:KEY_BOOKLET_FINISH];
    [self setOptionSettingToDefaultValue:KEY_BOOKLET_LAYOUT];
}



- (void)applyFinishingConstraintsWithPreviousValue:(NSInteger)previousFinishingSide
{
    kFinishingSide currentFinishingSide = (kFinishingSide)self.printDocument.previewSetting.finishingSide;
    kStapleType staple = (kStapleType)self.printDocument.previewSetting.staple;
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
    kPunchType punch = (kPunchType)self.printDocument.previewSetting.punch;
    BOOL isPaperLandscape = [PrintPreviewHelper isPaperLandscapeForPreviewSetting:self.printDocument.previewSetting];
    kFinishingSide finishingSide = (kFinishingSide)self.printDocument.previewSetting.finishingSide;
    
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
    kOrientation orientation = (kOrientation) self.printDocument.previewSetting.orientation;
    //TODO
}

-(void) applyPunchConstraint
{
    kPunchType punch = (kPunchType)self.printDocument.previewSetting.punch;
    kFinishingSide finishingSide = (kFinishingSide)self.printDocument.previewSetting.finishingSide;
    BOOL isPaperLandscape = [PrintPreviewHelper isPaperLandscapeForPreviewSetting:self.printDocument.previewSetting];
    
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
    kImposition currentImpositionValue  = (kImposition) self.printDocument.previewSetting.imposition;
    kImpositionOrder impositionOrder = (kImpositionOrder) self.printDocument.previewSetting.impositionOrder;
    switch(currentImpositionValue)
    {
        case kImpositionOff:
            [self setOptionSettingWithKey:KEY_IMPOSITION_ORDER toValue:kImpositionOrderLeftToRight];
            [self setState:NO forSettingKey:KEY_IMPOSITION_ORDER];
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
                [self setState:YES forSettingKey:KEY_IMPOSITION_ORDER];
                [self setOptionSettingWithKey:KEY_IMPOSITION_ORDER toValue:kImpositionOrderUpperLeftToRight];
            }
            break;
        default:
            break;
    }
}

-(void) setState:(BOOL)isEnabled forSettingKey:(NSString*)key
{
    NSIndexPath *indexPath = [self.indexPathsForSettings objectForKey:key];
    UITableViewCell *cell = [self.tableView cellForRowAtIndexPath:indexPath];
    [cell setUserInteractionEnabled:isEnabled];
}

-(void) setOptionSettingToDefaultValue: (NSString*)key
{
    NSIndexPath *indexPath = [self.indexPathsForSettings objectForKey:key];
    PrintSettingsItemOptionCell *itemOptionCell = (PrintSettingsItemOptionCell*)[self.tableView cellForRowAtIndexPath:indexPath];
   
    //get default value
    NSDictionary *group = [[self.printSettingsTree objectForKey:@"group"] objectAtIndex:indexPath.section - 1];
    NSArray *settings = [group objectForKey:@"setting"];
    NSDictionary *setting = [settings objectAtIndex:indexPath.row - 1];
    
    NSString *defaultValue = [setting objectForKey:@"default"];
    
    NSInteger value = [defaultValue integerValue];
    NSArray *options = [setting objectForKey:@"option"];
    NSString *selectedOption = [[options objectAtIndex:value] objectForKey:@"content-body"];
    itemOptionCell.valueLabel.localizationId = selectedOption;
    
    [self.printDocument.previewSetting setValue:[NSNumber numberWithInteger:value] forKey:key];
}

-(void) setOptionSettingWithKey:(NSString*)key toValue:(NSInteger)value
{
    NSIndexPath *indexPath = [self.indexPathsForSettings objectForKey:key];
    NSNumber *num = [NSNumber numberWithInt:value];
    [self.printDocument.previewSetting setValue:num forKey:key];
    [self.indexPathsToUpdate addObject:indexPath];
}

-(BOOL) isSettingEnabled:(NSString*) settingKey
{
    if([settingKey isEqualToString:KEY_DUPLEX])
    {
        if(self.printDocument.previewSetting.booklet == YES)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_FINISHING_SIDE])
    {
        if(self.printDocument.previewSetting.booklet == YES)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_STAPLE])
    {
        if(self.printDocument.previewSetting.booklet == YES)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_PUNCH])
    {
        if(self.printDocument.previewSetting.booklet == YES)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_IMPOSITION])
    {
        if(self.printDocument.previewSetting.booklet == YES)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_IMPOSITION_ORDER])
    {
        if(self.printDocument.previewSetting.booklet == YES ||
           self.printDocument.previewSetting.imposition == kImpositionOff)
        {
            return NO;
        }
    }
    
    if([settingKey isEqualToString:KEY_BOOKLET_LAYOUT] ||
       [settingKey isEqualToString:KEY_BOOKLET_FINISH])
    {
        return self.printDocument.previewSetting.booklet;
    }
    
    return YES;
}

@end

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
#import "PrintSettingsHelper.h"
#import "UIView+Localization.h"
#import "PDFFileManager.h"
#import "PrintDocument.h"
#import "PreviewSetting.h"

#define PRINTER_HEADER_CELL @"PrinterHeaderCell"
#define PRINTER_ITEM_CELL @"PrinterItemCell"
#define SETTING_HEADER_CELL @"SettingHeaderCell"
#define SETTING_ITEM_OPTION_CELL @"SettingItemOptionCell"
#define SETTING_ITEM_INPUT_CELL @"SettingItemInputCell"
#define SETTING_ITEM_SWITCH_CELL @"SettingItemSwitchCell"

#define ROW_HEIGHT_SINGLE 44
#define ROW_HEIGHT_DOUBLE 66

@interface PrintSettingsTableViewController ()

@property (nonatomic) BOOL showPrinterSelection;
@property (nonatomic, weak) NSDictionary *printSettingsTree;
@property (nonatomic, strong) NSMutableArray *expandedSections;
@property (nonatomic, weak) NSDictionary *currentSetting;
@property (nonatomic, strong) NSIndexPath *indexPathToUpdate;
@property (nonatomic, weak) PrintDocument *printDocument;
@property (nonatomic, weak) UITapGestureRecognizer *tapRecognizer;
@property (nonatomic, strong) NSMutableDictionary *textFieldBindings;
@property (nonatomic, strong) NSMutableDictionary *switchBindings;

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
}

- (void)viewDidAppear:(BOOL)animated
{
    if (self.indexPathToUpdate != nil)
    {
        [self.tableView reloadRowsAtIndexPaths:@[self.indexPathToUpdate] withRowAnimation:UITableViewRowAnimationFade];
        self.indexPathToUpdate = nil;
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
            printerItemCell.printerNameLabel.text = @"RISO Printer IS900";
            printerItemCell.printerIPLabel.text = @"192.168.1.123";
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
                self.indexPathToUpdate = indexPath;
                self.currentSetting = [settings objectAtIndex:row - 1];
                [self performSegueWithIdentifier:@"PrintSettings-PrintOptions" sender:self];
            }
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
}

/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

/*
#pragma mark - Navigation

// In a story board-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}

 */

@end

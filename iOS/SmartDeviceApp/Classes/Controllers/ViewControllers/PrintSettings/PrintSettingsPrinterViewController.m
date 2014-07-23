//
//  PrintSettingsPrinterViewController.m
//  RISOSmartPrint
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintSettingsPrinterViewController.h"
#import "PrintSettingsTableViewController.h"
#import "Printer.h"
#import "PrintSettingsPrinterItemCell.h"
#import "PrintSettingsPrinterHeaderCell.h"
#import "DirectPrintManager.h"
#import "PDFFileManager.h"
#import "PrinterManager.h"
#import "PrintDocument.h"
#import "AlertHelper.h"
#import "NetworkManager.h"
#import "Printer.h"
#import "PrintJobHistoryViewController.h"

#define SEGUE_TO_PRINTSETTINGS_TABLE @"PrintSettingsPrinter-PrintSettingsTable"
#define ROW_HEIGHT_SINGLE 44
#define ROW_HEIGHT_DOUBLE 55

#define PRINTER_HEADER_CELL @"PrinterHeaderCell"
#define PRINTER_ITEM_CELL @"PrinterItemCell"
#define PRINTER_ITEM_DEFAULT_CELL @"PrinterItemDefaultCell"

@interface PrintSettingsPrinterViewController () <DirectPrintManagerDelegate>

/**
 * Reference to the height constraint of the {@link printerTableView}.
 */
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *tableViewHeight;

/**
 * Flag that indicates whether this is for the "Default Print Settings" screen
 * or the "Print Settings" screen.
 */
@property (assign, nonatomic) BOOL isDefaultSettingsMode;

/**
 * Reference to the selected printer.
 */
@property (weak, nonatomic) Printer* printer;

/** 
 * Reference to the UITableView showing the selected printer and the print button.
 * The print button is only available for the "Print Settings" screen.
 */
@property (weak, nonatomic) IBOutlet UITableView *printerTableView;

/**
 * Reference to the PrintSettingsTableViewController.
 */
@property (weak, nonatomic) PrintSettingsTableViewController* settingsController;

/**
 * Responds to tapping the print button which executes the print operation.
 */
- (void)executePrint;

/**
 * Responds to tapping the selected printer, which 
 * loads the PrintSettingsPrinterListViewController.
 */
- (void)loadPrinterList;

/**
 * Called when the keypad is about to be shown.
 *
 * @param notification the notification object
 */
- (void)keyboardWillShow:(NSNotification *)notification;

/**
 * Called when the keypad is about to be dismissed.
 *
 * @param notification the notification object
 */
- (void)keyboardWillHide:(NSNotification *)notification;

@end

@implementation PrintSettingsPrinterViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    if(self.printerIndex != nil)
    {
        // launched from Printers
        self.isDefaultSettingsMode = YES;
        self.tableViewHeight.constant = ROW_HEIGHT_DOUBLE;
        
        PrinterManager* pm = [PrinterManager sharedPrinterManager];
        self.printer = [pm getPrinterAtIndex:[self.printerIndex unsignedIntegerValue]];
    }
    else
    {
        // launched from Print Preview
        self.isDefaultSettingsMode = NO;
        self.tableViewHeight.constant = ROW_HEIGHT_SINGLE + ROW_HEIGHT_DOUBLE;
        
        self.printer = nil;
    }
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
    {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    }
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    if (!self.isDefaultSettingsMode)
    {
        PDFFileManager* pdfm = [PDFFileManager sharedManager];
        PrintDocument* doc = pdfm.printDocument;
        if (self.printer != doc.printer)
        {
            self.printer = doc.printer;
            [self.printerTableView reloadRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:1 inSection:0]]
                                         withRowAnimation:UITableViewRowAnimationNone];
        }
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)dealloc
{
    if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
    {
        [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardDidShowNotification object:nil];
        [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardDidHideNotification object:nil];
    }
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

#pragma mark - TableView

- (NSInteger)numberOfSectionsInTableView:(UITableView*)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section
{
    if (self.isDefaultSettingsMode)
    {
        // show only the printer name
        return 1;
    }
    else
    {
        // show the print button and the printer name
        return 2;
    }
}

- (UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath*)indexPath
{
    if (self.isDefaultSettingsMode)
    {
        // expecting only one row
        
        PrintSettingsPrinterItemCell* cell = [tableView dequeueReusableCellWithIdentifier:PRINTER_ITEM_DEFAULT_CELL
                                                                             forIndexPath:indexPath];
        [cell setPrinterName:self.printer.name];
        cell.printerIPLabel.text = self.printer.ip_address;
        return cell;
    }
    else
    {
        // expecting two rows
        
        if (indexPath.row == 0)
        {
            PrintSettingsPrinterHeaderCell* cell = [tableView dequeueReusableCellWithIdentifier:PRINTER_HEADER_CELL
                                                                                   forIndexPath:indexPath];
            return cell;
        }
        else
        {
            PrintSettingsPrinterItemCell* cell = [tableView dequeueReusableCellWithIdentifier:PRINTER_ITEM_CELL
                                                                                 forIndexPath:indexPath];
            
            if (self.printer == nil)
            {
                cell.printerNameLabel.hidden = YES;
                cell.printerIPLabel.hidden = YES;
                cell.selectPrinterLabel.hidden = NO;
            }
            else
            {
                cell.printerNameLabel.hidden = NO;
                cell.printerIPLabel.hidden = NO;
                cell.selectPrinterLabel.hidden = YES;
                
                [cell setPrinterName:self.printer.name];
                cell.printerIPLabel.text = self.printer.ip_address;
            }
            
            return cell;
        }
    }
}

- (CGFloat)tableView:(UITableView*)tableView heightForRowAtIndexPath:(NSIndexPath*)indexPath
{
    if (self.isDefaultSettingsMode)
        return ROW_HEIGHT_DOUBLE;
    else
    {
        if (indexPath.row == 0)
        {
            return ROW_HEIGHT_SINGLE;
        }
        else
        {
            return ROW_HEIGHT_DOUBLE;
        }
    }
}

- (BOOL)tableView:(UITableView*)tableView shouldHighlightRowAtIndexPath:(NSIndexPath*)indexPath
{
    if (self.isDefaultSettingsMode)
        return NO;
    else
        return YES;
}

- (void)tableView:(UITableView*)tableView didSelectRowAtIndexPath:(NSIndexPath*)indexPath
{
    if (!self.isDefaultSettingsMode)
    {
        if (indexPath.row == 0)
        {
            //to force close keypads and save the textfield contents (i.e. copies, pin code)
            [self.settingsController endEditing];
            
            [self executePrint];
        }
        else
        {
            [self loadPrinterList];
        }
    }
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if([segue.identifier isEqualToString:SEGUE_TO_PRINTSETTINGS_TABLE] == YES)
    {
        PrintSettingsTableViewController *viewController = (PrintSettingsTableViewController *)segue.destinationViewController;
        viewController.printerIndex = self.printerIndex;
        self.settingsController = viewController;
    }
}

#pragma mark - Printing

- (void)executePrint
{
    // check if printer is selected
    if (self.printer == nil)
    {
        [AlertHelper displayResult:kAlertResultErrNoPrinterSelected
                         withTitle:kAlertTitleDefault
                       withDetails:nil];
        return;
    }
    
    if (![NetworkManager isConnectedToLocalWifi])
    {
        [AlertHelper displayResult:kAlertResultErrNoNetwork withTitle:kAlertTitleDefault withDetails:nil];
        return;
    }
    
    DirectPrintManager *manager = [[DirectPrintManager alloc] init];
    manager.delegate = self;
    
    if ([self.printer.port integerValue] == 0)
    {
        [manager printDocumentViaLPR];
    }
    else
    {
        [manager printDocumentViaRaw];
    }
}

- (void)documentDidFinishPrinting:(BOOL)successful
{
    // Do nothing
}

#pragma mark - Printer Selection

- (void)loadPrinterList
{
    [self performSegueWithIdentifier:@"PrintSettings-PrinterList" sender:self];
}


#pragma mark - NotificationCenter

- (void)keyboardWillShow:(NSNotification *)notification
{
    if (self.printerIndex == nil)
    {
        self.tableViewHeight.constant = ROW_HEIGHT_SINGLE;
        [UIView animateWithDuration:0.1 animations:^{
            [self.view layoutIfNeeded];
        }];
    }
}

- (void)keyboardWillHide:(NSNotification *)notification
{
    if (self.printerIndex == nil)
    {
        self.tableViewHeight.constant = ROW_HEIGHT_SINGLE + ROW_HEIGHT_DOUBLE;
        [UIView animateWithDuration:0.1 animations:^{
            [self.view layoutIfNeeded];
        }];
    }
}

@end

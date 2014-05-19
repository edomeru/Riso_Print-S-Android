//
//  PrintSettingsViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintSettingsViewController.h"
#import "PrintSettingsTableViewController.h"
#import "PDFFileManager.h"
#import "PrinterManager.h"
#import "PrintDocument.h"
#import "Printer.h"
#import "PreviewSetting.h"
#import "PrintSettingsHelper.h"
#import "UIView+Localization.h"
#import "PrintSettingsPrinterItemCell.h"
#import "PrintSettingsPrinterHeaderCell.h"
#import "DirectPrintManager.h"

#define SEGUE_TO_PRINTSETTINGS_TABLE @"PrintSettings-PrintSettingsTable"

#define PRINTER_HEADER_CELL @"PrinterHeaderCell"
#define PRINTER_ITEM_CELL @"PrinterItemCell"
#define PRINTER_ITEM_DEFAULT_CELL @"PrinterItemDefaultCell"

#define ROW_HEIGHT_SINGLE 44
#define ROW_HEIGHT_DOUBLE 55

@interface PrintSettingsViewController () <DirectPrintManagerDelegate>

@property (weak, nonatomic) IBOutlet UILabel *printSettingsScreenTitle;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *spaceTableViewToContainer;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *tableViewHeight;

@property (assign, nonatomic) BOOL isDefaultSettingsMode;
@property (weak, nonatomic) Printer* printer;

- (void)initialize;
- (void)executePrint;
- (void)loadPrinterList;

@end

@implementation PrintSettingsViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        [self initialize];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        [self initialize];
    }
    return self;
}

- (void)initialize
{
    self.slideDirection = SlideRight;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if(self.printerIndex != nil)
    {
        // launched from Printers
        
        self.printSettingsScreenTitle.localizationId = @"IDS_LBL_DEFAULT_PRINT_SETTINGS";
        self.isDefaultSettingsMode = YES;
        
        PrinterManager* pm = [PrinterManager sharedPrinterManager];
        self.printer = [pm getPrinterAtIndex:[self.printerIndex unsignedIntegerValue]];
    }
    else
    {
        // launched from Print Preview
        
        self.isDefaultSettingsMode = NO;
        
        PDFFileManager* pdfm = [PDFFileManager sharedManager];
        PrintDocument* doc = pdfm.printDocument;
        if (doc.printer.managedObjectContext != nil)
            self.printer = doc.printer;
        else
            self.printer = nil;
    }
    
    if ((UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) && (self.printerIndex == nil))
    {
        self.isFixedSize = NO;
    }
    else
    {
        self.isFixedSize = YES;

    }
}

- (void)viewWillAppear:(BOOL)animated
{
    if (self.isDefaultSettingsMode)
    {
        self.tableViewHeight.constant = ROW_HEIGHT_DOUBLE;
    }
    else
    {
        self.tableViewHeight.constant = ROW_HEIGHT_SINGLE + ROW_HEIGHT_DOUBLE;
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if([segue.identifier isEqualToString:SEGUE_TO_PRINTSETTINGS_TABLE] == YES)
    {
        PrintSettingsTableViewController *viewController = (PrintSettingsTableViewController *)[segue.destinationViewController topViewController];
        viewController.printerIndex = self.printerIndex;
    }
}

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
            [self executePrint];
        }
        else
        {
            [self loadPrinterList];
        }
    }
}

#pragma mark - Printing

- (void)executePrint
{
//    // Check if printer is selected
//    if (self.printer == nil)
//    {
//        [AlertHelper displayResult:kAlertResultErrDefault withTitle:kAlertTitleDefault withDetails:nil];
//        return;
//    }
//    
//    if (![NetworkManager isConnectedToLocalWifi])
//    {
//        [AlertHelper displayResult:kAlertResultErrNoNetwork withTitle:kAlertTitleDefault withDetails:nil];
//        return;
//    }
//    
//    DirectPrintManager *manager = [[DirectPrintManager alloc] init];
//    if ([self.printer.port integerValue] == 0)
//    {
//        [manager printDocumentViaLPR];
//    }
//    else
//    {
//        [manager printDocumentViaRaw];
//    }
//    manager.delegate = self;
}

- (void)documentDidFinishPrinting:(BOOL)successful
{
//    if (successful)
//    {
//        [self performSegueTo:[PrintJobHistoryViewController class]];
//    }
}

#pragma mark - Printer Selection

- (void)loadPrinterList
{
//    [self performSegueWithIdentifier:@"PrintSettings-PrinterList" sender:self];
}

- (IBAction)unwindToPrintSettings:(UIStoryboardSegue*)sender
{
    
}

@end

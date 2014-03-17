//
//  PrintersScreenController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintersIphoneViewController.h"
#import "AddPrinterViewController.h"
#import "PrinterSearchViewController.h"
#import "Printer.h"
#import "DefaultPrinter.h"
#import "PrinterManager.h"
#import "PrinterCell.h"
#import "AlertUtils.h"

#define SEGUE_TO_ADD_PRINTER    @"PrintersIphone-AddPrinter"
#define SEGUE_TO_PRINTER_SEARCH @"PrintersIphone-PrinterSearch"

#define PRINTERCELL             @"PrinterCell"

@interface PrintersIphoneViewController ()
/**
 Action when the PrinterCell is tapped to select as Default Printer
 */
- (IBAction)onTapPrinterCell:(id)sender;

/**
 Action when the PrinterCell Disclosure is tapped to segue to the PrinterInfo screen
 */
- (IBAction)onTapPrinterCellDisclosure:(id)sender;

@end

@implementation PrintersIphoneViewController

#pragma mark - Lifecycle

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Header

#pragma mark - TableView

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.printerManager.listSavedPrinters count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString* cellIdentifier = PRINTERCELL;
    PrinterCell* cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier
                                                        forIndexPath:indexPath];
    
    Printer* printer = [self.printerManager getPrinterAtIndex:indexPath.row];
    if ([self.printerManager isDefaultPrinter:printer])
    {
        self.defaultPrinterIndexPath = indexPath;
        [cell setAsDefaultPrinterCell:YES];
    }
    
    cell.printerName.text = printer.name;
    cell.printerStatus.statusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:printer.ip_address];
    cell.printerStatus.statusHelper.delegate = cell.printerStatus;

    [cell.printerStatus setStatus:[printer.onlineStatus boolValue]]; //initial status
    [cell.printerStatus.statusHelper startPrinterStatusPolling];
    
    return cell;
}

-(UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return UITableViewCellEditingStyleDelete;
}

-(BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    return YES;
}

-(void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
        if(editingStyle == UITableViewCellEditingStyleDelete)
        {
            if ([self.printerManager deletePrinterAtIndex:indexPath.row])
            {
                //check if reference to default printer was also deleted
                if (![self.printerManager hasDefaultPrinter])
                    self.defaultPrinterIndexPath = nil;

                //set the view of the cell to stop polling for printer status
                PrinterCell *cell = (PrinterCell *)[tableView cellForRowAtIndexPath:indexPath];
                [cell.printerStatus.statusHelper stopPrinterStatusPolling];
                
                //set view to non default printer cell style
                [cell setAsDefaultPrinterCell:NO];
                
                //remove cell from view
                [self.tableView deleteRowsAtIndexPaths:@[indexPath]
                                      withRowAnimation:UITableViewRowAnimationAutomatic];
            }
            else
            {
                [AlertUtils displayResult:ERR_DEFAULT withTitle:ALERT_PRINTER withDetails:nil];
            }
        }
}

#pragma mark - Gesture Recognizer Handler
- (IBAction)onTapPrinterCell:(id)sender
{
    NSIndexPath *selectedIndexPath = [self.tableView indexPathForRowAtPoint:[sender locationInView:self.tableView]];
    
    [self setDefaultPrinter:selectedIndexPath];
    
    if(self.defaultPrinterIndexPath != nil)
    {
        PrinterCell *previousDefaultCell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.defaultPrinterIndexPath];
        [previousDefaultCell setAsDefaultPrinterCell:NO];
    }
    
    //set the formatting of the selected cell to the default printer cell
    self.defaultPrinterIndexPath = selectedIndexPath;
    PrinterCell *selectedDefaultCell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:selectedIndexPath];
    [selectedDefaultCell setAsDefaultPrinterCell:YES];
    
}

- (IBAction)onTapPrinterCellDisclosure:(id)sender
{
    NSLog(@"[INFO][Printers] PrinterCell Tapped");
    //TODO Add segue to printer info
}

#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:SEGUE_TO_ADD_PRINTER])
    {
        AddPrinterViewController* destController = [segue destinationViewController];
        
        //give the child screen a reference to the printer manager
        destController.printerManager = self.printerManager;
    }
    
    if ([segue.identifier isEqualToString:SEGUE_TO_PRINTER_SEARCH])
    {
        PrinterSearchViewController* destController = [segue destinationViewController];
        
        //give the child screen a reference to the printer manager
        destController.printerManager = self.printerManager;
    }
}

- (IBAction)unwindToPrinters:(UIStoryboardSegue*)unwindSegue
{
    UIViewController* sourceViewController = [unwindSegue sourceViewController];
    if ([sourceViewController isKindOfClass:[AddPrinterViewController class]])
    {
        AddPrinterViewController* adderScreen = (AddPrinterViewController*)sourceViewController;
        if (adderScreen.hasAddedPrinters)
            [self.tableView reloadData];
    }
    if ([sourceViewController isKindOfClass:[PrinterSearchViewController class]])
    {
        PrinterSearchViewController* adderScreen = (PrinterSearchViewController*)sourceViewController;
        if (adderScreen.hasAddedPrinters)
            [self.tableView reloadData];
    }
}

- (IBAction)unwindFromSlidingDrawer:(UIStoryboardSegue *)segue
{
}

@end

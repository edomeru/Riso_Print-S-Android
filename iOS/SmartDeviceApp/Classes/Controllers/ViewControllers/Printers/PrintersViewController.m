//
//  PrintersViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/6/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintersViewController.h"
#import "HomeViewController.h"
#import "UIViewController+Segue.h"
#import "AddPrinterViewController.h"
#import "PrinterSearchViewController.h"
#import "PrinterManager.h"

@interface PrintersViewController ()

@end

@implementation PrintersViewController

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
    
    self.printerManager = [PrinterManager sharedPrinterManager];
    self.toDeleteIndexPath = nil;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark -

- (BOOL) setDefaultPrinter: (NSIndexPath *) indexPath
{
    //get selected printer from list
    Printer* selectedPrinter = [self.printerManager getPrinterAtIndex:indexPath.row];
    
    //set as default printer
    return [self.printerManager registerDefaultPrinter:selectedPrinter];
}

#pragma mark - IBActions

- (IBAction)mainMenuAction:(id)sender
{
    [self.mainMenuButton setEnabled:NO];
    [self performSegueTo:[HomeViewController class]];
}

- (IBAction)addPrinterAction:(id)sender
{
    [self.addPrinterButton setEnabled:NO];
    [self performSegueTo:[AddPrinterViewController class]];
}

- (IBAction)printerSearchAction:(id)sender
{
    [self.printerSearchButton setEnabled:NO];
    [self performSegueTo:[PrinterSearchViewController class]];
}

#pragma mark - Segue

- (IBAction)unwindToPrinters:(UIStoryboardSegue *)sender
{
    UIViewController* sourceViewController = [sender sourceViewController];
    
    if ([sourceViewController isKindOfClass:[HomeViewController class]])
    {
        [self.mainMenuButton setEnabled:YES];
    }
    else if ([sourceViewController isKindOfClass:[AddPrinterViewController class]])
    {
        [self.addPrinterButton setEnabled:YES];
        
        AddPrinterViewController* adderScreen = (AddPrinterViewController*)sourceViewController;
        if (adderScreen.hasAddedPrinters)
            [self reloadData];
    }
    else if ([sourceViewController isKindOfClass:[PrinterSearchViewController class]])
    {
        [self.printerSearchButton setEnabled:YES];
        
        PrinterSearchViewController* adderScreen = (PrinterSearchViewController*)sourceViewController;
        if (adderScreen.hasAddedPrinters)
            [self reloadData];
    }
}

- (void)reloadData
{
    NSLog(@"[INFO][Printers] reloading data");
    //should be implemented depending on display
}

@end

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
    // setup the PrinterManager
    self.toDeleteIndexPath = nil;
    NSLog(@"[INFO][Printers] setup");
    self.printerManager = [[PrinterManager alloc] init];
    [self.printerManager getListOfSavedPrinters];
    [self.printerManager getDefaultPrinter];
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
    Printer *selectedPrinter = [self.printerManager.listSavedPrinters objectAtIndex:indexPath.row];
    
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

- (IBAction)unwindToPrinters:(UIStoryboardSegue *)sender
{
}

@end

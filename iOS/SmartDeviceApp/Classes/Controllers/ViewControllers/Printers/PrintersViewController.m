//
//  PrintersViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintersViewController.h"
#import "AddPrinterViewController.h"
#import "PrinterSearchViewController.h"
#import "PrinterInfoViewController.h"
#import "PrintSettingsViewController.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"

@interface PrintersViewController ()

#pragma mark - Data Properties

#pragma mark - UI Properties

@property (weak, nonatomic) IBOutlet UIButton* mainMenuButton;
@property (weak, nonatomic) IBOutlet UIButton* addPrinterButton;
@property (weak, nonatomic) IBOutlet UIButton* printerSearchButton;

#pragma mark - Instance Methods

@end

@implementation PrintersViewController

#pragma mark - Lifecycle

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
#if 0
    while (self.printerManager.countSavedPrinters != 10)
    {
        PrinterDetails* pd = [[PrinterDetails alloc] init];
        pd.name = @"";
        pd.ip = [NSString stringWithFormat:@"192.168.0.%d", 10-self.printerManager.countSavedPrinters];
        [self.printerManager registerPrinter:pd];
    }
#endif
    
    self.toDeleteIndexPath = nil;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
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
    else if([sender.sourceViewController isKindOfClass:[PrintSettingsViewController class]])
    {
        UIViewController* controller =  [[self childViewControllers] lastObject];
        if([controller isKindOfClass:[PrinterInfoViewController class]])
        {
            [((PrinterInfoViewController *)controller).printSettingsButton setSelected:NO];
        }
        else
        {
            [self reloadData];
        }
    }
}

#pragma mark - Reload

- (void)reloadData
{
#if DEBUG_LOG_PRINTERS_SCREEN
    NSLog(@"[INFO][Printers] reloading data");
#endif
    //should be implemented depending on display
}

@end

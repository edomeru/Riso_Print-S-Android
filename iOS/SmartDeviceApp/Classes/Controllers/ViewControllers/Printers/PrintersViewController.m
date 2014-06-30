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
#import "AlertHelper.h"

@interface PrintersViewController ()

#pragma mark - Data Properties

#pragma mark - UI Properties

/**
 * Reference to the main menu button on the header.
 */
@property (weak, nonatomic) IBOutlet UIButton* mainMenuButton;

/**
 * Reference to the add printer button on the header.
 */
@property (weak, nonatomic) IBOutlet UIButton* addPrinterButton;

/**
 * Reference to the printer search button on the header.
 */
@property (weak, nonatomic) IBOutlet UIButton* printerSearchButton;

#pragma mark - Instance Methods

/** 
 * Unwind segue back to the "Printers" screen.
 * Called when transitioning back to the "Printers" screen from the
 * "Add Printer" screen, the "Printer Search" screen, or the Main
 * Menu panel.
 * 
 * @param sender the segue object
 */
- (IBAction)unwindToPrinters:(UIStoryboardSegue *)sender;

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
    self.emptyLabel.hidden = (self.printerManager.countSavedPrinters == 0 ? NO : YES);
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
    if ([self.printerManager isAtMaximumPrinters])
    {
        [AlertHelper displayResult:kAlertResultErrMaxPrinters
                         withTitle:kAlertTitlePrinters
                       withDetails:nil
                withDismissHandler:^(CXAlertView *alertView) {
                    [self.addPrinterButton setEnabled:YES];
                }];
    }
    else
    {

        [self performSegueTo:[AddPrinterViewController class]];
    }
}

- (IBAction)printerSearchAction:(id)sender
{
    [self.printerSearchButton setEnabled:NO];
    if ([self.printerManager isAtMaximumPrinters])
    {
        [AlertHelper displayResult:kAlertResultErrMaxPrinters
                         withTitle:kAlertTitlePrinters
                       withDetails:nil
                withDismissHandler:^(CXAlertView *alertView) {
                    [self.printerSearchButton setEnabled:YES];
                }];
    }
    else
    {

        [self performSegueTo:[PrinterSearchViewController class]];
    }
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
        {
            [self reloadPrinters];
        }
    }
    else if ([sourceViewController isKindOfClass:[PrinterSearchViewController class]])
    {
        [self.printerSearchButton setEnabled:YES];
        
        PrinterSearchViewController* adderScreen = (PrinterSearchViewController*)sourceViewController;
        if (adderScreen.hasAddedPrinters)
        {
            [self reloadPrinters];
        }
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
            [self reloadPrinters];
        }
    }
}

#pragma mark - Reload

- (void)reloadPrinters
{
#if DEBUG_LOG_PRINTERS_SCREEN
    NSLog(@"[INFO][Printers] reloading data");
#endif
    //should be implemented depending on display
    
    self.emptyLabel.hidden = (self.printerManager.countSavedPrinters == 0 ? NO : YES);
}

@end

//
//  PrinterInfoViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrinterInfoViewController.h"
#import "Printer.h"
#import "PrinterManager.h"
#import "UIViewController+Segue.h"
#import "PrintSettingsViewController.h"
#import "UIView+Localization.h"
#import "AlertHelper.h"

@interface PrinterInfoViewController ()

/**
 * Reference to the label displaying the printer's name.
 */
@property (weak, nonatomic) IBOutlet UILabel *printerName;

/**
 * Reference to the label displaying the printer's IP address.
 */
@property (weak, nonatomic) IBOutlet UILabel *ipAddress;

/**
 * Reference to the "Set as Default Printer" switch.
 * If this is the default printer, the {@link defaultSetIcon} is displayed instead.
 */
@property (weak, nonatomic) IBOutlet UISwitch *defaultPrinterSwitch;

/**
 * Reference to the icon indicating that this printer is the default printer.
 * If this is not the default printer, the {@link defaultPrinterSwitch} is displayed instead.
 */
@property (weak, nonatomic) IBOutlet UIImageView *defaultSetIcon;

/**
 * Reference to the port selection switch.
 */
@property (weak, nonatomic) IBOutlet UISegmentedControl *portSelection;

/**
 * Stores the previous state of {@link defaultPrinterSwitch} (on/off).
 */
@property (assign, nonatomic) BOOL switchPreviousState;

/**
 * Reference to the Printer object being displayed.
 */
@property (weak, nonatomic) Printer* printer;

/**
 * Reference to the PrinterManager singleton.
 */
@property (weak, nonatomic) PrinterManager *printerManager;

/**
 * Responds to the "Set as Default Printer" switch action.
 * If the switch is set to on, updates the Printer object to be set
 * as the default printer (using PrinterManager), then
 * updates the display ({@link hideDefaultSwitch}).
 *
 * @param sender the switch object
 */
- (IBAction)defaultPrinterSwitchAction:(id)sender;

/**
 * Responds to the default print settings button press.
 * Displays the "Default Print Settings" screen.
 *
 * @param sender the button object
 */
- (IBAction)printSettingsAction:(id)sender;

/**
 * Responds to the printer port selection action.
 * Updates the display then updates the printer object (using PrinterManager).
 *
 * @param sender the port selection object
 */
- (IBAction)selectPortAction:(id)sender;

/**
 * Toggles the display of the {@link defaultSetIcon} and the {@link defaultPrinterSwitch} views.
 *
 * @param hidden if YES, hides {@link defaultPrinterSwitch} then shows {@link defaultSetIcon}\n
 *               if NO, hides {@link defaultSetIcon} then shows {@link defaultPrinterSwitch}
 */
- (void)hideDefaultSwitch:(BOOL)hidden;

@end

@implementation PrinterInfoViewController

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
    
    [self.portSelection setTitle:NSLocalizedString(IDS_LBL_PORT_LPR, @"LPR") forSegmentAtIndex:0];
    [self.portSelection setTitle:NSLocalizedString(IDS_LBL_PORT_RAW, @"RAW") forSegmentAtIndex:1];
    
    self.printerManager = [PrinterManager sharedPrinterManager];
    
    self.printer = [self.printerManager getPrinterAtIndex:self.indexPath.row];

    if(self.printer != nil)
    {
        if(self.printer.name == nil || [self.printer.name isEqualToString:@""] == YES)
        {
            self.printerName.text = NSLocalizedString(IDS_LBL_NO_NAME, @"No name");
        }
        else
        {
            self.printerName.text = self.printer.name;
        }
        
        self.ipAddress.text = self.printer.ip_address;
        
        [self.portSelection setSelectedSegmentIndex:[self.printer.port integerValue]];
        [self.portSelection setEnabled:[self.printer.enabled_raw boolValue] forSegmentAtIndex:1];
        
        if(self.isDefaultPrinter == YES)
        {
            self.defaultPrinterSwitch.on = YES;
            [self hideDefaultSwitch:YES];
        }
        else
        {
            self.defaultPrinterSwitch.on = NO;
            [self hideDefaultSwitch:NO];
        }
    }
    
    self.switchPreviousState = NO;
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)hideDefaultSwitch:(BOOL)hidden
{
    self.defaultPrinterSwitch.hidden = hidden;
    self.defaultSetIcon.hidden = !hidden;
}

#pragma mark - IBActions

- (IBAction)defaultPrinterSwitchAction:(id)sender
{
    //if setting of printer as default failed, show alert message and turn off switch.
    if(self.switchPreviousState != [self.defaultPrinterSwitch isOn] && [self.defaultPrinterSwitch isOn])
    {
        self.switchPreviousState = YES;
        if([self.printerManager registerDefaultPrinter:self.printer])
        {
            self.isDefaultPrinter = self.defaultPrinterSwitch.on;
            if (self.isDefaultPrinter)
            {
                [self hideDefaultSwitch:YES];
            }
        }
        else
        {
            [AlertHelper displayResult:kAlertResultErrDB
                             withTitle:kAlertTitlePrinters
                           withDetails:nil
                    withDismissHandler:^(CXAlertView *alertView) {
                        [self.defaultPrinterSwitch setOn:NO animated:YES];
                        self.switchPreviousState = NO;
                    }];
        }

    }
    //switch is automatically turned off when a new default printer is selected
}

- (IBAction)onBack:(UIButton *)sender
{
    [self unwindFromOverTo:[self.parentViewController class]];
}

- (IBAction)printSettingsAction:(id)sender
{
    [self.delegate segueToPrintSettings];
    [self.printSettingsButton setSelected:YES];
}

- (IBAction)selectPortAction:(id)sender
{
    self.printer.port = [NSNumber numberWithInteger: self.portSelection.selectedSegmentIndex];
    [self.printerManager savePrinterChanges];
}

@end

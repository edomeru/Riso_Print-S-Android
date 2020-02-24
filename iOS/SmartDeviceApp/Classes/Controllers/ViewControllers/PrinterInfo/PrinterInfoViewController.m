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
 * Reference to the content view.
*/
@property (weak, nonatomic) IBOutlet UIView *contentView;

/**
 * Reference to the label displaying the printer's name.
 */
@property (weak, nonatomic) IBOutlet UILabel *printerName;

/**
 * Reference to the label displaying the printer's IP address.
 */
@property (weak, nonatomic) IBOutlet UILabel *ipAddress;

/**
 * Reference to the port selection switch.
 */
@property (weak, nonatomic) IBOutlet UISegmentedControl *portSelection;

/**
 * Reference to the default printer switch.
 */
@property (weak, nonatomic) IBOutlet UISegmentedControl *defaultPrinterSelection;

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
 * If the selection is set to Yes, updates the Printer object to be set
 * as the default printer (using PrinterManager), then
 * updates the display ({@link hideDefaultSwitch}).
 *
 * @param sender the switch object
 */
- (IBAction)defaultPrinterSelectionAction:(id)sender;

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

    if (@available(iOS 13.0, *)) {
        self.contentView.backgroundColor = [UIColor colorNamed:@"color_gray2_gray5"];
    }

    [self.portSelection setTitle:NSLocalizedString(IDS_LBL_PORT_LPR, @"LPR") forSegmentAtIndex:0];
    [self.portSelection setTitle:NSLocalizedString(IDS_LBL_PORT_RAW, @"RAW") forSegmentAtIndex:1];
    
    [self.defaultPrinterSelection setTitle:NSLocalizedString(IDS_LBL_YES, @"YES") forSegmentAtIndex:0];
    [self.defaultPrinterSelection setTitle:NSLocalizedString(IDS_LBL_NO, @"NO") forSegmentAtIndex:1];
    
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
        if (![self.printer.enabled_raw boolValue])
        {
            self.portSelection.userInteractionEnabled = NO;
        }
        else
        {
            self.portSelection.userInteractionEnabled = YES;
        }
        
        if(self.isDefaultPrinter == YES)
        {
            [self.defaultPrinterSelection setEnabled:NO forSegmentAtIndex:1];
            self.defaultPrinterSelection.userInteractionEnabled = NO;
        }
        else
        {
            [self.defaultPrinterSelection setSelectedSegmentIndex:1];
            self.defaultPrinterSelection.userInteractionEnabled = YES;
        }
    }
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

#pragma mark - IBActions

- (IBAction)defaultPrinterSelectionAction:(id)sender
{
    if(self.defaultPrinterSelection.selectedSegmentIndex == 0) //yes
    {
        if([self.printerManager registerDefaultPrinter:self.printer])
        {
            self.isDefaultPrinter = true;
            [self.defaultPrinterSelection setEnabled:NO forSegmentAtIndex:1];
            self.defaultPrinterSelection.userInteractionEnabled = NO;
        }
        else
        {
            [AlertHelper displayResult:kAlertResultErrDB
                             withTitle:kAlertTitlePrinters
                           withDetails:nil
                    withDismissHandler:^(CXAlertView *alertView) {
                        [self.defaultPrinterSelection setSelectedSegmentIndex:1];
                    }];
        }
    }
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

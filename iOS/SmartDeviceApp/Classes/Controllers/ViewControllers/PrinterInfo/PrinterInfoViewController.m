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
@property (weak, nonatomic) IBOutlet UILabel *printerName;
@property (weak, nonatomic) IBOutlet UILabel *ipAddress;
@property (weak, nonatomic) IBOutlet UISwitch *defaultPrinterSwitch;
@property (weak, nonatomic) IBOutlet UIImageView *defaultSetIcon;
@property (weak, nonatomic) IBOutlet UISegmentedControl *portSelection;

@property (weak, nonatomic) Printer* printer;
@property (weak, nonatomic) PrinterManager *printerManager;

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
                    self.defaultPrinterSwitch.on = NO;
                }];
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

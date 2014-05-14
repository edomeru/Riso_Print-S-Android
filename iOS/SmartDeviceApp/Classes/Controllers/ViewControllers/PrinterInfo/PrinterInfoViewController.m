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

@interface PrinterInfoViewController ()
@property (weak, nonatomic) IBOutlet UILabel *printerName;
@property (weak, nonatomic) IBOutlet UILabel *ipAddress;
@property (weak, nonatomic) IBOutlet UISwitch *defaultPrinterSwitch;
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
            self.printerName.text = NSLocalizedString(@"IDS_LBL_NO_NAME", @"No name");
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
        }
    }
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

-(void) viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void) updateDefaultPrinter: (BOOL) isDefaultOn
{
    if(isDefaultOn == NO)
    {
        if([self.printerManager isDefaultPrinter:self.printer] == YES)
        {
            [self.printerManager deleteDefaultPrinter];
        }
    }
    else
    {
        [self.printerManager registerDefaultPrinter:self.printer];
    }
}

#pragma mark - IBActions
- (IBAction)defautltPrinterSwitchAction:(id)sender
{
    if(self.isDefaultPrinter == self.defaultPrinterSwitch.on)
    {
        return;
    }
    
    self.isDefaultPrinter = self.defaultPrinterSwitch.on;

    [self updateDefaultPrinter:self.isDefaultPrinter];
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

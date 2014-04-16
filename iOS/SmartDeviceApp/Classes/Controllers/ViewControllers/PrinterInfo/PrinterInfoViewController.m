//
//  PrinterInfoScreenController.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/6/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterInfoViewController.h"
#import "Printer.h"
#import "PrinterManager.h"
#import "UIViewController+Segue.h"

@interface PrinterInfoViewController ()
@property (weak, nonatomic) IBOutlet UILabel *printerName;
@property (weak, nonatomic) IBOutlet UILabel *ipAddress;
@property (weak, nonatomic) IBOutlet UILabel *port;
@property (weak, nonatomic) IBOutlet UILabel *printerStatus;
@property (weak, nonatomic) IBOutlet UISwitch *defaultPrinterSwitch;

@property (weak, nonatomic) Printer* printer;
@property (weak, nonatomic) PrinterManager *printerManager;
@property (strong, nonatomic) PrinterStatusHelper *statusHelper;
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
    self.printerManager = [PrinterManager sharedPrinterManager];
    
    self.printer = [self.printerManager getPrinterAtIndex:self.indexPath.row];

    if(self.printer != nil)
    {
        self.printerName.text = self.printer.name;
        self.ipAddress.text = self.printer.ip_address;
        self.port.text = [self.printer.port stringValue];
        [self setStatus:self.onlineStatus];
        if(self.isDefaultPrinter == YES)
        {
            self.defaultPrinterSwitch.on = YES;
        }
    }
    
    self.statusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:self.printer.ip_address];
    self.statusHelper.delegate = self;
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self.statusHelper startPrinterStatusPolling];
}

-(void) viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    [self.statusHelper stopPrinterStatusPolling];
    self.statusHelper.delegate = nil;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void) setStatus: (BOOL) isOnline
{
    self.onlineStatus = isOnline;
    if(isOnline)
    {
        self.printerStatus.text = @"Online";    //TODO: should use localized strings
    }
    else
    {
        self.printerStatus.text = @"Offline";   //TODO: should use localized strings
    }
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


#pragma mark - PrinterStatusHelper method
- (void) statusDidChange: (BOOL) isOnline
{
    if(self.onlineStatus == isOnline)
    {
        return;
    }
    [self setStatus:isOnline];
}
@end

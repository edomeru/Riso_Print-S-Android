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

#define ONLINE_STATUS @"Online";
#define OFFLINE_STATUS @"Offline";

@interface PrinterInfoViewController ()
@property (weak, nonatomic) IBOutlet UILabel *printerName;
@property (weak, nonatomic) IBOutlet UILabel *ipAddress;
@property (weak, nonatomic) IBOutlet UILabel *port;
@property (weak, nonatomic) IBOutlet UILabel *onlineStatus;
@property (weak, nonatomic) IBOutlet UISwitch *defaultPrinterSwitch;

@property (weak, nonatomic) Printer* printer;
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
    if(self.delegate == nil)
    {
        NSLog(@"delegate not provided!");
    }
    
    self.printer = [self.delegate getPrinterAtIndexPath:self.indexPath];
    if(self.printer != nil)
    {
        self.printerName.text = self.printer.name;
        self.ipAddress.text = self.printer.ip_address;
        self.port.text = [self.printer.port stringValue];
        [self setStatus:self.printer.onlineStatus.boolValue];
        if(self.isDefaultPrinter == YES)
        {
            self.defaultPrinterSwitch.on = YES;
        }
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void) setStatus: (BOOL) isOnline
{
    self.printer.onlineStatus = [NSNumber numberWithBool:isOnline];
    if(isOnline)
    {
        self.onlineStatus.text = ONLINE_STATUS;
    }
    else
    {
        self.onlineStatus.text = OFFLINE_STATUS;
    }
}

- (IBAction)defautltPrinterSwitchAction:(id)sender
{
    if(self.isDefaultPrinter == self.defaultPrinterSwitch.on)
    {
        return;
    }
    
    self.isDefaultPrinter = self.defaultPrinterSwitch.on;

    [self.delegate updateDefaultPrinter:self.isDefaultPrinter  atIndexPath: self.indexPath];
}

- (IBAction)onBack:(UIButton *)sender
{
    [self unwindFromOverTo:[self.parentViewController class]];
}

- (void) updateStatus: (BOOL) isOnline
{
    if([self.printer.onlineStatus boolValue] == isOnline)
    {
        return;
    }
    [self setStatus:isOnline];
}

@end

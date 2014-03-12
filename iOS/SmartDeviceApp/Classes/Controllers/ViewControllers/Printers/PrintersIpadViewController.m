//
//  PrintersIpadViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/7/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintersIpadViewController.h"
#import "PrinterManager.h"

@interface PrintersIpadViewController ()

@property (strong, nonatomic) PrinterManager* printerManager;

@end

@implementation PrintersIpadViewController

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

    self.printerManager = [[PrinterManager alloc] init];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark -
#pragma mark IBActions
- (IBAction)addPrinterAction:(id)sender
{
    // TODO: Change button state
    [super addPrinterAction:sender];
}

- (IBAction)printerSearchAction:(id)sender
{
    // TODO: Change button state
    [super printerSearchAction:sender];
}

@end

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
	// Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark -
#pragma mark IBActions
- (IBAction)mainMenuAction:(id)sender
{
    [self performSegueTo:[HomeViewController class]];
}

- (IBAction)addPrinterAction:(id)sender
{
    [self performSegueTo:[AddPrinterViewController class]];
}

- (IBAction)printerSearchAction:(id)sender
{
    [self performSegueTo:[PrinterSearchViewController class]];
}

- (IBAction)unwindToPrinters:(UIStoryboardSegue *)sender
{
}

@end

//
//  PrintSettingsViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintSettingsViewController.h"
#import "PrintSettingsPrinterViewController.h"
#import "PrintSettingsTableViewController.h"

#import "PrintSettingsHelper.h"
#import "UIView+Localization.h"


#define SEGUE_TO_PRINTSETTINGS_PRINTER_TABLE @"PrintSettings-PrintSettingsPrinter"

@interface PrintSettingsViewController () 

@property (weak, nonatomic) IBOutlet UILabel *printSettingsScreenTitle;


- (void)initialize;
@end

@implementation PrintSettingsViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        [self initialize];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        [self initialize];
    }
    return self;
}

- (void)initialize
{
    self.slideDirection = SlideRight;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if(self.printerIndex != nil)
    {
        // launched from Printers
        self.printSettingsScreenTitle.localizationId = @"IDS_LBL_DEFAULT_PRINT_SETTINGS";
    }
    
    if ((UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) && (self.printerIndex == nil))
    {
        self.isFixedSize = NO;
    }
    else
    {
        self.isFixedSize = YES;

    }
}

- (void)viewWillAppear:(BOOL)animated
{
    /*if (self.isDefaultSettingsMode)
    {
        self.tableViewHeight.constant = ROW_HEIGHT_DOUBLE;
    }
    else
    {
        self.tableViewHeight.constant = ROW_HEIGHT_SINGLE + ROW_HEIGHT_DOUBLE;
    }*/
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if([segue.identifier isEqualToString:SEGUE_TO_PRINTSETTINGS_PRINTER_TABLE] == YES)
    {
        PrintSettingsPrinterViewController *viewController = (PrintSettingsPrinterViewController *)[segue.destinationViewController topViewController];
        viewController.printerIndex = self.printerIndex;
    }
}

- (IBAction)unwindToPrintSettings:(UIStoryboardSegue*)sender
{
    
}

@end

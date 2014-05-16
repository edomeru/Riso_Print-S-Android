//
//  PrintSettingsViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintSettingsViewController.h"
#import "PrintSettingsTableViewController.h"
#import "PDFFileManager.h"
#import "PrinterManager.h"
#import "PrintDocument.h"
#import "Printer.h"
#import "PreviewSetting.h"
#import "PrintSettingsHelper.h"
#import "UIView+Localization.h"

#define SEGUE_TO_PRINTSETTINGS_TABLE @"PrintSettings-PrintSettingsTable"

@interface PrintSettingsViewController ()

- (void)initialize;
@property (weak, nonatomic) IBOutlet UILabel *printSettingsScreenTitle;

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
	// Do any additional setup after loading the view.
    
    if ((UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) && (self.printerIndex == nil))
    {
        self.isFixedSize = NO;
    }
    else
    {
        self.isFixedSize = YES;
    }
    
    if(self.printerIndex != nil)
    {
       self.printSettingsScreenTitle.localizationId = @"IDS_LBL_DEFAULT_PRINT_SETTINGS";
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if([segue.identifier isEqualToString:SEGUE_TO_PRINTSETTINGS_TABLE] == YES)
    {
        PrintSettingsTableViewController *viewController = (PrintSettingsTableViewController *)[segue.destinationViewController topViewController];
        viewController.printerIndex = self.printerIndex;
    }
}
@end

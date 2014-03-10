//
//  PrinterSearchScreenController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterSearchScreenController.h"
#import "Printer.h"

#define SEARCHRESULTCELL    @"SearchResultCell"

#define UNWIND_TO_PRINTERS  @"UnwindRight"

@interface PrinterSearchScreenController ()

@property (strong, nonatomic) NSMutableArray* listSearchResults;

@end

@implementation PrinterSearchScreenController

#pragma mark - Lifecycle

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
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
    {
        self.isFixedSize = NO;
    }
    else
    {
        self.isFixedSize = YES;
    }
    self.slideDirection = SlideRight;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // setup properties
    //TODO: initalize a dictionary for the search results
    //TODO: <Printer> : <isNew>
    self.listSearchResults = [NSMutableArray arrayWithArray:self.listSavedPrinters];
    
    //TODO: perform search
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Header

- (IBAction)onBack:(UIBarButtonItem *)sender
{
    [self performSegueWithIdentifier:UNWIND_TO_PRINTERS sender:self];
}

#pragma mark - TableView

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.listSearchResults count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:SEARCHRESULTCELL
                                                            forIndexPath:indexPath];
    
    Printer* printer = [self.listSearchResults objectAtIndex:indexPath.row];
    //TODO: check if printer is on list of saved printers (isNew value)
    //TODO: add checkmark for already existing printers
    //TODO: add + button for new printers
    
    cell.textLabel.text = printer.name;
    cell.accessoryType = UITableViewCellAccessoryCheckmark;
    //cell.accessoryType = UITableViewCellAccessoryNone;
    
    return cell;
}

@end

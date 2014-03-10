//
//  PrintersScreenController.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PrintersScreenController : UIViewController <UITableViewDataSource, UITableViewDelegate>

/**
 Contains the Printer objects from DB.
 **/
@property (nonatomic, strong) NSMutableArray* listSavedPrinters;

/**
 Handler for the Add Printer button in the Header.
 **/
- (IBAction)onAdd:(UIButton *)sender;

/**
 Handler for the Printer Search button in the Header.
 **/
- (IBAction)onSearch:(UIButton *)sender;

/**
 Handler for the Main Menu button in the Header.
 **/
- (IBAction)onMenu:(UIButton *)sender;

@property (weak, nonatomic) IBOutlet UITableView *tableView;

@end

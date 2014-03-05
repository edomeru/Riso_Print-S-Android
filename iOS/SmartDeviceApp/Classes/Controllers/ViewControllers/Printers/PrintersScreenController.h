//
//  PrintersScreenController.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PrintersScreenController : UITableViewController

/**
 Contains the Printer objects from DB.
 **/
@property (nonatomic, strong) NSMutableArray* listSavedPrinters;

@end

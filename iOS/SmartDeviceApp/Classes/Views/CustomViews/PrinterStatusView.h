//
//  PrinterStatusView.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/5/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrinterStatusHelper.h"

@interface PrinterStatusView : UIImageView <PrinterStatusHelperDelegate>

@property (nonatomic) BOOL onlineStatus;
@property (strong, nonatomic) PrinterStatusHelper *statusHelper;

- (void) setStatus: (BOOL) isOnline;
@end

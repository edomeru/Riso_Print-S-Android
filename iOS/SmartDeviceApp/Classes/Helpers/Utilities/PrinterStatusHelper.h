//
//  PrinterStatusHelper.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/5/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SimplePing.h"

@protocol PrinterStatusHelperDelegate

-(void)statusDidChange:(BOOL)isOnline;

@end

@interface PrinterStatusHelper : NSObject <SimplePingDelegate>

@property (weak, nonatomic) id <PrinterStatusHelperDelegate> delegate;
@property (strong, nonatomic) NSString *ipAddress;

-(id) initWithPrinterIP:(NSString *) ipAddress;
-(void) stopPrinterStatusPolling;
-(void) startPrinterStatusPolling;

@end
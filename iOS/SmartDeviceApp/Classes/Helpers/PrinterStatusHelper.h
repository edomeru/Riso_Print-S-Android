//
//  PrinterStatusHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SimplePing.h"
@class PrinterStatusHelper;

@protocol PrinterStatusHelperDelegate
-(void)printerStatusHelper:(PrinterStatusHelper *)statusHelper statusDidChange:(BOOL)isOnline;
@end

@interface PrinterStatusHelper : NSObject <SimplePingDelegate>

@property (weak, nonatomic) id <PrinterStatusHelperDelegate> delegate;
@property (strong, nonatomic) NSString *ipAddress;

-(id) initWithPrinterIP:(NSString *) ipAddress;
-(void) stopPrinterStatusPolling;
-(void) startPrinterStatusPolling;
- (BOOL)isPolling;

@end
//
//  PrinterStatusHelper.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/5/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>
@protocol PrinterStatusHelperDelegate

-(void) updateStatus: (BOOL)isOnline;

@end

@interface PrinterStatusHelper : NSObject

@property (weak, nonatomic) id <PrinterStatusHelperDelegate> delegate;
@property NSString *ipAddress;

-(id) initWithPrinterIP:(NSString *) ipAddress;
-(void) stopPrinterStatusPolling;
-(void) startPrinterStatusPolling;

@end
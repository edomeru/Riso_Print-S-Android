//
//  SNMPManagerMock.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SNMPManagerMock : NSObject

- (void)searchForPrinterSuccessful:(NSString*)printerIP;
- (void)searchForPrinterFail:(NSString*)printerIP;
- (void)searchForAvailablePrintersSuccessful;
- (void)searchForAvailablePrintersFail;

@end

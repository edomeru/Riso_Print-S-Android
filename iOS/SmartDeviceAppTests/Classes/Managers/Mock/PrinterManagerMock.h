//
//  PrinterManagerMock.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

@interface PrinterManagerMock : NSObject

- (void)searchForPrinterSuccessful:(NSString*)printerIP;
- (void)searchForPrinterFail:(NSString*)printerIP;
- (void)searchForAllPrintersSuccessful;

@end

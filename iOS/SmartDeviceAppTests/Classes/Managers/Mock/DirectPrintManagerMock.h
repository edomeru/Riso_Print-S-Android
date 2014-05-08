//
//  DirectPrintManagerMock.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DirectPrintManagerMock : NSObject

- (void)printDocumentViaLPR;
- (void)printDocumentViaRaw;

@end

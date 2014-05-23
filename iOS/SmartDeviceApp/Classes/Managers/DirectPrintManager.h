//
//  DirectPrintManager.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol DirectPrintManagerDelegate;

@interface DirectPrintManager : NSObject

@property (nonatomic, weak) id<DirectPrintManagerDelegate> delegate;

+ (BOOL)idle;
+ (void)cancelAll;
- (void)printDocumentViaLPR;
- (void)printDocumentViaRaw;

@end

@protocol DirectPrintManagerDelegate <NSObject>

@required
- (void)documentDidFinishPrinting:(BOOL)successful;

@end

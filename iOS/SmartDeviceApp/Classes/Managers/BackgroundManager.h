//
//  BackgroundManager.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol BackgroundManagerCancellable;

@interface BackgroundManager : NSObject

+ (BackgroundManager *)sharedManager;
- (void)addCancellableObject:(id<BackgroundManagerCancellable>)object;
- (void)removeCancellableObject:(id<BackgroundManagerCancellable>)object;
- (void)cancelAll;
- (void)resumeAll;

@end

@protocol BackgroundManagerCancellable <NSObject>

@required
- (void)cancelToBackground;
- (void)resumeFromBackground;
- (BOOL)shouldResumeOnEnterForeground;

@end

//
//  SearchingIndicator.h
//  RISOSmartPrint
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

const float HEIGHT_WHILE_REFRESHING = 60.0f;

@interface SearchingIndicator : UIRefreshControl

- (void)setFrameIsInvalid:(BOOL)invalid;

@end

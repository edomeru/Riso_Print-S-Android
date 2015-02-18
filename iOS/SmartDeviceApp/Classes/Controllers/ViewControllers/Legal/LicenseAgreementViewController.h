//
//  LicenseAgreementViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//


#import <UIKit/UIKit.h>

@interface LicenseAgreementViewController : UIViewController

/**
 * returns YES if user have already agreed to License, NO otherwise
 */
+(BOOL) hasConfirmedToLicenseAgreement;


@end

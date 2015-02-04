//
//  LicenceAgreementViewController.h
//  RISOSmartPrint
//
//  Created by Gino on 2/4/15.
//  Copyright (c) 2015 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface LicenceAgreementViewController : UIViewController

/**
 * returns YES if user have already agreed to License, NO otherwise
 */
+(BOOL) hasConfirmedToLicenseAgreement;


@end

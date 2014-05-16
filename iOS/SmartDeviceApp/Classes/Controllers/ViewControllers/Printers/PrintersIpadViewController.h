//
//  PrintersIpadViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrintersViewController.h"
#import "PrinterCollectionViewCell.h"
#import "PrinterStatusHelper.h"

@interface PrintersIpadViewController : PrintersViewController<UICollectionViewDataSource, UICollectionViewDelegateFlowLayout, PrinterStatusHelperDelegate>

@end

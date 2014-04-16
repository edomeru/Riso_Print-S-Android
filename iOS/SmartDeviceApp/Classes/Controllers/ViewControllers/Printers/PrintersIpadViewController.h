//
//  PrintersIpadViewController.h
//  SmartDeviceApp
//
//  Created by Seph on 3/7/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrintersViewController.h"
#import "PrinterCollectionViewCell.h"

@interface PrintersIpadViewController : PrintersViewController<UICollectionViewDataSource, UICollectionViewDelegateFlowLayout, PrinterCollectionViewCellDelegate>

@end

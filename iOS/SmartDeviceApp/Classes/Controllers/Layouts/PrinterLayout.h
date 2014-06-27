//
//  PrinterLayout.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * Custom UICollectionView layout class for organizing the content 
 * of the "Printers" screen for tablets.
 * The printers are organized according to the following rules:
 *  - for portrait, the printers are listed in two columns
 *  - for landscape, the printers are listed in three columns
 *  - when a printer is deleted, the other printers are shifted to the left
 *  - 
 */
@interface PrinterLayout : UICollectionViewLayout

@end

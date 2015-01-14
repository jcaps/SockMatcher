//
//  SMGPageContentViewController.h
//  SockMatcher
//
//  Created by Joseph Caplan on 11/7/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SMGPageContentViewController : UIViewController

@property NSUInteger pageIndex;
@property NSString *imageFile;

@property (strong, nonatomic) IBOutlet UIImageView *backgroundImageView;

@end

//
//  SMGTutorialViewController.h
//  SockMatcher
//
//  Created by Joseph Caplan on 11/8/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SMGTutorialViewController : UIViewController <UIPageViewControllerDataSource>

@property (strong, nonatomic) UIPageViewController *pageViewController;
@property (strong, nonatomic) NSArray *pageImages;
@property (weak, nonatomic) IBOutlet UIButton *BackButtonOutlet;
@property (weak, nonatomic) IBOutlet UILabel *HowToPlayLabel;

- (IBAction)BackButtonPressedAction:(id)sender;

@end

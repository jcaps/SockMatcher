//
//  SMGPageContentViewController.m
//  SockMatcher
//
//  Created by Joseph Caplan on 11/7/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import "SMGPageContentViewController.h"

@interface SMGPageContentViewController ()

@end

@implementation SMGPageContentViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    self.backgroundImageView.image = [UIImage imageNamed:self.imageFile];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end

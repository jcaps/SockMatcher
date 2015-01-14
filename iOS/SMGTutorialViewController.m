//
//  SMGTutorialViewController.m
//  SockMatcher
//
//  Created by Joseph Caplan on 11/8/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import "SMGTutorialViewController.h"
#import "SMGPageContentViewController.h"

@interface SMGTutorialViewController ()

@end

@implementation SMGTutorialViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    _pageImages = @[@"tutorial1.png", @"tutorial2.png", @"tutorial3.png", @"tutorial4.png", @"tutorial5.png"];
    
    // Create page view controller
    self.pageViewController = [self.storyboard instantiateViewControllerWithIdentifier:@"PageViewController"];
    self.pageViewController.dataSource = self;
    
    SMGPageContentViewController *startingViewController = [self viewControllerAtIndex:0];
    NSArray *viewControllers = [[NSArray alloc] initWithObjects:startingViewController, nil];
    [self.pageViewController setViewControllers:viewControllers direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
    
    // Change the size of page view controller
    CGSize screenSize = [UIScreen mainScreen].bounds.size;
    self.pageViewController.view.frame = CGRectMake(0, self.BackButtonOutlet.frame.size.height, screenSize.width, screenSize.height-self.BackButtonOutlet.frame.size.height);
    
    NSDictionary *textAttributes = @{
                                     NSFontAttributeName: [UIFont fontWithName:@"ChalkboardSE-Bold" size:(18.0f*screenSize.width/320)],
                                     NSForegroundColorAttributeName: [UIColor whiteColor],
                                     NSStrokeColorAttributeName: [UIColor blackColor],
                                     NSStrokeWidthAttributeName: [NSNumber numberWithFloat:-3.0f]};
    [self.HowToPlayLabel setAttributedText:[[NSAttributedString alloc] initWithString:@"How to Play" attributes:textAttributes]];
    [self.HowToPlayLabel setTextAlignment:NSTextAlignmentCenter];
    
    
    [self addChildViewController:_pageViewController];
    [self.view addSubview:_pageViewController.view];
    [self.pageViewController didMoveToParentViewController:self];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:NO];
    self.pageViewController = nil;
    self.pageImages = nil;
    self.BackButtonOutlet = nil;
    self.HowToPlayLabel = nil;
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

#pragma mark - Page View Controller Data Source

- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerBeforeViewController:(UIViewController *)viewController
{
    NSUInteger index = ((SMGPageContentViewController*) viewController).pageIndex;
    
    if ((index == 0) || (index == NSNotFound)) {
        return nil;
    }
    
    index--;
    return [self viewControllerAtIndex:index];
}

- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerAfterViewController:(UIViewController *)viewController
{
    NSUInteger index = ((SMGPageContentViewController*) viewController).pageIndex;
    
    if (index == NSNotFound) {
        return nil;
    }
    
    index++;
    if (index == [self.pageImages count]) {
        return nil;
    }
    return [self viewControllerAtIndex:index];
}

- (SMGPageContentViewController *)viewControllerAtIndex:(NSUInteger)index
{
    if (([self.pageImages count] == 0) || (index >= [self.pageImages count])) {
        return nil;
    }
    
    // Create a new view controller and pass suitable data.
    SMGPageContentViewController *pageContentViewController = [self.storyboard instantiateViewControllerWithIdentifier:@"PageContentViewController"];
    pageContentViewController.imageFile = self.pageImages[index];
    pageContentViewController.pageIndex = index;
    
    return pageContentViewController;
}

- (NSInteger)presentationCountForPageViewController:(UIPageViewController *)pageViewController
{
    return [self.pageImages count];
}

- (NSInteger)presentationIndexForPageViewController:(UIPageViewController *)pageViewController
{
    return 0;
}

- (IBAction)BackButtonPressedAction:(id)sender
{
    [self performSegueWithIdentifier:@"unwindFromTutorial" sender:self];
}
@end

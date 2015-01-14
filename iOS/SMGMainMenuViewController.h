//
//  SMGMainMenuViewController.h
//  SockMatcher
//
//  Created by Joseph Caplan on 10/13/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SMGMainMenuViewController : UIViewController

@property (weak, nonatomic) IBOutlet UIButton *MusicButton;
@property (weak, nonatomic) IBOutlet UIButton *SoundButton;
@property (weak, nonatomic) IBOutlet UILabel *HighScoreLabel;
@property (weak, nonatomic) IBOutlet UILabel *HighScoreValueLabel;

- (IBAction)MusicButtonClicked:(id)sender;
- (IBAction)SoundButtonClicked:(id)sender;
- (IBAction)unwindToMainMenu:(UIStoryboardSegue*)segue;

@end

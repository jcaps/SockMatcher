//
//  SMGMainMenuViewController.m
//  SockMatcher
//
//  Created by Joseph Caplan on 10/13/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import "SMGMainMenuViewController.h"
#import "SMGViewController.h"
#import <AVFoundation/AVFoundation.h>

@interface SMGMainMenuViewController () {
    BOOL musicOn;
    BOOL soundOn;
    NSDictionary *textAttributes;
    NSNumber *highScore;
    NSMutableArray *soundPlayers;
    UIImage *musicButtonOnImage, *musicButtonOffImage, *soundButtonOnImage, *soundButtonOffImage;
}

@property (strong, nonatomic) AVAudioPlayer *bgMusicPlayer;

@end

@implementation SMGMainMenuViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];

    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    NSString *highScoreKey = @"highScoreKey";
    NSString *musicKey = @"musicKey";
    NSString *soundKey = @"soundKey";
    
    if (![[NSUserDefaults standardUserDefaults] objectForKey:highScoreKey]) {
        [[NSUserDefaults standardUserDefaults] setObject:[NSNumber numberWithInt:0] forKey:highScoreKey];
        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:musicKey];
        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:soundKey];
        [[NSUserDefaults standardUserDefaults] synchronize];
    }

    musicOn = [[NSUserDefaults standardUserDefaults] boolForKey:musicKey];
    soundOn = [[NSUserDefaults standardUserDefaults] boolForKey:soundKey];
    
    NSError *error;
    NSString *path = [NSString stringWithFormat:@"%@/bgmusic.mp3", [[NSBundle mainBundle] resourcePath]];
    NSURL *musicURL = [NSURL fileURLWithPath:path];
    self.bgMusicPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:musicURL error:&error];
    self.bgMusicPlayer.numberOfLoops = -1;
    [self.bgMusicPlayer prepareToPlay];
    
    NSArray *soundFileNames = [NSArray arrayWithObjects:@"%@/bleach.wav", @"%@/burn.wav", @"%@/drop.wav", @"%@/fireball.wav", @"%@/pop.wav", @"%@/sockremover.wav", @"%@/spray.wav", nil];
    soundPlayers = [[NSMutableArray alloc] init];
    AVAudioPlayer *soundPlayer;
    
    for (NSString* soundFile in soundFileNames) {
        path = [NSString stringWithFormat:soundFile, [[NSBundle mainBundle] resourcePath]];
        musicURL = [NSURL fileURLWithPath:path];
        soundPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:musicURL error:&error];
        [soundPlayer prepareToPlay];
        [soundPlayers addObject:soundPlayer];
    }
    
    musicButtonOnImage = [UIImage imageNamed:@"musicButtonOn.png"];
    musicButtonOffImage = [UIImage imageNamed:@"musicButtonOff.png"];
    soundButtonOnImage = [UIImage imageNamed:@"soundButtonOn.png"];
    soundButtonOffImage = [UIImage imageNamed:@"soundButtonOff.png"];
    
    if (musicOn) {
        [self.MusicButton setImage:musicButtonOnImage forState:UIControlStateNormal];
        [self.bgMusicPlayer play];
    } else {
        [self.MusicButton setImage:musicButtonOffImage forState:UIControlStateNormal];
    }
    
    if (soundOn) {
        [self.SoundButton setImage:soundButtonOnImage forState:UIControlStateNormal];
    } else {
        [self.SoundButton setImage:soundButtonOffImage forState:UIControlStateNormal];
    }
    
    CGSize screenSize = [UIScreen mainScreen].bounds.size;
    CGSize correctedSize = CGSizeMake(MIN(screenSize.width, screenSize.height), MAX(screenSize.width, screenSize.height));
    textAttributes = @{
                                     NSFontAttributeName: [UIFont fontWithName:@"ChalkboardSE-Bold" size:(28.0f*correctedSize.width/320)],
                                     NSForegroundColorAttributeName: [UIColor whiteColor],
                                     NSStrokeColorAttributeName: [UIColor blackColor],
                                     NSStrokeWidthAttributeName: [NSNumber numberWithFloat:-3.0f]};
    [self.HighScoreLabel setAttributedText:[[NSAttributedString alloc] initWithString:@"High Score:" attributes:textAttributes]];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:NO];
    highScore = (NSNumber*)[[NSUserDefaults standardUserDefaults] objectForKey:@"highScoreKey"];
    [self.HighScoreValueLabel setAttributedText:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%d", [highScore intValue]] attributes:textAttributes]];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    textAttributes = nil;
    highScore = nil;
    soundPlayers = nil;
    musicButtonOnImage = nil;
    musicButtonOffImage = nil;
    soundButtonOnImage = nil;
    soundButtonOffImage = nil;
}


#pragma mark - Navigation

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"PlayGameSegue"]) {
        SMGViewController *vc = (SMGViewController*)segue.destinationViewController;
        vc.sound = soundOn;
        vc.highScore = [highScore intValue];
        vc.soundPlayers = [NSArray arrayWithArray:soundPlayers];
    }
}

#pragma mark - IBActions

- (IBAction)MusicButtonClicked:(id)sender {
    if (musicOn) {
        [self.bgMusicPlayer stop];
        [self.MusicButton setImage:musicButtonOffImage forState:UIControlStateNormal];
        musicOn = NO;
    }
    
    else {
        [self.bgMusicPlayer play];
        [self.MusicButton setImage:musicButtonOnImage forState:UIControlStateNormal];
        musicOn = YES;
    }
    
    [[NSUserDefaults standardUserDefaults] setBool:musicOn forKey:@"musicKey"];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

- (IBAction)SoundButtonClicked:(id)sender {
    if (soundOn) {
        [self.SoundButton setImage:soundButtonOffImage forState:UIControlStateNormal];
        soundOn = NO;
    }
    
    else {
        [self.SoundButton setImage:soundButtonOnImage forState:UIControlStateNormal];
        soundOn = YES;
    }
    
    [[NSUserDefaults standardUserDefaults] setBool:soundOn forKey:@"soundKey"];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

- (IBAction)unwindToMainMenu:(UIStoryboardSegue *)segue
{
}

@end

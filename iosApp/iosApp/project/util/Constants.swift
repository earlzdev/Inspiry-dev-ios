//
//  Constants.swift
//  iosApp
//
//  Created by rst10h on 14.01.22.
//

import Foundation

class Constants {
    //Navigation MAIN
    static let TAG_EDIT_VIEW = "EditView"
    static let TAG_ONBOARDING_VIEW = "OnBoardingView"
    static let TAG_SUBSCRIBE_VIEW = "SubscribeView"
    //Navigation EDIT
    static let TAG_ANIMATION_VIEW = "TextAnimationsView"
    static let TAG_STICKERS_VIEW = "StickersView"
    static let TAG_MUSIC_LIBRARY = "MusicLibraryView"
    static let TAG_PICK_IMAGE = "PickImageView"
    
    //Settings
    static let KEY_ONBOARDING_SHOWN = "OnBoardingWasShown"
    static let TOP_SAFE_AREA_MIN_SIZE = 30.cg
    
    
    //Debug
    static let ONBOARDING_ALWAYS_VISIBLE = false
    
    //VideoEdit
    static let VISIBLE_VIDEO_DURATION_SIZE_MS = 500 * 60 // maximum visible time when user edites video (milliseconds)
    
    //TextAppearance
    static let TEXT_SHADOW_BLUR_DELIMITER = 20.cg //there is a value to calculate the blur radius of shadow, to achieve the same behavior as in android
    static let TEXT_OUTLINE_WIDTH = -4 //unlike android, ios automatically selects the thickness value depending on the size of the text
    
    //Keys
    static let media_library_second_access_dialog = "secondary_media_library_access_dialog"
}

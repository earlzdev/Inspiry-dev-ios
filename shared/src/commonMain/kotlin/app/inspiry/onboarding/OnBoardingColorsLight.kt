package app.inspiry.onboarding

import dev.icerock.moko.graphics.Color

class OnBoardingColorsLight: OnBoardingColors {

    override val textGradientStartColor: Color
        get() = Color(0x3F, 0x00, 0xff, 0xff)
    override val textGradientEndColor: Color
        get() = Color(0x09, 0xc4, 0xff, 0xff)

    override val videoPromoContinueGradientStart: Color
        get() = Color(0x14, 0xCB, 0xF5, 0xff)
    override val videoPromoContinueGradientEnd: Color
        get() = Color(0x64, 0x73, 0xFF, 0xff)

    override val secondQuizContinueGradientStart: Color
        get() = Color(0x00, 0x75, 0xff, 0xff)

    override val secondQuizContinueGradientEnd: Color
        get() = Color(0x09, 0xb5, 0xFF, 0xff)

    override val quizBg: Color
        get() = Color(0xfb, 0xfb, 0xfb, 0xff)

    override val skipQuizText: Color
        get() = Color(0xa8, 0xa8, 0xa8, 0xff)

    override val firstQuizUsefulAnswers: Color
        get() = Color(0x82, 0x82, 0x82, 0xff)

    override val firstQuizOptionBg: Color
        get() = Color(0x5b, 0x7e, 0xfe, 0xff)

    override val secondQuizOptionBg: Color
        get() = Color(0x6b, 0x8b, 0xfe, 0xff)

    override val secondQuizSuggestText: Color
        get() = Color(0x4f, 0x4f, 0x4f, 0xff)

    override val secondQuizSuggestTextHint: Color
        get() = Color(0xc4, 0xc4, 0xc4, 0xff)

    override val secondQuizSuggestBg: Color
        get() = Color(0xee, 0xee, 0xee, 0xff)

    override val pageIndicatorActive: Color
        get() = Color(0x55, 0x52, 0xff, 0xff)

    override val pageIndicatorInactive: Color
        get() = Color(0x9e, 0xa8, 0xff, 0xff)

    override val quizTextSkip: Color
        get() = Color(0xa5, 0xa5, 0xa5, 0xff)
}
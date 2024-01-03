package app.inspiry.onboarding

class OnBoardingDimensPhone: OnBoardingDimens() {
    override val buttonContinueTextSize: Int
        get() = 20
    override val buttonContinueHeight: Int
        get() = 50
    override val buttonContinuePaddingHorizontal: Int
        get() = 10
    override val buttonContinueMinWidth: Int
        get() = 200
    override val buttonContinueWidthPercent: Float
        get() = 0.65f
    override val buttonContinueCorners: Int
        get() = 16
    override val buttonContinueMaxLines: Int
        get() = 1
    override val skipText: Int
        get() = 14
    override val firstQuizUsefulAnswers: Int
        get() = 16
    override val firstQuizOption: Int
        get() = 20
    override val firstQuizOptionHeight: Int
        get() = 43
    override val firstQuizOptionPaddingVertical: Int
        get() = 10
    override val firstQuizTitlePaddingBottom: Int
        get() = 60
    override val firstQuizUsefulAnswersPaddingBottom: Int
        get() = 50
    override val secondQuizOption: Int
        get() = 18
    override val secondQuizOptionPaddingLeft: Int
        get() = 33
    override val optionCorners: Int
        get() = 12
    override val optionSelectedBorder: Int
        get() = 3
    override val pageIndicatorSize: Int
        get() = 7
    override val pageIndicatorPaddingHorizontal: Int
        get() = 8
    override val animatedTextWidthPercent: Float
        get() = 0.8f
    override val animatedTextMinHeight: Int
        get() = 50
    override val animatedPaddingBottom: Int
        get() = 22
    override val animatedPaddingTop: Int
        get() = 10
    override val videoPromoIndicatorPaddingTop: Int
        get() = 24
    override val videoPromoIndicatorPaddingBottom: Int
        get() = videoPromoIndicatorPaddingTop - 2
}
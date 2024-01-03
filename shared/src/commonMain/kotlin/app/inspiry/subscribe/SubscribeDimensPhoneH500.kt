package app.inspiry.subscribe

class SubscribeDimensPhoneH500: SubscribeDimens {
    override val headerCorners: Float
        get() = 20f
    override val headerTextStartEndPadding: Float
        get() = 18f
    override val headerTextTitle: Float
        get() = 27f
    override val headerTextSubTitle: Float
        get() = 16f
    override val headerTextBottomPadding: Float
        get() = 8f

    override val featuresListTopPadding: Float
        get() = 8f
    override val featuresListBottomPadding: Float
        get() = 3f
    override val featuresListItemHeight: Float
        get() = 90f
    override val featuresListItemText: Float
        get() = 10f
    override val featuresListItemBottomPadding: Float
        get() = 11f
    override val featuresListItemStartEndPadding: Float
        get() = 5f

    override val defaultBorderWidth: Float
        get() = 3f

    override val optionStartEndPadding: Float
        get() = 24f
    override val optionText: Float
        get() = 15f

    override val optionALabelText: Float
        get() = 12f
    override val optionAHeight: Float
        get() = 55f
    override val optionALabelHeight: Float
        get() = 26f
    override val optionATopPadding: Float
        get() = 8f
    override val optionACorners: Float
        get() = 18f
    override val optionALabelCorners: Float
        get() = 8f

    override val optionBTopPadding: Float
        get() = 8f
    override val optionBHeight: Float
        get() = 52f
    override val optionBLabelHeight: Float
        get() = 70f
    override val optionBWithLabelHeight: Float
        get() = 79f
    override val optionBCorners: Float
        get() = 22f
    override val optionBLabelCorners: Float
        get() = 16f
    override val optionBLabelText: Float
        get() = 14f
    override val optionBPriceText: Float
        get() = optionText - 1f

    override val trialInfoTopPadding: Float
        get() = 5f
    override val trialInfoHeight: Float
        get() = 14f
    override val trialInfoText: Float
        get() = 10f

    override val subscribeButtonHeight: Float
        get() = 45f
    override val subscribeButtonATopPadding: Float
        get() = 10f
    override val subscribeButtonBTopPadding: Float
        get() = 8f
    override val subscribeButtonAStartEndPadding: Float
        get() = 34f
    override val subscribeButtonBStartEndPadding: Float
        get() = optionStartEndPadding
    override val subscribeButtonText: Float
        get() = 24f
    override val subscribeButtonCorners: Float
        get() = 16f

    override val subscribeConditionsTopPadding: Float
        get() = 10f
    override val subscribeConditionsHeight: Float
        get() = 14f
    override val subscribeConditionsBottomPadding: Float
        get() = 10f
    override val subscribeConditionsText: Float
        get() = 10f

    override val radioButtonSize: Float
        get() = 20f
    override val radioStrokeWidth: Float
        get() = 2f
    override val radioButtonDotSize: Float
        get() = radioButtonSize - radioStrokeWidth
    override val radioRadius: Float
        get() = radioButtonSize / 2
    override val radioButtonPadding: Float
        get() = 2f
}
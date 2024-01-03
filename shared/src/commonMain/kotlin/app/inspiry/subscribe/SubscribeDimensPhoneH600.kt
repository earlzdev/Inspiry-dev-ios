package app.inspiry.subscribe

class SubscribeDimensPhoneH600: SubscribeDimens {
    override val headerCorners: Float
        get() = 23f
    override val headerTextStartEndPadding: Float
        get() = 18f
    override val headerTextTitle: Float
        get() = 30f
    override val headerTextSubTitle: Float
        get() = 20f
    override val headerTextBottomPadding: Float
        get() = 10f

    override val featuresListTopPadding: Float
        get() = 10f
    override val featuresListBottomPadding: Float
        get() = 5f
    override val featuresListItemHeight: Float
        get() = 100f
    override val featuresListItemText: Float
        get() = 11f
    override val featuresListItemBottomPadding: Float
        get() = 12f
    override val featuresListItemStartEndPadding: Float
        get() = 6f

    override val defaultBorderWidth: Float
        get() = 3f

    override val optionStartEndPadding: Float
        get() = 26f
    override val optionText: Float
        get() = 16f

    override val optionALabelText: Float
        get() = 13f
    override val optionAHeight: Float
        get() = 60f
    override val optionALabelHeight: Float
        get() = 28f
    override val optionATopPadding: Float
        get() = 8f
    override val optionACorners: Float
        get() = 18f
    override val optionALabelCorners: Float
        get() = 8f

    override val optionBTopPadding: Float
        get() = 8f
    override val optionBHeight: Float
        get() = 55f
    override val optionBLabelHeight: Float
        get() = 80f
    override val optionBWithLabelHeight: Float
        get() = 89f
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
        get() = 18f
    override val trialInfoText: Float
        get() = 12f

    override val subscribeButtonHeight: Float
        get() = 50f
    override val subscribeButtonATopPadding: Float
        get() = 12f
    override val subscribeButtonBTopPadding: Float
        get() = 8f
    override val subscribeButtonAStartEndPadding: Float
        get() = 37f
    override val subscribeButtonBStartEndPadding: Float
        get() = optionStartEndPadding
    override val subscribeButtonText: Float
        get() = 24f
    override val subscribeButtonCorners: Float
        get() = 16f

    override val subscribeConditionsTopPadding: Float
        get() = 12f
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
package app.inspiry.edit.ui

interface EditDimens {
    val exportTagUsBetweenPadding: Int
    val exportTagUsInspiryBgCornerRadius: Int
    val exportTagUsInspiryText: Int
    val exportChoiceTextMaxWidth: Int
    val exportChoiceTextPaddingHorizontal: Int
    val exportChoiceTextSize: Int
    val exportChoiceImageSize: Int
    val exportChoiceImagePaddingHorizontal: Int
    val exportChoiceImagePaddingTop: Int
    val exportChoiceImagePaddingBottom: Int
    val exportChoiceSingleHeight: Int
    val topBarTextSize: Int
    val topBarHeight: Int
    val instrumentsHeight: Int
    val instrumentsIconWidth: Int
    val exportBottomPanelHeightProgress: Int
    val exportBottomPanelPaddingHorizontal: Int
    val exportBottomPanelProgressPaddingTop: Int
    val exportBottomPanelProgressPaddingBetweenTextProgress: Int
    val exportCornerRadius: Int
    val exportImageElseVideoHeight: Int
    val exportImageElseVideoWidth: Int
        get() = exportImageElseVideoButtonWidth * 2 + exportImageElseVideoOuterPaddingHorizontal * 2
    val exportImageElseVideoButtonWidth: Int
    val exportImageElseVideoOuterPaddingHorizontal: Int
    val exportImageElseVideoCornerRadius: Int
    val exportImageElseVideoButtonCornerRadius: Int
    val exportImageElseVideoPaddingVertical: Int
    val exportChoiceInnerPaddingTop: Int
    val exportChoiceInnerPaddingBottom: Int
    val exportChoicePaddingTop: Int
    val editTemplateHorizontalPadding: Int
    val editTopBarHorizontalPadding: Int
    val editTopBarHeigth: Int
    val editTopBarTextRounding: Int
    val editTopBarTextPadding: Int
}
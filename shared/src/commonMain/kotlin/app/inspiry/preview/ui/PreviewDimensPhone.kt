package app.inspiry.preview.ui

class PreviewDimensPhone: PreviewDimens {
    override val defaultBorderWidth: Float
        get() = 1f
    override val shadowHeight: Float
        get() = 120f
    override val closeIconTopPadding: Float
        get() = 21f - closeIconClickablePadding
    override val closeIconEndPadding: Float
        get() = 10f - closeIconClickablePadding
    override val closeIconClickablePadding: Float
        get() = 5f

    override val waterMarkInspiryStartPadding: Float
        get() = 18f
    override val waterMarkInspiryScrollStep: Float
        get() = 0.7f
    override val waterMarkInspiryText: Float
        get() = 22f
    override val waterMarkInspiryTextOpacity: Float
        get() = 0.3f
    override val waterMarkInspiryItemBottomPadding: Float
        get() = 30f


    override val progressHeight: Float
        get() = 1.6f
    override val progressStartEndPadding: Float
        get() = 5f
    override val progressTopPadding: Float
        get() = 10f

    override val IGTopLayoutStartPadding: Float
        get() = 10f
    override val IGTopLayoutTopPadding: Float
        get() = 28f
    override val storiesIconSize: Float
        get() = 26f
    override val storiesProfileStartPadding: Float
        get() = 10f
    override val storiesProfileText: Float
        get() = 12f

    override val IGBottomLayoutBottomPadding: Float
        get() = 10f
    override val IGBottomLayoutStartPadding: Float
        get() = 14f
    override val IGBottomLayoutEndPadding: Float
        get() = 22f

    override val storiesPhotoSize: Float
        get() = 35f
    override val storiesCommentsHeight: Float
        get() = 35f
    override val storiesCommentsStartPadding: Float
        get() = 12f
    override val storiesCommentsEndPadding: Float
        get() = 23f
    override val storiesCommentsCorners: Float
        get() = 20f
    override val storiesContextEndPadding: Float
        get() = 16f
    override val storiesDirectBottomPadding: Float
        get() = 2f
}
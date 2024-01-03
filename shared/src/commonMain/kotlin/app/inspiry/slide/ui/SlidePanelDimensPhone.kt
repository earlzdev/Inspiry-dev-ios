package app.inspiry.slide.ui

class SlidePanelDimensPhone: SlidePanelDimens {
    override val trimCurrentTimeLineWidth: Int
        get() = 2
    override val tabPanelHeight: Int
        get() = 52
    override val contentPanelHeight: Int
        get() = 83
    override val tabTextSize: Int
        get() = 10
    override val trimBgPlayPauseSize: Int
        get() = 30
    override val volumeSliderPaddingSide: Int
        get() = 32
    override val noVolumePaddingRight: Int
        get() = 7
    override val volumeSliderHeight: Int
        get() = 32
    override val volumeSliderBgRounding: Int
        get() = 6
    override val trimContentPaddingSize: Int
        get() = 20
    override val trimTextIndicatorBgCornerRadius: Int
        get() = 4
    override val trimImageSequenceCornerRadius: Int
        get() = 8
    override val trimImageSequenceSize: Int
        get() = 40

    override val trimWhiteBorderThicknessHorizontal: Int
        get() = 8
    override val trimWhiteBorderThicknessVertical: Int
        get() = 4

    override val playPauseAddOffsetYIfHasTabs: Int
        get() = 12

    override val trimDragBoxSize: Int
        get() = 52
    override val BottomTrimDragBoxSize: Int
        get() = 4
    override val labelTextSize: Int
        get() = 11
}
package app.inspiry.slide.ui

import app.inspiry.edit.instruments.textPanel.BottomInstrumentsDimensPhone

class SlideInstrumentDimensPhone: SlidesInstrumentDimens {
    override val barHeight: Int
        get() = 60

    override val itemSize: Int
        get() = 34

    override val itemPadding: Int
        get() = 10

    override val addTextSize: Int
        get() = 10

    override val addIconSize: Int
        get() = 10
}
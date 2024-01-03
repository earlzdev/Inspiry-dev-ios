package app.inspiry.edit.instruments.textPanel

import app.inspiry.edit.instruments.defaultPanel.BottomInstrumentsDimens

class BottomInstrumentsDimensPhone : BottomInstrumentsDimens {
    override val labelTextSize: Int
        get() = 11
    override val barHeight: Int
        get() = 64
    override val instrumentsIconSize: Int
        get() = 24
    override val instrumentItemWidth: Int
        get() = 64
    override val panelMaxHeight: Int
        get() = 400
}
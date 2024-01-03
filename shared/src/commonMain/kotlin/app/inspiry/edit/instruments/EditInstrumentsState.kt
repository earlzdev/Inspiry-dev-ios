package app.inspiry.edit.instruments

data class EditInstrumentsState(
    val currentMainInstrument: InstrumentMain? = null,
    val currentAdditionalInstrument: InstrumentAdditional? = null,
    val currentSelectionType: SelectionType = SelectionType.NOTHING
)
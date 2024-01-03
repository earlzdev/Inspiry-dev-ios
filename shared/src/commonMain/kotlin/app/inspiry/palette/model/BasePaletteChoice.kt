package app.inspiry.palette.model


abstract class BasePaletteChoice<ELEMENT> {
    abstract var color: Int?
    abstract var elements: List<ELEMENT>
}
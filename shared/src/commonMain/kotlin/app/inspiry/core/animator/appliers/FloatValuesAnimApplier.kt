package app.inspiry.core.animator.appliers


interface FloatValuesAnimApplier: ToAsFromSwappableAnimApplier {
    var from: Float
    var to: Float

    fun getValue(value: Float): Float {
        return AnimApplier.calcAnimValue(from,to, value)
    }

    override fun setToAsFrom() {
        to = from
    }
}
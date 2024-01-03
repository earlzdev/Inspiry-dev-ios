package app.inspiry.subscribe.model

class DisplayProduct(
    val localizedPrice: String,
    val price: Number,
    val trialDays: Int,
    val id: String,
    val period: DisplayProductPeriod,
    // TODO: make concrete type with expect/actual. It requires us to connect ios dependencies to kotlin via cocoapods
    val underlyingModel: Any
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DisplayProduct) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "DisplayProduct(localizedPrice='$localizedPrice', price=$price, trialDays=$trialDays, id='$id', period=$period)"
    }
}
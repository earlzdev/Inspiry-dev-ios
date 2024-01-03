package app.inspiry.core.animator.interpolator

import kotlinx.serialization.Serializable

@Serializable
sealed class InspInterpolator {
    abstract fun getInterpolation(input: Float): Float

    companion object {
        private fun maySimpleType(type: String): InspPathInterpolator {
            val path = type.split(',').map {
                it.toFloatOrNull()
                    ?: throw IllegalArgumentException("bad interpolator value or unknown Interpolator ($it) in $type")
            }
            if (path.size != 4) throw IllegalArgumentException("enough data for interpolator $type")
            return InspPathInterpolator(path[0], path[1], path[2], path[3])
        }

        fun pathInterpolatorBy(type: String): InspPathInterpolator {
            return when (type) {
                //moderate
                "cubicInOut" -> {
                    InspPathInterpolator(0.645f, 0.045f, 0.355f, 1.0f)
                }
                "cubicIn" -> {
                    InspPathInterpolator(0.55f, 0.055f, 0.675f, 0.19f)
                }
                "cubicOut" -> {
                    InspPathInterpolator(0.215f, 0.61f, 0.355f, 1.0f)
                }

                //intense
                "expIn" -> {
                    InspPathInterpolator(0.95f, 0.05f, 0.795f, 0.035f)
                }
                "expOut" -> {
                    InspPathInterpolator(0.19f, 1.0f, 0.22f, 1.0f)
                }

                //they have two phases: begins with linear and then does expOut. First is more smooth, has less linear phase
                "linear25expOut" -> {
                    InspPathInterpolator(0.25f, 0.25f, 0.0f, 1.0f)
                }
                "linear50expOut" -> {
                    InspPathInterpolator(0.5f, 0.5f, 0.0f, 1.0f)
                }

                //more smooth than linear
                "flatIn25expOut" -> {
                    InspPathInterpolator(0.25f, 0.0f, 0.0f, 1.0f)
                }
                "flatIn20expOut" -> {
                    InspPathInterpolator(0.2f, 0.0f, 0.0f, 1.0f)
                }
                "flatIn50expOut" -> {
                    InspPathInterpolator(0.5f, 0.0f, 0.0f, 1.0f)
                }
                "flatInExpOut" -> {
                    InspPathInterpolator(0.75f, 0.0f, 0.0f, 1.0f)
                }
                "flatIn30expOut" -> {
                    InspPathInterpolator(0.3f, 0.0f, 0.0f, 1.0f)
                }
                "flatIn38expOut" -> {
                    InspPathInterpolator(0.38f, 0.0f, 0.0f, 1.0f)
                }

                //slow - fast - slow
                "easeInOutQuint" -> {
                    InspPathInterpolator(0.86f, 0.0f, 0.07f, 1.0f)
                }
                "fastInSuperfastOut1" -> {
                    InspPathInterpolator(0.0f, 0.5f, 1.0f, 0.0f)
                }

                //slow - fast - slow, but smoother than easeInOutQuint
                "fastInSuperfastOut1Invert" -> {
                    InspPathInterpolator(0.5f, 0.0f, 0.0f, 1.0f)

                    //twice lighter than the previous
                }
                "easeInOutQuintLight" -> {

                    InspPathInterpolator(0.65f, 0.25f, 0.2f, 1.0f)
                }
                "slowInExpOut" -> {
                    InspPathInterpolator(0.75f, 0.25f, 0.0f, 1.0f)
                }

                //used in yoga floating template
                "easeInOut" -> {
                    InspPathInterpolator(0.1f, 0.3f, 0.7f, 0.9f)
                }

                //ease - light, smooth interpolators
                "easeIn" -> {
                    InspPathInterpolator(0.42f, 0f, 0.58f, 1.0f)
                }
                "easeOut" -> {
                    InspPathInterpolator(0f, 0f, 0.58f, 1.0f)
                }
                "easeInLight" -> {
                    InspPathInterpolator(0.25f, 0f, 0.65f, 0.64f)
                }
                "lightOut" -> {
                    InspPathInterpolator(0.4f, 0f, 0.21f, 1f)
                }
                "fastOut" -> {
                    InspPathInterpolator(0.1f, 0f, 0f, 1f)

                }
                "easeOutLight" -> {
                    InspPathInterpolator(0.25f, 0.25f, 0.55f, 0.71f)

                }
                "easeInOutLight" -> {
                    InspPathInterpolator(0.28f, 0f, 0.68f, 1f)
                }
                "halloweenInOut" -> {
                    InspPathInterpolator(0f, 0f, 0f, 1f)
                }
                "beautyOut" -> {
                    InspPathInterpolator(0.18f, 0.1f, 0.5f, 1f)

                }
                "elegantlySlowOut" -> {
                    InspPathInterpolator(0.25f, 0.1f, 0.25f, 1f)

                }
                "smooth30InOut" -> {
                    InspPathInterpolator(0.3f, 0.0f, 0.24f, 1.0f)
                }
                "slow30InSmoothOut" -> {
                    InspPathInterpolator(0.3f, 0.0f, 0.19f, 0.99f)
                }
                "slow20InSmoothOut" -> {
                    InspPathInterpolator(0.2f, 0.0f, 0.19f, 0.99f)
                }
                else -> maySimpleType(type)
            }
        }
    }
}



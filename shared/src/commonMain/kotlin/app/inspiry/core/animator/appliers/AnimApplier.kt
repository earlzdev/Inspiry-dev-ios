package app.inspiry.core.animator.appliers

import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.manager.DebugManager
import app.inspiry.core.util.WorkerThread
import app.inspiry.views.InspView
import app.inspiry.views.text.InnerGenericText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
abstract class AnimApplier {

    @WorkerThread
    open fun onPrepared(view: InspView<*>, value: Float) {
    }

    open fun onPreDraw(view: InspView<*>, value: Float) {

    }

    companion object {
        inline fun calcAnimValue(from: Float, to: Float, value: Float) =
            ((to - from) * value) + from

    }

    open fun transformText(
        param: DrawBackgroundAnimParam,
        value: Float,
        view: InnerGenericText<*>
    ) {

    }

    fun subCompare(with: AnimApplier): Boolean {
        when (this) {
            is MoveAnimApplier -> {
                if (with !is MoveAnimApplier) return false
                return (this.fromX == with.fromX && this.toX == with.toX && this.fromY == with.fromY && this.toY == with.toY)
            }
            is ScaleOuterAnimApplier -> {
                if (with !is ScaleOuterAnimApplier) return false
                return (this.fromX == with.fromX && this.toX == with.toX && this.fromY == with.fromY && this.toY == with.toY)
            }
            is FadeAnimApplier -> {
                if (with !is FadeAnimApplier) return false
                return (this.from == with.from && this.to == with.to)
            }
            else -> {
                if (DebugManager.isDebug) throw IllegalStateException("NOT IMPLEMENTED!! AnimApplier comparing ${this::class.simpleName}")
            }
        }
        return false
    }
//    {
//        when (this) {
//            is BackgroundColorAnimApplier -> this.transformText(param, value, view)
//            is BlinkAnimApplier -> this.transformText(param, value, view)
//            is ClipAnimApplier -> this.transformText(param, value, view)
//            is FadeAnimApplier -> this.transformText(param, value, view)
//            is MoveAnimApplier -> this.transformText(param, value, view)
//            is MoveToXAnimApplier -> this.transformText(param, value, view)
//            is MoveToYAnimApplier -> this.transformText(param, value, view)
//            is RadiusAnimApplier -> this.transformText(param, value, view)
//            is RotateAnimApplier -> this.transformText(param, value, view)
//            is ScaleOuterAnimApplier -> this.transformText(param, value, view)
//            is ToneAnimApplier -> this.transformText(param, value, view)
//        }
//    }
}

@Serializable
@SerialName("nothing")
class NothingAnimApplier : AnimApplier()

/**
 * color types
 */
enum class ColorType {
    MAIN_COLOR,
    BACK_COLOR,
    SHADOW_COLOR,
    STROKE_COLOR,
    BACKGROUND_COLOR,
    ANIMATION_COLOR_1,
    ANIMATION_COLOR_2,
}
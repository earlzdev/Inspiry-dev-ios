package app.inspiry.core.media

import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.serialization.AnimatorSerializer
import app.inspiry.views.media.ColorFilterMode
import kotlinx.serialization.Serializable

@Serializable
class UndoRemoveBgData(
    val colorFilterMode: ColorFilterMode,
    val borderWidth: String?,
    val isMovable: Boolean? = false, //default value to prevent errors in saved templates
    val width: String,
    val height: String,
    val translationX: Float,
    val translationY: Float,
    val rotation: Float,
    val animatorsIn: List<@Serializable(with = AnimatorSerializer::class) InspAnimator>,
    val animatorsOut: List<@Serializable(with = AnimatorSerializer::class) InspAnimator>,
    val animatorsAll: List<@Serializable(with = AnimatorSerializer::class) InspAnimator>,
)
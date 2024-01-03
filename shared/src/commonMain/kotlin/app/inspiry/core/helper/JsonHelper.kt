package app.inspiry.core.helper

import app.inspiry.core.animator.appliers.*
import app.inspiry.core.animator.interpolator.*
import app.inspiry.core.media.Media
import app.inspiry.core.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.reflect.KClass

object JsonHelper {
    fun initJson() = Json {
        allowStructuredMapKeys = true
        prettyPrint = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {

            polymorphic(AnimApplier::class) {
                subclass(BackgroundColorAnimApplier::class)
                subclass(BlinkAnimApplier::class)
                subclass(BlurAnimApplier::class)
                subclass(ClipAnimApplier::class)
                subclass(ElevationAnimApplier::class)
                subclass(FadeAnimApplier::class)
                subclass(LetterSpacingAnimApplier::class)
                subclass(MoveAnimApplier::class)
                subclass(MoveToXAnimApplier::class)
                subclass(MoveToYAnimApplier::class)
                subclass(MoveInnerAnimApplier::class)
                subclass(RadiusAnimApplier::class)
                subclass(RotateAnimApplier::class)
                subclass(ScaleInnerAnimApplier::class)
                subclass(ScaleAnimSerializer)
                subclass(SizeAnimApplier::class)
                subclass(ToneAnimApplier::class)
                subclass(BrushAnimApplier::class)
                subclass(BackgroundFadeAnimApplier::class)
                subclass(RotateShapeAnimApplier::class)
            }

            polymorphic(Media::class) {
                subclass(MediaTextSerializer)
                subclass(MediaImageSerializer)
                subclass(MediaVectorSerializer)
                subclass(MediaGroupSerializer)
                subclass(MediaPathSerializer)
                subclass(MediaTextureSerializer)
            }

            polymorphic(InspInterpolator::class) {
                subclass(InspSpringInterpolator::class)
                subclass(InspPathInterpolator::class)
                subclass(InspDecelerateInterpolator::class)
                subclass(InspAccelerateInterpolator::class)
                subclass(InspOvershootInterpolator::class)
            }
        }
    }
}
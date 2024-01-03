package app.inspiry.views.factory

import android.content.Context
import app.inspiry.animator.helper.AnimationHelperAndroid
import app.inspiry.font.provider.FontsManager
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.MediaText
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.nullOrTrue
import app.inspiry.views.InspParent
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.text.InnerTextHolderAndroid
import app.inspiry.views.text.InspTextView
import app.inspiry.views.touch.MovableTouchHelperAndroid
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.viewplatform.ViewPlatformAndroid

class AndroidTextFactory(val context: Context) : InspViewFactory<MediaText, InspTextView> {

    override fun create(
        media: MediaText,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspTextView {

        val innerHolder = InnerTextHolderAndroid(context, media, unitsConverter)

        val viewPlatform = ViewPlatformAndroid(innerHolder)
        viewPlatform.viewForBackground = innerHolder.textView

        val animationHelper = AnimationHelperAndroid(media, innerHolder)

        val inspView = InspTextView(
            media,
            parentInsp,
            viewPlatform,
            unitsConverter,
            animationHelper,
            fontsManager,
            innerHolder,
            loggerGetter,
            movableTouchHelperFactory,
            templateView
        )

        innerHolder.textView.setStartTimeSource { inspView.getStartFrameShortCut() }
        innerHolder.textView.setDurationSource { inspView.duration }

        animationHelper.inspView = inspView

        innerHolder.drawListener = {
            animationHelper.drawAnimations(it, inspView.currentFrame)
        }
        if (media.duplicate == null)
            innerHolder.movableTouchHelper =
                inspView.movableTouchHelper as? MovableTouchHelperAndroid?

        innerHolder.canDraw = {
            inspView.templateParentNullable?.isInitialized?.value.nullOrTrue()
        }

        return inspView
    }
}
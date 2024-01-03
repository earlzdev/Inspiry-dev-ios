package app.inspiry.views.group

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.Surface
import android.view.ViewGroup
import app.inspiry.core.animator.helper.AbsAnimationHelper
import app.inspiry.helpers.K
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.MediaGroup
import app.inspiry.core.media.nullOrFalse
import app.inspiry.core.log.LoggerGetter
import app.inspiry.utils.TAG_TEMPLATE
import app.inspiry.views.InspParent
import app.inspiry.views.InspView
import app.inspiry.views.media.InnerMediaViewAndroid
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.RecordMode
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.viewplatform.ViewPlatformAndroid
import app.inspiry.views.viewplatform.getAndroidView

class InspGroupViewAndroid(
    media: MediaGroup,
    parentInsp: InspParent?,
    view: ViewPlatformAndroid?,
    unitsConverter: BaseUnitsConverter,
    animationHelper: AbsAnimationHelper<*>?,
    private val innerGroupViewAndroid: InnerGroupViewAndroid, loggerGetter: LoggerGetter,
    touchHelperFactory: MovableTouchHelperFactory, templateParent: InspTemplateView
) : InspGroupView(
    media, parentInsp, view, unitsConverter, animationHelper,
    loggerGetter, touchHelperFactory, templateParent
) {
    override fun setInnerCornerRadius(radius: Float) {
        super.setInnerCornerRadius(radius)
        innerGroupViewAndroid.cornerRadius = radius
    }

    private var needToSetBgForEditableProgram = false
    private var redrawProgramOnInvalidate = false

    private fun getAndroidViewGroup(): ViewGroup {
        return (view as ViewPlatformAndroid).view as ViewGroup
    }

    override fun addViewToHierarchy(view: InspView<*>) {
        getAndroidViewGroup().addView(view.getAndroidView())
        super.addViewToHierarchy(view)
    }

    override fun addViewToHierarchy(index: Int, view: InspView<*>) {
        getAndroidViewGroup().addView(view.getAndroidView(), index)
        super.addViewToHierarchy(index, view)
    }

    override fun removeViewFromHierarchy(view: InspView<*>, removeFromTemplateViews: Boolean) {
        getAndroidViewGroup().removeView(view.getAndroidView())
        super.removeViewFromHierarchy(view, removeFromTemplateViews)
    }

    /**
     * @param delay - delay before invalidate
     * @param instantly - instantly updating surface without invalidate
     * @param videoMatrixChanged - redrawProgram will be called if video texture has been changed
     *                             (bugfix: lag when rotating, moving, scaling the texture by the user)
     */
    override fun invalidateRedrawProgram(delay: Long, instantly: Boolean) {
        getAndroidViewGroup().postDelayed({

            //we don't need it anymore, since the program will be redrawn automatically after some delay.
            //only that it can speed up things.
            if (ALWAYS_REDRAW_PROGRAM_ON_INVALIDATE)
                redrawProgramOnInvalidate = true

            if (instantly) mayDrawOnGlCanvas(false)
            else
                view?.invalidate()
        }, delay)
    }

    private fun initNeedToSetBgEditableProgram() {

        if (media.textureIndex == null) return
        val templateParent = templateParentNullable ?: return

        if (!needToSetBgForEditableProgram) {

            val mediaWithProgram: InspMediaView? =
                templateParent.mediaViews.find { it.media.hasProgram() }
            if (mediaWithProgram?.media?.hasEditableProgram() == true &&
                (mediaWithProgram.innerMediaView as InnerMediaViewAndroid).isRenderingPrepared.nullOrFalse()
            ) {
                needToSetBgForEditableProgram = true
            }
        }
    }

    override fun refreshBackgroundColor() {
        initNeedToSetBgEditableProgram()

        if (!needToSetBgForEditableProgram)
            super.refreshBackgroundColor()
    }

    override fun mayDrawOnGlCanvas(fromOnDraw: Boolean): Boolean {

        if (media.textureIndex == null) {
            return false
        }

        val templateView = templateParentNullable

        //because we have drawn it already in ThreadRecord.
        if (templateView?.recordMode == RecordMode.VIDEO && fromOnDraw)
            return true

        val mediaWithProgramm = templateView?.mediaViews?.filter { it.media.hasProgram() }
        var result = false
        mediaWithProgramm?.forEach {

            val innerMediaViewAndroid = (it.innerMediaView as InnerMediaViewAndroid)

            if (!it.media.hasEditableProgram() || !innerMediaViewAndroid.isRenderingPrepared.nullOrFalse()) {

                if (needToSetBgForEditableProgram) {
                    needToSetBgForEditableProgram = false
                    super.refreshBackgroundColor()
                }

                val glSurface: Surface? =
                    innerMediaViewAndroid.getSurfaceForTemplate(this, media.textureIndex!!)

                if (glSurface != null && glSurface.isValid) {
                    val newCanvas = glSurface.lockHardwareCanvas()
                    //clear previous color, bug on some devices.
                    newCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                    innerGroupViewAndroid.originalDraw(newCanvas)
                    glSurface.unlockCanvasAndPost(newCanvas)

                    if (redrawProgramOnInvalidate) {
                        redrawProgramOnInvalidate = false
                        innerMediaViewAndroid.redrawProgram()
                    }

                } else {
                    K.i(TAG_TEMPLATE) {
                        "mayDrawOnGlCanvas cant glSurfaceValid ${glSurface?.isValid}"
                    }
                }

                result = true
            }
        }
        return result
    }

    companion object {
        //if FALSE, will be bug when editing images/video with a texture
        const val ALWAYS_REDRAW_PROGRAM_ON_INVALIDATE = true
    }
}
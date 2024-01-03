package app.inspiry.slide.model

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.util.PickMediaResult
import app.inspiry.edit.instruments.BottomInstrumentsViewModel
import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.template.doWhenInitializedOnce
import kotlinx.coroutines.flow.MutableStateFlow

class SlideInstrumentViewModel(
    val inspView: InspMediaView,
    val analyticsManager: AnalyticsManager
) : BottomInstrumentsViewModel {
    val slideList =
        MutableStateFlow<List<String>>(emptyList())
    val currentView = MutableStateFlow(inspView)
    val selectedSlideID = MutableStateFlow<String?>(null)
    val slidesParent = inspView.getSlidesParent()
    init {
        updateSlideList()
    }

    private fun updateSlideList() {
        slideList.value =
            slidesParent.getSlidesMedia().map {
                it.id ?: throw IllegalStateException("media image without ID! (slides view model)")
            }
        currentView.value.showOnTopForEdit()
        selectedSlideID.value = currentView.value.media.id
    }

    fun getUrlByID(id: String): String {
        return getViewById(id).media.originalSource
            ?: throw IllegalStateException("unknown originalSource for ID ($id)")
    }

    fun getUrlOrNull(id: String): String? {
        val view = slidesParent.getSlidesViews().find { it.media.id == id }
        return view?.media?.originalSource
    }

    private fun getViewById(id: String): InspMediaView {
        return slidesParent.getSlidesViews().find { it.media.id == id }
            ?: throw IllegalStateException("unknown view for ID ($id)")
    }

    fun setSelected(id: String) {
        getViewById(id).setSelected()
    }

    fun replaceSlides(id: String, newIndex: Int, onFinished: () -> Unit = {}) {
        //todo get slideUtilites from DI
        currentView.value.templateParent.slideUtilities.moveSlides(slidesParent, id, newIndex, onFinished = onFinished)

    }
    fun emptySlidesCount(): Int {
        return slidesParent.maxSlides - slideList.value.size
    }

    fun onNewSlideAppend(newSlides: List<PickMediaResult>) {
        currentView.value.templateParent.slideUtilities.appendNewSlides(
            slidesParent = slidesParent,
            source = newSlides
        ) {
            currentView.value.templateParent.doWhenInitializedOnce {
                slidesParent.selectLastSlide()
                analyticsManager.onSlideAdded(slidesParent.getSlidesCount(true))
            }
        }
    }

    override fun onSelectedViewChanged(newSelected: InspView<*>?) {
        if (newSelected?.isInSlides() != true) return
        currentView.value = newSelected as? InspMediaView ?: return
        updateSlideList()
    }
}
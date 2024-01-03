package app.inspiry.views.template

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import app.inspiry.core.manager.AppViewModel
import app.inspiry.views.group.BaseGroupZView
import app.inspiry.views.infoview.InfoViewColors
import app.inspiry.views.infoview.InfoViewColorsDark
import app.inspiry.views.infoview.InfoViewModel
import app.inspiry.views.infoview.InfoViewModelImpl
import org.koin.androidx.scope.activityScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object InspTemplateViewCreator : KoinComponent {

    fun createInspTemplateView(
        innerView: BaseGroupZView,
        initialTemplateMode: TemplateMode = TemplateMode.PREVIEW,
        colors: InfoViewColors = InfoViewColorsDark(),
        displayInfoText: Boolean = true,
        canShowProgress: Boolean = true
    ): InspTemplateView {

        val activityScope = (innerView.context as ComponentActivity).lifecycleScope

        val infoViewModel: InfoViewModel? = if (canShowProgress) InfoViewModelImpl(activityScope) else null

        val templateView = InspTemplateViewAndroid(
            innerView, get(), get(), get(), infoViewModel, get(), get(), get(), get(), get(), initialTemplateMode
        )

        if (infoViewModel != null) {
            val infoViewHelperAndroid = InfoViewHelperAndroid(
                templateView,
                innerView,
                infoViewModel,
                colors,
                displayInfoText
            )
            infoViewHelperAndroid.collectChanges(activityScope)
        }

        return templateView
    }
}

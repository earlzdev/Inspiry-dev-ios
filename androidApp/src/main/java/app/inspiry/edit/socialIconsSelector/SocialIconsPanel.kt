package app.inspiry.edit.socialIconsSelector

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import app.inspiry.R
import app.inspiry.core.util.getDefaultViewContainer
import app.inspiry.music.InstrumentViewAndroid
import app.inspiry.utils.getColorCompat
import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.template.TemplateEditIntent
import app.inspiry.views.vector.InspVectorView
import org.koin.core.component.KoinComponent

class SocialIconsPanel(val viewModel: SocialIconsViewModel, val activity: AppCompatActivity) :
    InstrumentViewAndroid, KoinComponent {

    private val viewController: SocialIconsViewController = SocialIconsViewController(
        activity,
        viewModel,
        createCallbacksForMovableIcons(viewModel.currentView) {
            viewModel.currentView.templateParent.editAction(
                TemplateEditIntent.PICK_IMAGE,
                it
            )
        }
    )

    private fun onSelectedViewChanged(newSelected: InspView<*>) {
        viewController.callbacks =
            createCallbacksForMovableIcons(newSelected) {
                newSelected.templateParent.editAction(
                    TemplateEditIntent.PICK_IMAGE,
                    it
                )
            }
        viewController.showIconList()

    }

    override fun createView(context: Context): View {
        val v = viewController.initView(LayoutInflater.from(context), context.getDefaultViewContainer())

        v.setBackgroundColor(context.getColorCompat(R.color.edit_instruments_bg))
        v.setOnClickListener { }
        viewModel.onSelectedChanged = { onSelectedViewChanged(it) }
        viewController.onViewCreated()

        return v
    }
}


private fun createCallbacksForMovableIcons(
    view: InspView<*>,
    pickNewImage: (InspView<*>?) -> Unit
) =
    object :
        IconsDialogCallbacks {
        override fun applySocialIcon(newIconPath: String): InspView<*>? {
            when (view) {
                is InspVectorView -> {
                    view.media.originalSource = newIconPath
                    view.refresh()
                    view.templateParent.isChanged.value = true

                }
                is InspMediaView -> {
                    return view.templateParent.replaceMediaWithVector(
                        view, newIconPath
                    )

                }
            }
            return null
        }

        //pick image for social icon
        override fun pickNewImage() {
            pickNewImage(view)
        }

        override fun disableSocialIcon() {
            applySocialIcon("")
        }
    }

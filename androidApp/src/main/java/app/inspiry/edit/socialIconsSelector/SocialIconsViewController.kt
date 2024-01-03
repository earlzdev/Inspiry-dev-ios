package app.inspiry.edit.socialIconsSelector

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.inspiry.databinding.DialogIconsBinding
import app.inspiry.core.log.KLogger
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf


class SocialIconsViewController(
    val activity: AppCompatActivity,
    val viewModel: SocialIconsViewModel,
    var callbacks: IconsDialogCallbacks?
) : KoinComponent {

    private val logger: KLogger by inject {
        parametersOf("IconsViewController")
    }
    lateinit var binding: DialogIconsBinding

    fun initView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): View {

        binding = DialogIconsBinding.inflate(inflater, container, false)

        return binding.root
    }
    fun showIconList() {
        binding.recyclerView.adapter =
            SocialIconsAdapter(
                viewModel.getIconsPath(),
                activity,
                ::onIconSelected,
                ::onPickNewImage,
                ::onDisableIcon)
        logger.debug { "show list" }
    }

    private fun onPickNewImage() {
        callbacks?.pickNewImage()
    }

    private fun onIconSelected(iconPath: String?) {
        viewModel.currentStickerPath.value = iconPath
    }

    private fun onDisableIcon() {
        viewModel.currentStickerPath.value = null
        callbacks?.disableSocialIcon()
    }

    fun onViewCreated() {

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        showIconList()
        activity.lifecycleScope.launch {
            viewModel.currentStickerPath.collect {
                it?.let { path ->
                    callbacks?.applySocialIcon(path)?.let { newView ->
                        viewModel.onSelectedChanged?.invoke(newView)
                    }

                }
            }
        }
    }
}
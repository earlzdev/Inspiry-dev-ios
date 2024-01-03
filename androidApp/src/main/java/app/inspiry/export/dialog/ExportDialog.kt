package app.inspiry.export.dialog

import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import app.inspiry.R
import app.inspiry.export.viewmodel.KEY_IMAGE_ELSE_VIDEO
import app.inspiry.utilities.putArgs
import app.inspiry.utilities.toCColor
import coil.ImageLoader
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

@Deprecated("use compose ui")
class ExportDialog : BottomSheetDialogFragment() {

    private lateinit var dialogViewModel: ExportDialogViewModel

    //if return true, then don't share it. String - packageName, activityName
    var exportListener: ((ResolveInfo) -> Unit)? = null

    override fun getTheme(): Int {
        return R.style.ShareDialogTheme
    }

    private val imageLoader: ImageLoader by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        dialogViewModel = ViewModelProvider(
            this, ExportDialogViewModelFactory(
                get(),
                requireContext().applicationContext.packageManager
            )
        )[ExportDialogViewModel::class.java]

        val imageElseVideo = requireArguments().getBoolean(KEY_IMAGE_ELSE_VIDEO)

        return ComposeView(requireContext()).also {
            it.setContent {

                val colors = LocalColors.current
                MaterialTheme(
                    colors = MaterialTheme.colors.copy(
                        isLight = colors.isLight,
                        background = colors.bg.toCColor()
                    )
                ) {
                    ExportDialogMainUI(
                        viewModel = dialogViewModel,
                        imageElseVideo = imageElseVideo,
                        imageLoader = imageLoader
                    ) { item ->

                        this@ExportDialog.dismissAllowingStateLoss()
                        exportListener?.invoke(item)
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(imageElseVideo: Boolean): ExportDialog {
            return ExportDialog().putArgs {
                putBoolean(KEY_IMAGE_ELSE_VIDEO, imageElseVideo)
            }
        }
    }
}

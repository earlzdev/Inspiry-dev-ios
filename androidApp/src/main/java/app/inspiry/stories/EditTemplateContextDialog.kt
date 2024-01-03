package app.inspiry.stories

import android.content.DialogInterface
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.TextView
import app.inspiry.R
import app.inspiry.databinding.DialogEditTemplateBinding
import app.inspiry.utils.dpToPixels
import app.inspiry.utils.dpToPxInt
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EditTemplateContextDialog : BottomSheetDialogFragment() {

    //Int - Action
    lateinit var editTemplateListener: (Int) -> Unit
    lateinit var onDismissListener: () -> Unit
    lateinit var binding: DialogEditTemplateBinding
    var showOnlyDelete = false

    override fun getTheme(): Int {
        return R.style.ContextDialogTheme
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener.invoke()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onDismissListener.invoke()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DialogEditTemplateBinding.inflate(inflater, container, false)
        binding.container.clipToOutline = true
        binding.container.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(
                    Rect(0, 0, binding.container.width, binding.container.height + 40.dpToPxInt()),
                    40.dpToPixels()
                )
                outline.alpha = 1f
            }
        }
        binding.container.elevation = 8f.dpToPixels()
        binding.container.translationZ = 4f.dpToPixels()

        if (!showOnlyDelete) {
            binding.linearItems.addView(
                createItem(
                    app.inspiry.projectutils.R.string.copy_action,
                    ACTION_COPY
                )
            )
            binding.linearItems.addView(
                createItem(
                    app.inspiry.projectutils.R.string.rename,
                    ACTION_RENAME
                )
            )
        }
        binding.linearItems.addView(
            createItem(
                app.inspiry.projectutils.R.string.delete,
                ACTION_DELETE
            )
        )

        return binding.root
    }

    private fun createItem(text: Int, action: Int): View {
        val v = TextView(requireContext())
        v.setTextColor(0xff333333.toInt())
        v.setText(text)
        v.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        v.gravity = Gravity.CENTER
        v.setBackgroundResource(R.drawable.edit_context_bg)
        v.setOnClickListener { editTemplateListener(action) }
        v.textSize = 18f

        val lp = LinearLayout.LayoutParams(MATCH_PARENT, 40.dpToPxInt())
        lp.topMargin = 5.dpToPxInt()
        lp.bottomMargin = 5.dpToPxInt()

        v.layoutParams = lp

        return v
    }

    companion object {
        const val ACTION_DELETE = 1
        const val ACTION_COPY = 2
        const val ACTION_RENAME = 3
    }
}
package app.inspiry.helpers

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Size
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import app.inspiry.R
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.palette.model.AbsPaletteColor
import app.inspiry.palette.model.PaletteColor
import app.inspiry.palette.util.getDrawableForList
import app.inspiry.utils.ImageUtils
import app.inspiry.utils.dpToPxInt
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener

class GradientPaletteAdapter(
    val originals: List<AbsPaletteColor>,
    val activity: AppCompatActivity,
    val onClickListener: (AbsPaletteColor) -> Unit,
    val onPickedExactColor: ((Int) -> Unit)? = null,
    val onClickRemoveOption: (() -> Unit)? = null,
    var selectedPosition: Int = -1,
    val useCheckForSelection: Boolean = true,
    val itemSize: Size = Size(
        activity.resources.getDimensionPixelSize(com.jaredrummler.android.colorpicker.R.dimen.cpv_item_size),
        activity.resources.getDimensionPixelSize(com.jaredrummler.android.colorpicker.R.dimen.cpv_item_size)
    )
) : RecyclerView.Adapter<GradientPaletteViewHolder>() {

    init {
        if (selectedPosition >= 0) selectedPosition += getItemsOffset()
    }

    val colors = originals.toMutableList()

    fun removeSelection() {
        val prevSelected = selectedPosition
        selectedPosition = -1
        notifyItemChanged(prevSelected)
    }

    fun setSelectedItem(item: AbsPaletteColor) {
        val index = colors.indexOf(item)
        if (index != -1) {
            selectedPosition = index + getItemsOffset()
        } else {
            selectedPosition = -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradientPaletteViewHolder {

        val root = FrameLayout(parent.context)
        root.setPadding(8.dpToPxInt(), 0, 8.dpToPxInt(), 0)

        val colorView = View(parent.context)
        root.addView(colorView, FrameLayout.LayoutParams(itemSize.width, itemSize.height, Gravity.CENTER))

        val imageCheck: ImageView?
        if (useCheckForSelection) {
            imageCheck = ImageView(parent.context)
            root.addView(imageCheck, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER))
        } else imageCheck = null

        return GradientPaletteViewHolder(root, colorView, imageCheck)
    }

    private fun showColorPicker() {
        val currentColor =
            if (selectedPosition == -1) Color.WHITE else colors[selectedPosition - getItemsOffset()].getFirstColor()

        val dialog = ColorPickerDialog.newBuilder()
            .setShowAlphaSlider(false).setColor(ArgbColorManager.colorWithoutAlpha(currentColor)).create()
        dialog.colorPickerDialogListener = object : ColorPickerDialogListener {

            var lastColorChanged = 0
            override fun onColorChanged(dialogId: Int, newColor: Int) {

                onPickedExactColor?.invoke(newColor)
                lastColorChanged = newColor
            }

            override fun onColorSelected(dialogId: Int, color: Int) {}

            override fun onDialogDismissed(dialogId: Int) {

                if (lastColorChanged != 0) {
                    val newColorPalette = PaletteColor(lastColorChanged)

                    if (!colors.contains(newColorPalette)) {
                        colors.clear()
                        colors.add(newColorPalette)
                        colors.addAll(originals)
                    }
                    setSelectedItem(newColorPalette)
                    notifyDataSetChanged()
                }
            }
        }
        dialog.show(activity.supportFragmentManager, "color-picker-dialog")
    }

    override fun onBindViewHolder(holder: GradientPaletteViewHolder, position: Int) {
        if (position == 0 && onPickedExactColor != null) {
            holder.itemView.setOnClickListener {
                showColorPicker()
            }
            holder.imageCheck?.setImageResource(0)
            holder.gradientView.isActivated = false
            holder.gradientView.background = ImageUtils.generatePickExactColorDrawable()

        } else if ((position == 1 && onPickedExactColor != null && onClickRemoveOption != null) ||
            (position == 0 && onClickRemoveOption != null && onPickedExactColor == null)
        ) {

            holder.imageCheck?.setImageResource(0)
            holder.gradientView.isActivated = false
            holder.gradientView.background =
                activity.getDrawable(R.drawable.ic_remove_color)
            holder.itemView.setOnClickListener {
                removeSelection()
                onClickRemoveOption.invoke()
            }
        } else {

            val itemPosition = position - getItemsOffset()
            val gradient = colors[itemPosition]

            holder.itemView.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = position
                if (oldPosition != -1) notifyItemChanged(oldPosition)
                notifyItemChanged(position)
                onClickListener(gradient)
            }
            val drawable = gradient.getDrawableForList()
            holder.gradientView.background = drawable

            holder.gradientView.isActivated = position == selectedPosition

            if (holder.imageCheck != null) {
                if (position == selectedPosition) {

                    holder.imageCheck.setImageResource(if (selectedPosition == position) com.jaredrummler.android.colorpicker.R.drawable.cpv_preset_checked else 0)

                    if (ColorUtils.calculateLuminance(gradient.getFirstColor()) >= 0.65) {
                        holder.imageCheck.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
                    } else {
                        holder.imageCheck.colorFilter = null
                    }

                } else {
                    holder.imageCheck.setImageResource(0)
                }
            }
        }
    }

    private fun getItemsOffset(): Int {
        var offset = 0
        if (onPickedExactColor != null)
            offset++
        if (onClickRemoveOption != null) {
            offset++
        }
        return offset
    }

    override fun getItemCount() = colors.size + getItemsOffset()
}

class GradientPaletteViewHolder(itemView: View, val gradientView: View, val imageCheck: ImageView?) : RecyclerView.ViewHolder(itemView)
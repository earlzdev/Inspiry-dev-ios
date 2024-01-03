/*
 * Copyright (C) 2017 Jared Rummler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaredrummler.android.colorpicker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*

/**
 *
 * A dialog to pick a color.
 *
 *
 * The [activity][Activity] that shows this dialog should implement [ColorPickerDialogListener]
 *
 *
 * Example usage:
 *
 * <pre>
 * ColorPickerDialog.newBuilder().show(activity);
</pre> *
 */
class ColorPickerDialog : BottomSheetDialogFragment(), ColorPickerView.OnColorChangedListener,
    TextWatcher {
    var rootView: LinearLayout? = null
    lateinit var presets: IntArray

    @ColorInt
    var color = 0
    var dialogId = 0
    var showColorShades = false
    var colorShape = 0

    // -- CUSTOM ---------------------------
    var colorPicker: ColorPickerView? = null
    var newColorPanel: OneColorView? = null
    var hexEditText: EditText? = null
    var showAlphaSlider = false
    private var presetsButtonStringRes = 0
    private var fromEditText = false
    private var customButtonStringRes = 0


    fun AppCompatDialogFragment.bindToActivityLifecycle() {
        requireActivity().doOnStop {
            dismissAllowingStateLoss()
        }
    }

    fun LifecycleOwner.doOnStop(action: () -> Unit) {
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(owner: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_STOP) {
                    action()
                    lifecycle.removeObserver(this)
                }
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private val onPickerTouchListener = OnTouchListener { v, event ->
        if (v !== hexEditText && hexEditText!!.hasFocus()) {
            hexEditText!!.clearFocus()
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(hexEditText!!.windowToken, 0)
            hexEditText!!.clearFocus()
            return@OnTouchListener true
        }
        false
    }
    var showButtons = false


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        bindToActivityLifecycle()
        dialogId = requireArguments().getInt(ARG_ID)
        showAlphaSlider = requireArguments().getBoolean(ARG_ALPHA)
        showColorShades = requireArguments().getBoolean(ARG_SHOW_COLOR_SHADES)
        if (savedInstanceState == null) {
            color = requireArguments().getInt(ARG_COLOR)
        } else {
            color = savedInstanceState.getInt(ARG_COLOR)
        }
        rootView = LinearLayout(requireActivity())
        rootView!!.orientation = LinearLayout.VERTICAL
        rootView!!.addView(createPickerView())

        if (showButtons) {
            var selectedButtonStringRes = requireArguments().getInt(ARG_SELECTED_BUTTON_TEXT)
            if (selectedButtonStringRes == 0) {
                selectedButtonStringRes = R.string.cpv_select
            }
            val padding = (resources.displayMetrics.density * 20).toInt()
            val buttonsLayout = LinearLayout(requireActivity())
            buttonsLayout.orientation = LinearLayout.HORIZONTAL
            val positiveButton = AppCompatButton(ContextThemeWrapper(requireActivity(),
                androidx.appcompat.R.style.Widget_AppCompat_Button_ButtonBar_AlertDialog), null, 0)
            positiveButton.setText(selectedButtonStringRes)
            positiveButton.setOnClickListener {
                onColorSelected(color)
                dismissAllowingStateLoss()
            }
            positiveButton.setPaddingRelative(padding, 0, padding, 0)
            presetsButtonStringRes = requireArguments().getInt(ARG_PRESETS_BUTTON_TEXT)
            customButtonStringRes = requireArguments().getInt(ARG_CUSTOM_BUTTON_TEXT)
            val frameNeutral = FrameLayout(requireActivity())

            val lpNeutral = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            buttonsLayout.addView(frameNeutral, lpNeutral)
            val positiveLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            buttonsLayout.addView(positiveButton, positiveLp)
            val buttonsLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            buttonsLp.bottomMargin = (resources.displayMetrics.density * 5).toInt()
            rootView!!.addView(buttonsLayout, buttonsLp)
        }
        return rootView
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog

        // http://stackoverflow.com/a/16972670/1048340
        dialog!!.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDialogDismissed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ARG_COLOR, color)
        super.onSaveInstanceState(outState)
    }

    // region Custom Picker
    fun createPickerView(): View {
        val contentView = View.inflate(activity, R.layout.cpv_dialog_color_picker, null)
        colorPicker = contentView.findViewById<View>(R.id.cpv_color_picker_view) as ColorPickerView
        val oldColorPanel = contentView.findViewById<View>(R.id.cpv_color_panel_old) as OneColorView
        newColorPanel = contentView.findViewById<View>(R.id.cpv_color_panel_new) as OneColorView
        hexEditText = contentView.findViewById<View>(R.id.cpv_hex) as EditText

        colorPicker!!.setAlphaSliderVisible(showAlphaSlider)
        oldColorPanel.setColor(requireArguments().getInt(ARG_COLOR))
        colorPicker!!.setColor(color, true)
        newColorPanel!!.setColor(color)
        setHex(color)
        if (!showAlphaSlider) {
            hexEditText!!.filters = arrayOf<InputFilter>(LengthFilter(6))
        }
        newColorPanel!!.setOnClickListener {
            if (newColorPanel!!.getColor() == color) {
                onColorSelected(color)
                dismiss()
            }
        }
        contentView.setOnTouchListener(onPickerTouchListener)
        colorPicker!!.setOnColorChangedListener(this)
        hexEditText!!.addTextChangedListener(this)
        hexEditText!!.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(hexEditText, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        return contentView
    }

    fun Int.colorWithoutAlpha() = Color.rgb(Color.red(this), Color.green(this), Color.blue(this))

    override fun onColorChanged(newColor: Int) {
        color = newColor.colorWithoutAlpha()
        if (newColorPanel != null) {
            newColorPanel!!.setColor(newColor)
        }
        if (!fromEditText && hexEditText != null) {
            setHex(newColor)
            if (hexEditText!!.hasFocus()) {
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(hexEditText!!.windowToken, 0)
                hexEditText!!.clearFocus()
            }
        }
        fromEditText = false
        notifyColorChanged()
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable) {
        if (hexEditText!!.isFocused) {
            try {
                val color = parseColorString(s.toString())

                if (color != colorPicker!!.color) {
                    fromEditText = true
                    colorPicker!!.setColor(color, true)
                }
            } catch (ignored: NumberFormatException) {
                // nothing
            }
        }
    }

    private fun setHex(color: Int) {
        if (showAlphaSlider) {
            hexEditText!!.setText(String.format("%08X", color))
        } else {
            hexEditText!!.setText(String.format("%06X", 0xFFFFFF and color))
        }
    }

    @Throws(NumberFormatException::class)
    private fun parseColorString(colorString: String): Int {
        var colorString = colorString
        val a: Int
        var r: Int
        val g: Int
        var b = 0
        if (colorString.startsWith("#")) {
            colorString = colorString.substring(1)
        }
        if (colorString.length == 0) {
            r = 0
            a = 255
            g = 0
        } else if (colorString.length <= 2) {
            a = 255
            r = 0
            b = colorString.toInt(16)
            g = 0
        } else if (colorString.length == 3) {
            a = 255
            r = colorString.substring(0, 1).toInt(16)
            g = colorString.substring(1, 2).toInt(16)
            b = colorString.substring(2, 3).toInt(16)
        } else if (colorString.length == 4) {
            a = 255
            r = colorString.substring(0, 2).toInt(16)
            g = r
            r = 0
            b = colorString.substring(2, 4).toInt(16)
        } else if (colorString.length == 5) {
            a = 255
            r = colorString.substring(0, 1).toInt(16)
            g = colorString.substring(1, 3).toInt(16)
            b = colorString.substring(3, 5).toInt(16)
        } else if (colorString.length == 6) {
            a = 255
            r = colorString.substring(0, 2).toInt(16)
            g = colorString.substring(2, 4).toInt(16)
            b = colorString.substring(4, 6).toInt(16)
        } else if (colorString.length == 7) {
            a = colorString.substring(0, 1).toInt(16)
            r = colorString.substring(1, 3).toInt(16)
            g = colorString.substring(3, 5).toInt(16)
            b = colorString.substring(5, 7).toInt(16)
        } else if (colorString.length == 8) {
            a = colorString.substring(0, 2).toInt(16)
            r = colorString.substring(2, 4).toInt(16)
            g = colorString.substring(4, 6).toInt(16)
            b = colorString.substring(6, 8).toInt(16)
        } else {
            b = -1
            g = -1
            r = -1
            a = -1
        }
        return Color.argb(a, r, g, b)
    }




    var colorPickerDialogListener: ColorPickerDialogListener? = null

    private fun notifyColorChanged() {
        if (colorPickerDialogListener != null) {
            colorPickerDialogListener?.onColorChanged(dialogId, color)
        } else {
            val activity: Activity? = activity
            if (activity is ColorPickerDialogListener) {
                (activity as ColorPickerDialogListener).onColorChanged(dialogId, color)
            } else {
                throw IllegalStateException("The activity must implement ColorPickerDialogListener")
            }
        }
    }

    private fun onColorSelected(color: Int) {
        if (colorPickerDialogListener != null) {
            colorPickerDialogListener?.onColorSelected(dialogId, color)
        } else {
            val activity: Activity? = activity
            if (activity is ColorPickerDialogListener) {
                (activity as ColorPickerDialogListener).onColorSelected(dialogId, color)
            } else {
                throw IllegalStateException("The activity must implement ColorPickerDialogListener")
            }
        }
    }

    private fun onDialogDismissed() {
        if (colorPickerDialogListener != null) {
            colorPickerDialogListener?.onDialogDismissed(dialogId)
        } else {
            val activity: Activity? = activity
            if (activity is ColorPickerDialogListener) {
                (activity as ColorPickerDialogListener).onDialogDismissed(dialogId)
            }
        }
    }

    private fun shadeColor(@ColorInt color: Int, percent: Double): Int {
        val hex = String.format("#%06X", 0xFFFFFF and color)
        val f = hex.substring(1).toLong(16)
        val t: Double = (if (percent < 0) 0.0 else 255.toDouble())
        val p = if (percent < 0) percent * -1 else percent
        val R = f shr 16
        val G = f shr 8 and 0x00FF
        val B = f and 0x0000FF
        val alpha = Color.alpha(color)
        val red = (Math.round((t - R) * p) + R).toInt()
        val green = (Math.round((t - G) * p) + G).toInt()
        val blue = (Math.round((t - B) * p) + B).toInt()
        return Color.argb(alpha, red, green, blue)
    }

    private fun getColorShades(@ColorInt color: Int): IntArray {
        return intArrayOf(shadeColor(color, 0.9),
            shadeColor(color, 0.7),
            shadeColor(color, 0.5),
            shadeColor(color, 0.333),
            shadeColor(color, 0.166),
            shadeColor(color, -0.125),
            shadeColor(color, -0.25),
            shadeColor(color, -0.375),
            shadeColor(color, -0.5),
            shadeColor(color, -0.675),
            shadeColor(color, -0.7),
            shadeColor(color, -0.775))
    }


    private fun unshiftIfNotExists(array: IntArray?, value: Int): IntArray {
        var present = false
        for (i in array!!) {
            if (i == value) {
                present = true
                break
            }
        }
        if (!present) {
            val newArray = IntArray(array.size + 1)
            newArray[0] = value
            System.arraycopy(array, 0, newArray, 1, newArray.size - 1)
            return newArray
        }
        return array
    }

    private fun pushIfNotExists(array: IntArray?, value: Int): IntArray {
        var present = false
        for (i in array!!) {
            if (i == value) {
                present = true
                break
            }
        }
        if (!present) {
            val newArray = IntArray(array.size + 1)
            newArray[newArray.size - 1] = value
            System.arraycopy(array, 0, newArray, 0, newArray.size - 1)
            return newArray
        }
        return array
    }

    private val selectedItemPosition: Int
        private get() {
            for (i in presets.indices) {
                if (presets[i] == color) {
                    return i
                }
            }
            return -1
        }


    class Builder  /*package*/
    internal constructor() {
        var colorPickerDialogListener: ColorPickerDialogListener? = null

        @StringRes
        var dialogTitle = R.string.cpv_default_title

        @StringRes
        var presetsButtonText = R.string.cpv_presets

        @StringRes
        var customButtonText = R.string.cpv_custom

        @StringRes
        var selectedButtonText = R.string.cpv_select

        @ColorInt
        var color = Color.BLACK
        var dialogId = 0
        var showAlphaSlider = false


        /**
         * Set the dialog title string resource id
         *
         * @param dialogTitle The string resource used for the dialog title
         * @return This builder object for chaining method calls
         */
        fun setDialogTitle(@StringRes dialogTitle: Int): Builder {
            this.dialogTitle = dialogTitle
            return this
        }

        /**
         * Set the selected button text string resource id
         *
         * @param selectedButtonText The string resource used for the selected button text
         * @return This builder object for chaining method calls
         */
        fun setSelectedButtonText(@StringRes selectedButtonText: Int): Builder {
            this.selectedButtonText = selectedButtonText
            return this
        }

        /**
         * Set the presets button text string resource id
         *
         * @param presetsButtonText The string resource used for the presets button text
         * @return This builder object for chaining method calls
         */
        fun setPresetsButtonText(@StringRes presetsButtonText: Int): Builder {
            this.presetsButtonText = presetsButtonText
            return this
        }

        /**
         * Set the custom button text string resource id
         *
         * @param customButtonText The string resource used for the custom button text
         * @return This builder object for chaining method calls
         */
        fun setCustomButtonText(@StringRes customButtonText: Int): Builder {
            this.customButtonText = customButtonText
            return this
        }

        /**
         * Set the original color
         *
         * @param color The default color for the color picker
         * @return This builder object for chaining method calls
         */
        fun setColor(color: Int): Builder {
            this.color = color
            return this
        }

        /**
         * Set the dialog id used for callbacks
         *
         * @param dialogId The id that is sent back to the [ColorPickerDialogListener].
         * @return This builder object for chaining method calls
         */
        fun setDialogId(dialogId: Int): Builder {
            this.dialogId = dialogId
            return this
        }

        /**
         * Show the alpha slider
         *
         * @param showAlphaSlider `true` to show the alpha slider. Currently only supported with the [                        ].
         * @return This builder object for chaining method calls
         */
        fun setShowAlphaSlider(showAlphaSlider: Boolean): Builder {
            this.showAlphaSlider = showAlphaSlider
            return this
        }

        /**
         * Create the [ColorPickerDialog] instance.
         *
         * @return A new [ColorPickerDialog].
         * @see .show
         */
        fun create(): ColorPickerDialog {
            val dialog = ColorPickerDialog()
            val args = Bundle()
            args.putInt(ARG_ID, dialogId)
            args.putInt(ARG_COLOR, color)
            args.putBoolean(ARG_ALPHA, showAlphaSlider)
            args.putInt(ARG_DIALOG_TITLE, dialogTitle)
            args.putInt(ARG_PRESETS_BUTTON_TEXT, presetsButtonText)
            args.putInt(ARG_CUSTOM_BUTTON_TEXT, customButtonText)
            args.putInt(ARG_SELECTED_BUTTON_TEXT, selectedButtonText)
            dialog.arguments = args
            return dialog
        }

        /**
         * Create and show the [ColorPickerDialog] created with this builder.
         *
         * @param activity The current activity.
         */
        fun show(activity: FragmentActivity) {
            create().show(activity.supportFragmentManager, "color-picker-dialog")
        }
    } // endregion

    companion object {
        private const val TAG = "ColorPickerDialog"
        private const val ARG_ID = "id"
        private const val ARG_COLOR = "color"
        private const val ARG_ALPHA = "alpha"
        private const val ARG_DIALOG_TITLE = "dialogTitle"
        private const val ARG_SHOW_COLOR_SHADES = "showColorShades"
        private const val ARG_PRESETS_BUTTON_TEXT = "presetsButtonText"
        private const val ARG_CUSTOM_BUTTON_TEXT = "customButtonText"
        private const val ARG_SELECTED_BUTTON_TEXT = "selectedButtonText"

        /**
         * Create a new Builder for creating a [ColorPickerDialog] instance
         *
         * @return The [builder][Builder] to create the [ColorPickerDialog].
         */
        fun newBuilder(): Builder {
            return Builder()
        }
    }
}
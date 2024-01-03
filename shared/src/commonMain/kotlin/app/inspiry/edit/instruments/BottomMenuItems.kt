package app.inspiry.edit.instruments

import app.inspiry.MR
import app.inspiry.core.manager.DebugManager
import app.inspiry.core.ui.CommonMenu
import app.inspiry.edit.instruments.addViewsPanel.AddViewsInstruments
import app.inspiry.edit.instruments.defaultPanel.DefaultInstruments
import app.inspiry.edit.instruments.textPanel.TextInstruments

object BottomMenuItems {

     val menuAddViews
        get() = CommonMenu<AddViewsInstruments>().apply {
            setMenuItem(
                AddViewsInstruments.ADD_TEXT,
                MR.strings.instrument_text,
                "ic_default_text",
                mayBeSelected = false
            )

            setMenuItem(
                AddViewsInstruments.ADD_STICKER,
                MR.strings.instrument_stickers,
                "ic_default_stickers",
                mayBeSelected = false
            )

            setMenuItem(
                AddViewsInstruments.ADD_LOGO,
                MR.strings.instrument_logo,
                "ic_add_logo",
                mayBeSelected = false
            )
            setMenuItem(
                AddViewsInstruments.ADD_FRAME,
                MR.strings.instrument_media,
                "ic_media",
                mayBeSelected = false
            )
        }

    val menuText
        get() = CommonMenu<TextInstruments>().apply {
            setMenuItem(
                TextInstruments.TEXT_ANIMATION,
                MR.strings.instrument_text_animation,
                "ic_instrument_animation",
                mayBeSelected = false
            )

            setMenuItem(
                TextInstruments.TEXT_COLOR,
                MR.strings.instrument_text_color,
                "ic_instrument_color",
                mayBeSelected = true
            )

            setMenuItem(
                TextInstruments.TEXT_BACKGROUND,
                MR.strings.instrument_text_back,
                "ic_instrument_back",
                mayBeSelected = true
            )

            setMenuItem(
                TextInstruments.TEXT_FONT,
                MR.strings.instrument_text_font,
                "ic_instrument_font",
                mayBeSelected = true
            )

            setMenuItem(
                TextInstruments.TEXT_SIZE,
                MR.strings.instrument_text_size,
                "ic_instrument_size",
                mayBeSelected = true
            )

            setMenuItem(
                TextInstruments.TEXT_ALIGNMENT,
                MR.strings.instrument_text_align,
                "ic_instrument_align_center",
                mayBeSelected = false
            )

            setMenuItem(
                TextInstruments.TEXT_LAYERS,
                MR.strings.instrument_timeline,
                "ic_default_time",
                mayBeSelected = false
            )
        }

    fun getMenuDefault(paletteEnabled: Boolean): CommonMenu<DefaultInstruments> {
        return CommonMenu<DefaultInstruments>().apply {
            setMenuItem(
                DefaultInstruments.DEFAULT_ADD,
                MR.strings.instrument_add,
                "ic_add_circle",
                mayBeSelected = false
            )
            setMenuItem(
                DefaultInstruments.DEFAULT_MUSIC,
                MR.strings.instrument_music,
                "ic_add_music",
                mayBeSelected = true
            )
            if (paletteEnabled) {
                setMenuItem(
                    DefaultInstruments.DEFAULT_COLOR,
                    MR.strings.instrument_text_color,
                    "ic_default_color",
                    mayBeSelected = true
                )
            }
            setMenuItem(
                DefaultInstruments.DEFAULT_LAYERS,
                MR.strings.instrument_timeline,
                "ic_default_time",
                mayBeSelected = false
            )
            setMenuItem(
                DefaultInstruments.DEFAULT_FORMAT,
                MR.strings.instrument_format,
                "ic_default_format",
                mayBeSelected = true
            )
            if (DebugManager.isDebug) {
                setMenuItem(
                    DefaultInstruments.DEFAULT_DEBUG,
                    MR.strings.edit_debug,
                    "ic_edit_static",
                    mayBeSelected = false
                )
            }
        }
    }
}
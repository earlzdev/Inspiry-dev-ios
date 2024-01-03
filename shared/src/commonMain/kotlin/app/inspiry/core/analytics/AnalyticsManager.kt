package app.inspiry.core.analytics

import app.inspiry.core.animator.clipmask.shape.ShapeType
import app.inspiry.music.model.TemplateMusic
import app.inspiry.core.data.OriginalTemplateData
import app.inspiry.core.media.Template
import app.inspiry.core.media.TemplateAvailability
import app.inspiry.core.util.getFileName
import app.inspiry.edit.instruments.moveAnimPanel.MoveAnimations
import com.russhwolf.settings.Settings
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import kotlin.math.roundToInt

fun MutableMap<String, Any?>.putBoolean(key: String, value: Boolean?) {
    put(key, value)
}

fun MutableMap<String, Any?>.putInt(key: String, value: Int?) {
    put(key, value)
}

fun MutableMap<String, Any?>.putString(key: String, value: String?) {
    put(key, value)
}

interface AnalyticsManager {

    fun sendEvent(
        eventName: String,
        outOfSession: Boolean = false,
        createParams: (MutableMap<String, Any?>.() -> Unit)? = null
    )

    fun setUserProperty(name: String, value: String)

    fun onStickerPicked(name: String, category: String, isPremium: Boolean) {
        sendEvent("sticker_picked", createParams = {
            putString("name", name)
            putString("category", category)
            putBoolean("is_premium", isPremium)
        })
    }
    fun onSlideAdded(slidesCount: Int) {
        sendEvent("slide_add") {
            putInt("slides_count", slidesCount)
        }
    }

    fun onMusicPickedFromLibrary(music: TemplateMusic?) {
        sendEvent("music_picked") {
            putString("title", music?.title)
            putString("artist", music?.artist)
            putString("album", music?.album)
            putString("tab", music?.tab?.name)
        }
    }

    fun onMusicEditDialogClose(music: TemplateMusic, initialStartTime: Long, initialVolume: Int) {
        if ((music.trimStartTime != initialStartTime || music.volume != initialVolume)) {
            sendEvent("music_edit") {
                if (music.trimStartTime != initialStartTime)
                    putInt("new_trim_start", (music.trimStartTime / 1000f).roundToInt())
                if (music.volume != initialVolume)
                    putInt("new_volume", music.volume)
            }
        }
    }

    fun onSubscribeClick(source: String, option: String) {
        sendEvent("click_subscribe", createParams = {
            putString("option", option)
            putString("source", source)
        })
    }

    fun onSubscribeScreenOpen(source: String) {
        sendEvent("open_subscribe_screen", createParams = {
            putString("source", source)
        })
    }

    fun onMediaAdded(isLogo: Boolean, isVideo: Boolean) {
        val eventName = if (isLogo) "logo_added" else "media_added"
        sendEvent(eventName) {
            if (isLogo) putBoolean("is_video", isVideo)
        }
    }

    fun onMediaSelected(isVideo: Boolean, mediaCount: Int, originalTemplateData: OriginalTemplateData) {
        sendEvent("media_selected") {
            putBoolean("has_video", isVideo)
            putInt("medias_count", mediaCount)
            originalTemplateData.toBundleAnalytics(this)
        }
    }

    fun onShapeChanged(shapeType: ShapeType) {
        sendEvent("shape_selected") {
            putString("shape", shapeType.name)
        }
    }
    fun onAnimationChanged(anim: MoveAnimations) {
        sendEvent("media_animation_selected") {
            putString("animation", anim.name)
        }
    }

    fun onNotificationOpened(id: String) {
        sendEvent("notification_opened") {
            putString("id", id)
        }
    }

    fun subscribeToInstClick(originalTemplateData: OriginalTemplateData?, source: String) {
        sendEvent("subscribe_to_inst_click", createParams = {
            putString("source", source)
            originalTemplateData?.toBundleAnalytics(this)
        })
    }

    fun onFontDialogClose(
        newFontPath: String?, startFontPath: String?, newFontStyle: String?,
        startFontStyle: String?, newCapsStyle: String, startCapsStyle: String,
        isPremium: Boolean, originalTemplateData: OriginalTemplateData, fontCategory: String
    ) {

        if (newFontPath != startFontPath || newFontStyle != startFontStyle || newCapsStyle != startCapsStyle) {

            sendEvent("text_font_changed") {

                if (newFontPath != startFontPath) {
                    putString("new_font_name", newFontPath?.getFileName() ?: "roboto")
                }

                if (newFontStyle != startFontStyle) {
                    putString("new_font_style", newFontStyle)
                }

                if (newCapsStyle != startCapsStyle) {
                    putString("new_caps_style", newCapsStyle)
                }

                putString("font_category", fontCategory)

                putBoolean("is_premium", isPremium)

                originalTemplateData.toBundleAnalytics(this)
            }
        }
    }

    fun customFontUploaded(name: String) {
        sendEvent("custom_font_uploaded", createParams = {
            putString("name", name)
        })
    }

    fun templateClick(template: Template, isStatic: Boolean) {
        sendEvent("template_click", createParams = {

            template.originalData!!.toBundleAnalytics(this)
            putBoolean("is_premium", template.availability == TemplateAvailability.PREMIUM)
            putBoolean("is_for_instagram", template.availability == TemplateAvailability.INSTAGRAM_SUBSCRIBED)
            putBoolean("animated_else_static", !isStatic)
        })
    }

    fun shareTemplate(
        activityName: String, fromDialog: Boolean, animatedElseStatic: Boolean, template: Template) {

        sendEvent(TEMPLATE_SHARE_ACTION) {
            putString("app_name", activityName)
            putBoolean("from_dialog", fromDialog)
            putBoolean("is_premium", template.availability == TemplateAvailability.PREMIUM)
            template.originalData!!.toBundleAnalytics(this)
            putBoolean("animated_else_static", animatedElseStatic)
            putString("format", template.format.analyticsName)
            putBoolean("has_music", template.music.let { it != null && it.volume > 0 })
        }
    }

    fun setDayAndSessionNums(settings: Settings, amplitudeAnalyticsManager: AmplitudeAnalyticsManager) {
        var sessionSinceFirstInstall = settings.getInt("session_num", 0)
        sessionSinceFirstInstall++
        settings.putInt("session_num", sessionSinceFirstInstall)

        setUserProperty(
            "day_since_first_install",
            daySinceFirstInstall(settings, amplitudeAnalyticsManager).toString()
        )
        setUserProperty("session_num", sessionSinceFirstInstall.toString())
    }

    fun onboardingOpen(name: String) {
        sendEvent("onboarding_open") {
            putString("name", name)
        }
    }
    private fun daySinceFirstInstall(settings: Settings, amplitudeAnalyticsManager: AmplitudeAnalyticsManager): Int {
        var timeOnFirstInstall = settings.getLong("time_on_first_install", 0L)
        val now = DateTime.now().unixMillisLong

        if (timeOnFirstInstall == 0L) {
            timeOnFirstInstall = now
            settings.putLong("time_on_first_install", timeOnFirstInstall)

            amplitudeAnalyticsManager.sendEvent("app_first_open")
        }

        val daySinceFirstInstall = (now - timeOnFirstInstall) / 1L.days.millisecondsLong

        return daySinceFirstInstall.toInt()
    }

    companion object {
        const val TEMPLATE_SHARE_ACTION = "template_share"
    }
}

package app.inspiry.core.manager

import kotlinx.coroutines.flow.StateFlow

interface InstagramSubscribeHolder {
    val subscribed: StateFlow<Boolean>
    fun setSubscribed()
}
const val INSTAGRAM_PROFILE_NAME = "in.spiry"
const val INSTAGRAM_DISPLAY_NAME = "@${INSTAGRAM_PROFILE_NAME}"
const val INSTAGRAM_APP_LINK = "instagram://user?username=$INSTAGRAM_PROFILE_NAME"
const val INSTAGRAM_PAGE_LINK = "https://instagram.com/$INSTAGRAM_PROFILE_NAME"
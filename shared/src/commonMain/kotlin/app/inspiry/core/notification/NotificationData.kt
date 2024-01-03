package app.inspiry.core.notification

import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.image.ImageDesc
import dev.icerock.moko.resources.desc.image.ImageDescResource

data class NotificationData(val type: NotificationType,
                            val title: StringResource,
                            val messageBody: StringResource,
                            val notificationChannelId: String,
                            val image: ImageResource? = null,
                            val sendWithSound: Boolean = false)
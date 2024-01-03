package app.inspiry.helpers.analytics

import com.adapty.Adapty
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AdaptyFirebaseMessagingService : FirebaseMessagingService() {

    private val pushHandler: MyAdaptyPushHandler by lazy {
        MyAdaptyPushHandler(this)
    }

    override fun onNewToken(pushToken: String) {
        super.onNewToken(pushToken)
        Adapty.refreshPushToken(pushToken)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (!pushHandler.handleNotification(message.data)) {
            /*
            here is your logic for other notifications
            that haven't been handled by Adapty
             */
        }
    }
}
package app.inspiry.export

actual object PredefineAppProvider {
    private const val INSTAGRAM_PACKAGE = "com.instagram.android"
    private const val FACEBOOK_PACKAGE = "com.facebook.katana"
    private const val WHATSAPP_PACKAGE = "com.whatsapp"

    actual val instagramPackage: String
        get() = INSTAGRAM_PACKAGE
    actual val facebookPackage: String
        get() = FACEBOOK_PACKAGE
    actual val whatsappPackage: String
        get() = WHATSAPP_PACKAGE
}
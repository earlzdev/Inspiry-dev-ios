package app.inspiry.export

actual object PredefineAppProvider {
    actual val instagramPackage: String
        get() = "instagram"
    actual val facebookPackage: String
        get() = "facebook"
    actual val whatsappPackage: String
        get() = "whatsapp"
}
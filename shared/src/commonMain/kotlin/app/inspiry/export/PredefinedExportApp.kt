package app.inspiry.export

data class PredefinedExportApp(
    val name: String,
    val iconRes: String,
    val whereToExport: WhereToExport
)

// TODO: mokoResources ImageResource is better, but doesn't work on ios. toUIImage() returns nil.
fun getDefaultPredefinedExportApps(): List<PredefinedExportApp> =
    listOf(
        PredefinedExportApp(
            "Instagram",
            "ic_export_inst",
            WhereToExport(PredefineAppProvider.instagramPackage)
        ),
        PredefinedExportApp(
            "WhatsApp",
            "ic_export_whatsapp",
            WhereToExport(PredefineAppProvider.whatsappPackage)
        ),
        PredefinedExportApp(
            "Facebook",
            "ic_export_facebook",
            WhereToExport(PredefineAppProvider.facebookPackage)
        ),
    )

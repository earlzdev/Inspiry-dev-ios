package app.inspiry.font.provider

import app.inspiry.MR
import app.inspiry.font.model.*
import app.inspiry.font.util.FontUtils
import app.inspiry.font.util.reversedMap
import app.inspiry.core.util.getFileName
import app.inspiry.core.util.removeScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FontsManager(
    private val isDebug: Boolean,
    private val platformFontPathProvider: PlatformFontPathProvider
) {

    private val alternativeCyrillicFontsMap: Map<String, PredefinedFontPath> by lazy {
        mapOf(
            "nunito" to PredefinedFontPath(
                "hero", regularId = MR.fonts.hero.regular,
                boldId = MR.fonts.hero.bold, lightId = MR.fonts.hero.light
            ),
            "cormorant" to PredefinedFontPath(
                "barkentina",
                regularId = MR.fonts.barkentina.regular
            ),
            "chalista" to PredefinedFontPath("roscherk", regularId = MR.fonts.roscherk.regular),
            "rustico" to PredefinedFontPath("rythmic", regularId = MR.fonts.rythmic.regular),
            "coconut" to PredefinedFontPath("misterk", regularId = MR.fonts.misterk.regular),
            "dk" to PredefinedFontPath(
                "beer_money",
                "Beer Money",
                regularId = MR.fonts.beer_money.regular
            ),
            "bradley" to PredefinedFontPath("voronov", regularId = MR.fonts.voronov.regular),
            "ancherr" to PredefinedFontPath(
                "tkachenko",
                regularId = MR.fonts.tkachenko.regular,
                boldId = MR.fonts.tkachenko.bold
            )
        )
    }


    private val scriptFonts by lazy {

        listOf(
            PredefinedFontPath("nautilus", regularId = MR.fonts.nautilus.regular),
            PredefinedFontPath("anastasia", regularId = MR.fonts.anastasia.regular),
            PredefinedFontPath(
                "made_script",
                displayName = "Made Script",
                regularId = MR.fonts.made_script.regular
            ),
            PredefinedFontPath(
                "nickainley",
                forPremium = true,
                regularId = MR.fonts.nickainley.regular
            ),
            PredefinedFontPath("inspiration", regularId = MR.fonts.inspiration.regular),
            PredefinedFontPath("pacifico", regularId = MR.fonts.pacifico.regular),
            PredefinedFontPath(
                "nexa_script", displayName = "Nexa Script", boldId = MR.fonts.nexa_script.bold,
                regularId = MR.fonts.nexa_script.regular, lightId = MR.fonts.nexa_script.light
            ),
            PredefinedFontPath("chalista", regularId = MR.fonts.chalista.regular),
            PredefinedFontPath("coconut", regularId = MR.fonts.coconut.regular),
            PredefinedFontPath(
                "savoy",
                displayName = "Savoy let",
                regularId = MR.fonts.savoy.regular
            ),
            PredefinedFontPath(
                "merriweather",
                displayName = "Merriweather",
                regularId = MR.fonts.merriweather.regular,
                boldId = MR.fonts.merriweather.bold,
                lightId =  MR.fonts.merriweather.light
            ),
            PredefinedFontPath(
                "afjat",
                displayName = "afjat",
                forPremium = true,
                regularId =  MR.fonts.afjat.light
            ),
        )
    }

    private val simpleFonts by lazy {
        listOf(
            platformFontPathProvider.getRobotoFont(),
            platformFontPathProvider.getSfProFont(),
            PredefinedFontPath(
                "nunito",
                regularId = MR.fonts.nunito.regular,
                boldId = MR.fonts.nunito.bold,
                italicId = MR.fonts.nunito.italic,
                lightId = MR.fonts.nunito.italic
            ),
            PredefinedFontPath(
                "monument",
                displayName = "Monument",
                forPremium = true, regularId = MR.fonts.monument.regular,
                lightId = MR.fonts.monument.light,
                boldId = MR.fonts.monument.bold
            ),
            PredefinedFontPath(
                "mont", regularId = MR.fonts.mont.regular,
                lightId = MR.fonts.mont.light, boldId = MR.fonts.mont.bold
            ),

            PredefinedFontPath(
                "made", regularId = MR.fonts.made.regular,
                boldId = MR.fonts.made.bold, lightId = MR.fonts.made.light
            ),

            PredefinedFontPath(
                "bebasneue",
                displayName = "BEBAS NEUE",
                regularId = MR.fonts.bebasneue.regular,
                boldId = MR.fonts.bebasneue.bold,
                lightId = MR.fonts.bebasneue.light
            ),

            PredefinedFontPath(
                "bloggersans", displayName = "BLOGGER SANS",
                regularId = MR.fonts.bloggersans.regular,
                boldId = MR.fonts.bloggersans.bold,
                lightId = MR.fonts.bloggersans.light, italicId = MR.fonts.bloggersans.italic
            ),

            PredefinedFontPath(
                "montserrat",
                regularId = MR.fonts.montserrat.regular,
                italicId = MR.fonts.montserrat.italic,
                boldId = MR.fonts.montserrat.bold,
                lightId = MR.fonts.montserrat.light
            ),

            PredefinedFontPath(
                "oswald", regularId = MR.fonts.oswald.regular,
                boldId = MR.fonts.oswald.bold, lightId = MR.fonts.oswald.light
            ),

            PredefinedFontPath(
                "buyan", regularId = MR.fonts.buyan.regular,
                boldId = MR.fonts.buyan.bold, lightId = MR.fonts.buyan.light
            ),

            PredefinedFontPath(
                "notcourier",
                regularId = MR.fonts.notcourier.regular,
                boldId = MR.fonts.notcourier.bold
            ),
            PredefinedFontPath(
                "dinpro",
                displayName = "Din Pro",
                regularId = MR.fonts.dinpro.regular,
                boldId = MR.fonts.dinpro.bold,
                lightId = MR.fonts.dinpro.light,
                italicId = MR.fonts.dinpro.italic
            ),
            PredefinedFontPath(
                "gilroy", regularId = MR.fonts.gilroy.regular, italicId = MR.fonts.gilroy.italic,
                boldId = MR.fonts.gilroy.bold, lightId = MR.fonts.gilroy.light
            ),
            PredefinedFontPath(
                "getvoip", regularId = MR.fonts.getvoip.regular, italicId = MR.fonts.getvoip.italic
            ),
        )
    }

    private val classicFonts by lazy {
        listOf(
            PredefinedFontPath(
                "cormorant",
                regularId = MR.fonts.cormorant.regular,
                boldId = MR.fonts.cormorant.bold,
                italicId = MR.fonts.cormorant.italic,
                lightId = MR.fonts.cormorant.light
            ),
            PredefinedFontPath(
                "garamond", regularId = MR.fonts.garamond.regular, boldId = MR.fonts.garamond.bold,
                italicId = MR.fonts.garamond.italic, lightId = MR.fonts.garamond.light
            ),
            PredefinedFontPath(
                "spectral",
                regularId = MR.fonts.spectral.regular,
                italicId = MR.fonts.spectral.italic,
                boldId = MR.fonts.spectral.bold,
                lightId = MR.fonts.spectral.light
            ),
            PredefinedFontPath(
                "oranienbaum",
                forPremium = true,
                regularId = MR.fonts.oranienbaum.regular
            ),
            PredefinedFontPath(
                "playfair", regularId = MR.fonts.playfair.regular, boldId = MR.fonts.playfair.bold,
                italicId = MR.fonts.playfair.italic
            ),
            PredefinedFontPath(
                "benguiat", displayName = "STRANGER",
                regularId = MR.fonts.benguiat.regular,
                boldId = MR.fonts.benguiat.bold, lightId = MR.fonts.benguiat.light
            ),
        )
    }

    private val brushFonts by lazy {
        listOf(
            PredefinedFontPath("rustico", regularId = MR.fonts.rustico.regular),
            PredefinedFontPath("dk", displayName = "Pumpkin", regularId = MR.fonts.dk.regular),
            PredefinedFontPath(
                "sunday",
                displayName = "Spooky",
                regularId = MR.fonts.sunday.regular
            ),
            PredefinedFontPath(
                "ancherr",
                displayName = "WITCH",
                regularId = MR.fonts.ancherr.regular
            ),
            PredefinedFontPath(
                "bradley", displayName = "Bradley Hand",
                regularId = MR.fonts.bradley.regular, boldId = MR.fonts.bradley.bold
            )
        )
    }

    private val arabicFonts by lazy {
        listOf(
            PredefinedFontPath(
                "cairo", forPremium = true,
                regularId = MR.fonts.cairo.regular,
                boldId = MR.fonts.cairo.bold, lightId = MR.fonts.cairo.light
            ),
            PredefinedFontPath(
                "janna", regularId = MR.fonts.janna.regular,
                boldId = MR.fonts.janna.bold
            ),
            PredefinedFontPath(
                "markazi", forPremium = true,
                regularId = MR.fonts.markazi.regular, boldId = MR.fonts.markazi.bold
            ),
            PredefinedFontPath(
                "mirza",
                regularId = MR.fonts.mirza.regular,
                boldId = MR.fonts.mirza.bold
            ),
            PredefinedFontPath(
                "reem_kufi",
                displayName = "Reem Kufi",
                regularId = MR.fonts.reem_kufi.regular
            ),
        )
    }

    val predefinedFontCategories: Map<String, List<PredefinedFontPath>> by lazy {
        val map = mutableMapOf<String, List<PredefinedFontPath>>()
        map.put("script", scriptFonts)
        map.put("simple", simpleFonts)
        map.put("classic", classicFonts)
        map.put("brush", brushFonts)
        map.put("arabic", arabicFonts)
        map
    }

    val allPredefinedFonts: List<PredefinedFontPath> by lazy {
        val res = mutableListOf<PredefinedFontPath>()

        predefinedFontCategories.values.forEach {
            res.addAll(it)
        }
        res.addAll(alternativeCyrillicFontsMap.values)
        res
    }

    val allCategories: List<String> by lazy {
        mutableListOf<String>().also {
        //mutableListOf(CATEGORY_ID_UPLOAD).also {
            it.addAll(predefinedFontCategories.keys)
        }
    }


    private fun selectedFontPathCyrillic(currentPath: String?): String? {
        if (currentPath != null)
            return replaceCyrillic(currentPath)
        return currentPath
    }

    private val fontCyrillicReplacements by lazy {
        alternativeCyrillicFontsMap.mapValues {
            it.value.path
        }
    }


    private val alternativeCyrillicFonts: Collection<PredefinedFontPath> by lazy {
        alternativeCyrillicFontsMap.values
    }

    private val reversedCyrillicFonts by lazy {
        fontCyrillicReplacements.reversedMap()
    }

    private fun getFontPathForCyrillic(fontPath: String?) =
        if (fontPath == null) null else fontCyrillicReplacements[fontPath.getFileName()]
            ?: fontPath

    fun replaceFontOnTextChange(
        fontData: FontData?, scope: CoroutineScope,
        createState: () -> StateFlow<String>, onNewFontCreated: (FontData) -> Unit
    ) {


        var cyrillicFontPath = getFontPathForCyrillic(fontData?.fontPath)
        if (cyrillicFontPath != null) {
            val state = createState()

            scope.launch {
                state.collect {

                    if (cyrillicFontPath != null && FontUtils.hasCyrillic(it)) {
                        val font = FontData(
                            cyrillicFontPath, fontData?.fontStyle
                                ?: InspFontStyle.regular
                        )
                        onNewFontCreated(font)
                        cyrillicFontPath = null
                        cancel()
                    }
                }
            }
        }
    }

    fun mayReplaceFontIfCyrillic(text: String, font: FontData): Boolean {
        val fontReplacement = getFontPathForCyrillic(font.fontPath)

        if (fontReplacement != null && FontUtils.hasCyrillic(text)) {

            font.fontPath = fontReplacement
            return true
        }
        return false
    }

    fun mayReplaceFontIfCyrillic(text: String, font: PredefinedFontPath): PredefinedFontPath {
        val fontReplacement = getFontPathForCyrillic(font.path)

        if (fontReplacement != null && FontUtils.hasCyrillic(text)) {

            return getFontPathById(fontReplacement)
        }
        return font
    }

    fun replaceCyrillic(fontPath: String): String {
        val cyrillic = reversedCyrillicFonts[fontPath]
        return cyrillic ?: fontPath
    }

    fun getFontData(fontPath: FontPath, fontStyle: InspFontStyle): FontData {
        return FontData(fontPath.path, fontStyle)
    }

    fun findInitialSelectedCategory(currentFont: String?): Int {

        if (currentFont == null) return 2

        val currentFontAlternative = selectedFontPathCyrillic(currentFont)

        val resultIndex = predefinedFontCategories.values.indexOfFirst {
            it.any {
                isCurrentFont(currentFont, it.path) ||
                        isCurrentFont(currentFontAlternative, it.path)
            }
        }
        //if not in predefined, then uploaded.
        return if (resultIndex == -1) 0 else resultIndex + 1
    }

    fun getFontPathByIdWithFile(path: String?): FontPath {
        if (path?.startsWith("file") == true) {
            return UploadedFontPath(path.removeScheme())
        } else
            return getFontPathById(path)
    }

    fun getFontPathById(path: String?): PredefinedFontPath {
        if (path == null) return platformFontPathProvider.getRobotoFont()

        val p = path.substringAfter(androidPathDelimiter)

        val fontPath = allPredefinedFonts.find { it.path == p }
        if (fontPath != null) return fontPath

        if (!isDebug) {
            return platformFontPathProvider.defaultFont()
        } else
            throw IllegalStateException("can't find path by id ${path}")
    }

    fun findSelectedIndex(fonts: List<FontPath>, currentPath: String?): Int {
        var selectedIndex = -1
        if (currentPath != null) {
            selectedIndex = fonts.indexOfFirst { it.path.endsWith(currentPath) }

            if (selectedIndex == -1) {

                val cyrillicPath = reversedCyrillicFonts[currentPath.getFileName()]
                if (cyrillicPath != null) {
                    selectedIndex =
                        fonts.indexOfFirst { it.path.endsWith(cyrillicPath) }
                }
            }
        }
        if (selectedIndex == -1)
            return 0
        return selectedIndex
    }

    fun getFontsByCategory(categoryId: String): List<PredefinedFontPath> {
        return predefinedFontCategories[categoryId]
            ?: throw IllegalArgumentException("can't find category by id ${categoryId}")
    }

    companion object {
        const val androidPathDelimiter = "assets://fonts/"
        const val ROBOTO_FONT_PATH = "roboto"
        const val SF_FONT_PATH = "sf_pro_display"
        const val SF_FONT_NAME = "SF PRO"
        const val CATEGORY_ID_UPLOAD = "upload"

        fun isCurrentFont(currentFont: String?, fontPath: String?): Boolean {
            return fontPath == currentFont || (fontPath != null && currentFont?.endsWith(
                fontPath
            ) == true) || (currentFont != null && fontPath?.endsWith(
                currentFont
            ) == true)
        }
    }
}
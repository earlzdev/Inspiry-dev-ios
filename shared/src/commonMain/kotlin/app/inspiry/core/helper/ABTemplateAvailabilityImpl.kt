package app.inspiry.core.helper

import app.inspiry.MR
import app.inspiry.core.media.TemplateAvailability
import dev.icerock.moko.resources.AssetResource

class ABTemplateAvailabilityImpl(private val abTestMode: Int): ABTemplateAvailability {

    // public for test
    private val newPremiumTemplatesPath = listOf(

        MR.assets.templates.art.Art1SingleImageTemporalBorder,
        MR.assets.templates.art.Art2DoubleMaskCoffeeColor,
        MR.assets.templates.beauty.BeautyCircleBranches,
        MR.assets.templates.business.InstaShop3Items,
        MR.assets.templates.classic.BeforeAfterHorizontal,
        MR.assets.templates.classic.BeforeAfter,
        MR.assets.templates.minimal.two_vertical_lines,
        MR.assets.templates.minimal.slide_show,
        MR.assets.templates.minimal.Minimal5_hold_on,
        MR.assets.templates.minimal.Minimal3_circles,
        MR.assets.templates.minimal.woman_and_flowers,
        MR.assets.templates.gradient.Gradient3CopiesDenseBg,
        MR.assets.templates.gradient.WaveGradientTextMask,
        MR.assets.templates.paper.Paper2Torn,
        MR.assets.templates.paper.TwoPaperMask,
        MR.assets.templates.film.CameraLikeFrame,
        MR.assets.templates.typography.simple_quote_black
    )


    override fun getTemplateAvailability(
        current: TemplateAvailability,
        originalTemplatePath: AssetResource
    ): TemplateAvailability {
        when (abTestMode) {
            TEMPLATE_AVAILABILITY_MORE_PAID -> {

                if (originalTemplatePath in newPremiumTemplatesPath) return TemplateAvailability.PREMIUM
            }
        }

        return current
    }

    companion object {
        const val TEMPLATE_AVAILABILITY_DEFAULT = 0
        const val TEMPLATE_AVAILABILITY_MORE_PAID = 1
    }
}
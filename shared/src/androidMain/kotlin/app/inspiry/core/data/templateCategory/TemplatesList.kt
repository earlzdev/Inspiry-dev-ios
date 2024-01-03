package app.inspiry.core.data.templateCategory

import app.inspiry.MR

actual object TemplatesList {
    actual fun allTemplates() = mutableListOf(

        TemplateCategory(
            id = "Portraits",
            displayName = MR.strings.category_portraits,
            templatePaths = listOf(
                MR.assets.templates.portraits.portraits1Triangle,
                MR.assets.templates.portraits.portraits1Circle,
                MR.assets.templates.portraits.portraits1Square,
            )
        ),

        TemplateCategory(
            "Gentle", MR.strings.category_gentle,
            listOf(
                MR.assets.templates.gentle.Gentle1Moments,
                MR.assets.templates.gentle.Gentle1Spa,
                MR.assets.templates.gentle.Gentle1Collection,
                MR.assets.templates.gentle.Gentle2NewProduct,
                MR.assets.templates.gentle.Gentle2SkinCare,
                MR.assets.templates.gentle.Gentle1BeInTheMoment
            )
        ),

        TemplateCategory(
            "grid", MR.strings.category_grid,
            listOf(
                MR.assets.templates.grid.SingleFramed,
                MR.assets.templates.grid.SingleBlurredBg,
                MR.assets.templates.grid.TwoPhotosZigzag,
                MR.assets.templates.grid.Vertical2Photos,
                MR.assets.templates.grid.Grid3x1Template,
                MR.assets.templates.grid.Grid3PhotosCurly,
                MR.assets.templates.grid.Grid4SlowPhotos,
                MR.assets.templates.grid.FourStraight,
                MR.assets.templates.grid.PortfolioTemplate,
                MR.assets.templates.grid.Mirror3x3,
                MR.assets.templates.grid.DualOverlappedTemplate,
                MR.assets.templates.grid.GridTop4Bottom,
                MR.assets.templates.grid.Grid3Even,
                MR.assets.templates.grid.Surrounding,
            )
        ),

        TemplateCategory(
            "blank", MR.strings.category_blank, listOf(
                MR.assets.templates.blank.BlankWhite,
                MR.assets.templates.blank.BlankStoryEmpty,
                MR.assets.templates.blank.BlankSquareEmpty,
                MR.assets.templates.blank.BlankHorizontalEmpty,
            )
        ),

        TemplateCategory(
            "art", MR.strings.category_art,
            templatePaths = listOf(
                MR.assets.templates.art.Art3ZoomBrushMask,
                MR.assets.templates.art.Art1SimpleWithBlueBrush,
                MR.assets.templates.art.Art1TheBestVacation,
                MR.assets.templates.art.Art1SingleImageTemporalBorder,
                MR.assets.templates.art.Art2DoubleMaskCoffeeColor,
                MR.assets.templates.art.Art1SingleWithBgUnderBrush,
                MR.assets.templates.art.Art3RoseBrushDrops,
                MR.assets.templates.art.Art2SmoothMask,
                MR.assets.templates.art.Art4LeavesOn4Photos,
                MR.assets.templates.art.Art1SingleWithRectangleOnTop,
                MR.assets.templates.art.Art3ThreeNiceGuys,
                MR.assets.templates.art.Art2DoublePhotoShow,
                MR.assets.templates.art.Art0TextGold,
                MR.assets.templates.art.Art3BrushWithFlowers
            )
        ),

        TemplateCategory(
            id = "removeBg",
            displayName = MR.strings.remove_bg_promo_title,
            templatePaths = listOf(
                MR.assets.templates.removeBg.removeBg1BigSale,
                MR.assets.templates.removeBg.removeBg1onlineShop,
                MR.assets.templates.removeBg.removeBg1planesWithLeaves,
                MR.assets.templates.removeBg.removeBg1waveBackground,
                MR.assets.templates.removeBg.removeBg4JewelryCircleBack,
                MR.assets.templates.removeBg.removeBg2ComparingThings,
                MR.assets.templates.removeBg.removeBg1SimpleWithCircle,
                MR.assets.templates.removeBg.removeBg1RoundedMild,
                MR.assets.templates.removeBg.removeBg2BigFlashingFace,
                MR.assets.templates.removeBg.removeBg1FocusedImage
            )
        ),

        TemplateCategory(
            "love", MR.strings.category_love,
            templatePaths = listOf(
                MR.assets.templates.love.Love1ColoredHearts,
                MR.assets.templates.love.Love2RotatedFrames,
                MR.assets.templates.love.LoveRotatedMask,
                MR.assets.templates.love.Love2Messages,
                MR.assets.templates.love.Love3Displace,
                MR.assets.templates.love.Love3SlidesNoDuplicates,
                MR.assets.templates.love.LoveHeartMask,
                MR.assets.templates.love.Love2BorderPhotos,
                MR.assets.templates.love.Love3SlideMasked
            )
        ),

        TemplateCategory(
            "minimal", MR.strings.category_minimal,
            templatePaths = listOf(
                MR.assets.templates.minimal.mood_board_squares,
                MR.assets.templates.minimal.pink_rectangle,
                MR.assets.templates.minimal.girl_with_hat,
                MR.assets.templates.minimal.two_vertical_lines,
                MR.assets.templates.minimal.circle_girl,
                MR.assets.templates.minimal.Minimal3_circles,
                MR.assets.templates.minimal.slide_show,
                MR.assets.templates.minimal.woman_and_flowers,
                MR.assets.templates.minimal.new_in_stock_3small,
                MR.assets.templates.minimal.Minimal4_with_text_in_center,
                MR.assets.templates.minimal.Minimal5_hold_on,
                MR.assets.templates.minimal.soft_grid
            )
        ),

        TemplateCategory(
            "blackFriday", MR.strings.category_black_friday,
            templatePaths = listOf(
                MR.assets.templates.blackFriday.blackFriday1DarkFrames,
                MR.assets.templates.blackFriday.blackFriday1softLight,
                MR.assets.templates.blackFriday.blackFriday1MinimalLiveBg,
                MR.assets.templates.blackFriday.blackFriday3vertical,
                MR.assets.templates.blackFriday.blackFriday1TransparentFrame,
                MR.assets.templates.blackFriday.blackFriday2NewCollection,
            )
        ),
        TemplateCategory(
            "social",
            MR.strings.category_social,
            templatePaths = listOf(
                MR.assets.templates.social.Social1AnimatedCirclesBg,
                MR.assets.templates.social.Social1MessageHighlights,
                MR.assets.templates.social.Social2RoundFace,
                MR.assets.templates.social.Social1Phone,
                MR.assets.templates.social.Social1SmallPhoto,
                MR.assets.templates.social.Social1BigPhoto,
            )
        ),
        TemplateCategory(
            "beauty", MR.strings.category_beauty,
            templatePaths = listOf(
                MR.assets.templates.beauty.BeautyCircleBranches,
                MR.assets.templates.beauty.BeautyCenterPotatoShape,
                MR.assets.templates.beauty.Beauty2Masks,
                MR.assets.templates.beauty.Beauty4SlideMasked,
                MR.assets.templates.beauty.BeautyVogueLike,
                MR.assets.templates.beauty.Beauty2BeforeAfter,
            )
        ),

        TemplateCategory(
            "business", MR.strings.category_business,
            templatePaths = listOf(
                MR.assets.templates.business.FlowersWorkshop,
                MR.assets.templates.business.FloatingYoga,
                MR.assets.templates.business.HireMediaManager,
                MR.assets.templates.business.InstaShop3Items,
                MR.assets.templates.business.SpringCollectionSale,
                MR.assets.templates.business.PhoneTextsPopping,
                MR.assets.templates.business.BusinessTextMaskBlackFriday,
                MR.assets.templates.business.BusinessCoffeeStickersMask,
                MR.assets.templates.business.NewPostEveryDay,
            )
        ),
        TemplateCategory(
            "classic", MR.strings.category_classic,
            templatePaths = listOf(
                MR.assets.templates.classic.BeforeAfter,
                MR.assets.templates.classic.BeforeAfterHorizontal,
                MR.assets.templates.classic.SingleTextMask,
                MR.assets.templates.classic.IntoBlur,
            )
        ),

        TemplateCategory(
            "gradient", MR.strings.category_gradient,
            templatePaths = listOf(
                MR.assets.templates.gradient.GradientBlackFriday,
                MR.assets.templates.gradient.Gradient3CopiesDenseBg,
                MR.assets.templates.gradient.ShadowInGradient,
                MR.assets.templates.gradient.BlurLineTrends,
                MR.assets.templates.gradient.SingleAlphaDisplaceMask,
                MR.assets.templates.gradient.Gradient3LinesOn2Photos,
                MR.assets.templates.gradient.WaveGradientTextMask,
                MR.assets.templates.gradient.WaterAlphaDisplace,
                MR.assets.templates.gradient.TextWithGradientBg,
            )
        ),
        TemplateCategory(
            "typography", MR.strings.category_typography,
            templatePaths = listOf(
                MR.assets.templates.typography.simple_quote_black,
                MR.assets.templates.typography.simple_quote_white,
            )
        ),

        TemplateCategory(
            "paper", MR.strings.category_paper,
            templatePaths = listOf(
                MR.assets.templates.paper.TwoWithPaper,
                MR.assets.templates.paper.Paper3Torn,
                MR.assets.templates.paper.Paper2Torn,
                MR.assets.templates.paper.HorizontalPaperMaskWithText,
                MR.assets.templates.paper.VerticalPaperMask,
                MR.assets.templates.paper.TwoPaperMask,
                MR.assets.templates.paper.PaperShadowTexts,
                MR.assets.templates.paper.ThreeTornPapers,
                MR.assets.templates.paper.UrbanFestival,
            )
        ),
        TemplateCategory(
            "film", MR.strings.category_film,
            templatePaths = listOf(
                MR.assets.templates.film.WithWhiteSimpleFrame,
                MR.assets.templates.film.SlidingGirlAndCat,
                MR.assets.templates.film.WithBlackSimpleFrame,
                MR.assets.templates.film.SlidingBlueGradient,
                MR.assets.templates.film.SingleFilmMask,
                MR.assets.templates.film.SingleScratches,
                MR.assets.templates.film.WithRetroFrame,
                MR.assets.templates.film.WhiteTapeDual,
                MR.assets.templates.film.ThreeFilmMask,
                MR.assets.templates.film.CameraLikeFrame,
                MR.assets.templates.film.Tape4Photos,
                MR.assets.templates.film.SlidingNeonFrame,
                MR.assets.templates.film.ThreeAtAngle,
                MR.assets.templates.film.ZoomShot,
                MR.assets.templates.film.PhotoFrames
            )
        ),

        TemplateCategory(
            "christmas", MR.strings.category_christmas,
            templatePaths = listOf(
                MR.assets.templates.christmas.Ch1FestiveMood,
                MR.assets.templates.christmas.Ch1Discount,
                MR.assets.templates.christmas.Ch4MomentsSlide,
                MR.assets.templates.christmas.Ch3Presents,
                MR.assets.templates.christmas.ChRoseText,
                MR.assets.templates.christmas.Ch1BrushMask,
                MR.assets.templates.christmas.Ch2BorderPhotos,
                MR.assets.templates.christmas.ChGoldGlitterText,
                MR.assets.templates.christmas.ChFramePaper,
                MR.assets.templates.christmas.Ch2BlurredDuplicate,
                MR.assets.templates.christmas.Ch3AlphaSlide,
                MR.assets.templates.christmas.Ch1PhoneFrame,
            )
        ),

        TemplateCategory(
            "halloween", MR.strings.category_halloween,
            templatePaths = listOf(
                MR.assets.templates.halloween.halloween1ColoredPumpkins,
                MR.assets.templates.halloween.halloween1RoundedBottomPhoto,
                MR.assets.templates.halloween.halloween1UnderworldWindow,
                MR.assets.templates.halloween.halloween1RoundingText,
                MR.assets.templates.halloween.QuickMovements,
                MR.assets.templates.halloween.DistortedClown,
                MR.assets.templates.halloween.BatsParty,
                MR.assets.templates.halloween.WalkingDeadParty,
                MR.assets.templates.halloween.SingleWeb,
                MR.assets.templates.halloween.ShadowText
            )
        ),
        TemplateCategory(
            "vhs", MR.strings.category_vhs,
            templatePaths = listOf(
                MR.assets.templates.vhs.SingleVHS2Mask,
                MR.assets.templates.vhs.SingleVHS3Mask,
                MR.assets.templates.vhs.SingleVHS4Mask,
                MR.assets.templates.vhs.SingleVHSMask,
            )
        ),
        TemplateCategory(
            "plastic", MR.strings.category_plastic,
            templatePaths = listOf(
                MR.assets.templates.plastic.PlasticBlackBg,
                MR.assets.templates.plastic.PlasticPaperMask,
                MR.assets.templates.plastic.SunshinePlastic,
                MR.assets.templates.plastic.SimplePlastic1,
                MR.assets.templates.plastic.PlasticBag,
                MR.assets.templates.plastic.LittleBagWithText,
                MR.assets.templates.plastic.TwoSquares,
                MR.assets.templates.plastic.TwoBags,
                MR.assets.templates.plastic.ThreeBags,
                MR.assets.templates.plastic.PinnedPlastic,
                MR.assets.templates.plastic.RoundedCornersPlastic,
                MR.assets.templates.plastic.TwoCircles,
            )
        )

    )
}
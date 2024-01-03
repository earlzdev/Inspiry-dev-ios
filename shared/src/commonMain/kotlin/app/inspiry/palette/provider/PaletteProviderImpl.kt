package app.inspiry.palette.provider

import app.inspiry.core.media.GradientOrientation
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.core.util.PredefinedColors

class PaletteProviderImpl : PaletteProvider {
    override fun getGradients() = mutableListOf(
        PaletteLinearGradient(
            GradientOrientation.BL_TR,
            arrayListOf(0xffd9a7c7.toInt(), 0xfffffcdc.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.TL_BR,
            arrayListOf(0xffFFDDE1.toInt(), 0xffEE9CA7.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BOTTOM_TOP,
            arrayListOf(0xffFF6CAB.toInt(), 0xff7366FF.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.TL_BR,
            arrayListOf(0xffFF9482.toInt(), 0xff7D77FF.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BL_TR,
            arrayListOf(0xffFCE38A.toInt(), 0xffF38181.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.TOP_BOTTOM,
            arrayListOf(0xff5B247A.toInt(), 0xff1BCEDF.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BL_TR,
            arrayListOf(0xff574BCD.toInt(), 0xff2999AD.toInt(), 0xff41E975.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BL_TR,
            arrayListOf(0xff879AF2.toInt(), 0xffD3208B.toInt(), 0xffFDA000.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BL_TR,
            arrayListOf(0xffFFCF1B.toInt(), 0xffFF881B.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BL_TR,
            arrayListOf(0xffF4E8FA.toInt(), 0xff00C0F9.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BOTTOM_TOP,
            arrayListOf(0xffFFCFA5.toInt(), 0xffEE4D5F.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BL_TR,
            arrayListOf(0xffF02FC2.toInt(), 0xff6094EA.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BL_TR,
            arrayListOf(0xff9EFFF8.toInt(), 0xff00B8BA.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BR_TL,
            arrayListOf(0xffFCCB90.toInt(), 0xffD57EEA.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BR_TL,
            arrayListOf(0xff276174.toInt(), 0xff33C58E.toInt(), 0xff63FD88.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BL_TR,
            arrayListOf(0xff17EAD9.toInt(), 0xff6078EA.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.TOP_BOTTOM,
            arrayListOf(0xffFCE38A.toInt(), 0xffF56868.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BL_TR,
            arrayListOf(0xff622774.toInt(), 0xffC53364.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BL_TR,
            arrayListOf(0xff6EE2F5.toInt(), 0xffB554F0.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BL_TR,
            arrayListOf(0xffF6072F.toInt(), 0xffFF7DD4.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BL_TR,
            arrayListOf(0xffFF5B94.toInt(), 0xff8441A4.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.LEFT_RIGHT,
            arrayListOf(0xff8390FF.toInt(), 0xffF0CBF6.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.TL_BR,
            arrayListOf(0xffFFB6B6.toInt(), 0xffF650A0.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.TOP_BOTTOM,
            arrayListOf(0xffFBF4FF.toInt(), 0xff00C0F9.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.TL_BR,
            arrayListOf(0xff7BF2E9.toInt(), 0xffDA42E1.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.BOTTOM_TOP,
            arrayListOf(0xffB8E1FC.toInt(), 0xffFCB8F1.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.TOP_BOTTOM,
            arrayListOf(0xffFFF4CC.toInt(), 0xffFF8A00.toInt())
        ),

        PaletteLinearGradient(
            GradientOrientation.LEFT_RIGHT,
            arrayListOf(0xffB2841B.toInt(), 0xff77451E.toInt())
        ),
        PaletteLinearGradient(
            GradientOrientation.LEFT_RIGHT,
            arrayListOf(0xffFFEAB1.toInt(), 0xffFC9D09.toInt())
        ),
    )

    override fun getSingleColors(): List<Int> = listOf(
        0xffffffff.toInt(),
        0xff000000.toInt(),
        0xff424242.toInt(),
        0xff9e9e9e.toInt(),
        0xffdbdbdb.toInt(),
        0xfff6cbca.toInt(),
        0xffef9084.toInt(),
        0xffEA394B.toInt(),
        0xffC3291B.toInt(),
        0xff9B1EF5.toInt(),
        0xffC329F1.toInt(),
        0xffDC85F6.toInt(),
        0xffDAAFFA.toInt(),
        0xffB9BCFA.toInt(),
        0xff8f9df8.toInt(),
        0xff586cf6.toInt(),
        0xff374ef5.toInt(),
        0xff3f8ee4.toInt(),
        0xff4faef8.toInt(),
        0xff94d6fb.toInt(),
        0xffB8E1FC.toInt(),
        0xffD7FDF4.toInt(),
        0xff91FCDE.toInt(),
        0xff10E599.toInt(),
        0xffD1FD51.toInt(),
        0xffFAFFB0.toInt(),
        0xffFBE68D.toInt(),
        0xffF6C743.toInt(),
        0xffF3AE3D.toInt(),
        0xffCC3E1F.toInt(),
        0xffEB4F27.toInt(),
        0xffED7742.toInt(),
        0xffF1A386.toInt(),
        0xffF9DED3.toInt(),
    )

    override fun getTwoColors(): List<IntArray> = listOf(
        intArrayOf(PredefinedColors.WHITE_ARGB, PredefinedColors.BLACK_ARGB),
        intArrayOf(PredefinedColors.BLACK_ARGB, PredefinedColors.WHITE_ARGB),
        intArrayOf(0xffDEF2F1.toInt(), PredefinedColors.BLACK_ARGB),
        intArrayOf(0xffFFF3F3.toInt(), PredefinedColors.BLACK_ARGB),
        intArrayOf(0xffFFCDD2.toInt(), PredefinedColors.BLACK_ARGB),
        intArrayOf(0xff212121.toInt(), 0xffF39F2C.toInt()),
        intArrayOf(0xffFEFEFE.toInt(), 0xff141483.toInt()),
        intArrayOf(0xffFFCB00.toInt(), 0xffFFFFFF.toInt()),
        intArrayOf(0xffFFDB4F.toInt(), 0xff000000.toInt()),
        intArrayOf(0xffCFFCFF.toInt(), 0xff424242.toInt()),
        intArrayOf(0xff1A237E.toInt(), 0xffffffff.toInt()),
        intArrayOf(0xff97D1DC.toInt(), 0xffCF2F5D.toInt()),
        intArrayOf(0xff273238.toInt(), 0xffffffff.toInt()),
        intArrayOf(0xffFDE0E5.toInt(), 0xff1B223E.toInt()),
        intArrayOf(0xff0000F9.toInt(), 0xff1DE9B6.toInt()),
        intArrayOf(0xff000000.toInt(), 0xffE59215.toInt()),
        intArrayOf(0xffFFF1CB.toInt(), 0xff45070A.toInt()),
        intArrayOf(0xffFFF5F5.toInt(), 0xffFFB1B1.toInt()),
        intArrayOf(0xffDAFFF3.toInt(), 0xffC31DEC.toInt()),
        intArrayOf(0xffEFFEFF.toInt(), 0xff0FEEEE.toInt()),
        intArrayOf(0xffFFF1DD.toInt(), 0xff51355A.toInt()),
        intArrayOf(0xffFFCAD6.toInt(), 0xff48A8FF.toInt()),
        intArrayOf(0xffEDFF4F.toInt(), 0xffFE59EC.toInt()),
        intArrayOf(0xffF2542D.toInt(), 0xffFFE2B7.toInt()),
        intArrayOf(0xffABFFE1.toInt(), 0xff4E25F4.toInt()),
        intArrayOf(0xffFEEAD7.toInt(), 0xffF07167.toInt()),
        intArrayOf(0xffFFFFFF.toInt(), 0xff923CFF.toInt()),
    )

    override fun getThreeColors(): List<IntArray> = listOf(
        intArrayOf(0xffEFC65F.toInt(), PredefinedColors.BLACK_ARGB, 0xffC97E9F.toInt()),
        intArrayOf(PredefinedColors.WHITE_ARGB, PredefinedColors.BLACK_ARGB, 0xffFAC031.toInt()),

        intArrayOf(0xff000000.toInt(), 0xffffffff.toInt(), 0xff757575.toInt()),
        intArrayOf(0xffF9F4ED.toInt(), 0xff000000.toInt(), 0xff7D62EE.toInt()),
        intArrayOf(0xffFDF1F1.toInt(), 0xff000000.toInt(), 0xffE39955.toInt()),
        intArrayOf(0xffDCF6FD.toInt(), 0xff001E13.toInt(), 0xff6FBFA4.toInt()),
        intArrayOf(0xffE1FFEB.toInt(), 0xff000000.toInt(), 0xff96C6ED.toInt()),
        intArrayOf(0xffFFE6D7.toInt(), 0xff331717.toInt(), 0xffB3EA4D.toInt()),
        intArrayOf(0xffFFE2E4.toInt(), 0xff311010.toInt(), 0xffFFFFFF.toInt()),
        intArrayOf(0xff20221D.toInt(), 0xffE19C34.toInt(), 0xffFFFEFB.toInt()),
        intArrayOf(0xffffffff.toInt(), 0xff16188B.toInt(), 0xffFFCFBF.toInt()),
        intArrayOf(0xffE2B700.toInt(), 0xffffffff.toInt(), 0xff9D00EB.toInt()),
        intArrayOf(0xffffffff.toInt(), 0xff000000.toInt(), 0xff1BEBB5.toInt()),
        intArrayOf(0xffFEDA50.toInt(), 0xff020308.toInt(), 0xffffffff.toInt()),
        intArrayOf(0xff000000.toInt(), 0xffffffff.toInt(), 0xff020308.toInt()),
        intArrayOf(0xffffffff.toInt(), 0xff363636.toInt(), 0xffBEBEBE.toInt()),
        intArrayOf(0xff131B9B.toInt(), 0xffffffff.toInt(), 0xff000000.toInt()),
        intArrayOf(0xffA137AA.toInt(), 0xffffffff.toInt(), 0xff0E3735.toInt()),
        intArrayOf(0xff26323A.toInt(), 0xffffffff.toInt(), 0xffE50D40.toInt()),
        intArrayOf(0xffFEDFE4.toInt(), 0xff1B223E.toInt(), 0xffFFFFFB.toInt()),
        intArrayOf(0xff000152.toInt(), 0xff1EE8B8.toInt(), 0xffFFFCFF.toInt()),
        intArrayOf(0xff000000.toInt(), 0xffBF8329.toInt(), 0xffCED3CF.toInt()),
        intArrayOf(0xffEFEDE1.toInt(), 0xff1A0001.toInt(), 0xffC6AD89.toInt()),
        intArrayOf(0xffEAEBEB.toInt(), 0xffBF8228.toInt(), 0xffCED3CF.toInt()),
        intArrayOf(0xffFFFDF8.toInt(), 0xffFF8C42.toInt(), 0xffEAFEFF.toInt()),
        intArrayOf(0xff9DFBF9.toInt(), 0xff7D53DE.toInt(), 0xffEBFEFF.toInt()),
        intArrayOf(0xffCBD2D0.toInt(), 0xffFFFDF2.toInt(), 0xffEEE117.toInt()),
        intArrayOf(0xffFFA51F.toInt(), 0xffFFDCCA.toInt(), 0xffE05515.toInt()),
        intArrayOf(0xffFEEFEC.toInt(), 0xff7A5050.toInt(), 0xff74FFFF.toInt()),
        intArrayOf(0xffDBFF6D.toInt(), 0xff006400.toInt(), 0xff74F38B.toInt()),
        intArrayOf(0xff12DFC4.toInt(), 0xffFCFF52.toInt(), 0xffFF2FB8.toInt()),
        intArrayOf(0xffF7F7FF.toInt(), 0xffFE5F55.toInt(), 0xffBDD5EA.toInt()),
        intArrayOf(0xffD3FAFF.toInt(), 0xff5117CC.toInt(), 0xffCD9FCC.toInt()),
        intArrayOf(0xffFBE6FF.toInt(), 0xff7E0095.toInt(), 0xffE1A4ED.toInt()),
    )
}
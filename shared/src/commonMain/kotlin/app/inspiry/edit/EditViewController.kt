package app.inspiry.edit

interface EditViewController {
    fun doOnPanelAnimEnd(function: () -> Unit)
    fun animatePanelAppearNotDialog(animateAlpha: Boolean, finishInstantly: Boolean, newSize: Int = 0)
}
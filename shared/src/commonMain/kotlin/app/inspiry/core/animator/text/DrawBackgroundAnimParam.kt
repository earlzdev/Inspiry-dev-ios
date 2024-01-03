package app.inspiry.core.animator.text

import app.inspiry.core.data.Rect

open class DrawBackgroundAnimParam {

    //size of the current part of the text
    var left = 0f
    var top = 0f
    var width = 0
    var height = 0

    var alpha = 1.0f
    var color = 0

    var cornersRadius = 0f

    var rotate = 0f
    var scaleX = 1.0f
    var scaleY = 1.0f
    var translateX = 0f
    var translateY = 0f
    var pivotX = 0.5f
    var pivotY = 0.5f

    var clipRect: Rect? = null

    var angleShift = 0f
    var spaceAngle = 0f
    var circleRadius = 0f
    var circleLength = 0f
    var circularLengthFactor = 0f
    var textFullWidth = 0f
    var textPositionStart = 0f


    fun setClipRect(left: Int, top: Int, right: Int, bottom: Int) {
        if (clipRect == null) {
            clipRect = Rect(left, top, right, bottom)
        } else {
            clipRect!!.set(left, top, right, bottom)
        }
    }

    open fun nullify() {
        left = 0f
        top = 0f
        width = 0
        height = 0
        alpha = 1.0f
        color = 0
        cornersRadius = 0f
        rotate = 0f
        scaleX = 1.0f
        scaleY = 1.0f
        translateX = 0f
        translateY = 0f
        pivotX = 0.5f
        pivotY = 0.5f
        clipRect = null
    }
}
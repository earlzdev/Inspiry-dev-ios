package app.inspiry.core.animator.clipmask

import app.inspiry.core.animator.clipmask.logic.ClipMaskSettings

interface PathMask<T> {
    /**
     * @return array of elements (eg array of path points or array of rectangles)
     */
    fun getMaskArray(maskSettings: ClipMaskSettings): Array<T>
}
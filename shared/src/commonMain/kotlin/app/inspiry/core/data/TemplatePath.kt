package app.inspiry.core.data

import app.inspiry.MR
import app.inspiry.core.media.Template
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.resources.AssetResource
import dev.icerock.moko.resources.getAssetByFilePath


// not sealed class because it can't be used with Parcelize.
@Parcelize
open class TemplatePath : Parcelable {
    open val path: String
        get() = throw UnsupportedOperationException()

    fun getOriginalPathAsset(template: Template): AssetResource {
        return if (this is PredefinedTemplatePath) res
        else MR.assets.getAssetByFilePath(template.getOriginalPath())
    }
}

@Parcelize
class UserSavedTemplatePath(override val path: String) : TemplatePath() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserSavedTemplatePath) return false

        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}

@Parcelize
class PredefinedTemplatePath(val res: AssetResource) : TemplatePath() {

    override val path: String
        get() = res.originalPath

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PredefinedTemplatePath) return false

        if (res != other.res) return false

        return true
    }

    override fun hashCode(): Int {
        return res.hashCode()
    }
}
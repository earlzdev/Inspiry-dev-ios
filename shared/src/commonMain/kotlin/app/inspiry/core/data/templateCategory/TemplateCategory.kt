package app.inspiry.core.data.templateCategory

import dev.icerock.moko.resources.AssetResource
import dev.icerock.moko.resources.StringResource

class TemplateCategory(
    val id: String,
    val displayName: StringResource,
    val templatePaths: List<AssetResource>,
    val icon: TemplateCategoryIcon = TemplateCategoryIcon.NONE
) {
    val size: Int
        get() = templatePaths.size
}

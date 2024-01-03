package app.inspiry.core.data.templateCategory

expect object TemplatesList {
    fun allTemplates(): MutableList<TemplateCategory>
}
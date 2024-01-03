package app.inspiry.textanim

interface TextAnimProvider {
    fun getCategories(): List<String>
    fun getAnimations(category: String): List<MediaWithRes>
}
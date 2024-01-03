package app.inspiry.core.template

import app.inspiry.core.manager.DefaultDirs
import com.russhwolf.settings.Settings
import okio.FileSystem

interface MyStoriesMigration {
    fun needToMigrate(): Boolean
    fun performMigration()
}

class MyStoriesMigrationAndroid(val templateReadWrite: TemplateReadWrite,
                                val fileSystem: FileSystem,
                                val settings: Settings): MyStoriesMigration {

    private val cacheMyStories by lazy {
        fileSystem.list(templateReadWrite.myStoriesFolderBase(DefaultDirs.cachesDirectory!!))
    }

    override fun needToMigrate(): Boolean {
        return !settings.getBoolean(PREF_MIGRATED, false)
    }

    override fun performMigration() {
        if (cacheMyStories.isEmpty()) {

        } else {

            val newFolder = templateReadWrite.myStoriesFolder()
            cacheMyStories.forEach {
                val name = it.name
                fileSystem.atomicMove(it, newFolder.resolve(name))
            }
        }
        setMigrated()
    }

    private fun setMigrated() {
        settings.putBoolean(PREF_MIGRATED, true)
    }

}

const val PREF_MIGRATED = "migrated_to_my_stories_files_not_cache"
package app.inspiry.core.database

import app.inspiry.core.database.data.ExternalResource
import app.inspiry.core.manager.DebugManager
import app.inspiry.core.util.getScheme
import app.inspiry.core.util.removeScheme

class ExternalResourceDao(private val database: InspDatabase) {

    private inline fun checkScheme(path: String?) {
        if (DebugManager.isDebug) {
            if (path != null) {
                if (path.getScheme() != null) {
                    throw IllegalStateException("path should not contain scheme $path")
                }
            }
        }
    }

    fun onGetNewResource(externalName: String, path: String) {
        checkScheme(path)
        database.externalResourceQueries.insertResourceNew(externalName, path, 1)
    }

    fun getExistingResourceAndIncrementCount(
        existingName: String
    ): String? {

        val existingResource: ExternalResource? =
            database.externalResourceQueries.selectResourceByName(
                existingName
            ).executeAsOneOrNull()

        if (existingResource != null) {
            database.externalResourceQueries.updateUsagesCount(
                existingResource.usagesCount + 1,
                existingResource.path
            )
        }

        return existingResource?.path
    }

    fun onResourceStoppedExisting(path: String) {
        database.externalResourceQueries.deleteResourceByPath(path)
    }

    /**
     * @return true if this resource is no more used.
     */
    fun onRemoveResource(path: String): Boolean {
        checkScheme(path)

        val existingResource: ExternalResource? =
            database.externalResourceQueries.selectResourceByPath(path).executeAsOneOrNull()

        if (existingResource == null)
            return true
        else {
            if (existingResource.usagesCount <= 1) {
                database.externalResourceQueries.deleteResourceByPath(path)
                return true
            } else {
                database.externalResourceQueries.updateUsagesCount(
                    existingResource.usagesCount - 1,
                    path
                )
                return false
            }
        }
    }

    fun onTemplateOrMediaCopy(usingPaths: List<String>) {
        if (usingPaths.isEmpty()) return

        database.externalResourceQueries.updateMultipleUsageCount(usingPaths.map { it.removeScheme() })
    }

    // only for tests
    fun removeAll() {
        database.externalResourceQueries.removeAll()
    }

    fun selectAll() = database.externalResourceQueries.selectAll().executeAsList()
}
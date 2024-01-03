package app.inspiry.logo.data

import app.inspiry.core.database.InspDatabase
import app.inspiry.core.database.data.LogoItem
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow

class LogoDataSourceImpl(
    db: InspDatabase
) : LogoDataSource {

    private val queries = db.logoItemQueries

    override suspend fun addLogo(path: String, dateAdded: String, height: Long, width: Long) {
        queries.addLogo(path, dateAdded, height, width)
    }

    override suspend fun removeLogo(id: Long) {
        queries.removeLogo(id)
    }

    override suspend fun updateLogo(id: Long, path: String, height: Long, width: Long) {
        queries.updateLogo(path, height, width, id)
    }

    override fun getLogosList(): Flow<List<LogoItem>> {
        return queries.getLogos().asFlow().mapToList()
    }

}
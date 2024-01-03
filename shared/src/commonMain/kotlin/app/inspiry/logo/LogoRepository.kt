package app.inspiry.logo

import app.inspiry.core.database.data.LogoItem
import app.inspiry.core.util.PickMediaResult
import app.inspiry.logo.data.LogoDataSource
import app.inspiry.logo.data.LogoGetFromLibrary
import kotlinx.coroutines.flow.Flow

class LogoRepository(
    private val logoDB: LogoDataSource,
    private val logoFromLibrary: LogoGetFromLibrary
) {


    suspend fun addLogo(path: String, dateAdded: String, height: Long, width: Long) {
            logoDB.addLogo(path, dateAdded, height, width)
    }

    suspend fun removeLogo(id: Long) {
            logoDB.removeLogo(id)
    }

    suspend fun updateLogoPath(id: Long, path: String, width: Long, height: Long) {
            logoDB.updateLogo(id, path, height, width)
    }

    fun getLogosListFlow(): Flow<List<LogoItem>> {
        return logoDB.getLogosList()
    }

    suspend fun getLogoFromLibrary()  {
        logoFromLibrary.getLogoFromLibrary()
    }
    fun getNewMediasFlow(): Flow<List<PickMediaResult>>{
        return logoFromLibrary.newMediasFlow
    }
}
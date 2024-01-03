package app.inspiry.logo.data

import app.inspiry.core.database.data.LogoItem
import kotlinx.coroutines.flow.Flow

interface LogoDataSource {

    suspend fun addLogo(path: String, dateAdded: String, height: Long, width: Long)

    suspend fun removeLogo(id: Long)

    suspend fun updateLogo(id: Long, path: String, height: Long, width: Long)

    fun getLogosList(): Flow<List<LogoItem>>

}
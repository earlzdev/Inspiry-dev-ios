package app.inspiry.logo.data

import app.inspiry.core.util.PickMediaResult
import kotlinx.coroutines.flow.Flow


interface LogoGetFromLibrary {
    val newMediasFlow: Flow<List<PickMediaResult>>
    suspend fun getLogoFromLibrary()
}
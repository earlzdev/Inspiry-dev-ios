package app.inspiry.helpers

import android.content.Context

interface ProbaSdkWrapper {
    fun initProbaSdk(context: Context, amplitudeUserId: String?, appsflyerId: String?)
    suspend fun getValueAfterInit(key: String, waitFetch: Boolean = true): String
    fun getValueInstantly(key: String): String
}
package app.inspiry.export.dialog

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.inspiry.core.database.InspDatabase
import app.inspiry.core.database.data.ShareItem
import app.inspiry.export.viewmodel.MIME_TYPE_IMAGE
import app.inspiry.export.viewmodel.MIME_TYPE_VIDEO
import app.inspiry.utils.POPULAR_SHARE_PACKAGES
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.zip
import java.util.concurrent.ConcurrentHashMap

class ExportDialogViewModel(
    private val inspDatabase: InspDatabase,
    private val packageManager: PackageManager
) : ViewModel() {

    //package - label
    private val textsCache = ConcurrentHashMap<String, String>()
    private val channel = Channel<String>()

    private val packageInfoStateVideo = MutableStateFlow<List<ResolveInfo>?>(null)
    private val packageInfoStateImage = MutableStateFlow<List<ResolveInfo>?>(null)

    fun getPackageInfoState(imageElseVideo: Boolean): StateFlow<List<ResolveInfo>?> {
        return if (imageElseVideo)
            packageInfoStateImage
        else
            packageInfoStateVideo
    }

    init {
        loadData()
    }

    private fun CoroutineScope.getMapShareItemsAsync(
        mimeType: String,
        clickedPackages: List<ShareItem>,
        emitTo: MutableStateFlow<List<ResolveInfo>?>
    ) = async {

        val infos = getMapShareItems(mimeType, clickedPackages)
        emitTo.emit(infos)
        infos
    }

    private fun loadData() {

        viewModelScope.launch(Dispatchers.IO) {

            val clickedPackages = inspDatabase.shareItemQueries.selectAll().executeAsList()

            val videoDeferred =
                getMapShareItemsAsync(MIME_TYPE_VIDEO, clickedPackages, packageInfoStateVideo)
            val imageDeferred =
                getMapShareItemsAsync(MIME_TYPE_IMAGE, clickedPackages, packageInfoStateImage)

            videoDeferred.await()
            imageDeferred.await()
        }

        viewModelScope.launch(Dispatchers.IO) {

            packageInfoStateVideo.zip(packageInfoStateImage) { video, image ->
                if (video != null && image != null) video + image
                else null
            }.collect {
                if (it != null) {
                    populateTexts(it)
                }
            }
        }
    }

    suspend fun getTextForItem(activityName: String, setText: (String?) -> Unit) {

        if (textsCache.containsKey(activityName)) {
            setText(textsCache[activityName])
        } else {
            setText(null)

            var result: String? = null
            while (true) {

                if (textsCache.containsKey(activityName)) {
                    result = textsCache[activityName]
                    break
                } else {
                    try {
                        channel.receive()
                    } catch (e: ClosedReceiveChannelException) {
                        break
                    }
                }
            }

            setText(result ?: textsCache[activityName])
        }
    }

    private fun getMapShareItems(
        mimeType: String,
        clickedPackages: List<ShareItem>
    ): List<ResolveInfo> {

        val intentShare = Intent(Intent.ACTION_SEND)
        intentShare.type = mimeType

        val infos: MutableList<ResolveInfo> =
            packageManager.queryIntentActivities(intentShare, PackageManager.MATCH_DEFAULT_ONLY)

        //access optimization
        val map = mutableMapOf<String, Long>()
        for ((index, p) in POPULAR_SHARE_PACKAGES.reversed().withIndex()) {
            map[p] = index * 1000L
        }
        for (p in clickedPackages) {
            map[p.packageName] = p.dateAdded
        }

        infos.sortWith { o1, o2 ->
            val firstTime = map[o1.activityInfo.packageName] ?: 0L
            val secondTime = map[o2.activityInfo.packageName] ?: 0L

            when {
                firstTime > secondTime -> -1
                secondTime > firstTime -> 1
                else -> 0
            }
        }
        return infos
    }

    private suspend fun populateTexts(infos: List<ResolveInfo>) {
        infos.forEach {
            val key = it.activityInfo.name

            var text = textsCache[key]
            if (text == null) {
                text = it.loadLabel(packageManager).toString()
                textsCache[key] = text
            }

            channel.send(text)
        }
        channel.close()
    }
}

val ResolveInfo.imageUri: Uri?
    get() {
        val icon = activityInfo.iconResource
        return if (icon != 0 && activityInfo.packageName != null)
            Uri.parse("android.resource://" + activityInfo.packageName + "/" + icon) else null
    }

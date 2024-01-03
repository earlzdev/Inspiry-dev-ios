package app.inspiry.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import app.inspiry.activities.EditJsonActivity
import app.inspiry.core.data.Size
import app.inspiry.core.data.UserSavedTemplatePath
import app.inspiry.core.util.PickMediaResult
import app.inspiry.edit.instruments.PickedMediaType
import app.inspiry.logo.LogoActivity
import app.inspiry.music.model.TemplateMusic
import app.inspiry.removebg.RemovingBgActivity
import app.inspiry.textanim.TextAnimationsActivity
import app.inspiry.utils.Constants
import app.inspiry.utils.putTemplatePath
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.ui.MatisseActivity

class EditJsonParams(val templatePath: UserSavedTemplatePath, val templateJson: String?)

class EditJsonActivityResult : ActivityResultContract<EditJsonParams, Boolean>() {
    override fun createIntent(context: Context, input: EditJsonParams) =
        Intent(
            context,
            EditJsonActivity::class.java
        ).putTemplatePath(input.templatePath)
            .putExtra(Constants.EXTRA_JSON, input.templateJson)

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK
    }
}

class PickMusicActivityResult : ActivityResultContract<TemplateMusic?, MusicResult>() {
    override fun createIntent(context: Context, input: TemplateMusic?): Intent {
        val intent = Intent(
            context,
            app.inspiry.music.android.ui.MusicLibraryActivity::class.java
        )

        if (input != null)
            intent.putExtra(
                app.inspiry.music.android.ui.MusicLibraryActivity.EXTRA_INITIAL_STATE,
                input
            )

        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): MusicResult {

        if (resultCode == Activity.RESULT_OK) {

            val newMusic: TemplateMusic? = intent?.getParcelableExtra("data")
            return MusicResult(true, newMusic)
        } else {
            return MusicResult(false, null)
        }
    }
}

class PickTextAnimationActivityResult : ActivityResultContract<String?, String?>() {
    override fun createIntent(context: Context, input: String?): Intent {

        val intent = Intent(context, TextAnimationsActivity::class.java)
        if (input != null) intent.putExtra("preview_text", input)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return intent?.getStringExtra("animation_path")
    }
}

class MatisseActivityResult : ActivityResultContract<Unit, List<PickMediaResult>>() {
    override fun createIntent(context: Context, request: Unit): Intent {

        return Intent(context, MatisseActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<PickMediaResult> {

        if (resultCode != AppCompatActivity.RESULT_OK || intent == null)
            return emptyList()

        val uris = Matisse.obtainResult(intent).map { it.toString() }
        val size =
            intent.getParcelableArrayListExtra<Point>(MatisseActivity.EXTRA_RESULT_SELECTION_SIZES)!!
                .map { Size(it.x, it.y) }
        val types =
            intent.getStringArrayListExtra(MatisseActivity.EXTRA_RESULT_SELECTION_TYPES)!!.map {
                if (it.contains("video")) PickedMediaType.VIDEO
                else PickedMediaType.MEDIA
            }
        val mediaList = buildList {
            uris.forEachIndexed { i, _ ->
                add(PickMediaResult(uris[i], types[i], size[i]))
            }
        }
        if (uris.isEmpty())
            return emptyList()

        return mediaList
    }
}

class RemovingBgActivityResult : ActivityResultContract<RemoveBgActivityData, List<PickMediaResult>?>() {
    override fun createIntent(context: Context, input: RemoveBgActivityData): Intent {
        return Intent(
            context,
            RemovingBgActivity::class.java
        ).putExtra(RemovingBgActivity.EXTRA_IMAGE_PATHS, input.paths)
            .putExtra(Constants.EXTRA_SOURCE, input.source)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<PickMediaResult>? {
        if (resultCode == Activity.RESULT_OK)
            return intent?.getParcelableArrayListExtra(RemovingBgActivity.EXTRA_RESULT)
        return null
    }
}

class LogoActivityResult : ActivityResultContract<Unit, PickMediaResult?>() {

    override fun createIntent(context: Context, request: Unit): Intent {
        return Intent(context, LogoActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): PickMediaResult? {

        if (resultCode != AppCompatActivity.RESULT_OK || intent == null)
            return null

        val uri = intent.getStringExtra(LogoActivity.URI)!!
        val size = intent.getParcelableExtra<Size>(LogoActivity.SIZE)!!

        if (uri.isEmpty())
            return null

        return PickMediaResult(uri, PickedMediaType.MEDIA, size)
    }
}

class RemoveBgActivityData(val source: String, val paths: ArrayList<String>)
class MusicResult(val setMusic: Boolean, val music: TemplateMusic?)
class MatisseResult(val uris: List<Uri>, val types: List<String>)

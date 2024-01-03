package app.inspiry.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import app.inspiry.core.data.UserSavedTemplatePath
import app.inspiry.core.manager.FileReadWrite
import app.inspiry.core.serialization.TemplateSerializer
import app.inspiry.core.template.TemplateReadWrite
import app.inspiry.databinding.ActivityEditJsonBinding
import app.inspiry.utils.Constants
import app.inspiry.utils.getTemplatePath
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.koin.android.ext.android.inject


class EditJsonActivity : BaseThemeActivity() {

    private fun String.processJson(): String {
        return replace("\\/", "/")
    }

    private lateinit var binding: ActivityEditJsonBinding
    private val json: Json by inject()
    private val fileReadWrite: FileReadWrite by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditJsonBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        var templateStr: String? = intent.getStringExtra(Constants.EXTRA_JSON)
        val filePath = intent.getTemplatePath() as UserSavedTemplatePath

        if (templateStr.isNullOrEmpty()) {
            templateStr = fileReadWrite.readContentFromFiles(filePath.path)
        }

        binding.editJson.setText(JSONObject(templateStr).toString(3).processJson())

        binding.buttonSave.setOnClickListener {
            try {

                val newTxt = binding.editJson.text.toString()
                //in order to validate
                json.decodeFromString(TemplateSerializer, newTxt)

                fileReadWrite.writeContentToFile(newTxt, filePath.path)

                setResult(RESULT_OK)
                finish()

            } catch (e: Exception) {
                Toast.makeText(this, "Json is mailformed, " + e.message, Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        binding.buttonClear.setOnClickListener {
            binding.editJson.setText("")
        }

        binding.buttonCopy.setOnClickListener {
            binding.editJson.text.copy()
        }

        binding.buttonCopyText.setOnClickListener {
            val o = JSONObject(binding.editJson.text.toString())
            o.leaveOnlyTexts()
            o.toString(3).copy()
        }
    }

    private val onlyNecessaryKeys by lazy {
        setOf(
            "type",
            "text",
            "width",
            "height",
            "translationX",
            "translationY",
            "lineSpacing",
            "letterSpacing",
            "textSize",
            "rotation",
            "innerTranslationX",
            "innerTranslationY",
            "innerScale",
            "demoSource",
            "originalSource"
        )
    }


    private fun JSONObject.leaveOnlyTexts() {

        val iterRoot = this.keys()
        while (iterRoot.hasNext()) {
            val keyRoot = iterRoot.next()


            if (keyRoot == "medias") {
                val medias = getJSONArray(keyRoot)

                var i = 0
                while (i < medias.length()) {
                    val media = medias.getJSONObject(i)

                    val type = media.optString("type")
                    when (type) {
                        "text", "video", "image", "vector" -> {

                            val iterMedia = media.keys()
                            while (iterMedia.hasNext()) {
                                val mediaKey = iterMedia.next()
                                if (!onlyNecessaryKeys.contains(mediaKey)) {
                                    iterMedia.remove()
                                }
                            }
                        }
                        "group" -> {
                            media.leaveOnlyTexts()
                        }
                        else -> {
                            medias.remove(i)
                            i--
                        }
                    }
                    i++
                }
            } else if (keyRoot != "translationX" && keyRoot != "translationY" && keyRoot != "type") {
                iterRoot.remove()
            }
        }
    }

    private fun CharSequence.copy() {
        val clipboard: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("inspiry json", this)
        clipboard.setPrimaryClip(clip)
    }
}
package app.inspiry.core.helper

import com.russhwolf.settings.Settings
import kotlin.random.Random

class ABTestPicker(val settings: Settings, val abTestName: String) {

    fun pick(elements: List<String>): String {
        val prefKey = abTestName + "_variant"
        var res = settings.getString(prefKey)
        if (res.isEmpty()) {
            res = elements[Random.nextInt(from = 0, until = elements.size)]
            settings.putString(prefKey, res)
        }
        return res
    }
}
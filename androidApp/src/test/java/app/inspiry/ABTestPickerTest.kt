package app.inspiry

import app.inspiry.core.helper.ABTestPicker
import com.russhwolf.settings.MockSettings
import org.junit.Test

class ABTestPickerTest {

    val variants = listOf("1", "2", "3", "4", "5")
    val settings = MockSettings()

    @Test
    fun testABTestPicker() {
        val pickedVariants = mutableSetOf<String>()
        (0 until 1000).forEach {
            val picker = ABTestPicker(settings = settings, "some_name_$it")
            pickedVariants.add(picker.pick(variants))
        }

        assert(pickedVariants.toList().sorted() == variants) {
            "pickedVariants are $pickedVariants"
        }
    }

    @Test
    fun testPersistence() {

        val pickedVariants = mutableSetOf<String>()
        val picker = ABTestPicker(settings = settings, "some_name")
        (0 until 1000).forEach {
            pickedVariants.add(picker.pick(variants))
        }
        assert(pickedVariants.size == 1) {
            "pickedVariants are $pickedVariants"
        }
    }
}
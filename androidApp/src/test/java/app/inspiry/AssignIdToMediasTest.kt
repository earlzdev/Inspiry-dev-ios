package app.inspiry

import app.inspiry.core.media.*
import app.inspiry.core.template.MediaIdGeneratorImpl
import org.junit.Test
import kotlin.test.assertEquals

class AssignIdToMediasTest {
    /**
     * returns all ID with fixed size and single string
     */
    private fun getAllId(template: Template): String {
        var ids = ""
        template.medias.forEach { ids += (it.id).toString().padEnd(25) }
        return ids
    }

    @Test
    fun testGeneratingID() {
        val template = Template(name = "AssignIdToMediasTest")
        val testLayout = LayoutPosition("wrap_content", "wrap_content")

        //adding 15 medias (10 without ID and 5 with "testID_0".."testID_4"
        repeat(5) {
            template.medias.add(MediaText(layoutPosition = testLayout))
        }
        repeat(5) {
            template.medias.add(MediaImage(layoutPosition = testLayout, id = "testID_$it"))
        }
        repeat(5) {
            template.medias.add(MediaVector(layoutPosition = testLayout, originalSource = ""))
        }

        val mediaIdGenerator = MediaIdGeneratorImpl()
        var nextID = mediaIdGenerator.getNextAssignID(template.medias)
        assertEquals(1, nextID)

        val ids = getAllId(template = template)
        println("Original ID:")
        println(ids)
        assertEquals(INITIAL, ids)

        mediaIdGenerator.fillEmptyID(template.medias)
        val newIDs = getAllId(template = template)
        println("Assigned ID:")
        println(newIDs)
        assertEquals(ASSIGNED, newIDs)

        println("with new Media:")
        template.medias.add(MediaText(layoutPosition = testLayout))

        nextID = mediaIdGenerator.getNextAssignID(template.medias)
        assertEquals(11, nextID)

        mediaIdGenerator.fillEmptyID(template.medias)
        val additionalAssigned = getAllId(template = template)
        assertEquals(ADDITIONAL, additionalAssigned)
        println(additionalAssigned)

        nextID = mediaIdGenerator.getNextAssignID(template.medias)
        assertEquals(12, nextID)
    }

    companion object {
        const val INITIAL =
            """null                     null                     null                     null                     null                     testID_0                 testID_1                 testID_2                 testID_3                 testID_4                 null                     null                     null                     null                     null                     """
        const val ASSIGNED =
            """_AUTO_ASSIGNED_ID_1      _AUTO_ASSIGNED_ID_2      _AUTO_ASSIGNED_ID_3      _AUTO_ASSIGNED_ID_4      _AUTO_ASSIGNED_ID_5      testID_0                 testID_1                 testID_2                 testID_3                 testID_4                 _AUTO_ASSIGNED_ID_6      _AUTO_ASSIGNED_ID_7      _AUTO_ASSIGNED_ID_8      _AUTO_ASSIGNED_ID_9      _AUTO_ASSIGNED_ID_10     """
        const val ADDITIONAL =
            """_AUTO_ASSIGNED_ID_1      _AUTO_ASSIGNED_ID_2      _AUTO_ASSIGNED_ID_3      _AUTO_ASSIGNED_ID_4      _AUTO_ASSIGNED_ID_5      testID_0                 testID_1                 testID_2                 testID_3                 testID_4                 _AUTO_ASSIGNED_ID_6      _AUTO_ASSIGNED_ID_7      _AUTO_ASSIGNED_ID_8      _AUTO_ASSIGNED_ID_9      _AUTO_ASSIGNED_ID_10     _AUTO_ASSIGNED_ID_11     """
    }
}
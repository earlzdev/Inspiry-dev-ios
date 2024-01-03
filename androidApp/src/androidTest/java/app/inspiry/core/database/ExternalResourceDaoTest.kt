package app.inspiry.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import app.inspiry.core.database.data.ExternalResource
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ExternalResourceDaoTest {

    val dao = ExternalResourceDao(
        InspDatabase(
            DriverFactory(
                InstrumentationRegistry
                    .getInstrumentation().targetContext
            ).createDriver()
        )
    )

    val res1 = ExternalResource("external/test1", "local/test1", 1)
    val res2 = ExternalResource("external/test2", "local/test2", 1)


    @Before
    fun setUp() {
        dao.removeAll()
    }

    @Test
    fun testBasicOperations() {
        dao.onGetNewResource(res1.externalName, res1.path)

        assertEquals(listOf(res1), dao.selectAll())

        assertEquals(true, dao.onRemoveResource("unknown/name"))

        dao.getExistingResourceAndIncrementCount(existingName = res1.externalName)
        dao.getExistingResourceAndIncrementCount(existingName = res1.externalName)
        dao.getExistingResourceAndIncrementCount(existingName = res1.externalName)

        assertEquals(false, dao.onRemoveResource(res1.path))
        assertEquals(false, dao.onRemoveResource(res1.path))
        assertEquals(false, dao.onRemoveResource(res1.path))
        assertEquals(true, dao.onRemoveResource(res1.path))

        assert(dao.selectAll().isEmpty())
    }

    @Test
    fun testTemplateCopy() {
        dao.onGetNewResource(res1.externalName, res1.path)
        dao.onGetNewResource(res2.externalName, res2.path)

        dao.onTemplateOrMediaCopy(listOf(res1.path, res2.path))

        assertEquals(
            listOf(res1.copy(usagesCount = 2), res2.copy(usagesCount = 2)),
            dao.selectAll()
        )

        dao.removeAll()
    }
}
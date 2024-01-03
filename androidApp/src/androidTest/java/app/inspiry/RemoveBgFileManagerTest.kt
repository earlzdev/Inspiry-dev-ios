package app.inspiry

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.inspiry.removebg.RemoveBgFileManager
import okio.FileSystem
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@SmallTest
class RemoveBgFileManagerTest {

    val tag = "RemoveBgFileManagerTest"

    @Test
    fun test1() {

        val context: Context = ApplicationProvider.getApplicationContext()
        val userDir = context.getExternalFilesDir(Environment.DIRECTORY_DCIM)!!
        val userFile = File(userDir, "some_photo.jpg")

        Log.i(tag, "original path ${userFile.absolutePath}")
        val removeBgFile = RemoveBgFileManager.generateRemovedBgFile(FileSystem.SYSTEM)
        Log.i(tag, "saved $removeBgFile")

        assert(RemoveBgFileManager.isRemovedBgFile(removeBgFile)) {
            "path is ${removeBgFile}"
        }
    }
}
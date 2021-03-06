package ktx.assets

import com.badlogic.gdx.Files
import com.badlogic.gdx.Files.FileType
import com.badlogic.gdx.Files.FileType.*
import com.badlogic.gdx.files.FileHandle
import java.io.File

/**
 * Provides [FileHandle] instances based on [File].
 * @author MJ
 */
class MockFiles : Files { // Implementation note: believe it or not, this was easier to set up than Mockito.
  override fun classpath(path: String): FileHandle = getFileHandle(path, Classpath)
  override fun internal(path: String): FileHandle = getFileHandle(path, Internal)
  override fun local(path: String): FileHandle = getFileHandle(path, Local)
  override fun external(path: String): FileHandle = getFileHandle(path, External)
  override fun absolute(path: String): FileHandle = getFileHandle(path, Absolute)
  override fun getFileHandle(path: String, type: FileType): FileHandle = MockFileHandle(File(path), type)
  override fun isLocalStorageAvailable(): Boolean = false
  override fun getLocalStoragePath(): String = ""
  override fun isExternalStorageAvailable(): Boolean = false
  override fun getExternalStoragePath(): String = ""
}

/**
 * Exposes protected ([File], [FileType]) constructor of [FileHandle].
 * @author MJ
 */
class MockFileHandle(file: File, type: FileType?) : FileHandle(file, type)
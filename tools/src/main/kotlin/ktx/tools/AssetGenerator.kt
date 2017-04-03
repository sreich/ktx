package ktx.tools

import com.antwerkz.kibble.FileSourceWriter
import com.antwerkz.kibble.model.KibbleFile
import java.io.File

class AssetGenerator(val srcDir: String, val outputDir: String) {

    val kibbleFile = KibbleFile()
    val fileName = "$outputDir/test-gen-file.kt"

    //todo option to flatten dirs
    data class Asset(val propertyName: String, val fileName: String)

    fun generateTextures() {
        println("running with outputDir: $outputDir")

        val includePattern = listOf("png", "ttf", "txt")

        val textureClass = kibbleFile.addClass("Textures")

        ensureOutputDirExists()

        val srcDirFile = File(srcDir)
        val consideredFiles = srcDirFile.walk().filter(File::isFile)
                .filter { file ->
                    file.extension in includePattern
                }

        val assets = consideredFiles.map { file ->
            val nameWithoutExtension = file.name.removeFileExtension(file.extension)
                    //remove 9 patch extension, if any
                    .replace(".9", "")

            val propertyName = nameWithoutExtension.hyphensToUnderscores()

            //NOTE: texture packer turns undescored filenames into hyphens in the atlas
            //so we need the 'real name' to be hyphenated if it isn't.
            val assetName = nameWithoutExtension
            Asset(propertyName = propertyName, fileName = assetName)
        }

        assets.forEach { asset ->
            println("file: $asset")
            textureClass.addProperty(asset.propertyName, "String", asset.fileName.quote())
        }

        writeAssetSourceFile()

        println("typesafe asset generation complete")
    }

    private fun writeAssetSourceFile() {
        val file = File(fileName)

        FileSourceWriter(file).use {
            kibbleFile.toSource(it)
        }
    }

    // we can't have _'s in property names (well, we can with `` but it's terrible)
    private fun String.hyphensToUnderscores() = replace("-", "_")
    private fun String.quote() = """"$this""""
    private fun String.removeFileExtension(extension: String) = removeSuffix(".$extension")

    private fun ensureOutputDirExists() {
        //ensure output dirs are made if they don't exist yet
        File(outputDir).mkdirs()
    }
}

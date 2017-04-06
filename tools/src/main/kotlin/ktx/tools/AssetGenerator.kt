package ktx.tools

import com.antwerkz.kibble.FileSourceWriter
import com.antwerkz.kibble.model.KibbleClass
import com.antwerkz.kibble.model.KibbleFile
import java.io.File

class AssetGenerator(val srcDir: String, val outputDir: String) {

    val kibbleFile = KibbleFile()
    val outputFileName = "$outputDir/test-gen-file.kt"

    val flattenDirs = false

    data class Asset(val propertyName: String, val fileName: String)

    fun generateTextures() {
        println("running with srcDir: $srcDir, outputDir: $outputDir")

        val includePattern = listOf("png", "ttf", "txt")

        val textureClass = kibbleFile.addClass("Textures")

        ensureOutputDirExists()

        val srcDirFile = File(srcDir)

        generate(topLevelClass = textureClass, includePattern = includePattern,
                 srcDirFile = srcDirFile, assetForFileName = this::textureAssetForFileName)

        println("typesafe asset generation complete")

        writeAssetSourceFile()
    }

    fun generateFonts() {
//        println("fonts running with srcDir: $srcDir, outputDir: $outputDir")
//
//        val includePattern = listOf("ttf")
//
//        val fontClass = kibbleFile.addClass("Fonts")
//
//        ensureOutputDirExists()
//
//        val srcDirFile = File(srcDir)
//
//        generate(topLevelClass = fontClass, includePattern = includePattern,
//                 srcDirFile = srcDirFile, assetForFileName = this::textureAssetForFileName)
//
//        println("typesafe asset generation complete")
//
//        writeAssetSourceFile()
    }


    fun generate(topLevelClass: KibbleClass,
                 includePattern: List<String>,
                 srcDirFile: File, assetForFileName: (String, String) -> Asset) {

        var currentClass = topLevelClass
        val fileWalk = srcDirFile.walk()
                .onEnter { dir ->
                    if (flattenDirs) {
                        //flattening, no need to create a hierarchy of nested classes
                        return@onEnter true
                    }

                    if (dir.path == srcDir) {
                        //root dir, no need to set currentClass
                    } else {
                        //convert to unix seperators if not, remove srcdir from beginning and remove 1st slash
                        val path = dir.invariantSeparatorsPath.removePrefix(srcDir).removePrefix("/")
                        //split into each dir, so we can find the right nested classes for it
                        //so e.g. `Textures.ui.widgets.buttons.button1` can be a thing
                        path.split('/').forEach { dirPath ->
                            currentClass = topLevelClass.classOrNew(className = dirPath)
                        }
                    }

                    true
                }

        fileWalk.filter(File::isFile)
                .filter { file -> file.extension in includePattern }
                .forEach { file ->
                    println("foreach it: $file , currentclass: ${currentClass.name}")

                    val asset = assetForFileName(file.nameWithoutExtension, file.path)
                    val propertyValue = fileNameToClass(asset.fileName)
                    currentClass.addProperty(asset.propertyName, "String", propertyValue)

                    println("adding property (${asset.propertyName}) with value ($propertyValue) to class (${currentClass.name})")
                }


        println()
    }

    private fun fileNameToClass(fileName: String): String {
        //todo ...
        return fileName.quote()
    }

    fun textureAssetForFileName(fileNameWithoutExtension: String, path: String): Asset {
        println("textureAssetForFileName path: $path")
        //remove 9 patch extension, if any
        val name = fileNameWithoutExtension.replace(".9", "")

        val propertyName = name.hyphensToUnderscores()

        //NOTE: texture packer turns undescored filenames into hyphens in the atlas
        //so we need the 'real name' to be hyphenated if it isn't.
        val assetName = name
        return Asset(propertyName = propertyName, fileName = assetName)
    }

    private fun writeAssetSourceFile() {
        val file = File(outputFileName)

        FileSourceWriter(file).use {
            kibbleFile.toSource(it)
        }

        val a = file.readLines()
        println()
    }

    // we can't have hyphens in property names (well, we can with `` but it's terrible)
    private fun String.hyphensToUnderscores() = replace("-", "_")

    private fun String.quote() = """"$this""""

    private fun ensureOutputDirExists() {
        //ensure output dirs are made if they don't exist yet
        File(outputDir).mkdirs()
    }

    private fun KibbleClass.classOrNew(className: String): KibbleClass {
        var klass = nestedClasses.find { it.name == className }
        if (klass == null) {
            klass = this.addClass(className)
        }
        return klass
    }
}

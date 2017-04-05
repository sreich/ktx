package ktx.tools

import com.antwerkz.kibble.FileSourceWriter
import com.antwerkz.kibble.model.KibbleClass
import com.antwerkz.kibble.model.KibbleFile
import java.io.File

class AssetGenerator(val srcDir: String, val outputDir: String) {

    val kibbleFile = KibbleFile()
    val fileName = "$outputDir/test-gen-file.kt"

    //todo option to flatten dirs
    data class Asset(val propertyName: String, val fileName: String)

    fun generateTextures() {
        println("running with srcDir: $srcDir, outputDir: $outputDir")

        val includePattern = listOf("png", "ttf", "txt")

        val textureClass = kibbleFile.addClass("Textures")

        ensureOutputDirExists()

        val srcDirFile = File(srcDir)

        var currentClass = textureClass
        val consideredFiles3 = srcDirFile.walk()
                .onEnter { file ->
                    println("----- onEnter: file: $file, filename:${file.name}. path: ${file.path} ")

                    file.startsWith(srcDir)
                    if (file.path == srcDir) {
                        println("onEnter this is our root dir")
                        //root dir, no need to set currentClass
                    } else {
                        //convert to unix seperators if not, remove srcdir from current dir, and remove 1st slash
                        val path = file.invariantSeparatorsPath.removePrefix(srcDir).removePrefix("/")
                        //split into each dir, so we can find the right nesting classes for it
                        path.split('/').forEach {
                            currentClass = textureClass.classOrNew(className = it)
                            println()
                        }

                        if (currentClass.name == "ui") {
                            println()
                        }
                    }

                    true
                }

        consideredFiles3.filter { file -> true /*file.extension in includePattern */ }
                .filter(File::isFile)
                .forEach { file ->
                    println("foreach it: $file , currentclass: ${currentClass.name}")

                    val asset = textureAssetForFileName(file.nameWithoutExtension, file.path)
                    val propertyValue = asset.fileName.quote()
                    currentClass.addProperty(asset.propertyName, "String", propertyValue)
                    println("adding property (${asset.propertyName}) with value (${propertyValue}) to class (${currentClass.name})")
                }

//        val consideredFiles4 = srcDirFile.walk().filter(File::isFile)

//        val assets = consideredFiles
//                .map { textureAssetForFileName(it.nameWithoutExtension, it.path) }
//
//        assets.forEach { asset ->
//            println("file: $asset")
//            textureClass.addProperty(asset.propertyName, "String", asset.fileName.quote())
//        }

        writeAssetSourceFile()

        println("typesafe asset generation complete")
    }

    private fun fileNameToClass(name: String): String {
        //todo see if we want to handle true upper camel (multi word)
//        return name.first().toUpperCase() + name.removePrefix(name.first().toTitleCase())
        return name
    }

    private fun textureAssetForFileName(fileNameWithoutExtension: String, path: String): Asset {
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
        val file = File(fileName)

        FileSourceWriter(file).use {
            kibbleFile.toSource(it)
        }
    }

    // we can't have hyphens in property names (well, we can with `` but it's terrible)
    private fun String.hyphensToUnderscores() = replace("-", "_")

    private fun String.quote() = """"$this""""

    private fun ensureOutputDirExists() {
        //ensure output dirs are made if they don't exist yet
        File(outputDir).mkdirs()
    }
}

private fun KibbleClass.classOrNew(className: String): KibbleClass {
    var klass = nestedClasses.find { it.name == className }
    if (klass == null) {
        klass = this.addClass(className)
    }
    return klass
}

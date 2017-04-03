package ktx.tools

import org.junit.Test

class AssetGeneratorTest {
    /**
     * fixme: this is required for kotlin asset source generator or gradle daemon will crash
     * deep inside the jvm
     * System.setProperty("kotlin.colors.enabled", "false")
     * that can be placed in your build.gradle, or for unit tests..set as jvm property in IDEA
     *
     * see issues:
     * https://youtrack.jetbrains.com/issue/KT-17031
     * https://github.com/fusesource/jansi/issues/66
     * https://github.com/gradle/gradle/issues/778
     */
    @Test
    fun testGenerate()
    {
        val gen = AssetGenerator("build/resources/test", "build/generated/source")
        gen.generateTextures()
    }
}

package org.inkstack.parsers.util

import org.junit.jupiter.api.Test
import org.inkstack.parsers.MangaLoaderContextMock
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.newParser
import java.io.File

class IntentFilterGenerator {

    @Test
    fun generateIntentFilter() {
        val output = File("out/test/resources/intent-filter.xml")
        check(output.parentFile.mkdirs() || output.parentFile.isDirectory) {
            "Cannot create ${output.parentFile}"
        }
        output.printWriter(Charsets.UTF_8).use { writer ->
            writer.appendLine("<intent-filter android:autoVerify=\"false\">")
            writer.appendTab().appendLine("<action android:name=\"android.intent.action.VIEW\" />")
            writer.appendLine()
            writer.appendTab().appendLine("<category android:name=\"android.intent.category.DEFAULT\" />")
            writer.appendTab().appendLine("<category android:name=\"android.intent.category.BROWSABLE\" />")
            writer.appendLine()
            writer.appendTab().appendLine("<data android:scheme=\"http\" />")
            writer.appendTab().appendLine("<data android:scheme=\"https\" />")
            writer.appendLine()
            for (source in MangaParserSource.entries) {
                val parser = source.newParser(MangaLoaderContextMock)
                parser.configKeyDomain.presetValues.forEach { domain ->
                    writer.appendTab().append("<data android:host=\"").append(domain).appendLine("\" />")
                }
            }
            writer.appendLine()
            writer.appendLine("</intent-filter>")
        }
        println(output.absolutePath)
    }

    private fun Appendable.appendTab() = append('\t')
}

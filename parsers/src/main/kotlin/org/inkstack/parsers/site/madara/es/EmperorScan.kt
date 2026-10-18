package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("EMPERORSCAN", "EmperorScan", "es")
internal class EmperorScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.EMPERORSCAN, "imperiomanhua.com") {

	override val withoutAjax = true

	override val datePattern = "MMMM dd, yyyy"

	override val selectDesc = "div.summary_content div.post-content_item:has(h5:contains(Sinopsis)) div"
}

package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGATILKISI", "MangaTilkisi", "tr")
internal class MangaTilkisi(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGATILKISI, "www.mangatilkisi.net", pageSize = 18) {
	override val datePattern = "dd/MM/yyyy"
  }

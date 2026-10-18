package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("BORUTOEXPLORER", "BorutoExplorer", "pt")
internal class BorutoExplorer(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.BORUTOEXPLORER, "leitor.borutoexplorer.com.br", 10) {
	override val datePattern: String = "dd 'de' MMMMM 'de' yyyy"
}

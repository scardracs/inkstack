package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.Broken

@Broken("Original site closed")
@MangaSourceParser("STKISSMANGABLOG", "1StKissManga.net", "en")
internal class StkissMangaBlog(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.STKISSMANGABLOG, "1stkissmanga.org", 20) {
            override val postReq = true
      }

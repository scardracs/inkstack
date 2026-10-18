package org.inkstack.parsers.site.heancms.pt

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.*
import org.inkstack.parsers.site.heancms.HeanCms

@Broken
@MangaSourceParser("MODESCANLATOR", "ModeScanlator", "pt")
internal class ModeScanlator(context: MangaLoaderContext) :
	HeanCms(context, MangaParserSource.MODESCANLATOR, "site.modescanlator.net") {
	override val apiPath = "api.modescanlator.net"
}


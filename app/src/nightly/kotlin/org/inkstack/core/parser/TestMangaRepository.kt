package org.inkstack.core.parser

import org.inkstack.core.cache.MemoryContentCache
import org.inkstack.core.model.TestMangaSource
import org.inkstack.parsers.MangaLoaderContext

@Suppress("unused")
class TestMangaRepository(
	private val loaderContext: MangaLoaderContext,
	cache: MemoryContentCache
) : EmptyMangaRepository(TestMangaSource)

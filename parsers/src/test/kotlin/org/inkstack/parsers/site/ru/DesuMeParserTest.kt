package org.inkstack.parsers.site.ru

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.inkstack.parsers.MangaLoaderContextMock
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.util.generateUid
import org.inkstack.test_util.mangaOf

internal class DesuMeParserTest {

    @Test
    fun migrateLegacyApiMangaUrl() = runTest {
        val parser = MangaLoaderContextMock.newParserInstance(MangaParserSource.DESUME)
        val legacyManga = mangaOf(MangaParserSource.DESUME, "https://desu.uno/manga/api/2602").copy(
            id = parser.generateUid(2602L),
        )

        val details = parser.getDetails(legacyManga)

        assertEquals(legacyManga.id, details.id)
        assertFalse("/manga/api/" in details.url)
        assertTrue(details.url.endsWith(".2602/"))
        assertTrue(details.publicUrl.endsWith(".2602/"))
        assertFalse(details.chapters.isNullOrEmpty())
    }
}

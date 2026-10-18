package org.inkstack.core.exceptions

import org.inkstack.details.ui.pager.EmptyMangaReason
import org.inkstack.parsers.model.Manga

class EmptyMangaException(
    val reason: EmptyMangaReason?,
    val manga: Manga,
    cause: Throwable?
) : IllegalStateException(cause)

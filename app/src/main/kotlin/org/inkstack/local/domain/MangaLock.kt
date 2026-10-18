package org.inkstack.local.domain

import org.inkstack.core.util.MultiMutex
import org.inkstack.parsers.model.Manga
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MangaLock @Inject constructor() : MultiMutex<Manga>()

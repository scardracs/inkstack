package org.inkstack.scrobbling.common.domain

import org.inkstack.scrobbling.anilist.data.AniListRepository
import org.inkstack.scrobbling.common.data.ScrobblerRepository
import org.inkstack.scrobbling.common.domain.model.ScrobblerService
import org.inkstack.scrobbling.kitsu.data.KitsuRepository
import org.inkstack.scrobbling.mal.data.MALRepository
import org.inkstack.scrobbling.shikimori.data.ShikimoriRepository
import javax.inject.Inject
import javax.inject.Provider

class ScrobblerRepositoryMap @Inject constructor(
	private val shikimoriRepository: Provider<ShikimoriRepository>,
	private val aniListRepository: Provider<AniListRepository>,
	private val malRepository: Provider<MALRepository>,
	private val kitsuRepository: Provider<KitsuRepository>,
) {

	operator fun get(scrobblerService: ScrobblerService): ScrobblerRepository = when (scrobblerService) {
		ScrobblerService.SHIKIMORI -> shikimoriRepository
		ScrobblerService.ANILIST -> aniListRepository
		ScrobblerService.MAL -> malRepository
		ScrobblerService.KITSU -> kitsuRepository
	}.get()
}

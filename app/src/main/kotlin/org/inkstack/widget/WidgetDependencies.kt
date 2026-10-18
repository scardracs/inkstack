package org.inkstack.widget

import coil3.ImageLoader
import dagger.Lazy
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.inkstack.core.prefs.AppSettings
import org.inkstack.favourites.domain.FavouritesRepository
import org.inkstack.history.data.HistoryRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetDependencies {
	fun favouritesRepository(): FavouritesRepository
	fun historyRepository(): HistoryRepository
	fun appSettings(): AppSettings
	fun imageLoader(): Lazy<ImageLoader>
}

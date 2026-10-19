package org.inkstack.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.inkstack.bookmarks.data.BookmarkEntity
import org.inkstack.bookmarks.data.BookmarksDao
import org.inkstack.core.db.dao.ChaptersDao
import org.inkstack.core.db.dao.MangaDao
import org.inkstack.core.db.dao.MangaSourcesDao
import org.inkstack.core.db.dao.PreferencesDao
import org.inkstack.core.db.dao.TagsDao
import org.inkstack.core.db.dao.TrackLogsDao
import org.inkstack.core.db.entity.ChapterEntity
import org.inkstack.core.db.entity.MangaEntity
import org.inkstack.core.db.entity.MangaPrefsEntity
import org.inkstack.core.db.entity.MangaSourceEntity
import org.inkstack.core.db.entity.MangaTagsEntity
import org.inkstack.core.db.entity.TagEntity
import org.inkstack.core.db.migrations.Migration10To11
import org.inkstack.core.db.migrations.Migration11To12
import org.inkstack.core.db.migrations.Migration12To13
import org.inkstack.core.db.migrations.Migration13To14
import org.inkstack.core.db.migrations.Migration14To15
import org.inkstack.core.db.migrations.Migration15To16
import org.inkstack.core.db.migrations.Migration16To17
import org.inkstack.core.db.migrations.Migration17To18
import org.inkstack.core.db.migrations.Migration18To19
import org.inkstack.core.db.migrations.Migration19To20
import org.inkstack.core.db.migrations.Migration1To2
import org.inkstack.core.db.migrations.Migration20To21
import org.inkstack.core.db.migrations.Migration21To22
import org.inkstack.core.db.migrations.Migration22To23
import org.inkstack.core.db.migrations.Migration23To24
import org.inkstack.core.db.migrations.Migration24To23
import org.inkstack.core.db.migrations.Migration24To25
import org.inkstack.core.db.migrations.Migration25To26
import org.inkstack.core.db.migrations.Migration26To27
import org.inkstack.core.db.migrations.Migration27To28
import org.inkstack.core.db.migrations.Migration2To3
import org.inkstack.core.db.migrations.Migration3To4
import org.inkstack.core.db.migrations.Migration4To5
import org.inkstack.core.db.migrations.Migration5To6
import org.inkstack.core.db.migrations.Migration6To7
import org.inkstack.core.db.migrations.Migration7To8
import org.inkstack.core.db.migrations.Migration8To9
import org.inkstack.core.db.migrations.Migration9To10
import org.inkstack.core.util.ext.processLifecycleScope
import org.inkstack.explore.data.SourcePresetEntity
import org.inkstack.explore.data.SourcePresetsDao
import org.inkstack.favourites.data.FavouriteCategoriesDao
import org.inkstack.favourites.data.FavouriteCategoryEntity
import org.inkstack.favourites.data.FavouriteEntity
import org.inkstack.favourites.data.FavouritesDao
import org.inkstack.history.data.HistoryDao
import org.inkstack.history.data.HistoryEntity
import org.inkstack.local.data.index.LocalMangaIndexDao
import org.inkstack.local.data.index.LocalMangaIndexEntity
import org.inkstack.scrobbling.common.data.ScrobblingDao
import org.inkstack.scrobbling.common.data.ScrobblingEntity
import org.inkstack.stats.data.StatsDao
import org.inkstack.stats.data.StatsEntity
import org.inkstack.suggestions.data.SuggestionDao
import org.inkstack.suggestions.data.SuggestionEntity
import org.inkstack.tracker.data.TrackEntity
import org.inkstack.tracker.data.TrackLogEntity
import org.inkstack.tracker.data.TracksDao

const val DATABASE_VERSION = 28

@Database(
	entities = [
		MangaEntity::class, TagEntity::class, HistoryEntity::class, MangaTagsEntity::class, ChapterEntity::class,
		FavouriteCategoryEntity::class, FavouriteEntity::class, MangaPrefsEntity::class, TrackEntity::class,
		TrackLogEntity::class, SuggestionEntity::class, BookmarkEntity::class, ScrobblingEntity::class,
		MangaSourceEntity::class, StatsEntity::class, LocalMangaIndexEntity::class,
		SourcePresetEntity::class,
	],
	version = DATABASE_VERSION,
)
abstract class MangaDatabase : RoomDatabase() {

	abstract fun getHistoryDao(): HistoryDao

	abstract fun getTagsDao(): TagsDao

	abstract fun getMangaDao(): MangaDao

	abstract fun getFavouritesDao(): FavouritesDao

	abstract fun getPreferencesDao(): PreferencesDao

	abstract fun getFavouriteCategoriesDao(): FavouriteCategoriesDao

	abstract fun getTracksDao(): TracksDao

	abstract fun getTrackLogsDao(): TrackLogsDao

	abstract fun getSuggestionDao(): SuggestionDao

	abstract fun getBookmarksDao(): BookmarksDao

	abstract fun getScrobblingDao(): ScrobblingDao

	abstract fun getSourcesDao(): MangaSourcesDao

	abstract fun getStatsDao(): StatsDao

	abstract fun getLocalMangaIndexDao(): LocalMangaIndexDao

	abstract fun getChaptersDao(): ChaptersDao

	abstract fun getSourcePresetsDao(): SourcePresetsDao
}

fun getDatabaseMigrations(context: Context): Array<Migration> = arrayOf(
	Migration1To2(),
	Migration2To3(),
	Migration3To4(),
	Migration4To5(),
	Migration5To6(),
	Migration6To7(),
	Migration7To8(),
	Migration8To9(),
	Migration9To10(),
	Migration10To11(),
	Migration11To12(),
	Migration12To13(),
	Migration13To14(),
	Migration14To15(),
	Migration15To16(),
	Migration16To17(context),
	Migration17To18(),
	Migration18To19(),
	Migration19To20(),
	Migration20To21(),
	Migration21To22(),
	Migration22To23(),
	Migration23To24(),
	Migration24To23(),
	Migration24To25(),
	Migration25To26(),
	Migration26To27(),
	Migration27To28(),
)

fun MangaDatabase(context: Context): MangaDatabase = Room
	.databaseBuilder(context, MangaDatabase::class.java, "inkstack-db")
	.addMigrations(*getDatabaseMigrations(context))
	.addCallback(DatabasePrePopulateCallback(context.resources))
	.build()

@OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
fun InvalidationTracker.removeObserverAsync(observer: InvalidationTracker.Observer) {
	val scope = processLifecycleScope
	if (scope.isActive) {
		processLifecycleScope.launch(Dispatchers.Default, CoroutineStart.ATOMIC) {
			removeObserver(observer)
		}
	}
}

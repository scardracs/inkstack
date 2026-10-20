package org.inkstack.settings.search

import android.content.Context
import dagger.Reusable
import org.inkstack.core.LocalizedAppContext
import javax.inject.Inject

/**
 * Builds the settings search index from a static Compose-oriented catalog
 * (no Preference XML inflation).
 */
@Reusable
class SettingsSearchHelper @Inject constructor(
	@LocalizedAppContext private val context: Context,
) {

	fun inflatePreferences(): List<SettingsItem> = SettingsSearchCatalog.inflate(context)
}

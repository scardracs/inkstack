package org.inkstack.parsers

import org.inkstack.parsers.config.ConfigKey
import org.inkstack.parsers.config.MangaSourceConfig

internal class SourceConfigMock : MangaSourceConfig {

	override fun <T> get(key: ConfigKey<T>): T = key.defaultValue
}
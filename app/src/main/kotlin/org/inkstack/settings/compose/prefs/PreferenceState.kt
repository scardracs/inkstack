package org.inkstack.settings.compose.prefs

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.preference.PreferenceManager

@Composable
fun rememberSharedPreferences(): SharedPreferences {
	val context = LocalContext.current
	return remember(context) { PreferenceManager.getDefaultSharedPreferences(context) }
}

@Composable
fun rememberBooleanPref(
	prefs: SharedPreferences,
	key: String,
	default: Boolean,
): MutableState<Boolean> {
	val state = remember(key) { mutableStateOf(prefs.getBoolean(key, default)) }
	DisposableEffect(prefs, key) {
		val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changed ->
			if (changed == key) state.value = prefs.getBoolean(key, default)
		}
		prefs.registerOnSharedPreferenceChangeListener(listener)
		onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
	}
	return object : MutableState<Boolean> by state {
		override var value: Boolean
			get() = state.value
			set(value) {
				state.value = value
				prefs.edit { putBoolean(key, value) }
			}
	}
}

@Composable
fun rememberStringPref(
	prefs: SharedPreferences,
	key: String,
	default: String?,
): MutableState<String> {
	val state = remember(key) { mutableStateOf(prefs.getString(key, default).orEmpty()) }
	DisposableEffect(prefs, key) {
		val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changed ->
			if (changed == key) state.value = prefs.getString(key, default).orEmpty()
		}
		prefs.registerOnSharedPreferenceChangeListener(listener)
		onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
	}
	return object : MutableState<String> by state {
		override var value: String
			get() = state.value
			set(value) {
				state.value = value
				prefs.edit { putString(key, value) }
			}
	}
}

@Composable
fun rememberStringSetPref(
	prefs: SharedPreferences,
	key: String,
	default: Set<String>,
): MutableState<Set<String>> {
	val state = remember(key) {
		mutableStateOf(prefs.getStringSet(key, default)?.toSet() ?: default)
	}
	DisposableEffect(prefs, key) {
		val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changed ->
			if (changed == key) {
				state.value = prefs.getStringSet(key, default)?.toSet() ?: default
			}
		}
		prefs.registerOnSharedPreferenceChangeListener(listener)
		onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
	}
	return object : MutableState<Set<String>> by state {
		override var value: Set<String>
			get() = state.value
			set(value) {
				state.value = value
				prefs.edit { putStringSet(key, value) }
			}
	}
}

@Composable
fun rememberIntPref(
	prefs: SharedPreferences,
	key: String,
	default: Int,
): MutableState<Int> {
	val state = remember(key) { mutableStateOf(prefs.getInt(key, default)) }
	DisposableEffect(prefs, key) {
		val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changed ->
			if (changed == key) state.value = prefs.getInt(key, default)
		}
		prefs.registerOnSharedPreferenceChangeListener(listener)
		onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
	}
	return object : MutableState<Int> by state {
		override var value: Int
			get() = state.value
			set(value) {
				state.value = value
				prefs.edit { putInt(key, value) }
			}
	}
}

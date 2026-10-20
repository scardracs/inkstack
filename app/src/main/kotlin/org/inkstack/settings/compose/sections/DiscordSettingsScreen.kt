package org.inkstack.settings.compose.sections

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.inkstack.R
import org.inkstack.core.prefs.AppSettings
import org.inkstack.scrobbling.discord.ui.DiscordAuthActivity
import org.inkstack.settings.compose.prefs.EditTextPreferenceItem
import org.inkstack.settings.compose.prefs.SwitchPreferenceItem
import org.inkstack.settings.compose.prefs.rememberSharedPreferences
import org.inkstack.settings.discord.DiscordSettingsViewModel
import org.inkstack.settings.discord.TokenState

@Composable
fun DiscordSettingsScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: DiscordSettingsViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val prefs = rememberSharedPreferences()
	var rpc by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_DISCORD_RPC, false)) }
	var skipNsfw by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_DISCORD_RPC_SKIP_NSFW, false)) }
	var token by remember { mutableStateOf(prefs.getString(AppSettings.KEY_DISCORD_TOKEN, "").orEmpty()) }
	val tokenState by viewModel.tokenState.collectAsState()

	val tokenSummary = when (tokenState.first) {
		TokenState.EMPTY, TokenState.REQUIRED -> stringResource(R.string.discord_token_summary)
		TokenState.INVALID -> stringResource(R.string.invalid_token, tokenState.second.orEmpty())
		TokenState.VALID -> tokenState.second.orEmpty()
		TokenState.CHECKING -> stringResource(R.string.loading_)
	}

	SettingsSectionScaffold(titleRes = R.string.discord, onBack = onBack, modifier = modifier) {
		item {
			SwitchPreferenceItem(
				titleRes = R.string.discord_rpc,
				iconRes = R.drawable.ic_discord,
				checked = rpc,
				onCheckedChange = {
					rpc = it
					prefs.edit { putBoolean(AppSettings.KEY_DISCORD_RPC, it) }
				},
			)
		}
		if (rpc) {
			item {
				EditTextPreferenceItem(
					titleRes = R.string.discord_token,
					value = token,
					summary = tokenSummary,
					iconRes = R.drawable.ic_lock,
					dialogMessage = stringResource(
						R.string.discord_token_description,
						stringResource(R.string.sign_in),
					),
					neutralLabelRes = R.string.sign_in,
					onNeutral = {
						context.startActivity(Intent(context, DiscordAuthActivity::class.java))
					},
					onValueChange = {
						token = it
						prefs.edit { putString(AppSettings.KEY_DISCORD_TOKEN, it) }
					},
				)
			}
			item {
				SwitchPreferenceItem(
					titleRes = R.string.disable_nsfw,
					summaryRes = R.string.rpc_skip_nsfw_summary,
					iconRes = R.drawable.ic_sfw,
					checked = skipNsfw,
					onCheckedChange = {
						skipNsfw = it
						prefs.edit { putBoolean(AppSettings.KEY_DISCORD_RPC_SKIP_NSFW, it) }
					},
				)
			}
		}
	}
}

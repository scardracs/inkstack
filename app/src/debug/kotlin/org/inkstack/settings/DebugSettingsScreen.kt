package org.inkstack.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import leakcanary.LeakCanary
import org.inkstack.InkstackApp
import org.inkstack.R
import org.inkstack.core.model.TestMangaSource
import org.inkstack.core.nav.router
import org.inkstack.core.util.ext.findActivity
import org.inkstack.settings.compose.sections.SettingsSectionScaffold
import org.koitharu.workinspector.WorkInspector

@Composable
fun DebugSettingsScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	val app = context.applicationContext as InkstackApp
	var leakEnabled by remember { mutableStateOf(app.isLeakCanaryEnabled) }

	SettingsSectionScaffold(
		titleRes = R.string.debug,
		onBack = onBack,
		modifier = modifier,
	) {
		item {
			ListItem(
				headlineContent = { Text("LeakCanary") },
				trailingContent = {
					Switch(
						checked = leakEnabled,
						onCheckedChange = {
							leakEnabled = it
							app.isLeakCanaryEnabled = it
						},
					)
				},
				modifier = Modifier
					.fillMaxWidth()
					.clickable {
						context.startActivity(LeakCanary.newLeakDisplayActivityIntent())
					},
			)
		}
		item {
			ListItem(
				headlineContent = { Text("Work Inspector") },
				modifier = Modifier
					.fillMaxWidth()
					.clickable {
						context.startActivity(WorkInspector.getIntent(context))
					},
			)
		}
		item {
			ListItem(
				headlineContent = { Text(stringResource(R.string.test_parser)) },
				modifier = Modifier
					.fillMaxWidth()
					.clickable {
						(context.findActivity() as? FragmentActivity)?.router?.openList(
							TestMangaSource,
							null,
							null,
						)
					},
			)
		}
	}
}

package org.inkstack.settings.compose.prefs

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceCategoryHeader(@StringRes titleRes: Int) {
	Text(
		text = stringResource(titleRes),
		style = MaterialTheme.typography.titleSmall,
		color = MaterialTheme.colorScheme.primary,
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 12.dp),
	)
}

@Composable
fun SwitchPreferenceItem(
	@StringRes titleRes: Int,
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier,
	@StringRes summaryRes: Int? = null,
	summary: String? = null,
	@DrawableRes iconRes: Int? = null,
	enabled: Boolean = true,
) {
	ListItem(
		headlineContent = { Text(stringResource(titleRes)) },
		supportingContent = {
			val text = summary ?: summaryRes?.let { stringResource(it) }
			if (text != null) Text(text)
		},
		leadingContent = iconRes?.let {
			{
				Icon(painter = painterResource(it), contentDescription = null)
			}
		},
		trailingContent = {
			Switch(
				checked = checked,
				onCheckedChange = onCheckedChange,
				enabled = enabled,
			)
		},
		modifier = modifier
			.fillMaxWidth()
			.clickable(enabled = enabled) { onCheckedChange(!checked) },
	)
}

@Composable
fun NavigationPreferenceItem(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	@StringRes titleRes: Int? = null,
	title: String? = null,
	@StringRes summaryRes: Int? = null,
	summary: String? = null,
	@DrawableRes iconRes: Int? = null,
	enabled: Boolean = true,
) {
	val headline = title ?: titleRes?.let { stringResource(it) }.orEmpty()
	ListItem(
		headlineContent = { Text(headline) },
		supportingContent = {
			val text = summary ?: summaryRes?.let { stringResource(it) }
			if (text != null) Text(text)
		},
		leadingContent = iconRes?.let {
			{
				Icon(painter = painterResource(it), contentDescription = null)
			}
		},
		modifier = modifier
			.fillMaxWidth()
			.clickable(enabled = enabled, onClick = onClick),
	)
}

@Composable
fun InfoPreferenceItem(
	summary: String,
	modifier: Modifier = Modifier,
	@DrawableRes iconRes: Int? = null,
) {
	ListItem(
		headlineContent = { },
		supportingContent = { Text(summary) },
		leadingContent = iconRes?.let {
			{
				Icon(painter = painterResource(it), contentDescription = null)
			}
		},
		modifier = modifier.fillMaxWidth(),
	)
}

@Composable
fun PreferenceSectionColumn(
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit,
) {
	Column(modifier = modifier.fillMaxWidth(), content = { content() })
}

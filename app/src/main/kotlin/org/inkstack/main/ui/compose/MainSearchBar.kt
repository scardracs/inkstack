package org.inkstack.main.ui.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackGlassShape
import org.inkstack.core.ui.compose.InkstackGlassSurface
import org.inkstack.core.ui.compose.inkstackGlassContentColor
import org.inkstack.core.ui.compose.inkstackGlassContentColorMuted


data class MainOverflowItem(
	val id: String,
	val titleRes: Int,
	val checked: Boolean? = null,
	val visible: Boolean = true,
	val onClick: () -> Unit,
)

@Composable
fun MainSearchBar(
	query: String,
	onQueryChange: (String) -> Unit,
	onSearch: (String) -> Unit,
	menuItems: List<MainOverflowItem>,
	modifier: Modifier = Modifier,
) {
	var menuExpanded by remember { mutableStateOf(false) }
	val visibleItems = menuItems.filter { it.visible }
	InkstackGlassSurface(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp),
		shape = InkstackGlassShape,
	) {
		val content = inkstackGlassContentColor()
		val muted = inkstackGlassContentColorMuted()
		TextField(
			value = query,
			onValueChange = onQueryChange,
			modifier = Modifier.fillMaxWidth(),
			placeholder = { Text(stringResource(R.string.search_manga), color = muted) },
			leadingIcon = {
				IconButton(onClick = { onSearch(query) }) {
					Icon(
						Icons.Filled.Search,
						contentDescription = stringResource(R.string.search_manga),
						tint = content,
					)
				}
			},
			trailingIcon = {
				IconButton(onClick = { menuExpanded = true }) {
					Icon(
						Icons.Filled.MoreVert,
						contentDescription = stringResource(R.string.more),
						tint = content,
					)
				}
				DropdownMenu(
					expanded = menuExpanded,
					onDismissRequest = { menuExpanded = false },
				) {
					visibleItems.forEach { item ->
						DropdownMenuItem(
							text = { Text(stringResource(item.titleRes)) },
							onClick = {
								menuExpanded = false
								item.onClick()
							},
							trailingIcon = item.checked?.let { checked ->
								{
									Checkbox(
										checked = checked,
										onCheckedChange = null,
									)
								}
							},
						)
					}
				}
			},
			singleLine = true,
			keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
			keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
			colors = TextFieldDefaults.colors(
				focusedContainerColor = Color.Transparent,
				unfocusedContainerColor = Color.Transparent,
				disabledContainerColor = Color.Transparent,
				focusedIndicatorColor = Color.Transparent,
				unfocusedIndicatorColor = Color.Transparent,
				disabledIndicatorColor = Color.Transparent,
				focusedTextColor = content,
				unfocusedTextColor = content,
				cursorColor = content,
				focusedLeadingIconColor = content,
				unfocusedLeadingIconColor = content,
				focusedTrailingIconColor = content,
				unfocusedTrailingIconColor = content,
				focusedPlaceholderColor = muted,
				unfocusedPlaceholderColor = muted,
			),
		)
	}
}


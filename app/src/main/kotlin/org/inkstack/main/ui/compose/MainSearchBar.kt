package org.inkstack.main.ui.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

@Composable
fun MainSearchBar(
	query: String,
	onQueryChange: (String) -> Unit,
	onSearch: (String) -> Unit,
	onOpenSettings: () -> Unit,
	modifier: Modifier = Modifier,
) {
	var menuExpanded by remember { mutableStateOf(false) }
	Surface(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp),
		shape = RoundedCornerShape(28.dp),
		color = MaterialTheme.colorScheme.surfaceContainerHigh,
		tonalElevation = 0.dp,
		shadowElevation = 0.dp,
	) {
		TextField(
			value = query,
			onValueChange = onQueryChange,
			modifier = Modifier.fillMaxWidth(),
			placeholder = { Text(stringResource(R.string.search_manga)) },
			leadingIcon = {
				Icon(Icons.Filled.Search, contentDescription = null)
			},
			trailingIcon = {
				IconButton(onClick = { menuExpanded = true }) {
					Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.settings))
				}
				DropdownMenu(
					expanded = menuExpanded,
					onDismissRequest = { menuExpanded = false },
				) {
					DropdownMenuItem(
						text = { Text(stringResource(R.string.settings)) },
						onClick = {
							menuExpanded = false
							onOpenSettings()
						},
					)
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
			),
		)
	}
}

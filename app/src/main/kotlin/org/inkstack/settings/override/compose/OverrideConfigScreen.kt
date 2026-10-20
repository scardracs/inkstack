package org.inkstack.settings.override.compose

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.alternatives.ui.covers.AlternativeCoversActivity
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.parsers.util.ifNullOrEmpty
import org.inkstack.settings.override.OverrideConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverrideConfigScreen(
	onBack: () -> Unit,
	onSaved: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: OverrideConfigViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val data by viewModel.data.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	var name by remember { mutableStateOf<String?>(null) }
	var error by remember { mutableStateOf<String?>(null) }

	val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
		if (uri != null) {
			if (uri.host?.startsWith(context.packageName) != true) {
				context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
			}
			viewModel.updateCover(uri.toString())
		}
	}
	val pickAlt = rememberLauncherForActivityResult(AlternativeCoversActivity.Contract()) { url ->
		if (url != null) viewModel.updateCover(url)
	}

	LaunchedEffect(data) {
		val override = data?.second
		if (name == null && override != null) {
			name = override.title
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onSaved.collectLatest { event ->
			event?.consume { onSaved() }
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { e -> error = e.getDisplayMessage(context.resources) }
		}
	}

	val manga = data?.first
	val override = data?.second
	val coverUrl = override?.coverUrl.ifNullOrEmpty { manga?.coverUrl }

	InkstackScaffold(
		modifier = modifier,
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.name)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
					}
				},
				actions = {
					Button(
						onClick = {
							error = null
							viewModel.save(title = name?.trim())
						},
						enabled = !isLoading,
						modifier = Modifier.padding(end = 8.dp),
					) { Text(stringResource(R.string.save)) }
				},
				windowInsets = WindowInsets.statusBars,
				colors = TopAppBarDefaults.topAppBarColors(),
			)
		},
	) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.verticalScroll(rememberScrollState())
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			AsyncImage(
				model = coverUrl,
				contentDescription = null,
				modifier = Modifier
					.fillMaxWidth()
					.height(200.dp),
				contentScale = ContentScale.Fit,
			)
			OutlinedTextField(
				value = name.orEmpty(),
				onValueChange = { name = it },
				modifier = Modifier.fillMaxWidth(),
				label = { Text(stringResource(R.string.name)) },
				placeholder = { Text(manga?.title.orEmpty()) },
				enabled = !isLoading,
				singleLine = true,
			)
			OutlinedButton(
				onClick = {
					try {
						pickFile.launch(arrayOf("image/*"))
					} catch (_: Exception) {
						Toast.makeText(context, R.string.operation_not_supported, Toast.LENGTH_SHORT).show()
					}
				},
				modifier = Modifier.fillMaxWidth(),
			) { Text(stringResource(R.string.pick_custom_file)) }
			OutlinedButton(
				onClick = { manga?.let { pickAlt.launch(it) } },
				modifier = Modifier.fillMaxWidth(),
				enabled = manga != null,
			) { Text(stringResource(R.string.alternative_covers)) }
			OutlinedButton(
				onClick = { viewModel.updateCover(null) },
				modifier = Modifier.fillMaxWidth(),
				enabled = !override?.coverUrl.isNullOrEmpty(),
			) { Text(stringResource(R.string.use_default_cover)) }
			error?.let {
				Text(it, color = MaterialTheme.colorScheme.error)
			}
		}
	}
}

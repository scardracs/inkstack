package org.inkstack.sourcescore.ui

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.core.ui.compose.ComposeAlertDialogFragment
import org.inkstack.parsers.util.runCatchingCancellable
import org.inkstack.sourcescore.data.CommunityApi
import org.inkstack.sourcescore.data.CommunitySettings
import org.inkstack.sourcescore.data.IdentityResponse
import org.inkstack.sourcescore.data.nicknameRejection
import javax.inject.Inject

/**
 * Three-step introduction to the community features.
 *
 * Consent is asked for once, in order, and nothing is created before it is given: the identity is
 * generated lazily on first use, so declining at step one means the app never contacts the server and
 * no row exists anywhere (PLAN.md §1).
 */
@AndroidEntryPoint
class CommunityOnboardingDialog : ComposeAlertDialogFragment() {

	@Inject
	lateinit var settings: CommunitySettings

	@Inject
	lateinit var syncScheduler: SourceScoreSyncWorker.Scheduler

	@Inject
	lateinit var api: CommunityApi

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		isCancelable = false
	}

	@Composable
	override fun DialogContent(onDismissRequest: () -> Unit) {
		CommunityOnboardingContent(
			settings = settings,
			api = api,
			syncScheduler = syncScheduler,
			openBrowser = { url, title -> router.openExternalBrowser(url, title) },
			nameOnly = arguments?.getBoolean(ARG_NAME_ONLY) == true,
			onFinished = onDismissRequest,
		)
	}

	companion object {

		private const val TAG = "community_onboarding"
		private const val ARG_NAME_ONLY = "name_only"

		const val SPONSOR_URL = "https://github.com/sponsors/scardracs"

		fun showIfNeeded(context: Context, settings: CommunitySettings, fm: FragmentManager) {
			if (fm.findFragmentByTag(TAG) != null) return
			if (settings.isEnabled && settings.nickname.isNullOrEmpty()) {
				showNicknameStep(fm)
				return
			}
			if (settings.hasSeenOnboarding || settings.isEnabled) return
			CommunityOnboardingDialog().show(fm, TAG)
		}

		fun showNicknameStep(fm: FragmentManager) {
			if (fm.findFragmentByTag(TAG) != null) return
			CommunityOnboardingDialog().apply {
				arguments = Bundle(1).apply { putBoolean(ARG_NAME_ONLY, true) }
			}.show(fm, TAG)
		}

		fun show(fm: FragmentManager) {
			if (fm.findFragmentByTag(TAG) == null) CommunityOnboardingDialog().show(fm, TAG)
		}
	}
}

private const val TAG_LOG = "CommunityIdentity"
private const val STEP_INTRO = 0
private const val STEP_TELEMETRY = 1
private const val STEP_NICKNAME = 2
private const val STEP_SPONSOR = 3
private const val NICKNAME_MIN = 2
private const val SHARE_SERVER = 0.20f
private const val SHARE_TRANSLATION = 0.40f
private const val SHARE_CHARITY = 0.40f
private val COLOR_TRANSLATION = Color(0xFF4E9F5B)
private val COLOR_CHARITY = Color(0xFFC0504D)
private val COLOR_SERVER = Color(0xFF7A7A7A)

@Composable
private fun CommunityOnboardingContent(
	settings: CommunitySettings,
	api: CommunityApi,
	syncScheduler: SourceScoreSyncWorker.Scheduler,
	openBrowser: (url: String, title: String) -> Unit,
	nameOnly: Boolean,
	onFinished: () -> Unit,
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	var step by remember { mutableIntStateOf(if (nameOnly) STEP_NICKNAME else STEP_INTRO) }
	var restored by remember { mutableStateOf<IdentityResponse?>(null) }
	var nickname by remember { mutableStateOf("") }
	var nicknameError by remember { mutableStateOf<String?>(null) }
	var busy by remember { mutableStateOf(false) }
	var showRecover by remember { mutableStateOf(false) }

	fun advance(next: Int) {
		step = next
	}

	fun finish() {
		settings.hasSeenOnboarding = true
		syncScheduler.schedule()
		onFinished()
	}

	fun tryRestore() {
		if (settings.peekSecret() == null) return
		android.util.Log.i(TAG_LOG, "restore: asking the server whether it knows the saved key")
		scope.launch {
			val result = runCatchingCancellable { api.helloWithStatus() }.getOrNull() ?: run {
				android.util.Log.w(TAG_LOG, "restore: could not reach the server")
				return@launch
			}
			settings.isRegistered = true
			val identity = result.identity
			if (result.created || identity.nickname.isNullOrEmpty()) {
				android.util.Log.i(TAG_LOG, "restore: nothing to come back to, so we will ask as usual")
				return@launch
			}
			android.util.Log.i(TAG_LOG, "restore: recovered " + identity.displayName + " by key")
			restored = identity
			settings.nickname = identity.nickname
			settings.syncedNickname = identity.nickname
			if (nameOnly && step == STEP_NICKNAME) {
				finish()
			}
			Toast.makeText(
				context,
				context.getString(R.string.community_recover_done, identity.displayName),
				Toast.LENGTH_LONG,
			).show()
		}
	}

	LaunchedEffect(nameOnly) {
		if (nameOnly) tryRestore()
	}

	fun nextAfterTelemetry(): Int =
		if (restored?.nickname.isNullOrEmpty()) STEP_NICKNAME else STEP_SPONSOR

	fun onPositive() {
		when (step) {
			STEP_INTRO -> {
				settings.isEnabled = true
				tryRestore()
				settings.hasSeenOnboarding = true
				advance(STEP_TELEMETRY)
			}
			STEP_TELEMETRY -> {
				settings.isTelemetryEnabled = true
				advance(nextAfterTelemetry())
			}
			STEP_NICKNAME -> {
				val value = nickname.trim()
				nicknameError = when {
					value.isEmpty() -> context.getString(R.string.community_nickname_required)
					value.length < NICKNAME_MIN -> context.getString(R.string.community_nickname_too_short)
					value.any { !it.isLetterOrDigit() && !it.isWhitespace() && it != '_' && it != '-' } ->
						context.getString(R.string.community_nickname_invalid)
					else -> null
				}
				if (nicknameError != null) return
				busy = true
				scope.launch {
					val result = runCatchingCancellable { api.setNickname(value) }
					busy = false
					result.exceptionOrNull()?.let(::nicknameRejection)?.let { rejection ->
						nicknameError = nicknameRejectionMessage(context, rejection)
						return@launch
					}
					settings.nickname = value
					if (result.isSuccess) settings.syncedNickname = value
					if (nameOnly) finish() else advance(STEP_SPONSOR)
				}
			}
			STEP_SPONSOR -> {
				openBrowser(
					CommunityOnboardingDialog.SPONSOR_URL,
					context.getString(R.string.community_sponsor),
				)
				finish()
			}
		}
	}

	fun onNegative() {
		when (step) {
			STEP_INTRO -> {
				settings.isEnabled = false
				settings.isTelemetryEnabled = false
				finish()
			}
			STEP_TELEMETRY -> {
				settings.isTelemetryEnabled = false
				advance(nextAfterTelemetry())
			}
			STEP_NICKNAME -> advance(STEP_INTRO)
			STEP_SPONSOR -> finish()
		}
	}

	val (titleRes, iconRes, positiveRes, negativeRes) = when (step) {
		STEP_INTRO -> Quad(
			R.string.community_intro_title,
			R.drawable.ic_info_outline,
			R.string.community_intro_optin,
			R.string.community_intro_skip,
		)
		STEP_TELEMETRY -> Quad(
			R.string.community_telemetry_title,
			R.drawable.ic_chart,
			R.string.community_telemetry_yes,
			R.string.community_telemetry_no,
		)
		STEP_NICKNAME -> Quad(
			R.string.community_nickname_title,
			R.drawable.ic_user,
			R.string.community_nickname_continue,
			R.string.community_nickname_back,
		)
		else -> Quad(
			R.string.community_sponsor_title,
			R.drawable.ic_heart,
			R.string.community_sponsor_open,
			R.string.community_sponsor_dismiss,
		)
	}

	AlertDialog(
		onDismissRequest = {},
		properties = DialogProperties(
			dismissOnBackPress = false,
			dismissOnClickOutside = false,
		),
		title = null,
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.verticalScroll(rememberScrollState()),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Box(
					modifier = Modifier
						.size(72.dp)
						.clip(CircleShape)
						.background(MaterialTheme.colorScheme.secondaryContainer),
					contentAlignment = Alignment.Center,
				) {
					Icon(
						painter = painterResource(iconRes),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSecondaryContainer,
						modifier = Modifier.size(34.dp),
					)
				}
				Spacer(Modifier.height(20.dp))
				Text(
					text = stringResource(titleRes),
					style = MaterialTheme.typography.headlineSmall,
					textAlign = TextAlign.Center,
				)
				Spacer(Modifier.height(14.dp))
				StepDots(active = step, count = 4)
				Spacer(Modifier.height(20.dp))
				when (step) {
					STEP_INTRO -> IntroStep(serverUrl = settings.serverUrl, onOpenLink = openBrowser)
					STEP_TELEMETRY -> Text(
						stringResource(R.string.community_telemetry_body),
						style = MaterialTheme.typography.bodyMedium,
					)
					STEP_NICKNAME -> NicknameStep(
						nickname = nickname,
						error = nicknameError,
						onNicknameChange = {
							nickname = it
							nicknameError = null
						},
					)
					STEP_SPONSOR -> SponsorStep()
				}
			}
		},
		confirmButton = {
			TextButton(onClick = { onPositive() }, enabled = !busy) {
				Text(stringResource(positiveRes))
			}
		},
		dismissButton = {
			Row {
				if (step == STEP_INTRO) {
					TextButton(onClick = { showRecover = true }) {
						Text(stringResource(R.string.community_recover))
					}
				}
				TextButton(onClick = { onNegative() }, enabled = !busy) {
					Text(stringResource(negativeRes))
				}
			}
		},
	)

	if (showRecover) {
		RecoverDialog(
			settings = settings,
			api = api,
			onDismiss = { showRecover = false },
			onRestored = { identity ->
				showRecover = false
				restored = identity
				settings.isEnabled = true
				settings.hasSeenOnboarding = true
				Toast.makeText(
					context,
					context.getString(R.string.community_recover_done, identity.displayName),
					Toast.LENGTH_LONG,
				).show()
				syncScheduler.schedule()
				advance(STEP_TELEMETRY)
			},
		)
	}
}

@Composable
private fun IntroStep(serverUrl: String, onOpenLink: (String, String) -> Unit) {
	val terms = stringResource(R.string.community_terms)
	val privacy = stringResource(R.string.community_privacy)
	val policyText = stringResource(R.string.community_policy_links, terms, privacy)
	val linkColor = MaterialTheme.colorScheme.primary
	val annotated = remember(policyText, terms, privacy, linkColor) {
		buildAnnotatedString {
			val termsStart = policyText.indexOf(terms)
			val privacyStart = policyText.indexOf(privacy)
			var i = 0
			while (i < policyText.length) {
				when (i) {
					termsStart -> {
						pushStringAnnotation("terms", "$serverUrl/terms")
						withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
							append(terms)
						}
						pop()
						i += terms.length
					}
					privacyStart -> {
						pushStringAnnotation("privacy", "$serverUrl/privacy")
						withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
							append(privacy)
						}
						pop()
						i += privacy.length
					}
					else -> {
						append(policyText[i])
						i++
					}
				}
			}
		}
	}
	Column(Modifier.fillMaxWidth()) {
		Text(stringResource(R.string.community_intro_body), style = MaterialTheme.typography.bodyMedium)
		Spacer(Modifier.height(12.dp))
		Text(
			stringResource(R.string.community_age_notice),
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.primary,
		)
		Spacer(Modifier.height(8.dp))
		ClickableText(
			text = annotated,
			style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
			onClick = { offset ->
				annotated.getStringAnnotations(offset, offset).firstOrNull()?.let { ann ->
					val title = if (ann.tag == "terms") terms else privacy
					onOpenLink(ann.item, title)
				}
			},
		)
	}
}

@Composable
private fun NicknameStep(
	nickname: String,
	error: String?,
	onNicknameChange: (String) -> Unit,
) {
	Column(Modifier.fillMaxWidth()) {
		Text(stringResource(R.string.community_nickname_body), style = MaterialTheme.typography.bodyMedium)
		Spacer(Modifier.height(16.dp))
		OutlinedTextField(
			value = nickname,
			onValueChange = { if (it.length <= 24) onNicknameChange(it) },
			modifier = Modifier.fillMaxWidth(),
			label = { Text(stringResource(R.string.community_nickname)) },
			supportingText = {
				Text("${nickname.length}/24")
			},
			isError = error != null,
			singleLine = true,
		)
		error?.let {
			Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
		}
		val typed = nickname.trim()
		if (typed.isNotEmpty()) {
			Spacer(Modifier.height(4.dp))
			Text(
				stringResource(R.string.community_nickname_preview, "$typed#••••"),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
	}
}

@Composable
private fun SponsorStep() {
	Column(Modifier.fillMaxWidth()) {
		Text(stringResource(R.string.community_sponsor_body), style = MaterialTheme.typography.bodyMedium)
		Spacer(Modifier.height(16.dp))
		Surface(
			shape = MaterialTheme.shapes.extraLarge,
			color = MaterialTheme.colorScheme.surfaceContainerHigh,
		) {
			Row(
				modifier = Modifier.padding(16.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				DonationPieChart(Modifier.size(96.dp))
				Spacer(Modifier.width(16.dp))
				Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					LegendRow(COLOR_TRANSLATION, SHARE_TRANSLATION, R.string.community_sponsor_split_translation)
					LegendRow(COLOR_CHARITY, SHARE_CHARITY, R.string.community_sponsor_split_charity)
					LegendRow(COLOR_SERVER, SHARE_SERVER, R.string.community_sponsor_split_server)
				}
			}
		}
		Spacer(Modifier.height(12.dp))
		Text(
			stringResource(R.string.community_sponsor_reports),
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
	}
}

@Composable
private fun LegendRow(color: Color, share: Float, labelRes: Int) {
	Row(verticalAlignment = Alignment.CenterVertically) {
		Box(
			Modifier
				.size(8.dp)
				.clip(CircleShape)
				.background(color),
		)
		Spacer(Modifier.width(10.dp))
		Text(
			"${stringResource(R.string.percent_format, (share * 100).toInt())}  ${stringResource(labelRes)}",
			style = MaterialTheme.typography.bodySmall,
		)
	}
}

@Composable
private fun DonationPieChart(modifier: Modifier = Modifier) {
	val segments = listOf(
		COLOR_TRANSLATION to SHARE_TRANSLATION,
		COLOR_CHARITY to SHARE_CHARITY,
		COLOR_SERVER to SHARE_SERVER,
	)
	val hole = MaterialTheme.colorScheme.surfaceContainerHigh
	Canvas(modifier) {
		var start = -90f
		val diameter = size.minDimension
		val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
		val arcSize = Size(diameter, diameter)
		segments.forEach { (color, share) ->
			val sweep = share * 360f
			drawArc(color, start, sweep, useCenter = true, topLeft = topLeft, size = arcSize)
			start += sweep
		}
		drawCircle(hole, radius = diameter / 4f)
	}
}

@Composable
private fun StepDots(active: Int, count: Int) {
	val activeW = 22.dp
	val inactive = 8.dp
	Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
		repeat(count) { index ->
			val isActive = index == active
			Box(
				Modifier
					.height(8.dp)
					.width(if (isActive) activeW else inactive)
					.clip(RoundedCornerShape(50))
					.background(
						if (isActive) MaterialTheme.colorScheme.primary
						else MaterialTheme.colorScheme.outlineVariant,
					),
			)
		}
	}
}

@Composable
private fun RecoverDialog(
	settings: CommunitySettings,
	api: CommunityApi,
	onDismiss: () -> Unit,
	onRestored: (IdentityResponse) -> Unit,
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	var key by remember { mutableStateOf("") }
	var error by remember { mutableStateOf<String?>(null) }
	var busy by remember { mutableStateOf(false) }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(stringResource(R.string.community_recover_title)) },
		text = {
			Column {
				Text(stringResource(R.string.community_recover_body), style = MaterialTheme.typography.bodyMedium)
				Spacer(Modifier.height(16.dp))
				OutlinedTextField(
					value = key,
					onValueChange = {
						key = it
						error = null
					},
					modifier = Modifier.fillMaxWidth(),
					label = { Text(stringResource(R.string.community_recover_hint)) },
					isError = error != null,
					supportingText = error?.let { { Text(it) } },
					singleLine = false,
				)
			}
		},
		confirmButton = {
			TextButton(
				enabled = !busy,
				onClick = {
					val trimmed = key.trim()
					if (!settings.isPlausibleSecret(trimmed)) {
						error = context.getString(R.string.community_recover_malformed)
						return@TextButton
					}
					busy = true
					scope.launch {
						val previous = settings.peekSecret()
						if (!settings.adoptSecret(trimmed)) {
							error = context.getString(R.string.community_recover_unknown)
							busy = false
							return@launch
						}
						val result = runCatchingCancellable { api.helloWithStatus() }.getOrNull()
						if (result == null) {
							settings.revertSecret(previous)
							error = context.getString(R.string.community_recover_failed)
							busy = false
							return@launch
						}
						if (result.created) {
							settings.revertSecret(previous)
							error = context.getString(R.string.community_recover_unknown)
							busy = false
							return@launch
						}
						settings.isRegistered = true
						settings.nickname = result.identity.nickname
						settings.syncedNickname = result.identity.nickname
						settings.replyCursor = null
						settings.lastProbeUploadDay = Long.MIN_VALUE
						busy = false
						onRestored(result.identity)
					}
				},
			) { Text(stringResource(R.string.community_recover_action)) }
		},
		dismissButton = {
			TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
		},
	)
}

private data class Quad(val a: Int, val b: Int, val c: Int, val d: Int)

package org.inkstack.details.ui

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar
import org.inkstack.R
import org.inkstack.alternatives.ui.MangaUnavailableDialog
import org.inkstack.core.exceptions.CloudFlareProtectedException
import org.inkstack.core.exceptions.UnsupportedSourceException
import org.inkstack.core.exceptions.resolve.ErrorObserver
import org.inkstack.core.exceptions.resolve.ExceptionResolver
import org.inkstack.core.model.isLocal
import org.inkstack.core.nav.router
import org.inkstack.core.prefs.SourceSettings
import org.inkstack.core.util.ext.findCloudFlareException
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.isContentNotFound
import org.inkstack.core.util.ext.isNetworkError
import org.inkstack.core.util.ext.isSerializable
import org.inkstack.parsers.exception.NotFoundException
import org.inkstack.parsers.exception.ParseException
import org.inkstack.parsers.model.Manga

class DetailsErrorObserver(
	override val activity: FragmentActivity,
	private val viewModel: DetailsViewModel,
	private val resolver: ExceptionResolver?,
	host: View = activity.findViewById(android.R.id.content) ?: activity.window.decorView,
) : ErrorObserver(
	host, null, resolver,
	{ isResolved ->
		if (isResolved) {
			viewModel.reload()
		}
	},
) {

	private var unavailableDialog: MangaUnavailableDialog? = null

	init {
		activity.lifecycle.addObserver(
			object : DefaultLifecycleObserver {
				override fun onDestroy(owner: LifecycleOwner) {
					unavailableDialog?.dismiss()
					unavailableDialog = null
				}
			},
		)
	}

	override suspend fun emit(value: Throwable) {
		val cf = value.findCloudFlareException()
		if (cf is CloudFlareProtectedException && resolver != null) {
			val autoDisabled = SourceSettings(host.context, cf.source).isCaptchaAutoResolveDisabled
			if (!autoDisabled) {
				val resolved = resolver.resolve(cf, tryAutoResolve = true)
				if (resolved) {
					viewModel.reload()
					return
				}
			}
		}
		val missingManga = viewModel.getMangaOrNull()?.takeIf { !it.isLocal && value.isContentNotFound() }
		if (missingManga != null && showUnavailableDialog(missingManga)) {
			return
		}
		val snackbar = Snackbar.make(host, value.getDisplayMessage(host.context.resources), Snackbar.LENGTH_SHORT)
		if (value is NotFoundException || value is UnsupportedSourceException) {
			snackbar.duration = Snackbar.LENGTH_INDEFINITE
		}
		when {
			canResolve(value) -> {
				snackbar.setAction(ExceptionResolver.getResolveStringId(value)) {
					resolve(value)
				}
			}

			value is ParseException -> {
				val router = router()
				if (router != null && value.isSerializable()) {
					snackbar.setAction(R.string.details) {
						router.showErrorDialog(value)
					}
				}
			}

			value.isNetworkError() -> {
				snackbar.setAction(R.string.try_again) {
					viewModel.reload()
				}
			}
		}
		snackbar.show()
	}

	private fun showUnavailableDialog(manga: Manga): Boolean {
		if (unavailableDialog?.isShowing == true) {
			return true
		}
		if (activity.isDestroyed || activity.isFinishing) {
			return false
		}
		val router = activity.router
		unavailableDialog = MangaUnavailableDialog.Builder(activity, manga)
			.setOnAlternativesClickListener { router.openAlternatives(manga) }
			.create()
			.also { it.show() }
		return true
	}
}

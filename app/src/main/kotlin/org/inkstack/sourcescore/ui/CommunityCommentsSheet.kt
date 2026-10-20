package org.inkstack.sourcescore.ui

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.core.ui.compose.sheet.ComposeAdaptiveSheet
import org.inkstack.sourcescore.ui.compose.CommunityCommentsContent
import javax.inject.Inject

/**
 * The comment thread for one work, or one chapter of it.
 *
 * Compose adaptive host with a Compose body (no AndroidView / layout XML).
 */
@AndroidEntryPoint
class CommunityCommentsSheet : ComposeAdaptiveSheet() {

	@Inject
	lateinit var nameGate: CommunityNameGate

	private val viewModel by viewModels<CommunityCommentsViewModel>()

	@Composable
	override fun sheetTitle(): String = stringResource(R.string.community_comments)

	override fun onSheetViewCreated(savedInstanceState: Bundle?) {
		disableFitToContents()
		setExpanded(isExpanded = true, isLocked = false)
	}

	@Composable
	override fun SheetContent(onDismissRequest: () -> Unit) {
		val args = requireArguments()
		val workId = args.getString(ARG_WORK_ID) ?: return
		val chapterId = args.getLong(ARG_CHAPTER_ID, 0L).takeIf { it != 0L }
		val focusCommentId = args.getString(ARG_FOCUS_COMMENT_ID)
		CommunityCommentsContent(
			viewModel = viewModel,
			nameGate = nameGate,
			workId = workId,
			chapterId = chapterId,
			focusCommentId = focusCommentId,
			fragmentManager = parentFragmentManager,
		)
	}

	companion object {

		private const val TAG = "CommunityCommentsSheet"
		private const val ARG_WORK_ID = "work_id"
		private const val ARG_CHAPTER_ID = "chapter_id"
		private const val ARG_FOCUS_COMMENT_ID = "focus_comment_id"

		fun show(fm: FragmentManager, workId: String, chapterId: Long? = null, focusCommentId: String? = null) {
			(fm.findFragmentByTag(TAG) as? CommunityCommentsSheet)?.dismissAllowingStateLoss()
			CommunityCommentsSheet().apply {
				arguments = Bundle(3).apply {
					putString(ARG_WORK_ID, workId)
					chapterId?.let { putLong(ARG_CHAPTER_ID, it) }
					focusCommentId?.let { putString(ARG_FOCUS_COMMENT_ID, it) }
				}
			}.show(fm, TAG)
		}
	}
}

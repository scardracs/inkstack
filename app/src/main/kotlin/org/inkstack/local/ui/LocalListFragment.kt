package org.inkstack.local.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.inkstack.R
import org.inkstack.core.model.LocalMangaSource
import org.inkstack.core.nav.router
import org.inkstack.core.os.OpenDocumentTreeHelper
import org.inkstack.core.ui.list.ListSelectionController
import org.inkstack.core.ui.widgets.TipView
import org.inkstack.core.util.ShareHelper
import org.inkstack.core.util.ext.addMenuProvider
import org.inkstack.core.util.ext.observeEvent
import org.inkstack.core.util.ext.tryLaunch
import org.inkstack.databinding.FragmentListBinding
import org.inkstack.filter.ui.FilterCoordinator
import org.inkstack.list.ui.MangaListFragment
import org.inkstack.remotelist.ui.MangaSearchMenuProvider
import org.inkstack.search.ui.MangaListActivity
import org.inkstack.remotelist.ui.RemoteListFragment

class LocalListFragment : MangaListFragment(), FilterCoordinator.Owner {

	private val pickFileTreeLauncher = OpenDocumentTreeHelper(
		activityResultCaller = this,
		flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
			or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
			or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
	) { uri: Uri? ->
		if (uri != null) {
			viewModel.onCustomDirectoryPicked(uri)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val args = arguments ?: Bundle(1)
		args.putString(
			RemoteListFragment.ARG_SOURCE,
			LocalMangaSource.name,
		) // required by FilterCoordinator
		arguments = args
	}

	override val viewModel by viewModels<LocalListViewModel>()

	override val filterCoordinator: FilterCoordinator
		get() = viewModel.filterCoordinator

	override fun onViewBindingCreated(binding: FragmentListBinding, savedInstanceState: Bundle?) {
		super.onViewBindingCreated(binding, savedInstanceState)
		addMenuProvider(LocalListMenuProvider(this, this::onEmptyActionClick))
		addMenuProvider(
            MangaSearchMenuProvider(filterCoordinator, viewModel) { isSearchMode ->
                (activity as? MangaListActivity)?.setFilterHeaderVisible(!isSearchMode)
            },
        )
		viewModel.onMangaRemoved.observeEvent(viewLifecycleOwner) { onItemRemoved() }
	}

	override fun onEmptyActionClick() {
		router.showImportDialog()
	}

	override fun onFilterClick(view: View?) {
		router.showFilterSheet()
	}

	override fun onPrimaryButtonClick(tipView: TipView) {
		if (!pickFileTreeLauncher.tryLaunch(null)) {
			Snackbar.make(tipView, R.string.operation_not_supported, Snackbar.LENGTH_SHORT).show()
		}
	}

	override fun onSecondaryButtonClick(tipView: TipView) {
		router.openDirectoriesSettings()
	}

	override fun onScrolledToEnd() = viewModel.loadNextPage()

	override fun onCreateActionMode(
		controller: ListSelectionController,
		menuInflater: MenuInflater,
		menu: Menu,
	): Boolean {
		menuInflater.inflate(R.menu.mode_local, menu)
		return super.onCreateActionMode(controller, menuInflater, menu)
	}

	override fun onActionItemClicked(
		controller: ListSelectionController,
		mode: ActionMode?,
		item: MenuItem,
	): Boolean {
		return when (item.itemId) {
			R.id.action_remove -> {
				showDeletionConfirm(selectedItemsIds, mode)
				true
			}

			R.id.action_share -> {
				val files = selectedItems.map { it.url.toUri().toFile() }
				ShareHelper.shareCbz(requireContext(), files)
				mode?.finish()
				true
			}

			else -> super.onActionItemClicked(controller, mode, item)
		}
	}

	private fun showDeletionConfirm(ids: Set<Long>, mode: ActionMode?) {
		MaterialAlertDialogBuilder(context ?: return)
			.setTitle(R.string.delete_manga)
			.setMessage(getString(R.string.text_delete_local_manga_batch))
			.setPositiveButton(R.string.delete) { _, _ ->
				viewModel.delete(ids)
				mode?.finish()
			}
			.setNegativeButton(android.R.string.cancel, null)
			.show()
	}

	private fun onItemRemoved() {
		Snackbar.make(
			requireViewBinding().recyclerView,
			R.string.removal_completed,
			Snackbar.LENGTH_SHORT,
		).show()
	}
}

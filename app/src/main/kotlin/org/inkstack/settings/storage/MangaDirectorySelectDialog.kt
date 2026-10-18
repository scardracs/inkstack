package org.inkstack.settings.storage

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.core.exceptions.resolve.ToastErrorObserver
import org.inkstack.core.os.OpenDocumentTreeHelper
import org.inkstack.core.ui.AlertDialogFragment
import org.inkstack.core.ui.list.OnListItemClickListener
import org.inkstack.core.util.ext.observe
import org.inkstack.core.util.ext.observeEvent
import org.inkstack.core.util.ext.tryLaunch
import org.inkstack.databinding.DialogDirectorySelectBinding

@AndroidEntryPoint
class MangaDirectorySelectDialog : AlertDialogFragment<DialogDirectorySelectBinding>(),
	OnListItemClickListener<DirectoryModel> {

	private val viewModel: MangaDirectorySelectViewModel by viewModels()
	private val pickFileTreeLauncher = OpenDocumentTreeHelper(
		activityResultCaller = this,
		flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
			or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
			or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
	) {
		if (it != null) viewModel.onCustomDirectoryPicked(it)
	}
	private val permissionRequestLauncher = registerForActivityResult(
		RequestStorageManagerPermissionContract(),
	) {
		if (it) {
			viewModel.refresh()
			if (!pickFileTreeLauncher.tryLaunch(null)) {
				Toast.makeText(
					context ?: return@registerForActivityResult,
					R.string.operation_not_supported,
					Toast.LENGTH_SHORT,
				).show()
			}
		}
	}

	override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): DialogDirectorySelectBinding {
		return DialogDirectorySelectBinding.inflate(inflater, container, false)
	}

	override fun onViewBindingCreated(binding: DialogDirectorySelectBinding, savedInstanceState: Bundle?) {
		super.onViewBindingCreated(binding, savedInstanceState)
		val adapter = AsyncListDifferDelegationAdapter(DirectoryDiffCallback(), directoryAD(this))
		binding.root.adapter = adapter
		viewModel.items.observe(viewLifecycleOwner) { adapter.items = it }
		viewModel.onDismissDialog.observeEvent(viewLifecycleOwner) { dismiss() }
		viewModel.onPickDirectory.observeEvent(viewLifecycleOwner) { pickCustomDirectory() }
		viewModel.onError.observeEvent(viewLifecycleOwner, ToastErrorObserver(binding.root, this))
	}

	override fun onBuildDialog(builder: MaterialAlertDialogBuilder): MaterialAlertDialogBuilder {
		return super.onBuildDialog(builder)
			.setCancelable(true)
			.setTitle(R.string.manga_save_location)
			.setNegativeButton(android.R.string.cancel, null)
	}

	override fun onItemClick(item: DirectoryModel, view: View) {
		viewModel.onItemClick(item)
	}

	private fun pickCustomDirectory() {
		val launched = if (Environment.isExternalStorageManager()) {
			pickFileTreeLauncher.tryLaunch(null)
		} else {
			permissionRequestLauncher.tryLaunch(Unit)
		}
		if (!launched) {
			Toast.makeText(context ?: return, R.string.operation_not_supported, Toast.LENGTH_SHORT).show()
		}
	}
}

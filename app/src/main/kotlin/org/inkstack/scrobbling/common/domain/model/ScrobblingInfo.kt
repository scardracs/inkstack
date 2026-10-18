package org.inkstack.scrobbling.common.domain.model

import org.inkstack.list.ui.ListModelDiffCallback
import org.inkstack.list.ui.model.ListModel

data class ScrobblingInfo(
	val scrobbler: ScrobblerService,
	val mangaId: Long,
	val targetId: Long,
	val status: ScrobblingStatus?,
	val chapter: Int,
	val comment: String?,
	val rating: Float,
	val title: String,
	val coverUrl: String,
	val description: CharSequence?,
	val externalUrl: String,
) : ListModel {

	override fun areItemsTheSame(other: ListModel): Boolean {
		return other is ScrobblingInfo && other.scrobbler == scrobbler && other.mangaId == mangaId
	}

	override fun getChangePayload(previousState: ListModel): Any? = when {
		previousState !is ScrobblingInfo -> null
		previousState.status != status || previousState.rating != rating -> ListModelDiffCallback.PAYLOAD_ANYTHING_CHANGED
		else -> super.getChangePayload(previousState)
	}
}

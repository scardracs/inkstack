package org.inkstack.alternatives.ui

import org.inkstack.list.ui.model.ListModel

object SourceReplacementEmptyState : ListModel {
	override fun equals(other: Any?): Boolean = other === this

	override fun hashCode(): Int = javaClass.hashCode()

	override fun areItemsTheSame(other: ListModel): Boolean = other === this
}

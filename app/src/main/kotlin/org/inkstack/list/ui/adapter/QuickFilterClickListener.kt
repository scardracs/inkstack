package org.inkstack.list.ui.adapter

import org.inkstack.list.domain.ListFilterOption

interface QuickFilterClickListener {

	fun onFilterOptionClick(option: ListFilterOption)
}

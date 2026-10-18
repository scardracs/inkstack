package org.inkstack.list.ui.adapter

import android.view.View
import org.inkstack.list.ui.model.ListHeader

interface ListHeaderClickListener {

	fun onListHeaderClick(item: ListHeader, view: View)
}

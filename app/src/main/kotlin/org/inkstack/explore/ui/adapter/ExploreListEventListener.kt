package org.inkstack.explore.ui.adapter

import android.view.View
import org.inkstack.list.ui.adapter.ListHeaderClickListener
import org.inkstack.list.ui.adapter.ListStateHolderListener

interface ExploreListEventListener : ListStateHolderListener, View.OnClickListener, ListHeaderClickListener

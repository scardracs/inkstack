package org.inkstack.list.ui.adapter

import android.view.View
import org.inkstack.core.ui.list.OnListItemClickListener
import org.inkstack.list.ui.model.MangaListModel
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaTag

interface MangaDetailsClickListener : OnListItemClickListener<MangaListModel> {

	fun onReadClick(manga: Manga, view: View)

	fun onTagClick(manga: Manga, tag: MangaTag, view: View)
}

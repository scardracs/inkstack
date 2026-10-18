package org.inkstack.core.model.parcelable

import android.os.Parcel
import kotlinx.parcelize.Parceler
import org.inkstack.core.model.MangaSource
import org.inkstack.parsers.model.MangaSource

class MangaSourceParceler : Parceler<MangaSource> {

	override fun create(parcel: Parcel): MangaSource = MangaSource(parcel.readString())

	override fun MangaSource.write(parcel: Parcel, flags: Int) {
		parcel.writeString(name)
	}
}

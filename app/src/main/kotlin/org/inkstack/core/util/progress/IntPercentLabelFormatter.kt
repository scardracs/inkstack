package org.inkstack.core.util.progress

import android.content.Context
import com.google.android.material.slider.LabelFormatter
import org.inkstack.R

class IntPercentLabelFormatter(context: Context) : LabelFormatter {

	private val pattern = context.getString(R.string.percent_string_pattern)

	override fun getFormattedValue(value: Float) = pattern.format(value.toInt().toString())
}

package org.inkstack.core.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import org.inkstack.R

class BigButtonsAlertDialog private constructor(
	private val delegate: AlertDialog,
) : DialogInterface by delegate {

	fun show() = delegate.show()

	class Builder(context: Context) {

		private val themedContext = MaterialThemeOverlay.wrap(context, null, 0, 0)
		private val density = themedContext.resources.displayMetrics.density
		private val pad = TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			24f,
			themedContext.resources.displayMetrics,
		).toInt()

		private val root = LinearLayout(themedContext).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(pad, pad, pad, pad)
		}
		private val icon = ImageView(themedContext).apply {
			layoutParams = LinearLayout.LayoutParams((32 * density).toInt(), (32 * density).toInt())
				.apply { gravity = Gravity.CENTER_HORIZONTAL }
			val tv = TypedValue()
			themedContext.theme.resolveAttribute(android.R.attr.colorPrimary, tv, true)
			ImageViewCompat.setImageTintList(this, android.content.res.ColorStateList.valueOf(tv.data))
		}
		private val title = TextView(themedContext).apply {
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT,
			).apply {
				topMargin = (18 * density).toInt()
				bottomMargin = (18 * density).toInt()
			}
			gravity = Gravity.CENTER_HORIZONTAL
			textAlignment = TextView.TEXT_ALIGNMENT_CENTER
			setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelLarge)
			textSize = 18f
		}
		private val button1 = createButton()
		private val button2 = createButton()
		private val button3 = createButton()

		init {
			root.addView(icon)
			root.addView(title)
			root.addView(button1)
			root.addView(button2)
			root.addView(button3)
		}

		private val delegate = MaterialAlertDialogBuilder(context)
			.setView(root)

		fun setTitle(@StringRes titleResId: Int): Builder {
			title.setText(titleResId)
			return this
		}

		fun setTitle(titleText: CharSequence): Builder {
			title.text = titleText
			return this
		}

		fun setIcon(@DrawableRes iconId: Int): Builder {
			icon.setImageResource(iconId)
			return this
		}

		fun setPositiveButton(
			@StringRes textId: Int,
			listener: DialogInterface.OnClickListener,
		): Builder {
			initButton(button1, DialogInterface.BUTTON_POSITIVE, textId, listener)
			return this
		}

		fun setNegativeButton(
			@StringRes textId: Int,
			listener: DialogInterface.OnClickListener? = null,
		): Builder {
			initButton(button3, DialogInterface.BUTTON_NEGATIVE, textId, listener)
			return this
		}

		fun setNeutralButton(
			@StringRes textId: Int,
			listener: DialogInterface.OnClickListener? = null,
		): Builder {
			initButton(button2, DialogInterface.BUTTON_NEUTRAL, textId, listener)
			return this
		}

		fun create(): BigButtonsAlertDialog {
			button1.adjustCorners(isFirst = true, isLast = button2.isGone && button3.isGone)
			button2.adjustCorners(isFirst = button1.isGone, isLast = button3.isGone)
			button3.adjustCorners(isFirst = button1.isGone && button2.isGone, isLast = true)
			val dialog = delegate.create()
			root.tag = dialog
			return BigButtonsAlertDialog(dialog)
		}

		private fun createButton(): MaterialButton = MaterialButton(
			themedContext,
			null,
			com.google.android.material.R.attr.materialButtonTonalStyle,
		).apply {
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT,
			)
			minHeight = (62 * density).toInt()
			isVisible = false
		}

		private fun MaterialButton.adjustCorners(isFirst: Boolean, isLast: Boolean) {
			if (!isVisible) return
			shapeAppearanceModel = shapeAppearanceModel.toBuilder().apply {
				if (!isFirst) {
					setTopLeftCornerSize(0f)
					setTopRightCornerSize(0f)
				}
				if (!isLast) {
					setBottomLeftCornerSize(0f)
					setBottomRightCornerSize(0f)
				}
			}.build()
		}

		private fun initButton(
			button: MaterialButton,
			which: Int,
			@StringRes textId: Int,
			listener: DialogInterface.OnClickListener?,
		) {
			button.setText(textId)
			button.isVisible = true
			button.setOnClickListener {
				val dialog = root.tag as DialogInterface
				listener?.onClick(dialog, which)
				dialog.dismiss()
			}
		}
	}
}

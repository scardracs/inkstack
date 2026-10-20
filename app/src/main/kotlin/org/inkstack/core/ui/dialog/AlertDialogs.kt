package org.inkstack.core.ui.dialog

import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.annotation.UiContext
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import org.inkstack.R
import com.google.android.material.R as materialR

inline fun buildAlertDialog(
    @UiContext context: Context,
    isCentered: Boolean = false,
    block: MaterialAlertDialogBuilder.() -> Unit,
): AlertDialog = MaterialAlertDialogBuilder(
    context,
    if (isCentered) materialR.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered else 0,
).apply(block).create()

fun <B : AlertDialog.Builder> B.setCheckbox(
    @StringRes textResId: Int,
    isChecked: Boolean,
    onCheckedChangeListener: OnCheckedChangeListener
) = apply {
    val pad = context.resources.getDimensionPixelOffset(R.dimen.screen_padding)
    val checkbox = MaterialCheckBox(context).apply {
        setText(textResId)
        this.isChecked = isChecked
        setOnCheckedChangeListener(onCheckedChangeListener)
        setPadding(pad, pad / 2, pad, 0)
    }
    setView(checkbox)
}

fun <B : AlertDialog.Builder, T> B.setRecyclerViewList(
    list: List<T>,
    delegate: AdapterDelegate<List<T>>,
) = apply {
    val delegatesManager = AdapterDelegatesManager<List<T>>()
    delegatesManager.addDelegate(delegate)
    setRecyclerViewList(ListDelegationAdapter(delegatesManager).also { it.items = list })
}

fun <B : AlertDialog.Builder, T> B.setRecyclerViewList(
    list: List<T>,
    vararg delegates: AdapterDelegate<List<T>>,
) = apply {
    val delegatesManager = AdapterDelegatesManager<List<T>>()
    delegates.forEach { delegatesManager.addDelegate(it) }
    setRecyclerViewList(ListDelegationAdapter(delegatesManager).also { it.items = list })
}

fun <B : AlertDialog.Builder> B.setRecyclerViewList(adapter: RecyclerView.Adapter<*>) = apply {
    val recyclerView = RecyclerView(context)
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.updatePadding(
        top = context.resources.getDimensionPixelOffset(R.dimen.list_spacing),
    )
    recyclerView.clipToPadding = false
    recyclerView.adapter = adapter
    setView(recyclerView)
}

fun <B : AlertDialog.Builder> B.setEditText(
    inputType: Int,
    singleLine: Boolean,
): EditText {
    val editText = AppCompatEditText(context)
    editText.inputType = inputType
    if (singleLine) {
        editText.setSingleLine()
        editText.imeOptions = EditorInfo.IME_ACTION_DONE
    }
    val layout = FrameLayout(context)
    val lp = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    val horizontalMargin = context.resources.getDimensionPixelOffset(R.dimen.screen_padding)
    lp.setMargins(
        horizontalMargin,
        context.resources.getDimensionPixelOffset(R.dimen.margin_small),
        horizontalMargin,
        0,
    )
    layout.addView(editText, lp)
    setView(layout)
    return editText
}

fun <B : AlertDialog.Builder> B.setEditText(
    entries: List<CharSequence>,
    inputType: Int,
    singleLine: Boolean,
): EditText {
    if (entries.isEmpty()) {
        return setEditText(inputType, singleLine)
    }
    val pad = context.resources.getDimensionPixelOffset(R.dimen.screen_padding)
    val row = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(pad, pad / 2, pad / 2, 0)
    }
    val autoComplete = AutoCompleteTextView(context).apply {
        layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        setAdapter(ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, entries))
        this.inputType = inputType
        if (singleLine) {
            setSingleLine()
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
    }
    val dropdown = ImageButton(context).apply {
        setImageResource(R.drawable.ic_expand_more)
        contentDescription = context.getString(R.string.more)
        setBackgroundResource(android.R.color.transparent)
        setOnClickListener { autoComplete.showDropDown() }
    }
    row.addView(autoComplete)
    row.addView(dropdown)
    setView(row)
    return autoComplete
}

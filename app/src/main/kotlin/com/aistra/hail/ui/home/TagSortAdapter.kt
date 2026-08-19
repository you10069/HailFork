package com.aistra.hail.ui.home

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aistra.hail.R
import com.google.android.material.textview.MaterialTextView

class TagSortAdapter(tags: List<Pair<String, Int>>) : RecyclerView.Adapter<TagSortAdapter.ViewHolder>() {
    private val items = tags.toMutableList()

    val currentList: List<Pair<String, Int>>
        get() = items.toList()

    fun moveItem(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition <= 0 || toPosition <= 0 ||
            fromPosition !in items.indices || toPosition !in items.indices ||
            fromPosition == toPosition
        ) return false
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val density = parent.resources.displayMetrics.density
        val horizontalPadding = (20 * density).toInt()
        val verticalPadding = (16 * density).toInt()
        val drawablePadding = (16 * density).toInt()
        return ViewHolder(MaterialTextView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPaddingRelative(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            compoundDrawablePadding = drawablePadding
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
        })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.apply {
            text = items[position].first
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                if (position == 0) R.drawable.ic_outline_lock else R.drawable.ic_outline_sort,
                0,
                0,
                0
            )
            alpha = if (position == 0) 0.6f else 1f
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val textView: MaterialTextView) : RecyclerView.ViewHolder(textView)
}

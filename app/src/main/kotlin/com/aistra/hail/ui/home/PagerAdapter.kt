package com.aistra.hail.ui.home

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aistra.hail.R
import com.aistra.hail.app.AppInfo
import com.aistra.hail.app.HailData
import com.aistra.hail.utils.AppIconCache
import com.aistra.hail.utils.HPackages.myUserId
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.Job

class PagerAdapter(
    private val selectedList: List<AppInfo>,
    private val flags: MutableMap<String, Int> = mutableMapOf()
) : RecyclerView.Adapter<PagerAdapter.ViewHolder>() {
    private val items = mutableListOf<AppInfo>()
    val currentList: List<AppInfo> get() = items
    private var loadIconJob: Job? = null
    var manualSort: Boolean = false
    lateinit var onItemClickListener: OnItemClickListener
    lateinit var onItemLongClickListener: OnItemLongClickListener
    lateinit var onStartDragListener: OnStartDragListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false)
    )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = items[position]
        flags[info.packageName] = info.getFlag(selectedList)
        holder.itemView.run {
            setOnClickListener {
                if (!manualSort) onItemClickListener.onItemClick(info)
            }
            setOnLongClickListener {
                if (manualSort) {
                    onStartDragListener.onStartDrag(holder)
                    true
                } else onItemLongClickListener.onItemLongClick(info)
            }
            findViewById<ImageView>(R.id.app_icon).run {
                info.applicationInfo?.let {
                    loadIconJob = AppIconCache.loadIconBitmapAsync(
                        context,
                        it,
                        myUserId,
                        this,
                        HailData.grayscaleIcon && info.state == AppInfo.State.FROZEN
                    )
                } ?: run {
                    setImageDrawable(context.packageManager.defaultActivityIcon)
                    colorFilter = null
                }
            }
            findViewById<TextView>(R.id.app_name).run {
                text = buildString {
                    if (!HailData.grayscaleIcon && info.state == AppInfo.State.FROZEN) append("\u2744\uFE0F")
                    if (info.whitelisted) append("\uD83D\uDD12")
                    append(info.name)
                }
                isEnabled = !HailData.grayscaleIcon || info.state != AppInfo.State.FROZEN
                when {
                    info in selectedList -> setTextColor(
                        MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary)
                    )

                    info.state == AppInfo.State.NOT_FOUND -> setTextColor(
                        MaterialColors.getColor(this, androidx.appcompat.R.attr.colorError)
                    )

                    else -> setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, HailData.homeFontSize)
            }
        }
    }

    fun submitList(list: List<AppInfo>) {
        val oldItems = items.toList()
        val oldFlags = flags.toMap()
        val diff = DiffUtil.calculateDiff(HomeDiff(oldItems, list, selectedList, oldFlags))
        items.clear()
        items.addAll(list)
        diff.dispatchUpdatesTo(this)
    }

    fun moveItem(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition !in items.indices || toPosition !in items.indices) return false
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    fun onDestroy() {
        if (loadIconJob?.isActive == true) loadIconJob?.cancel()
    }

    private class HomeDiff(
        private val oldItems: List<AppInfo>,
        private val newItems: List<AppInfo>,
        private val selectedList: List<AppInfo>,
        private val oldFlags: Map<String, Int>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldItems[oldItemPosition] == newItems[newItemPosition]

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldFlags[oldItems[oldItemPosition].packageName] == newItems[newItemPosition].getFlag(selectedList)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    interface OnItemClickListener {
        fun onItemClick(info: AppInfo)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(info: AppInfo): Boolean
    }

    interface OnStartDragListener {
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
    }
}

private fun AppInfo.getFlag(selectedList: List<AppInfo>) =
    (1 shl state.ordinal) or (this in selectedList).shl(3) or whitelisted.shl(4)

private fun Boolean.shl(bitCount: Int) = if (this) 1 shl bitCount else 0

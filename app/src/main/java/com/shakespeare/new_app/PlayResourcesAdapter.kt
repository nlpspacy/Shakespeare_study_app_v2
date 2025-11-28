package com.shakespeare.new_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shakespeare.new_app.models.PlayResource

sealed class ResourceRow {
    data class Header(val title: String) : ResourceRow()
    data class Item(val resource: PlayResource) : ResourceRow()
}

class PlayResourcesAdapter(
    private val onItemClicked: (PlayResource) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ResourceRow>()

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    fun submitList(newItems: List<ResourceRow>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ResourceRow.Header -> VIEW_TYPE_HEADER
            is ResourceRow.Item -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.row_resource_header, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_ITEM -> {
                val view = inflater.inflate(R.layout.row_resource_item, parent, false)
                ItemViewHolder(view, onItemClicked)
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = items[position]) {
            is ResourceRow.Header -> (holder as HeaderViewHolder).bind(row)
            is ResourceRow.Item -> (holder as ItemViewHolder).bind(row.resource)
        }
    }

    // ------------------------
    // ViewHolder: Header
    // ------------------------
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTitle: TextView = itemView.findViewById(R.id.headerTitleText)
        fun bind(row: ResourceRow.Header) {
            headerTitle.text = row.title
        }
    }

    // ------------------------
    // ViewHolder: Item
    // ------------------------
    class ItemViewHolder(
        itemView: View,
        private val onItemClicked: (PlayResource) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val iconView: ImageView = itemView.findViewById(R.id.resourceIcon)
        private val titleText: TextView = itemView.findViewById(R.id.resourceTitleText)
        private val subtitleText: TextView = itemView.findViewById(R.id.resourceSubtitleText)

        private var currentResource: PlayResource? = null

        init {
            itemView.setOnClickListener {
                currentResource?.let(onItemClicked)
            }
        }

        fun bind(resource: PlayResource) {
            currentResource = resource

            titleText.text = resource.title

            if (resource.shortDescription.isNullOrBlank()) {
                subtitleText.visibility = View.GONE
            } else {
                subtitleText.visibility = View.VISIBLE
                subtitleText.text = resource.shortDescription
            }

            iconView.setImageResource(iconForType(resource.resourceType))
        }

        private fun iconForType(resourceType: String): Int {
            val type = resourceType.uppercase()

            return when {
                type == "YOUTUBE" -> R.drawable.ic_launcher_foreground  // Replace with a real icon later
                type == "PRINT_AMAZON" || type == "PRINT_AUSTI_WEB" -> R.drawable.ic_launcher_foreground
                else -> R.drawable.ic_launcher_foreground
            }
        }
    }
}

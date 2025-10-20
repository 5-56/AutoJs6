package org.autojs.autojs.ui.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.autojs.autojs.R

class ChatAdapter(private val items: MutableList<ChatMessage> = mutableListOf()) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_ASSISTANT = 2
    }

    data class ChatMessage(
        val isUser: Boolean,
        val text: String,
        val card: Card? = null,
    ) {
        data class Card(val title: String, val lines: List<String>, val onClickActions: List<() -> Unit> = emptyList())
    }

    override fun getItemViewType(position: Int): Int =
        if (items[position].isUser) TYPE_USER else TYPE_ASSISTANT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout = when (viewType) {
            TYPE_USER -> R.layout.message_item_user
            else -> R.layout.message_item_assistant
        }
        val view = inflater.inflate(layout, parent, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tv = holder.itemView.findViewById<TextView>(R.id.messageText)
        val item = items[position]
        tv.text = buildString {
            append(item.text)
            item.card?.let { c ->
                append("\n\n")
                append(c.title)
                append("\n")
                c.lines.take(10).forEach { append("• ").append(it).append("\n") }
            }
        }
        // Simple click fan-out for card items: trigger all actions in order (MVP)
        holder.itemView.setOnClickListener {
            item.card?.onClickActions?.forEach { runCatching { it.invoke() } }
        }
    }

    override fun getItemCount(): Int = items.size

    fun add(message: ChatMessage) {
        val index = items.size
        items.add(message)
        notifyItemInserted(index)
    }
}

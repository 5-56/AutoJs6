package com.xihe.automation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xihe.automation.data.model.ChatMessage
import com.xihe.automation.data.model.MessageType
import com.xihe.automation.databinding.ItemMessageUserBinding
import com.xihe.automation.databinding.ItemMessageAiBinding
import com.xihe.automation.databinding.ItemMessageScriptBinding
import com.xihe.automation.databinding.ItemMessageSystemBinding
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 聊天消息适配器
 */
class ChatMessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI = 2
        private const val VIEW_TYPE_SCRIPT = 3
        private const val VIEW_TYPE_SYSTEM = 4
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].type) {
            MessageType.USER -> VIEW_TYPE_USER
            MessageType.AI -> VIEW_TYPE_AI
            MessageType.SCRIPT -> VIEW_TYPE_SCRIPT
            MessageType.SYSTEM -> VIEW_TYPE_SYSTEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val binding = ItemMessageUserBinding.inflate(inflater, parent, false)
                UserMessageViewHolder(binding)
            }
            VIEW_TYPE_AI -> {
                val binding = ItemMessageAiBinding.inflate(inflater, parent, false)
                AIMessageViewHolder(binding)
            }
            VIEW_TYPE_SCRIPT -> {
                val binding = ItemMessageScriptBinding.inflate(inflater, parent, false)
                ScriptMessageViewHolder(binding)
            }
            else -> {
                val binding = ItemMessageSystemBinding.inflate(inflater, parent, false)
                SystemMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is AIMessageViewHolder -> holder.bind(message)
            is ScriptMessageViewHolder -> holder.bind(message)
            is SystemMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    // ViewHolders
    inner class UserMessageViewHolder(private val binding: ItemMessageUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.textMessage.text = message.content
            binding.textTime.text = dateFormat.format(message.timestamp)
        }
    }

    inner class AIMessageViewHolder(private val binding: ItemMessageAiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.textMessage.text = message.content
            binding.textTime.text = dateFormat.format(message.timestamp)
        }
    }

    inner class ScriptMessageViewHolder(private val binding: ItemMessageScriptBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.textMessage.text = message.content
            binding.textTime.text = dateFormat.format(message.timestamp)
            
            message.scriptContent?.let { script ->
                binding.textScript.text = script
            }
        }
    }

    inner class SystemMessageViewHolder(private val binding: ItemMessageSystemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.textMessage.text = message.content
            binding.textTime.text = dateFormat.format(message.timestamp)
        }
    }
}

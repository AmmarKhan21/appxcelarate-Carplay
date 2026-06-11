package com.car.play.android.app.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.car.play.android.app.databinding.ItemChatMessageBinding

data class ChatMessage(
    val message: String,
    val isUser: Boolean
)

class ChatAdapter(
    private val messages: MutableList<ChatMessage> = mutableListOf()
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemChatMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]
        holder.binding.apply {
            if (msg.isUser) {
                llUser.visibility = View.VISIBLE
                llBot.visibility = View.GONE
                tvUserMessage.text = msg.message
            } else {
                llUser.visibility = View.GONE
                llBot.visibility = View.VISIBLE
                tvBotMessage.text = msg.message
            }
        }
    }

    override fun getItemCount() = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun getMessages() = messages.toList()
}

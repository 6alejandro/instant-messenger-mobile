package com.tegas.instant_messenger_mobile.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tegas.instant_messenger_mobile.R
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatsItem
import com.tegas.instant_messenger_mobile.databinding.ItemContactBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MainAdapter(
    private val originalChatList: MutableList<ChatsItem> = mutableListOf(),
    private val listener: (ChatsItem) -> Unit
) :
    RecyclerView.Adapter<MainAdapter.ChatViewHolder>() {

    private var chatList: List<ChatsItem> = listOf()

    fun setData(chatList: List<ChatsItem?>) {
        this.chatList = chatList.filterNotNull()
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(private val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatsItem) {

            binding.tvName.text = chat.name
            binding.tvMessage.text = chat.lastMessage
            binding.tvTime.text = formatDateTime(chat.lastMessageTime)

            Glide.with(itemView)
                .load(R.drawable.daniela_villarreal)
                .into(binding.ivPhoto)
        }

        private fun formatDateTime(timestamp: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            return outputFormat.format(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            ItemContactBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = originalChatList[position]
        holder.bind(chat)
    }

    override fun getItemCount(): Int {
        return originalChatList.size
    }


}
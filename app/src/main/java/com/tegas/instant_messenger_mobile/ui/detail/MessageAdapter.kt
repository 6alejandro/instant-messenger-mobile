package com.tegas.instant_messenger_mobile.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tegas.instant_messenger_mobile.data.retrofit.response.MessagesItem
import com.tegas.instant_messenger_mobile.databinding.ItemChatsBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(
    private val nim: String,
    private val data: MutableList<MessagesItem> = mutableListOf(),
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    fun setData(data: MutableList<MessagesItem>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    fun addMessage(message: MessagesItem) {
        data.add(message)
        notifyItemInserted(data.size - 1)
    }

    inner class MessageViewHolder(private val binding: ItemChatsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessagesItem) {
            if (item.senderId != nim) {
                binding.layoutSent.itemSents.visibility = View.GONE
                binding.layoutReceived.chatReceived.text = item.content
                binding.layoutReceived.tvTime.text = formatDateTime(item.sentAt)

                if (item.attachments != null && item.attachments != "null") {
                    binding.layoutReceived.ivAttachment.visibility = View.VISIBLE
                    Glide
                        .with(binding.layoutReceived.ivAttachment.context)
                        .load(item.attachments)
                        .into(binding.layoutReceived.ivAttachment)
                } else {
                    binding.layoutReceived.ivAttachment.visibility = View.GONE
                }

            } else {
                binding.layoutReceived.itemReceived.visibility = View.GONE
                binding.layoutSent.chatSent.text = item.content
                binding.layoutSent.tvTime.text = formatDateTime(item.sentAt)

                if (item.attachments != null && item.attachments != "null") {
                    binding.layoutSent.ivAttachment.visibility = View.VISIBLE
                    Glide
                        .with(binding.layoutSent.ivAttachment.context)
                        .load(item.attachments)
                        .into(binding.layoutSent.ivAttachment)
                } else {
                    binding.layoutSent.ivAttachment.visibility = View.GONE
                }
            }
        }

        private fun formatDateTime(timestamp: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            return outputFormat.format(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            (ItemChatsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }
}
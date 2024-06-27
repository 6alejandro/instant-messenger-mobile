package com.tegas.instant_messenger_mobile.ui.detail

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils.formatDateTime
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.tegas.instant_messenger_mobile.R
import com.tegas.instant_messenger_mobile.data.retrofit.response.MessagesItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.ParticipantDataItem
import com.tegas.instant_messenger_mobile.databinding.ItemChatsBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(
    private val viewModel: DetailViewModel,
    private val nim: String,
    private val data: MutableList<MessagesItem> = mutableListOf(),
//    private val chatType: String
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

                if (item.attachments != "")
                {
                  binding.layoutReceived.tvAttachments.visibility = View.VISIBLE
                  binding.layoutReceived.tvAttachments.text = item.attachments.toString()

                    binding.layoutReceived.tvAttachments.setOnClickListener {
                        val url = "http://192.168.137.1:5000/download?path=" + item.attachments.toString()
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        binding.root.context.startActivity(intent)
                    }
                }
                binding.layoutSent.itemSents.visibility = View.GONE
                binding.layoutReceived.chatReceived.text = item.content
                binding.layoutReceived.tvTime.text = formatDateTime(item.sentAt)
            } else {

                if (item.attachments != "")
                {
                    binding.layoutSent.tvAttachments.visibility = View.VISIBLE
                    binding.layoutSent.tvAttachments.text = item.attachments.toString()
                    binding.layoutSent.tvAttachments.setOnClickListener {
                        val url = "http://192.168.137.1:5000/" + item.attachments.toString()
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        binding.root.context.startActivity(intent)
                    }
                }

                binding.layoutReceived.itemReceived.visibility = View.GONE
                binding.layoutSent.chatSent.text = item.content
                binding.layoutSent.tvTime.text = formatDateTime(item.sentAt)
                binding.layoutSent.messageState.setImageResource(R.drawable.single_check)

                binding.layoutSent.tvName.visibility = View.GONE
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
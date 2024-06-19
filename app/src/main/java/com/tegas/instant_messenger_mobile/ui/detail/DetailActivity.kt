package com.tegas.instant_messenger_mobile.ui.detail

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tegas.instant_messenger_mobile.R
import com.tegas.instant_messenger_mobile.data.Result
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatsItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.MessagesItem
import com.tegas.instant_messenger_mobile.databinding.ActivityDetailSecondBinding
import com.tegas.instant_messenger_mobile.ui.ViewModelFactory
import com.tegas.instant_messenger_mobile.ui.login.LoginActivity
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DetailActivity : AppCompatActivity() {

    private lateinit var webSocketClient: WebSocketClient

    private lateinit var binding: ActivityDetailSecondBinding
    private val viewModel by viewModels<DetailViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var chat: MessagesItem

    //    private var chatId: String? = null
    private lateinit var adapter: MessageAdapter
//    private val nim = "21106050048"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailSecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item = intent.getParcelableExtra<ChatsItem>("item")
        val chatId = item?.chatId
        Log.d("CHAT ID", "CHAT ID: $chatId")
        binding.tvName.text = item?.name
        Glide
            .with(this)
            .load(
                R.drawable
                    .daniela_villarreal
            )
            .into(binding.ivImage)

        viewModel.getChatDetails(chatId!!)
        fetchData()
        getSession(chatId)
        setupSend()

        binding.backButton.setOnClickListener {
//            intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
            onBackPressed()
        }
        binding.ivPhone.setOnClickListener {
            viewModel.saveChat(item)
        }

        viewModel.resultFav.observe(this) {
            binding.ivPhone.changeIconColor(R.color.darker_grey)
        }

        viewModel.resultDelFav.observe(this) {
            binding.ivPhone.changeIconColor(R.color.blue)
        }

        viewModel.findFavorite(item?.chatId ?: "") {
            binding.ivImage.changeIconColor(R.color.darker_grey)
        }
        val uri = URI("ws://192.168.137.1:8181/ws") // Replace with your server IP or hostname
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("WebSocket", "Connected to server")
                // Send user ID after connection is open
                val chatID = "{\"id\": \"$chatId\"}" // Replace with appropriate user ID
                send(chatID)
            }

            override fun onMessage(message: String?) {
                Log.d("WebSocket", "Received: $message")
                val jSOnString = """$message"""
                val gson = Gson()
                val messageData: MessagesItem = gson.fromJson(jSOnString, MessagesItem::class.java)
                adapter.addMessage(messageData)

                Log.d("ADDMESSAGEEEEEEEEEEE", "MESSAGE REFRESHED BY WEBSOCKET")
                // Handle incoming messages here
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d("WebSocket", "Connection closed")
            }

            override fun onError(ex: Exception?) {
                Log.e("WebSocket", "Error: ${ex?.message}")
            }
        }

        webSocketClient.connect()
    }

    private fun getSession(chatId: String) {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            binding.rvChat.layoutManager = LinearLayoutManager(this).apply {
                stackFromEnd = true
            }
            binding.rvChat.setHasFixedSize(true)
            adapter = MessageAdapter(user.nim)
            binding.rvChat.adapter = adapter


            binding.iconSend.setOnClickListener {
                val senderId = user.nim
                val content = binding.editText.text.toString()

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.editText.windowToken, 0)

                Log.d("CONTENT", "CONTENT: $content")

                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                val currentTimeString = dateFormat.format(Date())

                val jSon = JsonObject().apply {
                    addProperty("chatId", chatId)
                    addProperty("senderId", senderId)
                    addProperty("content", content)
                    addProperty("sentAt", currentTimeString)
                    addProperty("attachment", "null")
                }
                viewModel.sendMessage(jSon)

            }

        }
    }

    private fun setupSend() {
        viewModel.sendMessage.observe(this) {
            when (it) {
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, it.error, Toast.LENGTH_SHORT).show()
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.editText.text?.clear()
                    Toast.makeText(
                        this,
                        "$it.data.messages, status: ${it.data.status}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun fetchData() {
        viewModel.detailViewModel.observe(this) {
            when (it) {
                is Result.Error -> {
                    Log.d("Result", "Error")
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error: ${it.error}", Toast.LENGTH_SHORT).show()
                    Log.d("ERROR ACTIVITY", "Error: ${it.error}")
                }

                is Result.Success -> {
                    Log.d("Result", "Success")
                    binding.progressBar.visibility = View.GONE
                    adapter.setData(it.data as MutableList<MessagesItem>)
                }

                else -> {
                    Log.d("Result", "Loading")
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }


    }
}


fun ImageView.changeIconColor(@ColorRes color: Int) {
    imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this.context, color))
}
package com.tegas.instant_messenger_mobile.ui.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.tegas.instant_messenger_mobile.data.ChatRepository
import com.tegas.instant_messenger_mobile.data.Result
import com.tegas.instant_messenger_mobile.data.UserModel
import com.tegas.instant_messenger_mobile.data.local.DbModule
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatsItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.MessagesItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.SendResponse
import kotlinx.coroutines.launch

class DetailViewModel(private val repository: ChatRepository, private val db: DbModule) :
    ViewModel() {

    private val _detailViewModel = MediatorLiveData<Result<List<MessagesItem>>>()
    val detailViewModel: LiveData<Result<List<MessagesItem>>> = _detailViewModel

    private val _sendMessage = MediatorLiveData<Result<SendResponse>>()
    val sendMessage: LiveData<Result<SendResponse>> = _sendMessage

    private var isSaved = false

    val resultFav = MutableLiveData<Boolean>()
    val resultDelFav = MutableLiveData<Boolean>()


    fun getChatDetails(chatId: String) {
        Log.d("DETAIL VIEW MODEL", "DetailViewModel Chat ID: $chatId")
        val liveData = repository.getChatDetails(chatId)
        _detailViewModel.addSource(liveData) { result ->
            _detailViewModel.value = result
        }
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun sendMessage(message: JsonObject) {
        val liveData = repository.sendMessage(message)
        _sendMessage.addSource(liveData) { result ->
            _sendMessage.value = result
        }
    }

    fun saveChat(chat: ChatsItem?) {
        viewModelScope.launch {
            chat?.let {
                if (isSaved) {
                    db.chatDao.delete(chat)
                    resultDelFav.value = true
                } else {
                    db.chatDao.insert((chat))
                    resultFav.value = true
                }
            }
            isSaved = !isSaved
        }
    }

    fun findFavorite(id: String, listenFavorite: () -> Unit) {
        viewModelScope.launch {
            val chat = db.chatDao.findById(id)
            if (chat != null) {
                listenFavorite()
                isSaved = true
            }
        }
    }

    class Factory(private val repository: ChatRepository, private val db: DbModule) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetailViewModel(repository, db) as T
    }
}
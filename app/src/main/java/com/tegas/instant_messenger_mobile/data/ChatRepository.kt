package com.tegas.instant_messenger_mobile.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.gson.JsonObject
import com.tegas.instant_messenger_mobile.data.pref.UserPreference
import com.tegas.instant_messenger_mobile.data.retrofit.ApiService
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatsItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.LoginResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.MessagesItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.SendResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    fun getChatList(nim: String): LiveData<Result<List<ChatsItem>>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.getChatList(nim)
                val chats = response.chats
                emit(Result.Success(chats))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun getChatDetails(chatId: String): LiveData<Result<List<MessagesItem>>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                Log.d("REPOSITORY", "REPOSITORY CHAT ID: $chatId")
                val response = apiService.getChatDetails(chatId)
                val messages = response.messages
                emit(Result.Success(messages))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun login(
        nim: String,
        password: String
    ): LiveData<Result<LoginResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.login(nim, password)
                val nim = response.data?.nim
                val name = response.data?.name
                Log.d("LOGIN SUCCESS", "Name: $name, NIM: $nim")
                emit(Result.Success(response))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun logins(auth: JsonObject): LiveData<Result<LoginResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.logins(auth)
                val data = response.data
                val error = response.error
                val name = response.data?.name
                val nim = response.data?.nim
                saveSession(UserModel(name!!, nim!!))
                emit(Result.Success(response))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun sendMessage(message: JsonObject): LiveData<Result<SendResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.sendMessage(message)
                Log.d("Success", response.messages.toString())
                emit(Result.Success(response))
            }catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    private suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    companion object {
        @Volatile
        private var instance: ChatRepository? = null
        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): ChatRepository =
            instance ?: synchronized(this) {
                instance ?: ChatRepository(apiService, userPreference)
            }.also { instance = it }
    }
}
package com.tegas.instant_messenger_mobile.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.gson.JsonObject
import com.tegas.instant_messenger_mobile.data.pref.UserPreference
import com.tegas.instant_messenger_mobile.data.retrofit.ApiService
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatDetailResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatsItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.DownloadResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.LoginResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.MessagesItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.ParticipantDataItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.SendResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

    fun getChatDetails(chatId: String, userId: String): LiveData<Result<ChatDetailResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                Log.d("REPOSITORY", "REPOSITORY CHAT ID: $chatId")
                val response = apiService.getChatDetails(chatId, userId)
                val item = response
                emit(Result.Success(item))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun getParticipant(chatId: String): LiveData<Result<List<ParticipantDataItem>>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                Log.d("REPOSITORY PARticipant", "REPOSITORY CHAT ID: $chatId")
                val response = apiService.getParticipants(chatId)
                val participants = response.participantData
                emit(Result.Success(participants))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun downloadFile(path: String): LiveData<Result<DownloadResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.downloadFile(path)
                val message = response.message
                Log.d("DOWNLOAD MESSAGE", message)
                emit(Result.Success(response))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
                Log.d("DOWNLOAD ERROR MESSAGE", e.message.toString())
            }
        }


    private fun writeFile(data: ByteArray, file: File) {
        try {
            val outputStream = FileOutputStream(file)
            outputStream.write(data)
            outputStream.close()
        } catch (e: IOException) {
            Log.e("DOWNLOAD", "Error writing file: $e")
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

    fun sendMessage(
        chatId: String,
        senderId: String,
        content: String,
        sentAt: String,
        attachments: MultipartBody.Part?
    ): LiveData<Result<SendResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                Log.d("SELECTED FILE IN REPOSITORY", attachments.toString())
                Log.d("TIME IN REPOSITORY TRY", sentAt)
                val data = mapOf(
                    "chatId" to RequestBody.create(MultipartBody.FORM, chatId),
                    "senderId" to RequestBody.create(MultipartBody.FORM, senderId),
                    "content" to RequestBody.create(MultipartBody.FORM, content),
                    "sentAt" to RequestBody.create(MultipartBody.FORM, sentAt),
                )
                val response =
                    if (attachments != null) {
                        apiService.sendMessage(data, attachments)
                    } else {
                        apiService.sendMessage(data)
                    }
                Log.d("Success", response.messages.toString())
                emit(Result.Success(response))
            } catch (e: Exception) {
                Log.d("SELECTED FILE IN REPOSITORY", attachments.toString())
                Log.d("TIME IN REPOSITORY CATCH", sentAt)
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
package com.tegas.instant_messenger_mobile.data.retrofit

import com.google.gson.JsonObject
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatDetailResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatListResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.LoginResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.ParticipantResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.SendResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("chatList")
    suspend fun getChatList(
        @Query("nim") nim: String
    ): ChatListResponse

    @GET("chatDetail")
    fun getChatDetail(
        @Query("chatId") chatId: String
    ): Call<ChatDetailResponse>

    @GET("chatDetail")
    suspend fun getChatDetails(
        @Query("chatId") chatId: String
    ): ChatDetailResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @POST("login")
    suspend fun logins(
        @Body auth: JsonObject
    ): LoginResponse

    @POST("messages")
    suspend fun sendMessage(
        @Body message: JsonObject
    ): SendResponse

    @GET("participant")
    suspend fun getParticipants(
        @Query("chatId") chatId: String
    ): ParticipantResponse
}
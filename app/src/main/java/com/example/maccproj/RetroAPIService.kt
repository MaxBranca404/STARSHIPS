package com.example.maccproj

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL =
    "https://maxbranca.eu.pythonanywhere.com"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

object RetroAPI{
    val retrofitService : RetroAPIService by lazy {
        retrofit.create(RetroAPIService::class.java)
    }
}

interface RetroAPIService {

    @POST("/add_user")
    suspend fun addUser(@Body data: JsonObject): JsonObject

    @PUT("/update_score")
    suspend fun updateScore(@Body data: JsonObject): JsonObject

    @GET("/get_score")
    suspend fun getScore(@Query("username") username: String): JsonObject

    @GET("/get_all_score")
    suspend fun getAllScore(): List<JsonObject>
}
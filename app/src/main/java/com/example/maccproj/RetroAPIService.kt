package com.example.maccproj

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL =
    "https://mikitronti.eu.pythonanywhere.com"

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

    @POST("/insert_score")
    suspend fun insertScore(@Body data: JsonObject): JsonObject

    @POST("/insert_user")
    suspend fun insertUser(@Body data: JsonObject): JsonObject

    @POST("/update_max_score")
    suspend fun updateMaxScore(@Body data: JsonObject): JsonObject

    @GET("/top_scores")
    suspend fun getTopScores(): List<JsonObject>

    @GET("/get_maxscore")
    suspend fun getMaxScore(@Query("username") username: String): JsonObject

    @GET("/get_userid")
    suspend fun getUserId(@Query("username") username: String): JsonObject

    @DELETE("/delete_user/{userid}")
    suspend fun deleteUser(@Path("userid") userid: String): JsonObject

    @DELETE("/delete_score/{userid}")
    suspend fun deleteScore(@Path("userid") userid: String): JsonObject
}
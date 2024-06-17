package com.example.maccproj

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

sealed interface RetroState {
    data class Success(val data: JsonObject) : RetroState
    object Error : RetroState
    object Loading : RetroState
}

class RetroViewModel : ViewModel() {
    //stato scrittura id
    var retroAddState: RetroState by mutableStateOf(RetroState.Loading)
        public set

    //stato aggiornamento score
    var retroUpdState: RetroState by mutableStateOf(RetroState.Loading)
        public set

    //stato lettura singolo score
    var retroScoreState: RetroState by mutableStateOf(RetroState.Loading)
        public set

    //stato lettura all score
    var retroAllScoreState = listOf<JsonObject>()
        public set

    // Check user registration status
    var isRegistered: RetroState by mutableStateOf(RetroState.Loading)
        public set


    fun addUser(data :JsonObject) {
        viewModelScope.launch {
            retroAddState = try {
                val jsonResult = RetroAPI.retrofitService.addUser(data)
                RetroState.Success(jsonResult)
            }catch (e: IOException){
                Log.println(Log.INFO,"ERR",e.message.toString())
                RetroState.Error
            }

        }
    }


    fun updateScore(data :JsonObject) {
        viewModelScope.launch {
            retroUpdState = try {
                val jsonResult = RetroAPI.retrofitService.updateScore(data)
                RetroState.Success(jsonResult)
            }catch (e: IOException){
                Log.println(Log.INFO,"ERR",e.message.toString())
                RetroState.Error
            }

        }
    }

    fun getScore(username :String) {
        viewModelScope.launch {
            retroScoreState = try {
                val jsonResult = RetroAPI.retrofitService.getScore(username)
                RetroState.Success(jsonResult)
            }catch (e: IOException){
                Log.println(Log.INFO,"ERR",e.message.toString())
                RetroState.Error
            }

        }
    }

    fun getAllScore() {
        viewModelScope.launch {
            retroAllScoreState = try {
                RetroAPI.retrofitService.getAllScore()
            }catch (e: IOException){
                Log.println(Log.INFO,"ERR",e.message.toString())
                listOf<JsonObject>()
            }

        }
    }

    fun checkUser(username: String) {
        viewModelScope.launch {
            try {
                val response = RetroAPI.retrofitService.checkUser(username)

                if (response.isSuccessful) {
                    val jsonResult = response.body()

                    if (jsonResult != null) {
                        isRegistered = RetroState.Success(jsonResult)
                    } else {
                        Log.e("RetroViewModel", "Response body is null")
                        isRegistered = RetroState.Error // Handle null response body
                    }
                } else {
                    Log.e("RetroViewModel", "Unsuccessful response: ${response.code()}")
                    isRegistered = RetroState.Error // Handle unsuccessful response
                }
            } catch (e: Exception) {
                Log.e("RetroViewModel", "Exception: ${e.message}")
                isRegistered = RetroState.Error // Handle exception
            }
        }
    }
}

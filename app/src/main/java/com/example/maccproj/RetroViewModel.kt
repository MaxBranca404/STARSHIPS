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

sealed interface RetroUiState {
    data class Success(val data: JsonObject) : RetroUiState
    object Error : RetroUiState
    object Loading : RetroUiState
}

class RetroViewModel : ViewModel() {

    var retroUiState: RetroUiState by mutableStateOf(RetroUiState.Loading)
        private set

    var retroGridState: RetroUiState by mutableStateOf(RetroUiState.Loading)
        private set

    init {
        //getTopScores()
    }

    fun insertUser(data :JsonObject) {
        viewModelScope.launch {
            retroUiState = try {
                val jsonResult = RetroAPI.retrofitService.insertUser(data)
                RetroUiState.Success(jsonResult)
            }catch (e: IOException){
                Log.println(Log.INFO,"ERR",e.message.toString())
                RetroUiState.Error
            }

        }
    }

    fun getUserId(username :String) {
        viewModelScope.launch {
            retroUiState = try {
                val jsonResult = RetroAPI.retrofitService.getUserId(username)
                RetroUiState.Success(jsonResult)
            }catch (e: IOException){

                Log.println(Log.INFO,"ERR",e.message.toString())
                RetroUiState.Error
            }

        }
    }

    fun insertUserMaxScore(data :JsonObject) {
        viewModelScope.launch {
            retroUiState = try {
                val jsonResult = RetroAPI.retrofitService.insertScore(data)
                RetroUiState.Success(jsonResult)
            }catch (e: IOException){
                Log.println(Log.INFO,"ERR",e.message.toString())
                RetroUiState.Error
            }

        }
    }

    fun getUserMaxScore(username :String) {
        viewModelScope.launch {
            retroUiState = try {
                val jsonResult = RetroAPI.retrofitService.getMaxScore(username)
                RetroUiState.Success(jsonResult)
            }catch (e: IOException){

                Log.println(Log.INFO,"ERR",e.message.toString())
                RetroUiState.Error
            }

        }
    }

    fun resetUiState() {
        retroUiState = RetroUiState.Loading
    }

    /*
    fun getTopScores() {
        viewModelScope.launch {
            retroUiState = try {
                val jsonResult = RetroAPI.retrofitService.getTopScores()
                RetroUiState.Success(jsonResult)
            }catch (e: IOException){

                Log.println(Log.INFO,"ERR",e.message.toString())
                RetroUiState.Error
            }

        }
    }

    fun fetchTopScores() {
        retroUiState = RetroUiState.Loading
        RetroAPI.retrofitService.getTopScores().enqueue(object : Callback<List<JsonObject>> {
            override fun onResponse(
                call: Call<List<JsonObject>>,
                response: Response<List<JsonObject>>
            ) {
                if (response.isSuccessful) {
                    val jsonArray = JsonArray().apply {
                        response.body()?.forEach { add(it) }
                    }
                    val result = JsonObject().apply {
                        add("topScores", jsonArray)
                    }
                    retroUiState = RetroUiState.Success(result)
                } else {
                    retroUiState = RetroUiState.Error
                }
            }

            override fun onFailure(call: Call<List<JsonObject>>, t: Throwable) {
                Log.println(Log.INFO, "ERR", t.message.toString())
                retroUiState = RetroUiState.Error
            }
        })
    }*/

    // Add other methods as needed
}

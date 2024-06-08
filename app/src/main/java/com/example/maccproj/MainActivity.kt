package com.example.maccproj

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.JsonReader
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.maccproj.ui.theme.MACCProjTheme
import com.google.gson.JsonObject
import io.github.sceneview.ar.ARScene
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import androidx.compose.runtime.LaunchedEffect as LaunchedEffect

class MainActivity : ComponentActivity() {

    lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window,false)
        requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE

        mediaPlayer = MediaPlayer.create(this, R.raw.soundtrack) //imposta audio del menu
        mediaPlayer.isLooping = true // Riproduzione in loop
        mediaPlayer.start()


        /*// Example usage to remove the user ID
        lifecycleScope.launch {
            UserPreferences.removeUserId(this@MainActivity)
        }*/

        // Check if userId is already saved
        lifecycleScope.launch {
            val userIdFlow = UserPreferences.getUserId(applicationContext)
            userIdFlow.collect { userId ->
                if (userId.isNullOrEmpty()) {
                    // If no userId, show the registration screen
                    setContent {
                        UserRegistrationScreen { username ->
                            registerUser(username)
                        }
                    }
                } else {
                    // If userId exists, proceed to the main content
                    // Replace this with your main screen composable
                    setContent {

                        MACCProjTheme {
                            val navController = rememberNavController()
                            val buttonMediaPlayer = MediaPlayer.create(LocalContext.current,R.raw.buttoncut) //imposta audio click tasti menu

                            NavHost(navController, startDestination = "menu") {
                                composable("menu") { MenuScreen(userId, navController, buttonMediaPlayer) }
                                composable("highscore") { HighscoreScreen("Max", navController, buttonMediaPlayer) }
                                composable("arscreen") { ARScreen(navController, buttonMediaPlayer) }
                                /*composable("menu") { MenuScreen(navController, buttonMediaPlayer) }
                                composable("highscore") { HighscoreScreen(navController, buttonMediaPlayer) }*/
                            }
                        }
                    }
                }
            }
        }


    }
    override fun onPause() {
        super.onPause()
        mediaPlayer.pause() // Mette in pausa la soundtrack quando l'app è in pausa
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer.start() // Riprende la soundtrack quando l'app riprende
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    private fun registerUser(username: String) {
        // Prepare the new user data
        val newUser = JsonObject().apply {
            addProperty("username", username)
        }

        /*// Call the API method to insert the new user
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetroAPI.retrofitService.insertUser(newUser)
                // Handle the response here
                println("User added successfully: $response")
            } catch (e: Exception) {
                // Handle the error here
                e.printStackTrace()
            }
        }

        recreate()*/

        lifecycleScope.launch {
            UserPreferences.saveUserId(applicationContext, username)
            // Now that the userId is saved, restart the activity to show the main content
            recreate()
        }
    }
}

/*
fun getUserId(username: String, onResult: (String?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetroAPI.retrofitService.getUserId(username)
            val userId = response.get("userid").asString
            onResult(userId)
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(null)
        }
    }
}*/


@Composable
fun UserRegistrationScreen(onEnterClick: (String) -> Unit) {
    var username by remember { mutableStateOf("") }

    Box(modifier = Modifier
        .background(Color(45, 67, 208))
        .paint(
            painter = painterResource(id = R.drawable.menuback),
            contentScale = ContentScale.FillBounds

        )
        .fillMaxSize(),
        contentAlignment = Alignment.Center){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AlertDialog(
                onDismissRequest = {  },
                title = { Text(text = "Create Account", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    ElevatedButton(onClick = { onEnterClick(username) }) {
                        Text("Enter")
                    }
                },
                dismissButton = {

                }
            )

        }
    }
}



@Composable
fun MenuScreen(userId: String, navController: NavController, buttonMediaPlayer: MediaPlayer){

    Box(modifier = Modifier
        .background(Color(45, 67, 208))
        .paint(
            painter = painterResource(id = R.drawable.menuback),
            contentScale = ContentScale.FillBounds

        )
        .fillMaxSize(),
        contentAlignment = Alignment.Center){

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            //Image(painter = painterResource(id = R.drawable.logo), contentDescription = null,
            //    modifier = Modifier.scale(1f)) //aggiungi un logo al centro del menu

            Spacer(modifier = Modifier.height(180.dp))

            Row(verticalAlignment = Alignment.CenterVertically){
                ElevatedButton(
                    onClick = {
                        navController.navigate("arscreen")
                        buttonMediaPlayer.start()
                    },
                    modifier = Modifier.width(150.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(31, 111, 139, 255)),
                    border = BorderStroke(2.dp, Color(22, 89, 112, 120)),
                ) {
                    Text("START",fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(30.dp))

                ElevatedButton(onClick = {
                    navController.navigate("highscore")
                    buttonMediaPlayer.start()
                },
                    modifier = Modifier.width(150.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(31, 111, 139, 255)),
                    border = BorderStroke(2.dp, Color(22, 89, 112, 120))
                ) {
                    Text("HIGHSCORE",fontWeight = FontWeight.Bold)
                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("User logged in with userid: ${userId}",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

        }
    }

}


@Composable
fun HighscoreScreen(name: String, navController: NavController, buttonMediaPlayer: MediaPlayer, modifier: Modifier = Modifier) {
    val mContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var helloWorldText by remember { mutableStateOf("Loading...") }



    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response = RetroAPI.retrofitService.helloWorld()
                helloWorldText = response.toString()
            } catch (e: Exception) {
                helloWorldText = "Error: ${e.message}"
            }
        }
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ){
        Column {
            Text(
                text = "Highscore of $name!",
                modifier = modifier
            )
            Text(text = helloWorldText)
        }
    }
}



@Composable
fun ARScreen(navController: NavController, buttonMediaPlayer: MediaPlayer) {

    var playerscore by remember { mutableStateOf(0) }

    var timeLeft by remember { mutableStateOf(20000L) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }

    isTimerRunning = true

    fun onCountdownEnd() {
        showPopup = true
    }

    // Countdown timer logic
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            object : CountDownTimer(timeLeft, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeft = millisUntilFinished
                }

                override fun onFinish() {
                    isTimerRunning = false
                    // Perform the action when the countdown ends
                    onCountdownEnd()
                }
            }.start()
        }
    }



    val mContext = LocalContext.current
    val laserMediaPlayer = MediaPlayer.create(mContext, R.raw.laser)
    //val gameMediaPlayer = MediaPlayer.create(mContext, R.raw.soundtrack)

    ARScene(

    )

    // Box for foreground (spaceship's cockpit)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                // Replace with your image id
                painterResource(id = R.drawable.foreground2),
                contentScale = ContentScale.FillBounds
            )

    )

    // Box for Fire! button
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        ElevatedButton(
            onClick = {
                if (laserMediaPlayer.isPlaying) {
                    laserMediaPlayer.stop()
                    laserMediaPlayer.prepare()
                }
                laserMediaPlayer.start()
                playerscore += 1
            },
            modifier = Modifier
                .padding(0.dp, 0.dp, 10.dp, 120.dp)
                .size(120.dp), // Makes the button circular by setting equal width and height
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = BorderStroke(5.dp, Color(209, 72, 61, 120)),
            shape = CircleShape // Applies a circular shape to the button
        ) {
            Text("FIRE!",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold)
        }
    }

    // Box for countdown
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
        if (isTimerRunning) {
            if(timeLeft/1000 < 10){
                Text(
                    text = "0${timeLeft / 1000}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(30.dp, 0.dp, 0.dp, 30.dp)
                        .rotate(25f)
                )
            }else{
                Text(
                    text = "${timeLeft / 1000}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(30.dp, 0.dp, 0.dp, 30.dp)
                        .rotate(25f)
                )
            }

        }
    }

    // Box for player score
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        if(playerscore < 10){
            Text(
                text = "0${playerscore}",
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(0.dp, 0.dp, 30.dp, 30.dp)
                    .rotate(-25f)
            )
        }else{
            Text(
                text = "${playerscore}",
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(0.dp, 0.dp, 30.dp, 30.dp)
                    .rotate(-25f)
            )
        }
    }

    // Open the popup menu when the countdown ends
    if (showPopup) {
        AlertDialog(
            onDismissRequest = { showPopup = false },
            title = { Text(text = "Game Over", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
            text = {
                Column {
                    Text("Player's Points: ${playerscore}", fontSize = 20.sp) // Replace with actual points
                }
            },
            confirmButton = {
                ElevatedButton(onClick = {
                        showPopup = false
                        navController.navigate("arscreen") { // Replace "current_screen" with your current screen route
                            popUpTo("arscreen") { inclusive = true }
                        }
                    }
                ) {
                    Text("Restart Game")
                }
            },
            dismissButton = {
                ElevatedButton(onClick = {
                        showPopup = false
                        navController.navigate("menu") // Replace "menu" with your menu screen route
                    }
                ) {
                    Text("Go to Menu")
                }
            }
        )
    }

}





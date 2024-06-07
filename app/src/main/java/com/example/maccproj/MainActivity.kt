package com.example.maccproj

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.maccproj.ui.theme.MACCProjTheme
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import io.github.sceneview.animation.Transition.animateRotation
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Rotation
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNode
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView


class MainActivity : ComponentActivity() {

    lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window,false)
        requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE

        mediaPlayer = MediaPlayer.create(this, R.raw.soundtrack) //imposta audio del menu
        mediaPlayer.isLooping = true // Riproduzione in loop
        mediaPlayer.start()

        setContent {

            MACCProjTheme {
                val navController = rememberNavController()
                val buttonMediaPlayer = MediaPlayer.create(LocalContext.current,R.raw.buttoncut) //imposta audio click tasti menu

                NavHost(navController, startDestination = "menu") {
                    composable("menu") { MenuScreen(navController, buttonMediaPlayer) }
                    composable("highscore") { HighscoreScreen("Max", navController, buttonMediaPlayer) }
                    composable("arscreen") { ARScreen(navController, buttonMediaPlayer) }
                    /*composable("menu") { MenuScreen(navController, buttonMediaPlayer) }
                    composable("highscore") { HighscoreScreen(navController, buttonMediaPlayer) }*/
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

}


@Composable
fun MenuScreen(navController: NavController, buttonMediaPlayer: MediaPlayer){

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
                ElevatedButton(onClick = {
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


        }
    }

}


@Composable
fun HighscoreScreen(name: String, navController: NavController, buttonMediaPlayer: MediaPlayer, modifier: Modifier = Modifier) {
    val mContext = LocalContext.current
    val laserMediaPlayer = MediaPlayer.create(mContext, R.raw.laser)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ){
        Text(
            text = "Highscore of $name!",
            modifier = modifier
        )
        ElevatedButton(onClick = {
            laserMediaPlayer.start()
        }) {
            Text("FIRE!",fontWeight = FontWeight.Bold)
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
                        .padding(30.dp,0.dp,0.dp,30.dp)
                        .rotate(25f)
                )
            }else{
                Text(
                    text = "${timeLeft / 1000}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(30.dp,0.dp,0.dp,30.dp)
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
                    .padding(0.dp,0.dp,30.dp,30.dp)
                    .rotate(-25f)
            )
        }else{
            Text(
                text = "${playerscore}",
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(0.dp,0.dp,30.dp,30.dp)
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





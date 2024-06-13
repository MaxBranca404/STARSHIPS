package com.example.maccproj

import android.content.Context
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.lifecycle.viewmodel.compose.viewModel
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.maccproj.ui.theme.MACCProjTheme
import com.google.gson.JsonObject
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import com.google.ar.core.Frame
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import androidx.compose.runtime.LaunchedEffect as LaunchedEffect


val viewModel = RetroViewModel()
class MainActivity : ComponentActivity() {
    lateinit var retroViewModel: RetroViewModel
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
            UserPreferences.removeUsername(this@MainActivity)
        }*/

        // Check if userId is already saved
        lifecycleScope.launch {
            val userNameFlow = UserPreferences.getUsername(applicationContext)
            userNameFlow.collect { userName ->
                if (userName.isNullOrEmpty()) {
                    // If no userId, show the registration screen
                    setContent {
                        retroViewModel = viewModel()
                        UserRegistrationScreen { username ->
                            registerUser(username, retroViewModel)
                        }
                    }
                } else {
                    // If username exists, proceed to the main content

                    setContent {

                        retroViewModel = viewModel()

                        MACCProjTheme {
                            val navController = rememberNavController()
                            val buttonMediaPlayer = MediaPlayer.create(LocalContext.current,R.raw.buttoncut) //imposta audio click tasti menu

                            NavHost(navController, startDestination = "menu") {
                                composable("menu") { MenuScreen(userName, navController, buttonMediaPlayer) }
                                composable("highscore") { HighscoreScreen(userName, navController, buttonMediaPlayer, retroViewModel) }
                                composable("arscreen") { ARScreen(userName, navController, buttonMediaPlayer, retroViewModel) }
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

    private fun registerUser(username: String, retroViewModel: RetroViewModel) {
        // Prepare the new user data
        val newUser = JsonObject().apply {
            addProperty("username", username)
        }

        retroViewModel.addUser(newUser)

        val state = retroViewModel.retroAddState
        when (state) {
            is RetroState.Loading -> {
            }

            is RetroState.Success -> {
                Log.println(Log.INFO,"ADD","AddUser: tutto ok!")
            }

            is RetroState.Error -> {
            }
        }
        lifecycleScope.launch {
            UserPreferences.saveUsername(applicationContext, username)
            // Now that the username is saved, restart the activity to show the main content
            delay(2000)
            recreate()
        }
    }
}


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
fun MenuScreen(userName: String, navController: NavController, buttonMediaPlayer: MediaPlayer){

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

            Text("User logged in with username: ${userName}",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

        }
    }

}


@Composable
fun HighscoreScreen(userName: String, navController: NavController, buttonMediaPlayer: MediaPlayer, retroViewModel: RetroViewModel, modifier: Modifier = Modifier) {
    val mContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    //var userId by remember { mutableStateOf("Loading...") }
    var score by remember { mutableStateOf("Loading...") }
    var ID by remember { mutableStateOf(0) }

    score = getScore(userName, retroViewModel)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ){
        Column {
            Text(
                text = "Welcome $userName!",
                modifier = modifier
            )
            Text(text = "Your highscore is: ${score}!")
        }
    }
}


@Composable
fun ARScreen(userName: String, navController: NavController, buttonMediaPlayer: MediaPlayer, retroViewModel: RetroViewModel) {

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

    /*val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val model = modelLoader.createModel("model.glb")
    var frame by remember { mutableStateOf<Frame?>(null) }
    val childNodes = rememberNodes()
    val cameraNode = rememberARCameraNode(engine)
    val view = rememberView(engine)
    val collisionSystem = rememberCollisionSystem(view)
    var planeRenderer by remember { mutableStateOf(true) }
    val modelInstances = remember { mutableListOf<ModelInstance>() }
    var modelInstancesShips = remember { mutableListOf<ModelInstance>() }
    var trackingFailureReason by remember {
        mutableStateOf<TrackingFailureReason?>(null)
    }
    var sensorManager: SensorManager? = null
    var accelerometer: Sensor? = null
    var gyroscope: Sensor? = null
    val gyroscopeValues = FloatArray(3)
    var lastTimestamp: Long = 0
    var tiltDetected = false
    val context = LocalContext.current
    sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)


    ARScene(
        modifier = Modifier.fillMaxSize(),
        childNodes = childNodes,
        engine = engine,
        view = view,
        modelLoader = modelLoader,
        collisionSystem = collisionSystem,
        sessionConfiguration = { session, config ->
            config.depthMode =
                when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
            config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
            config.lightEstimationMode =
                Config.LightEstimationMode.ENVIRONMENTAL_HDR
        },
        cameraNode = cameraNode,
        planeRenderer = planeRenderer,
        onTrackingFailureChanged = {
            trackingFailureReason = it
        },
        onSessionCreated = { session ->

           var shipNode = ModelNode(
                modelInstance = modelInstancesShips.apply {
                    if (isEmpty()) {
                        //inserici il path del modello!!!!!!
                        this += modelLoader.createInstancedModel(kModelFile_Rod, 2)
                            /*
                            .apply{
                            val randomX = random.nextFloat() * 2 - 1 // Random number between -1 and 1
                            val randomY = random.nextFloat() * 2 - 1 // Random number between -1 and 1
                            val randomZ = random.nextFloat() * 2 - 1 // Random number between -1 and 1
                            position = Position(randomX,randoY,randomZ)
                        }*/
                    }
                }.removeLast(),
                // Scale to fit in a 0.5 meters cube
                scaleToUnits = 1.0f

            )
            //val anchornode = AnchorNode(engine = engine, anchor = anchor)
            //anchornode.addChildNode(rodNode)
            childNodes += shipNode

        },
        onSessionUpdated = { session, updatedFrame ->
            }

    )*/

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
        val maxScore = getScore(userName, retroViewModel)
        if(playerscore>maxScore.toInt()){
            Log.println(Log.INFO,"NEWSCORE","NewScore>OldScore: caricamento in corso!")
            updateScore(userName,playerscore,retroViewModel)
        }else{
            Log.println(Log.INFO,"OLDSCORE","NewScore<OldScore: mantengo punteggio precedente!")
        }
        //updateScore(userName,playerscore,retroViewModel)
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




/*
fun extractUserId(input: String): String? {
    val regex = """userid":(\d+)""".toRegex()
    val matchResult = regex.find(input)
    return matchResult?.groups?.get(1)?.value
}*/

fun getScore(username: String, retroViewModel: RetroViewModel): String {
    var userMaxScore = ""
    retroViewModel.getScore(username)

    val state = retroViewModel.retroScoreState
    when (state) {
        is RetroState.Loading -> {
        }

        is RetroState.Success -> {
            Log.println(Log.INFO,"GET","GetScore: tutto ok!")
            val data = state.data.get("score")
            userMaxScore = data.toString()
        }

        is RetroState.Error -> {
        }
    }
    return userMaxScore
}

fun updateScore(username: String, score: Int, retroViewModel: RetroViewModel) {
    val newScore = JsonObject().apply {
        addProperty("username", username)
        addProperty("score", score)
    }

    retroViewModel.updateScore(newScore)

    val state = retroViewModel.retroUpdState
    when (state) {
        is RetroState.Loading -> {
        }

        is RetroState.Success -> {
            Log.println(Log.INFO,"UPD","UpdateScore: tutto ok!")
        }

        is RetroState.Error -> {
        }
    }
}

/*
fun updateUserMaxScore(name: Int, score: Int, retroViewModel: RetroViewModel) {
    val newScore = JsonObject().apply {
        addProperty("username", name)
        addProperty("maxscore", score)
    }

    retroViewModel.updateUserMaxScore(newScore)

    val state = retroViewModel.retroUiState
    when (state) {
        is RetroUiState.Loading -> {
        }

        is RetroUiState.Success -> {
        }

        is RetroUiState.Error -> {
        }
    }


}*/




package com.example.maccproj

import android.content.Context
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.graphics.Point
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
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
import io.github.sceneview.rememberEngine
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEnvironmentLoader
import java.util.Random
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.CountDownTimer
import androidx.compose.runtime.*
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberView
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

val viewModel = RetroViewModel()
var ship_path = "models/ship.glb"
// Data class to hold bullet and its initial forward direction
data class Bullet(val node: ModelNode, val direction: Float3)
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
                    Text("START",fontWeight = FontWeight.Bold, color = Color.White)
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
                    Text("HIGHSCORE",fontWeight = FontWeight.Bold, color = Color.White)
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

data class ScoreEntry(val username: String, val score: Int, val date: String)

@Composable
fun HighscoreScreen(
    userName: String,
    navController: NavController,
    buttonMediaPlayer: MediaPlayer,
    retroViewModel: RetroViewModel,
    modifier: Modifier = Modifier
) {
    val mContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var score by remember { mutableStateOf("Loading...") }
    val listScore = mutableListOf<ScoreEntry>()

    // Fetch and parse all scores
    val allScoreJson = getAllScore(retroViewModel)
    allScoreJson.forEach { scoreJson ->
        listScore.add(
            ScoreEntry(
                username = scoreJson.get("username").asString,
                score = scoreJson.get("maxscore").asInt,
                date = formatDate(scoreJson.get("date").asString)
            )
        )
    }

    // Sort listScore by score in descending order
    val sortedListScore = listScore.sortedByDescending { it.score }.take(10)  // Limit to top 10

    // Fetch user score
    score = getScore(userName, retroViewModel)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        // Background and overlay
        Box(modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.scoreback),
                contentScale = ContentScale.FillBounds
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Fixed header
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "TOP 10",
                            modifier = Modifier,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 35.sp
                        )
                        Text(
                            text = "Welcome $userName!",
                            modifier = Modifier,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Your highscore is: $score!",
                            modifier = Modifier,
                            color = Color.White,
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Scrollable content
                    LazyColumn(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        item {


                            // Display table header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(100.dp, 15.dp, 15.dp, 15.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text(text = "Position", modifier = Modifier.weight(1f), color = Color.White)
                                Text(text = "Username", modifier = Modifier.weight(1f), color = Color.White)
                                Text(text = "Score", modifier = Modifier.weight(1f), color = Color.White)
                                Text(text = "Date", modifier = Modifier.weight(1f), color = Color.White)
                            }
                        }

                        // Display score entries with position
                        itemsIndexed(sortedListScore) { index, entry ->
                            val backgroundColor = when (index) {
                                0 -> Color(0xFFFFD700).copy(alpha = 0.5f)  // Gold
                                1 -> Color(0xFFC0C0C0).copy(alpha = 0.5f)  // Silver
                                2 -> Color(0xFFCD7F32).copy(alpha = 0.5f)  // Bronze
                                else -> Color.Transparent
                            }
                            val textWeight = if (index in 0..2) FontWeight.Bold else FontWeight.Normal

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(backgroundColor)
                                    .padding(100.dp, 15.dp, 15.dp, 15.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text(text = (index + 1).toString(), modifier = Modifier.weight(1f), color = Color.White, fontWeight = textWeight)
                                Text(text = entry.username, modifier = Modifier.weight(1f), color = Color.White, fontWeight = textWeight)
                                Text(text = entry.score.toString(), modifier = Modifier.weight(1f), color = Color.White, fontWeight = textWeight)
                                Text(text = entry.date, modifier = Modifier.weight(1f), color = Color.White, fontWeight = textWeight)
                            }
                        }

                        item {
                            Text(text = "----------------", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}




fun formatDate(inputDate: String): String {
    // Define the input and output date formats
    val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

    // Parse the input date string to a Date object
    val date = inputFormat.parse(inputDate)

    // Format the Date object to the desired output format
    return outputFormat.format(date)
}




@Composable
fun ARScreen(userName: String, navController: NavController, buttonMediaPlayer: MediaPlayer, retroViewModel: RetroViewModel) {

    val mContext = LocalContext.current
    val laserMediaPlayer = MediaPlayer.create(mContext, R.raw.laser)
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)

    val cameraNode = rememberCameraNode(engine).apply {
        position = Position(x = 0.0f, y = 0.0f ,z = 0.0f)
    }

    // Add objects relative to the anchor node with random positions between (∣5∣,∣5∣,∣5∣) and (∣15∣,∣15∣,∣15∣)
    val listObjects = rememberNodes {
        for (i in 1..10) {
            val randomPosition = Position(
                x = Random().nextFloat() * 20 + (10 * (if (Random().nextBoolean()) 1 else -1)),
                y = Random().nextFloat() * 20 + (10 * (if (Random().nextBoolean()) 1 else -1)),
                z = Random().nextFloat() * 20 + (10 * (if (Random().nextBoolean()) 1 else -1))
            )
            add(ModelNode(
                modelLoader.createModelInstance("models/ship_1.glb")).apply {
                position = randomPosition
                rotation = Float3(0.0f, 0.0f, .0f)
                scale = Float3(0.4f,0.4f,0.4f)
                lookAt(cameraNode)
            })
        }
    }

    val gyroscopeRotation by rememberGyroscopeRotation()

    //INIZIO COMMENTO GUI
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



    // List to hold bullets
    val bullets = remember { mutableStateListOf<Bullet>() }

    // Function to shoot bullet
    fun shootBullet() {
        val forwardDirection = cameraNode.forwardDirection
        val bulletNode = ModelNode(modelLoader.createModelInstance("models/laser1.glb")).apply {
            position = cameraNode.position
            scale = Float3(2.0f, 2.0f, 2.0f)
            rotation = cameraNode.rotation
        }
        bullets.add(Bullet(bulletNode,forwardDirection))
        listObjects.add(bulletNode)
    }

    // Extension function to calculate distance between two positions
    fun Position.distanceTo(other: Position): Float {
        return sqrt(
            (this.x - other.x).pow(2) +
                    (this.y - other.y).pow(2) +
                    (this.z - other.z).pow(2)
        )
    }



    Scene(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        modelLoader = modelLoader,
        cameraNode = cameraNode,
        childNodes = listObjects,
        environment = environmentLoader.createHDREnvironment(
            assetFileLocation = "environments/sky_2k.hdr"
        )!!,
        onFrame = {
            // Update the camera's rotation based on gyroscope data
            cameraNode.rotation = gyroscopeRotation
            listObjects.forEach { node ->
                //node.position += Float3(0.1f,0.0f,0.0f)
            }
            // Update bullets positions
            val iterator = bullets.iterator()
            while (iterator.hasNext()) {
                val bullet = iterator.next()
                bullet.node.position += bullet.direction * 0.2f

                // Check distance from the camera to remove bullet if it exceeds the max distance
                if (bullet.node.position.distanceTo(cameraNode.position) > 10f) {
                    Log.println(Log.INFO,"RMV-LASER","Laser removed")
                    listObjects.remove(bullet.node)
                    iterator.remove()
                }
            }
        }
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
                shootBullet()
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

    //Vanno reimplementati i box del countdown e del punteggio, perchè il loro update a schermo fa laggare il gioco

    // Box for countdown
    /*Box(
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
    }*/




    // Open the popup menu when the countdown ends
    if (showPopup) {
        val maxScore = getScore(userName, retroViewModel)
        var maxScoreInt = 0
        if (!maxScore.equals("")) {
            maxScoreInt = maxScore.toInt()
        }
        if(playerscore>maxScoreInt){
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
    //FINE COMMENTO GUI


    val view = rememberView(engine)
    val collisionSystem = rememberCollisionSystem(view)













    /*--------Mik code
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    //val model = modelLoader.createModel("model.glb")
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

            var anchorNode = ModelNode(
                modelInstance = modelInstancesShips.apply {
                    if (isEmpty()) {
                        //inserici il path del modello!!!!!!
                        this += modelLoader.createInstancedModel(ship_path, 1)
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
                scaleToUnits = 0.2f

            )
            //val anchornode = AnchorNode(engine = engine, anchor = anchor)
            //anchornode.addChildNode(rodNode)
            childNodes += shipNode

        },
        onSessionUpdated = { session, updatedFrame ->

            frame = updatedFrame

        }

    )
    --------Mik code*/




//---------------TEST OF ARSCENE
    /*
    // The destroy calls are automatically made when their disposable effect leaves
    // the composition or its key changes.
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val cameraNode = rememberARCameraNode(engine)
    val childNodes = rememberNodes()
    val view = rememberView(engine)
    val collisionSystem = rememberCollisionSystem(view)

    var planeRenderer by remember { mutableStateOf(true) }

    var trackingFailureReason by remember {
        mutableStateOf<TrackingFailureReason?>(null)
    }
    var frame by remember { mutableStateOf<Frame?>(null) }

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
        onSessionUpdated = { session, updatedFrame ->
            frame = updatedFrame

            if (childNodes.isEmpty()) {
                updatedFrame.getUpdatedPlanes()
                    .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                    ?.let { it.createAnchorOrNull(it.centerPose) }?.let { anchor ->
                        childNodes += createAnchorNode(
                            engine = engine,
                            modelLoader = modelLoader,
                            materialLoader = materialLoader,
                            anchor = anchor
                        )
                    }
            }
        },
        onGestureListener = rememberOnGestureListener(
            onSingleTapConfirmed = { motionEvent, node ->
                if (node == null) {
                    val hitResults = frame?.hitTest(motionEvent.x, motionEvent.y)
                    hitResults?.firstOrNull {
                        it.isValid(
                            depthPoint = false,
                            point = false
                        )
                    }?.createAnchorOrNull()
                        ?.let { anchor ->
                            planeRenderer = false
                            childNodes += createAnchorNode(
                                engine = engine,
                                modelLoader = modelLoader,
                                materialLoader = materialLoader,
                                anchor = anchor
                            )
                        }
                }
            })

    )
    Text(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxWidth()
            .padding(top = 16.dp, start = 32.dp, end = 32.dp),
        textAlign = TextAlign.Center,
        fontSize = 28.sp,
        color = Color.White,
        text = trackingFailureReason?.let {
            it.getDescription(LocalContext.current)
        } ?: if (childNodes.isEmpty()) {
            stringResource(R.string.point_your_phone_down)
        } else {
            stringResource(R.string.tap_anywhere_to_add_model)
        }
    )*/

//---------------END TEST

}





@Composable
fun rememberGyroscopeRotation(): State<Rotation> {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    val rotationState = remember { mutableStateOf(Rotation()) }

    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    val rotationY = event.values[0]*5/100
                    val rotationX = -event.values[1]*5/100
                    //val rotationZ = event.values[2]

                    // Convert radians to degrees
                    val deltaRotationX = rotationX * 180 / PI.toFloat()
                    val deltaRotationY = rotationY * 180 / PI.toFloat()
                    //val deltaRotationZ = rotationZ * 180 / PI.toFloat()

                    // Update rotation state
                    rotationState.value = Rotation(
                        x = rotationState.value.x + deltaRotationX,
                        y = rotationState.value.y + deltaRotationY,
                        //z = rotationState.value.z + deltaRotationZ
                    )
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(sensorManager, gyroscope) {
        sensorManager.registerListener(sensorEventListener, gyroscope, SensorManager.SENSOR_DELAY_GAME)
        onDispose {
            sensorManager.unregisterListener(sensorEventListener, gyroscope)
        }
    }

    return rotationState
}














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

fun getAllScore(retroViewModel: RetroViewModel): List<JsonObject> {
    retroViewModel.getAllScore()
    Log.println(Log.INFO,"GETALL","GetAllScore: tutto ok!")
    return retroViewModel.retroAllScoreState
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


